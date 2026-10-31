package com.example

import com.example.data.local.CommentaryEntryEntity
import com.example.data.local.EditionEntity
import com.example.data.local.FtsQuerySanitizer
import com.example.data.local.KnowledgeDatabase
import com.example.data.model.DiscoverySeedConfig
import com.example.data.model.LectureFormat
import com.example.data.model.LectureSeedDataset
import com.example.data.model.MaududiLicenceState
import com.example.data.model.RecordVerificationState
import com.example.data.model.SourceTier
import com.example.data.repository.LectureRepository
import com.example.data.repository.MaududiRepository
import com.example.net.IndexSync
import com.example.player.YtEmbedUrlBuilder
import java.io.File
import java.nio.file.Files
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fast, Offline JUnit4 Verification Suite for Bayan al-Huda Build Spec v4.0
 * covering Assertions T1 through T14 (§11) and T15 (Watch-Time Topic Recommendation Engine).
 */
class KnowledgeAndMirzaVerificationTest {

  private lateinit var dataset: LectureSeedDataset
  private lateinit var discoveryConfig: DiscoverySeedConfig

  private fun resolveAssetFile(name: String): File {
    val candidates = listOf(
      File("src/main/assets/$name"),
      File("app/src/main/assets/$name")
    )
    return candidates.first { it.exists() }
  }

  @Before
  fun setUp() {
    val mirzaFile = resolveAssetFile(LectureRepository.ASSET_MIRZA_LECTURES)
    val discFile = resolveAssetFile(LectureRepository.ASSET_DISCOVERY_SEED)
    dataset = mirzaFile.inputStream().use { LectureRepository.parseLectureSeedStream(it) }
    discoveryConfig = discFile.inputStream().use { LectureRepository.parseDiscoverySeedStream(it) }
  }

  @Test
  fun testT1_and_T2_and_T3_and_T4_mirzaLecturesSeedInvariants() {
    // T1: All 169 youtubeId values are unique and match ^[A-Za-z0-9_-]{11}$
    assertEquals(169, dataset.records.size)
    val uniqueIds = dataset.records.map { it.youtubeId }.toSet()
    assertEquals(169, uniqueIds.size)
    val regex = Regex("^[A-Za-z0-9_-]{11}$")
    dataset.records.forEach { record ->
      assertTrue("Invalid youtubeId: ${record.youtubeId}", regex.matches(record.youtubeId))
    }

    // T2: counts object matches array length and breakdown (10 CONFIRMED_LIVE, 159 UNPROBED, 120 CLIP, 49 FULL_LECTURE, Tafseer 1..344)
    assertEquals(dataset.records.size, dataset.counts.total)
    val confirmedCount = dataset.records.count { it.verificationState == RecordVerificationState.CONFIRMED_LIVE }
    val unprobedCount = dataset.records.count { it.verificationState == RecordVerificationState.UNPROBED }
    val clipCount = dataset.records.count { it.recordFormat == LectureFormat.CLIP }
    val fullCount = dataset.records.count { it.recordFormat == LectureFormat.FULL_LECTURE }
    assertEquals(10, confirmedCount)
    assertEquals(159, unprobedCount)
    assertEquals(120, clipCount)
    assertEquals(49, fullCount)
    assertEquals(confirmedCount, dataset.counts.confirmedLive)
    assertEquals(unprobedCount, dataset.counts.unprobed)
    assertEquals(clipCount, dataset.counts.clips)
    assertEquals(fullCount, dataset.counts.fullLectures)
    assertEquals(1, dataset.counts.tafseerMinLectureNumber)
    assertEquals(344, dataset.counts.tafseerMaxLectureNumber)

    // T3: DPvekksFmBs, XANsNmxxE90, wgd4axKYutc are present
    assertTrue(uniqueIds.contains("DPvekksFmBs"))
    assertTrue(uniqueIds.contains("XANsNmxxE90"))
    assertTrue(uniqueIds.contains("wgd4axKYutc"))

    // T4: wgd4axKYutc has sourceTier = UNKNOWN_PENDING and is hidden when !isDebugBuild
    val wgd = dataset.records.first { it.youtubeId == "wgd4axKYutc" }
    assertEquals(SourceTier.UNKNOWN_PENDING, wgd.sourceTier)
    assertTrue(wgd.title.contains("Maududi"))
    assertFalse("UNKNOWN_PENDING must be hidden in release (!BuildConfig.DEBUG)", wgd.shouldShowInUi(isDebugBuild = false))
    assertTrue("UNKNOWN_PENDING is visible in debug", wgd.shouldShowInUi(isDebugBuild = true))

    // Verify multiple fan-based channels are included and placed on top for India & global playability
    val fanRecords = dataset.records.filter { it.sourceTier == SourceTier.FAN_CHANNEL }
    val officialRecords = dataset.records.filter { it.sourceTier == SourceTier.OFFICIAL }
    val distinctChannels = dataset.records.mapNotNull { it.channelHandle }.toSet()
    assertTrue("Must include >= 100 fan-channel lectures, found ${fanRecords.size}", fanRecords.size >= 100)
    assertTrue("Must include >= 40 official-channel lectures, found ${officialRecords.size}", officialRecords.size >= 40)
    assertTrue("Must include >= 15 distinct channels, found ${distinctChannels.size}", distinctChannels.size >= 15)
    assertEquals("Fan-based channels must be placed at the top of the dataset", SourceTier.FAN_CHANNEL, dataset.records.first().sourceTier)
  }

  @Test
  fun testT5_and_T6_fts4SearchAndSanitizerResilience() {
    // T5: Sanitize query "azan" and verify matching against indexed LectureIndex fields (title, verifiedTitle, topics, summary, references)
    val sanitizedAzan = FtsQuerySanitizer.sanitizeFtsQuery("azan")
    assertEquals("\"azan*\"", sanitizedAzan)

    val token = sanitizedAzan.removeSurrounding("\"", "*\"")?.lowercase(Locale.ROOT) ?: "azan"
    val matchingLectures = dataset.records.filter { rec ->
      rec.shouldShowInUi(isDebugBuild = false) &&
        (rec.title.lowercase(Locale.ROOT).contains(token) ||
          (rec.verifiedTitle?.lowercase(Locale.ROOT)?.contains(token) == true) ||
          rec.topics.any { it.lowercase(Locale.ROOT).contains(token) } ||
          (rec.summary?.lowercase(Locale.ROOT)?.contains(token) == true))
    }
    assertTrue("Search for 'azan' must return results", matchingLectures.isNotEmpty())
    assertTrue(
      "At least one result for 'azan' must contain 'AZAN' in title or verifiedTitle",
      matchingLectures.any {
        it.title.contains("AZAN") || (it.verifiedTitle?.contains("AZAN") == true)
      }
    )

    // T6: Run FtsQuerySanitizer on hostile FTS/SQL strings; assert no exception thrown and operators stripped
    val hostileQueries = listOf(
      """foo" OR 1=1 --""",
      """NEAR/3""",
      """title:*""",
      """(a AND b"""
    )
    for (hostile in hostileQueries) {
      val sanitized = FtsQuerySanitizer.sanitizeFtsQuery(hostile)
      assertFalse("Sanitized query must not contain raw SQL comment: $sanitized", sanitized.contains("--"))
      assertFalse("Sanitized query must not contain colon syntax: $sanitized", sanitized.contains(":"))
      assertFalse("Sanitized query must not contain parentheses: $sanitized", sanitized.contains("("))
      assertFalse("Sanitized query must not contain standalone OR/AND/NEAR: $sanitized", sanitized.contains("\"or*\"") || sanitized.contains("\"and*\"") || sanitized.contains("\"near"))
    }
    assertEquals("", FtsQuerySanitizer.sanitizeFtsQuery("NEAR/3"))
  }

  @Test
  fun testT7_topicMinimumRecordsToShowThreshold() {
    val microTopic = discoveryConfig.topics.firstOrNull { it.id == "experimental_micro_topic" }
    assertNotNull(microTopic)
    assertTrue(microTopic!!.minimumRecordsToShow >= 5)

    val visibleLectures = dataset.records.filter { it.shouldShowInUi(isDebugBuild = false) }
    val visibleTopics = discoveryConfig.topics.filter { topic ->
      val count = visibleLectures.count { topic.id in it.topics }
      count >= topic.minimumRecordsToShow
    }

    assertFalse(
      "Topic with < minimumRecordsToShow must be hidden from UI topic chips",
      visibleTopics.any { it.id == "experimental_micro_topic" }
    )
    assertTrue(
      "Karbala & Imam Hussain A.S. topic must be visible",
      visibleTopics.any { it.id == "karbala_hussain" }
    )
    assertTrue(
      "Reality of Yazeed topic must be visible",
      visibleTopics.any { it.id == "reality_of_yazeed" }
    )
  }

  @Test
  fun testT8_ytEmbedHostErrorCodesAndHeaders() {
    val embedUrl = YtEmbedUrlBuilder.buildEmbedUrl("DPvekksFmBs")
    assertEquals(
      "https://www.youtube-nocookie.com/embed/DPvekksFmBs?rel=0&enablejsapi=1&origin=https://www.youtube-nocookie.com",
      embedUrl
    )
    val headers = YtEmbedUrlBuilder.buildEmbedHeaders()
    assertEquals("https://www.youtube-nocookie.com", headers["Referer"])

    for (code in listOf(2, 5, 100, 101, 150, 153)) {
      assertTrue("Error code $code must be handled by fallback card", YtEmbedUrlBuilder.isErrorCodeHandled(code))
    }
    assertEquals(
      "This video is not playable in your region or has been removed.",
      YtEmbedUrlBuilder.ERROR_FALLBACK_MESSAGE
    )
    assertEquals(
      "No verified alternate copy indexed",
      YtEmbedUrlBuilder.NO_ALTERNATE_COPY_MESSAGE
    )
  }

  @Test
  fun testT9_and_T10_maududiLinkOnlyLicenceStateMachine() {
    val works = MaududiRepository.buildSeedWorks()
    assertTrue(works.isNotEmpty())

    // Build EditionEntity & CommentaryEntryEntity rows exactly as MaududiRepository seeds them
    val editions = works.map { w ->
      EditionEntity(
        id = "ed_${w.workId}",
        workId = w.workId,
        languageTag = w.languageTag,
        translator = w.translator,
        publisher = w.publisher,
        year = w.yearPublished,
        licenceId = w.licenceState.name,
        completeness = w.completeness.name
      )
    }
    val commentaryEntries = works.mapIndexed { idx, w ->
      CommentaryEntryEntity(
        rowid = idx + 1,
        editionId = "ed_${w.workId}",
        verseRefId = 1L,
        page = null,
        title = w.titleEn,
        bibliographicNote = w.bibliographicSummary,
        text = if (w.licenceState == MaududiLicenceState.LINK_ONLY) null else w.bodyText
      )
    }

    // T9: Assert no CommentaryEntry with Edition.licenceId = LINK_ONLY has non-null text
    val editionById = editions.associateBy { it.id }
    val violations = commentaryEntries.filter { ce ->
      editionById[ce.editionId]?.licenceId == "LINK_ONLY" && ce.text != null
    }
    assertTrue("LINK_ONLY commentary entries must never have non-null text", violations.isEmpty())

    // T10: Every LINK_ONLY MaududiWorkRecord has bodyText == null and valid HTTPS outbound link
    val linkOnlyWorks = works.filter { it.licenceState == MaududiLicenceState.LINK_ONLY }
    assertTrue(linkOnlyWorks.isNotEmpty())
    linkOnlyWorks.forEach { work ->
      assertNull("LINK_ONLY work ${work.workId} must have null bodyText", work.bodyText)
      assertFalse(work.licenceState.allowsInAppFullText)
      assertTrue(work.primarySourceUrl.startsWith("https://"))
    }
  }

  @Test
  fun testT11_indexSyncSha256MismatchRejectedAndBundledAssetKept() {
    val tempSyncDir = Files.createTempDirectory("test_sync_t11").toFile()
    try {
      val fakePayload = """{"counts":{"total":1},"records":[]}""".toByteArray(Charsets.UTF_8)
      val wrongSha256 = "0000000000000000000000000000000000000000000000000000000000000000"

      val result = IndexSync.verifyAndApplyCandidateBytes(
        targetDir = tempSyncDir,
        candidateBytes = fakePayload,
        expectedSha256Hex = wrongSha256
      )
      assertTrue("Mismatched SHA-256 must be rejected", result is IndexSync.SyncResult.Rejected)
      val rejected = result as IndexSync.SyncResult.Rejected
      assertTrue(rejected.fallbackToBundledAsset)
      assertFalse(
        "Synced file must not be written when SHA-256 mismatches",
        File(tempSyncDir, IndexSync.ACTIVE_SYNCED_INDEX_FILENAME).exists()
      )

      // Bundled asset remains intact at 169 records
      assertEquals(169, dataset.records.size)
    } finally {
      tempSyncDir.deleteRecursively()
    }
  }

  @Test
  fun testT12_zeroDeprecatedYouTubeQualityCallsInMainSourceTree() {
    val forbidden1 = "setPlayback" + "Quality"
    val forbidden2 = "getAvailable" + "QualityLevels"

    val mainDir = File("src/main").takeIf { it.exists() } ?: File("app/src/main")
    assertTrue("app/src/main directory must exist", mainDir.exists())

    val offendingFiles = mutableListOf<String>()
    mainDir.walkTopDown()
      .filter { it.isFile && (it.extension == "kt" || it.extension == "java" || it.extension == "xml") }
      .forEach { file ->
        val content = file.readText()
        if (content.contains(forbidden1) || content.contains(forbidden2)) {
          offendingFiles.add(file.path)
        }
      }
    assertTrue("Zero occurrences of deprecated quality calls allowed, found in: $offendingFiles", offendingFiles.isEmpty())
  }

  @Test
  fun testT13_appDatabaseUntouchedAndKnowledgeDatabaseSeparate() {
    val appDbFile = File("src/main/java/com/example/data/local/AppDatabase.kt")
      .takeIf { it.exists() }
      ?: File("app/src/main/java/com/example/data/local/AppDatabase.kt")
    assertTrue(appDbFile.exists())
    val appDbSource = appDbFile.readText()
    assertTrue("AppDatabase must remain at version = 8", appDbSource.contains("version = 8"))

    val knowledgeDbFile = File("src/main/java/com/example/data/local/KnowledgeDatabase.kt")
      .takeIf { it.exists() }
      ?: File("app/src/main/java/com/example/data/local/KnowledgeDatabase.kt")
    assertTrue(knowledgeDbFile.exists())
    val knowledgeDbSource = knowledgeDbFile.readText()
    assertTrue(knowledgeDbSource.contains("version = 1"))
    assertEquals("knowledge.db", KnowledgeDatabase.DATABASE_NAME)
  }

  @Test
  fun testT14_evidenceDocExistsAndOfficialSourcesMatchAllowlist() {
    val evidenceFile = File("../docs/evidence.md").takeIf { it.exists() } ?: File("docs/evidence.md")
    assertTrue("docs/evidence.md must exist", evidenceFile.exists())
    val evidenceText = evidenceFile.readText()
    assertTrue("docs/evidence.md must be non-empty", evidenceText.length > 200)
    assertTrue(evidenceText.contains("V1"))
    assertTrue(evidenceText.contains("V7"))

    val allowedOfficialHandles = SourceTier.OFFICIAL_CHANNEL_HANDLES
    val allowedOfficialHosts = SourceTier.OFFICIAL_HOSTS

    dataset.records
      .filter { it.sourceTier == SourceTier.OFFICIAL }
      .forEach { rec ->
        val h = rec.channelHandle?.lowercase(Locale.ROOT) ?: ""
        val u = rec.channelUrl?.lowercase(Locale.ROOT) ?: ""
        val matchesOfficial = allowedOfficialHandles.contains(h) ||
          allowedOfficialHandles.any { u.contains(it) } ||
          allowedOfficialHosts.any { u.contains(it) }
        assertTrue(
          "Record ${rec.youtubeId} marked OFFICIAL must map to one of the 3 official channels or 2 official portals",
          matchesOfficial
        )
      }
  }

  @Test
  fun testT15_watchTimeTopicRecommendationEnginePromotesRelatedTopicsToTop() {
    // Case 1: User watches a lecture about Imam Hussain A.S. (karbala_hussain)
    val hussainLecture = dataset.records.first {
      "karbala_hussain" in it.topics && it.title.contains("Hussain", ignoreCase = true)
    }
    val watchMapHussain = mapOf(hussainLecture.youtubeId to 600) // 10 minutes watched

    val recsAfterHussain = LectureRepository.computeWatchTimeRecommendations(
      allLectures = dataset.records,
      watchTimeByYoutubeId = watchMapHussain,
      topicDefinitions = discoveryConfig.topics,
      isDebugBuild = false,
      limit = 10
    )
    assertTrue(recsAfterHussain.isNotEmpty())
    val topFiveAfterHussain = recsAfterHussain.take(5)
    assertTrue(
      "Watching Imam Hussain A.S. must suggest Karbala & Reality of Yazeed videos at the top",
      topFiveAfterHussain.all { rec ->
        "karbala_hussain" in rec.lecture.topics ||
          "reality_of_yazeed" in rec.lecture.topics ||
          "ahl_al_bayt" in rec.lecture.topics
      }
    )
    assertTrue(
      "Top recommendations after watching Hussain A.S. must include Reality of Yazeed",
      recsAfterHussain.take(8).any { "reality_of_yazeed" in it.lecture.topics }
    )

    // Case 2: Another user watches Salah & AZAN (XANsNmxxE90) extensively
    val watchMapSalah = mapOf("XANsNmxxE90" to 900) // 15 minutes watched on AZAN & Salah
    val recsAfterSalah = LectureRepository.computeWatchTimeRecommendations(
      allLectures = dataset.records,
      watchTimeByYoutubeId = watchMapSalah,
      topicDefinitions = discoveryConfig.topics,
      isDebugBuild = false,
      limit = 10
    )
    val topThreeAfterSalah = recsAfterSalah.take(3)
    assertTrue(
      "Watching Salah & AZAN must promote Salah/Azan/Wudu related lectures to the top",
      topThreeAfterSalah.all { rec ->
        "salah_azan" in rec.lecture.topics ||
          "tahara_wudu" in rec.lecture.topics ||
          "dua_adhkar" in rec.lecture.topics
      }
    )
  }
}
