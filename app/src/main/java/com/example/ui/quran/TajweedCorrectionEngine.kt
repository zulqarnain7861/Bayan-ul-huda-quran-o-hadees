package com.example.ui.quran

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class TajweedWordGrade {
  CORRECT,        // Green: fully matching word
  SMALL_MISTAKE,  // Yellow: small mistake (minor acoustic mismatch)
  WRONG_OR_MISSED, // Red: wrong word or explicitly missed in sequence
  PENDING         // Neutral/Gray: not yet reached or evaluated
}

data class TajweedWordStatus(
  val index: Int,
  val canonicalWord: String,
  val grade: TajweedWordGrade = TajweedWordGrade.PENDING,
  val isMatched: Boolean = grade == TajweedWordGrade.CORRECT,
  val isSmallMistake: Boolean = grade == TajweedWordGrade.SMALL_MISTAKE,
  val isMissedOrWrong: Boolean = grade == TajweedWordGrade.WRONG_OR_MISSED,
  val isPending: Boolean = grade == TajweedWordGrade.PENDING
)

data class TajweedEvaluationResult(
  val surahNumber: Int,
  val verseNumber: Int,
  val canonicalWords: List<String>,
  val spokenWords: List<String>,
  val wordStatuses: List<TajweedWordStatus>,
  val matchedCount: Int,
  val smallMistakeCount: Int = 0,
  val wrongOrMissedCount: Int = 0,
  val totalCount: Int,
  val accuracyPercentage: Int,
  val feedbackSummary: String
)

data class TajweedRecordingState(
  val isListening: Boolean = false,
  val recognizedArabic: String = "",
  val activeSurahNumber: Int? = null,
  val activeVerseNumber: Int? = null,
  val evaluationResult: TajweedEvaluationResult? = null,
  val errorMessage: String? = null
)

object TajweedCorrectionEngine {

  private val ARABIC_DIACRITICS_REGEX = Regex("[\u064B-\u065F\u0670\u06D6-\u06ED]")
  private val QURAN_STOP_MARKS_REGEX = Regex("[\u06D6\u06D7\u06D8\u06D9\u06DA\u06DB\u06DC\u06DD\u06DE\u06DF\u06E0\u06E1\u06E2\u06E3\u06E4\u06E5\u06E6\u06E7\u06E8\u06E9\u06EA\u06EB\u06EC\u06ED۝۞۩]")

  /**
   * Normalizes an Arabic word for acoustic matching without modifying the sacred displayed text:
   * - Strips Harakat / Tashkeel diacritics
   * - Normalizes Alif variations (أ, إ, آ, ٱ -> ا)
   * - Normalizes Ta Marbuta (ة -> ه) and Alif Maqsura (ى -> ي)
   */
  fun normalizeArabicForComparison(word: String): String {
    var s = word.trim()
    s = s.replace(ARABIC_DIACRITICS_REGEX, "")
    s = s.replace(QURAN_STOP_MARKS_REGEX, "")
    s = s.replace('أ', 'ا')
      .replace('إ', 'ا')
      .replace('آ', 'ا')
      .replace('ٱ', 'ا')
      .replace('ة', 'ه')
      .replace('ى', 'ي')
      .replace("ـ", "") // Tatweel
    return s.trim()
  }

  /**
   * Evaluates recited Arabic speech against canonical Quran Uthmani text.
   * - Correct words -> GREEN (CORRECT)
   * - Small mistakes -> YELLOW (SMALL_MISTAKE)
   * - Wrong words or missed -> RED (WRONG_OR_MISSED)
   * Zero alteration to original canonical text.
   */
  fun evaluateRecitation(
    canonicalVerseText: String,
    recognizedSpokenArabic: String,
    surah: Int,
    ayah: Int
  ): TajweedEvaluationResult {
    val canonicalRawWords = canonicalVerseText.trim()
      .split(Regex("\\s+"))
      .filter { it.isNotBlank() }

    val spokenRawWords = recognizedSpokenArabic.trim()
      .split(Regex("\\s+"))
      .filter { it.isNotBlank() }

    val normalizedCanonical = canonicalRawWords.map { normalizeArabicForComparison(it) }
    val normalizedSpoken = spokenRawWords.map { normalizeArabicForComparison(it) }

    val wordStatuses = mutableListOf<TajweedWordStatus>()
    var spokenIdx = 0
    var exactMatches = 0
    var smallMistakes = 0
    var wrongOrMissed = 0

    for (cIdx in normalizedCanonical.indices) {
      val cWord = normalizedCanonical[cIdx]
      if (cWord.isEmpty()) continue

      var detectedGrade: TajweedWordGrade? = null
      val lookaheadLimit = (spokenIdx + 4).coerceAtMost(normalizedSpoken.size)

      // Step 1: Check for EXACT word match in lookahead window (GREEN)
      for (sIdx in spokenIdx until lookaheadLimit) {
        val sWord = normalizedSpoken[sIdx]
        if (isExactWordMatch(cWord, sWord)) {
          detectedGrade = TajweedWordGrade.CORRECT
          spokenIdx = sIdx + 1
          break
        }
      }

      // Step 2: If no exact match, check for SMALL MISTAKE in lookahead window (YELLOW)
      if (detectedGrade == null) {
        for (sIdx in spokenIdx until lookaheadLimit) {
          val sWord = normalizedSpoken[sIdx]
          if (isSmallMistakeMatch(cWord, sWord)) {
            detectedGrade = TajweedWordGrade.SMALL_MISTAKE
            spokenIdx = sIdx + 1
            break
          }
        }
      }

      // Step 3: Determine final grade. 
      // If we haven't found the word in the lookahead but there's still more speech, it's MISSED.
      // If we haven't reached this part of the speech yet, it's PENDING.
      val finalGrade = when {
        detectedGrade != null -> detectedGrade
        recognizedSpokenArabic.isNotBlank() -> TajweedWordGrade.WRONG_OR_MISSED
        else -> TajweedWordGrade.PENDING
      }

      when (finalGrade) {
        TajweedWordGrade.CORRECT -> exactMatches++
        TajweedWordGrade.SMALL_MISTAKE -> smallMistakes++
        TajweedWordGrade.WRONG_OR_MISSED -> wrongOrMissed++
        TajweedWordGrade.PENDING -> { /* Neutral */ }
      }

      wordStatuses.add(
        TajweedWordStatus(
          index = cIdx,
          canonicalWord = canonicalRawWords[cIdx],
          grade = finalGrade,
          isMatched = finalGrade == TajweedWordGrade.CORRECT,
          isSmallMistake = finalGrade == TajweedWordGrade.SMALL_MISTAKE,
          isMissedOrWrong = finalGrade == TajweedWordGrade.WRONG_OR_MISSED,
          isPending = finalGrade == TajweedWordGrade.PENDING
        )
      )
    }

    val totalCount = canonicalRawWords.size
    val accuracy = if (totalCount > 0) {
      // Weighted score: exact matches 100%, small mistakes 70%
      ((exactMatches * 100 + smallMistakes * 70) / totalCount).coerceIn(0, 100)
    } else 0

    val feedback = when {
      recognizedSpokenArabic.isBlank() -> "No speech detected. Tap Record Recitation and recite into the microphone."
      wrongOrMissed == 0 && smallMistakes == 0 -> "Excellent Tajweed Recitation! All words correct (Green). Listen to reciter to refine."
      wrongOrMissed == 0 -> "Very Good Recitation! ($exactMatches correct Green, $smallMistakes small mistake Yellow)."
      else -> "Recitation Analysis: Green = Correct, Yellow = Small Mistake, Red = Wrong word or missed."
    }

    return TajweedEvaluationResult(
      surahNumber = surah,
      verseNumber = ayah,
      canonicalWords = canonicalRawWords,
      spokenWords = spokenRawWords,
      wordStatuses = wordStatuses,
      matchedCount = exactMatches,
      smallMistakeCount = smallMistakes,
      wrongOrMissedCount = wrongOrMissed,
      totalCount = totalCount,
      accuracyPercentage = accuracy,
      feedbackSummary = feedback
    )
  }

  private fun isExactWordMatch(canon: String, spoken: String): Boolean {
    if (canon == spoken) return true
    if (canon.removePrefix("ال") == spoken.removePrefix("ال")) return true
    return false
  }

  private fun isSmallMistakeMatch(canon: String, spoken: String): Boolean {
    // Minor prefix variance (و, ف, ب, ل)
    if (canon.removePrefix("و") == spoken.removePrefix("و")) return true
    if (canon.removePrefix("ف") == spoken.removePrefix("ف")) return true
    if (canon.removePrefix("ب") == spoken.removePrefix("ب")) return true
    if (canon.removePrefix("ل") == spoken.removePrefix("ل")) return true

    // Small mistake: Levenshtein distance = 1 for words of length >= 3
    if (kotlin.math.abs(canon.length - spoken.length) <= 1 && canon.length >= 3) {
      val dist = levenshteinDistance(canon, spoken)
      if (dist <= 1) return true
    }
    return false
  }

  private fun levenshteinDistance(s1: String, s2: String): Int {
    val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
    for (i in 0..s1.length) dp[i][0] = i
    for (j in 0..s2.length) dp[0][j] = j
    for (i in 1..s1.length) {
      for (j in 1..s2.length) {
        val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
        dp[i][j] = minOf(
          dp[i - 1][j] + 1,
          dp[i][j - 1] + 1,
          dp[i - 1][j - 1] + cost
        )
      }
    }
    return dp[s1.length][s2.length]
  }
}

class TajweedVoiceRecorder(private val context: Context) {

  private var speechRecognizer: SpeechRecognizer? = null
  private val _state = MutableStateFlow(TajweedRecordingState())
  val state: StateFlow<TajweedRecordingState> = _state.asStateFlow()

  fun isRecognitionAvailable(): Boolean {
    return SpeechRecognizer.isRecognitionAvailable(context)
  }

  fun startListening(
    surah: Int,
    ayah: Int,
    canonicalVerseText: String
  ) {
    stopListening()

    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      _state.value = TajweedRecordingState(
        isListening = false,
        errorMessage = "Speech recognition is not available on this device. You can listen to the authentic human reciters above."
      )
      return
    }

    try {
      val sr = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
      speechRecognizer = sr

      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
        putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar", "ar-EG", "ar-AE"))
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      }

      _state.value = TajweedRecordingState(
        isListening = true,
        activeSurahNumber = surah,
        activeVerseNumber = ayah,
        errorMessage = null
      )

      sr.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
          _state.value = _state.value.copy(isListening = false)
        }

        override fun onError(error: Int) {
          _state.value = _state.value.copy(
            isListening = false,
            errorMessage = when (error) {
              SpeechRecognizer.ERROR_NO_MATCH -> "No Arabic speech recognized. Please recite clearly into the microphone."
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Recitation timed out. Tap Record Recitation to try again."
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required for Tajweed practice."
              else -> "Recitation input complete. Tap Listen to Reciter to hear authentic Tajweed."
            }
          )
        }

        override fun onResults(results: Bundle?) {
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          val spoken = matches?.firstOrNull() ?: ""
          handleRecognizedSpeech(canonicalVerseText, spoken, surah, ayah)
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          val spoken = matches?.firstOrNull() ?: return
          if (spoken.isNotBlank()) {
            val eval = TajweedCorrectionEngine.evaluateRecitation(
              canonicalVerseText = canonicalVerseText,
              recognizedSpokenArabic = spoken,
              surah = surah,
              ayah = ayah
            )
            _state.value = _state.value.copy(
              recognizedArabic = spoken,
              evaluationResult = eval
            )
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
      })

      sr.startListening(intent)
    } catch (e: Exception) {
      _state.value = TajweedRecordingState(
        isListening = false,
        errorMessage = "Could not initialize voice recording: ${e.localizedMessage}"
      )
    }
  }

  private fun handleRecognizedSpeech(
    canonicalVerseText: String,
    spoken: String,
    surah: Int,
    ayah: Int
  ) {
    val eval = TajweedCorrectionEngine.evaluateRecitation(
      canonicalVerseText = canonicalVerseText,
      recognizedSpokenArabic = spoken,
      surah = surah,
      ayah = ayah
    )
    _state.value = _state.value.copy(
      isListening = false,
      recognizedArabic = spoken,
      evaluationResult = eval,
      errorMessage = null
    )
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
      speechRecognizer?.cancel()
      speechRecognizer?.destroy()
    } catch (_: Exception) {
    }
    speechRecognizer = null
    if (_state.value.isListening) {
      _state.value = _state.value.copy(isListening = false)
    }
  }

  fun reset() {
    stopListening()
    _state.value = TajweedRecordingState()
  }
}
