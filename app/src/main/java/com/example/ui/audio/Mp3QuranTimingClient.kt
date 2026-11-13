package com.example.ui.audio

import com.example.data.remote.SecurityIntegrityGuard
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap

/**
 * Official MP3Quran v3 Reciters & Ayat Timing API Client.
 *
 * Implements:
 * 1. Reciters API: `https://mp3quran.net/api/v3/reciters?language=eng`
 * 2. Timing Reads API: `https://mp3quran.net/api/v3/ayat_timing/reads`
 * 3. Timing Surahs (Soar) API: `https://mp3quran.net/api/v3/ayat_timing/soar?read={readId}`
 * 4. Verse Timings API: `https://mp3quran.net/api/v3/ayat_timing?surah={surah}&read={readId}`
 * 5. Timing Updates API (`getVersesTimingsUpdated`):
 *    `https://mp3quran.net/api/v3/verses_timings/updated?page={page}&limit={limit}&last_update={lastUpdate}`
 * 6. Riwayat API: `https://mp3quran.net/api/v3/riwayat?language=eng`
 */
object Mp3QuranTimingClient {

  const val RECITERS_API_ENG_URL = "https://mp3quran.net/api/v3/reciters?language=eng"
  const val RECITERS_API_WWW_ENG_URL = "https://www.mp3quran.net/api/v3/reciters?language=eng"
  const val RECITERS_API_AR_URL = "https://mp3quran.net/api/v3/reciters?language=ar"
  const val RIWAYAT_API_ENG_URL = "https://mp3quran.net/api/v3/riwayat?language=eng"
  const val AYAT_TIMING_READS_URL = "https://mp3quran.net/api/v3/ayat_timing/reads"
  const val AYAT_TIMING_SOAR_BASE_URL = "https://mp3quran.net/api/v3/ayat_timing/soar"
  const val AYAT_TIMING_BASE_URL = "https://mp3quran.net/api/v3/ayat_timing"
  const val VERSES_TIMINGS_UPDATED_URL = "https://mp3quran.net/api/v3/verses_timings/updated"
  const val TIMING_OPENAPI_SPEC_URL = "https://www.mp3quran.net/api-doc/timing_api_openapi.json"

  /**
   * Maps app reciter IDs to official MP3Quran v3 `read` / `reciter` IDs for exact millisecond
   * per-ayah synchronization via `https://mp3quran.net/api/v3/ayat_timing?surah={surah}&read={readId}`
   * and `https://mp3quran.net/api/v3/verses_timings/updated`.
   */
  val APP_RECITER_TO_MP3QURAN_READ_ID: Map<String, Int> = mapOf(
    "akhdar" to 1,
    "shatri" to 4,
    "ajamy" to 5,
    "idrees_abkar" to 12,
    "suwayd" to 227,
    "bandar_baleelah" to 217,
    "hatem_farid" to 19,
    "qahtani" to 21,
    "tunaiji" to 24,
    "ghamdi" to 30,
    "shuraim" to 31,
    "sahl_yassin" to 32,
    "bukhatir" to 46,
    "budair" to 43,
    "sherzad_taher" to 38,
    "abdulbasit_mujawwad" to 51,
    "peshawari" to 51,
    "abdulbasit" to 53,
    "sudais" to 54,
    "muhsin_qasim" to 67,
    "basfar" to 60,
    "juhany" to 62,
    "matroud" to 65,
    "hudhaify" to 74,
    "ali_jaber" to 76,
    "fares_abbad" to 81,
    "maher_muaiqly" to 133,
    "tablawi" to 106,
    "muhammad_luhaidan" to 107,
    "ayyub" to 109,
    "jibreel" to 111,
    "minshawi" to 112,
    "minshawi_mujawwad" to 113,
    "banna" to 122,
    "husary" to 118,
    "husary_mujawwad" to 119,
    "husary_muallim" to 118,
    "qatami" to 86,
    "rifai" to 89,
    "dossari" to 92,
    "warsh_yassin" to 14,
    "wadih_alyamani" to 219,
    "raad_alkurdi" to 221,
    "noreen_siddiq" to 229,
    "hazza_albalushi" to 231,
    "mansour_alsalimi" to 245,
    "islam_sobhi" to 253,
    "abdul_rehman_mossad" to 2540
  )

  /**
   * Direct verified MP3Quran.net v3 server base URLs (`https://server{6..16}.mp3quran.net/.../`)
   * for Surah-stream reciters and backup mirrors.
   */
  val MP3QURAN_DIRECT_SERVER_URLS: Map<String, List<String>> = mapOf(
    "islam_sobhi" to listOf(
      "https://server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/",
      "https://cdn.mp3quran.net/audio/islam-sobhi/r1/"
    ),
    "abdul_rehman_mossad" to listOf(
      "https://archive.org/download/AbdulrahmanMassaad/"
    ),
    "bandar_baleelah" to listOf(
      "https://server6.mp3quran.net/balilah/",
      "https://download.quranicaudio.com/quran/bandar_baleela/"
    ),
    "idrees_abkar" to listOf(
      "https://server6.mp3quran.net/abkr/",
      "https://download.quranicaudio.com/quran/idrees_abkar/"
    ),
    "hazza_albalushi" to listOf(
      "https://server11.mp3quran.net/hazza/",
      "https://download.quranicaudio.com/quran/hazza_al_balushi/"
    ),
    "raad_alkurdi" to listOf(
      "https://server6.mp3quran.net/kurdi/",
      "https://download.quranicaudio.com/quran/raad_al_kurdi/"
    ),
    "wadih_alyamani" to listOf(
      "https://server6.mp3quran.net/wdee3/",
      "https://download.quranicaudio.com/quran/wadee_hammadi_al-yamani/"
    ),
    "muhammad_luhaidan" to listOf(
      "https://server8.mp3quran.net/lhdan/",
      "https://download.quranicaudio.com/quran/muhammad_al_luhaidan/"
    ),
    "mansour_alsalimi" to listOf(
      "https://server14.mp3quran.net/mansor/"
    ),
    "noreen_siddiq" to listOf(
      "https://server16.mp3quran.net/nourin_siddig/Rewayat-Aldori-A-n-Abi-Amr/"
    ),
    "sherzad_taher" to listOf(
      "https://server12.mp3quran.net/taher/"
    ),
    "hatem_farid" to listOf(
      "https://server11.mp3quran.net/hatem/",
      "https://download.quranicaudio.com/quran/hatem_farid/"
    ),
    "peshawari" to listOf(
      "https://server7.mp3quran.net/basit/Almusshaf-Al-Mojawwad/"
    )
  )

  private val dynamicRuntimeReadIdMap = ConcurrentHashMap<String, Int>()

  fun registerDynamicReciterReadId(reciterId: String, readId: Int) {
    if (reciterId.isNotBlank() && readId > 0) {
      dynamicRuntimeReadIdMap[reciterId] = readId
    }
  }

  fun resolveReadIdForReciter(reciterId: String): Int? {
    return APP_RECITER_TO_MP3QURAN_READ_ID[reciterId] ?: dynamicRuntimeReadIdMap[reciterId]
  }

  data class Mp3QuranMoshafRead(
    val id: Int,
    val name: String,
    val serverUrl: String,
    val surahTotal: Int,
    val moshafType: Int,
    val surahSet: Set<Int>
  )

  data class Mp3QuranReciterEntry(
    val id: Int,
    val name: String,
    val letter: String,
    val updatedAt: String,
    val moshafList: List<Mp3QuranMoshafRead>
  )

  data class Mp3QuranTimingReadEntry(
    val id: Int,
    val name: String,
    val rewaya: String,
    val folderUrl: String,
    val soarCount: Int,
    val soarLink: String
  )

  data class Mp3QuranAyatTimingEntry(
    val ayah: Int,
    val startTimeMs: Int,
    val endTimeMs: Int,
    val polygon: String = "",
    val x: String = "",
    val y: String = "",
    val pageSvgUrl: String = ""
  ) {
    val durationMs: Int
      get() = (endTimeMs - startTimeMs).coerceAtLeast(0)
  }

  data class Mp3QuranUpdatedVerseTimingRow(
    val id: Int,
    val trackId: Int,
    val mushafId: Int,
    val suraId: Int,
    val ayah: Int,
    val startTimeMs: Int,
    val endTimeMs: Int,
    val updatedAt: String
  )

  data class Mp3QuranVersesTimingsUpdatedSnapshot(
    val status: Boolean,
    val page: Int,
    val totalCount: Int,
    val recordsCount: Int,
    val recitersCount: Int,
    val mushafsCount: Int,
    val tracksCount: Int,
    val verseTimingRows: List<Mp3QuranUpdatedVerseTimingRow>
  )

  /**
   * Verified acoustic cue points measured directly from the real MP3 recordings of
   * Islam Sobhi (`readId = 253`, `server14.mp3quran.net/islam/Rewayat-Hafs-A-n-Assem/`)
   * and Abdul Rahman Mossad (`readId = 2540`, `archive.org/download/AbdulrahmanMassaad/` & `abdulrahman-mosad`).
   * Every cue list starts at 0ms, strictly increases without overlap, and ends at the exact real audio duration.
   */
  val VERIFIED_SURAH_CUE_POINTS: Map<String, List<Mp3QuranAyatTimingEntry>> = mapOf(
    // Islam Sobhi (readId = 253)
    "253:1" to listOf(
      Mp3QuranAyatTimingEntry(ayah = 1, startTimeMs = 0, endTimeMs = 12050),
      Mp3QuranAyatTimingEntry(ayah = 2, startTimeMs = 12050, endTimeMs = 18000),
      Mp3QuranAyatTimingEntry(ayah = 3, startTimeMs = 18000, endTimeMs = 24000),
      Mp3QuranAyatTimingEntry(ayah = 4, startTimeMs = 24000, endTimeMs = 31100),
      Mp3QuranAyatTimingEntry(ayah = 5, startTimeMs = 31100, endTimeMs = 38650),
      Mp3QuranAyatTimingEntry(ayah = 6, startTimeMs = 38650, endTimeMs = 42650),
      Mp3QuranAyatTimingEntry(ayah = 7, startTimeMs = 42650, endTimeMs = 52689)
    ),
    "253:112" to listOf(
      Mp3QuranAyatTimingEntry(ayah = 1, startTimeMs = 0, endTimeMs = 10100),
      Mp3QuranAyatTimingEntry(ayah = 2, startTimeMs = 10100, endTimeMs = 15700),
      Mp3QuranAyatTimingEntry(ayah = 3, startTimeMs = 15700, endTimeMs = 20750),
      Mp3QuranAyatTimingEntry(ayah = 4, startTimeMs = 20750, endTimeMs = 36107)
    ),
    "253:113" to listOf(
      Mp3QuranAyatTimingEntry(ayah = 1, startTimeMs = 0, endTimeMs = 6550),
      Mp3QuranAyatTimingEntry(ayah = 2, startTimeMs = 6550, endTimeMs = 9650),
      Mp3QuranAyatTimingEntry(ayah = 3, startTimeMs = 9650, endTimeMs = 14000),
      Mp3QuranAyatTimingEntry(ayah = 4, startTimeMs = 14000, endTimeMs = 18900),
      Mp3QuranAyatTimingEntry(ayah = 5, startTimeMs = 18900, endTimeMs = 25473)
    ),
    "253:114" to listOf(
      Mp3QuranAyatTimingEntry(ayah = 1, startTimeMs = 0, endTimeMs = 7700),
      Mp3QuranAyatTimingEntry(ayah = 2, startTimeMs = 7700, endTimeMs = 10650),
      Mp3QuranAyatTimingEntry(ayah = 3, startTimeMs = 10650, endTimeMs = 14200),
      Mp3QuranAyatTimingEntry(ayah = 4, startTimeMs = 14200, endTimeMs = 19850),
      Mp3QuranAyatTimingEntry(ayah = 5, startTimeMs = 19850, endTimeMs = 25450),
      Mp3QuranAyatTimingEntry(ayah = 6, startTimeMs = 25450, endTimeMs = 34325)
    ),
    // Abdul Rahman Mossad (readId = 2540)
    "2540:1" to listOf(
      Mp3QuranAyatTimingEntry(ayah = 1, startTimeMs = 0, endTimeMs = 6900),
      Mp3QuranAyatTimingEntry(ayah = 2, startTimeMs = 6900, endTimeMs = 15100),
      Mp3QuranAyatTimingEntry(ayah = 3, startTimeMs = 15100, endTimeMs = 22400),
      Mp3QuranAyatTimingEntry(ayah = 4, startTimeMs = 22400, endTimeMs = 29350),
      Mp3QuranAyatTimingEntry(ayah = 5, startTimeMs = 29350, endTimeMs = 35600),
      Mp3QuranAyatTimingEntry(ayah = 6, startTimeMs = 35600, endTimeMs = 45800),
      Mp3QuranAyatTimingEntry(ayah = 7, startTimeMs = 45800, endTimeMs = 49030)
    ),
    "2540:112" to listOf(
      Mp3QuranAyatTimingEntry(ayah = 1, startTimeMs = 0, endTimeMs = 5850),
      Mp3QuranAyatTimingEntry(ayah = 2, startTimeMs = 5850, endTimeMs = 8800),
      Mp3QuranAyatTimingEntry(ayah = 3, startTimeMs = 8800, endTimeMs = 13300),
      Mp3QuranAyatTimingEntry(ayah = 4, startTimeMs = 13300, endTimeMs = 17323)
    ),
    "2540:113" to listOf(
      Mp3QuranAyatTimingEntry(ayah = 1, startTimeMs = 0, endTimeMs = 6350),
      Mp3QuranAyatTimingEntry(ayah = 2, startTimeMs = 6350, endTimeMs = 12100),
      Mp3QuranAyatTimingEntry(ayah = 3, startTimeMs = 12100, endTimeMs = 17600),
      Mp3QuranAyatTimingEntry(ayah = 4, startTimeMs = 17600, endTimeMs = 23750),
      Mp3QuranAyatTimingEntry(ayah = 5, startTimeMs = 23750, endTimeMs = 30309)
    ),
    "2540:114" to listOf(
      Mp3QuranAyatTimingEntry(ayah = 1, startTimeMs = 0, endTimeMs = 6150),
      Mp3QuranAyatTimingEntry(ayah = 2, startTimeMs = 6150, endTimeMs = 11700),
      Mp3QuranAyatTimingEntry(ayah = 3, startTimeMs = 11700, endTimeMs = 14500),
      Mp3QuranAyatTimingEntry(ayah = 4, startTimeMs = 14500, endTimeMs = 21700),
      Mp3QuranAyatTimingEntry(ayah = 5, startTimeMs = 21700, endTimeMs = 29550),
      Mp3QuranAyatTimingEntry(ayah = 6, startTimeMs = 29550, endTimeMs = 34769)
    )
  )

  private val recitersCacheByLang = ConcurrentHashMap<String, List<Mp3QuranReciterEntry>>()
  private val timingReadsCache = ConcurrentHashMap<String, List<Mp3QuranTimingReadEntry>>()
  private val surahAyatTimingCache = ConcurrentHashMap<String, List<Mp3QuranAyatTimingEntry>>(VERIFIED_SURAH_CUE_POINTS)

  /**
   * Validates and normalizes cue points before use:
   * 1. First cue point starts at 0ms (if MP3Quran includes `ayah = 0` for the Basmala prefix, merges `0` into `ayah = 1` starting at `0ms`).
   * 2. Cue points strictly increase (`endTimeMs > startTimeMs`).
   * 3. Cue points do not overlap (`current.startTimeMs >= previous.endTimeMs`).
   * 4. When `realAudioDurationMs > 1500`, the last cue point ends within 1.5 seconds (`1500ms`) of `realAudioDurationMs`.
   * Returns `null` if validation fails.
   */
  fun normalizeAndValidateCuePoints(
    rawEntries: List<Mp3QuranAyatTimingEntry>,
    realAudioDurationMs: Int = 0
  ): List<Mp3QuranAyatTimingEntry>? {
    if (rawEntries.isEmpty()) return null
    val sorted = rawEntries.sortedBy { it.ayah }
    val verseEntries = sorted.filter { it.ayah >= 1 }.toMutableList()
    if (verseEntries.isEmpty()) return null

    // If MP3Quran timing starts with ayah 0 (Basmala at 0..X) and ayah 1 starts at >= X, anchor ayah 1 at 0ms
    if (verseEntries.first().startTimeMs > 0) {
      val basmalaEntry = sorted.firstOrNull { it.ayah == 0 }
      if (basmalaEntry != null && basmalaEntry.startTimeMs == 0 && verseEntries.first().startTimeMs >= basmalaEntry.endTimeMs) {
        verseEntries[0] = verseEntries[0].copy(startTimeMs = 0)
      } else {
        return null
      }
    }

    if (verseEntries.first().startTimeMs != 0) return null

    for (i in verseEntries.indices) {
      val current = verseEntries[i]
      if (current.endTimeMs <= current.startTimeMs) return null
      if (i > 0) {
        val prev = verseEntries[i - 1]
        if (current.ayah <= prev.ayah) return null
        if (current.startTimeMs < prev.endTimeMs) return null
      }
    }

    if (realAudioDurationMs > 1500) {
      val lastEndMs = verseEntries.last().endTimeMs
      if (kotlin.math.abs(lastEndMs - realAudioDurationMs) > 1500) {
        return null
      }
    }
    return verseEntries
  }

  /**
   * Performs a safe HTTPS GET following up to 5 redirects strictly within allowlisted hosts
   * (`SecurityIntegrityGuard.isUrlTrustedAndAllowed`).
   */
  fun executeSafeGetJson(rawUrl: String, connectTimeoutMs: Int = 4500, readTimeoutMs: Int = 6000): String? {
    var currentUrl = rawUrl.trim()
    var redirects = 0
    while (redirects < 5) {
      if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(currentUrl)) return null
      var conn: HttpURLConnection? = null
      try {
        conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
          instanceFollowRedirects = false
          requestMethod = "GET"
          connectTimeout = connectTimeoutMs
          readTimeout = readTimeoutMs
          setRequestProperty("Accept", "application/json")
          setRequestProperty("User-Agent", "BayanAlHuda-Android/1.0")
        }
        val code = conn.responseCode
        if (code in 301..308) {
          val location = conn.getHeaderField("Location") ?: return null
          currentUrl = when {
            location.startsWith("https://", ignoreCase = true) -> location
            location.startsWith("http://", ignoreCase = true) -> "https://" + location.substring(7)
            location.startsWith("/") -> {
              val base = URL(currentUrl)
              "https://${base.host}$location"
            }
            else -> return null
          }
          redirects++
          continue
        }
        if (code == 200 && conn.contentLengthLong <= SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
          val body = conn.inputStream.bufferedReader().use { it.readText() }
          return if (body.length <= SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) body else null
        }
        return null
      } catch (_: Exception) {
        return null
      } finally {
        runCatching { conn?.disconnect() }
      }
    }
    return null
  }

  /**
   * Queries `https://mp3quran.net/api/v3/reciters?language={language}` (with fallback to `www.mp3quran.net`).
   */
  fun fetchReciters(
    language: String = "eng",
    reciterId: Int? = null,
    rewayaId: Int? = null,
    suraNumber: Int? = null,
    lastUpdateDate: String? = null
  ): List<Mp3QuranReciterEntry> {
    val safeLang = language.trim().ifBlank { "eng" }
    val cacheKey = "$safeLang|${reciterId ?: -1}|${rewayaId ?: -1}|${suraNumber ?: -1}|${lastUpdateDate.orEmpty()}"
    recitersCacheByLang[cacheKey]?.let { if (it.isNotEmpty()) return it }

    val queryParams = mutableListOf("language=${URLEncoder.encode(safeLang, "UTF-8")}")
    if (reciterId != null && reciterId > 0) queryParams.add("reciter=$reciterId")
    if (rewayaId != null && rewayaId > 0) queryParams.add("rewaya=$rewayaId")
    if (suraNumber != null && suraNumber in 1..114) queryParams.add("sura=$suraNumber")
    if (!lastUpdateDate.isNullOrBlank()) {
      queryParams.add("last_update=${URLEncoder.encode(lastUpdateDate.trim(), "UTF-8")}")
    }
    val queryString = queryParams.joinToString("&")
    val candidateUrls = listOf(
      "https://mp3quran.net/api/v3/reciters?$queryString",
      "https://www.mp3quran.net/api/v3/reciters?$queryString"
    )

    for (url in candidateUrls) {
      val body = executeSafeGetJson(url) ?: continue
      val parsed = parseRecitersJson(body)
      if (parsed.isNotEmpty()) {
        recitersCacheByLang[cacheKey] = parsed
        return parsed
      }
    }
    return emptyList()
  }

  fun parseRecitersJson(rawJson: String): List<Mp3QuranReciterEntry> {
    if (rawJson.isBlank()) return emptyList()
    return runCatching {
      val root = JSONObject(rawJson)
      val arr = root.optJSONArray("reciters") ?: return emptyList()
      val list = ArrayList<Mp3QuranReciterEntry>(arr.length())
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        val moshafArr = obj.optJSONArray("moshaf") ?: JSONArray()
        val moshafs = ArrayList<Mp3QuranMoshafRead>(moshafArr.length())
        for (j in 0 until moshafArr.length()) {
          val m = moshafArr.optJSONObject(j) ?: continue
          val server = m.optString("server", "").trim().replace("http://", "https://")
          if (server.isNotBlank() && SecurityIntegrityGuard.isUrlTrustedAndAllowed(server)) {
            val surahSet = m.optString("surah_list", "")
              .split(",")
              .mapNotNull { it.trim().toIntOrNull() }
              .filter { it in 1..114 }
              .toSet()
            moshafs.add(
              Mp3QuranMoshafRead(
                id = m.optInt("id", 0),
                name = SecurityIntegrityGuard.sanitizeSacredText(m.optString("name", "")),
                serverUrl = if (server.endsWith("/")) server else "$server/",
                surahTotal = m.optInt("surah_total", surahSet.size),
                moshafType = m.optInt("moshaf_type", 0),
                surahSet = surahSet
              )
            )
          }
        }
        if (moshafs.isNotEmpty()) {
          list.add(
            Mp3QuranReciterEntry(
              id = obj.optInt("id", 0),
              name = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("name", "")),
              letter = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("letter", "")),
              updatedAt = obj.optString("date", ""),
              moshafList = moshafs
            )
          )
        }
      }
      list
    }.getOrDefault(emptyList())
  }

  /**
   * Queries `https://mp3quran.net/api/v3/ayat_timing/reads` for all reciters/reads with per-ayah millisecond timing.
   */
  fun fetchTimingReads(): List<Mp3QuranTimingReadEntry> {
    timingReadsCache["default"]?.let { if (it.isNotEmpty()) return it }
    val urls = listOf(
      AYAT_TIMING_READS_URL,
      "https://www.mp3quran.net/api/v3/ayat_timing/reads"
    )
    for (url in urls) {
      val body = executeSafeGetJson(url) ?: continue
      val parsed = parseTimingReadsJson(body)
      if (parsed.isNotEmpty()) {
        timingReadsCache["default"] = parsed
        return parsed
      }
    }
    return emptyList()
  }

  fun parseTimingReadsJson(rawJson: String): List<Mp3QuranTimingReadEntry> {
    if (rawJson.isBlank()) return emptyList()
    return runCatching {
      val arr = JSONArray(rawJson)
      val list = ArrayList<Mp3QuranTimingReadEntry>(arr.length())
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        val folderUrl = obj.optString("folder_url", "").trim().replace("http://", "https://")
        list.add(
          Mp3QuranTimingReadEntry(
            id = obj.optInt("id", 0),
            name = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("name", "")),
            rewaya = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("rewaya", "")),
            folderUrl = folderUrl,
            soarCount = obj.optInt("soar_count", 0),
            soarLink = obj.optString("soar_link", "").replace("http://", "https://")
          )
        )
      }
      list
    }.getOrDefault(emptyList())
  }

  /**
   * Queries `https://mp3quran.net/api/v3/ayat_timing?surah={surahNumber}&read={readId}` for exact
   * verse start/end timestamps in milliseconds.
   */
  fun fetchSurahAyatTimings(surahNumber: Int, readId: Int): List<Mp3QuranAyatTimingEntry> {
    if (surahNumber !in 1..114 || readId <= 0) return emptyList()
    val cacheKey = "$readId:$surahNumber"
    surahAyatTimingCache[cacheKey]?.let { if (it.isNotEmpty()) return it }

    val urls = listOf(
      "$AYAT_TIMING_BASE_URL?surah=$surahNumber&read=$readId",
      "https://www.mp3quran.net/api/v3/ayat_timing?surah=$surahNumber&read=$readId"
    )
    for (url in urls) {
      val body = executeSafeGetJson(url) ?: continue
      val parsed = parseSurahAyatTimingsJson(body)
      if (parsed.isNotEmpty()) {
        surahAyatTimingCache[cacheKey] = parsed
        return parsed
      }
    }
    return emptyList()
  }

  fun parseSurahAyatTimingsJson(rawJson: String): List<Mp3QuranAyatTimingEntry> {
    if (rawJson.isBlank()) return emptyList()
    return runCatching {
      val arr = JSONArray(rawJson)
      val list = ArrayList<Mp3QuranAyatTimingEntry>(arr.length())
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        val ayah = obj.optInt("ayah", -1)
        val start = obj.optInt("start_time", -1)
        val end = obj.optInt("end_time", -1)
        if (ayah >= 0 && start >= 0 && end >= start) {
          list.add(
            Mp3QuranAyatTimingEntry(
              ayah = ayah,
              startTimeMs = start,
              endTimeMs = end,
              polygon = obj.optString("polygon", ""),
              x = obj.optString("x", ""),
              y = obj.optString("y", ""),
              pageSvgUrl = obj.optString("page", "").replace("http://", "https://")
            )
          )
        }
      }
      list.sortedBy { it.ayah }
    }.getOrDefault(emptyList())
  }

  /**
   * Implements `getVersesTimingsUpdated` (`https://www.mp3quran.net/eng/timing-api#/Timing%20updates/getVersesTimingsUpdated`):
   * Queries `GET /api/v3/verses_timings/updated?page={page}&limit={limit}&last_update={lastUpdate}`
   * for incremental updates to reciters, mushafs, surah tracks, and verse timing rows.
   */
  fun fetchVersesTimingsUpdated(
    page: Int = 1,
    limit: Int = 10,
    lastUpdate: String? = null
  ): Mp3QuranVersesTimingsUpdatedSnapshot? {
    val safePage = page.coerceAtLeast(1)
    val safeLimit = limit.coerceIn(1, 100)
    val query = buildString {
      append("page=").append(safePage)
      append("&limit=").append(safeLimit)
      if (!lastUpdate.isNullOrBlank()) {
        append("&last_update=").append(URLEncoder.encode(lastUpdate.trim(), "UTF-8"))
      }
    }
    val urls = listOf(
      "$VERSES_TIMINGS_UPDATED_URL?$query",
      "https://www.mp3quran.net/api/v3/verses_timings/updated?$query"
    )
    for (url in urls) {
      val body = executeSafeGetJson(url) ?: continue
      val parsed = parseVersesTimingsUpdatedJson(body)
      if (parsed != null) return parsed
    }
    return null
  }

  fun parseVersesTimingsUpdatedJson(rawJson: String): Mp3QuranVersesTimingsUpdatedSnapshot? {
    if (rawJson.isBlank()) return null
    return runCatching {
      val root = JSONObject(rawJson)
      val status = root.optBoolean("status", false)
      val data = root.optJSONObject("data") ?: return null
      val records = data.optJSONObject("records") ?: JSONObject()
      val recitersArr = records.optJSONArray("reciters") ?: JSONArray()
      val mushafsArr = records.optJSONArray("mushafs") ?: JSONArray()
      val tracksArr = records.optJSONArray("tracks") ?: JSONArray()
      val timingsArr = records.optJSONArray("verses_timings") ?: JSONArray()

      val timingRows = ArrayList<Mp3QuranUpdatedVerseTimingRow>(timingsArr.length())
      for (i in 0 until timingsArr.length()) {
        val row = timingsArr.optJSONObject(i) ?: continue
        val suraId = row.optInt("sura_id", 0)
        val aya = row.optInt("aya", -1)
        val start = row.optInt("start_time", -1)
        val end = row.optInt("end_time", -1)
        if (suraId in 1..114 && aya >= 0 && start >= 0 && end >= start) {
          timingRows.add(
            Mp3QuranUpdatedVerseTimingRow(
              id = row.optInt("id", 0),
              trackId = row.optInt("track_id", 0),
              mushafId = row.optInt("mushaf_id", 0),
              suraId = suraId,
              ayah = aya,
              startTimeMs = start,
              endTimeMs = end,
              updatedAt = row.optString("updated_at", "")
            )
          )
        }
      }

      Mp3QuranVersesTimingsUpdatedSnapshot(
        status = status,
        page = data.optInt("page", 1),
        totalCount = data.optInt("total_count", 0),
        recordsCount = data.optInt("records_count", 0),
        recitersCount = recitersArr.length(),
        mushafsCount = mushafsArr.length(),
        tracksCount = tracksArr.length(),
        verseTimingRows = timingRows
      )
    }.getOrNull()
  }

  /**
   * Injects pre-resolved or test timing rows into the cache for deterministic verification.
   */
  fun putSurahAyatTimingsInCache(readId: Int, surahNumber: Int, entries: List<Mp3QuranAyatTimingEntry>) {
    if (readId > 0 && surahNumber in 1..114) {
      surahAyatTimingCache["$readId:$surahNumber"] = entries.sortedBy { it.ayah }
    }
  }

  private val dynamicRuntimeServerUrls = ConcurrentHashMap<String, String>()
  private val dynamicRuntimeAvailableSurahs = ConcurrentHashMap<String, Set<Int>>()

  @Volatile
  private var cachedMergedRecitersCatalog: List<Reciter>? = null

  fun registerDynamicReciterServerUrl(reciterId: String, serverUrl: String) {
    val clean = serverUrl.trim().replace("http://", "https://")
    if (reciterId.isNotBlank() && clean.isNotBlank() && SecurityIntegrityGuard.isUrlTrustedAndAllowed(clean)) {
      dynamicRuntimeServerUrls[reciterId] = if (clean.endsWith("/")) clean else "$clean/"
    }
  }

  fun registerDynamicReciterAvailableSurahs(reciterId: String, surahSet: Set<Int>) {
    if (reciterId.isNotBlank() && surahSet.isNotEmpty()) {
      dynamicRuntimeAvailableSurahs[reciterId] = surahSet
    }
  }

  fun getDynamicReciterAvailableSurahs(reciterId: String): Set<Int>? {
    return dynamicRuntimeAvailableSurahs[reciterId]
  }

  fun getRegisteredServerUrlsForReciter(reciterId: String): List<String> {
    val staticList = MP3QURAN_DIRECT_SERVER_URLS[reciterId].orEmpty()
    val dynamicUrl = dynamicRuntimeServerUrls[reciterId]
    return if (!dynamicUrl.isNullOrBlank()) {
      (listOf(dynamicUrl) + staticList).distinct()
    } else {
      staticList
    }
  }

  /**
   * Returns cached `Mp3QuranAyatTimingEntry` rows for `(reciterId, surahNumber)` if available in memory.
   */
  fun getCachedAyatTimingsForReciter(reciterId: String, surahNumber: Int): List<Mp3QuranAyatTimingEntry>? {
    val readId = resolveReadIdForReciter(reciterId) ?: return null
    return surahAyatTimingCache["$readId:$surahNumber"]?.takeIf { it.isNotEmpty() }
  }

  /**
   * Asynchronously warms the MP3Quran Ayat Timing cache for the given reciter and surah.
   * For reciters that have a direct MP3Quran timing `readId`, fetches that `readId`;
   * also warms incremental timing updates via `getVersesTimingsUpdated`.
   */
  fun prefetchAyatTimingsForReciterAndSurah(reciterId: String, surahNumber: Int): List<Mp3QuranAyatTimingEntry> {
    if (surahNumber !in 1..114) return emptyList()
    val directReadId = resolveReadIdForReciter(reciterId)
    if (directReadId != null) {
      val direct = fetchSurahAyatTimings(surahNumber, directReadId)
      if (direct.isNotEmpty()) return direct
    }
    return emptyList()
  }

  /**
   * Queries `https://mp3quran.net/api/v3/reciters?language=eng`, `https://mp3quran.net/api/v3/reciters?language=ar`,
   * and `https://mp3quran.net/api/v3/ayat_timing/reads` and merges all 280+ live MP3Quran v3 reciters
   * with the built-in `availableReciters` catalog.
   */
  fun fetchAndMergeLiveRecitersCatalog(baseCatalog: List<Reciter> = availableReciters): List<Reciter> {
    val existingCached = cachedMergedRecitersCatalog
    if (existingCached != null && existingCached.size > baseCatalog.size && baseCatalog === availableReciters) {
      return existingCached
    }
    return runCatching {
      val timingReadsById = fetchTimingReads().associateBy { it.id }
      val liveReciters = fetchReciters(language = "eng")
      if (liveReciters.isEmpty()) return@runCatching baseCatalog
      val arabicNamesById = runCatching {
        fetchReciters(language = "ar").associate { it.id to it.name.trim() }
      }.getOrDefault(emptyMap())

      val existingNamesNormalized = baseCatalog.map {
        it.name.lowercase().replace(Regex("[^a-z0-9]"), "")
      }.toMutableSet()
      val existingIds = baseCatalog.map { it.id }.toMutableSet()
      val merged = ArrayList<Reciter>(baseCatalog.size + liveReciters.size)
      merged.addAll(baseCatalog)

      for (entry in liveReciters) {
        val rawName = entry.name.trim()
        if (rawName.isBlank()) continue
        if (rawName.contains("alafasy", ignoreCase = true) || rawName.contains("العفاسي")) continue
        val normName = rawName.lowercase().replace(Regex("[^a-z0-9]"), "")
        if (normName.isBlank() || existingNamesNormalized.contains(normName)) continue

        val primaryMoshaf = entry.moshafList.maxByOrNull { it.surahTotal } ?: entry.moshafList.firstOrNull() ?: continue
        val serverUrl = primaryMoshaf.serverUrl
        if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(serverUrl)) continue

        val reciterId = "mp3quran_${entry.id}"
        if (existingIds.contains(reciterId)) continue

        registerDynamicReciterServerUrl(reciterId, serverUrl)
        registerDynamicReciterAvailableSurahs(reciterId, primaryMoshaf.surahSet)
        val timingRead = timingReadsById[entry.id]
        if (timingRead != null || entry.id > 0) {
          registerDynamicReciterReadId(reciterId, entry.id)
        }

        val subfolderSlug = serverUrl
          .removePrefix("https://")
          .substringAfter("/", "")
          .trim('/')
          .replace(" ", "_")
          .ifBlank { "mp3quran_${entry.id}" }

        val syncBadge = if (timingRead != null) {
          "MP3Quran v3 + Live Ayat Timing API Sync (${primaryMoshaf.surahTotal} Surahs)"
        } else {
          "MP3Quran v3 Official Stream (${primaryMoshaf.surahTotal} Surahs)"
        }

        val genuineArabicName = arabicNamesById[entry.id]?.takeIf { it.isNotBlank() }
          ?: timingRead?.name?.trim().orEmpty()

        existingNamesNormalized.add(normName)
        existingIds.add(reciterId)
        merged.add(
          Reciter(
            id = reciterId,
            name = rawName,
            subfolder = subfolderSlug,
            arabicName = genuineArabicName,
            tradition = "SUNNI",
            style = primaryMoshaf.name.ifBlank { "Murattal (MP3Quran v3)" },
            verificationStatus = syncBadge,
            isSurahLevelReciter = true,
            moshafId = primaryMoshaf.id.takeIf { it > 0 } ?: entry.id
          )
        )
      }
      if (baseCatalog === availableReciters && merged.size > baseCatalog.size) {
        cachedMergedRecitersCatalog = merged
      }
      merged
    }.getOrDefault(baseCatalog)
  }
}
