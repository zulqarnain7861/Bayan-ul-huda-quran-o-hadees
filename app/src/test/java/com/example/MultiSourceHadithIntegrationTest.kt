package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.download.DownloadManager
import com.example.data.download.HadithDownloadScopeType
import com.example.data.download.HadithDownloadSelectionRequest
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.OfflineHadithAssetLoader
import com.example.data.local.entities.HadithEntity
import com.example.data.remote.IslamicRemoteService
import com.example.data.remote.MultiSourceHadithCatalog
import com.example.data.remote.SecurityIntegrityGuard
import com.example.data.repository.IslamicRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MultiSourceHadithIntegrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testFawazAhmedEndpointsMatchSpecification() {
        // /editions
        assertEquals(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions.json",
            MultiSourceHadithCatalog.fawazEditionsUrl(minified = false)
        )
        assertEquals(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions.min.json",
            MultiSourceHadithCatalog.fawazEditionsUrl(minified = true)
        )
        // /editions/{editionName}
        assertEquals(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-abudawud.json",
            MultiSourceHadithCatalog.fawazWholeEditionUrl("eng-abudawud", minified = false)
        )
        // /editions/{editionName}/{HadithNo}
        assertEquals(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-abudawud/1035.json",
            MultiSourceHadithCatalog.fawazHadithByNumberUrl("eng-abudawud", 1035, minified = false)
        )
        assertEquals(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-abudawud/1035.min.json",
            MultiSourceHadithCatalog.fawazHadithByNumberUrl("eng-abudawud", 1035, minified = true)
        )
        // /editions/{editionName}/sections/{sectionNo}
        assertEquals(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-abudawud/sections/7.json",
            MultiSourceHadithCatalog.fawazSectionUrl("eng-abudawud", 7, minified = false)
        )
        // /info
        assertEquals(
            "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/info.json",
            MultiSourceHadithCatalog.fawazInfoUrl()
        )
    }

    @Test
    fun testAhmedBasetEndpointsStrictlyPinnedToTagV1_2_0() {
        // Verify tag is strictly v1.2.0 and never main
        assertEquals("v1.2.0", MultiSourceHadithCatalog.AHMED_BASET_TAG)
        assertEquals(
            "https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_chapter/the_9_books/bukhari/1.json",
            MultiSourceHadithCatalog.ahmedBasetByChapterForCollection("bukhari", 1)
        )
        assertEquals(
            "https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_book/the_9_books/bukhari.json",
            MultiSourceHadithCatalog.ahmedBasetByBookForCollection("bukhari")
        )
        assertEquals(
            "https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_chapter/other_books/riyad_assalihin/1.json",
            MultiSourceHadithCatalog.ahmedBasetByChapterForCollection("riyadussalihin", 1)
        )
        assertEquals(
            "https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_book/forties/nawawi40.json",
            MultiSourceHadithCatalog.ahmedBasetByBookForCollection("nawawi")
        )

        // All 17 AhmedBaset books must be pinned to v1.2.0
        assertEquals(17, MultiSourceHadithCatalog.ahmedBasetMappings.size)
        MultiSourceHadithCatalog.ahmedBasetMappings.values.forEach { coord ->
            val byBook = coord.byBookUrl()
            val byChap = coord.byChapterUrl(1)
            assertTrue("Must pin to v1.2.0: $byBook", byBook.contains("/v1.2.0/db/by_book/"))
            assertFalse("Must never use main branch: $byBook", byBook.contains("/main/"))
            assertTrue("Must pin to v1.2.0: $byChap", byChap.contains("/v1.2.0/db/by_chapter/"))
            assertTrue(SecurityIntegrityGuard.isUrlTrustedAndAllowed(byBook))
            assertTrue(SecurityIntegrityGuard.isUrlTrustedAndAllowed(byChap))
        }
    }

    @Test
    fun testHadithApiComAndSunnahComTrustedBySecurityGuard() {
        val sampleBooksUrl = MultiSourceHadithCatalog.hadithApiBooksUrl("sample_test_key")
        assertNotNull(sampleBooksUrl)
        assertTrue(SecurityIntegrityGuard.isUrlTrustedAndAllowed(sampleBooksUrl!!))

        val sampleChapterUrl = MultiSourceHadithCatalog.hadithApiChapterHadithsUrl("bukhari", 1, 50, "sample_test_key")
        assertNotNull(sampleChapterUrl)
        assertTrue(SecurityIntegrityGuard.isUrlTrustedAndAllowed(sampleChapterUrl!!))

        val sunnahUrl = MultiSourceHadithCatalog.sunnahBookHadithsUrl("bukhari", 1)
        assertNotNull(sunnahUrl)
        assertTrue(SecurityIntegrityGuard.isUrlTrustedAndAllowed(sunnahUrl!!))
    }

    @Test
    fun testParseAhmedBasetV1_2_0AndHadithApiComJsonStructures() {
        val ahmedBasetSampleJson = """
            {
              "id": 1,
              "metadata": {
                "id": 1,
                "length": 7,
                "arabic": { "title": "صحيح البخاري", "author": "الإمام محمد بن إسماعيل البخاري" },
                "english": { "title": "Sahih al-Bukhari", "author": "Imam Muhammad ibn Ismail al-Bukhari" }
              },
              "chapter": {
                "id": 1,
                "bookId": 1,
                "arabic": "كتاب بدء الوحي",
                "english": "Revelation"
              },
              "hadiths": [
                {
                  "id": 1,
                  "idInBook": 1,
                  "chapterId": 1,
                  "bookId": 1,
                  "arabic": "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
                  "english": {
                    "narrator": "Narrated 'Umar bin Al-Khattab:",
                    "text": "I heard Allah's Messenger saying, 'The reward of deeds depends upon the intentions.'"
                  }
                }
              ]
            }
        """.trimIndent()

        val parsedAhmed = IslamicRemoteService.parseAhmedBasetHadithJson(
            collectionKey = "bukhari",
            collectionName = "Sahih al-Bukhari",
            rawJson = ahmedBasetSampleJson,
            defaultBookNumber = 1
        )
        assertEquals(1, parsedAhmed.size)
        val h1 = parsedAhmed.first()
        assertEquals(1, h1.hadithNumber)
        assertTrue(h1.arabicText.contains("إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ"))
        assertTrue(h1.englishText.contains("The reward of deeds depends upon the intentions"))
        assertTrue(h1.narrator.contains("Umar bin Al-Khattab"))

        val hadithApiComSampleJson = """
            {
              "status": 200,
              "hadiths": {
                "data": [
                  {
                    "id": 1,
                    "hadithNumber": "1",
                    "englishNarrator": "Narrated 'Umar bin Al-Khattab:",
                    "hadithEnglish": "The reward of deeds depends upon the intentions.",
                    "hadithUrdu": "اعمال کا دارومدار نیتوں پر ہے۔",
                    "hadithArabic": "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
                    "chapterNumber": "1",
                    "status": "Sahih",
                    "chapter": {
                      "chapterNumber": "1",
                      "chapterEnglish": "How the Divine Revelation started"
                    }
                  }
                ]
              }
            }
        """.trimIndent()

        val parsedHadithApi = IslamicRemoteService.parseHadithApiComJson(
            collectionKey = "bukhari",
            collectionName = "Sahih al-Bukhari",
            rawJson = hadithApiComSampleJson,
            defaultBookNumber = 1
        )
        assertEquals(1, parsedHadithApi.size)
        val hApi1 = parsedHadithApi.first()
        assertEquals(1, hApi1.hadithNumber)
        assertEquals(h1.arabicText, hApi1.arabicText) // Shared canonical Arabic text!
        assertTrue(hApi1.urduText.contains("اعمال کا دارومدار"))
        assertEquals("Sahih", hApi1.grade)
    }

    @Test
    fun testMultipleNamedTranslationsPreserveCanonicalArabicAndStoreSeparately() = runBlocking {
        val repo = IslamicRepository.getInstance(context)
        val sampleHadith = HadithEntity(
            collectionKey = "bukhari",
            collectionName = "Sahih al-Bukhari",
            bookNumber = 1,
            bookName = "Book 1: Revelation",
            chapterNumber = 1,
            chapterName = "How Divine Revelation started",
            hadithNumber = 1,
            darussalamReference = "Darussalam: #1",
            inBookReference = "In-Book: Book 1, Hadith 1",
            arabicNumber = 1,
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى",
            englishText = "The reward of deeds depends upon the intentions.",
            urduText = "اعمال کا دارومدار نیتوں پر ہے۔",
            narrator = "'Umar ibn al-Khattab (RA)",
            grade = "Sahih",
            gradingScholar = "Imam al-Bukhari",
            reference = "Sahih al-Bukhari 1"
        )

        val editions = MultiSourceHadithCatalog.getNamedEditionsForCollection("bukhari")
        assertTrue("Expected multiple named editions for Bukhari", editions.size >= 5)

        val fawazEn = editions.first { it.editionKey == "eng-bukhari" }
        val ahmedBasetEn = editions.first { it.editionKey == "eng-ahmedbaset-bukhari-v1.2.0" }
        val fawazUr = editions.first { it.editionKey == "urd-bukhari" }

        val saved1 = repo.fetchAndCacheNamedHadithTranslation(sampleHadith, fawazEn)
        val saved2 = repo.fetchAndCacheNamedHadithTranslation(sampleHadith, ahmedBasetEn)
        val saved3 = repo.fetchAndCacheNamedHadithTranslation(sampleHadith, fawazUr)

        assertNotNull(saved1)
        assertNotNull(saved2)
        assertNotNull(saved3)

        val storedList = repo.getNamedTranslationsForHadith("bukhari", 1)
        val storedKeys = storedList.map { it.editionKey }.toSet()
        assertTrue(storedKeys.contains("eng-bukhari"))
        assertTrue(storedKeys.contains("eng-ahmedbaset-bukhari-v1.2.0"))
        assertTrue(storedKeys.contains("urd-bukhari"))
    }

    @Test
    fun testAll18SunniBooksHaveValidMultiSourceAndOfflineMappings() = runBlocking {
        val eighteenSunniKeys = listOf(
            "bukhari", "muslim", "abudawud", "tirmidhi", "nasai", "ibnmajah", "malik",
            "ahmad", "darimi", "riyadussalihin", "adab", "bulugh", "mishkat", "shamail",
            "nawawi", "qudsi", "dehlawi", "hisn"
        )
        for (key in eighteenSunniKeys) {
            val mapping = MultiSourceHadithCatalog.sunniBookMappings[key]
            assertNotNull("Missing SunniBookSourceMapping for $key", mapping)
            val namedEditions = MultiSourceHadithCatalog.getNamedEditionsForCollection(key)
            assertTrue("Expected at least 1 named edition for $key", namedEditions.isNotEmpty())

            val resolvedChapterUrl = MultiSourceHadithCatalog.resolveDownloadSourceUrl(
                collectionKey = key,
                chapterOrSectionNo = 1,
                hadithNo = null,
                preferFullBook = false
            )
            assertTrue(
                "Resolved URL for $key must be trusted: $resolvedChapterUrl",
                SecurityIntegrityGuard.isUrlTrustedAndAllowed(resolvedChapterUrl)
            )
        }

        // Verify DownloadManager resolves AhmedBaset v1.2.0 URL for riyadussalihin (not broken ara-riyadussalihin)
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.AppDatabase::class.java
        ).allowMainThreadQueries().build()
        try {
            val dm = DownloadManager(context = context, database = db)
            dm.ensureDefaultLicencesSeeded()
            val pkgId = dm.enqueueAndExecuteHadithDownload(
                request = HadithDownloadSelectionRequest(
                    collectionKey = "riyadussalihin",
                    collectionTitle = "Riyad as-Salihin",
                    tradition = "SUNNI",
                    scopeType = HadithDownloadScopeType.COMPLETE_CHAPTER,
                    selectedChapters = setOf(1)
                ),
                runInBackground = false
            )
            val items = db.downloadManifestDao().getItemsForPackage(pkgId)
            assertEquals(1, items.size)
            assertEquals(
                "https://raw.githubusercontent.com/AhmedBaset/hadith-json/v1.2.0/db/by_chapter/other_books/riyad_assalihin/1.json",
                items.first().sourceUrl
            )
        } finally {
            db.close()
        }

        // Verify bundled offline archive count is intact for the non-7 books
        val bundledNawawi = OfflineHadithAssetLoader.loadCollection(context, "nawawi")
        assertEquals(42, bundledNawawi.size)
        val bundledRiyad = OfflineHadithAssetLoader.loadCollection(context, "riyadussalihin")
        assertTrue(bundledRiyad.size >= 1800)

        // Verify ExtendedHadithDataProvider has 0 raw URL leaks in UI metadata
        ExtendedHadithDataProvider.primaryCollections.forEach { meta ->
            assertFalse(meta.coverageStatusBadge.contains("http://"))
            assertFalse(meta.coverageStatusBadge.contains("https://"))
        }
    }
}
