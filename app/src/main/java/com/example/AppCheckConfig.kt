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
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

/**
 * Initializes Firebase App Check before any Firestore or Auth calls.
 * - Release/production builds (`!BuildConfig.DEBUG`): Installs [PlayIntegrityAppCheckProviderFactory]
 *   to attest app authenticity and device integrity via Google Play Integrity API, with automatic
 *   token refresh enabled (`setTokenAutoRefreshEnabled(true)`).
 * - Debug/emulator builds (`BuildConfig.DEBUG`): Installs [DebugAppCheckProviderFactory] and
 *   synchronously persists any `FIREBASE_APPCHECK_DEBUG_TOKEN` supplied via the launch Intent.
 */
fun configureAppCheck(
  context: Context,
  intent: Intent?,
  forcePlayIntegrity: Boolean = false
) {
  try {
    // 1. Ensure FirebaseApp is initialized
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    val firebaseAppCheck = FirebaseAppCheck.getInstance()
    firebaseAppCheck.setTokenAutoRefreshEnabled(true)

    if (BuildConfig.DEBUG && !forcePlayIntegrity) {
      // 2. Debug: Read debug token from Intent extra
      val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)

      if (!debugToken.isNullOrEmpty()) {
        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        val prefs = context.getSharedPreferences(
          "$DEBUG_STORE_PREFIX$persistenceKey",
          Context.MODE_PRIVATE
        )
        // CRITICAL: MUST use commit() instead of apply() so the secret is written
        // synchronously before DebugAppCheckProviderFactory reads it.
        prefs.edit()
          .putString(DEBUG_SECRET_KEY, debugToken)
          .commit()
        Log.d(TAG, "App Check debug secret configured successfully.")
      }

      firebaseAppCheck.installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
      )
      Log.d(TAG, "DebugAppCheckProviderFactory installed.")
    } else {
      // 3. Release / Production: Primary attestation via Google Play Integrity API
      try {
        firebaseAppCheck.installAppCheckProviderFactory(
          PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        Log.d(TAG, "PlayIntegrityAppCheckProviderFactory installed.")
      } catch (playIntegrityError: Exception) {
        Log.e(TAG, "PlayIntegrityAppCheckProviderFactory failed; attempting fallback", playIntegrityError)
        // Fallback to reCAPTCHA Enterprise only if Play Integrity cannot initialize
        val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
        if (resId != 0) {
          val siteKey = context.getString(resId)
          if (siteKey.isNotBlank()) {
            firebaseAppCheck.installAppCheckProviderFactory(
              RecaptchaAppCheckProviderFactory.getInstance(siteKey)
            )
          }
        }
      }
    }
  } catch (e: Exception) {
    // Generic error log without exposing tokens or secrets
    Log.e(TAG, "Failed to initialize Firebase App Check.")
  }
}
