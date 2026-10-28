package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
  @PrimaryKey val number: Int,
  val nameArabic: String,
  val nameEnglish: String,
  val nameEnglishTranslation: String,
  val nameUrdu: String,
  val revelationType: String, // "Meccan" or "Medinan"
  val totalVerses: Int,
  val revelationOrder: Int,
  val juzStart: Int
)
