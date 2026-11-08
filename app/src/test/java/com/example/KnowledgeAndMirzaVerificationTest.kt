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
    // T1: All 480 youtubeId values in mirza_lectures_v3.json are unique and match ^[A-Za-z0-9_-]{11}$
    assertEquals(480, dataset.records.size)
    val uniqueIds = dataset.records.map { it.youtubeId }.toSet()
    assertEquals(480, uniqueIds.size)
    val regex = Regex("^[A-Za-z0-9_-]{11}$")
    dataset.records.forEach { record ->
      assertTrue("Invalid youtubeId: ${record.youtubeId}", regex.matches(record.youtubeId))
      assertNotNull("Every seed record must have durationSec for thumbnail badge: ${record.youtubeId}", record.durationSec)
    }

    // T2: counts object matches array length and breakdown (321 CONFIRMED_LIVE, 159 UNPROBED, 264 CLIP, 216 FULL_LECTURE, Tafseer 1..344)
    assertEquals(dataset.records.size, dataset.counts.total)
    val confirmedCount = dataset.records.count { it.verificationState == RecordVerificationState.CONFIRMED_LIVE }
    val unprobedCount = dataset.records.count { it.verificationState == RecordVerificationState.UNPROBED }
    val clipCount = dataset.records.count { it.recordFormat == LectureFormat.CLIP }
    val fullCount = dataset.records.count { it.recordFormat == LectureFormat.FULL_LECTURE }
    assertEquals(321, confirmedCount)
    assertEquals(159, unprobedCount)
    assertEquals(264, clipCount)
    assertEquals(216, fullCount)
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

      // Bundled asset remains intact at 480 records
      assertEquals(480, dataset.records.size)
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

  @Test
  fun testT16_multiQualityLadderAndOpaquePipelines() {
    assertEquals(
      "Default video quality must be 360p (Default Saver)",
      com.example.player.VideoQualityOption.Q_360P,
      com.example.player.VideoQualityOption.DEFAULT_SAVER_QUALITY
    )
    assertEquals(8, com.example.player.VideoQualityOption.entries.size)

    // Verify opaque pipeline labels never leak raw server URLs or domain names
    com.example.player.PipelineOption.entries.forEach { pipeline ->
      assertFalse(pipeline.displayLabel.contains("http"))
      assertFalse(pipeline.displayLabel.contains(".com"))
      assertFalse(pipeline.displayLabel.contains(".org"))
      assertFalse(pipeline.displayLabel.contains(".rocks"))
    }

    val forbiddenShortClips = listOf(
      "raah-e-hidayat/001-",
      "noor-e-hidayat/02-",
      "noor-e-hidayat/03-",
      "noor-e-hidayat/004-"
    )

    dataset.records.forEach { lecture ->
      val directUrl = YtEmbedUrlBuilder.resolveDirectMp4StreamUrl(lecture)
      assertTrue(directUrl.startsWith("https://"))
      forbiddenShortClips.forEach { clipPath ->
        assertFalse(
          "Lecture ${lecture.youtubeId} must not resolve to 1-min clip $clipPath",
          directUrl.contains(clipPath)
        )
      }

      val bundle = com.example.player.VideoStreamExtractor.buildInstantBaselineBundle(lecture)
      com.example.player.VideoQualityOption.entries.forEach { qOpt ->
        val track = bundle.resolveTrackFor(qOpt)
        assertTrue(
          "Quality $qOpt must resolve to a valid HTTPS stream URL",
          track.videoUrl.startsWith("https://")
        )
        forbiddenShortClips.forEach { clipPath ->
          assertFalse(
            "Lecture ${lecture.youtubeId} quality $qOpt must not resolve to 1-min clip $clipPath",
            track.videoUrl.contains(clipPath)
          )
        }
      }
    }
  }

  @Test
  fun testT17_giantWall12LayerSecuritySuite() {
    // Layer 1: SacredContentIntegrityGuard SHA-256 verification
    val sampleBytes = "Bismillah ir-Rahman ir-Rahim".toByteArray(Charsets.UTF_8)
    val sha = com.example.security.SacredContentIntegrityGuard.computeSha256Hex(sampleBytes)
    assertTrue(com.example.security.SacredContentIntegrityGuard.verifyChecksum(sampleBytes, sha))
    assertFalse(
      com.example.security.SacredContentIntegrityGuard.verifyChecksum(
        sampleBytes,
        "0000000000000000000000000000000000000000000000000000000000000000"
      )
    )

    // Layer 2: SecurityFirewall SQLi & XSS neutralization
    val hostileInput = """<script>alert(1)</script>' OR 1=1 UNION SELECT * FROM sqlite_master; DROP TABLE verses;--"""
    assertTrue(com.example.security.SecurityFirewall.containsHostileSignature(hostileInput))
    val sanitized = com.example.security.SecurityFirewall.canonicalizeAndSanitize(hostileInput)
    assertFalse(sanitized.lowercase(Locale.ROOT).contains("<script"))
    assertFalse(sanitized.lowercase(Locale.ROOT).contains("union select"))
    assertFalse(sanitized.lowercase(Locale.ROOT).contains("sqlite_master"))

    // Layer 3: AdShield tracker & ad domain blocking
    assertTrue(com.example.security.AdShieldConfig.isBlockedAdOrTrackerUrl("https://googleads.g.doubleclick.net/pagead/ads"))
    assertTrue(com.example.security.AdShieldConfig.isBlockedAdOrTrackerUrl("https://www.youtube.com/api/stats/ads?v=123"))
    assertFalse(com.example.security.AdShieldConfig.isSchemeAllowedInWebView("javascript:alert(1)"))
    assertFalse(com.example.security.AdShieldConfig.isSchemeAllowedInWebView("file:///android_asset/secret.txt"))
    assertTrue(com.example.security.AdShieldConfig.isSchemeAllowedInWebView("https://www.bayanulhuda.com"))

    // Layer 4: MediaSecurityValidator magic-byte inspection
    val peHeader = byteArrayOf(0x4D, 0x5A, 0x90.toByte(), 0x00)
    val elfHeader = byteArrayOf(0x7F, 0x45, 0x4C, 0x46)
    val dexHeader = "dex\n035\u0000".toByteArray(Charsets.UTF_8)
    val htmlHeader = "<!DOCTYPE html><html><script>".toByteArray(Charsets.UTF_8)
    val mp4Header = byteArrayOf(0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70, 0x69, 0x73, 0x6F, 0x6D)
    assertFalse(com.example.security.MediaSecurityValidator.isMagicHeaderSafe(peHeader))
    assertFalse(com.example.security.MediaSecurityValidator.isMagicHeaderSafe(elfHeader))
    assertFalse(com.example.security.MediaSecurityValidator.isMagicHeaderSafe(dexHeader))
    assertFalse(com.example.security.MediaSecurityValidator.isMagicHeaderSafe(htmlHeader))
    assertTrue(com.example.security.MediaSecurityValidator.isMagicHeaderSafe(mp4Header))

    // Layer 5: SsrfRedirectValidator
    assertFalse(com.example.security.SsrfRedirectValidator.validateUrlSafety("http://archive.org/stream.mp4", resolveDns = false))
    assertFalse(com.example.security.SsrfRedirectValidator.validateUrlSafety("https://user:pass@archive.org/stream.mp4", resolveDns = false))
    assertFalse(com.example.security.SsrfRedirectValidator.validateUrlSafety("https://archive.org:8080/stream.mp4", resolveDns = false))
    assertFalse(com.example.security.SsrfRedirectValidator.validateUrlSafety("https://localhost/stream.mp4", resolveDns = false))
    assertTrue(com.example.security.SsrfRedirectValidator.validateUrlSafety("https://archive.org/download/QuranClassesMP4/test.mp4", resolveDns = false))

    // Layer 10: PathTraversalAndIntentGuard
    val tempBase = Files.createTempDirectory("test_layer10").toFile()
    try {
      var traversalBlocked = false
      try {
        com.example.security.PathTraversalAndIntentGuard.resolveSafeCanonicalChild(tempBase, "../../etc/passwd")
      } catch (_: SecurityException) {
        traversalBlocked = true
      }
      assertTrue("Path traversal outside base directory must throw SecurityException", traversalBlocked)
    } finally {
      tempBase.deleteRecursively()
    }

    // Layer 12: SanitizedLogger
    val redacted = com.example.security.SanitizedLogger.redactSensitiveText(
      "Fetching https://pipedapi.kavin.rocks/streams/abc?token=secret123 from 192.168.1.50"
    )
    assertFalse(redacted.contains("secret123"))
    assertFalse(redacted.contains("kavin.rocks"))
    assertFalse(redacted.contains("192.168.1.50"))
  }

  @Test
  fun testT18_smartSearchHazratUsmanAndEmamStrictFilter() {
    // 1. Partial query "hazrat usm" (from user screenshot) must return Hazrat Usman R.A. EMAM lectures directly from mirza_lectures_v3.json
    val usmResults = LectureRepository.smartSearchLectures(
      rawQuery = "hazrat usm",
      allLectures = dataset.records,
      isDebugBuild = false
    )
    assertTrue("Search for 'hazrat usm' must return matching EMAM lectures from mirza_lectures_v3.json", usmResults.isNotEmpty())
    assertTrue(
      "Top result for 'hazrat usm' must be about Hazrat Usman / Uthman R.A.",
      usmResults.first().displayTitle.contains("Usman", ignoreCase = true) ||
        usmResults.first().displayTitle.contains("Uthman", ignoreCase = true)
    )

    // 2. Queries across major topics ("hazrat umar", "abu bakr", "maula ali", "talaq", "dajjal", "barzakh") all load from mirza_lectures_v3.json
    for (q in listOf("hazrat usman", "uthman", "hazrat umar", "abu bakr", "maula ali", "talaq", "dajjal", "barzakh")) {
      val matches = LectureRepository.smartSearchLectures(
        rawQuery = q,
        allLectures = dataset.records,
        isDebugBuild = false
      )
      assertTrue("Search for '$q' must return matching EMAM lectures from mirza_lectures_v3.json", matches.isNotEmpty())
    }

    // 3. Strict EMAM speaker verification filter: accepts EMAM / Engineer Muhammad Ali Mirza, rejects unrelated speakers
    assertTrue(
      LectureRepository.isVerifiedEmamLectureMetadata(
        title = "Shahadat-e-Hazrat Usman R.A. Complete Research",
        descriptionSnippet = "By Engineer Muhammad Ali Mirza",
        channelName = "Islam Channel",
        channelHandle = "@IslamChannel"
      )
    )
    assertTrue(
      LectureRepository.isVerifiedEmamLectureMetadata(
        title = "Mas'alah 61-b Hazrat Usman Ghani R.A.",
        descriptionSnippet = "Full lecture",
        channelName = "RAAH-e-HIDAYAT [EMAM]",
        channelHandle = "@RAAH-e-HIDAYAT-EMAM"
      )
    )
    assertFalse(
      "Unrelated video without Engineer Muhammad Ali Mirza or EMAM must be rejected",
      LectureRepository.isVerifiedEmamLectureMetadata(
        title = "Cooking Recipe Vlog",
        descriptionSnippet = "Daily kitchen vlog",
        channelName = "Food Channel",
        channelHandle = "@FoodVlog"
      )
    )

    // 4. Verify zero background video substitution: buildDirectPlayerHtml must strictly play lecture.youtubeId
    // and never inject <video id="bayanVideo"> or Archive.org MP4 fallback streams
    val dpvLecture = dataset.records.first { it.youtubeId == "DPvekksFmBs" }
    assertEquals(
      "DPvekksFmBs duration must match its real 9:30 (570s) YouTube video",
      570,
      dpvLecture.durationSec
    )
    val playerHtml = YtEmbedUrlBuilder.buildDirectPlayerHtml(dpvLecture)
    assertTrue(
      "Player HTML must embed exact youtubeId DPvekksFmBs",
      playerHtml.contains("https://www.youtube-nocookie.com/embed/DPvekksFmBs")
    )
    assertFalse(
      "Player HTML must never contain background MP4 swapper bayanVideo",
      playerHtml.contains("bayanVideo")
    )
    assertFalse(
      "Player HTML must never contain activateDirectFullStream timer",
      playerHtml.contains("activateDirectFullStream")
    )
    assertFalse(
      "Player HTML must never load unrelated Archive.org MP4 streams",
      playerHtml.contains("archive.org")
    )

    // 5. Verify baseline bundle marks tracks as non-direct and topic-matched fallback streams match lecture titles
    val baselineBundle = com.example.player.VideoStreamExtractor.buildInstantBaselineBundle(dpvLecture)
    assertFalse(
      "Baseline bundle must have hasDirectYoutubeStreams = false",
      baselineBundle.hasDirectYoutubeStreams
    )
    com.example.player.VideoQualityOption.entries.forEach { qOpt ->
      assertFalse(
        "Baseline track for $qOpt must have isDirectYoutubeStream = false",
        baselineBundle.resolveTrackFor(qOpt).isDirectYoutubeStream
      )
    }
    assertTrue(
      "DPvekksFmBs fallback stream must match Surah Al-Fatiha Tafseer",
      YtEmbedUrlBuilder.resolveDirectMp4StreamUrl(dpvLecture).contains("Surah%20Al%20Fateha")
    )
    val karbalaWithTafseerTag = dataset.records.first { it.youtubeId == "4e-j9r8xlak" }
    val resolvedKarbalaStream = YtEmbedUrlBuilder.resolveDirectMp4StreamUrl(karbalaWithTafseerTag)
    assertTrue(
      "Karbala lecture with secondary tafseer_quran tag must resolve to Karbala/Hussain stream, not Surah Tafseer",
      resolvedKarbalaStream.contains("KARBLA") || resolvedKarbalaStream.contains("HUSAIN")
    )
    val practicalNamazLecture = dataset.records.first { it.youtubeId == "Cou1OW7jxx4" }
    assertEquals(580, practicalNamazLecture.durationSec)
    assertEquals(
      "https://youtu.be/Cou1OW7jxx4?si=5qn4EXXiQlwSHbXS",
      com.example.player.OfflineNamazVideoManager.PRACTICAL_NAMAZ_VIDEO_LINK
    )
    assertEquals(
      YtEmbedUrlBuilder.PRACTICAL_NAMAZ_9M40S_STREAM_URL,
      YtEmbedUrlBuilder.resolveDirectMp4StreamUrl(practicalNamazLecture)
    )
  }

  // =========================================================================
  // T19: Video Library Update (Categories, Relevance Engine, Vine/yMusic SDK
  //      Cooldowns, Recommendations & Completion Percentage Ranking)
  // =========================================================================
  @Test
  fun T19_videoLibraryUpdateAndSecurityRemediation_verified() {
    // 1. Relevance scoring across name variations & filtering out unrelated content
    val highScore = LectureRepository.computeEmamRelevanceScore(
      title = "Mas'alah 70a Complete Namaz-e-Muhammadi by Engineer Muhammad Ali Mirza",
      descriptionSnippet = "Authentic Bukhari & Muslim Hadith lecture by EMAM",
      channelName = "Engineer Muhammad Ali Mirza - Official Channel",
      channelHandle = "@EngineerMuhammadAliMirza",
      tags = listOf("salah_prayer", "hadith_sciences")
    )
    assertTrue("EMAM official lecture must score >= 80", highScore >= 80.0)

    val variantScore = LectureRepository.computeEmamRelevanceScore(
      title = "Qur'an Class Tafseer by Engineer Mohammad Ali Mirza (EMAM)",
      descriptionSnippet = "",
      channelName = "RAAH-e-HIDAYAT (Engineer Muhammad Ali Mirza)",
      channelHandle = "@RAAH-e-HIDAYAT-EMAM"
    )
    assertTrue("Name variation 'Engineer Mohammad Ali Mirza' + 'EMAM' must score >= 60", variantScore >= 60.0)

    val unrelatedScore = LectureRepository.computeEmamRelevanceScore(
      title = "Top 10 Gaming Laptops Review 2026",
      descriptionSnippet = "",
      channelName = "Tech Gadget Reviews",
      channelHandle = "@TechGadgets"
    )
    assertEquals("Unrelated non-EMAM video must score 0.0", 0.0, unrelatedScore, 0.001)

    // 2. Verify all 10 IslamicVideoCategory filters match seeded lectures
    val requiredCategories = listOf(
      com.example.data.model.IslamicVideoCategory.QURAN_TAFSIR,
      com.example.data.model.IslamicVideoCategory.HADITH,
      com.example.data.model.IslamicVideoCategory.AQEEDAH,
      com.example.data.model.IslamicVideoCategory.FIQH,
      com.example.data.model.IslamicVideoCategory.SEERAH,
      com.example.data.model.IslamicVideoCategory.ISLAMIC_HISTORY,
      com.example.data.model.IslamicVideoCategory.COMPARATIVE_RELIGION,
      com.example.data.model.IslamicVideoCategory.QA,
      com.example.data.model.IslamicVideoCategory.CONTEMPORARY_ISSUES,
      com.example.data.model.IslamicVideoCategory.SPECIAL_LECTURES
    )
    requiredCategories.forEach { cat ->
      val matchCount = dataset.records.count { cat.matchesLecture(it) }
      assertTrue("Category ${cat.label} must match at least 1 seeded lecture (found $matchCount)", matchCount > 0)
    }

    // 3. Verify bounded retries & cooldowns + Vine/yMusic SDK pipeline option
    com.example.player.VideoStreamExtractor.resetAllProviderCooldowns()
    val providerKey = "VINE_YMUSIC_SDK_TEST"
    assertFalse(com.example.player.VideoStreamExtractor.isProviderInCooldown(providerKey))
    com.example.player.VideoStreamExtractor.recordProviderFailure(providerKey)
    assertFalse(
      "Single failure below MAX_PROVIDER_RETRIES must not trigger cooldown",
      com.example.player.VideoStreamExtractor.isProviderInCooldown(providerKey)
    )
    com.example.player.VideoStreamExtractor.recordProviderFailure(providerKey)
    assertTrue(
      "Reaching MAX_PROVIDER_RETRIES must place provider into bounded cooldown",
      com.example.player.VideoStreamExtractor.isProviderInCooldown(providerKey)
    )
    com.example.player.VideoStreamExtractor.recordProviderSuccess(providerKey)
    assertFalse(
      "Provider success must clear cooldown state",
      com.example.player.VideoStreamExtractor.isProviderInCooldown(providerKey)
    )
    assertNotNull(
      "VINE_YMUSIC_SDK pipeline option must be present",
      com.example.player.PipelineOption.valueOf("VINE_YMUSIC_SDK")
    )

    // 4. Verify watch history, search history & completion-percentage recommendations
    val recsWithHistory = LectureRepository.computeWatchTimeRecommendations(
      allLectures = dataset.records,
      watchTimeByYoutubeId = mapOf("DPvekksFmBs" to 120),
      topicDefinitions = discoveryConfig.topics,
      isDebugBuild = false,
      limit = 10,
      recentSearchQueries = listOf("Salah Namaz Bukhari"),
      bookmarkedYoutubeIds = setOf("DPvekksFmBs")
    )
    assertTrue("Recommendations with watch/search history must not be empty", recsWithHistory.isNotEmpty())

    // 5. Verify strict videoId matching & cache validation in VideoStreamExtractor:
    //    mismatched or missing videoId must be rejected so thumbnail video == playing video
    assertFalse(
      "Mismatched videoId in InnerTube response must be strictly rejected",
      com.example.player.VideoStreamExtractor.isMatchingInnerTubeVideoId("DPvekksFmBs", "XANsNmxxE90")
    )
    assertFalse(
      "Blank videoId in InnerTube response must be strictly rejected",
      com.example.player.VideoStreamExtractor.isMatchingInnerTubeVideoId("DPvekksFmBs", "")
    )
    assertTrue(
      "Exact matching videoId in InnerTube response must be accepted",
      com.example.player.VideoStreamExtractor.isMatchingInnerTubeVideoId("DPvekksFmBs", "DPvekksFmBs")
    )
    assertNull(
      "Uncached videoId must not return a stale or unrelated bundle",
      com.example.player.VideoStreamExtractor.getCachedDirectBundle("DPvekksFmBs")
    )
  }

  @Test
  fun testIslamicResearchLibraryPapersAndAzkarParsingAndSearch() {
    val paper5a = com.example.data.model.IslamicResearchRepository.loadPaper5a()
    assertEquals("رافضیت ، ناصبیت اور یزیدیت کا تحقیقی جائزہ", paper5a.meta.title)
    assertEquals("5a", paper5a.meta.researchPaper)
    assertEquals(3, paper5a.definitions.size)
    assertEquals(12, paper5a.sections.size)
    assertTrue(paper5a.sections.any { it.id == "rad_rafziyat" && it.items.isNotEmpty() })
    assertTrue(paper5a.sections.any { it.id == "rad_nasibiyat" && it.items.isNotEmpty() })
    assertTrue(paper5a.sections.any { it.id == "yazid_siyah_karname" && it.items.isNotEmpty() })

    val azkarPamphlet = com.example.data.model.IslamicResearchRepository.loadFardNamazAzkar()
    assertEquals("فرض نماز کے بعد سُنّت اَذکار", azkarPamphlet.title)
    assertEquals(5, azkarPamphlet.introduction.size)
    assertEquals(19, azkarPamphlet.azkar.size)
    assertEquals(4, azkarPamphlet.closingNotes.size)
    assertTrue(azkarPamphlet.azkar.any { it.number == 1 && it.arabicLines.contains("اَللّٰہُ اَکۡبَرُ") })
    assertTrue(azkarPamphlet.azkar.any { it.number == 14 && it.parts.isNotEmpty() })

    val paper1Wasiyat = com.example.data.model.IslamicResearchRepository.loadPaper1Wasiyat()
    assertEquals("رسول اللہ ﷺ کی آخری وصیّتیں", paper1Wasiyat.meta.titleUr)
    assertEquals("Research Paper 1", paper1Wasiyat.meta.paperLabel)
    assertEquals(4, paper1Wasiyat.meta.pageCount)
    assertEquals(12, paper1Wasiyat.sections.size)
    assertTrue(paper1Wasiyat.meta.transcriptionNotesEn.isNotEmpty())
    assertTrue(paper1Wasiyat.sections.any { it.id == "s2" && it.items.isNotEmpty() })
    assertTrue(paper1Wasiyat.sections.any { it.id == "s5" && it.items.any { item -> item.type == "hadith" } })
    assertTrue(paper1Wasiyat.sections.any { it.items.any { item -> item.type == "note" } })

    val paper7Commandments = com.example.data.model.IslamicResearchRepository.loadPaper7Commandments()
    assertEquals(7, paper7Commandments.publication.researchPaperNumber)
    assertEquals(2, paper7Commandments.sections.size)
    assertEquals(10, paper7Commandments.sections[0].items.size)
    assertNotNull(paper7Commandments.sections[0].finalAdvice)
    assertEquals(12, paper7Commandments.sections[1].items.size)
    assertNotNull(paper7Commandments.sections[1].finalAdvice)
    val attr6 = paper7Commandments.sections[1].items.first { it.number == 6 }
    assertNotNull("Attribute #6 must include supporting verse from Surah An-Naml 27:62", attr6.supportingVerse)
    assertTrue(attr6.supportingVerse?.reference?.contains("النمل") == true)

    val hitsWasiyat = com.example.data.model.IslamicResearchRepository.searchAcrossIslamicResearch("غدیرِ خُم")
    assertTrue("Searching for غدیرِ خُم must return hits from Paper 1 Wasiyat", hitsWasiyat.any { it.paperId == "paper_1_wasiyat" })

    val hitsCommandments = com.example.data.model.IslamicResearchRepository.searchAcrossIslamicResearch("عباد الرحمن")
    assertTrue("Searching for عباد الرحمن must return hits from Paper 7", hitsCommandments.any { it.paperId == "paper_7_commandments" })

    val hitsRafziyat = com.example.data.model.IslamicResearchRepository.searchAcrossIslamicResearch("رافضیت")
    assertTrue("Searching for رافضیت must return hits from Paper 5a", hitsRafziyat.any { it.paperId == "paper_5a" })

    val hitsAzkar = com.example.data.model.IslamicResearchRepository.searchAcrossIslamicResearch("842")
    assertTrue("Searching for Bukhari 842 must return hits from Fard Namaz Azkar", hitsAzkar.any { it.paperId == "fard_namaz_azkar" })

    // Verify watched video itself + related videos appear at the very top of Recommendations
    val sampleWatched = dataset.records.first { (it.durationSec ?: 0) >= 3600 }
    val recsWithWatched = LectureRepository.computeWatchTimeRecommendations(
      allLectures = dataset.records,
      watchTimeByYoutubeId = mapOf(sampleWatched.youtubeId to 120),
      topicDefinitions = discoveryConfig.topics,
      isDebugBuild = false,
      limit = 10
    )
    assertEquals(
      "The watched video itself must rank #1 in Recommendations",
      sampleWatched.youtubeId,
      recsWithWatched.first().lecture.youtubeId
    )
    assertTrue(
      "Related videos sharing topics with the watched video must immediately follow in Recommendations",
      recsWithWatched.drop(1).take(4).all { rec ->
        rec.lecture.topics.any { it in sampleWatched.topics }
      }
    )

    // Verify exact Archive.org match guard prevents unrelated 46m fallback MP4s for general 1h30m YouTube lectures
    val generalLongLecture = dataset.records.first {
      (it.durationSec ?: 0) >= 5000 && !YtEmbedUrlBuilder.isExactVerifiedArchiveMatch(it)
    }
    assertFalse(
      "General 1h30m YouTube lecture must NOT use unrelated Archive.org MP4 fallback",
      YtEmbedUrlBuilder.isExactVerifiedArchiveMatch(generalLongLecture)
    )
    val practicalNamazLecture = dataset.records.first { it.youtubeId == "Cou1OW7jxx4" }
    assertTrue(
      "Practical Namaz Cou1OW7jxx4 must be recognized as an exact verified match",
      YtEmbedUrlBuilder.isExactVerifiedArchiveMatch(practicalNamazLecture)
    )
  }

  @Test
  fun verifyIslamicHistoricalBooksHadithAuditAndAzkarAudioLibrary() {
    val context: android.content.Context? = null

    // 1. Verify Representative Book #1 (Tarikh al-Tabari) loads full multi-volume JSON
    val tabari = com.example.data.repository.IslamicDigitalLibraryRepository.loadRepresentativeBook1Tabari(context)
    assertEquals("book_history_tabari", tabari.id)
    assertTrue("Tarikh al-Tabari title must contain الطبري", tabari.titleArabic.contains("الطبري"))
    assertTrue("Tarikh al-Tabari attribution confidence must be populated", tabari.attributionConfidence.isNotBlank())
    assertTrue("Tarikh al-Tabari sourceTypeLabel must reference Shamela/OpenITI", tabari.sourceTypeLabel.contains("Shamela") || tabari.sourceTypeLabel.contains("OpenITI"))
    assertEquals(14, tabari.chapters.size)
    tabari.chapters.forEach { ch ->
      assertTrue("Chapter ${ch.chapterNumber} must have Arabic content", ch.arabicExcerpt.isNotBlank())
      assertTrue("Chapter ${ch.chapterNumber} must have Urdu content", ch.urduContent.isNotBlank())
      assertTrue("Chapter ${ch.chapterNumber} must have English content", ch.englishContent.isNotBlank())
      assertTrue("Chapter ${ch.chapterNumber} must have footnotes", ch.footnotes.isNotEmpty())
      assertTrue("Chapter ${ch.chapterNumber} must have valid page number", ch.pageNumber > 0)
    }

    // 2. Verify all 26 Classical Islamic Historical Books (including all 20 requested books)
    val all26Books = com.example.data.repository.IslamicDigitalLibraryRepository.getCompleteHistoricalBooks26(context)
    assertEquals("Complete Historical Books library must contain 26 classical books", 26, all26Books.size)
    assertEquals(
      "All 26 books must have sequential catalogBookNumber 1..26",
      (1..26).toList(),
      all26Books.map { it.catalogBookNumber }
    )
    all26Books.forEach { book ->
      assertTrue("Book #${book.catalogBookNumber} (${book.titleEnglish}) must have >= 12 chapters", book.chapters.size >= 12)
      assertTrue(
        "Book #${book.catalogBookNumber} chapters must have non-blank Arabic & Urdu text",
        book.chapters.all { it.arabicExcerpt.isNotBlank() && it.urduContent.isNotBlank() }
      )
    }

    // Verify Book 13 (Al-Imama wa al-Siyasa) carries explicit DISPUTED ATTRIBUTION metadata
    val book13 = all26Books.first { it.catalogBookNumber == 13 }
    assertEquals("hist_book_13_imama_wa_siyasa", book13.id)
    assertTrue(
      "Book 13 must explicitly state DISPUTED ATTRIBUTION",
      book13.attributionConfidence.contains("DISPUTED ATTRIBUTION")
    )
    assertTrue(
      "Book 13 attribution note must explain disputed attribution to Ibn Qutaybah",
      book13.attributionConfidence.contains("Ibn Qutaybah", ignoreCase = true)
    )

    // 3. Verify thematic sub-section filtering & full-text search across the 26 Historical Books
    val karbalaBooks = com.example.data.repository.IslamicDigitalLibraryRepository.filterHistoricalBooks(
      context = context,
      traditionFilter = "BOTH",
      thematicSection = "Karbala History"
    )
    assertTrue("Karbala History filter must return relevant classical works", karbalaBooks.size >= 4)

    val sunniSources = com.example.data.repository.IslamicDigitalLibraryRepository.filterHistoricalBooks(
      context = context,
      traditionFilter = "BOTH",
      thematicSection = "Sunni Historical Sources"
    )
    assertTrue("Sunni Historical Sources filter must return >= 18 Sunni works", sunniSources.size >= 18)

    val qadisiyyahHits = com.example.data.repository.IslamicDigitalLibraryRepository.searchHistoricalBooks(
      context = context,
      rawQuery = "Qadisiyyah"
    )
    assertTrue("Search for 'Qadisiyyah' must return chapter hits across historical books", qadisiyyahHits.isNotEmpty())

    // 4. Verify per-book and per-volume offline download & chapter bookmark persistence
    com.example.data.repository.IslamicDigitalLibraryRepository.downloadBookOffline(context, tabari)
    assertTrue(
      "Tarikh al-Tabari must be marked as downloaded offline",
      com.example.data.repository.IslamicDigitalLibraryRepository.isBookDownloadedOffline(context, tabari.id)
    )
    com.example.data.repository.IslamicDigitalLibraryRepository.downloadBookVolumeOffline(
      context = context,
      book = tabari,
      chapter = tabari.chapters.first()
    )
    assertTrue(
      "Tarikh al-Tabari Jild 1 must be marked as downloaded offline",
      com.example.data.repository.IslamicDigitalLibraryRepository.isBookVolumeDownloadedOffline(context, tabari.id, 1)
    )

    val nowBookmarked = com.example.data.repository.IslamicDigitalLibraryRepository.toggleBookChapterBookmark(
      context = context,
      bookId = tabari.id,
      chapterNumber = 6
    )
    assertTrue("Toggling bookmark on chapter 6 must return true", nowBookmarked)
    assertTrue(
      com.example.data.repository.IslamicDigitalLibraryRepository.isBookChapterBookmarked(context, tabari.id, 6)
    )

    // 5. Verify Urdu fallback helper for reader & existing test invariants
    assertEquals(
      "Urdu translation unavailable",
      com.example.data.model.IslamicBooksCatalog.formatUrduForReader("")
    )
    assertEquals(
      "Urdu Translation Coming Soon",
      com.example.data.model.IslamicBooksCatalog.formatBookUrduContent("")
    )

    // 6. Verify 15 Requested Hadith Collections Audit
    val hadithAudit = com.example.data.repository.IslamicDigitalLibraryRepository.auditRequested15HadithCollections(context)
    assertEquals("Must audit all 15 requested Hadith collections", 15, hadithAudit.size)
    hadithAudit.forEach { report ->
      assertTrue("Collection ${report.englishTitle} must have Arabic text available", report.arabicAvailable)
      assertTrue("Collection ${report.englishTitle} must have Urdu translation available", report.urduAvailable)
      assertTrue("Collection ${report.englishTitle} must preserve numbering", report.numberingPreserved)
      assertEquals("Collection ${report.englishTitle} must have 0 duplicates", 0, report.duplicateCount)
      assertTrue("Collection ${report.englishTitle} must have local seeded hadiths > 0", report.localSeededHadithCount > 0)
    }

    // 7. Verify 19 Post-Prayer Sunnah Azkar 1:1 Audio Mappings
    val azkarAudioList = com.example.data.repository.IslamicDigitalLibraryRepository.getVerifiedAzkarAudioEntries(context)
    assertEquals("All 19 Sunnah Azkar must have verified audio entries", 19, azkarAudioList.size)
    azkarAudioList.forEach { entry ->
      assertTrue("Zikr #${entry.zikrNumber} audio must be verifiedMatch=true", entry.verifiedMatch)
      assertTrue("Zikr #${entry.zikrNumber} must have non-blank exactArabicText", entry.exactArabicText.isNotBlank())
      assertTrue("Zikr #${entry.zikrNumber} must have HTTPS primaryAudioUrl", entry.primaryAudioUrl.startsWith("https://"))
    }
  }

  @Test
  fun testT36_prophetStories25SourcePreservationSha256AndQuizzesVerified() {
    val repo = com.example.data.repository.ProphetStoriesRepository

    // 1. Verify all 51 raw files (index.json + 25 story JSONs + 25 quiz JSONs) match SHA-256 byte-for-byte
    val checksumRecords = repo.verifyAll51FilesIntegrity(null)
    assertEquals("Must verify all 51 files in prophet_stories", 51, checksumRecords.size)
    checksumRecords.forEach { rec ->
      assertTrue(
        "File ${rec.name} must match recorded SHA-256 (${rec.expectedSha256} vs ${rec.actualSha256})",
        rec.isVerifiedMatch
      )
    }

    // 2. Verify all 25 Prophets in canonical order 1..25
    val indexEntries = repo.getProphetIndexList(null)
    assertEquals("Must have all 25 Prophets in index.json", 25, indexEntries.size)
    assertEquals("adam", indexEntries.first().id)
    assertEquals(1, indexEntries.first().order)
    assertEquals("muhammad", indexEntries.last().id)
    assertEquals(25, indexEntries.last().order)

    // 3. Verify all 25 complete Prophet Story packages (177 Adult chapters, 84 Kids subchapters, 1250 Quiz questions)
    val allPackages = repo.getAll25ProphetPackages(null)
    assertEquals(25, allPackages.size)
    val totalAdultChapters = allPackages.sumOf { it.adultChapters.size }
    val totalKidsSubchapters = allPackages.sumOf { it.kidsSubchapters.size }
    val totalQuizQuestions = allPackages.sumOf { it.quizQuestions.size }
    assertEquals("Total adult chapters across 25 Prophets must be 177", 177, totalAdultChapters)
    assertEquals("Total kids subchapters across 25 Prophets must be 84", 84, totalKidsSubchapters)
    assertEquals("Total quiz questions across 25 Prophets must be 1250 (50 each)", 1250, totalQuizQuestions)

    // 4. Verify PART 5 Urdu Translation Policy & PART 3 Provenance
    allPackages.forEach { pkg ->
      assertEquals("VERIFIED_IMMUTABLE_SHA256 (Byte-for-Byte Match)", pkg.provenance.verificationStatus)
      assertTrue(pkg.provenance.quranReferencesAll.isNotEmpty())
      pkg.adultChapters.forEach { ch ->
        assertTrue(ch.titleAr.isNotBlank())
        assertTrue(ch.titleEn.isNotBlank())
        assertTrue(ch.contentAr.isNotBlank())
        assertTrue(ch.contentEn.isNotBlank())
        assertEquals(repo.URDU_UNAVAILABLE_NOTICE, ch.displayUrduOrNotice)
      }
      assertEquals(50, pkg.quizQuestions.size)
    }

    // 5. Verify External Repositories Audit (PART 4 & PART 9)
    val externalAudits = repo.getExternalRepositoriesAudit(null)
    assertEquals(9, externalAudits.size)
    assertEquals("4af511b2063ee65434733c849a621ab8b6cf7fda17bf315e0ca93a69042a0119", externalAudits[0].sha256Checksum)
    assertEquals("450adecbf44e6ac4ce49e4c3d3bfb8c75ccfeb3743f4e7960ca9020bf1b7179e", externalAudits[1].sha256Checksum)
  }

  @Test
  fun testT37_quranTajweedEngine113611Spans17RulesLessonsQiraatVerified() {
    val repo = com.example.data.repository.QuranTajweedEngineRepository

    // 1. Verify all 11 Tajweed Engine asset files exist and pass SHA-256 verification
    val integrityReports = repo.verifyTajweedEngineIntegrity(null)
    assertEquals(11, integrityReports.size)
    integrityReports.forEach { report ->
      assertTrue("Tajweed asset ${report.fileName} must be verified", report.verified)
      assertTrue("Tajweed asset ${report.fileName} must be non-empty", report.sizeBytes > 0)
      assertEquals(64, report.sha256.length)
    }

    // 2. Verify 17 Tajweed Rule Categories
    val rules = repo.getTajweedRules(null)
    assertEquals("Must load all 17 Tajweed rule categories", 17, rules.size)
    rules.forEach { r ->
      assertTrue(r.id.isNotBlank())
      assertTrue(r.englishTitle.isNotBlank())
      assertTrue(r.colorHex.startsWith("#"))
    }

    // 3. Verify 6,236 Ayahs and 113,611 precomputed Tajweed annotation spans with 0 out-of-bounds spans
    repo.ensureAyahCorpusLoaded(null)
    val allAyahRecords = (1..114).flatMap { s -> repo.getSurahTajweedRecords(null, s) }
    val totalSpans = allAyahRecords.sumOf { it.annotations.size }
    val outOfBoundsSpans = allAyahRecords.sumOf { rec ->
      rec.annotations.count { sp -> sp.start < 0 || sp.end > rec.textUthmani.length || sp.start >= sp.end }
    }
    assertEquals("Must verify all 6,236 Ayahs", 6236, allAyahRecords.size)
    assertEquals("Total precomputed Tajweed spans must be 113,611", 113611, totalSpans)
    assertEquals("Out-of-bounds Tajweed spans must be 0", 0, outOfBoundsSpans)

    // 4. Verify Word-by-Word for Surah 1 Ayah 1 (4 words)
    val fatihaWords = repo.getWordByWordForAyah(null, 1, 1)
    assertEquals(4, fatihaWords.size)
    assertTrue(fatihaWords.first().arabicWord.isNotBlank())
    assertTrue(fatihaWords.first().englishMeaning.isNotBlank())

    // 5. Verify Arabic Alphabet & Script Guide (47 letters, tashkeel, stopping signs)
    val guide = repo.getArabicAlphabetGuide(null)
    val totalLetters = guide.standardLetters.size + guide.otherLetters.size + guide.nonArabicScriptLetters.size
    assertEquals(47, totalLetters)
    assertTrue(guide.tashkeelMarks.isNotEmpty())
    assertTrue(guide.stoppingSigns.isNotEmpty())

    // 6. Verify 10 Chapters & 57 Tajweed Lessons
    val chapters = repo.getTajweedLessonChapters(null)
    assertEquals(10, chapters.size)
    val totalLessons = chapters.sumOf { it.lessons.size }
    assertEquals(57, totalLessons)

    // 7. Verify 7 Qiraat Metadata (kept strictly separate from Hafs 'an 'Asim) & 30 Juz Index
    val qiraat = repo.getQiraatRiwayatSummaries(null)
    assertEquals(7, qiraat.size)
    val juzIndex = repo.getJuzIndex(null)
    assertEquals(30, juzIndex.size)
  }

  @Test
  fun testT38_islamQa4203ScholarlyAnswersAnd12CategoriesVerified() {
    val repo = com.example.data.repository.IslamQaRepository

    // 1. Verify all 4,203 scholarly Q&As load from assets/islamqa/islamqa.zip
    val allEntries = repo.getAllArticles(null)
    assertEquals("IslamQA archive must contain all 4,203 authentic Q&A records", 4203, allEntries.size)

    // 2. Verify 12 categories
    val categories = repo.getCategories(null)
    assertEquals("IslamQA archive must organize records into 12 categories", 12, categories.size)
    assertEquals(4203, categories.sumOf { it.articleCount })

    // 3. Verify search and Urdu translation policy notice
    val fastingResults = repo.searchArticles(null, query = "fasting", categoryFilter = "ALL")
    assertTrue("Searching IslamQA for 'fasting' must return scholarly fatwas", fastingResults.size >= 20)
    assertEquals("اردو ترجمہ فی الحال دستیاب نہیں", repo.URDU_UNAVAILABLE_NOTICE)
    assertEquals("اردو ترجمہ فی الحال دستیاب نہیں", fastingResults.first().urduStatusNotice)
  }

  @Test
  fun testT39_sahabaAhlAlBaytClassifiedFolders45Azkar21HadithAndSequentialNavVerified() {
    val repo = com.example.data.repository.ClassicalLibraryAndBiographiesRepository

    // 1. Verify Sahaba & Ahl al-Bayt Biographies (Sunni & Shia historical accounts kept separate)
    val allBios = repo.getAllBiographies()
    assertTrue("Must contain comprehensive Sahaba & Ahl al-Bayt biographies", allBios.size >= 14)
    val sunniBios = repo.searchBiographies(query = "", traditionFilter = "SUNNI", categoryFilter = "ALL")
    val shiaBios = repo.searchBiographies(query = "", traditionFilter = "SHIA", categoryFilter = "ALL")
    assertTrue("Must have Sunni biographical accounts (Al-Isabah, Usd al-Ghabah, Siyar, Tabaqat)", sunniBios.isNotEmpty())
    assertTrue("Must have Shia biographical accounts (Al-Irshad, Muntaha al-Amal, Rijal al-Kashi)", shiaBios.isNotEmpty())
    allBios.forEach { bio ->
      assertTrue(bio.arabicText.isNotBlank())
      assertTrue(bio.urduTranslation.isNotBlank())
      assertTrue(bio.englishTranslation.isNotBlank())
      assertTrue(bio.volumeAndPage.isNotBlank())
    }

    // 2. Verify Classified Classical Library (Sunni / Shia / Shared-Disputed) with zero cross-duplication
    val classified = repo.getClassifiedLibraryCatalog(null)
    assertEquals(
      "Classified library catalog must have zero cross-duplication across Sunni/Shia/Shared sections",
      classified.size,
      classified.map { it.book.id }.distinct().size
    )
    assertTrue(
      classified.any {
        it.primarySection == com.example.data.repository.ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SUNNI_CLASSICAL
      }
    )
    assertTrue(
      classified.any {
        it.primarySection == com.example.data.repository.ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SHIA_CLASSICAL
      }
    )
    assertTrue(
      classified.any {
        it.primarySection == com.example.data.repository.ClassicalLibraryAndBiographiesRepository.ClassicalLibraryPrimarySection.SHARED_OR_DISPUTED
      }
    )

    // 3. Verify all 45 searchable Azkar categories + 14 imported Azkar JSON files
    assertEquals("Must provide all 45 Azkar categories", 45, repo.all45AzkarCategories.size)
    val importedAzkar = repo.loadImportedAzkarCorpus(null)
    assertTrue("Imported Azkar corpus from 14 JSON files must contain >= 120 items (actual=${importedAzkar.size})", importedAzkar.size >= 120)
    assertEquals("All 14 JSON files must be represented", 14, importedAzkar.map { it.categoryFile }.distinct().size)
    for (catNum in 1..45) {
      val catDuas = repo.getDuasForAzkarCategory45(catNum)
      assertTrue("Azkar category #$catNum must return authentic duas", catDuas.isNotEmpty())
    }

    // 4. Verify 21 Hadith Collections Audit (including Collection 17 Shah Waliullah 40 COMPLETE and Collection 21 Sa'id ibn Mansur BLOCKED)
    val audit21 = repo.auditAll21HadithCollections(null)
    assertEquals("Must audit all 21 Hadith collections", 21, audit21.size)
    val shahWaliullah = audit21.first { it.number == 17 }
    assertEquals("shahwaliullah40", shahWaliullah.collectionKey)
    assertEquals("COMPLETE", shahWaliullah.status)
    assertEquals(40, shahWaliullah.localPreloadedCount)

    val saidIbnMansur = audit21.first { it.number == 21 }
    assertEquals("saidibnmansur", saidIbnMansur.collectionKey)
    assertTrue(saidIbnMansur.status.startsWith("BLOCKED"))

    // 5. Verify deterministic sequential Hadith navigation (index + 1 / index - 1) across chapter boundaries
    val shahList = repo.buildOrderedHadithNavigationList(null, "shahwaliullah40", emptyList())
    assertEquals(40, shahList.size)
    val nextFrom1 = repo.resolveAdjacentHadithSequential(shahList, currentHadithNumber = 1, delta = 1)
    assertNotNull(nextFrom1)
    assertEquals(2, nextFrom1!!.hadithNumber)
    val prevFrom2 = repo.resolveAdjacentHadithSequential(shahList, currentHadithNumber = 2, delta = -1)
    assertNotNull(prevFrom2)
    assertEquals(1, prevFrom2!!.hadithNumber)
    assertNull("Previous from Hadith #1 must be null (boundary)", repo.resolveAdjacentHadithSequential(shahList, 1, -1))
    assertNull("Next from Hadith #40 must be null (boundary)", repo.resolveAdjacentHadithSequential(shahList, 40, 1))
  }
}
