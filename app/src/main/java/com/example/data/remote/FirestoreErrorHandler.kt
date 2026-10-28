package com.example.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

enum class OperationType {
  CREATE,
  UPDATE,
  DELETE,
  LIST,
  GET,
  WRITE
}

fun handleFirestoreError(
  error: Exception,
  operationType: OperationType,
  path: String?
) {
  val auth = try {
    FirebaseAuth.getInstance()
  } catch (e: Exception) {
    null
  }
  val currentUser = auth?.currentUser

  val providerArray = org.json.JSONArray()
  currentUser?.providerData?.forEach { userInfo ->
    providerArray.put(
      JSONObject().apply {
        put("providerId", userInfo.providerId)
        put("displayName", "[REDACTED]")
        put("email", "[REDACTED]")
        put("photoUrl", JSONObject.NULL)
      }
    )
  }

  val maskedUid = currentUser?.uid?.let { uid ->
    if (uid.length > 6) "${uid.take(3)}***${uid.takeLast(3)}" else "***"
  }

  val authInfo = JSONObject().apply {
    put("userId", maskedUid ?: JSONObject.NULL)
    put("email", if (currentUser?.email != null) "[REDACTED]" else JSONObject.NULL)
    put("emailVerified", currentUser?.isEmailVerified ?: JSONObject.NULL)
    put("isAnonymous", currentUser?.isAnonymous ?: JSONObject.NULL)
    put("tenantId", currentUser?.tenantId ?: JSONObject.NULL)
    put("providerInfo", providerArray)
  }

  val errInfo = JSONObject().apply {
    put("error", error::class.java.simpleName)
    put("operationType", operationType.name.lowercase())
    put("path", path?.replace(Regex("users/[^/]+"), "users/[REDACTED]") ?: JSONObject.NULL)
    put("authInfo", authInfo)
  }

  Log.e("FirestoreError", errInfo.toString())
  throw error
}
