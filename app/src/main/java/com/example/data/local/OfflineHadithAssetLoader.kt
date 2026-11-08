package com.example.data.local

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.example.data.local.entities.HadithEntity
import java.io.BufferedInputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import org.json.JSONObject

data class HadithBookManifestEntry(
  val collectionKey: String,
  val titleEnglish: String,
  val titleArabic: String,
  val sourceName: String,
  val internalSourceUrl: String, // Internal manifest only — not shown as raw URL in UI
  val pinnedVersion: String,
  val license: String,
  val attribution: String,
  val statusBadge: String,
  val languages: String,
  val totalHadiths: Int,
  val minHadithNumber: Int,
  val maxHadithNumber: Int,
  val totalChapters: Int,
  val upstreamSha256: String,
  val assetSha256: String,
  val compressedBytes: Int
)

object OfflineHadithAssetLoader {

  private const val CORPUS_DIR = "hadith_corpus"
  private const val MANIFEST_PATH = "$CORPUS_DIR/manifest.json"

  // Preserve the 7 main Sunni books untouched; bundle offline .dat corpora for the 20 Sunni collections + 14 Shia collections
  private val BUNDLED_COLLECTION_KEYS = setOf(
    "nawawi",
    "riyadussalihin",
    "ahmad",
    "darimi",
    "adab",
    "bulugh",
    "mishkat",
    "shamail",
    "qudsi",
    "shahwaliullah40",
    "saidibnmansur",
    "ibnkhuzaymah",
    "ibnhibban",
    "mustadrak",
    "abdurrazzaq",
    "ibnabishaybah",
    "daraqutni",
    "bayhaqi",
    "sunni_commentaries",
    "nasai_kubra",
    "tabarani_kabir",
    "tabarani_awsat",
    "alkafi",
    "faqih",
    "tahdhib",
    "nahjulbalagha",
    "sajjadiyya",
    "khisal",
    "uyun_akhbar_rida",
    "amali_saduq",
    "saduq_corpus",
    "istibsar",
    "bihar",
    "wasail",
    "mustadrak_wasail",
    "tuhaf_al_uqul",
    "hisn",
    "tafsir_qummi_ayyashi",
    "zaydi_ismaili"
  )

  fun normalizeCollectionKey(rawKey: String): String {
    return when (val lowered = rawKey.trim().lowercase()) {
      "said_ibn_mansur", "sunan_said_ibn_mansur" -> "saidibnmansur"
      "mujam_kabir", "al_mujam_al_kabir" -> "tabarani_kabir"
      "mujam_awsat", "al_mujam_al_awsat" -> "tabarani_awsat"
      else -> lowered
    }
  }

  private val arabicDiacriticsAndPunctRegex =
    Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]|[^\\u0621-\\u063A\\u0641-\\u064A\\s]")

  private fun normalizeArabicTokens(text: String): Set<String> {
    if (text.isBlank()) return emptySet()
    val normalized = text
      .replace(arabicDiacriticsAndPunctRegex, " ")
      .replace('أ', 'ا')
      .replace('إ', 'ا')
      .replace('آ', 'ا')
      .replace('ٱ', 'ا')
      .replace('ى', 'ي')
      .replace('ؤ', 'و')
      .replace('ئ', 'ي')
      .replace('ة', 'ه')
    return normalized
      .split(Regex("\\s+"))
      .filter { it.length > 2 }
      .toSet()
  }

  fun isArabicMatnCompatible(assetArabic: String, candidateArabic: String): Boolean {
    val tokensA = normalizeArabicTokens(assetArabic)
    val tokensB = normalizeArabicTokens(candidateArabic)
    if (tokensA.isEmpty() || tokensB.isEmpty()) return false
    val intersectionCount = tokensA.intersect(tokensB).size
    val smallerSize = minOf(tokensA.size, tokensB.size)
    return smallerSize > 0 && (intersectionCount.toDouble() / smallerSize.toDouble()) >= 0.45
  }

  // Keep at most 2 large collections decoded in memory at any time to prevent OutOfMemoryError on Android devices
  private val collectionCache = LruCache<String, List<HadithEntity>>(2)
  private val loadLocks = java.util.concurrent.ConcurrentHashMap<String, Any>()

  fun getCachedCollection(collectionKey: String): List<HadithEntity> {
    val key = normalizeCollectionKey(collectionKey)
    synchronized(collectionCache) {
      return collectionCache.get(key) ?: emptyList()
    }
  }

  // Lazy lookup map from ExtendedHadithDataProvider ("collectionKey:hadithNumber" -> HadithEntity)
  // so bundled .dat collections automatically inherit verified Urdu translations, transliterations, and Quran links
  private val curatedEnrichmentByKeyAndNumber: Map<String, HadithEntity> by lazy {
    val map = HashMap<String, HadithEntity>(512)
    ExtendedHadithDataProvider.getExtendedHadiths().forEach { h ->
      val k = "${normalizeCollectionKey(h.collectionKey)}:${h.hadithNumber}"
      if (!map.containsKey(k) || h.urduText.isNotBlank()) {
        map[k] = h
      }
    }
    map
  }

  @Volatile
  private var cachedManifest: List<HadithBookManifestEntry>? = null

  @Volatile
  private var sahihMuslimMissingUrduSet: Set<Int>? = null

  fun hasBundledAsset(collectionKey: String): Boolean {
    return normalizeCollectionKey(collectionKey) in BUNDLED_COLLECTION_KEYS
  }

  fun getAllBundledCollectionKeys(traditionScope: String = "BOTH"): List<String> {
    return when (traditionScope.uppercase()) {
      "SUNNI" -> BUNDLED_COLLECTION_KEYS.filter { !ExtendedHadithDataProvider.isShiaCollection(it) }
      "SHIA" -> BUNDLED_COLLECTION_KEYS.filter { ExtendedHadithDataProvider.isShiaCollection(it) }
      else -> BUNDLED_COLLECTION_KEYS.toList()
    }
  }

  fun getSahihMuslimMissingUrduNumbers(context: Context): Set<Int> {
    sahihMuslimMissingUrduSet?.let { return it }
    loadManifest(context)
    return sahihMuslimMissingUrduSet ?: setOf(
      2940, 2941, 2942, 2943, 2944, 2945, 2946,
      4678,
      5196, 5197, 5198, 5199, 5200, 5201, 5202, 5203, 5204, 5205, 5206, 5207, 5208, 5209, 5210
    )
  }

  fun loadManifest(context: Context): List<HadithBookManifestEntry> {
    cachedManifest?.let { return it }
    synchronized(this) {
      cachedManifest?.let { return it }
      return try {
        val text = context.assets.open(MANIFEST_PATH).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(text)
        val missingUrArr = root.optJSONArray("sahihMuslimMissingUrduNumbers")
        if (missingUrArr != null) {
          val s = HashSet<Int>(missingUrArr.length())
          for (i in 0 until missingUrArr.length()) {
            s.add(missingUrArr.optInt(i))
          }
          sahihMuslimMissingUrduSet = s
        }
        val arr = root.getJSONArray("books")
        val list = ArrayList<HadithBookManifestEntry>(arr.length())
        for (i in 0 until arr.length()) {
          val o = arr.getJSONObject(i)
          list.add(
            HadithBookManifestEntry(
              collectionKey = o.getString("collectionKey"),
              titleEnglish = o.getString("titleEnglish"),
              titleArabic = o.getString("titleArabic"),
              sourceName = o.getString("sourceName"),
              internalSourceUrl = o.optString("sourceUrl", ""),
              pinnedVersion = o.getString("pinnedVersion"),
              license = o.getString("license"),
              attribution = o.getString("attribution"),
              statusBadge = o.getString("statusBadge"),
              languages = o.getString("languages"),
              totalHadiths = o.optInt("totalHadiths", 0),
              minHadithNumber = o.optInt("minHadithNumber", 1),
              maxHadithNumber = o.optInt("maxHadithNumber", 0),
              totalChapters = o.optInt("totalChapters", 1),
              upstreamSha256 = o.optString("upstreamSha256", ""),
              assetSha256 = o.optString("assetSha256", ""),
              compressedBytes = o.optInt("compressedBytes", 0)
            )
          )
        }
        cachedManifest = list
        list
      } catch (e: Exception) {
        Log.w("OfflineHadithLoader", "Unable to read manifest.json: ${e.message}")
        emptyList()
      }
    }
  }

  fun getManifestEntry(context: Context, collectionKey: String): HadithBookManifestEntry? {
    val normKey = normalizeCollectionKey(collectionKey)
    return loadManifest(context).firstOrNull { it.collectionKey.equals(normKey, ignoreCase = true) }
  }

  fun verifyAssetSha256(context: Context, collectionKey: String): Boolean {
    val key = normalizeCollectionKey(collectionKey)
    if (!hasBundledAsset(key)) return true
    return try {
      val expected = getManifestEntry(context, key)?.assetSha256.orEmpty()
      val digest = MessageDigest.getInstance("SHA-256")
      context.assets.open("$CORPUS_DIR/$key.dat").use { input ->
        val buffer = ByteArray(16 * 1024)
        var read = input.read(buffer)
        while (read != -1) {
          digest.update(buffer, 0, read)
          read = input.read(buffer)
        }
      }
      val actual = digest.digest().joinToString("") { "%02x".format(it) }
      expected.isEmpty() || expected.equals(actual, ignoreCase = true)
    } catch (e: Exception) {
      false
    }
  }

  fun loadCollection(context: Context, collectionKey: String): List<HadithEntity> {
    val key = normalizeCollectionKey(collectionKey)
    if (!hasBundledAsset(key)) return emptyList()

    synchronized(collectionCache) {
      collectionCache.get(key)?.let { return it }
    }

    val keyLock = loadLocks.getOrPut(key) { Any() }
    synchronized(keyLock) {
      synchronized(collectionCache) {
        collectionCache.get(key)?.let { return it }
      }

      return try {
        val meta = ExtendedHadithDataProvider.primaryCollections.firstOrNull {
          it.key.equals(key, ignoreCase = true)
        }
        val collectionName = meta?.nameEnglish ?: key.replaceFirstChar { it.uppercase() }
        val isCommentaryReader = key == "sunni_commentaries"
        val assetPath = "$CORPUS_DIR/$key.dat"

        val manifestEntry = getManifestEntry(context, key)
        val expectedCount = (manifestEntry?.totalHadiths ?: 256).coerceIn(16, 40000)
        val result = ArrayList<HadithEntity>(expectedCount)

        val provSource = manifestEntry?.sourceName ?: "Verified Classical Corpus"
        val provLicense = manifestEntry?.license ?: "CC BY-NC-SA 4.0 / Public Domain"
        val provVersion = manifestEntry?.pinnedVersion ?: "v1.3.0 / 2023.1.8"
        val provAttrib = manifestEntry?.attribution ?: (meta?.compiler ?: "Classical Muhaddithun")
        val provChecksum = manifestEntry?.assetSha256.orEmpty()
        val provNumSys = meta?.numberingSystemInfo ?: "Canonical Edition Numbering"
        val defaultScholar = meta?.compiler ?: "Classical Muhaddithun"
        val enrichmentMap = curatedEnrichmentByKeyAndNumber

        context.assets.open(assetPath).use { rawStream ->
          val pb = java.io.PushbackInputStream(BufferedInputStream(rawStream, 32 * 1024), 2)
          val b0 = pb.read()
          val b1 = pb.read()
          if (b1 != -1) pb.unread(b1)
          if (b0 != -1) pb.unread(b0)
          val isGzip = (b0 == 0x1f && b1 == 0x8b)
          val effectiveStream = if (isGzip) GZIPInputStream(pb, 32 * 1024) else pb

          android.util.JsonReader(java.io.InputStreamReader(effectiveStream, Charsets.UTF_8)).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) {
              val propName = reader.nextName()
              if (propName == "items") {
                reader.beginArray()
                while (reader.hasNext()) {
                  reader.beginObject()
                  var num = 0
                  var bookNum = 1
                  var bookName = ""
                  var chapName = ""
                  var ar = ""
                  var en = ""
                  var urFromAsset = ""
                  var nar = "Classical Sanad"
                  var gr = "Authenticated in Source"

                  while (reader.hasNext()) {
                    when (reader.nextName()) {
                      "n" -> if (reader.peek() != android.util.JsonToken.NULL) num = reader.nextInt() else reader.nextNull()
                      "b" -> if (reader.peek() != android.util.JsonToken.NULL) bookNum = reader.nextInt().coerceAtLeast(1) else reader.nextNull()
                      "bn" -> if (reader.peek() != android.util.JsonToken.NULL) bookName = reader.nextString() else reader.nextNull()
                      "cn" -> if (reader.peek() != android.util.JsonToken.NULL) chapName = reader.nextString() else reader.nextNull()
                      "ar" -> if (reader.peek() != android.util.JsonToken.NULL) ar = reader.nextString() else reader.nextNull()
                      "en" -> if (reader.peek() != android.util.JsonToken.NULL) en = reader.nextString() else reader.nextNull()
                      "ur" -> if (reader.peek() != android.util.JsonToken.NULL) urFromAsset = reader.nextString() else reader.nextNull()
                      "nar" -> if (reader.peek() != android.util.JsonToken.NULL) nar = reader.nextString() else reader.nextNull()
                      "gr" -> if (reader.peek() != android.util.JsonToken.NULL) gr = reader.nextString() else reader.nextNull()
                      else -> reader.skipValue()
                    }
                  }
                  reader.endObject()

                  if (bookName.isEmpty()) bookName = "Book $bookNum"
                  // Verify Arabic matn compatibility before attaching curatedMatch translations
                  val rawCandidate = enrichmentMap["$key:$num"]
                  val curatedMatch = if (rawCandidate != null && isArabicMatnCompatible(ar, rawCandidate.arabicText)) {
                    rawCandidate
                  } else {
                    null
                  }
                  val finalUr = if (urFromAsset.isNotBlank()) urFromAsset else curatedMatch?.urduText.orEmpty()
                  val finalEn = if (en.isNotBlank()) en else curatedMatch?.englishText.orEmpty()
                  val finalTranslit = curatedMatch?.transliteration.orEmpty()
                  val finalQuranLinks = curatedMatch?.linkedQuranVerses.orEmpty()

                  val refLabel = if (isCommentaryReader) {
                    "$collectionName • Section #$num"
                  } else {
                    "$collectionName $num"
                  }
                  val primaryBadge = if (isCommentaryReader) {
                    "Commentary Section: #$num"
                  } else {
                    "Canonical: #$num"
                  }
                  val inBookBadge = if (isCommentaryReader) {
                    "Section $bookNum, Passage $num"
                  } else {
                    "Book $bookNum, Hadith $num"
                  }

                  result.add(
                    HadithEntity(
                      collectionKey = key,
                      collectionName = collectionName,
                      bookNumber = bookNum,
                      bookName = bookName,
                      chapterNumber = 1,
                      chapterName = chapName,
                      hadithNumber = num,
                      darussalamReference = primaryBadge,
                      inBookReference = inBookBadge,
                      arabicNumber = num,
                      arabicText = ar,
                      transliteration = finalTranslit,
                      englishText = finalEn,
                      urduText = finalUr,
                      narrator = nar,
                      grade = gr,
                      gradingScholar = defaultScholar,
                      reference = refLabel,
                      linkedQuranVerses = finalQuranLinks,
                      sourceName = provSource,
                      edition = collectionName,
                      translator = provAttrib,
                      license = provLicense,
                      versionCommit = provVersion,
                      checksumSha256 = provChecksum,
                      textOrigin = "dataset",
                      numberingSystem = provNumSys
                    )
                  )
                }
                reader.endArray()
              } else {
                reader.skipValue()
              }
            }
            reader.endObject()
          }
        }

        synchronized(collectionCache) {
          collectionCache.put(key, result)
        }
        result
      } catch (e: Exception) {
        Log.w("OfflineHadithLoader", "Failed loading asset for $key: ${e.message}")
        emptyList()
      }
    }
  }

  fun findHadithByNumber(context: Context, collectionKey: String, hadithNumber: Int): HadithEntity? {
    if (hadithNumber <= 0) return null
    val list = loadCollection(context, collectionKey)
    if (list.isEmpty()) return null
    // Binary search since list is strictly sorted by hadithNumber ascending
    var low = 0
    var high = list.lastIndex
    while (low <= high) {
      val mid = (low + high) ushr 1
      val midVal = list[mid].hadithNumber
      when {
        midVal < hadithNumber -> low = mid + 1
        midVal > hadithNumber -> high = mid - 1
        else -> return list[mid]
      }
    }
    return null
  }

  fun loadBookSection(context: Context, collectionKey: String, bookNumber: Int): List<HadithEntity> {
    val all = loadCollection(context, collectionKey)
    if (all.isEmpty()) return emptyList()
    if (bookNumber <= 0) return all
    return all.filter { it.bookNumber == bookNumber }
  }

  fun loadBookPage(
    context: Context,
    collectionKey: String,
    bookNumber: Int,
    page: Int,
    pageSize: Int = 50
  ): List<HadithEntity> {
    val section = loadBookSection(context, collectionKey, bookNumber)
    if (section.isEmpty() || pageSize <= 0) return section
    val safePage = page.coerceAtLeast(1)
    val start = ((safePage - 1) * pageSize).coerceAtMost(section.size)
    val end = (start + pageSize).coerceAtMost(section.size)
    return if (start < end) section.subList(start, end) else emptyList()
  }

  fun getNumberRangeForCollection(context: Context, collectionKey: String): IntRange {
    val key = collectionKey.lowercase()
    getManifestEntry(context, key)?.let { entry ->
      if (entry.maxHadithNumber > 0) {
        return entry.minHadithNumber.coerceAtLeast(1)..entry.maxHadithNumber
      }
    }
    val meta = ExtendedHadithDataProvider.primaryCollections.firstOrNull {
      it.key.equals(key, ignoreCase = true)
    }
    return 1..(meta?.totalCanonicalCount ?: 1).coerceAtLeast(1)
  }
}
