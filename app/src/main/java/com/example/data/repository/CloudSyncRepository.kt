package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.remote.CloudBookmark
import com.example.data.remote.CloudNote
import com.example.data.remote.OperationType
import com.example.data.remote.SecurityIntegrityGuard
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
    if (userId.isBlank()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val path = "users/$userId/bookmarks"
    val registration = db.collection(path)
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          try {
            handleFirestoreError(error, OperationType.LIST, path)
          } catch (_: Exception) {
          }
          trySend(emptyList())
          return@addSnapshotListener
        }
        val items = snapshot?.toObjects(CloudBookmark::class.java).orEmpty()
        trySend(items)
      }
    awaitClose { registration.remove() }
  }

  fun getNotesFlow(userId: String): Flow<List<CloudNote>> = callbackFlow {
    if (userId.isBlank()) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }
    val path = "users/$userId/notes"
    val registration = db.collection(path)
      .orderBy("updatedAt", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          try {
            handleFirestoreError(error, OperationType.LIST, path)
          } catch (_: Exception) {
          }
          trySend(emptyList())
          return@addSnapshotListener
        }
        val items = snapshot?.toObjects(CloudNote::class.java).orEmpty()
        trySend(items)
      }
    awaitClose { registration.remove() }
  }

  suspend fun saveBookmark(userId: String, bookmarkId: String, bookmark: CloudBookmark) {
    if (userId.isBlank() || bookmarkId.isBlank()) return
    val path = "users/$userId/bookmarks/$bookmarkId"
    try {
      val existing = runCatching {
        db.document(path).get().await().toObject(CloudBookmark::class.java)
      }.getOrNull()
      val sanitized = bookmark.copy(
        referenceKey = SecurityIntegrityGuard.sanitizeSacredText(bookmark.referenceKey).take(100),
        title = SecurityIntegrityGuard.sanitizeSacredText(bookmark.title).take(199),
        arabicSnippet = SecurityIntegrityGuard.sanitizeSacredText(bookmark.arabicSnippet).take(1999),
        translationSnippet = SecurityIntegrityGuard.sanitizeSacredText(bookmark.translationSnippet).take(1999),
        createdAt = existing?.createdAt ?: bookmark.createdAt
      )
      db.document(path).set(sanitized, SetOptions.merge()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
    }
  }

  suspend fun getBookmark(userId: String, bookmarkId: String): CloudBookmark? {
    if (userId.isBlank() || bookmarkId.isBlank()) return null
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
    if (userId.isBlank() || bookmarkId.isBlank()) return
    val path = "users/$userId/bookmarks/$bookmarkId"
    try {
      db.document(path).delete().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, path)
    }
  }

  suspend fun saveNote(userId: String, noteId: String, note: CloudNote) {
    if (userId.isBlank() || noteId.isBlank()) return
    val path = "users/$userId/notes/$noteId"
    try {
      val existing = runCatching {
        db.document(path).get().await().toObject(CloudNote::class.java)
      }.getOrNull()
      val sanitized = note.copy(
        referenceKey = SecurityIntegrityGuard.sanitizeSacredText(note.referenceKey).take(100),
        title = SecurityIntegrityGuard.sanitizeSacredText(note.title).take(199),
        content = SecurityIntegrityGuard.sanitizeSacredText(note.content).take(5000),
        createdAt = existing?.createdAt ?: note.createdAt
      )
      db.document(path).set(sanitized, SetOptions.merge()).await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
    }
  }

  suspend fun getNote(userId: String, noteId: String): CloudNote? {
    if (userId.isBlank() || noteId.isBlank()) return null
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
    if (userId.isBlank() || noteId.isBlank()) return
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
