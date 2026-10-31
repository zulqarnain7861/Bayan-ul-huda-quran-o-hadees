package com.example.data.local

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Separate Room database (`knowledge.db`, version 1) for Section A (Engineer Muhammad Ali Mirza
 * Lecture Index), Section B (Fikr-e-Maududi Bibliographic & Licence-Enforced Works), and Section C
 * (Unified FTS4 Search & Watch-Time Discovery).
 *
 * Strictly isolated from `AppDatabase.kt` (version 8) per Hard Constraint C2 & V7.
 */

@Entity(tableName = "authors")
data class AuthorEntity(
  @PrimaryKey val id: String,
  val nameEn: String,
  val nameUr: String,
  val birthYear: Int?,
  val deathYear: Int?,
  val biographySummary: String,
  val officialPortalUrl: String?
)

@Entity(
  tableName = "works",
  indices = [Index(value = ["authorId"]), Index(value = ["type"])]
)
data class WorkEntity(
  @PrimaryKey val id: String,
  val authorId: String,
  val titleEn: String,
  val titleUr: String,
  val type: String, // BOOK | TAFSEER | ESSAY | COLLECTION | CORRESPONDENCE
  val categoryId: String,
  val summary: String,
  val primarySourceUrl: String
)

@Entity(
  tableName = "editions",
  indices = [Index(value = ["workId"]), Index(value = ["licenceId"])]
)
data class EditionEntity(
  @PrimaryKey val id: String,
  val workId: String,
  val languageTag: String,
  val translator: String?,
  val publisher: String?,
  val year: Int?,
  val licenceId: String, // LINK_ONLY | LICENSED_EXCERPT | FULL_TEXT_PUBLIC_DOMAIN | OWN_PERMISSION
  val completeness: String // FULL | PARTIAL | UNKNOWN
)

@Entity(
  tableName = "chapters",
  indices = [Index(value = ["workId", "ordinal"])]
)
data class ChapterEntity(
  @PrimaryKey val id: String,
  val workId: String,
  val ordinal: Int,
  val title: String,
  val sourceUrl: String
)

@Entity(
  tableName = "verse_refs",
  indices = [Index(value = ["surah", "ayah"], unique = true)]
)
data class VerseRefEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val surah: Int,
  val ayah: Int
)

@Entity(
  tableName = "commentary_entries",
  indices = [Index(value = ["editionId"]), Index(value = ["verseRefId"])]
)
data class CommentaryEntryEntity(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "rowid")
  val rowid: Int = 0,
  val editionId: String,
  val verseRefId: Long,
  val page: Int?,
  val title: String,
  val bibliographicNote: String,
  val text: String? // Strictly null for LINK_ONLY editions (T9)
)

@Fts4(
  contentEntity = CommentaryEntryEntity::class,
  tokenizer = FtsOptions.TOKENIZER_UNICODE61
)
@Entity(tableName = "commentary_index")
data class CommentaryIndexEntity(
  val title: String,
  val bibliographicNote: String,
  val text: String?
)

@Entity(
  tableName = "lectures",
  indices = [
    Index(value = ["youtubeId"], unique = true),
    Index(value = ["sourceTier"]),
    Index(value = ["verificationState"]),
    Index(value = ["recordFormat"]),
    Index(value = ["lectureNumber"])
  ]
)
data class LectureEntity(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "rowid")
  val rowid: Int = 0,
  val youtubeId: String,
  val title: String,
  val verifiedTitle: String?,
  val speaker: String,
  val channelHandle: String?,
  val channelUrl: String?,
  val sourceTier: String,
  val countryStatus: String,
  val verificationState: String,
  val recordFormat: String,
  val lectureNumber: Int?,
  val seriesName: String?,
  val recordedYear: Int?,
  val durationSec: Int?,
  val topicsCsv: String,
  val relatedYoutubeIdsCsv: String,
  val summary: String?,
  val transcriptStatus: String,
  val referenceStatus: String,
  val referencesCsv: String,
  val alternateYoutubeIdsCsv: String
)

@Fts4(
  contentEntity = LectureEntity::class,
  tokenizer = FtsOptions.TOKENIZER_UNICODE61
)
@Entity(tableName = "lecture_index")
data class LectureIndexEntity(
  val title: String,
  val verifiedTitle: String?,
  val topicsCsv: String,
  val summary: String?,
  val referencesCsv: String
)

@Entity(tableName = "topics")
data class TopicEntity(
  @PrimaryKey val id: String,
  val titleEn: String,
  val titleUr: String,
  val minimumRecordsToShow: Int,
  val relatedTopicIdsCsv: String,
  val keywordsCsv: String
)

@Entity(
  tableName = "source_links",
  indices = [Index(value = ["ownerType", "ownerId"])]
)
data class SourceLinkEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val ownerType: String, // LECTURE | WORK | EDITION | CHAPTER
  val ownerId: String,
  val url: String,
  val tier: String,
  val countryStatus: String,
  val checkedOn: String
)

@Entity(
  tableName = "knowledge_progress",
  indices = [Index(value = ["itemType", "itemId"], unique = true)]
)
data class ProgressEntity(
  @PrimaryKey val key: String, // e.g. "LECTURE:DPvekksFmBs" or "WORK:tafheem_001"
  val itemType: String,
  val itemId: String,
  val positionSec: Int,
  val totalWatchedSec: Int,
  val updatedAtEpochMs: Long
)

@Entity(
  tableName = "knowledge_bookmarks",
  indices = [Index(value = ["createdAtEpochMs"])]
)
data class KnowledgeBookmarkEntity(
  @PrimaryKey val key: String,
  val itemType: String, // LECTURE | WORK
  val itemId: String,
  val title: String,
  val subtitle: String,
  val createdAtEpochMs: Long
)

@Database(
  entities = [
    AuthorEntity::class,
    WorkEntity::class,
    EditionEntity::class,
    ChapterEntity::class,
    VerseRefEntity::class,
    CommentaryEntryEntity::class,
    CommentaryIndexEntity::class,
    LectureEntity::class,
    LectureIndexEntity::class,
    TopicEntity::class,
    SourceLinkEntity::class,
    ProgressEntity::class,
    KnowledgeBookmarkEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class KnowledgeDatabase : RoomDatabase() {
  abstract fun lectureDao(): LectureDao
  abstract fun maududiDao(): MaududiDao
  abstract fun searchDao(): KnowledgeSearchDao
  abstract fun progressDao(): KnowledgeProgressDao

  companion object {
    const val DATABASE_NAME = "knowledge.db"

    @Volatile
    private var INSTANCE: KnowledgeDatabase? = null

    fun getInstance(context: Context): KnowledgeDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          KnowledgeDatabase::class.java,
          DATABASE_NAME
        )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
