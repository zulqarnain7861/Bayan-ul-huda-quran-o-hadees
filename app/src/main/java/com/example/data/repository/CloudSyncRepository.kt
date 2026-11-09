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

  data class CloudDeletionReceipt(
    val userId: String,
    val deletedBookmarksCount: Int,
    val deletedNotesCount: Int,
    val remainingBookmarksCount: Int,
    val remainingNotesCount: Int,
    val verifiedClean: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
  )

  /**
   * Permanently deletes all cloud-synced bookmarks and study notes for the authenticated user
   * under `/users/{userId}/bookmarks` and `/users/{userId}/notes`, then re-queries both
   * collections to verify that 0 documents remain before allowing Firebase Auth account deletion.
   * Throws an exception if any document deletion or post-deletion verification fails.
   */
  suspend fun deleteAllUserDataAndVerify(userId: String): CloudDeletionReceipt {
    require(userId.isNotBlank()) { "userId must not be blank for cloud data deletion" }
    val bookmarksPath = "users/$userId/bookmarks"
    val notesPath = "users/$userId/notes"
    var deletedBookmarks = 0
    var deletedNotes = 0

    try {
      val bookmarkSnap = db.collection(bookmarksPath).get().await()
      for (doc in bookmarkSnap.documents) {
        doc.reference.delete().await()
        deletedBookmarks++
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, bookmarksPath)
    }

    try {
      val notesSnap = db.collection(notesPath).get().await()
      for (doc in notesSnap.documents) {
        doc.reference.delete().await()
        deletedNotes++
      }
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, notesPath)
    }

    val remainingBookmarks: Int = try {
      db.collection(bookmarksPath).get().await().size()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, bookmarksPath)
      throw e
    }
    val remainingNotes: Int = try {
      db.collection(notesPath).get().await().size()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, notesPath)
      throw e
    }

    val verifiedClean = remainingBookmarks == 0 && remainingNotes == 0
    if (!verifiedClean) {
      throw IllegalStateException(
        "Cloud data deletion verification failed for user $userId: remainingBookmarks=$remainingBookmarks, remainingNotes=$remainingNotes"
      )
    }

    return CloudDeletionReceipt(
      userId = userId,
      deletedBookmarksCount = deletedBookmarks,
      deletedNotesCount = deletedNotes,
      remainingBookmarksCount = remainingBookmarks,
      remainingNotesCount = remainingNotes,
      verifiedClean = true
    )
  }

  /**
   * Permanently deletes all cloud-synced bookmarks and study notes for the authenticated user
   * under `/users/{userId}/bookmarks` and `/users/{userId}/notes` and verifies zero remaining records.
   * Required for Google Play User Data & Account Deletion policy compliance.
   */
  suspend fun deleteAllUserData(userId: String): Int {
    if (userId.isBlank()) return 0
    val receipt = deleteAllUserDataAndVerify(userId)
    return receipt.deletedBookmarksCount + receipt.deletedNotesCount
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
