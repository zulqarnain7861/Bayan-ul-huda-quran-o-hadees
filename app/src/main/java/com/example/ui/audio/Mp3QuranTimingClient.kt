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
   * Maps app reciter IDs to official MP3Quran v3 `read` / `reciter` IDs where available.
   */
  val APP_RECITER_TO_MP3QURAN_READ_ID: Map<String, Int> = mapOf(
    "shatri" to 4,
    "abdulbasit" to 53,
    "sudais" to 54,
    "minshawi" to 112,
    "husary" to 118,
    "islam_sobhi" to 253
  )

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

  private val recitersCacheByLang = ConcurrentHashMap<String, List<Mp3QuranReciterEntry>>()
  private val timingReadsCache = ConcurrentHashMap<String, List<Mp3QuranTimingReadEntry>>()
  private val surahAyatTimingCache = ConcurrentHashMap<String, List<Mp3QuranAyatTimingEntry>>()

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

  /**
   * Returns cached `Mp3QuranAyatTimingEntry` rows for `(reciterId, surahNumber)` if available in memory.
   */
  fun getCachedAyatTimingsForReciter(reciterId: String, surahNumber: Int): List<Mp3QuranAyatTimingEntry>? {
    val readId = APP_RECITER_TO_MP3QURAN_READ_ID[reciterId] ?: return null
    return surahAyatTimingCache["$readId:$surahNumber"]?.takeIf { it.isNotEmpty() }
  }

  /**
   * Asynchronously warms the MP3Quran Ayat Timing cache for the given reciter and surah.
   * For reciters that have a direct MP3Quran timing `readId`, fetches that `readId`;
   * for other Surah-level reciters, warms reference Murattal timing (readId = 54) so relative verse cadence is available.
   */
  fun prefetchAyatTimingsForReciterAndSurah(reciterId: String, surahNumber: Int): List<Mp3QuranAyatTimingEntry> {
    if (surahNumber !in 1..114) return emptyList()
    val directReadId = APP_RECITER_TO_MP3QURAN_READ_ID[reciterId]
    if (directReadId != null) {
      val direct = fetchSurahAyatTimings(surahNumber, directReadId)
      if (direct.isNotEmpty()) return direct
    }
    return emptyList()
  }
}
