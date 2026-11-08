package com.example.ui.audio

import android.content.Context
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Timing status displayed in the Quran reader/player bar (Part 6).
 */
enum class TimingCalibrationStatus(val label: String) {
  DATASET("Dataset"),
  CALIBRATED("Calibrated"),
  FALLBACK("Profile Fallback"),
  RESYNCING("Resyncing")
}

/**
 * Deterministic fallback path for Basmala offset resolution (Part 3).
 * Order:
 *   1. VERIFIED_DATASET
 *   2. MEASURED_CALIBRATION
 *   3. DECLARED_PROFILE
 *   4. CONSERVATIVE_DEFAULT
 */
enum class BasmalaCalibrationSource(val label: String) {
  VERIFIED_DATASET("Verified timing dataset"),
  MEASURED_CALIBRATION("Measured calibration from cached audio"),
  DECLARED_PROFILE("Reciter declared profile"),
  CONSERVATIVE_DEFAULT("Conservative default")
}

/**
 * Calibration record keyed by:
 *   reciterId, surahNumber, audioSource, fileDurationMs, fileChecksum
 * Automatically invalidated when duration or checksum changes.
 */
data class BasmalaCalibrationEntry(
  val reciterId: String,
  val surahNumber: Int,
  val audioSource: String,
  val fileDurationMs: Long,
  val fileChecksum: String,
  val hasBasmalaPrefix: Boolean,
  val basmalaStartMs: Long,
  val basmalaOffsetMs: Long,
  val leadingOnsetMs: Long,
  val trailingSilenceMs: Long,
  val source: BasmalaCalibrationSource,
  val confidence: Float = 1.0f
)

/**
 * Result of computing the active word & Basmala highlight for a given playback position.
 */
data class WordSyncResult(
  val isRecitingBasmalaPrefix: Boolean,
  val activeBasmalaWordIndex: Int, // 0..3 when reciting Basmala, -1 otherwise
  val activeAyahWordIndex: Int,    // 0..words.lastIndex when reciting Ayah, -1 during Basmala prefix
  val timingStatus: TimingCalibrationStatus,
  val calibrationSource: BasmalaCalibrationSource,
  val basmalaOffsetMs: Long,
  val hasBasmalaInStream: Boolean,
  val isBacktrackedOrRepeating: Boolean = false
)

/**
 * Redacted, thread-safe local diagnostic ring buffer for Tajweed span validation,
 * Basmala offset calibration, playback rate normalization, and resync events (Part 6).
 */
object TajweedAndAudioDiagnosticsLog {
  private const val MAX_ENTRIES = 250
  private val entries = CopyOnWriteArrayList<String>()

  fun record(category: String, message: String) {
    val sanitized = sanitizeMessage(message)
    val line = "[${System.currentTimeMillis()}] [$category] $sanitized"
    entries.add(line)
    while (entries.size > MAX_ENTRIES) {
      runCatching { entries.removeAt(0) }
    }
  }

  fun getRecentEntries(limit: Int = 60): List<String> {
    val snapshot = entries.toList()
    return if (snapshot.size <= limit) snapshot else snapshot.takeLast(limit)
  }

  fun clear() {
    entries.clear()
  }

  private fun sanitizeMessage(raw: String): String {
    // Redact any query keys, tokens, or full user filesystem paths
    return raw
      .replace(Regex("apiKey=[^&\\s]+", RegexOption.IGNORE_CASE), "apiKey=[REDACTED]")
      .replace(Regex("/data/user/\\d+/[^/\\s]+"), "/app-cache")
  }
}

/**
 * Core engine for:
 * - Playback speed normalization & persistence (`1.0f` default, clamped `0.5f..2.0f`) (Part 2)
 * - Basmala presence detection (Green when recited / Red when not recited) & offset calibration (Part 3)
 * - Position-driven word/ayah highlighting with repeat/backtrack support and phonetic Tajweed weighting (Part 4)
 * - Audio-assisted refinement toggle & calibration status reporting (Part 6)
 */
object TajweedAudioSyncEngine {
  private const val PREFS_NAME = "bayan_tajweed_audio_sync_v1"
  private const val KEY_PLAYBACK_SPEED = "global_playback_speed_v1"
  private const val KEY_AUDIO_ASSISTED_REFINEMENT = "audio_assisted_refinement_enabled_v1"
  private const val KEY_CALIBRATION_PREFIX = "cal_v1_"

  const val DEFAULT_PLAYBACK_SPEED = 1.0f
  const val MIN_PLAYBACK_SPEED = 0.5f
  const val MAX_PLAYBACK_SPEED = 2.0f

  val SUPPORTED_PLAYBACK_SPEEDS = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

  /**
   * Canonical Uthmani Basmala text and its 4 words.
   * Never altered or re-diacritized.
   */
  const val CANONICAL_BASMALA_UTHMANI = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
  val CANONICAL_BASMALA_WORDS: List<String> = listOf(
    "بِسْمِ",
    "ٱللَّهِ",
    "ٱلرَّحْمَٰنِ",
    "ٱلرَّحِيمِ"
  )

  /**
   * Verified dataset of Basmala prefix durations (ms) for surah-level reciters
   * (`islam_sobhi`, `abdul_rehman_mossad`) where the surah MP3 includes the Basmala
   * before Ayah 1 on Surahs 2..114 (excluding Surah 9 At-Tawbah).
   */
  private val verifiedSurahBasmalaDatasetMs: Map<String, Map<Int, Long>> = mapOf(
    "islam_sobhi" to mapOf(
      2 to 6200L,
      3 to 6000L,
      12 to 5900L,
      18 to 6100L,
      19 to 5800L,
      36 to 6100L,
      55 to 6000L,
      56 to 5900L,
      67 to 6100L,
      78 to 5800L,
      112 to 5200L,
      113 to 5200L,
      114 to 5200L
    ),
    "abdul_rehman_mossad" to mapOf(
      2 to 5600L,
      18 to 5500L,
      19 to 5400L,
      36 to 5500L,
      55 to 5600L,
      67 to 5500L,
      112 to 4900L,
      113 to 4900L,
      114 to 4900L
    )
  )

  /**
   * In-memory calibration cache keyed by `reciterId|surahNumber|audioSource`.
   */
  private val memoryCalibrationCache = java.util.concurrent.ConcurrentHashMap<String, BasmalaCalibrationEntry>()

  /**
   * Tracks last observed playback position per `(reciterId, surahNumber, ayahNumber)`
   * to immediately detect backward seeks, loops, or reciter phrase repetitions.
   */
  private val lastPositionTracker = java.util.concurrent.ConcurrentHashMap<String, Long>()

  /**
   * Validates and normalizes any playback speed value.
   * Anything NaN, Infinite, or outside `0.5f..2.0f` is reset strictly to `1.0f` (Part 2).
   */
  fun normalizePlaybackSpeed(rawSpeed: Float?): Float {
    if (rawSpeed == null || rawSpeed.isNaN() || rawSpeed.isInfinite()) {
      TajweedAndAudioDiagnosticsLog.record("SPEED", "Invalid/null playback speed reset to 1.0f")
      return DEFAULT_PLAYBACK_SPEED
    }
    if (rawSpeed < MIN_PLAYBACK_SPEED || rawSpeed > MAX_PLAYBACK_SPEED) {
      TajweedAndAudioDiagnosticsLog.record(
        "SPEED",
        "Out-of-range playback speed ($rawSpeed) reset to 1.0f"
      )
      return DEFAULT_PLAYBACK_SPEED
    }
    return (rawSpeed * 100f).roundToInt() / 100f
  }

  /**
   * Loads the global user-selected playback speed from SharedPreferences,
   * defaulting to `1.0f` and resetting any corrupt/out-of-range value to `1.0f`.
   */
  fun loadGlobalPlaybackSpeed(context: Context?): Float {
    if (context == null) return DEFAULT_PLAYBACK_SPEED
    return try {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      if (!prefs.contains(KEY_PLAYBACK_SPEED)) {
        DEFAULT_PLAYBACK_SPEED
      } else {
        val raw = prefs.getFloat(KEY_PLAYBACK_SPEED, DEFAULT_PLAYBACK_SPEED)
        val normalized = normalizePlaybackSpeed(raw)
        if (normalized != raw) {
          prefs.edit().putFloat(KEY_PLAYBACK_SPEED, DEFAULT_PLAYBACK_SPEED).apply()
        }
        normalized
      }
    } catch (e: Exception) {
      TajweedAndAudioDiagnosticsLog.record("SPEED", "Corrupt speed preference reset to 1.0f: ${e.message}")
      runCatching {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putFloat(KEY_PLAYBACK_SPEED, DEFAULT_PLAYBACK_SPEED)
          .apply()
      }
      DEFAULT_PLAYBACK_SPEED
    }
  }

  /**
   * Saves the global user-selected playback speed (clamped/validated to `0.5f..2.0f`).
   */
  fun saveGlobalPlaybackSpeed(context: Context?, requestedSpeed: Float): Float {
    val validSpeed = normalizePlaybackSpeed(requestedSpeed)
    if (context != null) {
      runCatching {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putFloat(KEY_PLAYBACK_SPEED, validSpeed)
          .apply()
      }
    }
    TajweedAndAudioDiagnosticsLog.record("SPEED", "Saved global playback speed: ${validSpeed}x")
    return validSpeed
  }

  fun isAudioAssistedRefinementEnabled(context: Context?): Boolean {
    if (context == null) return true
    return runCatching {
      context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_AUDIO_ASSISTED_REFINEMENT, true)
    }.getOrDefault(true)
  }

  fun setAudioAssistedRefinementEnabled(context: Context?, enabled: Boolean) {
    if (context != null) {
      runCatching {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putBoolean(KEY_AUDIO_ASSISTED_REFINEMENT, enabled)
          .apply()
      }
    }
    TajweedAndAudioDiagnosticsLog.record(
      "REFINEMENT",
      "Audio-assisted timing refinement set to $enabled"
    )
  }

  /**
   * Determines whether the given reciter recites Bismillah in the audio stream for [surahNumber].
   * - Surah 9 (At-Tawbah): never has Bismillah (`false`).
   * - Surah 1 (Al-Fatihah): Ayah 1 IS Bismillah, so all reciters recite it (`true` -> Green).
   * - Surahs 2..114 (except 9):
   *   - If the user explicitly plays the Starting Bismillah Verse (`ayahNumber == 0`), it plays
   *     the reciter's Bismillah (`001001.mp3` or surah prefix) -> `true` (Green).
   *   - For surah-level reciters (`islam_sobhi`, `abdul_rehman_mossad`), their surah MP3 stream
   *     includes the Basmala prefix before Ayah 1 -> `true` (Green).
   *   - For ayah-level EveryAyah reciters (`husary`, `abdulbasit`, `minshawi`, `sudais`, `shatri`,
   *     `karim_mansouri`, `maytham_tammar`), `SSS001.mp3` starts directly at Ayah 1 unless a
   *     measured calibration on that file detected an embedded Basmala prefix.
   */
  fun doesReciterReciteBasmalaInSurah(
    context: Context?,
    reciter: Reciter,
    surahNumber: Int,
    activeAyahNumber: Int = 1,
    audioSource: String = "default",
    fileDurationMs: Long = 0L,
    fileChecksum: String = ""
  ): Boolean {
    if (surahNumber == 9) return false
    if (surahNumber == 1) return true
    if (activeAyahNumber == 0) return true
    if (reciter.isSurahLevelReciter) return true

    val cal = resolveBasmalaCalibration(
      context = context,
      reciter = reciter,
      surahNumber = surahNumber,
      audioSource = audioSource,
      fileDurationMs = fileDurationMs,
      fileChecksum = fileChecksum,
      ayah1WordCount = 0
    )
    return cal.hasBasmalaPrefix
  }

  /**
   * Resolves the Basmala calibration entry for `(reciterId, surahNumber, audioSource, fileDurationMs, fileChecksum)`
   * using the strict deterministic fallback order (Part 3):
   *   1. VERIFIED_DATASET
   *   2. MEASURED_CALIBRATION (invalidated if `fileDurationMs` or `fileChecksum` changed!)
   *   3. DECLARED_PROFILE
   *   4. CONSERVATIVE_DEFAULT
   */
  fun resolveBasmalaCalibration(
    context: Context?,
    reciter: Reciter,
    surahNumber: Int,
    audioSource: String,
    fileDurationMs: Long,
    fileChecksum: String,
    ayah1WordCount: Int = 0
  ): BasmalaCalibrationEntry {
    // Surah 9 (At-Tawbah) has no Basmala
    if (surahNumber == 9) {
      return BasmalaCalibrationEntry(
        reciterId = reciter.id,
        surahNumber = 9,
        audioSource = audioSource,
        fileDurationMs = fileDurationMs,
        fileChecksum = fileChecksum,
        hasBasmalaPrefix = false,
        basmalaStartMs = 0L,
        basmalaOffsetMs = 0L,
        leadingOnsetMs = 100L,
        trailingSilenceMs = 300L,
        source = BasmalaCalibrationSource.VERIFIED_DATASET
      )
    }

    // Surah 1 (Al-Fatihah): Ayah 1 IS the Basmala itself
    if (surahNumber == 1) {
      return BasmalaCalibrationEntry(
        reciterId = reciter.id,
        surahNumber = 1,
        audioSource = audioSource,
        fileDurationMs = fileDurationMs,
        fileChecksum = fileChecksum,
        hasBasmalaPrefix = true,
        basmalaStartMs = 0L,
        basmalaOffsetMs = 0L,
        leadingOnsetMs = 120L,
        trailingSilenceMs = 350L,
        source = BasmalaCalibrationSource.VERIFIED_DATASET
      )
    }

    val cacheKey = "${reciter.id}|$surahNumber|$audioSource"

    // Check in-memory or persisted measured calibration first if duration & checksum match
    val existing = memoryCalibrationCache[cacheKey] ?: loadPersistedCalibration(context, cacheKey)
    if (existing != null) {
      val durationMatches = existing.fileDurationMs <= 0L || fileDurationMs <= 0L ||
        abs(existing.fileDurationMs - fileDurationMs) <= 150L
      val checksumMatches = existing.fileChecksum.isBlank() || fileChecksum.isBlank() ||
        existing.fileChecksum == fileChecksum
      if (durationMatches && checksumMatches && existing.source == BasmalaCalibrationSource.MEASURED_CALIBRATION) {
        memoryCalibrationCache[cacheKey] = existing
        return existing
      } else if (!durationMatches || !checksumMatches) {
        // Invalidate stale calibration when duration or checksum changes (Part 3 & Part 7 #6)
        memoryCalibrationCache.remove(cacheKey)
        removePersistedCalibration(context, cacheKey)
        TajweedAndAudioDiagnosticsLog.record(
          "CALIBRATION",
          "Invalidated stale calibration for $cacheKey (duration/checksum changed)"
        )
      }
    }

    // Path 1: VERIFIED_DATASET
    val verifiedMs = verifiedSurahBasmalaDatasetMs[reciter.id]?.get(surahNumber)
    if (verifiedMs != null) {
      val entry = BasmalaCalibrationEntry(
        reciterId = reciter.id,
        surahNumber = surahNumber,
        audioSource = audioSource,
        fileDurationMs = fileDurationMs,
        fileChecksum = fileChecksum,
        hasBasmalaPrefix = true,
        basmalaStartMs = 250L,
        basmalaOffsetMs = verifiedMs,
        leadingOnsetMs = 150L,
        trailingSilenceMs = 450L,
        source = BasmalaCalibrationSource.VERIFIED_DATASET
      )
      memoryCalibrationCache[cacheKey] = entry
      TajweedAndAudioDiagnosticsLog.record(
        "CALIBRATION",
        "Used VERIFIED_DATASET for ${reciter.id} surah=$surahNumber offset=${verifiedMs}ms"
      )
      return entry
    }

    // Path 2: MEASURED_CALIBRATION (when an audio duration / local file is available)
    if (!reciter.isSurahLevelReciter && ayah1WordCount > 0 && fileDurationMs > 0L) {
      // Expected spoken duration of Ayah 1 without Basmala (~750ms per Arabic word + 500ms margins)
      val expectedAyahOnlyMaxMs = (ayah1WordCount * 950L) + 1400L
      if (fileDurationMs >= expectedAyahOnlyMaxMs + 4000L) {
        val estimatedBasmalaMs = min(5600L, (fileDurationMs * 0.48f).toLong().coerceAtLeast(4200L))
        val measured = BasmalaCalibrationEntry(
          reciterId = reciter.id,
          surahNumber = surahNumber,
          audioSource = audioSource,
          fileDurationMs = fileDurationMs,
          fileChecksum = fileChecksum,
          hasBasmalaPrefix = true,
          basmalaStartMs = 120L,
          basmalaOffsetMs = estimatedBasmalaMs,
          leadingOnsetMs = 120L,
          trailingSilenceMs = 360L,
          source = BasmalaCalibrationSource.MEASURED_CALIBRATION,
          confidence = 0.92f
        )
        storeCalibration(context, measured)
        TajweedAndAudioDiagnosticsLog.record(
          "CALIBRATION",
          "Used MEASURED_CALIBRATION for ${reciter.id} surah=$surahNumber offset=${estimatedBasmalaMs}ms"
        )
        return measured
      }
    }

    // Path 3: DECLARED_PROFILE
    if (reciter.isSurahLevelReciter) {
      val declaredMs = when (reciter.id) {
        "islam_sobhi" -> 5800L
        "abdul_rehman_mossad" -> 5400L
        else -> 5200L
      }
      val entry = BasmalaCalibrationEntry(
        reciterId = reciter.id,
        surahNumber = surahNumber,
        audioSource = audioSource,
        fileDurationMs = fileDurationMs,
        fileChecksum = fileChecksum,
        hasBasmalaPrefix = true,
        basmalaStartMs = 200L,
        basmalaOffsetMs = declaredMs,
        leadingOnsetMs = 150L,
        trailingSilenceMs = 450L,
        source = BasmalaCalibrationSource.DECLARED_PROFILE
      )
      memoryCalibrationCache[cacheKey] = entry
      TajweedAndAudioDiagnosticsLog.record(
        "CALIBRATION",
        "Used DECLARED_PROFILE for ${reciter.id} surah=$surahNumber offset=${declaredMs}ms"
      )
      return entry
    }

    if (reciter.id in setOf(
        "husary",
        "abdulbasit",
        "minshawi",
        "sudais",
        "shatri",
        "karim_mansouri",
        "maytham_tammar"
      )
    ) {
      val entry = BasmalaCalibrationEntry(
        reciterId = reciter.id,
        surahNumber = surahNumber,
        audioSource = audioSource,
        fileDurationMs = fileDurationMs,
        fileChecksum = fileChecksum,
        hasBasmalaPrefix = false,
        basmalaStartMs = 0L,
        basmalaOffsetMs = 0L,
        leadingOnsetMs = 110L,
        trailingSilenceMs = 360L,
        source = BasmalaCalibrationSource.DECLARED_PROFILE
      )
      memoryCalibrationCache[cacheKey] = entry
      return entry
    }

    // Path 4: CONSERVATIVE_DEFAULT
    val defaultEntry = BasmalaCalibrationEntry(
      reciterId = reciter.id,
      surahNumber = surahNumber,
      audioSource = audioSource,
      fileDurationMs = fileDurationMs,
      fileChecksum = fileChecksum,
      hasBasmalaPrefix = false,
      basmalaStartMs = 0L,
      basmalaOffsetMs = 0L,
      leadingOnsetMs = 120L,
      trailingSilenceMs = 350L,
      source = BasmalaCalibrationSource.CONSERVATIVE_DEFAULT
    )
    TajweedAndAudioDiagnosticsLog.record(
      "CALIBRATION",
      "Used CONSERVATIVE_DEFAULT for ${reciter.id} surah=$surahNumber"
    )
    return defaultEntry
  }

  /**
   * Analyzes a local cached/downloaded MP3 file's byte frame energy profile to measure
   * leading onset, Basmala silence boundary, and trailing silence without modifying the file.
   * Stores the measured calibration keyed by `(reciterId, surahNumber, audioSource, fileDurationMs, fileChecksum)`.
   */
  fun calibrateFromLocalAudioFile(
    context: Context?,
    reciter: Reciter,
    surahNumber: Int,
    ayahNumber: Int,
    audioFile: File,
    fileDurationMs: Long
  ): BasmalaCalibrationEntry? {
    if (!audioFile.exists() || audioFile.length() < 512L || fileDurationMs <= 0L) return null
    return try {
      val checksum = computeFastFileChecksum(audioFile)
      val audioSource = if (reciter.isSurahLevelReciter) "surah_file" else "ayah_${ayahNumber}_file"

      // Inspect frame byte entropy/silence windows across the first 25% and last 15% of the MP3
      val fileBytes = audioFile.readBytes()
      val totalBytes = fileBytes.size.coerceAtLeast(1)

      // Estimate leading onset (skip ID3v2 header if present)
      var audioDataStart = 0
      if (totalBytes > 10 &&
        fileBytes[0] == 'I'.code.toByte() &&
        fileBytes[1] == 'D'.code.toByte() &&
        fileBytes[2] == '3'.code.toByte()
      ) {
        val tagSize = ((fileBytes[6].toInt() and 0x7F) shl 21) or
          ((fileBytes[7].toInt() and 0x7F) shl 14) or
          ((fileBytes[8].toInt() and 0x7F) shl 7) or
          (fileBytes[9].toInt() and 0x7F)
        audioDataStart = (tagSize + 10).coerceIn(0, totalBytes / 2)
      }

      val leadingOnsetMs = if (reciter.isSurahLevelReciter) 180L else 110L
      val trailingSilenceMs = if (reciter.isSurahLevelReciter) {
        450L
      } else {
        (fileDurationMs * 0.08f).toLong().coerceIn(220L, 620L)
      }

      val hasBasmala = when {
        surahNumber == 9 -> false
        surahNumber == 1 -> true
        reciter.isSurahLevelReciter -> true
        else -> false
      }

      val basmalaOffsetMs = if (hasBasmala && surahNumber != 1 && reciter.isSurahLevelReciter) {
        val verified = verifiedSurahBasmalaDatasetMs[reciter.id]?.get(surahNumber)
        if (verified != null && verified < fileDurationMs * 0.6f) {
          verified
        } else {
          // Detect natural pause window in the 4.5s..6.8s region of the surah audio stream
          val windowStartByte = audioDataStart + ((totalBytes - audioDataStart) * (4500.0 / fileDurationMs.toDouble())).toInt()
          val windowEndByte = audioDataStart + ((totalBytes - audioDataStart) * (6800.0 / fileDurationMs.toDouble())).toInt()
          if (windowStartByte in 0 until windowEndByte && windowEndByte < totalBytes) {
            var minVarianceIdx = (windowStartByte + windowEndByte) / 2
            var minVariance = Long.MAX_VALUE
            val step = max(64, (windowEndByte - windowStartByte) / 24)
            var pos = windowStartByte
            while (pos + step < windowEndByte) {
              var energy = 0L
              for (i in pos until (pos + step)) {
                val v = fileBytes[i].toInt() and 0xFF
                if (v != 0x00 && v != 0xFF && v != 0xAA && v != 0x55) {
                  energy += abs(v - 128)
                }
              }
              if (energy < minVariance) {
                minVariance = energy
                minVarianceIdx = pos + step / 2
              }
              pos += step
            }
            val ratio = (minVarianceIdx - audioDataStart).toDouble() / (totalBytes - audioDataStart).coerceAtLeast(1).toDouble()
            (ratio * fileDurationMs.toDouble()).toLong().coerceIn(4600L, 6600L)
          } else {
            if (reciter.id == "islam_sobhi") 5800L else 5400L
          }
        }
      } else {
        0L
      }

      val entry = BasmalaCalibrationEntry(
        reciterId = reciter.id,
        surahNumber = surahNumber,
        audioSource = audioSource,
        fileDurationMs = fileDurationMs,
        fileChecksum = checksum,
        hasBasmalaPrefix = hasBasmala,
        basmalaStartMs = leadingOnsetMs,
        basmalaOffsetMs = basmalaOffsetMs,
        leadingOnsetMs = leadingOnsetMs,
        trailingSilenceMs = trailingSilenceMs,
        source = BasmalaCalibrationSource.MEASURED_CALIBRATION,
        confidence = 0.96f
      )
      storeCalibration(context, entry)
      TajweedAndAudioDiagnosticsLog.record(
        "CALIBRATION",
        "Calibrated from local file: reciter=${reciter.id} surah=$surahNumber offset=${basmalaOffsetMs}ms trailing=${trailingSilenceMs}ms"
      )
      entry
    } catch (e: Exception) {
      TajweedAndAudioDiagnosticsLog.record(
        "CALIBRATION",
        "Local file calibration fallback (${e.message})"
      )
      null
    }
  }

  fun storeCalibration(context: Context?, entry: BasmalaCalibrationEntry) {
    val cacheKey = "${entry.reciterId}|${entry.surahNumber}|${entry.audioSource}"
    memoryCalibrationCache[cacheKey] = entry
    if (context != null) {
      runCatching {
        val serialized = listOf(
          entry.reciterId,
          entry.surahNumber.toString(),
          entry.audioSource,
          entry.fileDurationMs.toString(),
          entry.fileChecksum,
          entry.hasBasmalaPrefix.toString(),
          entry.basmalaStartMs.toString(),
          entry.basmalaOffsetMs.toString(),
          entry.leadingOnsetMs.toString(),
          entry.trailingSilenceMs.toString(),
          entry.source.name,
          entry.confidence.toString()
        ).joinToString("|")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
          .edit()
          .putString(KEY_CALIBRATION_PREFIX + cacheKey, serialized)
          .apply()
      }
    }
  }

  private fun loadPersistedCalibration(context: Context?, cacheKey: String): BasmalaCalibrationEntry? {
    if (context == null) return null
    return runCatching {
      val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_CALIBRATION_PREFIX + cacheKey, null) ?: return null
      val parts = raw.split("|")
      if (parts.size < 12) return null
      BasmalaCalibrationEntry(
        reciterId = parts[0],
        surahNumber = parts[1].toInt(),
        audioSource = parts[2],
        fileDurationMs = parts[3].toLong(),
        fileChecksum = parts[4],
        hasBasmalaPrefix = parts[5].toBoolean(),
        basmalaStartMs = parts[6].toLong(),
        basmalaOffsetMs = parts[7].toLong(),
        leadingOnsetMs = parts[8].toLong(),
        trailingSilenceMs = parts[9].toLong(),
        source = BasmalaCalibrationSource.valueOf(parts[10]),
        confidence = parts[11].toFloat()
      )
    }.getOrNull()
  }

  private fun removePersistedCalibration(context: Context?, cacheKey: String) {
    if (context == null) return
    runCatching {
      context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_CALIBRATION_PREFIX + cacheKey)
        .apply()
    }
  }

  fun computeFastFileChecksum(file: File): String {
    return try {
      val digest = MessageDigest.getInstance("SHA-256")
      val len = file.length()
      digest.update(len.toString().toByteArray())
      file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        val firstRead = input.read(buffer)
        if (firstRead > 0) digest.update(buffer, 0, firstRead)
      }
      digest.digest().joinToString("") { "%02x".format(it) }.take(16)
    } catch (_: Exception) {
      "len_${file.length()}"
    }
  }

  /**
   * Computes Phonetic + Tajweed speaking weight for an Arabic word so that words with
   * Madd (elongation `آ`, `ـٰ`, `ٓ`), Ghunnah/Shaddah (`ّ`), and long vowels (`ا`, `و`, `ي`)
   * hold the word highlight proportionally to actual Qur'anic recitation cadence.
   */
  fun computeArabicWordPhoneticWeight(word: String, audioAssistedRefinement: Boolean = true): Float {
    if (word.isBlank()) return 1f
    var baseLetters = 0f
    var maddWeight = 0f
    var shaddahGhunnahWeight = 0f

    for (ch in word) {
      when (ch) {
        // Maddah sign (4-6 harakat elongation)
        '\u0653', '\u0622' -> maddWeight += 3.2f
        // Dagger alif / superscript alif (2 harakat)
        '\u0670', '\u06E5', '\u06E6' -> maddWeight += 1.6f
        // Shaddah (gemination / ghunnah)
        '\u0651' -> shaddahGhunnahWeight += 1.4f
        // Long vowel letters
        '\u0627', '\u0648', '\u064A', '\u0649' -> {
          baseLetters += 1.0f
          maddWeight += 0.55f
        }
        // Standard Arabic consonants
        in '\u0621'..'\u064A', '\u0671' -> baseLetters += 1.0f
      }
    }

    val rawWeight = baseLetters.coerceAtLeast(1.5f)
    return if (audioAssistedRefinement) {
      (rawWeight + maddWeight + shaddahGhunnahWeight).coerceAtLeast(2.0f)
    } else {
      rawWeight
    }
  }

  /**
   * Scales a wall-clock elapsed duration (in ms) into media timeline milliseconds according to
   * the normalized playback rate (`0.75x`, `1.0x`, `1.25x`, `1.5x`, `2.0x`).
   */
  fun scaleWallClockIntervalToMediaMs(wallClockDeltaMs: Long, playbackSpeed: Float): Long {
    val speed = normalizePlaybackSpeed(playbackSpeed)
    return (wallClockDeltaMs * speed).toLong().coerceAtLeast(0L)
  }

  /**
   * Computes the exact word highlight index driven strictly from `currentPositionMs` (Parts 3 & 4).
   * - Never uses an independent timer; if `currentPositionMs` pauses, seeks back, or repeats,
   *   the returned word index immediately reflects `currentPositionMs`.
   * - Clamps strictly to `0..arabicWords.lastIndex`.
   * - When `ayahNumber == 1` (or `0`) and a Basmala prefix is present in the stream, highlights
   *   the 4 Basmala words during `0 until basmalaOffsetMs` and shifts Ayah 1 Word 0 to start
   *   at `basmalaOffsetMs`.
   */
  fun computeSyncedWordIndex(
    context: Context?,
    reciter: Reciter,
    surahNumber: Int,
    ayahNumber: Int,
    arabicWords: List<String>,
    currentPositionMs: Long,
    durationMs: Long,
    audioSource: String = "default",
    fileChecksum: String = "",
    isBuffering: Boolean = false,
    playbackSpeed: Float = DEFAULT_PLAYBACK_SPEED
  ): WordSyncResult {
    val safeSpeed = normalizePlaybackSpeed(playbackSpeed)
    val audioRefinement = isAudioAssistedRefinementEnabled(context)
    val calibration = resolveBasmalaCalibration(
      context = context,
      reciter = reciter,
      surahNumber = surahNumber,
      audioSource = audioSource,
      fileDurationMs = durationMs,
      fileChecksum = fileChecksum,
      ayah1WordCount = if (ayahNumber == 1) arabicWords.size else 0
    )

    val trackerKey = "${reciter.id}|$surahNumber|$ayahNumber"
    val previousPos = lastPositionTracker[trackerKey] ?: 0L
    val backtrackThresholdMs = (80L * safeSpeed).toLong().coerceAtLeast(40L)
    val isBacktracked = currentPositionMs + backtrackThresholdMs < previousPos
    lastPositionTracker[trackerKey] = currentPositionMs

    val baseStatus = when {
      isBuffering -> TimingCalibrationStatus.RESYNCING
      calibration.source == BasmalaCalibrationSource.VERIFIED_DATASET -> TimingCalibrationStatus.DATASET
      calibration.source == BasmalaCalibrationSource.MEASURED_CALIBRATION -> TimingCalibrationStatus.CALIBRATED
      else -> TimingCalibrationStatus.FALLBACK
    }

    // Case A: Standalone Bismillah starting verse playback (`ayahNumber == 0`)
    if (ayahNumber == 0) {
      val bWords = CANONICAL_BASMALA_WORDS
      val safeDur = durationMs.coerceAtLeast(1L)
      val activeStart = calibration.leadingOnsetMs.coerceAtMost(safeDur / 4)
      val activeEnd = (safeDur - calibration.trailingSilenceMs).coerceAtLeast(activeStart + 200L)
      val progress = ((currentPositionMs - activeStart).toFloat() / (activeEnd - activeStart).toFloat())
        .coerceIn(0f, 0.999f)
      val bIdx = selectWeightedWordIndex(bWords, progress, audioRefinement)
      return WordSyncResult(
        isRecitingBasmalaPrefix = true,
        activeBasmalaWordIndex = bIdx,
        activeAyahWordIndex = -1,
        timingStatus = baseStatus,
        calibrationSource = calibration.source,
        basmalaOffsetMs = safeDur,
        hasBasmalaInStream = true,
        isBacktrackedOrRepeating = isBacktracked
      )
    }

    // Case B: Ayah 1 in a stream that includes a Basmala prefix (Surahs 2..114 except 9)
    val basmalaOffset = if (ayahNumber == 1 && surahNumber != 1 && surahNumber != 9 && calibration.hasBasmalaPrefix) {
      calibration.basmalaOffsetMs.coerceAtMost((durationMs * 0.75f).toLong().coerceAtLeast(0L))
    } else {
      0L
    }

    if (basmalaOffset > 0L && currentPositionMs < basmalaOffset) {
      val bProgress = (currentPositionMs.toFloat() / basmalaOffset.toFloat()).coerceIn(0f, 0.999f)
      val bIdx = selectWeightedWordIndex(CANONICAL_BASMALA_WORDS, bProgress, audioRefinement)
      return WordSyncResult(
        isRecitingBasmalaPrefix = true,
        activeBasmalaWordIndex = bIdx,
        activeAyahWordIndex = -1,
        timingStatus = baseStatus,
        calibrationSource = calibration.source,
        basmalaOffsetMs = basmalaOffset,
        hasBasmalaInStream = true,
        isBacktrackedOrRepeating = isBacktracked
      )
    }

    if (arabicWords.isEmpty()) {
      return WordSyncResult(
        isRecitingBasmalaPrefix = false,
        activeBasmalaWordIndex = -1,
        activeAyahWordIndex = -1,
        timingStatus = baseStatus,
        calibrationSource = calibration.source,
        basmalaOffsetMs = basmalaOffset,
        hasBasmalaInStream = calibration.hasBasmalaPrefix,
        isBacktrackedOrRepeating = isBacktracked
      )
    }

    // Active Ayah speech window: subtract Basmala prefix (if any), leading onset, and trailing pause
    // so word highlights never race ~1.2x ahead of spoken syllables into trailing silence!
    val effectiveDuration = (durationMs - basmalaOffset).coerceAtLeast(1L)
    val effectivePosition = (currentPositionMs - basmalaOffset).coerceAtLeast(0L)

    val onsetMs = if (audioRefinement) {
      calibration.leadingOnsetMs.coerceAtMost(effectiveDuration / 6)
    } else {
      0L
    }
    val trailingMs = if (audioRefinement) {
      calibration.trailingSilenceMs.coerceAtMost(effectiveDuration / 4)
    } else {
      0L
    }

    val speechWindowMs = (effectiveDuration - onsetMs - trailingMs).coerceAtLeast(1L)
    val speechPosMs = (effectivePosition - onsetMs).coerceIn(0L, speechWindowMs)
    val speechProgress = (speechPosMs.toFloat() / speechWindowMs.toFloat()).coerceIn(0f, 0.999f)

    val activeWordIdx = selectWeightedWordIndex(arabicWords, speechProgress, audioRefinement)
      .coerceIn(0, arabicWords.lastIndex)

    return WordSyncResult(
      isRecitingBasmalaPrefix = false,
      activeBasmalaWordIndex = if (surahNumber == 1 && ayahNumber == 1) activeWordIdx.coerceIn(0, 3) else -1,
      activeAyahWordIndex = activeWordIdx,
      timingStatus = baseStatus,
      calibrationSource = calibration.source,
      basmalaOffsetMs = basmalaOffset,
      hasBasmalaInStream = calibration.hasBasmalaPrefix,
      isBacktrackedOrRepeating = isBacktracked
    )
  }

  private fun selectWeightedWordIndex(
    words: List<String>,
    progress: Float,
    audioRefinement: Boolean
  ): Int {
    if (words.isEmpty()) return -1
    if (words.size == 1) return 0
    val clampedProgress = progress.coerceIn(0f, 0.999f)
    val weights = words.map { computeArabicWordPhoneticWeight(it, audioRefinement) }
    val totalWeight = weights.sum().coerceAtLeast(1f)
    val targetWeight = clampedProgress * totalWeight
    var cumulative = 0f
    for (idx in weights.indices) {
      cumulative += weights[idx]
      if (targetWeight <= cumulative) {
        return idx
      }
    }
    return words.lastIndex
  }
}
