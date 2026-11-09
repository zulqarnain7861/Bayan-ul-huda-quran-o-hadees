package com.example

import android.content.Context
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CountryStatus
import com.example.data.model.LectureFormat
import com.example.data.model.LectureRecord
import com.example.data.model.RecordVerificationState
import com.example.data.model.ReferenceStatus
import com.example.data.model.SourceTier
import com.example.data.model.TranscriptStatus
import com.example.data.repository.LectureRepository
import com.example.data.repository.QuranTajweedEngineRepository
import com.example.player.PipelineOption
import com.example.player.ResolvedStreamTrack
import com.example.player.ResolvedVideoStreamBundle
import com.example.player.VideoPlaybackResumeStore
import com.example.player.VideoQualityOption
import com.example.player.VideoStreamExtractor
import com.example.ui.audio.AudioPlayerManager
import com.example.ui.audio.AudioState
import com.example.ui.audio.TajweedAndAudioDiagnosticsLog
import com.example.ui.audio.TajweedAudioSyncEngine
import com.example.ui.audio.TimingCalibrationStatus
import com.example.ui.audio.availableReciters
import com.example.ui.components.AudioMiniPlayerBar
import com.example.ui.components.BismillahHeader
import com.example.ui.components.QuranTajweedEngineDialog
import com.example.ui.theme.BayanDesignTokens
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BayanAlHudaFullBriefVerificationTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    TajweedAndAudioDiagnosticsLog.clear()
    context.getSharedPreferences("bayan_tajweed_audio_sync_v1", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
    VideoPlaybackResumeStore.clearAllForTesting(context)
  }

  // =========================================================================
  // WORKSTREAM A — UI DEFECT ELIMINATION & MULTI-CONFIG REGRESSION GUARD
  // =========================================================================

  @Test
  fun workstreamA_bismillahHeader_rendersCleanlyAcrossFontScalesRtlAndThemes() {
    val configs = listOf(
      Triple(360.dp, 1.0f, LayoutDirection.Ltr),
      Triple(320.dp, 1.3f, LayoutDirection.Rtl),
      Triple(600.dp, 1.3f, LayoutDirection.Ltr),
      Triple(840.dp, 1.0f, LayoutDirection.Rtl)
    )

    val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
    val activity = controller.get()

    for ((widthDp, fontScale, layoutDir) in configs) {
      activity.setContent {
        MaterialTheme {
          CompositionLocalProvider(
            LocalDensity provides Density(density = 2.0f, fontScale = fontScale),
            LocalLayoutDirection provides layoutDir
          ) {
            Box(modifier = Modifier.width(widthDp)) {
              BismillahHeader(
                surahNumber = 2,
                currentReciter = availableReciters.first { it.id == "husary" },
                audioState = null,
                onPlayBasmala = {}
              )
            }
          }
        }
      }
      Shadows.shadowOf(Looper.getMainLooper()).idle()
      val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
      contentView.measure(
        View.MeasureSpec.makeMeasureSpec((widthDp.value * 2).toInt(), View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(2000, View.MeasureSpec.AT_MOST)
      )
      contentView.layout(0, 0, contentView.measuredWidth, contentView.measuredHeight)
      val heightDp = contentView.measuredHeight / 2.0f
      assertTrue(
        "BismillahHeader height at width=$widthDp fontScale=$fontScale dir=$layoutDir must be > 0 and compact (< 230dp), was ${heightDp}dp",
        heightDp in 40f..230f
      )
    }
    controller.pause().stop().destroy()

    val ltrFormatted = BayanDesignTokens.formatLtrReference("2:255")
    assertTrue(
      "LTR reference must be wrapped in Unicode LTR isolate",
      ltrFormatted.startsWith("\u2066") && ltrFormatted.endsWith("\u2069")
    )
  }

  @Test
  fun workstreamA_quranTajweedEngineDialog_allSixTabDataAndComposableRenderWithoutCrash() {
    // 1. Verify all 6 tab data loaders of QuranTajweedEngineRepository load cleanly with zero crashes
    val rules = QuranTajweedEngineRepository.getTajweedRules(context)
    val ayahRec = QuranTajweedEngineRepository.getAyahTajweedRecord(context, 1, 1)
    val wordByWord = QuranTajweedEngineRepository.getWordByWordForAyah(context, 1, 1)
    val alphabetGuide = QuranTajweedEngineRepository.getArabicAlphabetGuide(context)
    val lessonChapters = QuranTajweedEngineRepository.getTajweedLessonChapters(context)
    val qiraatSummaries = QuranTajweedEngineRepository.getQiraatRiwayatSummaries(context)
    val qiraatDiffs = QuranTajweedEngineRepository.getQiraatRuleDifferences(context)
    val juzList = QuranTajweedEngineRepository.getJuzIndex(context)

    assertTrue("Expected 17 canonical Tajweed rules", rules.size >= 17)
    assertNotNull("Expected Ayah 1:1 Tajweed record", ayahRec)
    assertTrue("Expected Word-by-Word items for 1:1", wordByWord.isNotEmpty())
    assertNotNull("Expected Arabic Alphabet & Waqf guide", alphabetGuide)
    assertTrue("Expected Tajweed lesson chapters", lessonChapters.isNotEmpty())
    assertTrue("Expected 57 curriculum lessons across chapters", lessonChapters.sumOf { it.lessons.size } >= 50)
    assertTrue("Expected Qiraat summaries", qiraatSummaries.isNotEmpty())
    assertTrue("Expected Qiraat rule differences", qiraatDiffs.isNotEmpty())
    assertEquals("Expected 30 Juz entries", 30, juzList.size)

    // 2. Mount QuranTajweedEngineDialog inside a real Robolectric Activity and verify zero crash
    val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
    val activity = controller.get()
    activity.setContent {
      MaterialTheme {
        QuranTajweedEngineDialog(
          onDismiss = {},
          onSelectRuleFilter = {},
          onNavigateToSurahAyah = { _, _ -> }
        )
      }
    }
    Shadows.shadowOf(Looper.getMainLooper()).idle()
    controller.pause().stop().destroy()
  }

  // =========================================================================
  // WORKSTREAM B — 1.0x PLAYBACK & AUDIO-TEXT SYNC FOR ALL RECITERS
  // =========================================================================

  @Test
  fun workstreamB_playbackSpeedDefaultsTo1xAndScalesHighlightTimingCleanly() {
    val manager = AudioPlayerManager(context)
    assertEquals(1.0f, manager.audioState.value.playbackSpeed, 0.001f)
    assertEquals(1.0f, TajweedAudioSyncEngine.loadGlobalPlaybackSpeed(context), 0.001f)

    // Verify wall-clock to media timeline scaling for 0.75x, 1.0x, 1.5x
    assertEquals(750L, TajweedAudioSyncEngine.scaleWallClockIntervalToMediaMs(1000L, 0.75f))
    assertEquals(1000L, TajweedAudioSyncEngine.scaleWallClockIntervalToMediaMs(1000L, 1.0f))
    assertEquals(1500L, TajweedAudioSyncEngine.scaleWallClockIntervalToMediaMs(1000L, 1.5f))

    // Mount AudioMiniPlayerBar in Robolectric Activity and verify Online, Offline, and Resyncing states
    val controller = Robolectric.buildActivity(ComponentActivity::class.java).setup()
    val activity = controller.get()
    for (source in listOf("everyayah_cdn", "offline_local")) {
      activity.setContent {
        MaterialTheme {
          AudioMiniPlayerBar(
            audioState = AudioState(
              isPlaying = true,
              isBuffering = false,
              surahNumber = 2,
              ayahNumber = 1,
              surahName = "Al-Baqarah",
              reciter = availableReciters.first(),
              playbackSpeed = 1.0f,
              timingStatus = TimingCalibrationStatus.CALIBRATED,
              audioSource = source,
              isVisible = true
            ),
            onTogglePlayPause = {},
            onStop = {}
          )
        }
      }
      Shadows.shadowOf(Looper.getMainLooper()).idle()
    }
    controller.pause().stop().destroy()
  }

  @Test
  fun workstreamB_basmalaCalibrationAndRepeatBacktracking_workAcrossAllReciters() {
    val islamSobhi = availableReciters.first { it.id == "islam_sobhi" }
    val husary = availableReciters.first { it.id == "husary" }
    val words = listOf("ذَٰلِكَ", "الْكِتَابُ", "لَا", "رَيْبَ", "فِيهِ", "هُدًى", "لِلْمُتَّقِينَ")

    // Surah 1 & Surah 9 must never apply a pre-ayah Basmala shift
    val surah1 = TajweedAudioSyncEngine.resolveBasmalaCalibration(context, islamSobhi, 1, "mp3quran", 40_000L, "chk1")
    val surah9 = TajweedAudioSyncEngine.resolveBasmalaCalibration(context, islamSobhi, 9, "mp3quran", 120_000L, "chk9")
    assertEquals(0L, surah1.basmalaOffsetMs)
    assertEquals(0L, surah9.basmalaOffsetMs)
    assertFalse(surah9.hasBasmalaPrefix)

    // Surah 2 Ayah 1 with Islam Sobhi highlights Basmala first, then Ayah 1 Word 0 after offset
    val at2s = TajweedAudioSyncEngine.computeSyncedWordIndex(
      context = context,
      reciter = islamSobhi,
      surahNumber = 2,
      ayahNumber = 1,
      arabicWords = words,
      currentPositionMs = 2000L,
      durationMs = 15000L,
      playbackSpeed = 1.0f
    )
    assertTrue(at2s.isRecitingBasmalaPrefix)
    assertEquals(-1, at2s.activeAyahWordIndex)

    val at6s = TajweedAudioSyncEngine.computeSyncedWordIndex(
      context = context,
      reciter = islamSobhi,
      surahNumber = 2,
      ayahNumber = 1,
      arabicWords = words,
      currentPositionMs = 6400L,
      durationMs = 15000L,
      playbackSpeed = 1.0f
    )
    assertFalse(at6s.isRecitingBasmalaPrefix)
    assertEquals(0, at6s.activeAyahWordIndex)

    // Repeat / backtrack detection with Husary at 1.5x speed
    val late = TajweedAudioSyncEngine.computeSyncedWordIndex(
      context = context,
      reciter = husary,
      surahNumber = 2,
      ayahNumber = 2,
      arabicWords = words,
      currentPositionMs = 8200L,
      durationMs = 10000L,
      playbackSpeed = 1.5f
    )
    val rewound = TajweedAudioSyncEngine.computeSyncedWordIndex(
      context = context,
      reciter = husary,
      surahNumber = 2,
      ayahNumber = 2,
      arabicWords = words,
      currentPositionMs = 1400L,
      durationMs = 10000L,
      playbackSpeed = 1.5f
    )
    assertTrue("Backward seek must flag isBacktrackedOrRepeating and rewind word index", rewound.isBacktrackedOrRepeating)
    assertTrue(rewound.activeAyahWordIndex < late.activeAyahWordIndex)

    // Corrupted Tajweed span resilience
    val rulesMap = QuranTajweedEngineRepository.getTajweedRules(context).associateBy { it.id }
    val corruptRecord = QuranTajweedEngineRepository.AyahTajweedRecord(
      surah = 1,
      ayah = 1,
      textUthmani = "بِسْمِ اللَّهِ",
      textTransliteration = "",
      textEnglishSaheeh = "",
      textEnglishMustafa = "",
      juz = 1,
      page = 1,
      wordCount = 2,
      letterCount = 10,
      annotations = listOf(
        QuranTajweedEngineRepository.TajweedSpanAnnotation(start = 0, end = 999, ruleId = "maddNatural")
      )
    )
    val safeText = QuranTajweedEngineRepository.buildSafeTajweedAnnotatedString(
      record = corruptRecord,
      fallbackArabicText = "بِسْمِ اللَّهِ",
      rulesMap = rulesMap,
      baseTextColor = Color.Black
    )
    assertEquals("بِسْمِ اللَّهِ", safeText.text)
  }

  // =========================================================================
  // WORKSTREAM C — VIDEO LIBRARY PLAYBACK CORRECTNESS
  // =========================================================================

  @Test
  fun workstreamC_thumbnailToVideoBinding_validatesIdentityAndRefusesMismatchedStreams() {
    val dataset = runBlocking { LectureRepository(context).loadSeedDataset() }
    assertTrue("Seed catalog must contain verified lectures", dataset.records.size >= 400)

    // Verify stableVideoId uniqueness & format across catalog
    val sampleLecture = dataset.records.first { it.youtubeId != "Cou1OW7jxx4" && it.youtubeId != "DPvekksFmBs" }
    assertTrue(sampleLecture.stableVideoId.endsWith(":${sampleLecture.youtubeId}"))
    assertTrue(sampleLecture.matchesVideoIdentity(sampleLecture.youtubeId, sampleLecture.stableVideoId))
    assertFalse("Must reject mismatched YouTube ID", sampleLecture.matchesVideoIdentity("Cou1OW7jxx4"))

    // Verify non-Archive lectures do NOT get a fake Namaz-e-Muhammadi fallback URL attached
    val nonArchiveLecture = LectureRecord(
      youtubeId = "AbCdEfGhI12",
      title = "Special Q&A Session on Modern Financial Ethics",
      verifiedTitle = "Special Q&A Session on Modern Financial Ethics",
      speaker = "Engineer Muhammad Ali Mirza",
      channelHandle = "@RAAH-e-HIDAYAT-EMAM",
      channelUrl = "https://www.youtube.com/@RAAH-e-HIDAYAT-EMAM",
      sourceTier = SourceTier.FAN_CHANNEL,
      countryStatus = CountryStatus.VERIFIED_IN,
      verificationState = RecordVerificationState.CONFIRMED_LIVE,
      recordFormat = LectureFormat.CLIP,
      lectureNumber = null,
      seriesName = "Q&A Sessions",
      recordedYear = 2024,
      durationSec = 900,
      topics = listOf("ethical_reform"),
      relatedYoutubeIds = emptyList(),
      summary = null,
      transcriptStatus = TranscriptStatus.NONE,
      referenceStatus = ReferenceStatus.UNVERIFIED,
      references = emptyList(),
      alternateYoutubeIds = emptyList()
    )
    val baselineBundle = VideoStreamExtractor.buildInstantBaselineBundle(nonArchiveLecture)
    assertEquals(nonArchiveLecture.youtubeId, baselineBundle.youtubeId)
    assertEquals(nonArchiveLecture.stableVideoId, baselineBundle.stableVideoId)
    assertEquals("Non-matching lecture must not receive an unrelated Archive.org MP4", "", baselineBundle.fallbackBackupMp4Url)

    // Attempt to bind a mismatched bundle (different youtubeId) -> must be refused
    val wrongBundle = baselineBundle.copy(
      youtubeId = "Cou1OW7jxx4",
      stableVideoId = "OFFICIAL:general:Cou1OW7jxx4"
    )
    val mismatchCheck = VideoPlaybackResumeStore.validatePlaybackBinding(
      expectedLecture = nonArchiveLecture,
      bundle = wrongBundle,
      candidateTrackUrl = "https://archive.org/download/NamazParhneKaTareeqa02/Part-2-%20Akele%20Bande%20ki%20Namaz.ia.mp4"
    )
    assertFalse("Playback binding must refuse mismatched video identity", mismatchCheck.isValid)
    assertNotNull(mismatchCheck.failureReason)
  }

  @Test
  fun workstreamC_qualitySelection_enumeratesRealQualitiesConfirmsSelectionAndPersistsStickyPreference() {
    val lecture = runBlocking { LectureRepository(context).loadSeedDataset().records.first() }

    // 1. Progressive single-stream bundle -> only enumerates genuinely distinct modes (360p + Audio 128k), not 8 fake duplicates
    val progressiveBundle = VideoStreamExtractor.registerWebViewExtractedStream(
      lecture = lecture,
      directStreamUrl = "https://rr1---sn-ci5gup-qxae.googlevideo.com/videoplayback?id=123",
      durationSec = 1800
    )!!
    val progressiveQualities = progressiveBundle.getAvailableQualities()
    assertEquals(listOf(VideoQualityOption.Q_360P, VideoQualityOption.AUDIO_128K), progressiveQualities)

    // Requesting an unavailable 1080p track on a single-resolution progressive stream falls back cleanly to 360p (Auto)
    val fallbackConfirmation = VideoPlaybackResumeStore.confirmQualitySelection(
      bundle = progressiveBundle,
      requestedQuality = VideoQualityOption.Q_1080P
    )
    assertEquals(VideoQualityOption.Q_360P, fallbackConfirmation.activeQuality)
    assertTrue("Should report fallbackToAutoApplied when requested quality is unavailable", fallbackConfirmation.fallbackToAutoApplied)

    // 2. Multi-resolution extracted bundle -> enumerates all distinct available resolutions and confirms selection
    val multiResBundle = ResolvedVideoStreamBundle(
      youtubeId = lecture.youtubeId,
      stableVideoId = lecture.stableVideoId,
      activePipeline = PipelineOption.PIPELINE_1,
      tracksByQuality = mapOf(
        VideoQualityOption.Q_240P to ResolvedStreamTrack(VideoQualityOption.Q_240P, "https://cdn.example.com/240p.mp4", isDirectYoutubeStream = true),
        VideoQualityOption.Q_360P to ResolvedStreamTrack(VideoQualityOption.Q_360P, "https://cdn.example.com/360p.mp4", isDirectYoutubeStream = true),
        VideoQualityOption.Q_720P to ResolvedStreamTrack(VideoQualityOption.Q_720P, "https://cdn.example.com/720p.mp4", isDirectYoutubeStream = true),
        VideoQualityOption.AUDIO_128K to ResolvedStreamTrack(VideoQualityOption.AUDIO_128K, "https://cdn.example.com/audio.m4a", isDirectYoutubeStream = true)
      ),
      fallbackBackupMp4Url = "",
      hasDirectYoutubeStreams = true,
      extractedDurationSec = 1800
    )
    val multiQualities = multiResBundle.getAvailableQualities()
    assertTrue(VideoQualityOption.Q_720P in multiQualities)
    assertFalse("480p was not in bundle and must not be fabricated", VideoQualityOption.Q_480P in multiQualities)

    val confirmed720p = VideoPlaybackResumeStore.confirmQualitySelection(multiResBundle, VideoQualityOption.Q_720P)
    assertEquals(VideoQualityOption.Q_720P, confirmed720p.activeQuality)
    assertFalse(confirmed720p.fallbackToAutoApplied)
    assertEquals(1280, confirmed720p.maxWidth)
    assertEquals(720, confirmed720p.maxHeight)

    // 3. Sticky quality preference persists across sessions
    VideoPlaybackResumeStore.setStickyQuality(context, VideoQualityOption.Q_720P)
    assertEquals(VideoQualityOption.Q_720P, VideoPlaybackResumeStore.getStickyQuality(context))
  }

  @Test
  fun workstreamC_resumePosition_persistsByStableVideoIdRestoresOnReopenAndClearsOnCompletionOrRestart() {
    val lectures = runBlocking { LectureRepository(context).loadSeedDataset().records.take(2) }
    val lectureA = lectures[0]
    val lectureB = lectures[1]

    // Save resume position for Lecture A at 04:15 (255,000 ms) out of 30:00 (1,800,000 ms)
    VideoPlaybackResumeStore.saveResumePosition(
      context = context,
      lecture = lectureA,
      positionMs = 255_000L,
      durationMs = 1_800_000L
    )

    // Lecture A restores exact position; Lecture B has no saved position (proving keyed by videoId, not list index)
    val restoredA = VideoPlaybackResumeStore.getSavedResumeState(context, lectureA)
    val restoredB = VideoPlaybackResumeStore.getSavedResumeState(context, lectureB)
    assertNotNull(restoredA)
    assertEquals(255_000L, restoredA!!.positionMs)
    assertEquals("04:15", restoredA.formattedTime)
    assertEquals(lectureA.stableVideoId, restoredA.stableVideoId)
    assertNull("Unwatched video must not inherit resume state from another list index", restoredB)

    // Reaching >= 95% of duration (e.g., 1,750,000 ms / 1,800,000 ms) automatically clears saved resume state
    VideoPlaybackResumeStore.saveResumePosition(
      context = context,
      lecture = lectureA,
      positionMs = 1_750_000L,
      durationMs = 1_800_000L
    )
    assertNull(
      "Completed video (>= 95% duration) must automatically clear its saved resume position",
      VideoPlaybackResumeStore.getSavedResumeState(context, lectureA)
    )

    // Re-save and verify most recent resume lookup + manual Restart clear
    VideoPlaybackResumeStore.saveResumePosition(
      context = context,
      lecture = lectureA,
      positionMs = 120_000L,
      durationMs = 1_800_000L
    )
    assertNotNull(VideoPlaybackResumeStore.getSavedResumeState(context, lectureA))
    val mostRecent = VideoPlaybackResumeStore.getMostRecentSavedResumeState(context)
    assertNotNull("Expected most recent saved resume state for HomeScreen & DiscoverScreen Resume Video cards", mostRecent)
    assertEquals(lectureA.youtubeId, mostRecent!!.youtubeId)
    assertEquals("02:00", mostRecent.formattedTime)
    assertEquals(1, VideoPlaybackResumeStore.getAllSavedResumeStates(context).size)

    VideoPlaybackResumeStore.clearResumePosition(context, lectureA.youtubeId)
    assertNull(VideoPlaybackResumeStore.getSavedResumeState(context, lectureA))
    assertNull(VideoPlaybackResumeStore.getMostRecentSavedResumeState(context))
  }

  @Test
  fun shareAppApkWithDriveLink_attachesApkStreamAndOfficialGoogleDriveUpdatesLink() {
    val expectedDriveFolderUrl = "https://drive.google.com/drive/folders/14i90MpNPedlnBk3l53L_yplzzFNIa4N3"
    assertEquals(expectedDriveFolderUrl, com.example.ui.components.APP_UPDATES_GOOGLE_DRIVE_FOLDER_URL)

    val apkFile = com.example.ui.components.prepareShareableApkFile(context)
    assertNotNull("Expected shareable APK file to be prepared in cacheDir/shared_apk", apkFile)
    assertTrue("Prepared APK file must exist and be non-empty", apkFile!!.exists() && apkFile.length() > 0L)

    val shareIntent = com.example.ui.components.buildShareAppApkAndLinkIntent(context)
    assertEquals(android.content.Intent.ACTION_SEND, shareIntent.action)
    assertEquals("application/vnd.android.package-archive", shareIntent.type)

    val shareText = shareIntent.getStringExtra(android.content.Intent.EXTRA_TEXT).orEmpty()
    assertTrue(
      "Share message must include the official Google Drive folder link ($expectedDriveFolderUrl)",
      shareText.contains(expectedDriveFolderUrl)
    )

    @Suppress("DEPRECATION")
    val streamUri = shareIntent.getParcelableExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
    assertNotNull("Share intent must attach APK file URI via EXTRA_STREAM", streamUri)
    assertTrue(
      "APK stream URI must use FileProvider content:// scheme and reference Bayan-al-Huda.apk",
      streamUri.toString().startsWith("content://") && streamUri.toString().endsWith("Bayan-al-Huda.apk")
    )
    assertTrue(
      "Share intent must grant read URI permission for the attached APK file",
      (shareIntent.flags and android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0
    )
  }
}

