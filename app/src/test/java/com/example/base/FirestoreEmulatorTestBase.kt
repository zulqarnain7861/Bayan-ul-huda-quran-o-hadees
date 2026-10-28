package com.example.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
abstract class FirestoreEmulatorTestBase {

  protected lateinit var context: Context
  protected lateinit var firebaseApp: FirebaseApp
  protected lateinit var auth: FirebaseAuth
  protected lateinit var firestore: FirebaseFirestore

  protected val projectId = "gen-lang-client-0170738823"
  protected lateinit var databaseId: String

  @Before
  open fun setUpFirebase() {
    context = ApplicationProvider.getApplicationContext()
    databaseId = context.getString(R.string.firestore_database_id)

    FirebaseApp.clearInstancesForTest()

    val options = FirebaseOptions.Builder()
      .setProjectId(projectId)
      .setApplicationId("1:1234567890:android:abcdef")
      .setApiKey("fake-api-key-for-emulator")
      .build()

    firebaseApp = FirebaseApp.initializeApp(context, options)

    auth = FirebaseAuth.getInstance(firebaseApp).apply {
      useEmulator("127.0.0.1", 9099)
    }

    firestore = FirebaseFirestore.getInstance(firebaseApp, databaseId).apply {
      useEmulator("127.0.0.1", 8085)
      firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
        .build()
    }

    clearEmulatorData()
  }

  @After
  open fun tearDownFirebase() {
    if (::firestore.isInitialized) {
      try {
        com.google.android.gms.tasks.Tasks.await(
          firestore.terminate(),
          5,
          java.util.concurrent.TimeUnit.SECONDS
        )
      } catch (_: Exception) {
      }
    }
    FirebaseApp.clearInstancesForTest()
  }

  protected suspend fun signInTestUser(): String {
    val result = auth.signInAnonymously().await()
    return result.user!!.uid
  }

  protected fun signOutTestUser() {
    auth.signOut()
  }

  private fun clearEmulatorData() {
    val endpoint =
      "http://127.0.0.1:8085/emulator/v1/projects/$projectId/databases/$databaseId/documents"
    val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
      requestMethod = "DELETE"
      connectTimeout = 1500
      readTimeout = 2000
    }
    try {
      val code = conn.responseCode
      check(code in 200..299) {
        "Failed to clear Firestore emulator data (HTTP $code) at $endpoint. Is the emulator running?"
      }
    } catch (e: java.net.ConnectException) {
      org.junit.Assume.assumeNoException("Skipping Firestore emulator test because 127.0.0.1:8085 is not running", e)
    } finally {
      conn.disconnect()
    }
  }
}
