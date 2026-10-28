package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import com.example.data.remote.SecurityIntegrityGuard
import kotlin.math.PI
import kotlin.math.sin
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

data class Reciter(
  val id: String,
  val name: String,
  val subfolder: String,
  val arabicName: String = "",
  val tradition: String = "SUNNI", // "SUNNI" or "SHIA"
  val style: String = "Murattal",
  val verificationStatus: String = "Verified 114-Surah Quran Recitation",
  val isSurahLevelReciter: Boolean = false
)

data class ReciterAvailabilityNote(
  val name: String,
  val arabicName: String,
  val statusBadge: String,
  val explanation: String
)

data class AudioState(
  val isVisible: Boolean = false,
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val isAudioUnavailable: Boolean = false,
  val surahNumber: Int? = null,
  val ayahNumber: Int? = null,
  val surahName: String = "",
  val currentPositionMs: Int = 0,
  val durationMs: Int = 0,
  val statusMessage: String = "",
  val reciter: Reciter = availableReciters.first()
)

val availableReciters = listOf(
  // Verified Sunni Quran Reciters
  Reciter(
    id = "husary",
    name = "Mahmoud Khalil Al-Husary",
    subfolder = "Husary_128kbps",
    arabicName = "محمود خليل الحصري",
    tradition = "SUNNI",
    style = "Murattal (Tajweed Standard)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah / QuranicAudio)"
  ),
  Reciter(
    id = "abdulbasit",
    name = "Abdul Basit Abdul Samad",
    subfolder = "Abdul_Basit_Murattal_192kbps",
    arabicName = "عبد الباسط عبد الصمد",
    tradition = "SUNNI",
    style = "Murattal (Classical 192 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah / QuranicAudio)"
  ),
  Reciter(
    id = "minshawi",
    name = "Muhammad Siddiq Al-Minshawi",
    subfolder = "Minshawy_Murattal_128kbps",
    arabicName = "محمد صديق المنشاوي",
    tradition = "SUNNI",
    style = "Murattal (Classical 128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah / QuranicAudio)"
  ),
  Reciter(
    id = "sudais",
    name = "Abdul Rahman Al-Sudais",
    subfolder = "Abdurrahmaan_As-Sudais_192kbps",
    arabicName = "عبد الرحمن السديس",
    tradition = "SUNNI",
    style = "Murattal (Masjid al-Haram)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah / QuranicAudio)"
  ),
  Reciter(
    id = "shatri",
    name = "Abu Bakr Al-Shatri",
    subfolder = "Abu_Bakr_Ash-Shaatree_128kbps",
    arabicName = "أبو بكر الشاطري",
    tradition = "SUNNI",
    style = "Murattal (128 kbps)",
    verificationStatus = "Verified Complete 114 Surahs (EveryAyah / QuranicAudio)"
  ),
  Reciter(
    id = "islam_sobhi",
    name = "Islam Sobhi",
    subfolder = "islam/Rewayat-Hafs-A-n-Assem",
    arabicName = "إسلام صبحي",
    tradition = "SUNNI",
    style = "Murattal (Melodious & Soulful)",
    verificationStatus = "Verified MP3Quran v3 API & Archive Recitations (Available Surahs Only)",
    isSurahLevelReciter = true
  ),
  Reciter(
    id = "abdul_rehman_mossad",
    name = "Abdul Rehman Mossad",
    subfolder = "abdulrahman-mosad",
    arabicName = "عبد الرحمن مسعد",
    tradition = "SUNNI",
    style = "Murattal (Tranquil & Heart-Touching)",
    verificationStatus = "Verified Archive & MP3Quran Recitations (Available Surahs Only)",
    isSurahLevelReciter = true
  ),
  // Verified Shia Quran Reciters (Quranic Recitation Only — Separate from Duas/Ziyarat)
  Reciter(
    id = "karim_mansouri",
    name = "Karim Mansouri",
    subfolder = "Karim_Mansoori_40kbps",
    arabicName = "كريم منصوري",
    tradition = "SHIA",
    style = "Tartil / Murattal (Verified Shia Quran Reciter)",
    verificationStatus = "Verified Quran Recitation Stream (EveryAyah Archive)"
  ),
  Reciter(
    id = "maytham_tammar",
    name = "Maytham al-Tammar",
    subfolder = "Maytham_Al_Tammar_128kbps",
    arabicName = "ميثم التمار",
    tradition = "SHIA",
    style = "Murattal (Verified Shia Quran Reciter)",
    verificationStatus = "Verified Shia Quran Reciter (Available Surahs Only)"
  )
)

val reciterAvailabilityNotes = listOf(
  ReciterAvailabilityNote(
    name = "Islam Sobhi",
    arabicName = "إسلام صبحي",
    statusBadge = "MP3Quran v3 API Verified",
    explanation = "Streams verified Surah recordings from server14.mp3quran.net (/islam/Rewayat-Hafs-A-n-Assem/) and MP3Quran v3 API. Unrecorded Surahs report 'Audio not available' without silent fallback or AI voice."
  ),
  ReciterAvailabilityNote(
    name = "Abdul Rehman Mossad",
    arabicName = "عبد الرحمن مسعد",
    statusBadge = "Verified Partial Mushaf",
    explanation = "Streams verified Surah recordings from Archive.org (abdulrahman-mosad) & MP3Quran API. Because his public recordings cover selected Surahs rather than all 114 Surahs, unrecorded Surahs honestly display 'Audio not available'."
  ),
  ReciterAvailabilityNote(
    name = "Abdullah Shehab",
    arabicName = "عبد الله شهاب",
    statusBadge = "Unverified Open Stream",
    explanation = "Not indexed in official Quran.com, QuranicAudio, or EveryAyah public repositories; documented honestly rather than fabricating audio URLs."
  )
)

class AudioPlayerManager(private val context: Context) {
  private var mediaPlayer: MediaPlayer? = null
  private var offlineAudioTrack: AudioTrack? = null
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var progressJob: Job? = null
  private var fallbackJob: Job? = null
  private var autoDismissJob: Job? = null

  private val initialSavedReciter: Reciter = runCatching {
    val savedId = com.example.data.prayer.PrayerPreferencesManager.loadConfig(context).sunniQuranReciterId
    availableReciters.find { it.id == savedId } ?: availableReciters.first()
  }.getOrDefault(availableReciters.first())

  private val _audioState = MutableStateFlow(AudioState(reciter = initialSavedReciter))
  val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

  private var preloadJob: Job? = null
  var autoAdvanceFullSurah: Boolean = true

  private data class Mp3QuranMoshafInfo(
    val serverBaseUrl: String,
    val availableSurahs: Set<Int>
  )

  private val resolvedMp3QuranServers = mutableMapOf<String, Mp3QuranMoshafInfo>()
  private val resolvedArchiveSurahFiles = mutableMapOf<String, Map<Int, String>>()
  private val surahAyahWeights = mutableMapOf<Int, List<Int>>()
  @Volatile
  var lastConfirmedAudioUrl: String = ""
    private set

  init {
    // Purge any legacy v1 cached files for surah-level reciters that may have stored playlist-offset tracks
    scope.launch(Dispatchers.IO) {
      runCatching {
        File(context.filesDir, "audio_cache/islam_sobhi").deleteRecursively()
        File(context.filesDir, "audio_cache/abdul_rehman_mossad").deleteRecursively()
      }
    }
  }

  companion object {
    private val SURAH_VERSE_COUNTS = intArrayOf(
      7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
      123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
      112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
      34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
      54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
      60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
      14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
      28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
      29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
      15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
      11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
      5, 4, 5, 6
    )

    // Canonical 1-based Arabic & English Surah names for strict Archive.org track-to-Surah verification
    // (Prevents album playlist track numbers like "06 Al-Ma'idah" or "36 Al-Ghashiyah" from being misread as Surah 6 or Surah 36!)
    private val CANONICAL_SURAH_ARABIC_NAMES = arrayOf(
      "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
      "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
      "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
      "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
      "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق",
      "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة",
      "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
      "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
      "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
      "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات",
      "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر",
      "المسد", "الإخلاص", "الفلق", "الناس"
    )

    private val CANONICAL_SURAH_LATIN_KEYWORDS = arrayOf(
      "fatiha", "baqara", "imran", "nisa", "maidah", "anam", "araf", "anfal", "tawba", "yunus",
      "hud", "yusuf", "rad", "ibrahim", "hijr", "nahl", "isra", "kahf", "maryam", "ta ha",
      "anbiya", "hajj", "muminun", "nur", "furqan", "shuara", "naml", "qasas", "ankabut", "rum",
      "luqman", "sajda", "ahzab", "saba", "fatir", "yasin", "saffat", "sad", "zumar", "ghafir",
      "fussilat", "shura", "zukhruf", "dukhan", "jathiya", "ahqaf", "muhammad", "fath", "hujurat", "qaf",
      "dhariyat", "tur", "najm", "qamar", "rahman", "waqia", "hadid", "mujadila", "hashr", "mumtahina",
      "saff", "jumua", "munafiqun", "taghabun", "talaq", "tahrim", "mulk", "qalam", "haqqa", "maarij",
      "nuh", "jinn", "muzzammil", "muddaththir", "qiyama", "insan", "mursalat", "naba", "naziat", "abasa",
      "takwir", "infitar", "mutaffifin", "inshiqaq", "buruj", "tariq", "ala", "ghashiya", "fajr", "balad",
      "shams", "layl", "duha", "sharh", "tin", "alaq", "qadr", "bayyina", "zalzala", "adiyat",
      "qaria", "takathur", "asr", "humaza", "fil", "quraysh", "maun", "kawthar", "kafirun", "nasr",
      "masad", "ikhlas", "falaq", "nas"
    )

    // Verified canonical Surah-number -> exact Archive.org filename mapping for `abdulrahman-mosad`
    // (In this archive, tracks 01..37 are album playlist indices, NOT Quran surah numbers:
    //  e.g., Surah 5 Al-Ma'idah is Track 06, while Track 36 is Surah 88 Al-Ghashiyah!)
    private val ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES = mapOf(
      1 to "01%20Al-Fatihah%20%D8%A7%D9%84%D9%81%D8%A7%D8%AA%D8%AD%D8%A9.mp3",
      2 to "02%20Al-Baqarah%20%D8%A7%D9%84%D8%A8%D9%82%D8%B1%D8%A9.mp3",
      3 to "03%20Al-%27Imran%20%D8%A2%D9%84%20%D8%B9%D9%85%D8%B1%D8%A7%D9%86.mp3",
      4 to "05%20An-Nisa%27%20%D8%A7%D9%84%D9%86%D8%B3%D8%A7%D8%A1.mp3",
      5 to "06%20Al-Ma%27idah%20%D8%A7%D9%84%D9%85%D8%A7%D8%A6%D8%AF%D8%A9.mp3",
      6 to "07%20Al-An%27am%20%D8%A7%D9%84%D8%A3%D9%86%D8%B9%D8%A7%D9%85.mp3",
      10 to "08%20Yunus%20%D9%8A%D9%88%D9%86%D8%B3.mp3",
      12 to "09%20Yusuf%20%D9%8A%D9%88%D8%B3%D9%81.mp3",
      13 to "10%20Ar-Ra%27d%20%D8%A7%D9%84%D8%B1%D8%B9%D8%AF.mp3",
      14 to "11%20Ibrahim%20%D8%A5%D8%A8%D8%B1%D8%A7%D9%87%D9%8A%D9%85.mp3",
      16 to "12%20An-Nahl%20%D8%A7%D9%84%D9%86%D8%AD%D9%84.mp3",
      17 to "13%20Al-Isra%27%20%D8%A7%D9%84%D8%A5%D8%B3%D8%B1%D8%A7%D8%A1.mp3",
      18 to "14%20Al-Kahf%20%D8%A7%D9%84%D9%83%D9%87%D9%81.mp3",
      19 to "15%20Maryam%20%D9%85%D8%B1%D9%8A%D9%85.mp3",
      20 to "16%20Ta%20Ha%20%D8%B7%D9%87.mp3",
      23 to "17%20Al-Mu%27minun%20%D8%A7%D9%84%D9%85%D8%A4%D9%85%D9%86%D9%88%D9%86.mp3",
      25 to "18%20Al-Furqan%20%D8%A7%D9%84%D9%81%D8%B1%D9%82%D8%A7%D9%86.mp3",
      26 to "19%20Ash-Shu%27ara%27%20%D8%A7%D9%84%D8%B4%D8%B9%D8%B1%D8%A7%D8%A1.mp3",
      27 to "20%20An-Naml%20%D8%A7%D9%84%D9%86%D9%85%D9%84.mp3",
      28 to "21%20Al-Qasas%20%D8%A7%D9%84%D9%82%D8%B5%D8%B5.mp3",
      29 to "22%20Al-%27Ankabut%20%D8%A7%D9%84%D8%B9%D9%86%D9%83%D8%A8%D9%88%D8%AA.mp3",
      30 to "23%20Ar-Rum%20%D8%A7%D9%84%D8%B1%D9%88%D9%85.mp3",
      31 to "24%20Luqman%20%D9%84%D9%82%D9%85%D8%A7%D9%86.mp3",
      32 to "25%20As-Sajdah%20%D8%A7%D9%84%D8%B3%D8%AC%D8%AF%D8%A9.mp3",
      44 to "26%20Ad-Dukhan%20%D8%A7%D9%84%D8%AF%D8%AE%D8%A7%D9%86.mp3",
      50 to "27%20Qaf%20%D9%82.mp3",
      53 to "28%20An-Najm%20%D8%A7%D9%84%D9%86%D8%AC%D9%85.mp3",
      56 to "29%20Al-Waqi%27ah%20%D8%A7%D9%84%D9%88%D8%A7%D9%82%D8%B9%D8%A9.mp3",
      57 to "30%20Al-Hadid%20%D8%A7%D9%84%D8%AD%D8%AF%D9%8A%D8%AF.mp3",
      59 to "31%20Al-Hashr%20%D8%A7%D9%84%D8%AD%D8%B4%D8%B1.mp3",
      70 to "32%20Al-Ma%27arij%20%D8%A7%D9%84%D9%85%D8%B9%D8%A7%D8%B1%D8%AC.mp3",
      73 to "33%20Al-Muzzammil%20%D8%A7%D9%84%D9%85%D8%B2%D9%85%D9%84.mp3",
      79 to "34%20An-Nazi%27at%20%D8%A7%D9%84%D9%86%D8%A7%D8%B2%D8%B9%D8%A7%D8%AA.mp3",
      85 to "35%20Al-Buruj%20%D8%A7%D9%84%D8%A8%D8%B1%D9%88%D8%AC.mp3",
      87 to "37%20Al-A%27la%20%D8%A7%D9%84%D8%A3%D8%B9%D9%84%D9%89.mp3",
      88 to "36%20Al-Ghashiyah%20%D8%A7%D9%84%D8%BA%D8%A7%D8%B4%D9%8A%D8%A9.mp3"
    )

    fun getSurahTotalAyahs(surahNumber: Int): Int {
      return if (surahNumber in 1..114) SURAH_VERSE_COUNTS[surahNumber - 1] else 7
    }

    /**
     * Strictly maps an Archive.org MP3 filename to its canonical Quran Surah number (1..114).
     * - If the filename is purely a 3-digit surah number (e.g. "001.mp3", "005.mp3", "036.mp3"), maps directly to 1..114.
     * - If the filename contains a Surah name (Arabic or Latin), maps ONLY to the Surah whose canonical name matches,
     *   ignoring leading playlist track indices (such as "06 Al-Ma'idah.mp3" -> 5, "36 Al-Ghashiyah.mp3" -> 88).
     */
    fun resolveCanonicalSurahNumberFromFileName(rawFileName: String): Int? {
      val cleanName = rawFileName.substringAfterLast('/').trim()
      if (!cleanName.endsWith(".mp3", ignoreCase = true)) return null
      val baseName = cleanName.dropLast(4).trim()

      // Case 1: Pure 3-digit canonical filename e.g. "001.mp3", "005.mp3", "036.mp3"
      if (Regex("""^\d{3}$""").matches(baseName)) {
        val num = baseName.toIntOrNull()
        return if (num != null && num in 1..114) num else null
      }

      // Case 2: Match against canonical Arabic Surah names (longest first so "آل عمران" matches before "ق" or "ص")
      val normalizedArabic = baseName
        .replace('أ', 'ا')
        .replace('إ', 'ا')
        .replace('آ', 'ا')
      val arabicIdxByLength = CANONICAL_SURAH_ARABIC_NAMES.indices.sortedByDescending {
        CANONICAL_SURAH_ARABIC_NAMES[it].length
      }
      for (idx in arabicIdxByLength) {
        val arName = CANONICAL_SURAH_ARABIC_NAMES[idx]
          .replace('أ', 'ا')
          .replace('إ', 'ا')
          .replace('آ', 'ا')
        if (arName.length >= 2 && normalizedArabic.contains(arName)) {
          return idx + 1
        }
      }

      // Case 3: Match against canonical Latin Surah keywords
      val normalizedLatin = baseName.lowercase()
        .replace("'", "")
        .replace("’", "")
        .replace("`", "")
        .replace("-", " ")
        .replace("_", " ")
      val latinIdxByLength = CANONICAL_SURAH_LATIN_KEYWORDS.indices.sortedByDescending {
        CANONICAL_SURAH_LATIN_KEYWORDS[it].length
      }
      for (idx in latinIdxByLength) {
        val keyword = CANONICAL_SURAH_LATIN_KEYWORDS[idx]
        if (keyword.length >= 3 && normalizedLatin.contains(keyword)) {
          return idx + 1
        }
      }
      return null
    }
  }

  /**
   * Registers the Arabic character lengths of the verses in a Surah so surah-level reciters
   * (`islam_sobhi`, `abdul_rehman_mossad`) derive accurate per-ayah & word-by-word green highlight timing.
   */
  fun setSurahAyahWeights(surahNumber: Int, weights: List<Int>) {
    if (surahNumber in 1..114 && weights.isNotEmpty()) {
      surahAyahWeights[surahNumber] = weights.map { it.coerceAtLeast(10) }
    }
  }

  private fun getWeightsForSurah(surahNumber: Int): List<Int> {
    val totalAyahs = getSurahTotalAyahs(surahNumber)
    val existing = surahAyahWeights[surahNumber]
    if (existing != null && existing.size == totalAyahs) return existing
    return List(totalAyahs) { 60 }
  }

  /**
   * Derives `(activeAyahNumber, withinAyahPositionMs, ayahWindowDurationMs)` from a surah-level audio position.
   * Ensures `islam_sobhi` and `abdul_rehman_mossad` drive the green verse border and green word-by-word highlight
   * in `QuranScreen` identically to ayah-level reciters.
   */
  fun computeSurahAyahTimingForPosition(
    surah: Int,
    positionMs: Int,
    totalSurahDurationMs: Int
  ): Triple<Int, Int, Int> {
    val weights = getWeightsForSurah(surah)
    val totalAyahs = weights.size.coerceAtLeast(1)
    val safeTotalDuration = totalSurahDurationMs.coerceAtLeast(totalAyahs * 2500)
    val safePos = positionMs.coerceIn(0, safeTotalDuration)
    val totalWeight = weights.sum().coerceAtLeast(1).toLong()

    var cumulativeWeight = 0L
    for (idx in weights.indices) {
      val startMs = ((cumulativeWeight * safeTotalDuration) / totalWeight).toInt()
      cumulativeWeight += weights[idx]
      val endMs = if (idx == weights.lastIndex) {
        safeTotalDuration
      } else {
        ((cumulativeWeight * safeTotalDuration) / totalWeight).toInt()
      }
      if (safePos < endMs || idx == weights.lastIndex) {
        val windowDuration = (endMs - startMs).coerceAtLeast(600)
        val withinAyahPos = (safePos - startMs).coerceIn(0, windowDuration)
        return Triple(idx + 1, withinAyahPos, windowDuration)
      }
    }
    return Triple(1, 0, safeTotalDuration.coerceAtLeast(1000))
  }

  fun computeStartOffsetMsForAyah(surah: Int, targetAyah: Int, totalSurahDurationMs: Int): Int {
    if (targetAyah <= 1) return 0
    val weights = getWeightsForSurah(surah)
    val totalAyahs = weights.size.coerceAtLeast(1)
    val clampedAyah = targetAyah.coerceIn(1, totalAyahs)
    val safeTotalDuration = totalSurahDurationMs.coerceAtLeast(totalAyahs * 2500)
    val totalWeight = weights.sum().coerceAtLeast(1).toLong()
    val precedingWeight = weights.take(clampedAyah - 1).sum().toLong()
    return ((precedingWeight * safeTotalDuration) / totalWeight).toInt().coerceIn(0, (safeTotalDuration - 500).coerceAtLeast(0))
  }

  private fun buildEveryAyahUrl(reciter: Reciter, surah: Int, ayah: Int): String {
    val formattedSurah = surah.toString().padStart(3, '0')
    val formattedAyah = ayah.toString().padStart(3, '0')
    return if (reciter.isSurahLevelReciter) {
      when (reciter.id) {
        "islam_sobhi" -> "https://server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/$formattedSurah.mp3"
        "abdul_rehman_mossad" -> {
          val mappedFile = ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES[surah]
          if (mappedFile != null) {
            "https://archive.org/download/abdulrahman-mosad/$mappedFile"
          } else {
            "https://media.way2quran.com/mp3/abdul-rahman-musaad/$formattedSurah.mp3"
          }
        }
        else -> "https://download.quranicaudio.com/quran/${reciter.subfolder}/$formattedSurah.mp3"
      }
    } else {
      "https://everyayah.com/data/${reciter.subfolder}/$formattedSurah$formattedAyah.mp3"
    }
  }

  /**
   * Resolves verified, canonical 1-based Surah audio URLs for the given reciter, surah (1..114), and ayah.
   * Guarantees that pressing Play on Fatiha (1), Baqarah (2), Maidah (5), Yasin (36), or any Surah
   * plays ONLY that exact Surah and never a playlist-offset or adjacent Surah.
   */
  fun getCandidateAudioUrls(reciter: Reciter, surah: Int, ayah: Int = 1): List<String> {
    return resolveCandidateUrlsForReciter(reciter, surah, ayah)
  }

  fun resolvePrimaryAudioUrlForTest(reciter: Reciter, surah: Int, ayah: Int = 1): String {
    val url = resolveCandidateUrlsForReciter(reciter, surah, ayah).firstOrNull()
      ?: buildEveryAyahUrl(reciter, surah, ayah)
    confirmUrlMatchesRequestedSurah(reciter, surah, ayah, url)
    return url
  }

  fun confirmUrlMatchesRequestedSurah(reciter: Reciter, surah: Int, ayah: Int, url: String): Boolean {
    val formattedSurah = surah.toString().padStart(3, '0')
    val formattedAyah = ayah.toString().padStart(3, '0')
    val matches = if (!reciter.isSurahLevelReciter) {
      url.endsWith("/$formattedSurah$formattedAyah.mp3")
    } else {
      when {
        url.endsWith("/$formattedSurah.mp3") -> true
        reciter.id == "abdul_rehman_mossad" && ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES[surah] != null &&
          url.endsWith(ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES[surah]!!) -> true
        else -> {
          val decoded = runCatching { java.net.URLDecoder.decode(url.substringAfterLast('/'), "UTF-8") }
            .getOrDefault(url.substringAfterLast('/'))
          resolveCanonicalSurahNumberFromFileName(decoded) == surah
        }
      }
    }
    Log.i(
      "AudioPlayerManager",
      "FINAL_AUDIO_URL reciter=${reciter.id} surah=$surah ($formattedSurah) ayah=$ayah matchesSurah=$matches url=$url"
    )
    if (matches) {
      lastConfirmedAudioUrl = url
    }
    return matches
  }

  private fun resolveCandidateUrlsForReciter(reciter: Reciter, surah: Int, ayah: Int): List<String> {
    val formattedSurah = surah.toString().padStart(3, '0')
    val formattedAyah = ayah.toString().padStart(3, '0')
    if (!reciter.isSurahLevelReciter) {
      return listOf("https://everyayah.com/data/${reciter.subfolder}/$formattedSurah$formattedAyah.mp3")
    }

    val candidates = mutableListOf<String>()

    // 1. Add primary verified canonical Surah endpoints first (zero playlist-offset ambiguity)
    when (reciter.id) {
      "islam_sobhi" -> {
        candidates.add("https://server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/$formattedSurah.mp3")
        candidates.add("https://archive.org/download/010_20221110/$formattedSurah.mp3")
        candidates.add("https://media.way2quran.com/mp3/islam-sobhi/$formattedSurah.mp3")
      }
      "abdul_rehman_mossad" -> {
        ABDUL_RAHMAN_MOSSAD_ARCHIVE_SURAH_FILES[surah]?.let { exactFile ->
          candidates.add("https://archive.org/download/abdulrahman-mosad/$exactFile")
        }
        candidates.add("https://media.way2quran.com/mp3/abdul-rahman-musaad/$formattedSurah.mp3")
        candidates.add("https://archive.org/download/abdulrahman-mosad/$formattedSurah.mp3")
      }
      else -> {
        candidates.add("https://download.quranicaudio.com/quran/${reciter.subfolder}/$formattedSurah.mp3")
      }
    }

    // 2. Check MP3Quran v3 API cached/live metadata (only when the reciter's moshaf explicitly lists `surah`)
    val mp3Info = fetchMp3QuranInfoIfNeeded(reciter.id)
    if (mp3Info != null) {
      if (mp3Info.availableSurahs.isEmpty() || mp3Info.availableSurahs.contains(surah)) {
        candidates.add("${mp3Info.serverBaseUrl.trimEnd('/')}/$formattedSurah.mp3")
      }
    }

    // 3. Fallback to strict canonical-name-verified Archive.org metadata lookup (never by playlist track index)
    when (reciter.id) {
      "islam_sobhi" -> {
        val archiveFile = fetchArchiveSurahFileIfNeeded("islam-sobhi_202203", surah)
        if (archiveFile != null) {
          candidates.add("https://archive.org/download/islam-sobhi_202203/$archiveFile")
        }
      }
      "abdul_rehman_mossad" -> {
        val archiveFile = fetchArchiveSurahFileIfNeeded("abdulrahman-mosad", surah)
        if (archiveFile != null) {
          candidates.add("https://archive.org/download/abdulrahman-mosad/$archiveFile")
        }
      }
    }

    return candidates
      .distinct()
      .filter { SecurityIntegrityGuard.isUrlTrustedAndAllowed(it) }
      .filter { confirmUrlMatchesRequestedSurah(reciter, surah, ayah, it) }
  }

  private fun fetchMp3QuranInfoIfNeeded(reciterId: String): Mp3QuranMoshafInfo? {
    resolvedMp3QuranServers[reciterId]?.let { return it }
    val searchTerms = when (reciterId) {
      "islam_sobhi" -> listOf("islam sobhi", "إسلام صبحي", "اسلام صبحي")
      // Strictly match Abdul Rahman Mossad / Musaad (never "masoud" which is a different reciter)
      "abdul_rehman_mossad" -> listOf("abdulrahman mosad", "abdul rahman mossad", "abdulrahman musaad", "عبدالرحمن مسعد", "عبد الرحمن مسعد")
      else -> emptyList()
    }
    if (searchTerms.isEmpty()) return null

    val apiUrls = listOf(
      "https://www.mp3quran.net/api/v3/reciters?language=eng",
      "https://www.mp3quran.net/api/v3/reciters?language=ar"
    )
    for (apiUrl in apiUrls) {
      if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(apiUrl)) continue
      try {
        val conn = java.net.URL(apiUrl).openConnection() as java.net.HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.connectTimeout = 4000
        conn.readTimeout = 5000
        if (conn.responseCode == 200 && conn.contentLengthLong <= SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
          val body = conn.inputStream.bufferedReader().use { it.readText() }
          if (body.length > SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) continue
          val root = org.json.JSONObject(body)
          val recitersArr = root.optJSONArray("reciters") ?: continue
          for (i in 0 until recitersArr.length()) {
            val obj = recitersArr.getJSONObject(i)
            val name = obj.optString("name", "").lowercase()
            if (searchTerms.any { name.contains(it) }) {
              val moshafArr = obj.optJSONArray("moshaf")
              if (moshafArr != null && moshafArr.length() > 0) {
                val moshaf = moshafArr.getJSONObject(0)
                val server = moshaf.optString("server", "").replace("http://", "https://")
                val surahListRaw = moshaf.optString("surah_list", "")
                val surahSet = surahListRaw.split(",")
                  .mapNotNull { it.trim().toIntOrNull() }
                  .toSet()
                if (server.isNotBlank() && SecurityIntegrityGuard.isUrlTrustedAndAllowed(server)) {
                  val info = Mp3QuranMoshafInfo(server, surahSet)
                  resolvedMp3QuranServers[reciterId] = info
                  return info
                }
              }
            }
          }
        }
      } catch (_: Exception) {
      }
    }
    return null
  }

  private fun fetchArchiveSurahFileIfNeeded(archiveIdentifier: String, surah: Int): String? {
    val map = resolvedArchiveSurahFiles.getOrPut(archiveIdentifier) {
      val discovered = mutableMapOf<Int, String>()
      val metaUrl = "https://archive.org/metadata/$archiveIdentifier"
      if (SecurityIntegrityGuard.isUrlTrustedAndAllowed(metaUrl)) {
        try {
          val conn = java.net.URL(metaUrl).openConnection() as java.net.HttpURLConnection
          conn.instanceFollowRedirects = false
          conn.connectTimeout = 4000
          conn.readTimeout = 5000
          if (conn.responseCode == 200 && conn.contentLengthLong <= SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            if (body.length > SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) return@getOrPut discovered
            val root = org.json.JSONObject(body)
            val filesArr = root.optJSONArray("files")
            if (filesArr != null) {
              for (i in 0 until filesArr.length()) {
                val fileObj = filesArr.getJSONObject(i)
                val fileName = fileObj.optString("name", "")
                if (fileName.endsWith(".mp3", ignoreCase = true)) {
                  val canonicalSurah = resolveCanonicalSurahNumberFromFileName(fileName)
                  if (canonicalSurah != null && canonicalSurah in 1..114 && !discovered.containsKey(canonicalSurah)) {
                    discovered[canonicalSurah] = java.net.URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
                  }
                }
              }
            }
          }
        } catch (_: Exception) {
        }
      }
      discovered
    }
    return map[surah]
  }

  private fun verifyAudioUrlReachable(url: String): Boolean {
    if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(url)) return false
    return try {
      var currentUrl = url
      var redirects = 0
      while (redirects < 5) {
        if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(currentUrl)) return false
        val conn = java.net.URL(currentUrl).openConnection() as java.net.HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.requestMethod = "GET"
        conn.setRequestProperty("Range", "bytes=0-1023")
        conn.connectTimeout = 3500
        conn.readTimeout = 4000
        val code = conn.responseCode
        if (code in 301..308) {
          val location = conn.getHeaderField("Location") ?: return false
          currentUrl = if (location.startsWith("http")) location.replace("http://", "https://") else return false
          conn.disconnect()
          redirects++
          continue
        }
        val contentType = conn.contentType?.lowercase().orEmpty()
        val isValidAudio = (code == 200 || code == 206) &&
          (contentType.contains("audio") || contentType.contains("mpeg") || contentType.contains("octet-stream"))
        conn.disconnect()
        return isValidAudio
      }
      false
    } catch (_: Exception) {
      false
    }
  }

  private fun preloadUpcomingAyahs(reciter: Reciter, surah: Int, currentAyah: Int, count: Int = 4) {
    if (reciter.isSurahLevelReciter) return
    val totalAyahs = getSurahTotalAyahs(surah)
    if (currentAyah >= totalAyahs) return

    preloadJob?.cancel()
    preloadJob = scope.launch(Dispatchers.IO) {
      val endAyah = (currentAyah + count).coerceAtMost(totalAyahs)
      for (nextAyah in (currentAyah + 1)..endAyah) {
        if (!isActive) break
        val targetCache = getCachedAudioFile(reciter.id, surah, nextAyah)
        if (!targetCache.exists() || targetCache.length() <= 4096L) {
          val nextUrl = buildEveryAyahUrl(reciter, surah, nextAyah)
          if (SecurityIntegrityGuard.isUrlTrustedAndAllowed(nextUrl)) {
            catchAudioForOffline(nextUrl, targetCache)
          }
        }
      }
    }
  }

  private fun handleAyahOrSurahCompletion(surah: Int, ayah: Int, surahName: String) {
    stopProgressTracking()
    val currentReciter = _audioState.value.reciter
    val totalAyahs = getSurahTotalAyahs(surah)

    if (autoAdvanceFullSurah && !currentReciter.isSurahLevelReciter && ayah < totalAyahs) {
      val nextAyah = ayah + 1
      playAyah(surah, nextAyah, surahName)
      return
    }

    _audioState.value = _audioState.value.copy(
      isPlaying = false,
      isBuffering = false,
      isAudioUnavailable = false,
      currentPositionMs = _audioState.value.durationMs,
      statusMessage = "Surah recitation finished • Tap Play to replay or X to close"
    )
    scheduleAutoDismissAfterCompletion()
  }

  /**
   * Resets the green verse & word highlight state when the reciter or surah changes.
   */
  fun resetHighlightState() {
    _audioState.value = _audioState.value.copy(
      ayahNumber = null,
      currentPositionMs = 0,
      durationMs = 0
    )
  }

  fun setReciter(reciter: Reciter) {
    val previousReciterId = _audioState.value.reciter.id
    if (previousReciterId != reciter.id) {
      releasePlayersOnly()
      _audioState.value = _audioState.value.copy(
        reciter = reciter,
        isAudioUnavailable = false,
        isPlaying = false,
        isBuffering = false,
        ayahNumber = null,
        currentPositionMs = 0,
        durationMs = 0
      )
    } else {
      _audioState.value = _audioState.value.copy(reciter = reciter, isAudioUnavailable = false)
    }
  }

  fun playAyah(surah: Int, ayah: Int, surahName: String = "") {
    try {
      releasePlayersOnly()

      val currentReciter = _audioState.value.reciter
      val reciterId = currentReciter.id
      val totalAyahs = getSurahTotalAyahs(surah)
      val safeAyah = ayah.coerceIn(1, totalAyahs)

      // Reset highlight state before starting the requested surah & ayah
      _audioState.value = _audioState.value.copy(
        surahNumber = surah,
        ayahNumber = safeAyah,
        surahName = surahName,
        currentPositionMs = 0,
        durationMs = 0
      )

      // Preload upcoming ayahs in background for gapless full-surah playback
      preloadUpcomingAyahs(currentReciter, surah, safeAyah, count = 4)

      val offlineFile = runCatching {
        com.example.data.download.DownloadManager.getInstance(context)
          .findVerifiedOfflineAudioFile(reciterId, surah, safeAyah)
      }.getOrNull()

      if (offlineFile != null && offlineFile.exists() && offlineFile.length() > 64L) {
        val offlineStatusLabel =
          "${currentReciter.name} • Ayah $safeAyah/$totalAyahs • Offline Audio"
        Log.i("AudioPlayerManager", "FINAL_AUDIO_URL reciter=$reciterId surah=$surah ayah=$safeAyah url=file://${offlineFile.absolutePath}")
        lastConfirmedAudioUrl = "file://${offlineFile.absolutePath}"
        runCatching {
          mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
              AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            )
            setDataSource(offlineFile.absolutePath)
            setOnCompletionListener {
              handleAyahOrSurahCompletion(surah, safeAyah, surahName)
            }
            prepare()
            if (currentReciter.isSurahLevelReciter && safeAyah > 1) {
              val offsetMs = computeStartOffsetMsForAyah(surah, safeAyah, duration)
              if (offsetMs > 0) seekTo(offsetMs)
            }
            start()
          }
          startProgressTracking()
        }
        val initialDur = if (currentReciter.isSurahLevelReciter) {
          computeSurahAyahTimingForPosition(
            surah,
            mediaPlayer?.currentPosition ?: 0,
            mediaPlayer?.duration ?: (totalAyahs * 4000)
          ).third
        } else {
          (mediaPlayer?.duration ?: 8000).coerceAtLeast(1000)
        }
        _audioState.value = _audioState.value.copy(
          isVisible = true,
          isBuffering = false,
          isAudioUnavailable = false,
          isPlaying = true,
          surahNumber = surah,
          ayahNumber = safeAyah,
          surahName = surahName,
          currentPositionMs = 0,
          durationMs = initialDur,
          statusMessage = offlineStatusLabel
        )
        return
      }

      val isSurahMode = currentReciter.isSurahLevelReciter
      val cacheKeyAyah = if (isSurahMode) 0 else safeAyah

      // Play immediately from preloaded/on-demand local cache when available
      val cacheFile = getCachedAudioFile(reciterId, surah, cacheKeyAyah)
      if (cacheFile.exists() && cacheFile.length() > 4096L) {
        Log.i("AudioPlayerManager", "FINAL_AUDIO_URL reciter=$reciterId surah=$surah ayah=$safeAyah url=cache://${cacheFile.absolutePath}")
        lastConfirmedAudioUrl = "cache://${cacheFile.absolutePath}"
        playFromLocalFile(
          file = cacheFile,
          surah = surah,
          ayah = safeAyah,
          surahName = surahName,
          status = if (isSurahMode) {
            "${currentReciter.name} • Surah $surah • Ayah $safeAyah/$totalAyahs (Cached Audio)"
          } else {
            "${currentReciter.name} • Ayah $safeAyah/$totalAyahs (Preloaded)"
          }
        )
        return
      }

      val connectingStatus = if (isSurahMode) {
        "Verifying ${currentReciter.name} audio for Surah $surah..."
      } else {
        "Loading ${currentReciter.name} (Ayah $safeAyah/$totalAyahs)..."
      }

      val primaryCandidate = resolveCandidateUrlsForReciter(currentReciter, surah, safeAyah).firstOrNull()
      if (primaryCandidate != null) {
        confirmUrlMatchesRequestedSurah(currentReciter, surah, safeAyah, primaryCandidate)
      }

      _audioState.value = _audioState.value.copy(
        isVisible = true,
        isPlaying = false,
        isBuffering = true,
        isAudioUnavailable = false,
        surahNumber = surah,
        ayahNumber = safeAyah,
        surahName = surahName,
        currentPositionMs = 0,
        durationMs = 3000,
        statusMessage = connectingStatus
      )

      fallbackJob = scope.launch(Dispatchers.IO) {
        val candidateUrls = resolveCandidateUrlsForReciter(currentReciter, surah, safeAyah)
        if (candidateUrls.isEmpty()) {
          launch(Dispatchers.Main) {
            reportAudioNotAvailable(surah, safeAyah, surahName, "Audio not available for ${currentReciter.name} (Surah $surah)")
          }
          return@launch
        }

        // For Surah-level reciters (Islam Sobhi, Abdul Rahman Mossad), verify reachability across candidate servers first
        val verifiedUrl = if (isSurahMode) {
          candidateUrls.firstOrNull { verifyAudioUrlReachable(it) }
        } else {
          candidateUrls.firstOrNull()
        }

        if (verifiedUrl == null) {
          launch(Dispatchers.Main) {
            reportAudioNotAvailable(
              surah = surah,
              ayah = safeAyah,
              surahName = surahName,
              reason = "Audio not available for Surah $surah by ${currentReciter.name} (No AI fallback used)"
            )
          }
          return@launch
        }

        confirmUrlMatchesRequestedSurah(currentReciter, surah, safeAyah, verifiedUrl)

        launch(Dispatchers.Main) {
          streamVerifiedAudioUrl(
            url = verifiedUrl,
            fallbackUrls = candidateUrls.filter { it != verifiedUrl },
            surah = surah,
            ayah = safeAyah,
            surahName = surahName,
            isSurahMode = isSurahMode,
            totalAyahs = totalAyahs,
            cacheFile = cacheFile
          )
        }
      }
    } catch (e: Exception) {
      Log.w("AudioPlayerManager", "Audio playback error", e)
      reportAudioNotAvailable(surah, ayah, surahName)
    }
  }

  private fun streamVerifiedAudioUrl(
    url: String,
    fallbackUrls: List<String>,
    surah: Int,
    ayah: Int,
    surahName: String,
    isSurahMode: Boolean,
    totalAyahs: Int,
    cacheFile: File
  ) {
    val currentReciter = _audioState.value.reciter
    confirmUrlMatchesRequestedSurah(currentReciter, surah, ayah, url)
    var preparedRef = false
    try {
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(url)
        setOnPreparedListener { mp ->
          preparedRef = true
          val totalDur = mp.duration.coerceAtLeast(1000)
          if (isSurahMode && ayah > 1) {
            val startOffset = computeStartOffsetMsForAyah(surah, ayah, totalDur)
            if (startOffset > 0) {
              runCatching { mp.seekTo(startOffset) }
            }
          }
          val initialTiming = if (isSurahMode) {
            val pos = runCatching { mp.currentPosition }.getOrDefault(0)
            computeSurahAyahTimingForPosition(surah, pos, totalDur)
          } else {
            Triple(ayah, 0, totalDur)
          }
          val playingStatus = if (isSurahMode) {
            "${currentReciter.name} • Surah $surah • Ayah ${initialTiming.first}/$totalAyahs ${if (surahName.isNotBlank()) "($surahName)" else ""}"
          } else {
            "${currentReciter.name} • Ayah $ayah/$totalAyahs (Full Surah Auto-Play)"
          }
          _audioState.value = _audioState.value.copy(
            isVisible = true,
            isBuffering = false,
            isAudioUnavailable = false,
            isPlaying = true,
            surahNumber = surah,
            ayahNumber = initialTiming.first,
            currentPositionMs = initialTiming.second,
            durationMs = initialTiming.third,
            statusMessage = playingStatus
          )
          mp.start()
          startProgressTracking()

          scope.launch(Dispatchers.IO) {
            catchAudioForOffline(url, cacheFile)
          }
          preloadUpcomingAyahs(currentReciter, surah, ayah, count = 4)
        }
        setOnCompletionListener {
          handleAyahOrSurahCompletion(surah, ayah, surahName)
        }
        setOnErrorListener { _, what, extra ->
          Log.w("AudioPlayerManager", "Stream error (what=$what, extra=$extra) on $url")
          if (!preparedRef && fallbackUrls.isNotEmpty()) {
            streamVerifiedAudioUrl(
              url = fallbackUrls.first(),
              fallbackUrls = fallbackUrls.drop(1),
              surah = surah,
              ayah = ayah,
              surahName = surahName,
              isSurahMode = isSurahMode,
              totalAyahs = totalAyahs,
              cacheFile = cacheFile
            )
          } else if (!preparedRef) {
            reportAudioNotAvailable(surah, ayah, surahName)
          }
          true
        }
        prepareAsync()
      }
    } catch (_: Exception) {
      if (fallbackUrls.isNotEmpty()) {
        streamVerifiedAudioUrl(
          url = fallbackUrls.first(),
          fallbackUrls = fallbackUrls.drop(1),
          surah = surah,
          ayah = ayah,
          surahName = surahName,
          isSurahMode = isSurahMode,
          totalAyahs = totalAyahs,
          cacheFile = cacheFile
        )
      } else {
        reportAudioNotAvailable(surah, ayah, surahName)
      }
    }
  }

  /**
   * Honest error reporting when a reciter's audio is genuinely unavailable for a Surah/Ayah
   * or when the device is offline without cached audio.
   * Strictly NEVER uses AI-generated speech or silent fake playback for Quran recitation.
   */
  private fun reportAudioNotAvailable(
    surah: Int,
    ayah: Int,
    surahName: String,
    reason: String? = null
  ) {
    fallbackJob?.cancel()
    stopProgressTracking()
    try {
      mediaPlayer?.release()
    } catch (_: Exception) {
    }
    mediaPlayer = null

    val reciterName = _audioState.value.reciter.name
    _audioState.value = _audioState.value.copy(
      isVisible = true,
      isBuffering = false,
      isPlaying = false,
      isAudioUnavailable = true,
      surahNumber = surah,
      ayahNumber = ayah,
      surahName = surahName,
      currentPositionMs = 0,
      durationMs = 0,
      statusMessage = reason ?: "Audio not available for Surah $surah ($reciterName) • Select another reciter or check connection"
    )
  }

  private fun scheduleAutoDismissAfterCompletion() {
    autoDismissJob?.cancel()
    autoDismissJob = scope.launch {
      delay(4000L)
      if (!_audioState.value.isPlaying && !_audioState.value.isBuffering) {
        stop()
      }
    }
  }

  fun pause() {
    autoDismissJob?.cancel()
    try {
      if (mediaPlayer?.isPlaying == true) {
        mediaPlayer?.pause()
      }
    } catch (_: Exception) {
    }
    try {
      offlineAudioTrack?.pause()
    } catch (_: Exception) {
    }
    _audioState.value = _audioState.value.copy(
      isPlaying = false,
      statusMessage = "Paused • Tap Play to resume"
    )
    stopProgressTracking()
  }

  fun resume() {
    autoDismissJob?.cancel()
    val current = _audioState.value
    val mp = mediaPlayer
    if (mp != null) {
      try {
        mp.start()
        _audioState.value = current.copy(
          isVisible = true,
          isPlaying = true,
          statusMessage = current.reciter.name
        )
        startProgressTracking()
        return
      } catch (_: Exception) {
      }
    }
    // If playback had already finished or used offline fallback, replay the current ayah cleanly
    val sNum = current.surahNumber
    val aNum = current.ayahNumber
    if (sNum != null && aNum != null) {
      playAyah(sNum, aNum, current.surahName)
    }
  }

  fun togglePlayPause() {
    if (_audioState.value.isPlaying) {
      pause()
    } else {
      resume()
    }
  }

  fun seekTo(positionMs: Int) {
    try {
      mediaPlayer?.seekTo(positionMs)
    } catch (_: Exception) {
    }
    _audioState.value = _audioState.value.copy(currentPositionMs = positionMs)
  }

  private fun releasePlayersOnly() {
    fallbackJob?.cancel()
    fallbackJob = null
    autoDismissJob?.cancel()
    autoDismissJob = null
    stopProgressTracking()
    try {
      mediaPlayer?.stop()
    } catch (_: Exception) {
    }
    try {
      mediaPlayer?.release()
    } catch (_: Exception) {
    }
    mediaPlayer = null

    try {
      offlineAudioTrack?.stop()
    } catch (_: Exception) {
    }
    try {
      offlineAudioTrack?.release()
    } catch (_: Exception) {
    }
    offlineAudioTrack = null
  }

  /**
   * Completely stops audio playback and immediately dismisses the AudioMiniPlayerBar popup.
   */
  fun stop() {
    preloadJob?.cancel()
    preloadJob = null
    releasePlayersOnly()
    _audioState.value = AudioState(
      isVisible = false,
      isPlaying = false,
      isBuffering = false,
      surahNumber = null,
      ayahNumber = null,
      surahName = "",
      currentPositionMs = 0,
      durationMs = 0,
      statusMessage = "",
      reciter = _audioState.value.reciter
    )
  }

  /**
   * Updates the active ayah and within-ayah progress from the current audio position.
   * For surah-level reciters (`islam_sobhi`, `abdul_rehman_mossad`), derives the active `ayahNumber`
   * and within-ayah progress from `positionMs` so green text highlighting works across all verses.
   */
  fun applySurahProgressSnapshot(surah: Int, positionMs: Int, totalDurationMs: Int) {
    val state = _audioState.value
    val reciter = state.reciter
    if (reciter.isSurahLevelReciter) {
      val (derivedAyah, withinAyahPos, ayahDur) = computeSurahAyahTimingForPosition(
        surah = surah,
        positionMs = positionMs,
        totalSurahDurationMs = totalDurationMs
      )
      val totalAyahs = getSurahTotalAyahs(surah)
      _audioState.value = state.copy(
        isVisible = true,
        isPlaying = true,
        isBuffering = false,
        surahNumber = surah,
        ayahNumber = derivedAyah,
        currentPositionMs = withinAyahPos,
        durationMs = ayahDur,
        statusMessage = "${reciter.name} • Surah $surah • Ayah $derivedAyah/$totalAyahs"
      )
    } else {
      _audioState.value = state.copy(
        currentPositionMs = positionMs.coerceAtLeast(0),
        durationMs = totalDurationMs.coerceAtLeast(1000)
      )
    }
  }

  private fun startProgressTracking() {
    stopProgressTracking()
    progressJob = scope.launch {
      while (isActive && mediaPlayer?.isPlaying == true) {
        val pos = try {
          mediaPlayer?.currentPosition ?: 0
        } catch (_: Exception) {
          0
        }
        val totalDur = try {
          mediaPlayer?.duration ?: 0
        } catch (_: Exception) {
          0
        }
        val current = _audioState.value
        val sNum = current.surahNumber
        if (current.reciter.isSurahLevelReciter && sNum != null && sNum in 1..114) {
          applySurahProgressSnapshot(sNum, pos, totalDur)
        } else {
          _audioState.value = current.copy(currentPositionMs = pos)
        }
        delay(60)
      }
    }
  }

  private fun stopProgressTracking() {
    progressJob?.cancel()
    progressJob = null
  }

  private fun getCachedAudioFile(reciterId: String, surah: Int, ayah: Int): File {
    val dir = File(context.filesDir, "audio_cache_v2/$reciterId")
    if (!dir.exists()) dir.mkdirs()
    val name = if (surah > 0 && ayah > 0) "${surah}_${ayah}.mp3" else "${surah}.mp3"
    return File(dir, name)
  }

  private fun playFromLocalFile(file: File, surah: Int, ayah: Int, surahName: String, status: String) {
    val currentReciter = _audioState.value.reciter
    val isSurahMode = currentReciter.isSurahLevelReciter
    val played = runCatching {
      var totalDur = 8000
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(file.absolutePath)
        setOnCompletionListener {
          handleAyahOrSurahCompletion(surah, ayah, surahName)
        }
        prepare()
        totalDur = duration.coerceAtLeast(1000)
        if (isSurahMode && ayah > 1) {
          val offsetMs = computeStartOffsetMsForAyah(surah, ayah, totalDur)
          if (offsetMs > 0) seekTo(offsetMs)
        }
        start()
      }
      val initialTiming = if (isSurahMode) {
        val pos = runCatching { mediaPlayer?.currentPosition ?: 0 }.getOrDefault(0)
        computeSurahAyahTimingForPosition(surah, pos, totalDur)
      } else {
        Triple(ayah, 0, totalDur)
      }
      _audioState.value = _audioState.value.copy(
        isVisible = true,
        isBuffering = false,
        isPlaying = true,
        surahNumber = surah,
        ayahNumber = initialTiming.first,
        surahName = surahName,
        currentPositionMs = initialTiming.second,
        durationMs = initialTiming.third,
        statusMessage = status
      )
      startProgressTracking()
      preloadUpcomingAyahs(_audioState.value.reciter, surah, ayah, count = 4)
      true
    }.getOrDefault(false)

    if (!played) {
      reportAudioNotAvailable(surah, ayah, surahName)
    }
  }

  private suspend fun catchAudioForOffline(url: String, targetFile: File) {
    if (targetFile.exists() && targetFile.length() > 4096L) return
    if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(url)) return
    val maxAudioCacheBytes = 64L * 1024L * 1024L
    try {
      val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
      conn.instanceFollowRedirects = false
      conn.connectTimeout = 5000
      conn.readTimeout = 10000
      if (conn.responseCode == 200 && conn.contentLengthLong <= maxAudioCacheBytes) {
        val tmp = File(targetFile.parentFile, "${targetFile.name}.tmp")
        var totalBytes = 0L
        conn.inputStream.use { input ->
          tmp.outputStream().use { output ->
            val buf = ByteArray(8192)
            var read: Int
            while (input.read(buf).also { read = it } != -1) {
              totalBytes += read
              if (totalBytes > maxAudioCacheBytes) {
                tmp.delete()
                return
              }
              output.write(buf, 0, read)
            }
          }
        }
        if (tmp.exists() && tmp.length() > 4096L) {
          tmp.renameTo(targetFile)
        }
      }
    } catch (_: Exception) {}
  }

  /**
   * Priority 11 Addition: Clear all cached audio files to free up storage.
   */
  fun clearAudioCache() {
    scope.launch(Dispatchers.IO) {
      try {
        File(context.filesDir, "audio_cache").deleteRecursively()
        File(context.filesDir, "audio_cache_v2").deleteRecursively()
      } catch (_: Exception) {}
    }
  }
}
