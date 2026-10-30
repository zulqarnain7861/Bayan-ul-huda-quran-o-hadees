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
 * STRICT AUDIO POLICY:
 * - Iman, Kalimas, Namaz, Wudu, and Daily Duas NEVER use AI/TTS voice:
 *   - For Quranic Duas and verified Quranic verses (`quranSurahAyah != null`), plays ONLY the exact single
 *     verse using authentic human reciter audio (`AudioPlayerManager.playSingleAyahOnly`).
 *   - For non-Quranic items in Iman, Kalimas, Namaz, Wudu, and Daily Duas, displays:
 *     "Real voice coming soon, In Sha Allah."
 * - Noorani Qaida (`allowClearMaleAiVoiceFallback = true`):
 *   - Plays authentic human reciter audio whenever an exact Quranic verse (`quranSurahAyah != null`) is linked.
 *   - When real human audio is not available for a Qaida letter/harakat/syllable drill, plays a
 *     clear male Arabic teacher voice (TTS with male Arabic voice selection and 0.85f male Ustad pitch).
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

    /**
     * Converts Noorani Qaida single letters, joined letters, standing vowels, and harakat drills
     * into explicit full Arabic phonetic vocalization so the male Arabic teacher voice pronounces
     * every letter name and vowel count accurately.
     */
    fun buildNooraniQaidaVocalizedArabicForMaleTts(
      id: String,
      arabicText: String,
      transliteration: String
    ): String {
      return when (id) {
        // Lesson 1: 29 Single Arabic Letters (Huroof-e-Mufradat)
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
        // Lesson 2: Joined / Compound Letters (Huroof-e-Murakkabat)
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
        // Lesson 4: Harakat
        "l4_01" -> "أَ ، إِ ، أُ"
        // Lesson 7: Standing vowels (2-count elongation)
        "l7_01" -> "بَا ، بِي ، بُو"
        "l7_02" -> "يَا ، يِي ، يُو"
        "l7_03" -> "رَا ، رِي ، رُو"
        "l7_06" -> "بِهِي"
        "l7_07" -> "لَهُو"
        else -> {
          val normalized = arabicText
            .replace("ـٰ", "ا")
            .replace("\u0670", "ا")
            .replace("\u0656", "ي")
            .replace("\u0657", "و")
            .trim()
          val parts = normalized.split(Regex("\\s+")).filter { it.isNotBlank() }
          if (parts.size in 2..4 && (id.startsWith("l4_") || id.startsWith("l5_") || id.startsWith("l8_") || id.startsWith("l10_") || id.startsWith("l12_"))) {
            parts.joinToString(" ، ")
          } else {
            normalized.ifBlank { transliteration }
          }
        }
      }
    }
  }

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var vocalJob: Job? = null
  private var ttsEngine: android.speech.tts.TextToSpeech? = null
  private var isTtsReady: Boolean = false
  private var isArabicTtsSupported: Boolean = false

  private val _playbackState = MutableStateFlow(DuaVocalPlaybackState())
  val playbackState: StateFlow<DuaVocalPlaybackState> = _playbackState.asStateFlow()

  private fun ensureMaleArabicTtsInitialized(onReady: () -> Unit) {
    if (ttsEngine != null && isTtsReady) {
      configureClearMaleArabicVoice(ttsEngine)
      onReady()
      return
    }
    try {
      ttsEngine = android.speech.tts.TextToSpeech(context.applicationContext) { status ->
        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
          isTtsReady = true
          configureClearMaleArabicVoice(ttsEngine)
          onReady()
        } else {
          isTtsReady = false
          onReady()
        }
      }
    } catch (_: Exception) {
      isTtsReady = false
      onReady()
    }
  }

  private fun configureClearMaleArabicVoice(tts: android.speech.tts.TextToSpeech?) {
    if (tts == null) return
    try {
      val arSa = java.util.Locale("ar", "SA")
      val arGen = java.util.Locale("ar")
      val res = tts.setLanguage(arSa)
      isArabicTtsSupported = if (res == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || res == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
        val fallbackRes = tts.setLanguage(arGen)
        fallbackRes != android.speech.tts.TextToSpeech.LANG_MISSING_DATA && fallbackRes != android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED
      } else {
        true
      }

      // Select a clear male Arabic voice if available on the device
      val availableVoices = runCatching { tts.voices }.getOrNull().orEmpty()
      val arabicVoices = availableVoices.filter { it.locale.language.equals("ar", ignoreCase = true) }
      val preferredMaleVoice = arabicVoices.firstOrNull { v ->
        val n = v.name.lowercase(java.util.Locale.US)
        n.contains("male") || n.contains("ar-xa-x-ard") || n.contains("ar-xa-x-are") || n.contains("ar-xa-x-arz") || n.contains("m0") || n.contains("#male")
      } ?: arabicVoices.firstOrNull { !it.isNetworkConnectionRequired } ?: arabicVoices.firstOrNull()

      if (preferredMaleVoice != null) {
        runCatching { tts.voice = preferredMaleVoice }
      }

      // Warm, clear male Ustad pitch (0.85f) and measured Tajweed articulation rate
      tts.setPitch(0.85f)
    } catch (_: Exception) {
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

  /**
   * Plays an item from Noorani Qaida, Iman/Kalimas, Wudu/Namaz, or Daily Duas.
   * - When `allowClearMaleAiVoiceFallback == false` (Iman, Kalimas, Wudu, Namaz, Daily Duas):
   *   Plays authentic human reciter audio ONLY when an exact Quranic verse (`quranSurahAyah`) is linked;
   *   otherwise shows "Real voice coming soon, In Sha Allah." (Never uses AI/TTS voice).
   * - When `allowClearMaleAiVoiceFallback == true` (Noorani Qaida ONLY):
   *   Plays authentic human reciter audio when `quranSurahAyah != null`, and falls back to a clear
   *   male Arabic teacher voice (AI/TTS) for Noorani Qaida letters, harakat, and syllable drills.
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
    preferQuranReciterIfAvailable: Boolean = true,
    allowClearMaleAiVoiceFallback: Boolean = false
  ) {
    val current = _playbackState.value
    val targetReps = (repeatCountOverride ?: current.repeatCountPreset).coerceIn(1, 10)
    val targetRate = (speechRateOverride ?: current.speechRatePreset).coerceIn(0.5f, 1.5f)

    if (quranSurahAyah == null && allowClearMaleAiVoiceFallback) {
      playNooraniQaidaClearMaleVoice(
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
      quranSurahAyah = quranSurahAyah
    )
    playDuaVocal(
      dua = syntheticDua,
      includeTranslations = false,
      repetitions = targetReps,
      speechRate = targetRate,
      includeUrduOverride = false,
      preferQuranReciterIfAvailable = true,
      allowClearMaleAiVoiceFallback = allowClearMaleAiVoiceFallback
    )
  }

  private fun playNooraniQaidaClearMaleVoice(
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
    val vocalizedSpeechText = buildNooraniQaidaVocalizedArabicForMaleTts(
      id = id,
      arabicText = arabicText,
      transliteration = transliteration
    )

    vocalJob = scope.launch {
      _playbackState.value = _playbackState.value.copy(
        isPlaying = true,
        activeDuaId = id,
        activeDuaTitle = title,
        activeStageLabel = "Noorani Qaida • Clear Male Teacher Voice ($transliteration)",
        currentRepetition = 1,
        totalRepetitions = safeReps,
        activeArabicWordIndex = 0,
        totalArabicWords = arabicWords.size.coerceAtLeast(1),
        isComingSoonNotice = false
      )

      ensureMaleArabicTtsInitialized {}
      // Give TTS engine a brief moment if initializing for the first time
      var waitInitMs = 0L
      while (isActive && !isTtsReady && waitInitMs < 900L) {
        delay(100)
        waitInitMs += 100
      }

      for (rep in 1..safeReps) {
        if (!isActive) break

        _playbackState.value = _playbackState.value.copy(
          currentRepetition = rep,
          activeStageLabel = "Noorani Qaida • Clear Male Teacher Voice • Rep $rep/$safeReps",
          activeArabicWordIndex = 0,
          totalArabicWords = arabicWords.size.coerceAtLeast(1),
          isComingSoonNotice = false
        )

        val tts = ttsEngine
        if (tts != null && isTtsReady) {
          configureClearMaleArabicVoice(tts)
          tts.setPitch(0.85f)
          tts.setSpeechRate((0.74f * speechRate).coerceIn(0.45f, 1.15f))
          val textToSpeak = if (isArabicTtsSupported) vocalizedSpeechText else transliteration.ifBlank { vocalizedSpeechText }
          val params = android.os.Bundle().apply {
            putFloat(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
          }
          runCatching {
            tts.speak(
              textToSpeak,
              android.speech.tts.TextToSpeech.QUEUE_FLUSH,
              params,
              "qaida_${id}_$rep"
            )
          }
        }

        val totalWords = arabicWords.size.coerceAtLeast(1)
        val perWordMs = (750L / speechRate.coerceAtLeast(0.6f)).toLong().coerceIn(450L, 1200L)
        for (wIdx in 0 until totalWords) {
          if (!isActive) break
          _playbackState.value = _playbackState.value.copy(
            activeArabicWordIndex = wIdx,
            totalArabicWords = totalWords,
            progressFraction = (wIdx + 1).toFloat() / totalWords.toFloat()
          )
          delay(perWordMs)
        }

        // Wait if TTS is still speaking
        var extraWaitMs = 0L
        while (isActive && runCatching { ttsEngine?.isSpeaking == true }.getOrDefault(false) && extraWaitMs < 4000L) {
          delay(120)
          extraWaitMs += 120
        }

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
          progressFraction = 0f,
          isComingSoonNotice = false
        )
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

    val quranRef = dua.quranSurahAyah
    if (quranRef == null) {
      if (allowClearMaleAiVoiceFallback) {
        playNooraniQaidaClearMaleVoice(
          id = dua.id,
          title = dua.title,
          arabicText = dua.arabicText,
          transliteration = dua.transliteration,
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
            if (allowClearMaleAiVoiceFallback) {
              quranAudioPlayerManager.stop()
              playNooraniQaidaClearMaleVoice(
                id = dua.id,
                title = dua.title,
                arabicText = dua.arabicText,
                transliteration = dua.transliteration,
                repetitions = safeReps,
                speechRate = speechRate
              )
            } else {
              showRealVoiceComingSoon(duaId = dua.id, duaTitle = dua.title)
            }
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
    runCatching { ttsEngine?.stop() }

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
    try {
      ttsEngine?.stop()
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
    try {
      ttsEngine?.shutdown()
    } catch (_: Exception) {
    }
    ttsEngine = null
    isTtsReady = false
  }
}
