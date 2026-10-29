package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.DailyDuaItem
import java.util.Locale
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
  ARABIC_ONLY("Arabic Vocal Only"),
  ARABIC_AND_ENGLISH("Arabic + English Translation"),
  ARABIC_AND_URDU("Arabic + Urdu Translation")
}

data class DuaAudioPlaybackState(
  val isPlaying: Boolean = false,
  val activeDuaId: String? = null,
  val activeDuaTitle: String = "",
  val activeStageLabel: String = "",
  val repeatCountTarget: Int = 1,
  val currentRepeatIteration: Int = 1,
  val speechRateMultiplier: Float = 1.0f,
  val languageMode: DuaAudioLanguageMode = DuaAudioLanguageMode.ARABIC_ONLY,
  val progressFraction: Float = 0f,
  val activeWordIndex: Int = -1,
  val activeLetterIndexInWord: Int = -1,
  val totalWords: Int = 0,
  val isHumanReciterAudio: Boolean = false
)

/**
 * Pure-vocal Masnoon & Special Occasion Dua Audio Controller.
 *
 * 1. Prioritizes authentic verified human reciter recordings (EveryAyah / MP3Quran) whenever available.
 * 2. When falling back to vocal speech for Hadith Duas or translations, enforces a deep male voice ("man sound", pitch 0.82f).
 * 3. Provides real-time word-by-word and letter-by-letter green highlighting synchronization.
 */
class DuaAudioController(
  private val context: Context,
  private val quranAudioPlayerManager: AudioPlayerManager
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var tts: TextToSpeech? = null
  private var isTtsReady = false
  private var activeProgressJob: Job? = null

  private val _playbackState = MutableStateFlow(DuaAudioPlaybackState())
  val playbackState: StateFlow<DuaAudioPlaybackState> = _playbackState.asStateFlow()

  init {
    initTextToSpeech()
  }

  private fun initTextToSpeech() {
    runCatching {
      tts = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
          isTtsReady = true
          tts?.setAudioAttributes(
            AudioAttributes.Builder()
              .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .build()
          )
          // Deep resonant male voice configuration ("man sound")
          tts?.setPitch(0.82f)
          tts?.setSpeechRate(0.86f * _playbackState.value.speechRateMultiplier)
        }
      }
    }
  }

  private fun selectMaleVoiceForLocale(engine: TextToSpeech, locale: Locale) {
    runCatching {
      engine.language = locale
      engine.setPitch(0.82f)
      engine.setSpeechRate((0.86f * _playbackState.value.speechRateMultiplier).coerceIn(0.45f, 1.4f))
      val voices = engine.voices ?: return@runCatching
      val langPrefix = locale.language.lowercase()
      val matchingVoices = voices.filter { v ->
        v.locale.language.lowercase() == langPrefix && !v.isNetworkConnectionRequired
      }.ifEmpty {
        voices.filter { v -> v.locale.language.lowercase() == langPrefix }
      }
      val preferredMale = matchingVoices.firstOrNull { v ->
        val nameLower = v.name.lowercase()
        nameLower.contains("male") && !nameLower.contains("female")
      } ?: matchingVoices.firstOrNull { v ->
        val nameLower = v.name.lowercase()
        (nameLower.contains("-m-") || nameLower.contains("_m_") || nameLower.endsWith("b") || nameLower.endsWith("d")) &&
          !nameLower.contains("female")
      }
      if (preferredMale != null) {
        engine.voice = preferredMale
      }
    }
  }

  fun setLanguageMode(mode: DuaAudioLanguageMode) {
    _playbackState.value = _playbackState.value.copy(languageMode = mode)
  }

  fun toggleRepeatTarget() {
    val current = _playbackState.value.repeatCountTarget
    val next = when (current) {
      1 -> 3
      3 -> 5
      else -> 1
    }
    _playbackState.value = _playbackState.value.copy(repeatCountTarget = next)
  }

  fun setRepeatTarget(count: Int) {
    _playbackState.value = _playbackState.value.copy(repeatCountTarget = count.coerceIn(1, 10))
  }

  fun toggleSlowPlaybackMode(): Float {
    val nextRate = if (_playbackState.value.speechRateMultiplier < 0.85f) 1.0f else 0.65f
    _playbackState.value = _playbackState.value.copy(speechRateMultiplier = nextRate)
    return nextRate
  }

  fun setSpeechRateMultiplier(multiplier: Float) {
    _playbackState.value = _playbackState.value.copy(speechRateMultiplier = multiplier.coerceIn(0.5f, 1.5f))
  }

  fun playLearningItem(
    id: String,
    title: String,
    arabicText: String,
    transliteration: String,
    englishText: String,
    urduText: String,
    quranSurahAyah: Pair<Int, Int>? = null
  ) {
    val syntheticDua = DailyDuaItem(
      id = id,
      title = title,
      category = com.example.data.model.DuaCategory.HOME_MASJID,
      occasionBadge = "Learn Islam",
      arabicText = arabicText,
      transliteration = transliteration,
      englishTranslation = englishText,
      urduTranslation = urduText,
      authenticReference = "",
      virtueAndTiming = "",
      recommendedCount = _playbackState.value.repeatCountTarget,
      quranSurahAyah = quranSurahAyah
    )
    playDuaVocal(
      dua = syntheticDua,
      preferQuranReciterIfAvailable = (_playbackState.value.speechRateMultiplier >= 0.9f && _playbackState.value.repeatCountTarget == 1)
    )
  }

  fun playDuaVocal(
    dua: DailyDuaItem,
    preferQuranReciterIfAvailable: Boolean = true
  ) {
    activeProgressJob?.cancel()
    runCatching { tts?.stop() }

    val arabicWords = dua.arabicText.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val totalWordsCount = arabicWords.size.coerceAtLeast(1)

    // Always prefer verified human reciter audio when the Dua has a Quranic ayah reference
    val quranRef = dua.quranSurahAyah
    if (quranRef != null && preferQuranReciterIfAvailable) {
      quranAudioPlayerManager.playAyah(
        surah = quranRef.first,
        ayah = quranRef.second,
        surahName = dua.title
      )
      _playbackState.value = _playbackState.value.copy(
        isPlaying = true,
        activeDuaId = dua.id,
        activeDuaTitle = dua.title,
        activeStageLabel = "Verified Human Reciter (${quranAudioPlayerManager.audioState.value.reciter.name})",
        currentRepeatIteration = 1,
        progressFraction = 0.05f,
        activeWordIndex = 0,
        activeLetterIndexInWord = 0,
        totalWords = totalWordsCount,
        isHumanReciterAudio = true
      )
      activeProgressJob = scope.launch {
        val totalMs = (dua.arabicText.length * 55L).coerceIn(4000L, 14000L)
        val tickMs = 45L
        val totalTicks = (totalMs / tickMs).toInt().coerceAtLeast(20)
        for (tick in 1..totalTicks) {
          if (!isActive || !_playbackState.value.isPlaying) break
          delay(tickMs)
          val qState = quranAudioPlayerManager.audioState.value
          val fraction = if (qState.isPlaying && qState.durationMs > 1000 && qState.currentPositionMs > 0) {
            (qState.currentPositionMs.toFloat() / qState.durationMs.toFloat()).coerceIn(0f, 1f)
          } else {
            (tick.toFloat() / totalTicks.toFloat()).coerceIn(0f, 1f)
          }
          val exactWordFloat = fraction * totalWordsCount
          val wordIdx = exactWordFloat.toInt().coerceIn(0, totalWordsCount - 1)
          val activeWord = arabicWords.getOrElse(wordIdx) { "" }
          val wordInternalFraction = (exactWordFloat - wordIdx).coerceIn(0f, 0.999f)
          val letterIdx = (wordInternalFraction * activeWord.length.coerceAtLeast(1)).toInt()
            .coerceIn(0, (activeWord.length - 1).coerceAtLeast(0))

          _playbackState.value = _playbackState.value.copy(
            progressFraction = fraction,
            activeWordIndex = wordIdx,
            activeLetterIndexInWord = letterIdx,
            totalWords = totalWordsCount
          )
        }
        if (_playbackState.value.activeDuaId == dua.id) {
          _playbackState.value = _playbackState.value.copy(
            isPlaying = false,
            progressFraction = 1f,
            activeWordIndex = -1,
            activeLetterIndexInWord = -1,
            activeStageLabel = "Human Recitation Completed"
          )
        }
      }
      return
    }

    val repeatTarget = _playbackState.value.repeatCountTarget
    val langMode = _playbackState.value.languageMode

    _playbackState.value = _playbackState.value.copy(
      isPlaying = true,
      activeDuaId = dua.id,
      activeDuaTitle = dua.title,
      activeStageLabel = "Male Vocal Recitation (${langMode.label})",
      currentRepeatIteration = 1,
      progressFraction = 0f,
      activeWordIndex = 0,
      activeLetterIndexInWord = 0,
      totalWords = totalWordsCount,
      isHumanReciterAudio = false
    )

    activeProgressJob = scope.launch {
      for (iteration in 1..repeatTarget) {
        if (!isActive || !_playbackState.value.isPlaying) break

        _playbackState.value = _playbackState.value.copy(
          currentRepeatIteration = iteration,
          activeStageLabel = "Arabic Male Vocal ($iteration of $repeatTarget • Green Word & Letter Sync)"
        )

        speakSegmentWithWordSync(dua.arabicText, arabicWords, Locale("ar"))
        val approxArabicDurationMs = (dua.arabicText.length * 46L).coerceIn(2800L, 10500L)
        val tickMs = 45L
        val totalTicks = (approxArabicDurationMs / tickMs).toInt().coerceAtLeast(20)
        for (tick in 1..totalTicks) {
          if (!isActive || !_playbackState.value.isPlaying) break
          delay(tickMs)
          val iterFraction = (tick.toFloat() / totalTicks.toFloat()).coerceIn(0f, 1f)
          val baseProgress = (iteration - 1).toFloat() / repeatTarget.toFloat()
          val overallProgress = (baseProgress + iterFraction / repeatTarget.toFloat()).coerceIn(0f, 1f)

          val exactWordFloat = iterFraction * totalWordsCount
          val wordIdx = exactWordFloat.toInt().coerceIn(0, totalWordsCount - 1)
          val activeWord = arabicWords.getOrElse(wordIdx) { "" }
          val wordInternalFraction = (exactWordFloat - wordIdx).coerceIn(0f, 0.999f)
          val letterIdx = (wordInternalFraction * activeWord.length.coerceAtLeast(1)).toInt()
            .coerceIn(0, (activeWord.length - 1).coerceAtLeast(0))

          _playbackState.value = _playbackState.value.copy(
            progressFraction = overallProgress,
            activeWordIndex = wordIdx,
            activeLetterIndexInWord = letterIdx,
            totalWords = totalWordsCount
          )
        }

        if (!isActive || !_playbackState.value.isPlaying) break

        if (langMode == DuaAudioLanguageMode.ARABIC_AND_ENGLISH) {
          _playbackState.value = _playbackState.value.copy(
            activeWordIndex = -1,
            activeLetterIndexInWord = -1,
            activeStageLabel = "English Translation Male Read-Aloud ($iteration of $repeatTarget)"
          )
          speakSegmentIfAvailable(dua.englishTranslation, Locale.US)
          delay((dua.englishTranslation.length * 34L).coerceIn(2000L, 6500L))
        } else if (langMode == DuaAudioLanguageMode.ARABIC_AND_URDU) {
          _playbackState.value = _playbackState.value.copy(
            activeWordIndex = -1,
            activeLetterIndexInWord = -1,
            activeStageLabel = "Urdu Translation Male Read-Aloud ($iteration of $repeatTarget)"
          )
          speakSegmentIfAvailable(dua.urduTranslation, Locale("ur"))
          delay((dua.urduTranslation.length * 36L).coerceIn(2000L, 6500L))
        }
      }

      if (_playbackState.value.isPlaying && _playbackState.value.activeDuaId == dua.id) {
        _playbackState.value = _playbackState.value.copy(
          isPlaying = false,
          progressFraction = 1f,
          activeWordIndex = -1,
          activeLetterIndexInWord = -1,
          activeStageLabel = "Completed Playback • Tap Play to listen again"
        )
      }
    }
  }

  private fun speakSegmentWithWordSync(text: String, words: List<String>, locale: Locale) {
    if (!isTtsReady) return
    runCatching {
      val engine = tts ?: return
      selectMaleVoiceForLocale(engine, locale)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {}
          override fun onDone(utteranceId: String?) {}
          override fun onError(utteranceId: String?) {}
          override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            // Map character range [start, end] to word index and letter index
            var charCursor = 0
            for (idx in words.indices) {
              val wLen = words[idx].length
              if (start <= charCursor + wLen) {
                val letterOffset = (start - charCursor).coerceIn(0, (wLen - 1).coerceAtLeast(0))
                _playbackState.value = _playbackState.value.copy(
                  activeWordIndex = idx,
                  activeLetterIndexInWord = letterOffset
                )
                break
              }
              charCursor += wLen + 1
            }
          }
        })
      }
      val params = Bundle().apply {
        putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
      }
      engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "dua_${System.currentTimeMillis()}")
    }
  }

  private fun speakSegmentIfAvailable(text: String, locale: Locale) {
    if (!isTtsReady) return
    runCatching {
      val engine = tts ?: return
      selectMaleVoiceForLocale(engine, locale)
      val params = Bundle().apply {
        putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
      }
      engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, "dua_${System.currentTimeMillis()}")
    }
  }

  fun stop() {
    activeProgressJob?.cancel()
    activeProgressJob = null
    runCatching { tts?.stop() }
    if (_playbackState.value.isHumanReciterAudio) {
      runCatching { quranAudioPlayerManager.stop() }
    }
    _playbackState.value = _playbackState.value.copy(
      isPlaying = false,
      activeDuaId = null,
      activeDuaTitle = "",
      activeStageLabel = "",
      progressFraction = 0f,
      activeWordIndex = -1,
      activeLetterIndexInWord = -1
    )
  }

  fun shutdown() {
    stop()
    isTtsReady = false
    runCatching { tts?.shutdown() }
    tts = null
  }
}
