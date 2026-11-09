package com.example

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

// App Check contract keys defined by the Firebase SDK and runtime harness.
const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

object AppCheckConfigState {
  @Volatile
  var lastInstalledProvider: String = "NONE"
    internal set

  @Volatile
  var lastDebugTokenConfigured: Boolean = false
    internal set

  @Volatile
  var lastPersistenceStoreName: String = ""
    internal set
}

fun configureAppCheck(
  context: Context,
  intent: Intent?,
  forcePlayIntegrity: Boolean = false
) {
  try {
    // 1. Ensure FirebaseApp is initialized before accessing persistenceKey or FirebaseAppCheck
    if (FirebaseApp.getApps(context).isEmpty()) {
      runCatching { FirebaseApp.initializeApp(context) }
    }

    val firebaseApp = runCatching { FirebaseApp.getInstance() }.getOrNull()
    val firebaseAppCheck = runCatching {
      if (firebaseApp != null) FirebaseAppCheck.getInstance(firebaseApp) else null
    }.getOrNull()

    if (BuildConfig.DEBUG && !forcePlayIntegrity) {
      // 2. Debug: Read debug token from Intent extra
      val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)?.trim()
      val persistenceKey = runCatching { firebaseApp?.persistenceKey }.getOrNull()
        ?.takeIf { it.isNotBlank() } ?: "[DEFAULT]"
      val storeName = "$DEBUG_STORE_PREFIX$persistenceKey"
      AppCheckConfigState.lastPersistenceStoreName = storeName

      if (!debugToken.isNullOrEmpty()) {
        val prefs = context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
        // CRITICAL: MUST use commit() instead of apply() so the secret is written
        // synchronously before DebugAppCheckProviderFactory reads it.
        prefs.edit()
          .putString(DEBUG_SECRET_KEY, debugToken)
          .commit()
        AppCheckConfigState.lastDebugTokenConfigured = true
        Log.d(TAG, "App Check debug secret configured synchronously.")
      } else {
        AppCheckConfigState.lastDebugTokenConfigured = false
        Log.w(TAG, "No debug token found in Intent extra; using generated/cached debug secret.")
      }

      AppCheckConfigState.lastInstalledProvider = "DebugAppCheckProviderFactory"
      runCatching {
        firebaseAppCheck?.installAppCheckProviderFactory(
          DebugAppCheckProviderFactory.getInstance()
        )
      }
      Log.d(TAG, "DebugAppCheckProviderFactory installed.")
    } else {
      // 3. Release / Production: Look up recaptcha_site_key safely without throwing Resources.NotFoundException.
      // If a real non-placeholder reCAPTCHA Enterprise site key is configured, install RecaptchaAppCheckProviderFactory;
      // otherwise install PlayIntegrityAppCheckProviderFactory so production builds always enforce hardware/Play attestation.
      val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
      val siteKey = if (resId != 0) context.getString(resId).trim() else ""
      val hasConfiguredRecaptchaKey = siteKey.isNotBlank() &&
        !siteKey.startsWith("YOUR_", ignoreCase = true) &&
        !siteKey.equals("PLACEHOLDER", ignoreCase = true)

      if (hasConfiguredRecaptchaKey && !forcePlayIntegrity) {
        AppCheckConfigState.lastInstalledProvider = "RecaptchaAppCheckProviderFactory"
        runCatching {
          firebaseAppCheck?.installAppCheckProviderFactory(
            RecaptchaAppCheckProviderFactory.getInstance(siteKey)
          )
        }
        Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
      } else {
        AppCheckConfigState.lastInstalledProvider = "PlayIntegrityAppCheckProviderFactory"
        runCatching {
          firebaseAppCheck?.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
          )
        }
        Log.d(TAG, "PlayIntegrityAppCheckProviderFactory installed for production attestation.")
      }
    }
  } catch (e: Exception) {
    // Log explicitly to keep App Check attestation failures visible
    Log.e(TAG, "Failed to initialize Firebase App Check", e)
  }
}
