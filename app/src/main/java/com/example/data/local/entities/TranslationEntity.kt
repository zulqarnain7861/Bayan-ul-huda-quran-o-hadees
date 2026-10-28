package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "translations",
  indices = [
    Index(value = ["sourceType", "referenceKey", "editionKey"], unique = true),
    Index(value = ["sourceType", "surahOrBookNumber", "verseOrHadithNumber"]),
    Index(value = ["editionKey"])
  ]
)
data class TranslationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val sourceType: String, // "QURAN" or "HADITH"
  val referenceKey: String, // e.g. "2:255" or "bukhari:5027"
  val surahOrBookNumber: Int,
  val verseOrHadithNumber: Int,
  val editionKey: String, // e.g. "en.sahih", "ur.jalandhry", "en.darussalam", "ur.darussalam"
  val editionTitle: String, // e.g. "Sahih International", "Fateh Muhammad Jalandhry"
  val languageCode: String, // "en", "ur", "ar", "tr"
  val text: String,
  val isVerified: Boolean = true
)
