package com.example.data.remote

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CloudBookmark(
  @DocumentId val id: String = "",
  val itemType: String = "AYAH",
  val referenceKey: String = "",
  val title: String = "",
  val arabicSnippet: String = "",
  val translationSnippet: String = "",
  @ServerTimestamp val createdAt: Date? = null
)

data class CloudNote(
  @DocumentId val id: String = "",
  val itemType: String = "AYAH",
  val referenceKey: String = "",
  val title: String = "",
  val content: String = "",
  @ServerTimestamp val createdAt: Date? = null,
  @ServerTimestamp val updatedAt: Date? = null
)
