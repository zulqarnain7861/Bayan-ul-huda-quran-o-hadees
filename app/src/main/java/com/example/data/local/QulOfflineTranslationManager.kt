package com.example.data.local

import android.content.Context
import android.util.Log
import com.example.data.local.dao.TranslationDao
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.TranslationEntity
import com.example.data.remote.SecurityIntegrityGuard
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Manages offline and downloadable multi-language Quran translations from QUL and fawazahmed0/quran-api.
 *
 * 1. Pre-bundled offline asset (`assets/qul_translations_offline_v1.dat`) provides immediate zero-network
 *    offline translations across all 73 languages (127 editions).
 * 2. Full 6,236-ayah packs can be downloaded and stored permanently in `filesDir/qul_translations/{editionKey}.json.gz`
 *    and indexed in Room's `translations` table.
 * 3. Cleans HTML/footnote tags while preserving footnote content (`[[FN: ...]]` or `[1]`) for the reader UI.
 */
object QulOfflineTranslationManager {

  private const val BUNDLED_ASSET_NAME = "qul_translations_offline_v1.dat"

  // Canonical verse counts for all 114 Surahs (6,236 total)
  val SURAH_VERSE_COUNTS = intArrayOf(
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

  private val CANONICAL_VERSE_KEYS: List<String> by lazy {
    val list = ArrayList<String>(6236)
    for (sIdx in SURAH_VERSE_COUNTS.indices) {
      val sNum = sIdx + 1
      val vCount = SURAH_VERSE_COUNTS[sIdx]
      for (vNum in 1..vCount) {
        list.add("$sNum:$vNum")
      }
    }
    list
  }

  @Volatile
  private var bundledAssetCache: Map<String, Map<String, String>>? = null

  private val fullEditionMemoryCache = ConcurrentHashMap<String, Map<String, String>>()

  private fun ensureBundledAssetLoaded(context: Context): Map<String, Map<String, String>> {
    bundledAssetCache?.let { return it }
    synchronized(this) {
      bundledAssetCache?.let { return it }
      val loaded = HashMap<String, Map<String, String>>()
      try {
        val rawBytes = context.assets.open(BUNDLED_ASSET_NAME).use { it.readBytes() }
        val jsonText = if (rawBytes.size >= 2 &&
          rawBytes[0] == 0x1f.toByte() &&
          rawBytes[1] == 0x8b.toByte()
        ) {
          GZIPInputStream(BufferedInputStream(rawBytes.inputStream()))
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        } else {
          String(rawBytes, Charsets.UTF_8)
        }
        val rootObj = JSONObject(jsonText)
        val editionKeys = rootObj.keys()
        while (editionKeys.hasNext()) {
          val edKey = editionKeys.next()
          val vObj = rootObj.optJSONObject(edKey) ?: continue
          val vMap = HashMap<String, String>(vObj.length())
          val vKeys = vObj.keys()
          while (vKeys.hasNext()) {
            val vk = vKeys.next()
            val txt = vObj.optString(vk, "")
            if (txt.isNotBlank()) {
              vMap[vk] = txt
            }
          }
          loaded[edKey] = vMap
        }
      } catch (e: Exception) {
        Log.w("QulOfflineManager", "Could not read bundled QUL asset: ${e.message}")
      }
      bundledAssetCache = loaded
      return loaded
    }
  }

  private fun getFullPackFile(context: Context, editionKey: String): File {
    val dir = File(context.filesDir, "qul_translations")
    if (!dir.exists()) dir.mkdirs()
    val safeName = editionKey.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    return File(dir, "$safeName.json.gz")
  }

  fun isFull6236PackDownloaded(context: Context, editionKey: String): Boolean {
    val file = getFullPackFile(context, editionKey)
    return file.exists() && file.length() > 5_000L
  }

  private fun loadFullPackFromDisk(context: Context, editionKey: String): Map<String, String>? {
    fullEditionMemoryCache[editionKey]?.let { return it }
    val file = getFullPackFile(context, editionKey)
    if (!file.exists() || file.length() < 100L) return null
    return try {
      val jsonText = GZIPInputStream(BufferedInputStream(FileInputStream(file)))
        .bufferedReader(Charsets.UTF_8)
        .use { it.readText() }
      val obj = JSONObject(jsonText)
      val map = HashMap<String, String>(obj.length())
      val keys = obj.keys()
      while (keys.hasNext()) {
        val k = keys.next()
        val v = obj.optString(k, "")
        if (v.isNotBlank()) {
          map[k] = v
        }
      }
      if (map.isNotEmpty()) {
        fullEditionMemoryCache[editionKey] = map
      }
      map
    } catch (e: Exception) {
      null
    }
  }

  private fun saveFullPackToDisk(context: Context, editionKey: String, verses: Map<String, String>) {
    try {
      val file = getFullPackFile(context, editionKey)
      val obj = JSONObject()
      for ((k, v) in verses) {
        obj.put(k, v)
      }
      val bytes = obj.toString().toByteArray(Charsets.UTF_8)
      GZIPOutputStream(FileOutputStream(file)).use { it.write(bytes) }
      fullEditionMemoryCache[editionKey] = verses
    } catch (e: Exception) {
      Log.w("QulOfflineManager", "Failed saving full pack for $editionKey: ${e.message}")
    }
  }

  /**
   * Cleans QUL HTML & footnote tags while preserving footnote numbers/content cleanly.
   */
  fun cleanQulTranslationText(raw: String): String {
    if (raw.isBlank()) return ""
    var cleaned = SecurityIntegrityGuard.stripUnicodeCloakingAndBidiTrojan(raw)
    cleaned = Regex("""<sup[^>]*>(.*?)</sup>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
      .replace(cleaned) { match ->
        val inner = match.groupValues[1].replace(Regex("<[^>]+>"), "").trim()
        if (inner.isNotEmpty()) " [$inner]" else ""
      }
    cleaned = cleaned.replace(Regex("<[^>]+>"), " ")
      .replace("&quot;", "\"")
      .replace("&amp;", "&")
      .replace("&#39;", "'")
      .replace("&apos;", "'")
      .replace("&lt;", "<")
      .replace("&gt;", ">")
      .replace("&nbsp;", " ")
      .replace(Regex("""\s+"""), " ")
      .trim()
    return cleaned
  }

  /**
   * Splits a translation string into main verse text and optional footnote text if present.
   */
  fun splitVerseAndFootnotes(rawText: String): Pair<String, String?> {
    val fnMarkerIdx = rawText.indexOf("[[FN:")
    if (fnMarkerIdx >= 0) {
      val mainText = rawText.substring(0, fnMarkerIdx).trim()
      val fnContent = rawText.substring(fnMarkerIdx + 5).removeSuffix("]]").trim()
      return mainText to fnContent.takeIf { it.isNotEmpty() }
    }
    return rawText to null
  }

  /**
   * Looks up a single verse translation from:
   * 1. Full downloaded pack on disk (`filesDir/qul_translations/{editionKey}.json.gz`)
   * 2. Pre-bundled offline asset (`assets/qul_translations_offline_v1.dat`)
   */
  fun getOfflineVerseText(
    context: Context,
    editionKey: String,
    surahNumber: Int,
    verseNumber: Int
  ): String? {
    val refKey = "$surahNumber:$verseNumber"
    loadFullPackFromDisk(context, editionKey)?.get(refKey)?.let {
      if (it.isNotBlank()) return it
    }
    val bundled = ensureBundledAssetLoaded(context)
    return bundled[editionKey]?.get(refKey)?.takeIf { it.isNotBlank() }
  }

  /**
   * Ensures that all verses of `surahNumber` for `editionKey` are loaded into Room's `translations` table.
   * Uses local full pack -> bundled offline asset -> live QUL / Fawaz / QuranEnc network fetch when online.
   */
  suspend fun ensureSurahLoadedForQulEdition(
    context: Context,
    editionKey: String,
    surahNumber: Int,
    ayahs: List<AyahEntity>,
    translationDao: TranslationDao
  ) = withContext(Dispatchers.IO) {
    val spec = QulTranslationCatalog.findEditionByKey(editionKey) ?: return@withContext

    // Check if this Surah is already cached in Room for this editionKey
    val firstRef = "$surahNumber:1"
    val existingFirst = translationDao.getTranslationsForReferenceSnapshot("QURAN", firstRef)
    if (existingFirst.any { it.editionKey == editionKey && it.text.isNotBlank() }) {
      return@withContext
    }

    val fullMap = loadFullPackFromDisk(context, editionKey)
    val bundledMap = ensureBundledAssetLoaded(context)[editionKey].orEmpty()

    val resolvedVerses = HashMap<Int, String>()
    for (a in ayahs) {
      val vk = "${a.surahNumber}:${a.verseNumber}"
      val txt = fullMap?.get(vk) ?: bundledMap[vk]
      if (!txt.isNullOrBlank()) {
        resolvedVerses[a.verseNumber] = txt
      }
    }

    // If any verses of this Surah are not in the starter bundle yet, fetch full edition or surah live
    if (resolvedVerses.size < ayahs.size) {
      val downloadedMap = fetchFullEditionFromNetwork(spec)
      if (downloadedMap.isNotEmpty()) {
        saveFullPackToDisk(context, editionKey, downloadedMap)
        for (a in ayahs) {
          val vk = "${a.surahNumber}:${a.verseNumber}"
          downloadedMap[vk]?.let { resolvedVerses[a.verseNumber] = it }
        }
      } else if (spec.quranEncKey != null) {
        val surahMap = fetchSurahFromQuranEnc(spec.quranEncKey, surahNumber)
        for ((vNum, txt) in surahMap) {
          resolvedVerses[vNum] = txt
        }
      }
    }

    if (resolvedVerses.isNotEmpty()) {
      val batch = ArrayList<TranslationEntity>(resolvedVerses.size)
      for (a in ayahs) {
        val translatedText = resolvedVerses[a.verseNumber] ?: continue
        batch.add(
          TranslationEntity(
            sourceType = "QURAN",
            referenceKey = "${a.surahNumber}:${a.verseNumber}",
            surahOrBookNumber = a.surahNumber,
            verseOrHadithNumber = a.verseNumber,
            editionKey = spec.editionKey,
            editionTitle = "${spec.languageNameNative} (${spec.languageNameEn}) • ${spec.translatorName}",
            languageCode = spec.languageCode,
            text = translatedText
          )
        )
      }
      if (batch.isNotEmpty()) {
        translationDao.insertTranslations(batch)
      }
    }
  }

  /**
   * Downloads and caches the full 6,236-verse translation pack for `editionKey` for 100% offline use
   * across all 114 Surahs.
   */
  suspend fun downloadFullEditionForOffline(
    context: Context,
    editionKey: String,
    translationDao: TranslationDao? = null
  ): Boolean = withContext(Dispatchers.IO) {
    val spec = QulTranslationCatalog.findEditionByKey(editionKey) ?: return@withContext false
    if (isFull6236PackDownloaded(context, editionKey)) return@withContext true

    val downloaded = fetchFullEditionFromNetwork(spec)
    if (downloaded.isNotEmpty()) {
      saveFullPackToDisk(context, editionKey, downloaded)
      // Also seed Surah 1 into Room immediately
      if (translationDao != null) {
        val batch = ArrayList<TranslationEntity>(7)
        for (v in 1..7) {
          val txt = downloaded["1:$v"] ?: continue
          batch.add(
            TranslationEntity(
              sourceType = "QURAN",
              referenceKey = "1:$v",
              surahOrBookNumber = 1,
              verseOrHadithNumber = v,
              editionKey = spec.editionKey,
              editionTitle = "${spec.languageNameNative} (${spec.languageNameEn}) • ${spec.translatorName}",
              languageCode = spec.languageCode,
              text = txt
            )
          )
        }
        if (batch.isNotEmpty()) {
          translationDao.insertTranslations(batch)
        }
      }
      return@withContext true
    }
    false
  }

  private fun fetchFullEditionFromNetwork(
    spec: QulTranslationCatalog.QulTranslationEdition
  ): Map<String, String> {
    // 1. Try Quran.com v4 API (QUL official backend)
    if (spec.qcomTranslationId != null) {
      val url = "https://api.quran.com/api/v4/quran/translations/${spec.qcomTranslationId}?fields=verse_key"
      val raw = executeSafeHttpGet(url)
      if (!raw.isNullOrBlank()) {
        try {
          val json = JSONObject(raw)
          val arr = json.optJSONArray("translations")
          if (arr != null && arr.length() > 0) {
            val map = HashMap<String, String>(arr.length())
            for (i in 0 until arr.length()) {
              val item = arr.optJSONObject(i) ?: continue
              val vk = item.optString("verse_key", "").ifEmpty {
                CANONICAL_VERSE_KEYS.getOrNull(i).orEmpty()
              }
              val txt = cleanQulTranslationText(item.optString("text", ""))
              if (vk.isNotEmpty() && txt.isNotEmpty()) {
                map[vk] = txt
              }
            }
            if (map.size >= 6000) return map
          }
        } catch (_: Exception) {
        }
      }
    }

    // 2. Try fawazahmed0/quran-api single minified JSON
    if (spec.fawazEditionKey != null) {
      val url = "https://cdn.jsdelivr.net/gh/fawazahmed0/quran-api@1/editions/${spec.fawazEditionKey}.min.json"
      val raw = executeSafeHttpGet(url)
      if (!raw.isNullOrBlank()) {
        try {
          val json = JSONObject(raw)
          val arr = json.optJSONArray("quran")
          if (arr != null && arr.length() > 0) {
            val map = HashMap<String, String>(arr.length())
            for (i in 0 until arr.length()) {
              val item = arr.optJSONObject(i) ?: continue
              val s = item.optInt("chapter", 0)
              val v = item.optInt("verse", 0)
              val txt = cleanQulTranslationText(item.optString("text", ""))
              if (s in 1..114 && v >= 1 && txt.isNotEmpty()) {
                map["$s:$v"] = txt
              }
            }
            if (map.size >= 6000) return map
          }
        } catch (_: Exception) {
        }
      }
    }

    return emptyMap()
  }

  private fun fetchSurahFromQuranEnc(quranEncKey: String, surahNumber: Int): Map<Int, String> {
    val url = "https://quranenc.com/api/v1/translation/sura/$quranEncKey/$surahNumber"
    val raw = executeSafeHttpGet(url) ?: return emptyMap()
    return try {
      val json = JSONObject(raw)
      val arr = json.optJSONArray("result") ?: return emptyMap()
      val map = HashMap<Int, String>(arr.length())
      for (i in 0 until arr.length()) {
        val item = arr.optJSONObject(i) ?: continue
        val vNum = item.optString("aya", "").toIntOrNull() ?: item.optInt("aya", 0)
        val txt = cleanQulTranslationText(item.optString("translation", ""))
        val fn = cleanQulTranslationText(item.optString("footnotes", ""))
        if (vNum >= 1 && txt.isNotEmpty()) {
          map[vNum] = if (fn.isNotEmpty()) "$txt [[FN: $fn]]" else txt
        }
      }
      map
    } catch (_: Exception) {
      emptyMap()
    }
  }

  private fun executeSafeHttpGet(urlStr: String): String? {
    if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(urlStr)) return null
    var conn: HttpURLConnection? = null
    return try {
      conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15_000
        readTimeout = 20_000
        instanceFollowRedirects = false
        setRequestProperty("Accept", "application/json")
        setRequestProperty("User-Agent", "BayanAlHuda-Android/1.0")
      }
      if (conn.responseCode in 200..299) {
        val contentLen = conn.contentLengthLong
        if (contentLen > SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
          return null
        }
        conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
          val sb = StringBuilder()
          val buffer = CharArray(8192)
          var totalRead = 0L
          var read: Int
          while (reader.read(buffer).also { read = it } != -1) {
            totalRead += read
            if (totalRead > SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
              return null
            }
            sb.append(buffer, 0, read)
          }
          sb.toString()
        }
      } else {
        null
      }
    } catch (_: Exception) {
      null
    } finally {
      conn?.disconnect()
    }
  }
}
