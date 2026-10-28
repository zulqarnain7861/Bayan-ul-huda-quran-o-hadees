package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "translations_fts")
@Fts4(contentEntity = TranslationEntity::class)
data class TranslationFtsEntity(
  val text: String,
  val referenceKey: String
)
