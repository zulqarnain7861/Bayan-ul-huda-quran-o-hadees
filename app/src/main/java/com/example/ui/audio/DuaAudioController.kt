package com.example.ui.audio

import android.content.Context
import android.widget.Toast
import com.example.data.model.DailyDuaItem
import com.example.data.model.DuaCategory
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
 * Authentic Audio Controller for Daily Duas, Iman, Kalimas, Wudu, Namaz, and Noorani Qaida.
 *
 * STRICT AUDIO POLICY (No AI / Robotic TTS Voice):
 * - AI/TTS synthesized voice is completely removed from Iman, Kalimas, Namaz, Wudu, and Daily Duas.
 * - For Quranic Duas and verified Quranic verses (`quranSurahAyah != null`), plays ONLY the exact single
 *   verse using authentic human reciter audio (`AudioPlayerManager.playSingleAyahOnly`).
 * - For non-Quranic items without authentic human audio, displays:
 *   "Real voice coming soon, In Sha Allah."
 */
class DuaAudioController(
  private val context: Context,
  private val quranAudioPlayerManager: AudioPlayerManager = AudioPlayerManager(context)
) {

  companion object {
    const val REAL_VOICE_COMING_SOON_MESSAGE = "Real voice coming soon, In Sha Allah."

    @Volatile
    private var instance: DuaAudioController? = null

    fun getInstance(context: Context): DuaAudioController {
      return instance ?: synchronized(this) {
        instance ?: DuaAudioController(context.applicationContext).also { instance = it }
      }
    }
  }

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var vocalJob: Job? = null

  private val _playbackState = MutableStateFlow(DuaVocalPlaybackState())
  val playbackState: StateFlow<DuaVocalPlaybackState> = _playbackState.asStateFlow()

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

  /**
   * Plays an item from Noorani Qaida, Iman/Kalimas, Wudu/Namaz, or Daily Duas.
   * Plays authentic human reciter audio ONLY when an exact Quranic verse (`quranSurahAyah`) is linked;
   * otherwise shows "Real voice coming soon, In Sha Allah." (Never uses AI/TTS voice).
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
    speechRateOverride: Float? = null,
    repeatCountOverride: Int? = null,
    preferQuranReciterIfAvailable: Boolean = true
  ) {
    val current = _playbackState.value
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
      recommendedCount = repeatCountOverride ?: current.repeatCountPreset,
      quranSurahAyah = quranSurahAyah
    )
    playDuaVocal(
      dua = syntheticDua,
      includeTranslations = false,
      repetitions = repeatCountOverride ?: current.repeatCountPreset,
      speechRate = speechRateOverride ?: current.speechRatePreset,
      includeUrduOverride = false,
      preferQuranReciterIfAvailable = true
    )
  }

  fun playDuaVocal(
    dua: DailyDuaItem,
    includeTranslations: Boolean = false,
    repetitions: Int = 1,
    speechRate: Float = _playbackState.value.speechRatePreset,
    includeUrduOverride: Boolean = false,
    preferQuranReciterIfAvailable: Boolean = true
  ) {
    if (_playbackState.value.isPlaying && _playbackState.value.activeDuaId == dua.id) {
      stop()
      return
    }

    val quranRef = dua.quranSurahAyah
    if (quranRef == null) {
      showRealVoiceComingSoon(duaId = dua.id, duaTitle = dua.title)
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
        _playbackState.value = _playbackState.value.copy(
          isPlaying = false,
          activeDuaId = null,
          activeDuaTitle = "",
          activeStageLabel = "",
          currentRepetition = 0,
          totalRepetitions = 0,
          activeArabicWordIndex = -1,
          totalArabicWords = 0,
          isComingSoonNotice = false
        )
      }
    }
  }

  private fun showRealVoiceComingSoon(duaId: String, duaTitle: String) {
    vocalJob?.cancel()
    vocalJob = null
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
    showRealVoiceComingSoon(
      duaId = "dua_jumuah_salawat_ibrahimiyyah",
      duaTitle = "Salawat Ibrahimiyyah (Durood-e-Ibrahimi)"
    )
  }

  fun stop() {
    vocalJob?.cancel()
    vocalJob = null
    try {
      quranAudioPlayerManager.stop()
    } catch (_: Exception) {
    }
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

  fun shutdown() {
    stop()
  }
}
