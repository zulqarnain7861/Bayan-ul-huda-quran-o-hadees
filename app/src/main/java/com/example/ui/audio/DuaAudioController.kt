package com.example.ui.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.widget.Toast
import com.example.data.model.DailyDuaItem
import com.example.data.model.DuaCategory
import com.example.data.model.QaidaPracticeItem
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class DuaAudioLanguageMode(val label: String) {
  ARABIC_ONLY("Arabic Only"),
  ARABIC_AND_ENGLISH("Arabic + English"),
  ARABIC_AND_URDU("Arabic + Urdu"),
  ARABIC_ENGLISH_URDU("Arabic + English + Urdu")
}

data class DuaVocalPlaybackState(
  val isPlaying: Boolean = false,
  val activeDuaId: String? = null,
  val activeDuaTitle: String = "",
  val activeStageLabel: String = "",
  val currentRepetition: Int = 0,
  val totalRepetitions: Int = 0,
  val activeArabicWordIndex: Int = -1,
  val totalArabicWords: Int = 0,
  val activeLetterIndexInWord: Int = -1,
  val progressFraction: Float = 0f,
  val languageMode: DuaAudioLanguageMode = DuaAudioLanguageMode.ARABIC_ONLY,
  val speechRatePreset: Float = 1.0f, // 1.0f = Normal, 0.65f = Slow Teacher Mode
  val repeatCountPreset: Int = 1,     // 1x, 3x, 5x
  val includeUrduMeaning: Boolean = false,
  val includeEnglishMeaning: Boolean = false,
  val isComingSoonNotice: Boolean = false
) {
  val speechRateMultiplier: Float get() = speechRatePreset
  val repeatCountTarget: Int get() = repeatCountPreset
  val activeWordIndex: Int get() = activeArabicWordIndex
}

typealias DuaAudioPlaybackState = DuaVocalPlaybackState

/**
 * Authentic Audio Controller for Noorani Qaida, Daily Duas, Iman, Kalimas, Wudu, and Namaz.
 *
 * STRICT AUDIO POLICY (ZERO AI / TTS VOICE):
 * - Noorani Qaida (all 17 lessons, 149 items `l1_01`..`l17_07`):
 *   - Plays 100% offline authentic human audio bundled in `assets/qaida_audio/{id}.mp3`.
 *   - Never uses AI/TTS voice and never shows "Coming Soon".
 * - Iman, Kalimas, Namaz, Wudu, and Daily Duas:
 *   - For Quranic Duas and verified Quranic verses (`quranSurahAyah != null`), plays ONLY the exact single
 *     verse using authentic human reciter audio (`AudioPlayerManager.playSingleAyahOnly`).
 *   - For Prophetic & Masnoon Duas with verified human audio (`humanAudioUrl != null` from Hisn al-Muslim /
 *     OpenDua / EveryAyah), plays and caches the authentic human recording with zero AI voice.
 *   - For non-Quranic items without verified human audio, displays:
 *     "Real voice coming soon, In Sha Allah."
 */
class DuaAudioController(
  private val context: Context,
  private val quranAudioPlayerManager: AudioPlayerManager = AudioPlayerManager(context)
) {

  companion object {
    const val REAL_VOICE_COMING_SOON_MESSAGE = "No audio available"
    const val QAIDA_ASSET_FOLDER = "qaida_audio"
    private const val DUA_CACHE_SUBDIR = "dua_audio_cache_v3"

    @Volatile
    private var instance: DuaAudioController? = null

    fun getInstance(context: Context): DuaAudioController {
      return instance ?: synchronized(this) {
        instance ?: DuaAudioController(context.applicationContext).also { instance = it }
      }
    }

    /**
     * Builds verified human audio URLs for Hisn al-Muslim / Masnun Dua / EveryAyah recordings.
     * Note: Archive.org's `HisnulMuslimAudio_201510/n{N}.mp3` has shifted track numbering for N >= 24
     * because rows #24 and #26 in `hisnul_database.csv` have no audio, so `n{N}.mp3` must NEVER be
     * used as a 1-to-1 mirror for `{N}hm.mp3` when N >= 24. Instead, any legacy `n{N}.mp3` URL is
     * mapped directly to `sheikhhanif/Hisnul_Muslim_Database/master/audio/{N}hm.mp3`.
     */
    fun buildCandidateHumanAudioUrls(primaryUrl: String): List<String> {
      val trimmed = primaryUrl.trim()
      if (trimmed.isEmpty()) return emptyList()
      val urls = mutableListOf<String>()

      val legacyArchiveHisnNumber = Regex("""HisnulMuslimAudio_201510/n(\d+)\.mp3""")
        .find(trimmed)?.groupValues?.getOrNull(1)?.toIntOrNull()
      val githubHisnNumber = Regex("""Hisnul_Muslim_Database/master/audio/(\d+)hm\.mp3""")
        .find(trimmed)?.groupValues?.getOrNull(1)?.toIntOrNull()

      val hisnNumber = githubHisnNumber ?: legacyArchiveHisnNumber
      if (hisnNumber != null) {
        val canonicalGithubUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/${hisnNumber}hm.mp3"
        urls.add(canonicalGithubUrl)
        if (hisnNumber < 24) {
          val archiveMirrorForEarlyTracks = "https://archive.org/download/HisnulMuslimAudio_201510/n${hisnNumber}.mp3"
          if (archiveMirrorForEarlyTracks !in urls) {
            urls.add(archiveMirrorForEarlyTracks)
          }
        }
      } else {
        urls.add(trimmed)
      }
      return urls.filter { com.example.data.remote.SecurityIntegrityGuard.isUrlTrustedAndAllowed(it) }
    }

    /**
     * Checks whether an offline Noorani Qaida MP3 exists in `assets/qaida_audio/{id}.mp3`
     * (works both on Android runtime via AssetManager and in local JVM unit tests).
     */
    fun hasOfflineQaidaAudioAsset(context: Context?, id: String): Boolean {
      if (id.isBlank()) return false
      val assetRelPath = "$QAIDA_ASSET_FOLDER/$id.mp3"
      if (context != null) {
        val existsInAssets = runCatching {
          context.assets.openFd(assetRelPath).use { it.length > 0L }
        }.getOrElse {
          runCatching {
            context.assets.open(assetRelPath).use { it.available() > 0 }
          }.getOrDefault(false)
        }
        if (existsInAssets) return true
      }
      val candidateFiles = listOf(
        File("app/src/main/assets/$assetRelPath"),
        File("src/main/assets/$assetRelPath")
      )
      return candidateFiles.any { it.exists() && it.length() > 100L }
    }

    /**
     * Returns the canonical vocalized Arabic phonetic script for Noorani Qaida items.
     */
    fun buildNooraniQaidaVocalizedArabicForMaleTts(
      id: String,
      arabicText: String,
      transliteration: String
    ): String {
      return when (id) {
        "l1_01" -> "أَلِفْ"
        "l1_02" -> "بَاءْ"
        "l1_03" -> "تَاءْ"
        "l1_04" -> "ثَاءْ"
        "l1_05" -> "جِيمْ"
        "l1_06" -> "حَاءْ"
        "l1_07" -> "خَاءْ"
        "l1_08" -> "دَالْ"
        "l1_09" -> "ذَالْ"
        "l1_10" -> "رَاءْ"
        "l1_11" -> "زَايْ"
        "l1_12" -> "سِينْ"
        "l1_13" -> "شِينْ"
        "l1_14" -> "صَادْ"
        "l1_15" -> "ضَادْ"
        "l1_16" -> "طَاءْ"
        "l1_17" -> "ظَاءْ"
        "l1_18" -> "عَيْنْ"
        "l1_19" -> "غَيْنْ"
        "l1_20" -> "فَاءْ"
        "l1_21" -> "قَافْ"
        "l1_22" -> "كَافْ"
        "l1_23" -> "لَامْ"
        "l1_24" -> "مِيمْ"
        "l1_25" -> "نُونْ"
        "l1_26" -> "وَاوْ"
        "l1_27" -> "هَاءْ"
        "l1_28" -> "هَمْزَةْ"
        "l1_29" -> "يَاءْ"
        "l2_01" -> "لَامْ أَلِفْ"
        "l2_02" -> "بَاءْ أَلِفْ"
        "l2_03" -> "تَاءْ أَلِفْ"
        "l2_04" -> "ثَاءْ أَلِفْ"
        "l2_05" -> "نُونْ أَلِفْ"
        "l2_06" -> "يَاءْ أَلِفْ"
        "l2_07" -> "بَاءْ جِيمْ"
        "l2_08" -> "بَاءْ تَاءْ ثَاءْ"
        "l2_09" -> "يَاءْ هَاءْ بَاءْ"
        "l2_10" -> "سِينْ لَامْ كَافْ"
        else -> arabicText.ifBlank { transliteration }
      }
    }
  }

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var vocalJob: Job? = null
  private var offlineMediaPlayer: MediaPlayer? = null

  private val _playbackState = MutableStateFlow(DuaVocalPlaybackState())
  val playbackState: StateFlow<DuaVocalPlaybackState> = _playbackState.asStateFlow()

  fun clearComingSoonNotice() {
    if (_playbackState.value.isComingSoonNotice) {
      _playbackState.value = _playbackState.value.copy(
        isComingSoonNotice = false,
        activeDuaId = null,
        activeDuaTitle = "",
        activeStageLabel = ""
      )
    }
  }

  fun setSpeechRatePreset(rate: Float) {
    val clamped = rate.coerceIn(0.5f, 1.5f)
    _playbackState.value = _playbackState.value.copy(speechRatePreset = clamped)
  }

  fun toggleSlowPlaybackMode() {
    val current = _playbackState.value.speechRatePreset
    val next = if (current < 0.85f) 1.0f else 0.65f
    setSpeechRatePreset(next)
  }

  fun setRepeatCountPreset(repeatCount: Int) {
    val safeRepeat = repeatCount.coerceIn(1, 10)
    _playbackState.value = _playbackState.value.copy(repeatCountPreset = safeRepeat)
  }

  fun toggleRepeatTarget() {
    val next = when (_playbackState.value.repeatCountPreset) {
      1 -> 3
      3 -> 5
      else -> 1
    }
    setRepeatCountPreset(next)
  }

  fun setMeaningPlaybackOptions(includeUrdu: Boolean, includeEnglish: Boolean) {
    _playbackState.value = _playbackState.value.copy(
      includeUrduMeaning = includeUrdu,
      includeEnglishMeaning = includeEnglish
    )
  }

  fun setLanguageMode(mode: DuaAudioLanguageMode) {
    _playbackState.value = _playbackState.value.copy(
      languageMode = mode,
      includeEnglishMeaning = mode == DuaAudioLanguageMode.ARABIC_AND_ENGLISH || mode == DuaAudioLanguageMode.ARABIC_ENGLISH_URDU,
      includeUrduMeaning = mode == DuaAudioLanguageMode.ARABIC_AND_URDU || mode == DuaAudioLanguageMode.ARABIC_ENGLISH_URDU
    )
  }

  fun hasOfflineQaidaAudio(id: String): Boolean {
    return hasOfflineQaidaAudioAsset(context, id)
  }

  /**
   * Plays an item from Noorani Qaida, Iman/Kalimas, Wudu/Namaz, or Daily Duas.
   * - If a bundled offline authentic human MP3 exists in `assets/qaida_audio/{id}.mp3`, plays it offline immediately.
   * - Otherwise, if `quranSurahAyah != null`, plays the exact Quranic verse via `quranAudioPlayerManager`.
   * - Otherwise, if `humanAudioUrl` is provided, plays the verified human recording (cached for offline reuse).
   * - Otherwise, shows "Real voice coming soon, In Sha Allah." (Never uses AI/TTS voice).
   */
  fun playLearningItem(
    id: String,
    title: String,
    arabicText: String,
    transliteration: String = "",
    englishText: String = "",
    urduText: String = "",
    urduMeaning: String = "",
    englishMeaning: String = "",
    quranSurahAyah: Pair<Int, Int>? = null,
    humanAudioUrl: String? = null,
    speechRateOverride: Float? = null,
    repeatCountOverride: Int? = null,
    preferQuranReciterIfAvailable: Boolean = true,
    allowClearMaleAiVoiceFallback: Boolean = false
  ) {
    val current = _playbackState.value
    val targetReps = (repeatCountOverride ?: current.repeatCountPreset).coerceIn(1, 10)
    val targetRate = (speechRateOverride ?: current.speechRatePreset).coerceIn(0.5f, 1.5f)

    if (hasOfflineQaidaAudio(id)) {
      playOfflineQaidaAsset(
        id = id,
        title = title,
        arabicText = arabicText,
        transliteration = transliteration,
        repetitions = targetReps,
        speechRate = targetRate
      )
      return
    }

    val syntheticDua = DailyDuaItem(
      id = id,
      title = title,
      category = DuaCategory.HOME_MASJID,
      occasionBadge = "Learn Islam",
      arabicText = arabicText,
      transliteration = transliteration,
      englishTranslation = englishText.ifBlank { englishMeaning },
      urduTranslation = urduText.ifBlank { urduMeaning },
      authenticReference = "",
      virtueAndTiming = "",
      recommendedCount = targetReps,
      quranSurahAyah = quranSurahAyah,
      humanAudioUrl = humanAudioUrl
    )
    playDuaVocal(
      dua = syntheticDua,
      includeTranslations = false,
      repetitions = targetReps,
      speechRate = targetRate,
      includeUrduOverride = false,
      preferQuranReciterIfAvailable = true
    )
  }

  /**
   * Plays all practice items of a Noorani Qaida lesson sequentially using their bundled offline human MP3s.
   */
  fun playQaidaLessonSequence(
    lessonNumber: Int,
    lessonTitle: String,
    items: List<QaidaPracticeItem>
  ) {
    if (items.isEmpty()) return
    val lessonSeqId = "qaida_lesson_seq_$lessonNumber"
    if (_playbackState.value.isPlaying && _playbackState.value.activeDuaId == lessonSeqId) {
      stop()
      return
    }

    stop()
    val targetRate = _playbackState.value.speechRatePreset.coerceIn(0.5f, 1.5f)
    val targetReps = _playbackState.value.repeatCountPreset.coerceIn(1, 5)

    vocalJob = scope.launch {
      for ((idx, item) in items.withIndex()) {
        if (!isActive) break
        for (rep in 1..targetReps) {
          if (!isActive) break
          _playbackState.value = _playbackState.value.copy(
            isPlaying = true,
            activeDuaId = item.id,
            activeDuaTitle = "${item.arabicText} (${item.transliteration})",
            activeStageLabel = "Lesson $lessonNumber Offline Audio (${idx + 1}/${items.size}) • ${item.transliteration}",
            currentRepetition = rep,
            totalRepetitions = targetReps,
            activeArabicWordIndex = 0,
            totalArabicWords = 1,
            progressFraction = (idx + 1).toFloat() / items.size.toFloat(),
            isComingSoonNotice = false
          )
          playSingleAssetBlocking(
            assetRelPath = "$QAIDA_ASSET_FOLDER/${item.id}.mp3",
            speechRate = targetRate
          )
          if (rep < targetReps && isActive) {
            delay(250)
          }
        }
        if (isActive && idx < items.lastIndex) {
          delay(320)
        }
      }

      if (isActive) {
        resetPlaybackState()
      }
    }
  }

  private fun playOfflineQaidaAsset(
    id: String,
    title: String,
    arabicText: String,
    transliteration: String,
    repetitions: Int,
    speechRate: Float
  ) {
    if (_playbackState.value.isPlaying && _playbackState.value.activeDuaId == id) {
      stop()
      return
    }

    stop()

    val safeReps = repetitions.coerceIn(1, 10)
    val arabicWords = tokenizeArabicWords(arabicText)
    val assetRelPath = "$QAIDA_ASSET_FOLDER/$id.mp3"

    vocalJob = scope.launch {
      _playbackState.value = _playbackState.value.copy(
        isPlaying = true,
        activeDuaId = id,
        activeDuaTitle = title,
        activeStageLabel = "Offline Authentic Audio • $transliteration",
        currentRepetition = 1,
        totalRepetitions = safeReps,
        activeArabicWordIndex = 0,
        totalArabicWords = arabicWords.size.coerceAtLeast(1),
        isComingSoonNotice = false
      )

      for (rep in 1..safeReps) {
        if (!isActive) break

        _playbackState.value = _playbackState.value.copy(
          currentRepetition = rep,
          activeStageLabel = "Offline Authentic Audio ($transliteration) • Rep $rep/$safeReps",
          activeArabicWordIndex = 0,
          totalArabicWords = arabicWords.size.coerceAtLeast(1),
          isComingSoonNotice = false
        )

        val playedOk = playSingleAssetBlocking(
          assetRelPath = assetRelPath,
          speechRate = speechRate,
          onProgress = { posMs, durMs ->
            val totalWords = arabicWords.size.coerceAtLeast(1)
            val wordIdx = ((posMs.toFloat() / durMs.coerceAtLeast(1).toFloat()) * totalWords)
              .toInt()
              .coerceIn(0, totalWords - 1)
            _playbackState.value = _playbackState.value.copy(
              activeArabicWordIndex = wordIdx,
              totalArabicWords = totalWords,
              progressFraction = (posMs.toFloat() / durMs.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            )
          }
        )

        if (!playedOk) {
          break
        }

        if (rep < safeReps && isActive) {
          delay(300)
        }
      }

      if (isActive) {
        resetPlaybackState()
      }
    }
  }

  private suspend fun playSingleAssetBlocking(
    assetRelPath: String,
    speechRate: Float,
    onProgress: ((Int, Int) -> Unit)? = null
  ): Boolean {
    releaseOfflineMediaPlayer()
    var completed = false
    var failed = false

    val player = try {
      MediaPlayer().apply {
        context.assets.openFd(assetRelPath).use { afd ->
          setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        }
        setOnCompletionListener {
          completed = true
        }
        setOnErrorListener { _, _, _ ->
          failed = true
          true
        }
        prepare()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && speechRate != 1.0f) {
          runCatching {
            playbackParams = PlaybackParams().setSpeed(speechRate.coerceIn(0.5f, 1.5f))
          }
        }
        start()
      }
    } catch (_: Exception) {
      null
    }

    if (player == null) {
      return false
    }

    offlineMediaPlayer = player
    val durationMs = runCatching { player.duration.coerceAtLeast(400) }.getOrDefault(1500)
    var waitedMs = 0L
    val maxWaitMs = (durationMs * 2L + 4000L).coerceIn(3000L, 45000L)

    while (!completed && !failed && waitedMs < maxWaitMs) {
      val isStillPlaying = runCatching { player.isPlaying }.getOrDefault(false)
      val currentPos = runCatching { player.currentPosition }.getOrDefault(waitedMs.toInt())
      onProgress?.invoke(currentPos, durationMs)
      if (!isStillPlaying && waitedMs >= 350L) {
        break
      }
      delay(80)
      waitedMs += 80
    }

    releaseOfflineMediaPlayer()
    return !failed
  }

  private suspend fun playCachedFileBlocking(
    audioFile: File,
    speechRate: Float,
    onProgress: ((Int, Int) -> Unit)? = null
  ): Boolean {
    if (!audioFile.exists() || audioFile.length() < 120L) return false
    releaseOfflineMediaPlayer()
    var completed = false
    var failed = false

    val player = try {
      MediaPlayer().apply {
        FileInputStream(audioFile).use { fis ->
          setDataSource(fis.fd)
        }
        setOnCompletionListener {
          completed = true
        }
        setOnErrorListener { _, _, _ ->
          failed = true
          true
        }
        prepare()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && speechRate != 1.0f) {
          runCatching {
            playbackParams = PlaybackParams().setSpeed(speechRate.coerceIn(0.5f, 1.5f))
          }
        }
        start()
      }
    } catch (_: Exception) {
      null
    }

    if (player == null) {
      return false
    }

    offlineMediaPlayer = player
    val durationMs = runCatching { player.duration.coerceAtLeast(500) }.getOrDefault(2500)
    var waitedMs = 0L
    val maxWaitMs = (durationMs * 2L + 5000L).coerceIn(4000L, 90000L)

    while (!completed && !failed && waitedMs < maxWaitMs) {
      val isStillPlaying = runCatching { player.isPlaying }.getOrDefault(false)
      val currentPos = runCatching { player.currentPosition }.getOrDefault(waitedMs.toInt())
      onProgress?.invoke(currentPos, durationMs)
      if (!isStillPlaying && waitedMs >= 400L) {
        break
      }
      delay(90)
      waitedMs += 90
    }

    releaseOfflineMediaPlayer()
    return !failed
  }

  private suspend fun resolveOrDownloadVerifiedHumanAudioFiles(
    duaId: String,
    rawAudioUrls: String
  ): List<File>? = withContext(Dispatchers.IO) {
    runCatching {
      File(context.cacheDir, "dua_audio_cache").deleteRecursively()
      File(context.cacheDir, "dua_audio_cache_v2").deleteRecursively()
    }
    val segments = rawAudioUrls
      .split("|")
      .map { it.trim() }
      .filter { it.isNotEmpty() }
    if (segments.isEmpty()) return@withContext null

    val safeBaseName = duaId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").ifBlank { "dua_audio" }
    val cacheDir = File(context.cacheDir, DUA_CACHE_SUBDIR).apply { mkdirs() }
    val resolvedFiles = mutableListOf<File>()

    for ((segIdx, segmentUrl) in segments.withIndex()) {
      val fileSuffix = if (segments.size == 1) "" else "_part${segIdx + 1}"
      val targetFile = File(cacheDir, "${safeBaseName}${fileSuffix}.mp3")
      if (targetFile.exists() && targetFile.length() > 256L) {
        resolvedFiles.add(targetFile)
        continue
      }

      val candidates = buildCandidateHumanAudioUrls(segmentUrl)
      var segmentDownloaded = false
      for (urlStr in candidates) {
        if (!com.example.data.remote.SecurityIntegrityGuard.isUrlTrustedAndAllowed(urlStr)) {
          continue
        }
        val downloaded = runCatching {
          val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
            connectTimeout = 7000
            readTimeout = 12000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "BayanAlHuda-Android/1.0")
          }
          try {
            val contentLength = conn.contentLengthLong
            if (contentLength > com.example.data.remote.SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
              false
            } else if (conn.responseCode in 200..299) {
              val tempFile = File(cacheDir, "${safeBaseName}${fileSuffix}_tmp.mp3")
              var totalBytesRead = 0L
              var exceededMax = false
              conn.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                  val buffer = ByteArray(8192)
                  while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    totalBytesRead += read
                    if (totalBytesRead > com.example.data.remote.SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
                      exceededMax = true
                      break
                    }
                    output.write(buffer, 0, read)
                  }
                }
              }
              if (!exceededMax && tempFile.exists() && tempFile.length() > 256L) {
                tempFile.renameTo(targetFile)
                true
              } else {
                tempFile.delete()
                false
              }
            } else {
              false
            }
          } finally {
            conn.disconnect()
          }
        }.getOrDefault(false)

        if (downloaded && targetFile.exists() && targetFile.length() > 256L) {
          resolvedFiles.add(targetFile)
          segmentDownloaded = true
          break
        }
      }
      if (!segmentDownloaded) {
        return@withContext null
      }
    }

    resolvedFiles.takeIf { it.size == segments.size }
  }

  private fun playVerifiedHumanAudioUrl(
    dua: DailyDuaItem,
    primaryAudioUrl: String,
    repetitions: Int,
    speechRate: Float
  ) {
    stop()
    val safeReps = repetitions.coerceIn(1, 10)
    val arabicWords = tokenizeArabicWords(dua.arabicText)

    vocalJob = scope.launch {
      _playbackState.value = _playbackState.value.copy(
        isPlaying = true,
        activeDuaId = dua.id,
        activeDuaTitle = dua.title,
        activeStageLabel = "Loading Authentic Human Audio...",
        currentRepetition = 1,
        totalRepetitions = safeReps,
        activeArabicWordIndex = 0,
        totalArabicWords = arabicWords.size.coerceAtLeast(1),
        isComingSoonNotice = false
      )

      val audioFiles = resolveOrDownloadVerifiedHumanAudioFiles(dua.id, primaryAudioUrl)
      if (!isActive) return@launch

      if (audioFiles.isNullOrEmpty()) {
        showRealVoiceComingSoon(duaId = dua.id, duaTitle = dua.title)
        return@launch
      }

      val totalSegments = audioFiles.size
      for (rep in 1..safeReps) {
        if (!isActive) break
        _playbackState.value = _playbackState.value.copy(
          currentRepetition = rep,
          activeStageLabel = "Authentic Human Recitation • Rep $rep/$safeReps",
          activeArabicWordIndex = 0,
          totalArabicWords = arabicWords.size.coerceAtLeast(1),
          isComingSoonNotice = false
        )

        for ((segIdx, audioFile) in audioFiles.withIndex()) {
          if (!isActive) break
          val playedOk = playCachedFileBlocking(
            audioFile = audioFile,
            speechRate = speechRate,
            onProgress = { posMs, durMs ->
              val totalWords = arabicWords.size.coerceAtLeast(1)
              val segFrac = (posMs.toFloat() / durMs.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
              val overallFrac = ((segIdx.toFloat() + segFrac) / totalSegments.toFloat()).coerceIn(0f, 1f)
              val wordIdx = (overallFrac * totalWords).toInt().coerceIn(0, totalWords - 1)
              _playbackState.value = _playbackState.value.copy(
                activeArabicWordIndex = wordIdx,
                totalArabicWords = totalWords,
                progressFraction = overallFrac,
                activeStageLabel = "Authentic Human Recitation • Word ${wordIdx + 1}/$totalWords (Rep $rep/$safeReps)"
              )
            }
          )

          if (!playedOk) {
            showRealVoiceComingSoon(duaId = dua.id, duaTitle = dua.title)
            return@launch
          }

          if (segIdx < audioFiles.lastIndex && isActive) {
            delay(180)
          }
        }

        if (rep < safeReps && isActive) {
          delay(320)
        }
      }

      if (isActive) {
        resetPlaybackState()
      }
    }
  }

  private fun releaseOfflineMediaPlayer() {
    val player = offlineMediaPlayer
    offlineMediaPlayer = null
    if (player != null) {
      runCatching {
        if (player.isPlaying) {
          player.stop()
        }
      }
      runCatching {
        player.release()
      }
    }
  }

  fun playDuaVocal(
    dua: DailyDuaItem,
    includeTranslations: Boolean = false,
    repetitions: Int = 1,
    speechRate: Float = _playbackState.value.speechRatePreset,
    includeUrduOverride: Boolean = false,
    preferQuranReciterIfAvailable: Boolean = true,
    allowClearMaleAiVoiceFallback: Boolean = false
  ) {
    if (_playbackState.value.isPlaying && _playbackState.value.activeDuaId == dua.id) {
      stop()
      return
    }

    if (hasOfflineQaidaAudio(dua.id)) {
      playOfflineQaidaAsset(
        id = dua.id,
        title = dua.title,
        arabicText = dua.arabicText,
        transliteration = dua.transliteration,
        repetitions = repetitions,
        speechRate = speechRate
      )
      return
    }

    val quranRef = dua.quranSurahAyah
    val verifiedUrl = dua.humanAudioUrl?.trim().orEmpty()
    if (quranRef == null || verifiedUrl.contains("|")) {
      if (verifiedUrl.isNotEmpty()) {
        playVerifiedHumanAudioUrl(
          dua = dua,
          primaryAudioUrl = verifiedUrl,
          repetitions = repetitions,
          speechRate = speechRate
        )
      } else {
        showRealVoiceComingSoon(duaId = dua.id, duaTitle = dua.title)
      }
      return
    }

    stop()

    val safeReps = repetitions.coerceIn(1, 10)
    val arabicWords = tokenizeArabicWords(dua.arabicText)

    vocalJob = scope.launch {
      _playbackState.value = _playbackState.value.copy(
        isPlaying = true,
        activeDuaId = dua.id,
        activeDuaTitle = dua.title,
        activeStageLabel = "Authentic Quranic Verse Audio (${quranRef.first}:${quranRef.second}) — Exact Verse Only",
        currentRepetition = 1,
        totalRepetitions = safeReps,
        activeArabicWordIndex = 0,
        totalArabicWords = arabicWords.size,
        isComingSoonNotice = false
      )

      for (rep in 1..safeReps) {
        if (!isActive) break

        _playbackState.value = _playbackState.value.copy(
          currentRepetition = rep,
          activeStageLabel = "Playing Exact Quranic Verse (${quranRef.first}:${quranRef.second}) • Rep $rep/$safeReps",
          activeArabicWordIndex = 0,
          totalArabicWords = arabicWords.size,
          isComingSoonNotice = false
        )

        // Play ONLY the exact single verse using authentic human reciter audio (no continuous Surah auto-advance)
        quranAudioPlayerManager.playSingleAyahOnly(
          surah = quranRef.first,
          ayah = quranRef.second,
          surahName = dua.title
        )

        var elapsedMs = 0L
        val maxWaitMs = 45000L
        delay(250)

        while (isActive && elapsedMs < maxWaitMs) {
          val qState = quranAudioPlayerManager.audioState.value
          if (qState.isAudioUnavailable) {
            showRealVoiceComingSoon(duaId = dua.id, duaTitle = dua.title)
            return@launch
          }
          if (!qState.isPlaying && !qState.isBuffering && elapsedMs >= 600L) {
            break
          }
          val totalDur = qState.durationMs.coerceAtLeast(1500)
          val pos = qState.currentPositionMs.coerceIn(0, totalDur)
          val wordIdx = if (arabicWords.isNotEmpty()) {
            ((pos.toFloat() / totalDur.toFloat()) * arabicWords.size).toInt()
              .coerceIn(0, arabicWords.size - 1)
          } else {
            -1
          }
          _playbackState.value = _playbackState.value.copy(
            activeArabicWordIndex = wordIdx,
            totalArabicWords = arabicWords.size,
            activeStageLabel = "Authentic Quranic Verse (${quranRef.first}:${quranRef.second}) • Word ${wordIdx + 1}/${arabicWords.size.coerceAtLeast(1)}"
          )
          delay(120)
          elapsedMs += 120
        }

        quranAudioPlayerManager.stop()
        if (rep < safeReps && isActive) {
          delay(350)
        }
      }

      if (isActive) {
        resetPlaybackState()
      }
    }
  }

  private fun showRealVoiceComingSoon(duaId: String, duaTitle: String) {
    vocalJob?.cancel()
    vocalJob = null
    releaseOfflineMediaPlayer()
    runCatching { quranAudioPlayerManager.stop() }

    _playbackState.value = _playbackState.value.copy(
      isPlaying = false,
      activeDuaId = duaId,
      activeDuaTitle = duaTitle,
      activeStageLabel = REAL_VOICE_COMING_SOON_MESSAGE,
      currentRepetition = 0,
      totalRepetitions = 0,
      activeArabicWordIndex = -1,
      totalArabicWords = 0,
      isComingSoonNotice = true
    )

    runCatching {
      Toast.makeText(context, REAL_VOICE_COMING_SOON_MESSAGE, Toast.LENGTH_SHORT).show()
    }
  }

  private fun tokenizeArabicWords(arabicText: String): List<String> {
    return arabicText
      .replace("۝", " ")
      .trim()
      .split(Regex("\\s+"))
      .filter { it.isNotBlank() }
  }

  fun playFridaySalawatVocal(repetitions: Int = 3) {
    val salawatDua = DailyDuaItem(
      id = "dua_jumuah_salawat_ibrahimiyyah",
      title = "Salawat Ibrahimiyyah (Durood-e-Ibrahimi)",
      category = DuaCategory.SPECIAL_OCCASIONS,
      occasionBadge = "Jumu'ah Salawat",
      arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ",
      transliteration = "Allahumma salli 'ala Muhammadin wa 'ala ali Muhammadin...",
      englishTranslation = "",
      urduTranslation = "",
      authenticReference = "Sahih al-Bukhari 3370",
      virtueAndTiming = "",
      recommendedCount = repetitions,
      humanAudioUrl = "https://raw.githubusercontent.com/sheikhhanif/Hisnul_Muslim_Database/master/audio/53hm.mp3"
    )
    playDuaVocal(
      dua = salawatDua,
      repetitions = repetitions,
      speechRate = _playbackState.value.speechRatePreset
    )
  }

  private fun resetPlaybackState() {
    _playbackState.value = _playbackState.value.copy(
      isPlaying = false,
      activeDuaId = null,
      activeDuaTitle = "",
      activeStageLabel = "",
      currentRepetition = 0,
      totalRepetitions = 0,
      activeArabicWordIndex = -1,
      totalArabicWords = 0,
      activeLetterIndexInWord = -1,
      progressFraction = 0f,
      isComingSoonNotice = false
    )
  }

  fun stop() {
    vocalJob?.cancel()
    vocalJob = null
    releaseOfflineMediaPlayer()
    try {
      quranAudioPlayerManager.stop()
    } catch (_: Exception) {
    }
    resetPlaybackState()
  }

  fun shutdown() {
    stop()
  }
}
