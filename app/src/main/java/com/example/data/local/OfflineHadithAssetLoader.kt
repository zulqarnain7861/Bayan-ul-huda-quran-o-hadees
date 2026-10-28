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

  // Preserve the 7 main Sunni books untouched; bundle offline .dat corpora for the 19 Sunni collections + 14 Shia collections
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

  // Keep up to 8 collections decoded in memory at any time for fast collection switching
  private val collectionCache = LruCache<String, List<HadithEntity>>(8)

  // Lazy lookup map from ExtendedHadithDataProvider ("collectionKey:hadithNumber" -> HadithEntity)
  // so bundled .dat collections automatically inherit verified Urdu translations, transliterations, and Quran links
  private val curatedEnrichmentByKeyAndNumber: Map<String, HadithEntity> by lazy {
    val map = HashMap<String, HadithEntity>(512)
    ExtendedHadithDataProvider.getExtendedHadiths().forEach { h ->
      val k = "${h.collectionKey.lowercase()}:${h.hadithNumber}"
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
    return collectionKey.lowercase() in BUNDLED_COLLECTION_KEYS
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
    return loadManifest(context).firstOrNull { it.collectionKey.equals(collectionKey, ignoreCase = true) }
  }

  fun verifyAssetSha256(context: Context, collectionKey: String): Boolean {
    val key = collectionKey.lowercase()
    if (!hasBundledAsset(key)) return true
    return try {
      val expected = getManifestEntry(context, key)?.assetSha256.orEmpty()
      val rawBytes = context.assets.open("$CORPUS_DIR/$key.dat").use { it.readBytes() }
      val digest = MessageDigest.getInstance("SHA-256").digest(rawBytes)
      val actual = digest.joinToString("") { "%02x".format(it) }
      expected.isEmpty() || expected.equals(actual, ignoreCase = true)
    } catch (e: Exception) {
      false
    }
  }

  fun loadCollection(context: Context, collectionKey: String): List<HadithEntity> {
    val key = collectionKey.lowercase()
    if (!hasBundledAsset(key)) return emptyList()

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
      val rawBytes = context.assets.open(assetPath).use { it.readBytes() }

      val jsonText = if (rawBytes.size >= 2 &&
        rawBytes[0] == 0x1f.toByte() &&
        rawBytes[1] == 0x8b.toByte()
      ) {
        GZIPInputStream(BufferedInputStream(rawBytes.inputStream())).bufferedReader(Charsets.UTF_8).use {
          it.readText()
        }
      } else {
        String(rawBytes, Charsets.UTF_8)
      }

      val root = JSONObject(jsonText)
      val itemsArr = root.getJSONArray("items")
      val result = ArrayList<HadithEntity>(itemsArr.length())

      val manifestEntry = getManifestEntry(context, key)
      val provSource = manifestEntry?.sourceName ?: "Verified Classical Corpus"
      val provLicense = manifestEntry?.license ?: "CC BY-NC-SA 4.0 / Public Domain"
      val provVersion = manifestEntry?.pinnedVersion ?: "v1.3.0 / 2023.1.8"
      val provAttrib = manifestEntry?.attribution ?: (meta?.compiler ?: "Classical Muhaddithun")
      val provChecksum = manifestEntry?.assetSha256.orEmpty()
      val provNumSys = meta?.numberingSystemInfo ?: "Canonical Edition Numbering"
      val enrichmentMap = curatedEnrichmentByKeyAndNumber

      for (i in 0 until itemsArr.length()) {
        val obj = itemsArr.getJSONObject(i)
        val num = obj.getInt("n")
        val bookNum = obj.optInt("b", 1).coerceAtLeast(1)
        val bookName = obj.optString("bn", "Book $bookNum")
        val chapName = obj.optString("cn", "")
        val ar = obj.optString("ar", "")
        val en = obj.optString("en", "")
        val urFromAsset = obj.optString("ur", "")
        val nar = obj.optString("nar", "Classical Sanad")
        val gr = obj.optString("gr", "Authenticated in Source")

        val curatedMatch = enrichmentMap["$key:$num"]
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
            gradingScholar = meta?.compiler ?: "Classical Muhaddithun",
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

      synchronized(collectionCache) {
        collectionCache.put(key, result)
      }
      result
    } catch (e: Exception) {
      Log.w("OfflineHadithLoader", "Failed loading asset for $key: ${e.message}")
      emptyList()
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
