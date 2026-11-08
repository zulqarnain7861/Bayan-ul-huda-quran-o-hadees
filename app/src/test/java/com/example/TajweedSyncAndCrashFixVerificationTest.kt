package com.example

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.QuranTajweedEngineRepository
import com.example.ui.audio.AudioPlayerManager
import com.example.ui.audio.BasmalaCalibrationSource
import com.example.ui.audio.TajweedAndAudioDiagnosticsLog
import com.example.ui.audio.TajweedAudioSyncEngine
import com.example.ui.audio.availableReciters
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TajweedSyncAndCrashFixVerificationTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    TajweedAndAudioDiagnosticsLog.clear()
    context.getSharedPreferences("bayan_tajweed_audio_sync_v1", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  @Test
  fun test1_tajweedLoadsShortMediumAndLongSurahsWithoutCrash() {
    val rules = QuranTajweedEngineRepository.getTajweedRules(context)
    assertTrue("Expected 17 canonical Tajweed rules", rules.size >= 17)
    val rulesMap = rules.associateBy { it.id }

    // Short (1 Al-Fatihah, 112 Al-Ikhlas), Medium (36 Yasin, 18 Al-Kahf, 9 At-Tawbah), Long (2 Al-Baqarah)
    val testSurahs = listOf(1, 2, 9, 18, 36, 112)
    for (surahNum in testSurahs) {
      val records = QuranTajweedEngineRepository.getSurahTajweedRecords(context, surahNum)
      assertTrue("Surah $surahNum must return non-empty Tajweed records", records.isNotEmpty())
      val firstRecord = records.first()
      val annotated = QuranTajweedEngineRepository.buildSafeTajweedAnnotatedString(
        record = firstRecord,
        fallbackArabicText = firstRecord.textUthmani,
        rulesMap = rulesMap,
        baseTextColor = Color.Black
      )
      assertEquals(
        "Uthmani Arabic text must never be altered or re-diacritized (Surah $surahNum)",
        firstRecord.textUthmani,
        annotated.text
      )
    }
  }

  @Test
  fun test2_malformedAndOutOfBoundsTajweedSpansFallBackCleanly() {
    val rules = QuranTajweedEngineRepository.getTajweedRules(context)
    val rulesMap = rules.associateBy { it.id }
    val validRuleIds = rulesMap.keys

    val uthmaniSample = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
    val rawSpans = listOf(
      // Valid span
      QuranTajweedEngineRepository.TajweedSpanAnnotation(start = 7, end = 8, ruleId = "hamzatWaslSilent"),
      // Invalid negative start
      QuranTajweedEngineRepository.TajweedSpanAnnotation(start = -5, end = 4, ruleId = "hamzatWaslSilent"),
      // Invalid end beyond text length
      QuranTajweedEngineRepository.TajweedSpanAnnotation(start = 10, end = 999, ruleId = "maddNatural"),
      // Invalid start >= end
      QuranTajweedEngineRepository.TajweedSpanAnnotation(start = 15, end = 12, ruleId = "lamShamsiyah"),
      // Invalid unknown ruleId
      QuranTajweedEngineRepository.TajweedSpanAnnotation(start = 0, end = 3, ruleId = "unknown_corrupt_rule_xyz")
    )

    val filtered = QuranTajweedEngineRepository.validateAndFilterAnnotations(
      surah = 1,
      ayah = 1,
      textUthmani = uthmaniSample,
      rawAnnotations = rawSpans,
      validRuleIds = validRuleIds
    )
    assertEquals("Only the single valid span should be retained", 1, filtered.size)
    assertEquals("hamzatWaslSilent", filtered.first().ruleId)

    val corruptRecord = QuranTajweedEngineRepository.AyahTajweedRecord(
      surah = 1,
      ayah = 1,
      textUthmani = uthmaniSample,
      textTransliteration = "",
      textEnglishSaheeh = "",
      textEnglishMustafa = "",
      juz = 1,
      page = 1,
      wordCount = 4,
      letterCount = 19,
      annotations = rawSpans
    )
    val built = QuranTajweedEngineRepository.buildSafeTajweedAnnotatedString(
      record = corruptRecord,
      fallbackArabicText = uthmaniSample,
      rulesMap = rulesMap
    )
    assertEquals(uthmaniSample, built.text)
    assertTrue(
      "Diagnostics log must record dropped invalid spans",
      TajweedAndAudioDiagnosticsLog.getRecentEntries().any { it.contains("TAJWEED_VALIDATION") }
    )
  }

  @Test
  fun test3_defaultPlaybackSpeedIsStrictlyOnePointZeroAndPersistsValidSpeeds() {
    // Fresh state must be strictly 1.0f
    val defaultSpeed = TajweedAudioSyncEngine.loadGlobalPlaybackSpeed(context)
    assertEquals(1.0f, defaultSpeed, 0.001f)

    val manager = AudioPlayerManager(context)
    assertEquals(1.0f, manager.audioState.value.playbackSpeed, 0.001f)

    // Save valid user speed 1.25x
    manager.setPlaybackSpeed(1.25f)
    assertEquals(1.25f, manager.audioState.value.playbackSpeed, 0.001f)
    assertEquals(1.25f, TajweedAudioSyncEngine.loadGlobalPlaybackSpeed(context), 0.001f)
  }

  @Test
  fun test4_corruptOrOutOfRangePlaybackSpeedResetsToOnePointZero() {
    assertEquals(1.0f, TajweedAudioSyncEngine.normalizePlaybackSpeed(Float.NaN), 0.001f)
    assertEquals(1.0f, TajweedAudioSyncEngine.normalizePlaybackSpeed(Float.POSITIVE_INFINITY), 0.001f)
    assertEquals(1.0f, TajweedAudioSyncEngine.normalizePlaybackSpeed(0.1f), 0.001f)
    assertEquals(1.0f, TajweedAudioSyncEngine.normalizePlaybackSpeed(3.5f), 0.001f)
    assertEquals(1.0f, TajweedAudioSyncEngine.normalizePlaybackSpeed(-1.0f), 0.001f)

    // Write corrupt speed directly to SharedPreferences and verify loadGlobalPlaybackSpeed resets to 1.0f
    context.getSharedPreferences("bayan_tajweed_audio_sync_v1", Context.MODE_PRIVATE)
      .edit()
      .putFloat("global_playback_speed_v1", 4.2f)
      .commit()

    val recovered = TajweedAudioSyncEngine.loadGlobalPlaybackSpeed(context)
    assertEquals(1.0f, recovered, 0.001f)
  }

  @Test
  fun test5_basmalaOffsetAlignmentAndGreenRedReciterStatusAndCacheInvalidation() {
    val islamSobhi = availableReciters.first { it.id == "islam_sobhi" }
    val husary = availableReciters.first { it.id == "husary" }

    // Surah 9 (At-Tawbah): never has Basmala
    assertFalse(TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(context, islamSobhi, 9))
    assertFalse(TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(context, husary, 9))
    val surah9Cal = TajweedAudioSyncEngine.resolveBasmalaCalibration(context, islamSobhi, 9, "surah_stream", 60000L, "")
    assertEquals(0L, surah9Cal.basmalaOffsetMs)

    // Surah 1 (Al-Fatihah): Basmala is Ayah 1 (Green for all reciters, 0ms pre-ayah1 offset)
    assertTrue(TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(context, islamSobhi, 1))
    assertTrue(TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(context, husary, 1))
    val surah1Cal = TajweedAudioSyncEngine.resolveBasmalaCalibration(context, islamSobhi, 1, "surah_stream", 45000L, "")
    assertEquals(0L, surah1Cal.basmalaOffsetMs)

    // Surah 36 (Yasin): Islam Sobhi recites Basmala in stream (Green, > 0ms offset),
    // while Husary EveryAyah 036001.mp3 starts directly at Ayah 1 (Red, 0ms offset unless Basmala verse 0 is played)
    assertTrue(TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(context, islamSobhi, 36))
    assertFalse(TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(context, husary, 36, activeAyahNumber = 1))
    assertTrue(TajweedAudioSyncEngine.doesReciterReciteBasmalaInSurah(context, husary, 36, activeAyahNumber = 0))

    val sobhi36Cal = TajweedAudioSyncEngine.resolveBasmalaCalibration(context, islamSobhi, 36, "surah_stream", 900000L, "")
    assertTrue(sobhi36Cal.hasBasmalaPrefix)
    assertEquals(BasmalaCalibrationSource.VERIFIED_DATASET, sobhi36Cal.source)
    assertTrue(sobhi36Cal.basmalaOffsetMs >= 5000L)

    // Cache invalidation when fileDurationMs or fileChecksum changes
    val customEntry = sobhi36Cal.copy(
      surahNumber = 40,
      audioSource = "local_test",
      fileDurationMs = 120000L,
      fileChecksum = "abc12345",
      basmalaOffsetMs = 5432L,
      source = BasmalaCalibrationSource.MEASURED_CALIBRATION
    )
    TajweedAudioSyncEngine.storeCalibration(context, customEntry)
    val hit = TajweedAudioSyncEngine.resolveBasmalaCalibration(
      context, islamSobhi, 40, "local_test", 120000L, "abc12345"
    )
    assertEquals(BasmalaCalibrationSource.MEASURED_CALIBRATION, hit.source)
    assertEquals(5432L, hit.basmalaOffsetMs)

    // Change checksum -> must invalidate MEASURED_CALIBRATION and fall back to DECLARED_PROFILE
    val invalidated = TajweedAudioSyncEngine.resolveBasmalaCalibration(
      context, islamSobhi, 40, "local_test", 120000L, "changed_checksum_999"
    )
    assertEquals(BasmalaCalibrationSource.DECLARED_PROFILE, invalidated.source)
  }

  @Test
  fun test6_backwardSeekAndRepeatMoveHighlightBackwardAndClampWithinAyah() {
    val husary = availableReciters.first { it.id == "husary" }
    val words = listOf("ٱلْحَمْدُ", "لِلَّهِ", "رَبِّ", "ٱلْعَٰلَمِينَ")

    val lateSync = TajweedAudioSyncEngine.computeSyncedWordIndex(
      context = context,
      reciter = husary,
      surahNumber = 1,
      ayahNumber = 2,
      arabicWords = words,
      currentPositionMs = 4200L,
      durationMs = 5000L
    )
    assertEquals(3, lateSync.activeAyahWordIndex)

    // Seek backward to 600ms -> word highlight must immediately move back to word 0
    val backSync = TajweedAudioSyncEngine.computeSyncedWordIndex(
      context = context,
      reciter = husary,
      surahNumber = 1,
      ayahNumber = 2,
      arabicWords = words,
      currentPositionMs = 600L,
      durationMs = 5000L
    )
    assertEquals(0, backSync.activeAyahWordIndex)
    assertTrue(backSync.isBacktrackedOrRepeating)

    // Extreme position beyond duration -> must clamp strictly inside 0..words.lastIndex
    val clampedSync = TajweedAudioSyncEngine.computeSyncedWordIndex(
      context = context,
      reciter = husary,
      surahNumber = 1,
      ayahNumber = 2,
      arabicWords = words,
      currentPositionMs = 99999L,
      durationMs = 5000L
    )
    assertTrue(clampedSync.activeAyahWordIndex in 0..words.lastIndex)
  }

  @Test
  fun test7_switchingRecitersPreservesSavedSpeedAndResetsCalibration() {
    val manager = AudioPlayerManager(context)
    manager.setPlaybackSpeed(1.5f)
    val sobhi = availableReciters.first { it.id == "islam_sobhi" }
    val minshawi = availableReciters.first { it.id == "minshawi" }

    manager.setReciter(sobhi, autoResumeCurrentAyah = false)
    assertEquals("islam_sobhi", manager.audioState.value.reciter.id)
    assertEquals(1.5f, manager.audioState.value.playbackSpeed, 0.001f)
    assertTrue(manager.audioState.value.hasBasmalaInCurrentSurahStream)

    manager.setReciter(minshawi, autoResumeCurrentAyah = false)
    assertEquals("minshawi", manager.audioState.value.reciter.id)
    assertEquals(1.5f, manager.audioState.value.playbackSpeed, 0.001f)
  }

  @Test
  fun test8_localAudioFileCalibrationProducesMeasuredEntry() {
    val sobhi = availableReciters.first { it.id == "islam_sobhi" }
    val tempAudio = File(context.cacheDir, "test_surah_36.mp3")
    val syntheticBytes = ByteArray(16384) { idx -> ((idx * 31) and 0xFF).toByte() }
    tempAudio.writeBytes(syntheticBytes)

    val entry = TajweedAudioSyncEngine.calibrateFromLocalAudioFile(
      context = context,
      reciter = sobhi,
      surahNumber = 36,
      ayahNumber = 1,
      audioFile = tempAudio,
      fileDurationMs = 90000L
    )
    assertNotNull(entry)
    assertEquals(BasmalaCalibrationSource.MEASURED_CALIBRATION, entry?.source)
    assertTrue((entry?.basmalaOffsetMs ?: 0L) >= 4600L)
    tempAudio.delete()
  }
}
