package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "ayahs",
  indices = [
    Index(value = ["surahNumber", "verseNumber"], unique = true),
    Index(value = ["juzNumber"])
  ]
)
data class AyahEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val surahNumber: Int,
  val verseNumber: Int,
  val textArabic: String,
  val translationEnglish: String,
  val translationUrdu: String,
  val transliteration: String = "",
  val juzNumber: Int = 1,
  val pageNumber: Int = 1,
  val audioUrl: String = ""
)
