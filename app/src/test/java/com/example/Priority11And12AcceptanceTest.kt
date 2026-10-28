package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.download.AudioDownloadScopeMode
import com.example.data.download.AyahRangeSelection
import com.example.data.download.DownloadManager
import com.example.data.download.HadithDownloadScopeType
import com.example.data.download.HadithDownloadSelectionRequest
import com.example.data.download.OfflineCatalogRegistry
import com.example.data.download.OfflineContentResult
import com.example.data.download.OfflineTransferTransport
import com.example.data.download.QuranAudioDownloadRequest
import com.example.data.download.QuranDownloadSelectionRequest
import com.example.data.download.TransferResponse
import com.example.data.evidence.ClaimVerificationStatus
import com.example.data.evidence.EvidenceVerificationEngine
import com.example.data.evidence.HadithGradeCategory
import com.example.data.local.AppDatabase
import com.example.data.local.ImportStatus
import com.example.data.local.InitialDataLoader
import com.example.data.local.OfflineQuranAssetLoader
import com.example.data.local.RawSunnahHadithRecord
import com.example.data.local.SunnahDotComImporter
import com.example.data.local.VerificationStatus
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.DownloadManifestStatus
import com.example.data.local.entities.NoteEntity
import com.example.data.prayer.AppThemeMode
import com.example.data.prayer.PrayerPreferencesManager
import com.example.data.repository.SourceRegistryRepository
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Priority11And12AcceptanceTest {

  private lateinit var context: Context
  private lateinit var db: AppDatabase
  private lateinit var downloadManager: DownloadManager
  private lateinit var evidenceEngine: EvidenceVerificationEngine

  @Before
  fun setUp() = runBlocking {
    context = ApplicationProvider.getApplicationContext()
    File(context.filesDir, "offline_packages").deleteRecursively()
    context.getSharedPreferences("bayan_offline_download_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    db.surahDao().insertSurahs(InitialDataLoader.getAll114Surahs())
    db.hadithDao().insertHadiths(InitialDataLoader.getFoundationalHadiths())
    downloadManager = DownloadManager(context = context, database = db)
    DownloadManager.setInstanceForTesting(downloadManager)
    downloadManager.ensureDefaultLicencesSeeded()
    evidenceEngine = EvidenceVerificationEngine(
      context = context,
      database = db,
      downloadManager = downloadManager
    )
  }

  @After
  fun tearDown() {
    DownloadManager.setInstanceForTesting(null)
    File(context.filesDir, "offline_packages").deleteRecursively()
    context.getSharedPreferences("bayan_offline_download_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    db.close()
  }

  // =========================================================================
  // PRIORITY 11 — 16 OFFLINE ACCEPTANCE TESTS
  // =========================================================================

  @Test
  fun `priority 11 tests 1 2 3 13 - complete Quran text all 114 surahs separate translations and missing translation report`() = runBlocking {
    // Test 1 & 2: Download complete Quran text & verify all 114 surahs are represented
    val pkgIds = downloadManager.enqueueAndExecuteQuranSelection(
      request = QuranDownloadSelectionRequest(
        includeCompleteArabicText = true,
        selectAll114Surahs = true,
        selectedTranslationEditions = setOf("en.sahih", "en.clearquran"),
        selectedTafsirEditions = setOf("ibn_kathir"),
        includeNavigationMetadata = true,
        includeSearchIndex = true
      ),
      runInBackground = false
    )
    assertTrue(pkgIds.contains("quran_arabic_complete_114"))
    assertTrue(pkgIds.contains("quran_translation_en.sahih"))
    assertTrue(pkgIds.contains("quran_translation_en.clearquran"))
    assertTrue(pkgIds.contains("quran_tafsir_ibn_kathir"))

    val completePkg = db.downloadManifestDao().getPackageById("quran_arabic_complete_114")
    assertNotNull(completePkg)
    assertEquals(DownloadManifestStatus.COMPLETE, completePkg!!.statusEnum)
    assertTrue(completePkg.isVerifiedOfflineReopenable)
    assertEquals(114, completePkg.totalItems)
    assertEquals(114, completePkg.completedItems)
    assertEquals(0, completePkg.failedItems)
    assertEquals(100, completePkg.overallProgressPercent)

    val surahItems = db.downloadManifestDao().getItemsForPackage("quran_arabic_complete_114")
    assertEquals(114, surahItems.size)
    for (s in 1..114) {
      assertTrue(surahItems.any { it.resourceIdentifier == "quran:surah:$s" && it.statusEnum == DownloadManifestStatus.COMPLETE })
    }

    // Test 3: Verify both translations contain all 6,236 expected verses and downloading en.clearquran did NOT overwrite en.sahih
    val sahihCount = db.translationDao().getTranslationCountForEdition("en.sahih")
    val clearQuranCount = db.translationDao().getTranslationCountForEdition("en.clearquran")
    assertEquals(6236, sahihCount)
    assertEquals(6236, clearQuranCount)

    val translationsAt255 = db.translationDao().getTranslationsForReferenceSnapshot("QURAN", "2:255")
    assertTrue(translationsAt255.any { it.editionKey == "en.sahih" })
    assertTrue(translationsAt255.any { it.editionKey == "en.clearquran" })

    // Test 13: Missing translations are reported accurately
    val missingFor255 = downloadManager.getMissingTranslationsForAyah(
      surahNumber = 2,
      verseNumber = 255,
      requestedEditionKeys = listOf("en.sahih", "en.clearquran", "ur.maududi")
    )
    assertEquals(listOf("ur.maududi"), missingFor255)
  }

  @Test
  fun `priority 11 test 4 and 6 - single ayah 2_255 and multiple selected surahs download and open offline`() = runBlocking {
    // Before downloading Surah 2 or Ayah 2:255, strict offline open reports "This content is not available offline"
    val beforeResult = downloadManager.openAyahOffline(
      surahNumber = 2,
      verseNumber = 255,
      requireExplicitDownloadManifest = true
    )
    assertTrue(beforeResult is OfflineContentResult.NotAvailableOffline)
    assertEquals(
      "This content is not available offline",
      (beforeResult as OfflineContentResult.NotAvailableOffline).message
    )

    // Test 4: Select and download single Ayah 2:255 (Surah Al-Baqarah, Ayah 255)
    val singleAyahIds = downloadManager.enqueueAndExecuteQuranSelection(
      request = QuranDownloadSelectionRequest(
        includeCompleteArabicText = false,
        selectAll114Surahs = false,
        specificAyahRange = AyahRangeSelection(surahNumber = 2, startAyah = 255, endAyah = 255),
        selectedTranslationEditions = emptySet(),
        selectedTafsirEditions = emptySet()
      ),
      runInBackground = false
    )
    assertEquals("quran_ayah_2_255_255", singleAyahIds.first())

    val afterResult = downloadManager.openAyahOffline(
      surahNumber = 2,
      verseNumber = 255,
      requireExplicitDownloadManifest = true
    )
    assertTrue(afterResult is OfflineContentResult.Available)
    val ayah255 = (afterResult as OfflineContentResult.Available).data
    assertEquals(2, ayah255.surahNumber)
    assertEquals(255, ayah255.verseNumber)
    assertTrue(ayah255.textArabic.isNotBlank())

    // Test 6: Multiple selected surahs download correctly (e.g., Surahs 1, 13, 36, 112)
    val multiSurahIds = downloadManager.enqueueAndExecuteQuranSelection(
      request = QuranDownloadSelectionRequest(
        includeCompleteArabicText = false,
        selectAll114Surahs = false,
        selectedSurahNumbers = setOf(1, 13, 36, 112),
        selectedTranslationEditions = emptySet(),
        selectedTafsirEditions = emptySet()
      ),
      runInBackground = false
    )
    val multiPkg = db.downloadManifestDao().getPackageById(multiSurahIds.first())
    assertNotNull(multiPkg)
    assertEquals(DownloadManifestStatus.COMPLETE, multiPkg!!.statusEnum)
    assertEquals(4, multiPkg.totalItems)
    assertEquals(4, multiPkg.completedItems)
  }

  @Test
  fun `priority 11 tests 5 and 12 - separate Quran audio download offline playback and deleting audio preserves Quran text and bookmarks`() = runBlocking {
    // Seed Quran text, a bookmark, and a personal study note
    val surah1 = OfflineQuranAssetLoader.loadSurahFromAssets(context, 1)
    db.ayahDao().insertAyahs(surah1)
    db.bookmarkDao().insertBookmark(
      BookmarkEntity(
        itemType = "QURAN",
        itemKey = "ayah_1_1",
        title = "Al-Fatihah 1:1",
        subtitle = "In the name of Allah",
        arabicSnippet = surah1.first().textArabic
      )
    )
    db.noteDao().insertNote(
      NoteEntity(
        referenceKey = "1:1",
        title = "Personal Reflection",
        content = "Bismillah study note preserved across downloads"
      )
    )

    val reciter = OfflineCatalogRegistry.verifiedReciters.first() // Husary
    assertNotEquals(reciter.chapterReciterId, reciter.ayahReciterId)

    // Test 5: Download full-surah chapter audio for Surahs 1 and 112
    val chapterAudioPkgId = downloadManager.enqueueAndExecuteQuranAudioDownload(
      request = QuranAudioDownloadRequest(
        reciter = reciter,
        mode = AudioDownloadScopeMode.FULL_CHAPTER_AUDIO,
        selectAll114Surahs = false,
        selectedSurahNumbers = setOf(1, 112)
      ),
      runInBackground = false
    )
    val audioPkg = db.downloadManifestDao().getPackageById(chapterAudioPkgId)
    assertNotNull(audioPkg)
    assertEquals(DownloadManifestStatus.COMPLETE, audioPkg!!.statusEnum)
    assertEquals(2, audioPkg.completedItems)

    val verifiedAudioFile = downloadManager.findVerifiedOfflineAudioFile(reciter.id, 1, 1)
    assertNotNull(verifiedAudioFile)
    assertTrue(verifiedAudioFile!!.exists())
    assertTrue(verifiedAudioFile.length() > 100L)

    // Play downloaded recording offline via AudioPlayerManager
    downloadManager.setOfflineModeEnabled(true)
    val playerManager = com.example.ui.audio.AudioPlayerManager(context)
    playerManager.playAyah(1, 1, "Al-Fatihah")
    assertTrue(playerManager.audioState.value.isVisible)
    assertTrue(playerManager.audioState.value.statusMessage.contains("Offline Audio", ignoreCase = true))
    playerManager.stop()

    // Test 12: Removing audio does NOT remove Quran text, bookmarks, or personal notes
    downloadManager.deleteAudioPackageOnly(chapterAudioPkgId)
    assertFalse(verifiedAudioFile.exists())
    assertEquals(7, db.ayahDao().getAyahsBySurahSnapshot(1).size)
    assertTrue(db.bookmarkDao().getAllBookmarks().first().any { it.itemKey == "ayah_1_1" })
    assertEquals(1, db.noteDao().getAllNotes().first().size)
  }

  @Test
  fun `priority 11 tests 7 8 15 - failed file never counts as complete interrupted download recovers safely and progress reflects actual work`() = runBlocking {
    val reciter = OfflineCatalogRegistry.verifiedReciters.first()

    // Test 7: Inject a transport where Surah 2 audio fails with HTTP 500 while Surah 1 succeeds
    var failSurah2 = true
    downloadManager.customTransport = object : OfflineTransferTransport {
      override suspend fun executeTransfer(
        url: String,
        resourceIdentifier: String,
        expectedFormat: String,
        existingPartialBytes: ByteArray?
      ): TransferResponse {
        if (failSurah2 && resourceIdentifier.endsWith(":002")) {
          return TransferResponse(
            httpStatusCode = 500,
            payloadBytes = ByteArray(0),
            supportsByteRange = false
          )
        }
        val validMp3 = byteArrayOf(
          0x49, 0x44, 0x33, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20,
          0xFF.toByte(), 0xFB.toByte(), 0x90.toByte(), 0x64
        ) + ByteArray(256) { 0x55 }
        return TransferResponse(
          httpStatusCode = 200,
          payloadBytes = validMp3,
          supportsByteRange = true
        )
      }
    }

    val pkgId = downloadManager.enqueueAndExecuteQuranAudioDownload(
      request = QuranAudioDownloadRequest(
        reciter = reciter,
        mode = AudioDownloadScopeMode.FULL_CHAPTER_AUDIO,
        selectAll114Surahs = false,
        selectedSurahNumbers = setOf(1, 2)
      ),
      runInBackground = false
    )

    val partialPkg = db.downloadManifestDao().getPackageById(pkgId)
    assertNotNull(partialPkg)
    // A package with a failed item must be marked PARTIAL, never COMPLETE
    assertEquals(DownloadManifestStatus.PARTIAL, partialPkg!!.statusEnum)
    assertEquals(2, partialPkg.totalItems)
    assertEquals(1, partialPkg.completedItems)
    assertEquals(1, partialPkg.failedItems)
    assertEquals(1, partialPkg.remainingItems)
    assertEquals(50, partialPkg.overallProgressPercent)
    assertFalse(partialPkg.isVerifiedOfflineReopenable)

    // Test 8: Simulate an interrupted .part file and verify recovery without corrupting content
    val items = db.downloadManifestDao().getItemsForPackage(pkgId)
    val failedItem = items.first { it.statusEnum == DownloadManifestStatus.FAILED }
    val partFile = File("${failedItem.localFilePath}.part")
    partFile.parentFile?.mkdirs()
    // Write garbage partial bytes and test server returning 200 (no byte-range support) -> safely restarts and writes clean file
    partFile.writeBytes(byteArrayOf(0x00, 0x01, 0x02))

    failSurah2 = false
    downloadManager.retryFailedItems(pkgId)

    val recoveredPkg = db.downloadManifestDao().getPackageById(pkgId)
    assertNotNull(recoveredPkg)
    assertEquals(DownloadManifestStatus.COMPLETE, recoveredPkg!!.statusEnum)
    assertEquals(2, recoveredPkg.completedItems)
    assertEquals(0, recoveredPkg.failedItems)
    assertEquals(100, recoveredPkg.overallProgressPercent)
    assertTrue(recoveredPkg.isVerifiedOfflineReopenable)
    assertFalse(partFile.exists())

    downloadManager.customTransport = null
  }

  @Test
  fun `priority 11 tests 9 10 11 14 - Sunni and Shia hadith numbering partial vs complete collection offline search and licence restrictions`() = runBlocking {
    // Test 9 & 10: Download a single chapter of Sahih al-Bukhari -> verify numbering & verify partial collection is NOT labelled complete
    val partialHadithPkgId = downloadManager.enqueueAndExecuteHadithDownload(
      request = HadithDownloadSelectionRequest(
        collectionKey = "bukhari",
        collectionTitle = "Sahih al-Bukhari",
        tradition = "SUNNI",
        scopeType = HadithDownloadScopeType.COMPLETE_CHAPTER,
        selectedChapters = setOf(1)
      ),
      runInBackground = false
    )
    val partialHadithPkg = db.downloadManifestDao().getPackageById(partialHadithPkgId)
    assertNotNull(partialHadithPkg)
    assertEquals(DownloadManifestStatus.COMPLETE, partialHadithPkg!!.statusEnum)
    assertTrue(partialHadithPkg.scopeDescription.contains("Partial Collection", ignoreCase = true))

    // Test 10: Partial chapter download must NEVER mark the entire collection as completely downloaded
    assertFalse(downloadManager.isHadithCollectionCompletelyDownloaded("bukhari"))

    // Verify Hadith numbers and edition references in Room
    val h1 = db.hadithDao().getHadith("bukhari", 1)
    assertNotNull(h1)
    assertEquals(1, h1!!.hadithNumber)
    assertTrue(h1.darussalamReference.isNotBlank())
    assertTrue(h1.inBookReference.isNotBlank())

    // Test Shia Hadith chapter download (Al-Kafi)
    val shiaPkgId = downloadManager.enqueueAndExecuteHadithDownload(
      request = HadithDownloadSelectionRequest(
        collectionKey = "alkafi",
        collectionTitle = "Kitab al-Kafi",
        tradition = "SHIA",
        scopeType = HadithDownloadScopeType.COMPLETE_CHAPTER,
        selectedChapters = setOf(1)
      ),
      runInBackground = false
    )
    val shiaPkg = db.downloadManifestDao().getPackageById(shiaPkgId)
    assertEquals(DownloadManifestStatus.COMPLETE, shiaPkg!!.statusEnum)

    // Test 11: Search works on downloaded content with the internet disabled (Offline Mode)
    downloadManager.setOfflineModeEnabled(true)
    val (_, offlineHadithHits) = downloadManager.searchOfflineContent("intentions")
    assertTrue(offlineHadithHits.isNotEmpty())

    // Test 14: Content is never downloaded when its permissions prohibit bulk download (e.g. Complete Collection of Musnad Ahmad)
    val restrictedPkgId = downloadManager.enqueueAndExecuteHadithDownload(
      request = HadithDownloadSelectionRequest(
        collectionKey = "ahmad",
        collectionTitle = "Musnad Ahmad",
        tradition = "SUNNI",
        scopeType = HadithDownloadScopeType.COMPLETE_COLLECTION
      ),
      runInBackground = false
    )
    val restrictedPkg = db.downloadManifestDao().getPackageById(restrictedPkgId)
    assertNotNull(restrictedPkg)
    assertEquals(DownloadManifestStatus.LICENCE_RESTRICTED, restrictedPkg!!.statusEnum)
    assertEquals(0, restrictedPkg.completedItems)
    assertEquals(0L, restrictedPkg.downloadedBytes)
    assertTrue(restrictedPkg.errorMessage.contains("prohibited", ignoreCase = true))
    assertTrue(restrictedPkg.externalFallbackUrl.startsWith("https://"))
  }

  @Test
  fun `priority 11 test 16 - existing features SunnahDotComImporter Surah Ar-Rad 13_1 small alif and multiple themes work`() = runBlocking {
    // 1. Verify Surah Ar-Ra'd (13:1) preserves canonical Tanzil Uthmani الٓمٓر and dagger alif (\u0670) and Surah 2:1 remains الٓمٓ
    val surah13 = OfflineQuranAssetLoader.loadSurahFromAssets(context, 13)
    assertTrue(surah13.isNotEmpty())
    assertTrue(
      "Expected Surah 13:1 to start with canonical الٓمٓر but was: ${surah13.first().textArabic}",
      surah13.first().textArabic.startsWith("الٓمٓر") && surah13.first().textArabic.contains('\u0670')
    )
    val surah2 = OfflineQuranAssetLoader.loadSurahFromAssets(context, 2)
    assertEquals("الٓمٓ", surah2.first().textArabic)

    // 2. Verify SunnahDotComImporter and SourceRegistry integration
    val sourceRepo = SourceRegistryRepository(db.sourceRegistryDao())
    sourceRepo.ensureDefaultSourcesSeeded()
    val importer = SunnahDotComImporter(
      sourceRegistryDao = db.sourceRegistryDao(),
      hadithDao = db.hadithDao(),
      collectionKey = "custom_sunnah_test",
      collectionTitle = "Sunan Test Collection"
    )
    val summary = importer.importCollection(
      records = listOf(
        RawSunnahHadithRecord(
          collectionKey = "custom_sunnah_test",
          collectionName = "Sunan Test Collection",
          bookNumber = 1,
          bookName = "Introduction",
          chapterNumber = 1,
          chapterName = "Merits of Knowledge",
          hadithNumber = 1,
          darussalamNumber = "Darussalam: #1",
          inBookReference = "In-Book: Book 1, Hadith 1",
          arabicEditionNumber = 1,
          arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
          englishText = "Actions are judged by intentions.",
          urduText = "اعمال کا دارومدار نیتوں پر ہے۔",
          narrator = "Umar ibn al-Khattab",
          grade = "Sahih",
          gradingScholar = "Al-Bukhari"
        )
      ),
      totalExpectedInSource = 1
    )
    assertEquals(1, summary.totalImportedRecords)
    assertEquals(ImportStatus.SUCCESS, summary.importStatus)
    assertEquals(VerificationStatus.VERIFIED, summary.verificationStatus)

    // 3. Verify new themes (ROYAL_EMERALD & MADINAH_PARCHMENT) persist cleanly
    val baseConfig = PrayerPreferencesManager.loadConfig(context)
    PrayerPreferencesManager.saveConfig(context, baseConfig.copy(themeMode = AppThemeMode.ROYAL_EMERALD))
    assertEquals(AppThemeMode.ROYAL_EMERALD, PrayerPreferencesManager.loadConfig(context).themeMode)
    PrayerPreferencesManager.saveConfig(context, baseConfig.copy(themeMode = AppThemeMode.MADINAH_PARCHMENT))
    assertEquals(AppThemeMode.MADINAH_PARCHMENT, PrayerPreferencesManager.loadConfig(context).themeMode)
  }

  // =========================================================================
  // PRIORITY 12 — 10 ISLAM WITH EVIDENCE ACCEPTANCE TESTS
  // =========================================================================

  @Test
  fun `priority 12 tests 1 5 6 8 9 10 - exact quotation Sunni and Shia identification thematic separation Urdu meaning offline mode and clickable citations`() = runBlocking {
    downloadManager.setOfflineModeEnabled(true)

    // Test 1, 6, 8, 9, 10: Verify "Actions are judged by intentions" in Offline Mode
    val intentionsResp = evidenceEngine.verifyClaimOrQuestion(
      rawQuery = "Actions are judged by intentions",
      isOfflineModeOverride = true
    )
    assertEquals(ClaimVerificationStatus.EXACT_SOURCE_FOUND, intentionsResp.status)
    assertTrue(intentionsResp.workedOfflineOnly)
    assertTrue(intentionsResp.directQuotations.isNotEmpty())

    val bukhariCitation = intentionsResp.directQuotations.first { it.narrationOrVerseIdentifier == "Sahih al-Bukhari 1" }
    assertEquals("DIRECT_PRIMARY_QUOTATION", bukhariCitation.relationshipType)
    assertEquals("https://sunnah.com/bukhari:1", bukhariCitation.officialVerificationUrl)
    assertEquals("bukhari", bukhariCitation.navigableCollectionKey)
    assertEquals(1, bukhariCitation.navigableHadithNumber)
    assertTrue(bukhariCitation.urduTranslation.contains("اعمال کا دارومدار نیتوں پر ہے"))
    assertTrue(intentionsResp.commentaryAndSynthesisUrdu.contains("صحیح بخاری"))

    // Test 6: Thematic Quran-hadith relationship (Quran 98:5 on sincerity) is labelled THEMATIC_CORROBORATION, never mislabelled as a direct quotation of the hadith
    val thematicQuran = intentionsResp.thematicCorroborations.first { it.narrationOrVerseIdentifier == "Quran 98:5" }
    assertEquals("THEMATIC_CORROBORATION", thematicQuran.relationshipType)
    assertEquals(98, thematicQuran.navigableSurahNumber)
    assertEquals(5, thematicQuran.navigableVerseNumber)

    // Test 5: Sunni and Shia sources are clearly identified (Hadith al-Thaqalayn)
    val thaqalaynResp = evidenceEngine.verifyClaimOrQuestion("Hadith al-Thaqalayn Quran and Ahl al-Bayt")
    assertEquals(ClaimVerificationStatus.EXACT_SOURCE_FOUND, thaqalaynResp.status)
    assertTrue(thaqalaynResp.directQuotations.any { it.sourceType == "HADITH_SUNNI" && it.traditionBadge.contains("Sunni") })
    assertTrue(thaqalaynResp.directQuotations.any { it.sourceType == "HADITH_SHIA" && it.traditionBadge.contains("Twelver Shia") })

    // Test 10: Every displayed citation can be opened and checked, and Report Incorrect Reference persists in Room
    val allDisplayedCitations = intentionsResp.directQuotations + intentionsResp.thematicCorroborations + thaqalaynResp.directQuotations
    for (c in allDisplayedCitations) {
      assertTrue(c.officialVerificationUrl.startsWith("https://"))
      val hasInAppTarget = (c.navigableSurahNumber != null && c.navigableVerseNumber != null) ||
        (c.navigableCollectionKey != null && c.navigableHadithNumber != null)
      assertTrue("Citation ${c.citationId} must have an in-app navigable target", hasInAppTarget)
    }

    val reportId = evidenceEngine.reportIncorrectReference(
      claimOrQuery = "Actions are judged by intentions",
      referenceIdentifier = "Sahih al-Bukhari 1",
      sourceCollection = "Sahih al-Bukhari",
      issueCategory = "Edition Numbering Check",
      userNotes = "Testing administrator review queue"
    )
    assertTrue(reportId > 0L)
    val savedReports = evidenceEngine.getSubmittedReferenceReports()
    assertEquals(1, savedReports.size)
    assertEquals("PENDING_ADMIN_REVIEW", savedReports.first().status)
  }

  @Test
  fun `priority 12 tests 2 3 4 7 - missing narration never fabricated differing gradings attributed historical claim caution and unverified speaker claims`() = runBlocking {
    // Test 2: A missing narration does NOT generate a fabricated replacement and is NEVER labelled "FALSE" merely because search returned no results
    val missingResp = evidenceEngine.verifyClaimOrQuestion("Whoever eats seven green apples on Tuesday morning will fly over the mountains")
    assertEquals(ClaimVerificationStatus.NO_VERIFIED_MATCH_IN_INDEXED_SOURCES, missingResp.status)
    assertNotEquals("FALSE", missingResp.status.displayLabel)
    assertTrue(missingResp.directQuotations.isEmpty())
    assertTrue(missingResp.thematicCorroborations.isEmpty())
    assertTrue(missingResp.headlineVerdict.contains("unresolved", ignoreCase = true))

    // Test 3: Differing scholarly gradings are presented with attribution (Cave of Thawr spider & doves in Musnad Ahmad #3251)
    val spiderResp = evidenceEngine.verifyClaimOrQuestion("Spider web and dove at the Cave of Thawr during Hijrah")
    assertEquals(ClaimVerificationStatus.SCHOLARLY_GRADING_DISAGREEMENT, spiderResp.status)
    assertTrue(spiderResp.scholarlyAssessments.size >= 2)
    assertTrue(spiderResp.scholarlyAssessments.any { it.scholarName.contains("Ibn Hajar") && it.assignedGrade == HadithGradeCategory.HASAN })
    assertTrue(spiderResp.scholarlyAssessments.any { it.scholarName.contains("Albani") && it.assignedGrade == HadithGradeCategory.DAIF })

    // Test 4: Historical claims are not presented as certain without sufficient evidence
    val historyResp = evidenceEngine.verifyClaimOrQuestion("Investigate a historical claim in Tarikh al-Tabari via Abu Mikhnaf or Sayf ibn Umar")
    assertEquals(ClaimVerificationStatus.HISTORICAL_CLAIM_NEEDS_MORE_EVIDENCE, historyResp.status)
    assertTrue(historyResp.scholarlyAssessments.any { it.scholarName.contains("Tabari") })

    // Test 7: Unverified popular/speaker claims are not treated as authoritative ("He who knows himself knows his Lord" & "Seek knowledge even unto China")
    val knowsSelfResp = evidenceEngine.verifyClaimOrQuestion("He who knows himself knows his Lord")
    assertEquals(ClaimVerificationStatus.ATTRIBUTION_DISPUTED, knowsSelfResp.status)
    assertTrue(knowsSelfResp.scholarlyAssessments.any { it.scholarName.contains("Nawawi") && it.assignedGrade == HadithGradeCategory.MAWDU })

    val chinaResp = evidenceEngine.verifyClaimOrQuestion("Seek knowledge even if you have to go as far as China")
    assertEquals(ClaimVerificationStatus.ATTRIBUTION_DISPUTED, chinaResp.status)
    assertTrue(chinaResp.scholarlyAssessments.any { it.scholarName.contains("Bayhaqi") })

    // Also verify all 10 Learning Modules and all 7 Controversial/Historical Dossiers are populated
    val modules = evidenceEngine.getAll10LearningModules()
    assertEquals(10, modules.size)
    assertTrue(modules.all { it.workedExampleArabic.isNotBlank() && it.quiz.options.size >= 3 && it.furtherReading.isNotEmpty() })

    val dossiers = evidenceEngine.getControversialAndHistoricalDossiers()
    assertEquals(7, dossiers.size)
  }
}
