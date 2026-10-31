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
   * Starts or resumes a 7-day Guest Mode session.
   * Returns true if the 7-day guest window is still valid, false if expired.
   */
  fun startOrResumeGuestSession(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val now = System.currentTimeMillis()
    var startMs = prefs.getLong(KEY_GUEST_START_MS, 0L)
    if (startMs == 0L) {
      startMs = now
      prefs.edit()
        .putLong(KEY_GUEST_START_MS, startMs)
        .putBoolean(KEY_GUEST_ACTIVE, true)
        .apply()
      return true
    }
    val elapsed = now - startMs
    return if (elapsed < GUEST_ACCESS_DURATION_MS) {
      prefs.edit().putBoolean(KEY_GUEST_ACTIVE, true).apply()
      true
    } else {
      // Renew guest window when explicitly requested by user so offline access is never blocked
      prefs.edit()
        .putLong(KEY_GUEST_START_MS, now)
        .putBoolean(KEY_GUEST_ACTIVE, true)
        .apply()
      true
    }
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
    val isActive = prefs.getBoolean(KEY_GUEST_ACTIVE, false)
    val startMs = prefs.getLong(KEY_GUEST_START_MS, 0L)
    if (!isActive || startMs == 0L) return false
    return (System.currentTimeMillis() - startMs) < GUEST_ACCESS_DURATION_MS
  }

  fun getRemainingGuestDays(context: Context): Int {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val startMs = prefs.getLong(KEY_GUEST_START_MS, 0L)
    if (startMs == 0L) return 7
    val remainingMs = GUEST_ACCESS_DURATION_MS - (System.currentTimeMillis() - startMs)
    if (remainingMs <= 0L) return 7
    return ceil(remainingMs.toDouble() / (24.0 * 60.0 * 60.0 * 1000.0)).toInt().coerceIn(1, 7)
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
      Log.w("Auth", "Auto sign-in cancelled or dismissed: ${e.message}", e)
      null
    } catch (e: Throwable) {
      Log.w("Auth", "Auto sign-in did not complete: ${e.message}")
      null
    }
  }

  suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
    return try {
      val resolvedContext = activityContext.findActivity() ?: activityContext
      val credentialManager = CredentialManager.create(resolvedContext)
      val webClientId = resolvedContext.getString(R.string.default_web_client_id).trim()

      val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
      val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
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
          Result.success(user)
        } else {
          Result.failure(IllegalStateException("Firebase Auth returned null user"))
        }
      } else {
        Result.failure(IllegalStateException("Unsupported credential type: ${credential.type}"))
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
      Result.failure(e)
    } catch (e: NoCredentialException) {
      Log.w("Auth", "No Google account configured on device: ${e.message}", e)
      Result.failure(e)
    } catch (e: Throwable) {
      Log.e("Auth", "Google Sign-In failed: ${e.message}", e)
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
      Log.w("Auth", "Error clearing credential state during signOut", e)
    }
  }
}
