package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.text.Normalizer
import java.util.Locale

/**
 * FTS4 Query Sanitizer & Room DAOs for `KnowledgeDatabase` (`knowledge.db`).
 *
 * Implements Spec §8.2:
 * - Strips `" ' * ( ) : ^ -` and SQL/FTS control syntax.
 * - Normalizes with NFKD diacritic folding and lowercases via `Locale.ROOT`.
 * - Wraps each safe token in double quotes (`"token*"`) so hostile inputs like
 *   `foo" OR 1=1 --`, `NEAR/3`, `title:*`, `(a AND b` never throw SQLiteException.
 */
object FtsQuerySanitizer {

  private val FTS_OPERATORS = setOf("and", "or", "not", "near")
  private val NEAR_DISTANCE_REGEX = Regex("""(?i)\bnear\s*/\s*\d+\b""")
  private val STRIP_CHARS_REGEX = Regex("""["'*():^\-;\\/<>=!@#$%&+\[\]{}|~`?]""")
  private val COMBINING_MARKS_REGEX = Regex("""\p{InCombiningDiacriticalMarks}+""")
  private val TOKEN_SPLIT_REGEX = Regex("""[^\p{L}\p{N}]+""")

  fun sanitizeFtsQuery(raw: String): String {
    if (raw.isBlank()) return ""
    val nfkd = Normalizer.normalize(raw, Normalizer.Form.NFKD)
    val deAccented = COMBINING_MARKS_REGEX.replace(nfkd, "")
    val withoutNearDist = NEAR_DISTANCE_REGEX.replace(deAccented, " ")
    val stripped = STRIP_CHARS_REGEX.replace(withoutNearDist, " ")
      .lowercase(Locale.ROOT)

    val tokens = TOKEN_SPLIT_REGEX.split(stripped)
      .map { it.trim() }
      .filter { token ->
        token.isNotEmpty() &&
          token !in FTS_OPERATORS &&
          !token.startsWith("near")
      }
      .take(12)

    if (tokens.isEmpty()) return ""
    return tokens.joinToString(" ") { token -> "\"$token*\"" }
  }
}

@Dao
interface LectureDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertLectures(lectures: List<LectureEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTopics(topics: List<TopicEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSourceLinks(links: List<SourceLinkEntity>)

  @Query("INSERT INTO lecture_index(lecture_index) VALUES('rebuild')")
  suspend fun rebuildLectureFtsIndex()

  @Query("SELECT COUNT(*) FROM lectures")
  suspend fun getLectureCount(): Int

  @Query("SELECT * FROM lectures ORDER BY CASE WHEN verificationState = 'CONFIRMED_LIVE' THEN 0 ELSE 1 END, recordedYear DESC, rowid ASC")
  suspend fun getAllLectures(): List<LectureEntity>

  @Query(
    """
    SELECT * FROM lectures
    WHERE (:includePending = 1 OR sourceTier != 'UNKNOWN_PENDING')
    ORDER BY CASE WHEN verificationState = 'CONFIRMED_LIVE' THEN 0 ELSE 1 END,
             CASE WHEN sourceTier = 'OFFICIAL' THEN 0 ELSE 1 END,
             recordedYear DESC,
             rowid ASC
    """
  )
  suspend fun getVisibleLectures(includePending: Int): List<LectureEntity>

  @Query("SELECT * FROM lectures WHERE youtubeId = :youtubeId LIMIT 1")
  suspend fun getLectureByYoutubeId(youtubeId: String): LectureEntity?

  @Query("SELECT * FROM topics ORDER BY id ASC")
  suspend fun getAllTopics(): List<TopicEntity>

  @Query("UPDATE lectures SET verificationState = :newState, countryStatus = :countryStatus WHERE youtubeId = :youtubeId")
  suspend fun updateVerificationState(youtubeId: String, newState: String, countryStatus: String)
}

@Dao
interface MaududiDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAuthors(authors: List<AuthorEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertWorks(works: List<WorkEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertEditions(editions: List<EditionEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertChapters(chapters: List<ChapterEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertVerseRefs(refs: List<VerseRefEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCommentaryEntries(entries: List<CommentaryEntryEntity>)

  @Query("INSERT INTO commentary_index(commentary_index) VALUES('rebuild')")
  suspend fun rebuildCommentaryFtsIndex()

  @Query("SELECT * FROM works ORDER BY categoryId ASC, id ASC")
  suspend fun getAllWorks(): List<WorkEntity>

  @Query("SELECT * FROM works WHERE categoryId = :categoryId ORDER BY id ASC")
  suspend fun getWorksByCategory(categoryId: String): List<WorkEntity>

  @Query("SELECT * FROM editions WHERE workId = :workId")
  suspend fun getEditionsForWork(workId: String): List<EditionEntity>

  @Query("SELECT * FROM chapters WHERE workId = :workId ORDER BY ordinal ASC")
  suspend fun getChaptersForWork(workId: String): List<ChapterEntity>

  @Query("SELECT * FROM commentary_entries WHERE editionId = :editionId ORDER BY rowid ASC")
  suspend fun getCommentaryForEdition(editionId: String): List<CommentaryEntryEntity>

  @Query(
    """
    SELECT ce.* FROM commentary_entries ce
    INNER JOIN editions ed ON ce.editionId = ed.id
    WHERE ed.licenceId = 'LINK_ONLY' AND ce.text IS NOT NULL
    """
  )
  suspend fun findLinkOnlyViolationsWithNonNullText(): List<CommentaryEntryEntity>
}

@Dao
interface KnowledgeSearchDao {

  @Query(
    """
    SELECT l.* FROM lectures l
    INNER JOIN lecture_index fts ON l.rowid = fts.rowid
    WHERE lecture_index MATCH :sanitizedFtsQuery
      AND (:includePending = 1 OR l.sourceTier != 'UNKNOWN_PENDING')
    ORDER BY CASE WHEN l.verificationState = 'CONFIRMED_LIVE' THEN 0 ELSE 1 END,
             CASE WHEN l.sourceTier = 'OFFICIAL' THEN 0 ELSE 1 END,
             l.rowid ASC
    LIMIT :limit
    """
  )
  suspend fun searchLecturesFts(
    sanitizedFtsQuery: String,
    includePending: Int,
    limit: Int = 50
  ): List<LectureEntity>

  @Query(
    """
    SELECT ce.* FROM commentary_entries ce
    INNER JOIN commentary_index fts ON ce.rowid = fts.rowid
    WHERE commentary_index MATCH :sanitizedFtsQuery
    ORDER BY ce.rowid ASC
    LIMIT :limit
    """
  )
  suspend fun searchCommentaryFts(
    sanitizedFtsQuery: String,
    limit: Int = 30
  ): List<CommentaryEntryEntity>
}

@Dao
interface KnowledgeProgressDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertProgress(progress: ProgressEntity)

  @Query("SELECT * FROM knowledge_progress ORDER BY updatedAtEpochMs DESC LIMIT :limit")
  suspend fun getRecentProgress(limit: Int = 200): List<ProgressEntity>

  @Query(
    """
    DELETE FROM knowledge_progress
    WHERE key NOT IN (
      SELECT key FROM knowledge_progress ORDER BY updatedAtEpochMs DESC LIMIT 200
    )
    """
  )
  suspend fun pruneProgressBeyond200()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertBookmark(bookmark: KnowledgeBookmarkEntity)

  @Query("DELETE FROM knowledge_bookmarks WHERE key = :key")
  suspend fun deleteBookmark(key: String)

  @Query("SELECT * FROM knowledge_bookmarks ORDER BY createdAtEpochMs DESC LIMIT :limit")
  suspend fun getBookmarks(limit: Int = 200): List<KnowledgeBookmarkEntity>

  @Query(
    """
    DELETE FROM knowledge_bookmarks
    WHERE key NOT IN (
      SELECT key FROM knowledge_bookmarks ORDER BY createdAtEpochMs DESC LIMIT 200
    )
    """
  )
  suspend fun pruneBookmarksBeyond200()
}
