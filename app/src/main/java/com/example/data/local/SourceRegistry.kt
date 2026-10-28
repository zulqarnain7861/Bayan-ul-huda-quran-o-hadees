package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class VerificationStatus {
  VERIFIED,
  PARTIALLY_VERIFIED,
  UNVERIFIED,
  PENDING_AUDIT
}

enum class ImportStatus {
  SUCCESS,
  PARTIAL,
  IN_PROGRESS,
  FAILED,
  IDLE
}

class SourceRegistryConverters {
  @TypeConverter
  fun fromVerificationStatus(status: VerificationStatus): String = status.name

  @TypeConverter
  fun toVerificationStatus(value: String): VerificationStatus =
    runCatching { VerificationStatus.valueOf(value) }.getOrDefault(VerificationStatus.UNVERIFIED)

  @TypeConverter
  fun fromImportStatus(status: ImportStatus): String = status.name

  @TypeConverter
  fun toImportStatus(value: String): ImportStatus =
    runCatching { ImportStatus.valueOf(value) }.getOrDefault(ImportStatus.IDLE)
}

@Entity(tableName = "source_registry")
data class SourceRegistry(
  @PrimaryKey
  val sourceId: String,
  val title: String,
  val author: String = "",
  val tradition: String = "Sunni / General Islamic",
  val language: String = "Arabic",
  val translationLanguages: String = "English, Urdu",
  val officialUrl: String = "",
  val datasetLocation: String = "",
  val license: String = "Scholarly Open Access / Attribution",
  val lastSyncTime: Long = System.currentTimeMillis(),
  val totalAvailableRecords: Int = 0,
  val totalImportedRecords: Int = 0,
  val duplicateCount: Int = 0,
  val invalidCount: Int = 0,
  val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
  val importStatus: ImportStatus = ImportStatus.SUCCESS
)
