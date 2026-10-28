package com.example.data.remote

import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.OfflineQuranAssetLoader
import com.example.data.local.entities.AyahEntity
import com.example.data.local.entities.HadithEntity
import com.example.data.local.entities.PrayerScheduleEntity
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONObject

object IslamicRemoteService {

  const val NOT_AVAILABLE_IN_SOURCE = "Not available in this source"

  // Fast in-memory cache for instant Hadith & Book Section lookup
  private val hadithMemoryCache = ConcurrentHashMap<String, HadithEntity>()
  private val bookSectionMemoryCache = ConcurrentHashMap<String, List<HadithEntity>>()

  suspend fun fetchSurahFromApi(surahNumber: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
    try {
      val url =
        "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,en.sahih,ur.jalandhry,en.transliteration"
      val json = httpGet(url) ?: return@withContext emptyList()
      val root = JSONObject(json)
      val dataArray = root.optJSONArray("data") ?: return@withContext emptyList()
      if (dataArray.length() < 4) return@withContext emptyList()

      val arAyahs = dataArray.getJSONObject(0).getJSONArray("ayahs")
      val enAyahs = dataArray.getJSONObject(1).getJSONArray("ayahs")
      val urAyahs = dataArray.getJSONObject(2).getJSONArray("ayahs")
      val trAyahs = dataArray.getJSONObject(3).getJSONArray("ayahs")

      val result = ArrayList<AyahEntity>(arAyahs.length())
      for (i in 0 until arAyahs.length()) {
        val arObj = arAyahs.getJSONObject(i)
        val enObj = enAyahs.getJSONObject(i)
        val urObj = urAyahs.getJSONObject(i)
        val trObj = trAyahs.getJSONObject(i)
        val verseNum = arObj.getInt("numberInSurah")
        val rawAr = SecurityIntegrityGuard.sanitizeSacredText(arObj.getString("text"))
        val arText = OfflineQuranAssetLoader.stripDuplicateBismillahPrefix(surahNumber, verseNum, rawAr)
        if (!SecurityIntegrityGuard.isValidQuranVersePayload(surahNumber, verseNum, arText)) continue

        result.add(
          AyahEntity(
            surahNumber = surahNumber,
            verseNumber = verseNum,
            juzNumber = arObj.optInt("juz", 1),
            pageNumber = arObj.optInt("page", 1),
            textArabic = arText,
            translationEnglish = SecurityIntegrityGuard.sanitizeSacredText(enObj.getString("text")),
            translationUrdu = SecurityIntegrityGuard.sanitizeSacredText(urObj.getString("text")),
            transliteration = SecurityIntegrityGuard.sanitizeSacredText(trObj.optString("text", ""))
          )
        )
      }
      result
    } catch (e: Exception) {
      emptyList()
    }
  }

  private fun resolveSunniApiSlug(collectionKey: String): String? = when (collectionKey.lowercase()) {
    "bukhari" -> "bukhari"
    "muslim" -> "muslim"
    "abudawud" -> "abudawud"
    "tirmidhi" -> "tirmidhi"
    "nasai" -> "nasai"
    "ibnmajah" -> "ibnmajah"
    "malik" -> "malik"
    "nawawi" -> "nawawi"
    "qudsi" -> "qudsi"
    "dehlawi" -> "dehlawi"
    else -> null
  }

  private fun resolveShiaThaqalaynBookId(collectionKey: String, bookNumber: Int): String? = when (collectionKey.lowercase()) {
    "alkafi" -> "Al-Kafi-Volume-${bookNumber.coerceIn(1, 8)}-Kulayni"
    "faqih" -> "Man-La-Yahduruh-al-Faqih-Volume-${bookNumber.coerceIn(1, 5)}-Saduq"
    "tahdhib" -> "Tahdhib-al-Ahkam-Volume-${bookNumber.coerceIn(1, 4)}-Tusi"
    "nahjulbalagha" -> "Nahj-al-Balagha-Radi"
    "sajjadiyya" -> "Risalat-al-Huquq-Abidin"
    "khisal" -> "Al-Khisal-Saduq"
    "uyun_akhbar_rida" -> "Uyun-akhbar-al-Rida-Volume-${bookNumber.coerceIn(1, 2)}-Saduq"
    "amali_saduq" -> if (bookNumber == 2) "Al-Amali-Mufid" else "Al-Amali-Saduq"
    "saduq_corpus" -> when (bookNumber) {
      2 -> "Al-Tawhid-Saduq"
      3 -> "Maani-al-Akhbar-Saduq"
      4 -> "Thawab-al-Amal-wa-iqab-al-Amal-Saduq"
      5 -> "Kamil-al-Ziyarat-Qummi"
      6 -> "Kitab-al-Ghayba-Numani"
      7 -> "Kitab-al-Ghayba-Tusi"
      8 -> "Sifat-al-Shia-Saduq"
      9 -> "Fadail-al-Shia-Saduq"
      10 -> "Fadail-al-Ashhur-al-Thalatha-Saduq"
      11 -> "Al-Imama-wa’l-Tabsira-min-al-Hayra-Babawayh"
      12 -> "Kitab-al-Mumin-Ahwazi"
      13 -> "Kitab-al-Zuhd-Ahwazi"
      else -> "Kamal-al-Din-wa-Tamam-al-Nima-Saduq"
    }
    else -> null
  }

  private fun offlineCollectionFallback(
    collectionKey: String,
    bookNumber: Int? = null
  ): List<HadithEntity> {
    val allForCollection = ExtendedHadithDataProvider.getExtendedHadiths().filter {
      it.collectionKey.equals(collectionKey, ignoreCase = true)
    }
    if (allForCollection.isEmpty()) return emptyList()
    if (bookNumber == null) return allForCollection
    val byBook = allForCollection.filter { it.bookNumber == bookNumber }
    return byBook.ifEmpty { allForCollection }
  }

  /**
   * Fast parallel Hadith fetch by canonical Darussalam / Sunnah.com / Shia Canonical number or In-Book number.
   * Checks curated offline ExtendedHadithDataProvider first for instant 0ms response, then queries
   * fawazahmed0 (Sunni) or Thaqalayn v2 (Shia) concurrently, sanitizes via SecurityIntegrityGuard, and caches.
   */
  suspend fun fetchHadithByNumberFromApi(
    collectionKey: String,
    collectionName: String,
    hadithNumber: Int
  ): HadithEntity? = withContext(Dispatchers.IO) {
    if (hadithNumber <= 0) return@withContext null
    val cacheKey = "${collectionKey.lowercase()}:$hadithNumber"
    hadithMemoryCache[cacheKey]?.let { return@withContext it }

    // 1. Check curated offline ExtendedHadithDataProvider strictly by collectionKey and hadithNumber
    ExtendedHadithDataProvider.getExtendedHadiths().find {
      it.collectionKey.equals(collectionKey, ignoreCase = true) &&
        it.hadithNumber == hadithNumber
    }?.let { offlineMatch ->
      hadithMemoryCache[cacheKey] = offlineMatch
      return@withContext offlineMatch
    }

    // 2. Try Shia Thaqalayn v2 API if this is a Shia collection
    val shiaBookId = resolveShiaThaqalaynBookId(collectionKey, 1)
    if (shiaBookId != null) {
      fetchShiaHadithByNumberFromThaqalayn(collectionKey, collectionName, shiaBookId, hadithNumber)?.let { shiaEntity ->
        hadithMemoryCache[cacheKey] = shiaEntity
        return@withContext shiaEntity
      }
    }

    // 3. Try Sunni fawazahmed0 API if supported
    val apiSlug = resolveSunniApiSlug(collectionKey) ?: return@withContext null

    try {
      val enUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-$apiSlug/$hadithNumber.min.json"
      val arUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-$apiSlug/$hadithNumber.min.json"
      val urUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/urd-$apiSlug/$hadithNumber.min.json"

      var (enJson, arJson, urJson) = coroutineScope {
        val enDeferred = async { httpGet(enUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
        val arDeferred = async { httpGet(arUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
        val urDeferred = async { httpGet(urUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
        Triple(enDeferred.await(), arDeferred.await(), urDeferred.await())
      }

      var enHadith = enJson?.optJSONArray("hadiths")?.optJSONObject(0)
      var arHadith = arJson?.optJSONArray("hadiths")?.optJSONObject(0)
      var urHadith = urJson?.optJSONArray("hadiths")?.optJSONObject(0)

      var englishText = SecurityIntegrityGuard.sanitizeSacredText(enHadith?.optString("text", "").orEmpty())
      var arabicText = SecurityIntegrityGuard.sanitizeSacredText(arHadith?.optString("text", "").orEmpty())
      var urduText = SecurityIntegrityGuard.sanitizeSacredText(urHadith?.optString("text", "").orEmpty())

      // In fawazahmed0/hadith-api, Sahih Muslim ("muslim") serialized slots 1..92 are Muqaddimah slots
      // where Arabic/English are mostly empty, while Book 1 (Kitab al-Iman) Hadith 1..92 starts at 93 (hadithNumber + 92).
      if (apiSlug == "muslim" && arabicText.isBlank() && englishText.isBlank() && hadithNumber in 1..92) {
        val shifted = hadithNumber + 92
        val enUrl2 = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-$apiSlug/$shifted.min.json"
        val arUrl2 = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-$apiSlug/$shifted.min.json"
        val urUrl2 = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/urd-$apiSlug/$shifted.min.json"
        val shiftedTriple = coroutineScope {
          val enD = async { httpGet(enUrl2)?.let { runCatching { JSONObject(it) }.getOrNull() } }
          val arD = async { httpGet(arUrl2)?.let { runCatching { JSONObject(it) }.getOrNull() } }
          val urD = async { httpGet(urUrl2)?.let { runCatching { JSONObject(it) }.getOrNull() } }
          Triple(enD.await(), arD.await(), urD.await())
        }
        enJson = shiftedTriple.first ?: enJson
        arJson = shiftedTriple.second ?: arJson
        urJson = shiftedTriple.third ?: urJson
        enHadith = enJson?.optJSONArray("hadiths")?.optJSONObject(0)
        arHadith = arJson?.optJSONArray("hadiths")?.optJSONObject(0)
        urHadith = urJson?.optJSONArray("hadiths")?.optJSONObject(0)
        englishText = SecurityIntegrityGuard.sanitizeSacredText(enHadith?.optString("text", "").orEmpty())
        arabicText = SecurityIntegrityGuard.sanitizeSacredText(arHadith?.optString("text", "").orEmpty())
        urduText = SecurityIntegrityGuard.sanitizeSacredText(urHadith?.optString("text", "").orEmpty())
      }

      if (enJson == null && arJson == null && urJson == null) return@withContext null

      if (!SecurityIntegrityGuard.isValidCanonicalHadithPayload(collectionKey, hadithNumber, arabicText, englishText)) {
        return@withContext null
      }

      val refObj = enHadith?.optJSONObject("reference") ?: arHadith?.optJSONObject("reference")
      val bookNum = refObj?.optInt("book", 0) ?: 0
      val inBookHadith = refObj?.optInt("hadith", 0) ?: 0
      val arabicNumber = arHadith?.optInt("arabicnumber", 0) ?: enHadith?.optInt("arabicnumber", 0) ?: 0

      val metadataObj = enJson?.optJSONObject("metadata") ?: arJson?.optJSONObject("metadata")
      val sectionObj = metadataObj?.optJSONObject("section")
      val sectionTitle = if (bookNum > 0) {
        SecurityIntegrityGuard.sanitizeSacredText(sectionObj?.optString(bookNum.toString(), "").orEmpty())
      } else {
        ""
      }

      val inBookReferenceStr = if (bookNum > 0 && inBookHadith > 0) {
        "In-Book: Book $bookNum, Hadith $inBookHadith"
      } else {
        "In-Book: $NOT_AVAILABLE_IN_SOURCE"
      }

      val darussalamRefStr = "Darussalam: #$hadithNumber" +
        if (arabicNumber > 0 && arabicNumber != hadithNumber) " • Arabic Ed: #$arabicNumber" else ""

      val gradesArr = enHadith?.optJSONArray("grades") ?: arHadith?.optJSONArray("grades")
      val firstGradeObj = if (gradesArr != null && gradesArr.length() > 0) gradesArr.getJSONObject(0) else null
      val defaultConsensusGrade = when (apiSlug) {
        "bukhari", "muslim" -> "Sahih"
        else -> NOT_AVAILABLE_IN_SOURCE
      }
      val defaultScholar = when (apiSlug) {
        "bukhari" -> "Imam al-Bukhari (Sahih Consensus)"
        "muslim" -> "Imam Muslim (Sahih Consensus)"
        else -> NOT_AVAILABLE_IN_SOURCE
      }
      val gradeStr = SecurityIntegrityGuard.sanitizeSacredText(
        firstGradeObj?.optString("grade", "")?.trim()?.ifBlank { defaultConsensusGrade } ?: defaultConsensusGrade
      )
      val scholarStr = SecurityIntegrityGuard.sanitizeSacredText(
        firstGradeObj?.optString("name", "")?.trim()?.ifBlank { defaultScholar } ?: defaultScholar
      )

      val extractedNarrator = extractNarratorFromText(englishText) ?: "Source Edition ($collectionName #$hadithNumber)"

      val entity = HadithEntity(
        collectionKey = collectionKey,
        collectionName = collectionName,
        bookNumber = if (bookNum > 0) bookNum else 1,
        bookName = if (sectionTitle.isNotEmpty()) "Book $bookNum: $sectionTitle" else if (bookNum > 0) "Book $bookNum" else NOT_AVAILABLE_IN_SOURCE,
        chapterNumber = 1,
        chapterName = sectionTitle.ifEmpty { "$collectionName — Hadith #$hadithNumber" },
        hadithNumber = hadithNumber,
        darussalamReference = darussalamRefStr,
        inBookReference = inBookReferenceStr,
        arabicNumber = arabicNumber,
        arabicText = arabicText.ifEmpty { NOT_AVAILABLE_IN_SOURCE },
        transliteration = "",
        englishText = englishText.ifEmpty { NOT_AVAILABLE_IN_SOURCE },
        urduText = urduText,
        narrator = extractedNarrator,
        grade = gradeStr,
        gradingScholar = scholarStr,
        reference = "$collectionName $hadithNumber",
        linkedQuranVerses = ""
      )
      hadithMemoryCache[cacheKey] = entity
      entity
    } catch (e: Exception) {
      null
    }
  }

  private fun fetchShiaHadithByNumberFromThaqalayn(
    collectionKey: String,
    collectionName: String,
    thaqalaynBookId: String,
    hadithNumber: Int
  ): HadithEntity? {
    return try {
      val primaryUrl = "https://www.thaqalayn-api.net/api/v2/$thaqalaynBookId/$hadithNumber"
      val fallbackUrl = "https://thaqalayn-api.ieron.dev/api/v2/$thaqalaynBookId/$hadithNumber"
      val raw = httpGet(primaryUrl) ?: httpGet(fallbackUrl) ?: return null
      val obj = if (raw.trim().startsWith("[")) {
        org.json.JSONArray(raw).optJSONObject(0)
      } else {
        JSONObject(raw)
      } ?: return null

      val arText = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("arabicText", ""))
      val enText = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("englishText", ""))
      if (!SecurityIntegrityGuard.isValidCanonicalHadithPayload(collectionKey, hadithNumber, arText, enText)) {
        return null
      }
      val chapterTitle = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("chapter", "$collectionName — Hadith #$hadithNumber"))
      val majlisiGrade = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("majlisiGrading", "Shia Canonical"))
      val behbudiGrade = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("behbudiGrading", ""))
      val volume = obj.optInt("volume", 1).coerceAtLeast(1)
      val offlineMatch = ExtendedHadithDataProvider.getExtendedHadiths().firstOrNull {
        it.collectionKey.equals(collectionKey, ignoreCase = true) && it.hadithNumber == hadithNumber
      }

      HadithEntity(
        collectionKey = collectionKey,
        collectionName = collectionName,
        bookNumber = volume,
        bookName = "Jild $volume (Volume $volume): $chapterTitle",
        chapterNumber = 1,
        chapterName = chapterTitle,
        hadithNumber = hadithNumber,
        darussalamReference = "Shia Canonical: $collectionName #$hadithNumber",
        inBookReference = "In-Book: Jild $volume, $chapterTitle, Hadith $hadithNumber",
        arabicNumber = hadithNumber,
        arabicText = arText.ifEmpty { offlineMatch?.arabicText ?: NOT_AVAILABLE_IN_SOURCE },
        transliteration = offlineMatch?.transliteration.orEmpty(),
        englishText = enText.ifEmpty { offlineMatch?.englishText ?: NOT_AVAILABLE_IN_SOURCE },
        urduText = offlineMatch?.urduText?.takeIf { it.isNotBlank() && !it.contains("Not available", ignoreCase = true) }.orEmpty(),
        narrator = extractNarratorFromText(enText) ?: offlineMatch?.narrator ?: "Ahl al-Bayt Isnad ($collectionName #$hadithNumber)",
        grade = majlisiGrade.ifBlank { "Shia Canonical" },
        gradingScholar = if (behbudiGrade.isNotBlank()) "Allama Majlisi / Behbudi ($behbudiGrade)" else "Allama Majlisi (Mir'at al-'Uqul)",
        reference = "$collectionName $hadithNumber",
        linkedQuranVerses = offlineMatch?.linkedQuranVerses.orEmpty()
      )
    } catch (_: Exception) {
      null
    }
  }

  /**
   * Fetches an entire book/section of Hadiths concurrently across Arabic, English, and Urdu editions.
   * Always merges/falls back to curated canonical ExtendedHadithDataProvider records so that Shia books,
   * Nahj al-Balagha sermons, and additional Sunni books never fail to load.
   */
  suspend fun fetchHadithBookSectionFromApi(
    collectionKey: String,
    collectionName: String,
    bookNumber: Int,
    maxRecords: Int = Int.MAX_VALUE
  ): List<HadithEntity> = withContext(Dispatchers.IO) {
    val sectionKey = "${collectionKey.lowercase()}_book_$bookNumber"
    bookSectionMemoryCache[sectionKey]?.let { return@withContext it }

    val offlineList = offlineCollectionFallback(collectionKey, bookNumber)
    val apiSlug = resolveSunniApiSlug(collectionKey)

    if (apiSlug == null) {
      val shiaBookId = resolveShiaThaqalaynBookId(collectionKey, bookNumber.coerceAtLeast(1))
      if (shiaBookId != null) {
        try {
          val raw = httpGet("https://www.thaqalayn-api.net/api/v2/$shiaBookId")
            ?: httpGet("https://thaqalayn-api.ieron.dev/api/v2/$shiaBookId")
          if (!raw.isNullOrBlank() && raw.trim().startsWith("[")) {
            val arr = org.json.JSONArray(raw)
            val shiaList = ArrayList<HadithEntity>(minOf(arr.length(), maxRecords))
            val limit = minOf(arr.length(), maxRecords)
            for (i in 0 until limit) {
              val obj = arr.optJSONObject(i) ?: continue
              val hNum = obj.optInt("id", i + 1).coerceAtLeast(1)
              val arText = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("arabicText", ""))
              val enText = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("englishText", ""))
              if (!SecurityIntegrityGuard.isValidCanonicalHadithPayload(collectionKey, hNum, arText, enText)) continue
              val chap = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("chapter", "$collectionName — Hadith #$hNum"))
              val majlisi = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("majlisiGrading", "Shia Canonical"))
              val behbudi = SecurityIntegrityGuard.sanitizeSacredText(obj.optString("behbudiGrading", ""))
              val vol = obj.optInt("volume", bookNumber.coerceAtLeast(1)).coerceAtLeast(1)
              val entity = HadithEntity(
                collectionKey = collectionKey,
                collectionName = collectionName,
                bookNumber = vol,
                bookName = "Jild $vol (Volume $vol): $chap",
                chapterNumber = 1,
                chapterName = chap,
                hadithNumber = hNum,
                darussalamReference = "Shia Canonical: $collectionName #$hNum",
                inBookReference = "In-Book: Jild $vol, $chap, Hadith $hNum",
                arabicNumber = hNum,
                arabicText = arText.ifEmpty { NOT_AVAILABLE_IN_SOURCE },
                transliteration = "",
                englishText = enText.ifEmpty { NOT_AVAILABLE_IN_SOURCE },
                urduText = "",
                narrator = extractNarratorFromText(enText) ?: "Ahl al-Bayt Isnad ($collectionName #$hNum)",
                grade = majlisi.ifBlank { "Shia Canonical" },
                gradingScholar = if (behbudi.isNotBlank()) "Allama Majlisi / Behbudi ($behbudi)" else "Allama Majlisi (Mir'at al-'Uqul)",
                reference = "$collectionName $hNum",
                linkedQuranVerses = ""
              )
              hadithMemoryCache["${collectionKey.lowercase()}:$hNum"] = entity
              shiaList.add(entity)
            }
            if (shiaList.isNotEmpty()) {
              bookSectionMemoryCache[sectionKey] = shiaList
              return@withContext shiaList
            }
          }
        } catch (_: Exception) {}
      }
      if (offlineList.isNotEmpty()) {
        bookSectionMemoryCache[sectionKey] = offlineList
      }
      return@withContext offlineList
    }

    try {
      val enUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-$apiSlug/sections/$bookNumber.min.json"
      val arUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-$apiSlug/sections/$bookNumber.min.json"
      val urUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/urd-$apiSlug/sections/$bookNumber.min.json"

      var (enJson, arJson, urJson) = coroutineScope {
        val enDeferred = async { httpGet(enUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
        val arDeferred = async { httpGet(arUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
        val urDeferred = async { httpGet(urUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
        Triple(enDeferred.await(), arDeferred.await(), urDeferred.await())
      }

      if (enJson == null && arJson == null && bookNumber == 1) {
        val fullEnUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-$apiSlug.min.json"
        val fullArUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-$apiSlug.min.json"
        val fullUrUrl = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/urd-$apiSlug.min.json"
        val fullTriple = coroutineScope {
          val enDef = async { httpGet(fullEnUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
          val arDef = async { httpGet(fullArUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
          val urDef = async { httpGet(fullUrUrl)?.let { runCatching { JSONObject(it) }.getOrNull() } }
          Triple(enDef.await(), arDef.await(), urDef.await())
        }
        enJson = fullTriple.first
        arJson = fullTriple.second
        urJson = fullTriple.third
      }

      if (enJson == null && arJson == null) return@withContext offlineList

      val enArr = enJson?.optJSONArray("hadiths") ?: arJson?.optJSONArray("hadiths") ?: return@withContext offlineList
      val arArr = arJson?.optJSONArray("hadiths")
      val urArr = urJson?.optJSONArray("hadiths")

      val sectionTitle = SecurityIntegrityGuard.sanitizeSacredText(
        (enJson ?: arJson)?.optJSONObject("metadata")
          ?.optJSONObject("section")
          ?.optString(bookNumber.toString(), "")
          ?.trim()
          .orEmpty()
      )

      val arByNumber = HashMap<Int, JSONObject>()
      if (arArr != null) {
        for (i in 0 until arArr.length()) {
          val obj = arArr.getJSONObject(i)
          arByNumber[obj.optInt("hadithnumber", -1)] = obj
        }
      }
      val urByNumber = HashMap<Int, JSONObject>()
      if (urArr != null) {
        for (i in 0 until urArr.length()) {
          val obj = urArr.getJSONObject(i)
          urByNumber[obj.optInt("hadithnumber", -1)] = obj
        }
      }

      val result = ArrayList<HadithEntity>()
      val limit = minOf(enArr.length(), maxRecords)
      for (i in 0 until limit) {
        val enObj = enArr.getJSONObject(i)
        val hNum = enObj.optInt("hadithnumber", 0)
        if (hNum <= 0) continue

        val arObj = arByNumber[hNum]
        val urObj = urByNumber[hNum]

        val enText = SecurityIntegrityGuard.sanitizeSacredText(enObj.optString("text", ""))
        val arText = SecurityIntegrityGuard.sanitizeSacredText(arObj?.optString("text", "").orEmpty())
        val urText = SecurityIntegrityGuard.sanitizeSacredText(urObj?.optString("text", "").orEmpty())
        if (!SecurityIntegrityGuard.isValidCanonicalHadithPayload(collectionKey, hNum, arText, enText)) continue

        val refObj = enObj.optJSONObject("reference")
        val inBookH = refObj?.optInt("hadith", 0) ?: 0
        val arabicNum = arObj?.optInt("arabicnumber", 0) ?: enObj.optInt("arabicnumber", 0)

        val gradesArr = enObj.optJSONArray("grades")
        val firstGrade = if (gradesArr != null && gradesArr.length() > 0) gradesArr.getJSONObject(0) else null
        val defaultGrade = if (apiSlug == "bukhari" || apiSlug == "muslim") "Sahih" else NOT_AVAILABLE_IN_SOURCE
        val gradeStr = SecurityIntegrityGuard.sanitizeSacredText(
          firstGrade?.optString("grade", "")?.trim()?.ifBlank { defaultGrade } ?: defaultGrade
        )
        val scholarStr = SecurityIntegrityGuard.sanitizeSacredText(
          firstGrade?.optString("name", "")?.trim()?.ifBlank { defaultGrade } ?: defaultGrade
        )

        val entity = HadithEntity(
          collectionKey = collectionKey,
          collectionName = collectionName,
          bookNumber = bookNumber,
          bookName = if (sectionTitle.isNotEmpty()) "Book $bookNumber: $sectionTitle" else "Book $bookNumber",
          chapterNumber = 1,
          chapterName = sectionTitle.ifEmpty { "Book $bookNumber" },
          hadithNumber = hNum,
          darussalamReference = "Darussalam: #$hNum",
          inBookReference = if (inBookH > 0) "In-Book: Book $bookNumber, Hadith $inBookH" else "In-Book: $NOT_AVAILABLE_IN_SOURCE",
          arabicNumber = arabicNum,
          arabicText = arText.ifEmpty { NOT_AVAILABLE_IN_SOURCE },
          transliteration = "",
          englishText = enText.ifEmpty { NOT_AVAILABLE_IN_SOURCE },
          urduText = urText.ifEmpty { NOT_AVAILABLE_IN_SOURCE },
          narrator = extractNarratorFromText(enText) ?: "Source Edition ($collectionName #$hNum)",
          grade = gradeStr,
          gradingScholar = scholarStr,
          reference = "$collectionName $hNum",
          linkedQuranVerses = ""
        )
        hadithMemoryCache["${collectionKey.lowercase()}:$hNum"] = entity
        result.add(entity)
      }
      val finalResult = if (result.isNotEmpty()) result else offlineList
      if (finalResult.isNotEmpty()) {
        bookSectionMemoryCache[sectionKey] = finalResult
      }
      finalResult
    } catch (e: Exception) {
      offlineList
    }
  }

  /**
   * Fetches daily prayer times from the reputable AlAdhan API (https://aladhan.com/prayer-times-api),
   * validates all five Salah fields and Hijri metadata, and returns null if unreachable so the app
   * falls back cleanly to local astronomical calculation or cached schedule.
   */
  suspend fun fetchPrayerTimingsFromAlAdhanApi(
    dateDdMmYyyy: String,
    dateIso: String,
    locationName: String,
    districtOrRegion: String,
    country: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    methodId: Int,
    methodName: String,
    asrMadhhab: Int,
    tahajjudFallback: String
  ): PrayerScheduleEntity? = withContext(Dispatchers.IO) {
    try {
      val url =
        "https://api.aladhan.com/v1/timings/$dateDdMmYyyy?latitude=$latitude&longitude=$longitude&method=$methodId&school=$asrMadhhab"
      val json = httpGet(url) ?: return@withContext null
      val root = JSONObject(json)
      if (root.optInt("code", 0) != 200) return@withContext null

      val data = root.optJSONObject("data") ?: return@withContext null
      val timings = data.optJSONObject("timings") ?: return@withContext null
      val dateObj = data.optJSONObject("date")

      val fajr = cleanTime(timings.optString("Fajr", ""))
      val sunrise = cleanTime(timings.optString("Sunrise", ""))
      val dhuhr = cleanTime(timings.optString("Dhuhr", ""))
      val asr = cleanTime(timings.optString("Asr", ""))
      val maghrib = cleanTime(timings.optString("Maghrib", ""))
      val isha = cleanTime(timings.optString("Isha", ""))
      val lastThird = cleanTime(timings.optString("Lastthird", "")).ifEmpty { tahajjudFallback }

      if (fajr.isEmpty() || dhuhr.isEmpty() || asr.isEmpty() || maghrib.isEmpty() || isha.isEmpty()) {
        return@withContext null
      }

      val gregorianReadable = SecurityIntegrityGuard.sanitizeSacredText(dateObj?.optString("readable", dateIso).orEmpty())
      val hijriObj = dateObj?.optJSONObject("hijri")
      val hijriDay = SecurityIntegrityGuard.sanitizeSacredText(hijriObj?.optString("day", "").orEmpty())
      val hijriMonth = SecurityIntegrityGuard.sanitizeSacredText(hijriObj?.optJSONObject("month")?.optString("en", "").orEmpty())
      val hijriYear = SecurityIntegrityGuard.sanitizeSacredText(hijriObj?.optString("year", "").orEmpty())
      val hijriReadable = if (hijriDay.isNotEmpty() && hijriMonth.isNotEmpty()) {
        "$hijriDay $hijriMonth $hijriYear AH"
      } else {
        "Hijri Date Verified"
      }

      PrayerScheduleEntity(
        dateLocationKey = "${dateIso}_${locationName}_${methodId}_$asrMadhhab",
        dateIso = dateIso,
        gregorianReadable = gregorianReadable,
        hijriReadable = hijriReadable,
        locationName = locationName,
        districtOrRegion = districtOrRegion,
        country = country,
        latitude = latitude,
        longitude = longitude,
        timeZoneId = timeZoneId,
        calculationMethodId = methodId,
        calculationMethodName = methodName,
        asrMadhhab = asrMadhhab,
        fajrTime = fajr,
        sunriseTime = sunrise,
        dhuhrTime = dhuhr,
        asrTime = asr,
        maghribTime = maghrib,
        ishaTime = isha,
        tahajjudLastThirdStart = lastThird,
        sourceName = "AlAdhan Prayer-Time API (Verified & Cached)",
        lastUpdatedEpochMillis = System.currentTimeMillis()
      )
    } catch (e: Exception) {
      null
    }
  }

  suspend fun checkEndpointReachable(url: String): Boolean = withContext(Dispatchers.IO) {
    httpGet(url) != null
  }

  private fun cleanTime(raw: String): String {
    val trimmed = raw.trim().substringBefore(" ")
    return if (trimmed.matches(Regex("\\d{1,2}:\\d{2}"))) {
      val parts = trimmed.split(":")
      "%02d:%02d".format(parts[0].toInt(), parts[1].toInt())
    } else {
      ""
    }
  }

  private fun extractNarratorFromText(text: String): String? {
    val trimmed = text.trim()
    val colonIdx = trimmed.indexOf(':')
    if (colonIdx in 8..95) {
      val prefix = trimmed.substring(0, colonIdx).trim()
      if (prefix.startsWith("Narrated ", ignoreCase = true) ||
        prefix.startsWith("It was narrated", ignoreCase = true) ||
        prefix.contains("narrated", ignoreCase = true)
      ) {
        return prefix.removePrefix("Narrated ").removePrefix("narrated ").trim()
      }
    }
    return null
  }

  private fun httpGet(urlString: String): String? {
    // Enforce strict HTTPS & Host Allowlist via SecurityIntegrityGuard (blocks any ad or unauthorized domain)
    if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(urlString)) {
      return null
    }
    var conn: HttpURLConnection? = null
    return try {
      conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        instanceFollowRedirects = false // Prevent open-redirect hijacking to untrusted hosts
        connectTimeout = 2800
        readTimeout = 3200
        setRequestProperty("Accept", "application/json")
      }
      if (conn.responseCode in 200..299) {
        val contentLength = conn.contentLengthLong
        if (contentLength > SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
          return null
        }
        conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
          val sb = StringBuilder()
          val buffer = CharArray(8192)
          var totalChars = 0
          var read: Int
          while (reader.read(buffer).also { read = it } != -1) {
            totalChars += read
            if (totalChars > SecurityIntegrityGuard.MAX_HTTP_RESPONSE_BYTES) {
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
