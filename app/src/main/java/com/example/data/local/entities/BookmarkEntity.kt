package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "bookmarks",
  indices = [Index(value = ["itemKey"], unique = true)]
)
data class BookmarkEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val itemType: String, // "QURAN" or "HADITH"
  val itemKey: String,
  val title: String,
  val subtitle: String,
  val arabicSnippet: String,
  val timestamp: Long = System.currentTimeMillis()
)
