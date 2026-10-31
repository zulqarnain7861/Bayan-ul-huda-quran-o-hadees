package com.example.data.repository

import android.content.Context
import com.example.data.local.CommentaryEntryEntity
import com.example.data.local.FtsQuerySanitizer
import com.example.data.local.KnowledgeDatabase
import com.example.data.model.DiscoveryTopicDefinition
import com.example.data.model.LectureRecord
import com.example.data.model.MaududiWorkRecord
import com.example.data.model.RecommendedLectureItem
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Unified Search & Discovery Repository (Section C) combining:
 * - Room `@Fts4` full-text search over `LectureIndexEntity` and `CommentaryIndexEntity`
 *   with `FtsQuerySanitizer` (Test T5 & T6).
 * - Topic filtering enforcing `minimumRecordsToShow` threshold (Test T7).
 * - Watch-Time Based Topic Recommendation Engine surfacing related Engineer Muhammad Ali Mirza
 *   lectures (since 2013) at the top based on which topics/titles the user watches most.
 */
class DiscoverRepository(
  private val context: Context,
  private val database: KnowledgeDatabase = KnowledgeDatabase.getInstance(context),
  val lectureRepository: LectureRepository = LectureRepository(context, database),
  val maududiRepository: MaududiRepository = MaududiRepository(context, database)
) {

  data class TopicChipItem(
    val definition: DiscoveryTopicDefinition,
    val matchingLectureCount: Int,
    val userWatchedSeconds: Int
  )

  data class UnifiedSearchResults(
    val rawQuery: String,
    val sanitizedFtsQuery: String,
    val lectures: List<LectureRecord>,
    val commentaryEntries: List<CommentaryEntryEntity>,
    val maududiWorks: List<MaududiWorkRecord>
  )

  suspend fun initializeAll() = withContext(Dispatchers.IO) {
    lectureRepository.ensureDatabaseSeeded()
    maududiRepository.ensureDatabaseSeeded()
  }

  /**
   * Returns only topics whose matching record count is `>= minimumRecordsToShow` (Test T7),
   * ordered by user watch time descending so the user's most-watched topics appear first.
   */
  fun buildVisibleTopics(
    allLectures: List<LectureRecord>,
    allTopics: List<DiscoveryTopicDefinition>,
    topicWatchMap: Map<String, Int>,
    isDebugBuild: Boolean
  ): List<TopicChipItem> {
    val visibleLectures = allLectures.filter { it.shouldShowInUi(isDebugBuild) }
    return allTopics.mapNotNull { topic ->
      val count = visibleLectures.count { topic.id in it.topics }
      if (count >= topic.minimumRecordsToShow) {
        TopicChipItem(
          definition = topic,
          matchingLectureCount = count,
          userWatchedSeconds = topicWatchMap[topic.id] ?: 0
        )
      } else {
        null
      }
    }.sortedWith(
      compareByDescending<TopicChipItem> { it.userWatchedSeconds }
        .thenByDescending { it.matchingLectureCount }
    )
  }

  /**
   * Executes safe FTS4 search across both `lecture_index` and `commentary_index` (Test T5, T6).
   * Never throws on hostile inputs; returns empty results when sanitizer strips all tokens.
   */
  suspend fun searchAll(
    rawQuery: String,
    isDebugBuild: Boolean
  ): UnifiedSearchResults = withContext(Dispatchers.IO) {
    lectureRepository.ensureDatabaseSeeded()
    maududiRepository.ensureDatabaseSeeded()

    val sanitized = FtsQuerySanitizer.sanitizeFtsQuery(rawQuery)
    if (sanitized.isBlank()) {
      return@withContext UnifiedSearchResults(
        rawQuery = rawQuery,
        sanitizedFtsQuery = "",
        lectures = emptyList(),
        commentaryEntries = emptyList(),
        maududiWorks = emptyList()
      )
    }

    val includePending = if (isDebugBuild) 1 else 0
    val ftsLectures = try {
      database.searchDao()
        .searchLecturesFts(sanitized, includePending = includePending, limit = 50)
        .map { it.toDomainModel() }
    } catch (_: Exception) {
      emptyList()
    }

    val ftsCommentary = try {
      database.searchDao().searchCommentaryFts(sanitized, limit = 30)
    } catch (_: Exception) {
      emptyList()
    }

    val lowerRaw = rawQuery.trim().lowercase(Locale.ROOT)
    val matchingWorks = maududiRepository.getAllWorksMemory().filter { work ->
      work.titleEn.lowercase(Locale.ROOT).contains(lowerRaw) ||
        work.titleUr.contains(lowerRaw) ||
        work.bibliographicSummary.lowercase(Locale.ROOT).contains(lowerRaw)
    }

    UnifiedSearchResults(
      rawQuery = rawQuery,
      sanitizedFtsQuery = sanitized,
      lectures = ftsLectures,
      commentaryEntries = ftsCommentary,
      maududiWorks = matchingWorks
    )
  }

  /**
   * Computes watch-time-ranked recommendations across all visible lectures.
   */
  suspend fun getWatchTimeRecommendations(
    isDebugBuild: Boolean,
    limit: Int = 18
  ): List<RecommendedLectureItem> = withContext(Dispatchers.IO) {
    val dataset = lectureRepository.loadSeedDataset()
    val config = lectureRepository.loadDiscoveryConfig()
    val watchMap = lectureRepository.getWatchTimeMap()
    LectureRepository.computeWatchTimeRecommendations(
      allLectures = dataset.records,
      watchTimeByYoutubeId = watchMap,
      topicDefinitions = config.topics,
      isDebugBuild = isDebugBuild,
      limit = limit
    )
  }
}
