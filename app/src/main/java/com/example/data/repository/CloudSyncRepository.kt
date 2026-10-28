package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.remote.CloudBookmark
import com.example.data.remote.CloudNote
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CloudSyncRepository(
  private val db: FirebaseFirestore
) {

  fun getBookmarksFlow(userId: String): Flow<List<CloudBookmark>> = callbackFlow {
    val path = "users/$userId/bookmarks"
    val registration = db.collection(path)
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          try {
            handleFirestoreError(error, OperationType.LIST, path)
          } catch (_: Exception) {
          }
          close(error)
          return@addSnapshotListener
        }
        val items = snapshot?.toObjects(CloudBookmark::class.java).orEmpty()
        trySend(items)
      }
    awaitClose { registration.remove() }
  }

  fun getNotesFlow(userId: String): Flow<List<CloudNote>> = callbackFlow {
    val path = "users/$userId/notes"
    val registration = db.collection(path)
      .orderBy("updatedAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          try {
            handleFirestoreError(error, OperationType.LIST, path)
          } catch (_: Exception) {
          }
          close(error)
          return@addSnapshotListener
        }
        val items = snapshot?.toObjects(CloudNote::class.java).orEmpty()
        trySend(items)
      }
    awaitClose { registration.remove() }
  }

  suspend fun saveBookmark(userId: String, bookmarkId: String, bookmark: CloudBookmark) {
    val path = "users/$userId/bookmarks/$bookmarkId"
    try {
      db.document(path).set(bookmark, SetOptions.merge()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
    }
  }

  suspend fun getBookmark(userId: String, bookmarkId: String): CloudBookmark? {
    val path = "users/$userId/bookmarks/$bookmarkId"
    return try {
      val snapshot = db.document(path).get().await()
      snapshot.toObject(CloudBookmark::class.java)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, path)
      null
    }
  }

  suspend fun deleteBookmark(userId: String, bookmarkId: String) {
    val path = "users/$userId/bookmarks/$bookmarkId"
    try {
      db.document(path).delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
    }
  }

  suspend fun saveNote(userId: String, noteId: String, note: CloudNote) {
    val path = "users/$userId/notes/$noteId"
    try {
      db.document(path).set(note, SetOptions.merge()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
    }
  }

  suspend fun getNote(userId: String, noteId: String): CloudNote? {
    val path = "users/$userId/notes/$noteId"
    return try {
      val snapshot = db.document(path).get().await()
      snapshot.toObject(CloudNote::class.java)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, path)
      null
    }
  }

  suspend fun deleteNote(userId: String, noteId: String) {
    val path = "users/$userId/notes/$noteId"
    try {
      db.document(path).delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: CloudSyncRepository? = null

    fun getInstance(context: Context): CloudSyncRepository {
      return INSTANCE ?: synchronized(this) {
        val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
        val firestore = FirebaseFirestore.getInstance(databaseId)
        val instance = CloudSyncRepository(firestore)
        INSTANCE = instance
        instance
      }
    }
  }
}
