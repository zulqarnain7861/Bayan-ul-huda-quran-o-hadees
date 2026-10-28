package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "ayahs_fts")
@Fts4(contentEntity = AyahEntity::class)
data class AyahFtsEntity(
  val textArabic: String,
  val translationEnglish: String,
  val translationUrdu: String,
  val transliteration: String
)
