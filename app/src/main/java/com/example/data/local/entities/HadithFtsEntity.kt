package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "hadiths_fts")
@Fts4(contentEntity = HadithEntity::class)
data class HadithFtsEntity(
  val arabicText: String,
  val englishText: String,
  val urduText: String,
  val narrator: String,
  val bookName: String,
  val chapterName: String,
  val reference: String
)
