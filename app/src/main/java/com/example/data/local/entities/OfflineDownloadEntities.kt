package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Priority 11 Section E — Mandatory Download Manifest Statuses.
 */
enum class DownloadManifestStatus {
  QUEUED,
  DOWNLOADING,
  PAUSED,
  COMPLETE,
  PARTIAL,
  INTERRUPTED,
  FAILED,
  VERIFICATION_FAILED,
  LICENCE_RESTRICTED
}

/**
 * Priority 11 Section E — Package-level manifest tracking every downloadable Quran, Audio,
 * Translation, Tafsir, Hadith, and Search Index package.
 */
@Entity(
  tableName = "download_package_manifests",
  indices = [
    Index(value = ["category"]),
    Index(value = ["status"])
  ]
)
data class DownloadPackageManifestEntity(
  @PrimaryKey
  val packageId: String,
  val title: String,
  val category: String, // QURAN_TEXT, QURAN_TRANSLATION, QURAN_TAFSIR, QURAN_AUDIO_CHAPTER, QURAN_AUDIO_AYAH, HADITH_SUNNI, HADITH_SHIA, SEARCH_INDEX
  val scopeDescription: String,
  val provider: String,
  val licenceId: String,
  val licenceSummary: String,
  val languageCode: String = "ar",
  val translatorOrReciter: String = "",
  val editionKey: String = "",
  val versionTag: String = "v1.0",
  val status: String = DownloadManifestStatus.QUEUED.name,
  val totalItems: Int = 1,
  val completedItems: Int = 0,
  val failedItems: Int = 0,
  val totalBytes: Long = 0L,
  val downloadedBytes: Long = 0L,
  val speedBytesPerSec: Long = 0L,
  val estimatedRemainingSeconds: Long = -1L,
  val destinationPath: String = "",
  val expectedChecksumSha256: String = "",
  val actualChecksumSha256: String = "",
  val isVerifiedOfflineReopenable: Boolean = false,
  val errorMessage: String = "",
  val externalFallbackUrl: String = "",
  val updatedAt: Long = System.currentTimeMillis()
) {
  val statusEnum: DownloadManifestStatus
    get() = runCatching { DownloadManifestStatus.valueOf(status) }
      .getOrDefault(DownloadManifestStatus.QUEUED)

  val remainingItems: Int
    get() = (totalItems - completedItems).coerceAtLeast(0)

  val overallPercent: Int
    get() = when {
      statusEnum == DownloadManifestStatus.COMPLETE -> 100
      totalItems > 0 -> ((completedItems * 100) / totalItems).coerceIn(0, 100)
      totalBytes > 0L -> ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
      else -> 0
    }

  val overallProgressPercent: Int
    get() = overallPercent
}

/**
 * Priority 11 Section E — Individual item inside a downloadable package manifest.
 */
@Entity(
  tableName = "download_manifest_items",
  indices = [
    Index(value = ["packageId"]),
    Index(value = ["resourceIdentifier"])
  ]
)
data class DownloadItemEntity(
  @PrimaryKey
  val itemId: String,
  val packageId: String,
  val resourceIdentifier: String, // e.g. "quran:2:255", "audio:chapter:alafasy:001", "hadith:bukhari:1"
  val title: String,
  val sourceUrl: String,
  val localFilePath: String,
  val licence: String,
  val expectedFormat: String, // JSON_UTF8, MP3_AUDIO, TAFSIR_JSON, SQLITE_RECORD
  val expectedBytes: Long = 0L,
  val downloadedBytes: Long = 0L,
  val expectedSha256: String = "",
  val actualSha256: String = "",
  val status: String = DownloadManifestStatus.QUEUED.name,
  val httpStatusCode: Int = 0,
  val supportsByteRange: Boolean = true,
  val verifiedReopenable: Boolean = false,
  val updatedAt: Long = System.currentTimeMillis()
) {
  val statusEnum: DownloadManifestStatus
    get() = runCatching { DownloadManifestStatus.valueOf(status) }
      .getOrDefault(DownloadManifestStatus.QUEUED)

  val itemPercent: Int
    get() = when {
      statusEnum == DownloadManifestStatus.COMPLETE -> 100
      expectedBytes > 0L -> ((downloadedBytes * 100L) / expectedBytes).toInt().coerceIn(0, 100)
      else -> 0
    }
}

/**
 * Priority 11 Section H — Source-specific licence & offline permission record.
 */
@Entity(tableName = "content_licence_permissions")
data class ContentLicenceEntity(
  @PrimaryKey
  val resourceId: String,
  val provider: String,
  val title: String,
  val rightsHolder: String,
  val licenceUrl: String,
  val offlineStorageAllowed: Boolean,
  val bulkDownloadAllowed: Boolean,
  val redistributionAllowed: Boolean,
  val requiresContentSyncProtocol: Boolean = false,
  val attributionRequirements: String,
  val refreshScheduleDays: Int = 30,
  val lastSyncTimestamp: Long = System.currentTimeMillis(),
  val nextRequiredSyncTimestamp: Long = System.currentTimeMillis() + 30L * 24L * 3600L * 1000L,
  val restrictionExplanation: String = "",
  val externalSourceUrl: String = ""
) {
  val requiresContentSyncProcedure: Boolean
    get() = requiresContentSyncProtocol
}

/**
 * Priority 12 Section H — "Report Incorrect Reference" record persisted for administrator review.
 */
@Entity(tableName = "incorrect_reference_reports")
data class IncorrectReferenceReportEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val claimOrQuery: String,
  val referenceIdentifier: String,
  val sourceCollection: String,
  val issueCategory: String,
  val userNotes: String,
  val status: String = "PENDING_ADMIN_REVIEW",
  val createdAt: Long = System.currentTimeMillis()
)
