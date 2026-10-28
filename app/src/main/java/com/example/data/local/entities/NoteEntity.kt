package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val referenceKey: String,
  val title: String,
  val content: String,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
