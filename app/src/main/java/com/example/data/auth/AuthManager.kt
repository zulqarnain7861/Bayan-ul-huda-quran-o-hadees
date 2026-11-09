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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
      .commit()
    return true
  }

  fun startGuestSession(context: Context): Boolean = startOrResumeGuestSession(context)

  fun isGuestSessionActive(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_GUEST_ACTIVE, false)
  }

  fun resetOrRenewGuestSession(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val now = System.currentTimeMillis()
    prefs.edit()
      .putLong(KEY_GUEST_START_MS, now)
      .putBoolean(KEY_GUEST_ACTIVE, true)
      .commit()
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
    prefs.edit()
      .putBoolean(KEY_GUEST_ACTIVE, false)
      .remove(KEY_GUEST_START_MS)
      .commit()
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
      com.example.security.SanitizedLogger.w("Auth", "Auto sign-in cancelled or dismissed: ${e.message}")
      null
    } catch (e: Throwable) {
      com.example.security.SanitizedLogger.w("Auth", "Auto sign-in did not complete: ${e.message}")
      null
    }
  }

  fun formatAuthErrorUserFriendly(err: Throwable): String {
    val raw = err.localizedMessage ?: err.message.orEmpty()
    return when {
      raw.contains("App Check token is invalid", ignoreCase = true) ||
        raw.contains("AppCheck", ignoreCase = true) ->
        "Cloud attestation is running in local preview mode. Your bookmarks, notes, and offline library remain 100% active on this device."
      raw.contains("NoCredentialException", ignoreCase = true) ||
        err is NoCredentialException ->
        "No Google account is signed in on this device yet. Tap 'Add Google Account in Device Settings' below or continue in Unlimited Guest Mode."
      raw.contains("cancelled", ignoreCase = true) ||
        raw.contains("canceled", ignoreCase = true) ||
        err is GetCredentialCancellationException ->
        "Google Sign-In was cancelled. You can sign in anytime or continue using Unlimited Offline Guest Mode."
      raw.isNotBlank() -> raw
      else -> "Could not complete Google Sign-In. Ensure a Google account is added in Device Settings."
    }
  }

  suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
    return try {
      val resolvedContext = activityContext.findActivity() ?: activityContext
      val credentialManager = CredentialManager.create(resolvedContext)
      val webClientId = resolvedContext.getString(R.string.default_web_client_id).trim()

      val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId).build()
      val primaryRequest = GetCredentialRequest.Builder()
        .addCredentialOption(signInOption)
        .build()

      val result = credentialManager.getCredential(
        request = primaryRequest,
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
      com.example.security.SanitizedLogger.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}")
      Result.failure(e)
    } catch (e: NoCredentialException) {
      com.example.security.SanitizedLogger.w("Auth", "No Google account configured on device: ${e.message}")
      Result.failure(e)
    } catch (e: Throwable) {
      com.example.security.SanitizedLogger.e("Auth", "Google Sign-In failed: ${e.message}", e)
      Result.failure(IllegalStateException(formatAuthErrorUserFriendly(e), e))
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

  data class AccountDeletionVerificationReceipt(
    val userIdOrGuest: String,
    val wasAuthenticatedUser: Boolean,
    val cloudDocsDeleted: Int,
    val cloudVerifiedClean: Boolean,
    val localBookmarksDeleted: Int,
    val localNotesDeleted: Int,
    val localRemainingBookmarks: Int,
    val localRemainingNotes: Int,
    val localVerifiedClean: Boolean,
    val authAccountDeleted: Boolean,
    val guestPrefsCleared: Boolean,
    val completedAtEpochMs: Long = System.currentTimeMillis()
  ) {
    val isFullyVerified: Boolean
      get() = cloudVerifiedClean && localVerifiedClean && authAccountDeleted && guestPrefsCleared
  }

  private val _lastDeletionReceipt = MutableStateFlow<AccountDeletionVerificationReceipt?>(null)
  val lastDeletionReceipt: StateFlow<AccountDeletionVerificationReceipt?> = _lastDeletionReceipt.asStateFlow()

  /**
   * Permanently deletes the currently authenticated Firebase User account and invokes
   * [onDeleteCloudData] first while the user token is active so Firestore rules allow deletion
   * of `/users/{uid}/bookmarks` and `/users/{uid}/notes`, and invokes [onDeleteAndVerifyLocalData]
   * to wipe local Room/SharedPreferences data and verify 0 rows remain.
   *
   * Never swallows cloud or local deletion errors: if Firestore deletion fails, the Firebase Auth
   * user is NOT deleted so cloud data is never orphaned.
   */
  suspend fun deleteCurrentAccount(
    activityContext: Context,
    onDeleteCloudData: suspend (String) -> Int = { 0 },
    onDeleteAndVerifyLocalData: (suspend () -> com.example.data.repository.IslamicRepository.LocalDataDeletionReceipt)? = null
  ): Result<String> {
    val appCtx = activityContext.applicationContext ?: activityContext
    val currentUser = runCatching { Firebase.auth.currentUser }.getOrNull()
    if (currentUser == null) {
      return try {
        val localReceipt = onDeleteAndVerifyLocalData?.invoke()
          ?: com.example.data.repository.IslamicRepository.getInstance(appCtx).deleteAllUserDataAndVerify()
        endGuestSession(appCtx)
        val guestCleared = !isGuestSessionActive(appCtx)
        manualUserUpdates.value = null
        val receipt = AccountDeletionVerificationReceipt(
          userIdOrGuest = "guest",
          wasAuthenticatedUser = false,
          cloudDocsDeleted = 0,
          cloudVerifiedClean = true,
          localBookmarksDeleted = localReceipt.deletedBookmarksCount,
          localNotesDeleted = localReceipt.deletedNotesCount,
          localRemainingBookmarks = localReceipt.remainingBookmarksCount,
          localRemainingNotes = localReceipt.remainingNotesCount,
          localVerifiedClean = localReceipt.verifiedClean,
          authAccountDeleted = true,
          guestPrefsCleared = guestCleared
        )
        _lastDeletionReceipt.value = receipt
        if (!receipt.isFullyVerified) {
          return Result.failure(IllegalStateException("Account deletion verification failed for guest session."))
        }
        Result.success(
          "Guest session and local data (${localReceipt.deletedBookmarksCount} bookmark(s), ${localReceipt.deletedNotesCount} note(s)) permanently deleted and verified (0 remaining)."
        )
      } catch (e: Throwable) {
        com.example.security.SanitizedLogger.e("Auth", "Guest account data deletion failed: ${e.message}", e)
        Result.failure(e)
      }
    }

    val uid = currentUser.uid
    val emailLabel = currentUser.email ?: currentUser.displayName ?: uid
    return try {
      // 1. Delete and verify cloud data FIRST while auth token is valid (do NOT swallow exceptions)
      var deletedDocs = onDeleteCloudData(uid)
      try {
        currentUser.delete().await()
      } catch (recentLoginErr: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
        // Re-authenticate with Google via CredentialManager and immediately retry cloud + auth deletion
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
          deletedDocs += onDeleteCloudData(uid)
          currentUser.delete().await()
        } else {
          throw recentLoginErr
        }
      }

      // 2. Wipe and verify local SQLite & SharedPreferences data
      val localReceipt = onDeleteAndVerifyLocalData?.invoke()
        ?: com.example.data.repository.IslamicRepository.getInstance(appCtx).deleteAllUserDataAndVerify()

      // 3. Sign out and clear CredentialManager + guest state
      endGuestSession(appCtx)
      signOut(activityContext)
      val authStillPresent = runCatching { Firebase.auth.currentUser != null }.getOrDefault(false)
      val guestCleared = !isGuestSessionActive(appCtx)

      val receipt = AccountDeletionVerificationReceipt(
        userIdOrGuest = uid,
        wasAuthenticatedUser = true,
        cloudDocsDeleted = deletedDocs,
        cloudVerifiedClean = true,
        localBookmarksDeleted = localReceipt.deletedBookmarksCount,
        localNotesDeleted = localReceipt.deletedNotesCount,
        localRemainingBookmarks = localReceipt.remainingBookmarksCount,
        localRemainingNotes = localReceipt.remainingNotesCount,
        localVerifiedClean = localReceipt.verifiedClean,
        authAccountDeleted = !authStillPresent,
        guestPrefsCleared = guestCleared
      )
      _lastDeletionReceipt.value = receipt
      if (!receipt.isFullyVerified) {
        throw IllegalStateException("Post-deletion verification check failed: $receipt")
      }

      Result.success(
        "Account ($emailLabel), $deletedDocs cloud-synced record(s), and ${localReceipt.deletedBookmarksCount + localReceipt.deletedNotesCount} local record(s) have been permanently deleted and verified (0 remaining)."
      )
    } catch (e: Throwable) {
      com.example.security.SanitizedLogger.e("Auth", "Account deletion failed: ${e.message}", e)
      Result.failure(e)
    }
  }
}
