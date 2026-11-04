package com.example

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.FirebaseApp

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val TOKEN_STORE_PREFIX = "com.google.firebase.appcheck.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

/**
 * Ensures [FirebaseApp] is initialized before any Firestore or Auth calls and purges any stale
 * unregistered App Check debug tokens from SharedPreferences so that Google Sign-In
 * (`Firebase.auth.signInWithCredential`) never fails with
 * `An internal error has occurred. [ Firebase App Check token is invalid. ]`.
 */
fun configureAppCheck(
  context: Context,
  intent: Intent?,
  forcePlayIntegrity: Boolean = false
) {
  try {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    val persistenceKey = runCatching { FirebaseApp.getInstance().persistenceKey }.getOrNull()
    if (!persistenceKey.isNullOrEmpty()) {
      val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
      val debugPrefs = context.getSharedPreferences(
        "$DEBUG_STORE_PREFIX$persistenceKey",
        Context.MODE_PRIVATE
      )
      if (!debugToken.isNullOrEmpty() && !forcePlayIntegrity) {
        debugPrefs.edit().putString(DEBUG_SECRET_KEY, debugToken).commit()
      } else {
        // Clear any unregistered local debug secret and cached invalid App Check tokens
        debugPrefs.edit().clear().commit()
        context.getSharedPreferences(
          "$TOKEN_STORE_PREFIX$persistenceKey",
          Context.MODE_PRIVATE
        ).edit().clear().commit()
      }
    }
  } catch (e: Exception) {
    Log.e(TAG, "Failed to initialize Firebase runtime configuration.")
  }
}

