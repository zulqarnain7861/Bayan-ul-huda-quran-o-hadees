package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hadith_collections")
data class HadithCollectionEntity(
  @PrimaryKey val collectionKey: String,
  val nameEnglish: String,
  val nameArabic: String,
  val nameUrdu: String,
  val compiler: String,
  val deathHijri: String,
  val totalCanonicalHadiths: Int,
  val totalBooks: Int,
  val darussalamEditionName: String,
  val inBookEditionName: String,
  val apiEndpointSlug: String,
  val locallyCachedCount: Int = 0,
  val hasCompleteArabic: Boolean = true,
  val hasCompleteEnglish: Boolean = true,
  val hasCompleteUrdu: Boolean = true,
  val paginationStatus: String = "Curated Offline + On-Demand API Sync",
  val lastSyncedTimestamp: Long = System.currentTimeMillis()
)
