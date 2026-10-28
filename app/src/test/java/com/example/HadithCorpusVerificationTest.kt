package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.InitialDataLoader
import com.example.data.local.OfflineHadithAssetLoader
import com.example.data.repository.IslamicRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HadithCorpusVerificationTest {

  private lateinit var context: Context
  private lateinit var db: AppDatabase
  private lateinit var repository: IslamicRepository

  @Before
  fun setUp() = runBlocking {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    db.surahDao().insertSurahs(InitialDataLoader.getAll114Surahs())
    db.hadithDao().insertHadiths(InitialDataLoader.getFoundationalHadiths())
    repository = IslamicRepository(
      database = db,
      appContext = context
    )
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `verify 7 main Sunni books preserved untouched and Bukhari Hadith 1 is clean`() = runBlocking {
    val sevenMainBooks = listOf("bukhari", "muslim", "abudawud", "tirmidhi", "nasai", "ibnmajah", "malik")
    val allFoundational = ExtendedHadithDataProvider.getExtendedHadiths()

    for (key in sevenMainBooks) {
      assertFalse(
        "Main Sunni book $key must NOT be overridden by bundled asset loader",
        OfflineHadithAssetLoader.hasBundledAsset(key)
      )
      val meta = ExtendedHadithDataProvider.primaryCollections.firstOrNull { it.key == key }
      assertNotNull("Metadata must exist for $key", meta)
      val records = allFoundational.filter { it.collectionKey == key }
      assertTrue("Local foundational records must exist for $key", records.isNotEmpty())
      val h1 = records.firstOrNull { it.hadithNumber == 1 }
      assertNotNull("Hadith #1 must exist in foundational records for $key", h1)
      assertTrue("Arabic text must be non-blank for $key #1", h1!!.arabicText.isNotBlank())
      assertTrue("English text must be non-blank for $key #1", h1.englishText.isNotBlank())
    }

    val bukhari1 = allFoundational.first { it.collectionKey == "bukhari" && it.hadithNumber == 1 }
    assertEquals(1, bukhari1.arabicNumber)

    // Verify Sahih Muslim missing Urdu list matches the exact 23 numbers audited in Step 2-D
    val missingMuslimUrdu = OfflineHadithAssetLoader.getSahihMuslimMuslimUrduCheck(context)
    assertEquals(23, missingMuslimUrdu.size)
    assertTrue(missingMuslimUrdu.containsAll(listOf(2940, 2946, 4678, 5196, 5210)))
  }

  private fun OfflineHadithAssetLoader.getSahihMuslimMuslimUrduCheck(context: Context): Set<Int> =
    getSahihMuslimMissingUrduNumbers(context)

  @Test
  fun `verify all 19 bundled Hadith corpus assets pass SHA-256 verification and full book matrix`() = runBlocking {
    val expectedBooks = listOf(
      "nawawi",
      "qudsi",
      "riyadussalihin",
      "ahmad",
      "darimi",
      "adab",
      "bulugh",
      "mishkat",
      "shamail",
      "ibnkhuzaymah",
      "ibnhibban",
      "mustadrak",
      "abdurrazzaq",
      "ibnabishaybah",
      "daraqutni",
      "bayhaqi",
      "nasai_kubra",
      "tabarani_kabir",
      "sunni_commentaries",
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

    val manifest = OfflineHadithAssetLoader.loadManifest(context)
    assertEquals(43, manifest.size)

    for (key in expectedBooks) {
      assertTrue("Book $key must have bundled asset", OfflineHadithAssetLoader.hasBundledAsset(key))
      assertTrue("SHA-256 checksum must match manifest.json for $key", OfflineHadithAssetLoader.verifyAssetSha256(context, key))

      val manifestEntry = OfflineHadithAssetLoader.getManifestEntry(context, key)
      assertNotNull("Manifest entry must exist for $key", manifestEntry)
      val meta = ExtendedHadithDataProvider.primaryCollections.firstOrNull { it.key == key }
      assertNotNull("Metadata must exist for $key", meta)

      // 1. Chapters / Book sections list loads
      val allItems = OfflineHadithAssetLoader.loadCollection(context, key)
      assertTrue("Collection items must be non-empty for $key", allItems.isNotEmpty())
      assertEquals(
        "Loaded count for $key must match manifest totalHadiths",
        manifestEntry!!.totalHadiths,
        allItems.size
      )

      val distinctBooks = allItems.map { it.bookNumber }.distinct()
      assertTrue("Distinct chapters/books must be non-empty for $key", distinctBooks.isNotEmpty())

      // 2. Number range & total count
      val range = OfflineHadithAssetLoader.getNumberRangeForCollection(context, key)
      val minNum = range.first
      val maxNum = range.last
      assertEquals("Min number for $key must be 1", 1, minNum)
      assertTrue("Max number for $key must be >= 40", maxNum >= 40)

      // 3. Hadith #1 opens
      val h1 = repository.findOrFetchHadithByNumber(key, 1)
      assertNotNull("Hadith #1 must open for $key", h1)
      assertEquals(1, h1!!.hadithNumber)
      assertTrue("Hadith #1 Arabic text must be non-blank for $key", h1.arabicText.isNotBlank())

      // 4. Hadith #50 and #70 open (if within book's max number; otherwise out-of-range returns null)
      if (maxNum >= 50) {
        val h50 = OfflineHadithAssetLoader.findHadithByNumber(context, key, 50)
        assertNotNull("Hadith #50 must open for $key", h50)
        assertEquals(50, h50!!.hadithNumber)
        assertTrue("Hadith #50 Arabic text must be non-blank for $key", h50.arabicText.isNotBlank())
      } else {
        val h50 = OfflineHadithAssetLoader.findHadithByNumber(context, key, 50)
        assertNull("Out-of-range Hadith #50 for $key (max=$maxNum) must return null", h50)
      }

      if (maxNum >= 70) {
        val h70 = OfflineHadithAssetLoader.findHadithByNumber(context, key, 70)
        assertNotNull("Hadith #70 must open for $key", h70)
        assertEquals(70, h70!!.hadithNumber)
        assertTrue("Hadith #70 Arabic text must be non-blank for $key", h70.arabicText.isNotBlank())
      } else {
        val h70 = OfflineHadithAssetLoader.findHadithByNumber(context, key, 70)
        assertNull("Out-of-range Hadith #70 for $key (max=$maxNum) must return null", h70)
      }

      // 5. Last Hadith (#maxNum) opens
      val hLast = OfflineHadithAssetLoader.findHadithByNumber(context, key, maxNum)
      assertNotNull("Last Hadith #$maxNum must open for $key", hLast)
      assertEquals(maxNum, hLast!!.hadithNumber)
      assertTrue("Last Hadith #$maxNum Arabic text must be non-blank for $key", hLast.arabicText.isNotBlank())

      // 6. Out-of-range number (maxNum + 500) returns null
      assertNull(
        "Out-of-range Hadith #${maxNum + 500} must return null for $key",
        OfflineHadithAssetLoader.findHadithByNumber(context, key, maxNum + 500)
      )

      // 7. Next / Previous navigation on loaded chapter
      val firstChapterHadiths = OfflineHadithAssetLoader.loadBookSection(context, key, distinctBooks.first())
      assertTrue("First chapter Hadiths must not be empty for $key", firstChapterHadiths.isNotEmpty())
      if (firstChapterHadiths.size >= 2) {
        val firstItem = firstChapterHadiths[0]
        val secondItem = firstChapterHadiths[1]
        assertTrue(secondItem.hadithNumber > firstItem.hadithNumber)
      }

      // 8. Normalized Arabic search works for every book
      val firstBundled = allItems.first()
      val bookTradition = meta!!.tradition.uppercase()
      val sampleArabicWord = ExtendedHadithDataProvider.normalizeArabicForSearch(firstBundled.arabicText)
        .split(Regex("[^\\p{InArabic}]+"))
        .firstOrNull { it.length >= 3 } ?: "الله"
      val arHits = ExtendedHadithDataProvider.searchHadithsSmartOffline(
        localCollectionHadiths = allItems.take(200),
        rawQuery = sampleArabicWord,
        activeCollectionKey = key,
        traditionFilter = bookTradition,
        includeCrossCollectionMatches = false
      )
      assertTrue("Normalized Arabic search for '$sampleArabicWord' in $key must return hits", arHits.isNotEmpty())

      // 9. English search works for bilingual books
      if (manifestEntry.languages.contains("English", ignoreCase = true)) {
        assertTrue("Bilingual book $key must have non-blank English in #1", firstBundled.englishText.isNotBlank())
        val sampleEnglishWord = firstBundled.englishText
          .split(Regex("[^A-Za-z]+"))
          .firstOrNull { it.length >= 4 } ?: "Allah"
        val enHits = ExtendedHadithDataProvider.searchHadithsSmartOffline(
          localCollectionHadiths = allItems.take(200),
          rawQuery = sampleEnglishWord,
          activeCollectionKey = key,
          traditionFilter = bookTradition,
          includeCrossCollectionMatches = false
        )
        assertTrue("English search for '$sampleEnglishWord' in $key must return hits", enHits.isNotEmpty())
      }
    }
  }

  @Test
  fun `verify Hadith number query parser and smart search support English Arabic-Indic Urdu-Indic and hash formats`() = runBlocking {
    assertEquals(50, ExtendedHadithDataProvider.parseHadithNumberQuery("50"))
    assertEquals(50, ExtendedHadithDataProvider.parseHadithNumberQuery("Hadith 50"))
    assertEquals(70, ExtendedHadithDataProvider.parseHadithNumberQuery("hadith #70"))
    assertEquals(50, ExtendedHadithDataProvider.parseHadithNumberQuery("حديث ٥٠"))
    assertEquals(70, ExtendedHadithDataProvider.parseHadithNumberQuery("حدیث ۷۰"))
    assertEquals(70, ExtendedHadithDataProvider.parseHadithNumberQuery("#70"))
    assertEquals(1896, ExtendedHadithDataProvider.parseHadithNumberQuery("Hadith #1896"))
    assertNull(ExtendedHadithDataProvider.parseHadithNumberQuery("actions are by intentions"))

    // Verify smart search in Riyad as-Salihin for "Hadith 50", "hadith #70", "50", and "حديث ٥٠"
    val riyad50A = repository.searchHadithsSmart("Hadith 50", activeCollectionKey = "riyadussalihin")
    assertEquals(1, riyad50A.size)
    assertEquals(50, riyad50A.first().hadithNumber)

    val riyad70 = repository.searchHadithsSmart("hadith #70", activeCollectionKey = "riyadussalihin")
    assertEquals(1, riyad70.size)
    assertEquals(70, riyad70.first().hadithNumber)

    val riyad50Ar = repository.searchHadithsSmart("حديث ٥٠", activeCollectionKey = "riyadussalihin")
    assertEquals(1, riyad50Ar.size)
    assertEquals(50, riyad50Ar.first().hadithNumber)
  }

  @Test
  fun `verify zero fake Urdu padding zero cloned Shia Jild records and zero raw URLs in UI metadata`() {
    for (meta in ExtendedHadithDataProvider.primaryCollections) {
      assertFalse(
        "UI metadata for ${meta.key} must not contain raw http/https URLs",
        meta.coverageStatusBadge.contains("http://") || meta.coverageStatusBadge.contains("https://") ||
          meta.numberingSystemInfo.contains("http://") || meta.numberingSystemInfo.contains("https://")
      )
    }

    val fallbackRecords = ExtendedHadithDataProvider.getExtendedHadiths()
    for (record in fallbackRecords) {
      assertFalse(
        "Record ${record.collectionKey}:${record.hadithNumber} must NOT contain fake Urdu English-wrapped string",
        record.urduText.contains("یہ حدیث مبارکہ مستند کتب حدیث سے ماخوذ ہے:")
      )
      assertFalse(
        "Record ${record.collectionKey}:${record.hadithNumber} must NOT contain cloned Jild placeholder",
        record.bookName.contains(" — Jild ")
      )
      assertFalse(
        "Record ${record.collectionKey}:${record.hadithNumber} must NOT contain fabricated Sahih Intl Ref",
        record.inBookReference.contains("Sahih Intl Ref")
      )
    }
  }
}
