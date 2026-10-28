package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "hadiths",
  indices = [
    Index(value = ["collectionKey", "hadithNumber"], unique = true),
    Index(value = ["collectionKey", "bookNumber"])
  ]
)
data class HadithEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val collectionKey: String,
  val collectionName: String,
  val bookNumber: Int = 1,
  val bookName: String,
  val chapterNumber: Int = 1,
  val chapterName: String = "",
  val hadithNumber: Int, // Canonical Sunnah.com / Darussalam Number
  val darussalamReference: String = "", // Explicit Darussalam Edition label e.g. "Darussalam #5027"
  val inBookReference: String = "", // Book & Hadith In-Book Reference / Other Edition Numbering
  val arabicNumber: Int = 0, // Arabic Edition Number when provided by source
  val arabicText: String,
  val transliteration: String = "",
  val englishText: String,
  val urduText: String,
  val narrator: String,
  val grade: String = "Sahih",
  val gradingScholar: String = "",
  val reference: String,
  val linkedQuranVerses: String = "", // Comma-separated verse references e.g. "2:255, 112:1"
  // Authentic Numbering Systems
  val uscMsaReference: String = "", // USC-MSA web (English) numbering
  val fathAlBariReference: String = "", // Fath al-Bari scholarly cross-references (for Bukhari)
  val abdulBaqiReference: String = "", // Muhammad Fu'ad Abdul Baqi edition references (for Bukhari/Muslim/Ibn Majah)
  val abdulHamidReference: String = "", // Muhammad Muhyi al-Din Abdul Hamid edition references (for Abu Dawud)
  val baitAlAfkarReference: String = "", // Bait al-Afkar al-Dawliyya edition references (for Abu Dawud)
  val yahyaAlLaythiReference: String = "", // Yahya ibn Yahya al-Laythi recension references (for Muwatta Malik)
  val otherPrintedReference: String = "", // Edition-specific printed references for Tirmidhi/Nasa'i/Ibn Majah/Malik
  // Per-record Provenance & Audit Metadata
  val sourceName: String = "",
  val edition: String = "",
  val translator: String = "",
  val license: String = "",
  val versionCommit: String = "",
  val checksumSha256: String = "",
  val textOrigin: String = "dataset", // dataset | official API | verified OCR | scan image
  val numberingSystem: String = ""
)

