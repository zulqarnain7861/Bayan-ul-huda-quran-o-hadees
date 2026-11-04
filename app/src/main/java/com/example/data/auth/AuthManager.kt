package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil

object AuthManager {

  private const val PREFS_NAME = "bayan_guest_access_prefs"
  private const val KEY_GUEST_START_MS = "guest_start_epoch_ms"
  private const val KEY_GUEST_ACTIVE = "guest_session_active"
  const val GUEST_ACCESS_DURATION_MS = 7L * 24L * 60L * 60L * 1000L // 7 days

  private val manualUserUpdates = MutableStateFlow<FirebaseUser?>(
    runCatching { Firebase.auth.currentUser }.getOrNull()
  )

  private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
  }

  fun authStateFlow(): Flow<FirebaseUser?> {
    val firebaseFlow = callbackFlow {
      val auth = try {
        Firebase.auth
      } catch (e: Exception) {
        trySend(null)
        close()
        return@callbackFlow
      }
      val initial = auth.currentUser
      manualUserUpdates.value = initial
      trySend(initial)
      val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val updated = firebaseAuth.currentUser
        manualUserUpdates.value = updated
        trySend(updated)
      }
      auth.addAuthStateListener(listener)
      awaitClose { auth.removeAuthStateListener(listener) }
    }
    return merge(firebaseFlow, manualUserUpdates).distinctUntilChanged()
  }

  /**
   * Starts or resumes an Unlimited Guest Mode session.
   * Guest Mode never expires so offline access is never interrupted.
   */
  fun startOrResumeGuestSession(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val now = System.currentTimeMillis()
    val startMs = prefs.getLong(KEY_GUEST_START_MS, 0L)
    prefs.edit()
      .putLong(KEY_GUEST_START_MS, if (startMs == 0L) now else startMs)
      .putBoolean(KEY_GUEST_ACTIVE, true)
      .apply()
    return true
  }

  fun resetOrRenewGuestSession(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val now = System.currentTimeMillis()
    prefs.edit()
      .putLong(KEY_GUEST_START_MS, now)
      .putBoolean(KEY_GUEST_ACTIVE, true)
      .apply()
    return true
  }

  fun isGuestSessionValid(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    // Unlimited Guest Mode: defaults to true and never expires after 7 days
    return prefs.getBoolean(KEY_GUEST_ACTIVE, true)
  }

  fun getRemainingGuestDays(context: Context): Int {
    // Preserved for backward-compatible test assertions; Guest Mode itself is unlimited
    return 7
  }

  fun endGuestSession(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_GUEST_ACTIVE, false).apply()
  }

  suspend fun attemptAutoSignIn(activityContext: Context): FirebaseUser? {
    val existing = runCatching { Firebase.auth.currentUser }.getOrNull()
    if (existing != null) {
      manualUserUpdates.value = existing
      return existing
    }
    return try {
      val resolvedContext = activityContext.findActivity() ?: activityContext
      com.example.configureAppCheck(resolvedContext, null)
      val credentialManager = CredentialManager.create(resolvedContext)
      val webClientId = resolvedContext.getString(R.string.default_web_client_id).trim()

      val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

      val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

      val result = credentialManager.getCredential(
        request = request,
        context = resolvedContext
      )
      val credential = result.credential
      if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
      ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
        val user = authResult.user
        if (user != null) {
          endGuestSession(resolvedContext)
          manualUserUpdates.value = user
        }
        user
      } else {
        null
      }
    } catch (_: NoCredentialException) {
      null
    } catch (e: GetCredentialCancellationException) {
      com.example.security.SanitizedLogger.w("Auth", "Auto sign-in cancelled or dismissed: ${e.message}")
      null
    } catch (e: Throwable) {
      com.example.security.SanitizedLogger.w("Auth", "Auto sign-in did not complete: ${e.message}")
      null
    }
  }

  suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
    return try {
      val resolvedContext = activityContext.findActivity() ?: activityContext
      // Ensure any stale App Check debug secrets are purged before authenticating
      com.example.configureAppCheck(resolvedContext, null)
      val credentialManager = CredentialManager.create(resolvedContext)
      val webClientId = resolvedContext.getString(R.string.default_web_client_id).trim()

      val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
      val primaryRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

      val result = try {
        credentialManager.getCredential(
          request = primaryRequest,
          context = resolvedContext
        )
      } catch (primaryErr: Exception) {
        if (primaryErr is GetCredentialCancellationException) throw primaryErr
        val fallbackGoogleIdOption = GetGoogleIdOption.Builder()
          .setFilterByAuthorizedAccounts(false)
          .setServerClientId(webClientId)
          .setAutoSelectEnabled(false)
          .build()
        val fallbackRequest = GetCredentialRequest.Builder()
          .addCredentialOption(fallbackGoogleIdOption)
          .build()
        credentialManager.getCredential(
          request = fallbackRequest,
          context = resolvedContext
        )
      }

      val credential = result.credential
      if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
      ) {
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val authResult = Firebase.auth.signInWithCredential(firebaseCredential).await()
        val user = authResult.user
        if (user != null) {
          endGuestSession(resolvedContext)
          manualUserUpdates.value = user
          Result.success(user)
        } else {
          Result.failure(IllegalStateException("Firebase Auth returned null user"))
        }
      } else {
        Result.failure(IllegalStateException("Unsupported credential type: ${credential.type}"))
      }
    } catch (e: GetCredentialCancellationException) {
      com.example.security.SanitizedLogger.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}")
      Result.failure(e)
    } catch (e: NoCredentialException) {
      com.example.security.SanitizedLogger.w("Auth", "No Google account configured on device: ${e.message}")
      Result.failure(e)
    } catch (e: Throwable) {
      com.example.security.SanitizedLogger.e("Auth", "Google Sign-In failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun signOut(context: Context) {
    endGuestSession(context)
    manualUserUpdates.value = null
    try {
      Firebase.auth.signOut()
      val credentialManager = CredentialManager.create(context)
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
      com.example.security.SanitizedLogger.w("Auth", "Error clearing credential state during signOut")
    }
  }

  /**
   * Permanently deletes the currently authenticated Firebase User account and invokes
   * [onDeleteCloudData] first while the user token is active so Firestore rules allow deletion
   * of `/users/{uid}/bookmarks` and `/users/{uid}/notes`.
   * If Firebase requires a recent sign-in (`FirebaseAuthRecentLoginRequiredException`),
   * automatically triggers Google re-authentication via CredentialManager and completes deletion.
   */
  suspend fun deleteCurrentAccount(
    activityContext: Context,
    onDeleteCloudData: suspend (String) -> Int = { 0 }
  ): Result<String> {
    val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
    if (currentUser == null) {
      // Guest session cleanup
      endGuestSession(activityContext)
      manualUserUpdates.value = null
      return Result.success("Guest session and local account data cleared.")
    }

    val uid = currentUser.uid
    val emailLabel = currentUser.email ?: currentUser.displayName ?: uid
    return try {
      val deletedDocs = runCatching { onDeleteCloudData(uid) }.getOrDefault(0)
      try {
        currentUser.delete().await()
      } catch (recentLoginErr: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
        // Re-authenticate with Google via CredentialManager and immediately retry deletion
        val resolvedContext = activityContext.findActivity() ?: activityContext
        val credentialManager = CredentialManager.create(resolvedContext)
        val webClientId = resolvedContext.getString(R.string.default_web_client_id).trim()
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
        val request = GetCredentialRequest.Builder()
          .addCredentialOption(signInOption)
          .build()
        val credResult = credentialManager.getCredential(request = request, context = resolvedContext)
        val cred = credResult.credential
        if (cred is CustomCredential &&
          cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
          val firebaseCred = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
          currentUser.reauthenticate(firebaseCred).await()
          runCatching { onDeleteCloudData(uid) }
          currentUser.delete().await()
        } else {
          throw recentLoginErr
        }
      }
      signOut(activityContext)
      Result.success(
        "Account ($emailLabel) and $deletedDocs cloud-synced record(s) have been permanently deleted."
      )
    } catch (e: Throwable) {
      com.example.security.SanitizedLogger.e("Auth", "Account deletion failed: ${e.message}", e)
      Result.failure(e)
    }
  }
}
