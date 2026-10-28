package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AyahDao
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.DownloadManifestDao
import com.example.data.local.dao.HadithCollectionDao
import com.example.data.local.dao.HadithDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.PrayerScheduleDao
import com.example.data.local.dao.SourceRegistryDao
import com.example.data.local.dao.SurahDao
import com.example.data.local.dao.TranslationDao
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.AyahFtsEntity
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.ContentLicenceEntity
import com.example.data.local.entities.DownloadItemEntity
import com.example.data.local.entities.DownloadPackageManifestEntity
import com.example.data.local.entities.HadithCollectionEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.HadithFtsEntity
import com.example.data.local.entities.IncorrectReferenceReportEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.PrayerScheduleEntity
import com.example.data.local.entities.SurahEntity
import com.example.data.local.entities.TranslationEntity
import com.example.data.local.entities.TranslationFtsEntity

@Database(
  entities = [
    SurahEntity::class,
    AyahEntity::class,
    AyahFtsEntity::class,
    HadithCollectionEntity::class,
    HadithEntity::class,
    TranslationEntity::class,
    TranslationFtsEntity::class,
    HadithFtsEntity::class,
    PrayerScheduleEntity::class,
    BookmarkEntity::class,
    NoteEntity::class,
    SourceRegistry::class,
    DownloadPackageManifestEntity::class,
    DownloadItemEntity::class,
    ContentLicenceEntity::class,
    IncorrectReferenceReportEntity::class
  ],
  version = 8,
  exportSchema = false
)
@TypeConverters(SourceRegistryConverters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun surahDao(): SurahDao
  abstract fun ayahDao(): AyahDao
  abstract fun hadithCollectionDao(): HadithCollectionDao
  abstract fun hadithDao(): HadithDao
  abstract fun translationDao(): TranslationDao
  abstract fun prayerScheduleDao(): PrayerScheduleDao
  abstract fun bookmarkDao(): BookmarkDao
  abstract fun noteDao(): NoteDao
  abstract fun sourceRegistryDao(): SourceRegistryDao
  abstract fun downloadManifestDao(): DownloadManifestDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    private const val CREATE_SOURCE_REGISTRY_SQL =
      "CREATE TABLE IF NOT EXISTS `source_registry` (`sourceId` TEXT NOT NULL, `title` TEXT NOT NULL, `author` TEXT NOT NULL, `tradition` TEXT NOT NULL, `language` TEXT NOT NULL, `translationLanguages` TEXT NOT NULL, `officialUrl` TEXT NOT NULL, `datasetLocation` TEXT NOT NULL, `license` TEXT NOT NULL, `lastSyncTime` INTEGER NOT NULL, `totalAvailableRecords` INTEGER NOT NULL, `totalImportedRecords` INTEGER NOT NULL, `duplicateCount` INTEGER NOT NULL, `invalidCount` INTEGER NOT NULL, `verificationStatus` TEXT NOT NULL, `importStatus` TEXT NOT NULL, PRIMARY KEY(`sourceId`))"

    private const val CREATE_PACKAGE_MANIFESTS_SQL =
      "CREATE TABLE IF NOT EXISTS `download_package_manifests` (`packageId` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `scopeDescription` TEXT NOT NULL, `provider` TEXT NOT NULL, `licenceId` TEXT NOT NULL, `licenceSummary` TEXT NOT NULL, `languageCode` TEXT NOT NULL, `translatorOrReciter` TEXT NOT NULL, `editionKey` TEXT NOT NULL, `versionTag` TEXT NOT NULL, `status` TEXT NOT NULL, `totalItems` INTEGER NOT NULL, `completedItems` INTEGER NOT NULL, `failedItems` INTEGER NOT NULL, `totalBytes` INTEGER NOT NULL, `downloadedBytes` INTEGER NOT NULL, `speedBytesPerSec` INTEGER NOT NULL, `estimatedRemainingSeconds` INTEGER NOT NULL, `destinationPath` TEXT NOT NULL, `expectedChecksumSha256` TEXT NOT NULL, `actualChecksumSha256` TEXT NOT NULL, `isVerifiedOfflineReopenable` INTEGER NOT NULL, `errorMessage` TEXT NOT NULL, `externalFallbackUrl` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`packageId`))"

    private const val CREATE_DOWNLOAD_ITEMS_SQL =
      "CREATE TABLE IF NOT EXISTS `download_manifest_items` (`itemId` TEXT NOT NULL, `packageId` TEXT NOT NULL, `resourceIdentifier` TEXT NOT NULL, `title` TEXT NOT NULL, `sourceUrl` TEXT NOT NULL, `localFilePath` TEXT NOT NULL, `licence` TEXT NOT NULL, `expectedFormat` TEXT NOT NULL, `expectedBytes` INTEGER NOT NULL, `downloadedBytes` INTEGER NOT NULL, `expectedSha256` TEXT NOT NULL, `actualSha256` TEXT NOT NULL, `status` TEXT NOT NULL, `httpStatusCode` INTEGER NOT NULL, `supportsByteRange` INTEGER NOT NULL, `verifiedReopenable` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`itemId`))"

    private const val CREATE_CONTENT_LICENCES_SQL =
      "CREATE TABLE IF NOT EXISTS `content_licence_permissions` (`resourceId` TEXT NOT NULL, `provider` TEXT NOT NULL, `title` TEXT NOT NULL, `rightsHolder` TEXT NOT NULL, `licenceUrl` TEXT NOT NULL, `offlineStorageAllowed` INTEGER NOT NULL, `bulkDownloadAllowed` INTEGER NOT NULL, `redistributionAllowed` INTEGER NOT NULL, `requiresContentSyncProtocol` INTEGER NOT NULL, `attributionRequirements` TEXT NOT NULL, `refreshScheduleDays` INTEGER NOT NULL, `lastSyncTimestamp` INTEGER NOT NULL, `nextRequiredSyncTimestamp` INTEGER NOT NULL, `restrictionExplanation` TEXT NOT NULL, `externalSourceUrl` TEXT NOT NULL, PRIMARY KEY(`resourceId`))"

    private const val CREATE_INCORRECT_REF_REPORTS_SQL =
      "CREATE TABLE IF NOT EXISTS `incorrect_reference_reports` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `claimOrQuery` TEXT NOT NULL, `referenceIdentifier` TEXT NOT NULL, `sourceCollection` TEXT NOT NULL, `issueCategory` TEXT NOT NULL, `userNotes` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"

    private fun applyV6Tables(db: SupportSQLiteDatabase) {
      db.execSQL(CREATE_SOURCE_REGISTRY_SQL)
      db.execSQL(CREATE_PACKAGE_MANIFESTS_SQL)
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_download_package_manifests_category` ON `download_package_manifests` (`category`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_download_package_manifests_status` ON `download_package_manifests` (`status`)")
      db.execSQL(CREATE_DOWNLOAD_ITEMS_SQL)
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_download_manifest_items_packageId` ON `download_manifest_items` (`packageId`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_download_manifest_items_resourceIdentifier` ON `download_manifest_items` (`resourceIdentifier`)")
      db.execSQL(CREATE_CONTENT_LICENCES_SQL)
      db.execSQL(CREATE_INCORRECT_REF_REPORTS_SQL)
    }

    private fun applyV8HadithProvenanceColumns(db: SupportSQLiteDatabase) {
      val cols = listOf(
        "sourceName" to "''",
        "edition" to "''",
        "translator" to "''",
        "license" to "''",
        "versionCommit" to "''",
        "checksumSha256" to "''",
        "textOrigin" to "'dataset'",
        "numberingSystem" to "''"
      )
      for ((col, defVal) in cols) {
        runCatching {
          db.execSQL("ALTER TABLE `hadiths` ADD COLUMN `$col` TEXT NOT NULL DEFAULT $defVal")
        }
      }
    }

    val MIGRATION_3_6 = object : Migration(3, 6) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `source_registry`")
        applyV6Tables(db)
      }
    }

    val MIGRATION_4_6 = object : Migration(4, 6) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `source_registry`")
        applyV6Tables(db)
      }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
      override fun migrate(db: SupportSQLiteDatabase) {
        applyV6Tables(db)
      }
    }

    val MIGRATION_6_8 = object : Migration(6, 8) {
      override fun migrate(db: SupportSQLiteDatabase) {
        applyV6Tables(db)
        applyV8HadithProvenanceColumns(db)
      }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
      override fun migrate(db: SupportSQLiteDatabase) {
        applyV8HadithProvenanceColumns(db)
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "bayan_al_huda.db"
        )
        .addMigrations(MIGRATION_3_6, MIGRATION_4_6, MIGRATION_5_6, MIGRATION_6_8, MIGRATION_7_8)
        .fallbackToDestructiveMigration(true)
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
