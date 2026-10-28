package com.example

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthManager
import com.example.data.local.AppDatabase
import com.example.data.local.BayanDatabase
import com.example.data.local.ExtendedHadithDataProvider
import com.example.data.local.InitialDataLoader
import com.example.data.local.OfflineQuranAssetLoader
import com.example.data.local.ImportStatus
import com.example.data.local.SourceRegistry
import com.example.data.local.VerificationStatus
import com.example.data.local.entities.TranslationEntity
import com.example.data.model.IslamicBooksCatalog
import com.example.data.prayer.AppThemeMode
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.PrayerPreferencesManager
import com.example.data.prayer.PrayerTimeCalculator
import com.example.data.repository.IslamicRepository
import com.example.data.repository.SourceRegistryRepository
import com.example.data.repository.TafsirRepository
import com.example.ui.prayer.AzanAudioController
import com.example.ui.prayer.BootAndTimeChangeReceiver
import com.example.ui.prayer.PrayerAlarmScheduler
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ScheherazadeFontFamily
import com.example.ui.theme.UrduNastaliqFontFamily
import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.flow.first
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
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches Bayan al-Huda`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Bayan al-Huda", appName)
  }

  @Test
  fun `verify bundled arabic fonts and navigation routes and 7 day guest mode`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertTrue(R.font.amiri_quran != 0)
    assertTrue(R.font.amiri != 0)
    assertTrue(R.font.scheherazade_new != 0)
    assertTrue(R.font.noto_nastaliq_urdu != 0)
    assertNotNull(AmiriFontFamily)
    assertNotNull(ScheherazadeFontFamily)
    assertNotNull(UrduNastaliqFontFamily)

    assertEquals("home", BayanNavRoutes.HOME)
    assertEquals("quran", BayanNavRoutes.QURAN)
    assertEquals("hadith", BayanNavRoutes.HADITH)
    assertEquals("search", BayanNavRoutes.SEARCH)
    assertEquals("library", BayanNavRoutes.LIBRARY)

    assertEquals("https://wa.me/7889632693?text=Assalamu%20Alaikum", com.example.ui.components.WHATSAPP_SUPPORT_URL)

    assertTrue(AuthManager.startOrResumeGuestSession(context))
    assertTrue(AuthManager.isGuestSessionValid(context))
    assertEquals(7, AuthManager.getRemainingGuestDays(context))
  }

  @Test
  fun `scenario 1 and 6 - five prayer times for Kashmir India and offline Room cache`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
      set(2026, Calendar.MAY, 15, 10, 0, 0)
    }
    val schedule = PrayerTimeCalculator.calculateOfflineSchedule(
      calendar = cal,
      locationName = "Srinagar",
      districtOrRegion = "Srinagar District, Kashmir",
      country = "India",
      latitude = 34.0837,
      longitude = 74.7973,
      timeZoneId = "Asia/Kolkata",
      methodId = 1, // University of Islamic Sciences, Karachi
      asrMadhhab = 1, // Hanafi
      highLatitudeRule = 0
    )
    assertTrue(schedule.fajrTime.matches(Regex("\\d{2}:\\d{2}")))
    assertTrue(schedule.dhuhrTime.matches(Regex("\\d{2}:\\d{2}")))
    assertTrue(schedule.asrTime.matches(Regex("\\d{2}:\\d{2}")))
    assertTrue(schedule.maghribTime.matches(Regex("\\d{2}:\\d{2}")))
    assertTrue(schedule.ishaTime.matches(Regex("\\d{2}:\\d{2}")))
    assertTrue(schedule.tahajjudLastThirdStart.matches(Regex("\\d{2}:\\d{2}")))
    assertTrue(schedule.hijriReadable.isNotBlank())

    val nextInfo = PrayerTimeCalculator.computeNextPrayerAndCountdown(
      schedule = schedule,
      nowMillis = cal.timeInMillis
    )
    assertEquals("Dhuhr", nextInfo.nextPrayerName)
    assertTrue(nextInfo.countdownFormatted.isNotBlank())

    // Verify offline caching in Room PrayerScheduleDao
    val db = Room.inMemoryDatabaseBuilder(context, BayanDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    db.prayerScheduleDao().insertSchedule(schedule)
    val cached = db.prayerScheduleDao().getScheduleForDateAndLocation(
      dateIso = "2026-05-15",
      locationName = "Srinagar"
    )
    assertNotNull(cached)
    assertEquals(schedule.fajrTime, cached?.fajrTime)
    assertEquals(schedule.ishaTime, cached?.ishaTime)
    db.close()
  }

  @Test
  fun `scenario 2 3 4 5 - Fajr 15 minute wake up alarm dismiss snooze and reboot rescheduling`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val updatedConfig = PrayerAndAlarmConfig(
      city = "Srinagar",
      districtOrRegion = "Srinagar District, Kashmir",
      country = "India",
      fajrWakeUpAlarmEnabled = true,
      fajrWakeUpOffsetMinutes = 20,
      tahajjudWakeUpEnabled = true,
      wakeUpLanguage = "ALL",
      progressiveSequence = true,
      alarmVolumePercent = 85,
      vibrationEnabled = true
    )
    PrayerPreferencesManager.saveConfig(context, updatedConfig)
    val loadedConfig = PrayerPreferencesManager.loadConfig(context)
    assertTrue(loadedConfig.fajrWakeUpAlarmEnabled)
    assertEquals(20, loadedConfig.fajrWakeUpOffsetMinutes)
    assertTrue(loadedConfig.tahajjudWakeUpEnabled)
    assertEquals(900, AzanAudioController.MAX_FAJR_ALARM_DURATION_SECONDS)

    // Trigger special 15-minute Fajr alarm
    AzanAudioController.startAzanOrFajrAlarm(
      context = context,
      prayerName = "Fajr",
      isSpecial15MinFajrAlarm = true,
      isTahajjudAlarm = false
    )
    val activeState = AzanAudioController.playbackState.value
    assertTrue(activeState.isActive)
    assertTrue(activeState.isFajrSpecialAlarm)
    assertEquals(900, activeState.maxDurationSeconds)
    assertTrue(activeState.arabicWakeMessage.contains("الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ"))
    assertEquals("As-salatu khayrun minan-nawm.", activeState.transliterationMessage)
    assertTrue(activeState.englishWakeMessage.contains("Prayer is better than sleep"))

    // Test Snooze (5 minutes)
    AzanAudioController.snoozeActiveAlarm(context, 5)
    assertFalse(AzanAudioController.playbackState.value.isActive)

    // Start again and test Stop
    AzanAudioController.startAzanOrFajrAlarm(
      context = context,
      prayerName = "Fajr",
      isSpecial15MinFajrAlarm = true,
      isTahajjudAlarm = false
    )
    assertTrue(AzanAudioController.playbackState.value.isActive)
    AzanAudioController.stopAlarm(context)
    assertFalse(AzanAudioController.playbackState.value.isActive)

    // Verify BootAndTimeChangeReceiver reschedules cleanly on BOOT_COMPLETED and TIMEZONE_CHANGED
    val receiver = BootAndTimeChangeReceiver()
    receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
    receiver.onReceive(context, Intent(Intent.ACTION_TIMEZONE_CHANGED))
    val diagnostics = PrayerAlarmScheduler.getSetupDiagnostics(context)
    assertNotNull(diagnostics)
    assertTrue(diagnostics.scheduledAlarmsSummary.isNotEmpty())
  }

  @Test
  fun `scenario 7 - light dark and eye comfort themes persist and switch cleanly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val base = PrayerPreferencesManager.loadConfig(context)

    PrayerPreferencesManager.saveConfig(context, base.copy(themeMode = AppThemeMode.LIGHT))
    assertEquals(AppThemeMode.LIGHT, PrayerPreferencesManager.loadConfig(context).themeMode)

    PrayerPreferencesManager.saveConfig(context, base.copy(themeMode = AppThemeMode.DARK))
    assertEquals(AppThemeMode.DARK, PrayerPreferencesManager.loadConfig(context).themeMode)

    PrayerPreferencesManager.saveConfig(context, base.copy(themeMode = AppThemeMode.EYE_COMFORT))
    assertEquals(AppThemeMode.EYE_COMFORT, PrayerPreferencesManager.loadConfig(context).themeMode)
    assertTrue(AppThemeMode.EYE_COMFORT.description.contains("Warm", ignoreCase = true))
  }

  @Test
  fun `scenario 8 9 10 - Room schema entities Sunnah com Hadith numbering separation and 6236 Quran verses`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, BayanDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    // Seed Surahs, HadithCollections, Foundational Hadiths, and Translations
    db.surahDao().insertSurahs(InitialDataLoader.getAll114Surahs())
    db.hadithCollectionDao().insertCollections(ExtendedHadithDataProvider.getHadithCollectionEntities())
    db.hadithDao().insertHadiths(InitialDataLoader.getFoundationalHadiths())
    db.translationDao().insertTranslations(
      listOf(
        TranslationEntity(
          sourceType = "QURAN",
          referenceKey = "1:1",
          surahOrBookNumber = 1,
          verseOrHadithNumber = 1,
          editionKey = "en.sahih",
          editionTitle = "Sahih International",
          languageCode = "en",
          text = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
          isVerified = true
        )
      )
    )

    val surahsInDb = db.surahDao().getAllSurahs().first()
    assertEquals(114, surahsInDb.size)

    val collectionsInDb = db.hadithCollectionDao().getAllCollections().first()
    assertEquals(ExtendedHadithDataProvider.primaryCollections.size, collectionsInDb.size)
    assertTrue(collectionsInDb.size >= 26)
    assertTrue(collectionsInDb.any { it.collectionKey == "bukhari" && it.darussalamEditionName.contains("Darussalam") })
    assertTrue(collectionsInDb.any { it.collectionKey == "ahmad" })
    assertTrue(collectionsInDb.any { it.collectionKey == "alkafi" })
    assertTrue(collectionsInDb.any { it.collectionKey == "nahjulbalagha" })

    // Verify separate Darussalam and In-Book numbering on HadithEntity
    val bukhariHadith1 = db.hadithDao().getHadith("bukhari", 1)
    assertNotNull(bukhariHadith1)
    assertTrue(bukhariHadith1!!.darussalamReference.startsWith("Darussalam:"))
    assertTrue(bukhariHadith1.inBookReference.contains("Book"))

    // Verify Fajr phrase hadith in Sunan Abu Dawud #500
    val abuDawud500 = db.hadithDao().getHadith("abudawud", 500)
    assertNotNull(abuDawud500)
    assertTrue(abuDawud500!!.arabicText.contains("الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ"))

    // Verify Sahih International translation entity in Room
    val translations = db.translationDao().getTranslationsForReference("QURAN", "1:1").first()
    assertEquals(1, translations.size)
    assertEquals("Sahih International", translations.first().editionTitle)

    // Verify all 114 surahs / 6236 verses in offline asset bundle and Bismillah handling
    var totalAssetAyahs = 0
    for (s in 1..114) {
      val surahAyahs = OfflineQuranAssetLoader.loadSurahFromAssets(context, s)
      totalAssetAyahs += surahAyahs.size
      val v1 = surahAyahs.first().textArabic
      if (s == 1) {
        assertTrue(v1.startsWith("بِسْمِ"))
      } else {
        // Surahs 2..114 (including Surah 9 At-Tawbah) must NOT have duplicate Bismillah inside Verse 1
        assertFalse("Surah $s Verse 1 should not contain duplicate prepended Bismillah", v1.startsWith("بِسْمِ"))
        assertTrue("Surah $s Verse 1 must not be empty", v1.isNotBlank())
      }
    }
    assertEquals(6236, totalAssetAyahs)
    assertEquals("الٓمٓ", OfflineQuranAssetLoader.loadSurahFromAssets(context, 2).first().textArabic)
    assertEquals("ٱلرَّحْمَٰنُ", OfflineQuranAssetLoader.loadSurahFromAssets(context, 55).first().textArabic)
    assertEquals("قُلْ هُوَ ٱللَّهُ أَحَدٌ", OfflineQuranAssetLoader.loadSurahFromAssets(context, 112).first().textArabic)

    // Verify diagnostic audit report honesty (no fabricated 100% claims) + security guard entry
    val repo = IslamicRepository(
      database = db,
      appContext = context
    )
    val audit = repo.generateDiagnosticAuditReport()
    assertTrue(audit.sourceEntries.size >= 5)
    assertEquals(114, audit.totalSurahsLocal)
    assertTrue(audit.sourceEntries.any { it.sourceName.contains("Hadith", ignoreCase = true) })
    assertTrue(audit.sourceEntries.any { it.sourceName.contains("Security", ignoreCase = true) })

    db.close()
  }

  @Test
  fun `verify 8 authentic tafsirs and 32 islamic books are available`() {
    assertEquals(14, TafsirRepository.getEditionsForTradition("SUNNI").size)
    assertEquals(8, TafsirRepository.getEditionsForTradition("SHIA").size)
    assertEquals(22, TafsirRepository.editions.size)
    val ayahs = InitialDataLoader.getFoundationalAyahs()
    val surahs = InitialDataLoader.getAll114Surahs()
    val allHadiths = ExtendedHadithDataProvider.getExtendedHadiths()
    val commentary = TafsirRepository.getTafsirForAyah(
      ayah = ayahs.first(),
      surah = surahs.first(),
      editionKey = "ibn_kathir",
      allHadiths = allHadiths
    )
    assertTrue(commentary.arabicText.isNotBlank())
    assertTrue(commentary.commentaryEnglish.isNotBlank())
    assertTrue(commentary.commentaryUrdu.isNotBlank())
    assertTrue(commentary.linkedHadiths.isNotEmpty())

    assertEquals(32, IslamicBooksCatalog.all32Books.size)
    assertTrue(IslamicBooksCatalog.all32Books.all { it.chapters.size >= 2 })
  }

  @Test
  fun `verify audio played popup dismissal and anti-ad security integrity guard`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val playerManager = com.example.ui.audio.AudioPlayerManager(context)
    assertFalse(playerManager.audioState.value.isVisible)

    // Start playing Surah 1, Ayah 1 -> popup becomes visible
    playerManager.playAyah(1, 1, "Al-Fatihah")
    assertTrue(playerManager.audioState.value.isVisible)
    assertEquals(1, playerManager.audioState.value.surahNumber)

    // Stop / Close popup -> immediately dismisses popup and clears surahNumber
    playerManager.stop()
    assertFalse(playerManager.audioState.value.isVisible)
    assertEquals(null, playerManager.audioState.value.surahNumber)
    assertFalse(playerManager.audioState.value.isPlaying)
    assertFalse(playerManager.audioState.value.isBuffering)

    // Verify SecurityIntegrityGuard blocks ad networks, cleartext HTTP, and script injections
    val guard = com.example.data.remote.SecurityIntegrityGuard
    assertTrue(guard.isUrlTrustedAndAllowed("https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-bukhari/1.min.json"))
    assertTrue(guard.isUrlTrustedAndAllowed("https://api.alquran.cloud/v1/surah/1"))
    assertFalse(guard.isUrlTrustedAndAllowed("http://api.alquran.cloud/v1/surah/1")) // Reject cleartext HTTP
    assertFalse(guard.isUrlTrustedAndAllowed("https://googleads.g.doubleclick.net/pagead/ads")) // Reject ad network
    assertFalse(guard.isUrlTrustedAndAllowed("https://untrusted-hacker-domain.org/inject.json"))

    val dirtyText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ<script>alert('ad')</script><iframe src='https://admob.com'></iframe>"
    val sanitized = guard.sanitizeSacredText(dirtyText)
    assertEquals("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", sanitized)
    assertTrue(guard.isValidQuranVersePayload(1, 1, sanitized))
    assertTrue(guard.isAuthorizedDeveloper("sheikhzulqar@gmail.com"))
    assertFalse(guard.isAuthorizedDeveloper("intruder@example.com"))
  }

  @Test
  fun `verify SourceRegistry Room entity schema DAO and repository seeding`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val inMemoryDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    try {
      val dao = inMemoryDb.sourceRegistryDao()
      val repo = SourceRegistryRepository(dao)

      // Initial count should be 0
      assertEquals(0, dao.getSourceCount())

      // Test seeding default authoritative sources
      repo.ensureDefaultSourcesSeeded()
      val sources = dao.getAllSourcesSnapshot()
      assertTrue(sources.size >= 5)

      // Verify specific sources and required schema fields:
      // sourceId, title, author, tradition, language, translationLanguages, officialUrl,
      // datasetLocation, license, lastSyncTime, totalAvailableRecords, totalImportedRecords,
      // duplicateCount, invalidCount, verificationStatus (Enum), and importStatus (Enum)
      val quranSrc = dao.getSourceByIdSnapshot("quran_uthmani_tanzil")
      assertNotNull(quranSrc)
      assertEquals("quran_uthmani_tanzil", quranSrc!!.sourceId)
      assertEquals("Tanzil & Quran Foundation Uthmani Text", quranSrc.title)
      assertTrue(quranSrc.author.contains("Zayd ibn Thabit"))
      assertEquals("Quran al-Kareem (Universal Tawatur)", quranSrc.tradition)
      assertEquals("Classical Arabic (العربية الفصحى)", quranSrc.language)
      assertTrue(quranSrc.translationLanguages.contains("English") && quranSrc.translationLanguages.contains("Urdu"))
      assertTrue(quranSrc.officialUrl.contains("tanzil.net"))
      assertTrue(quranSrc.datasetLocation.contains("quran_complete_v1.dat"))
      assertTrue(quranSrc.license.contains("Open Source"))
      assertTrue(quranSrc.lastSyncTime > 0)
      assertEquals(6236, quranSrc.totalAvailableRecords)
      assertEquals(6236, quranSrc.totalImportedRecords)
      assertEquals(0, quranSrc.duplicateCount)
      assertEquals(0, quranSrc.invalidCount)
      assertEquals(VerificationStatus.VERIFIED, quranSrc.verificationStatus)
      assertEquals(ImportStatus.SUCCESS, quranSrc.importStatus)

      val sunnahSrc = dao.getSourceByIdSnapshot("sunnah_kutub_sittah")
      assertNotNull(sunnahSrc)
      assertEquals("Sunnah.com / Kutub al-Sittah Hadith Archive", sunnahSrc!!.title)
      assertEquals("Sunni / Ahl al-Sunnah wal-Jama'ah", sunnahSrc.tradition)
      assertEquals(VerificationStatus.PARTIALLY_VERIFIED, sunnahSrc.verificationStatus)
      assertEquals(38500, sunnahSrc.totalAvailableRecords)
      assertEquals(140, sunnahSrc.totalImportedRecords)
      assertEquals(ImportStatus.SUCCESS, sunnahSrc.importStatus)

      val aladhanSrc = dao.getSourceByIdSnapshot("aladhan_prayer_engine")
      assertNotNull(aladhanSrc)
      assertEquals(VerificationStatus.VERIFIED, aladhanSrc!!.verificationStatus)
      assertTrue(aladhanSrc.officialUrl.contains("aladhan.com"))

      val fiqhSrc = dao.getSourceByIdSnapshot("fiqh_fatwa_registry")
      assertNotNull(fiqhSrc)
      assertTrue(fiqhSrc!!.title.contains("Islamic Fiqh Academy"))
      assertEquals(VerificationStatus.PARTIALLY_VERIFIED, fiqhSrc.verificationStatus)

      // Test updating sync stats
      dao.updateSyncStats(
        sourceId = "sunnah_kutub_sittah",
        totalImported = 250,
        importStatus = ImportStatus.SUCCESS
      )
      val updatedSunnah = dao.getSourceByIdSnapshot("sunnah_kutub_sittah")
      assertEquals(250, updatedSunnah!!.totalImportedRecords)
      assertEquals(ImportStatus.SUCCESS, updatedSunnah.importStatus)

      // Test updating verification status enum
      dao.updateVerificationStatus("sunnah_kutub_sittah", VerificationStatus.VERIFIED)
      val verifiedSunnah = dao.getSourceByIdSnapshot("sunnah_kutub_sittah")
      assertEquals(VerificationStatus.VERIFIED, verifiedSunnah!!.verificationStatus)

      // Test reactive Flow emissions
      val flowList = dao.getAllSources().first()
      assertTrue(flowList.any { it.sourceId == "quran_uthmani_tanzil" })
    } finally {
      inMemoryDb.close()
    }
  }

  @Test
  fun `verify Quran Reader Reading Mode state transitions and canonical text preservation`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.viewmodels.BayanViewModel(application)

    // Initially Reading Mode and Continuous Mushaf Flow are false
    assertFalse(viewModel.uiState.value.isReadingMode)
    assertFalse(viewModel.uiState.value.isContinuousMushafFlow)

    // Open Surah Ar-Ra'd (Surah 13) in reader
    val surahArRad = InitialDataLoader.getAll114Surahs().first { it.number == 13 }
    viewModel.openSurahReader(surahArRad)
    assertTrue(viewModel.uiState.value.isReaderOpen)
    assertEquals(13, viewModel.uiState.value.selectedSurah?.number)

    // Toggle Reading Mode ON
    viewModel.toggleReadingMode()
    assertTrue(viewModel.uiState.value.isReadingMode)

    // Toggle Continuous Mushaf Flow ON and OFF
    viewModel.toggleContinuousMushafFlow()
    assertTrue(viewModel.uiState.value.isContinuousMushafFlow)
    viewModel.toggleContinuousMushafFlow()
    assertFalse(viewModel.uiState.value.isContinuousMushafFlow)

    // Verify Surah Ar-Ra'd 13:1 canonical text and dagger alif (\u0670) remain untouched
    val arRadAyahs = OfflineQuranAssetLoader.loadSurahFromAssets(application, 13)
    val arRadAyah1 = arRadAyahs.first { it.verseNumber == 1 }
    assertTrue(arRadAyah1.textArabic.startsWith("الٓمٓر"))
    assertTrue(arRadAyah1.textArabic.contains('\u0670'))

    // Closing Surah Reader resets Reading Mode
    viewModel.closeSurahReader()
    assertFalse(viewModel.uiState.value.isReaderOpen)
    assertFalse(viewModel.uiState.value.isReadingMode)
  }

  @Test
  fun `verify Qibla compass geodesic calculation and accelerometer magnetometer sensor fusion`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Verify Srinagar, Kashmir (34.0837, 74.7973) Qibla bearing (~256.82° WSW) and distance (~3695 km)
    val srinagarBearing = com.example.data.prayer.QiblaCompassCalculator.calculateQiblaBearing(34.0837, 74.7973)
    assertTrue("Expected ~256.82 deg but got $srinagarBearing", srinagarBearing in 256.0..257.5)
    assertEquals("WSW", com.example.data.prayer.QiblaCompassCalculator.formatCardinalDirection(srinagarBearing))

    val srinagarDistanceKm = com.example.data.prayer.QiblaCompassCalculator.calculateDistanceToKaabaKm(34.0837, 74.7973)
    assertTrue("Expected ~3695 km but got $srinagarDistanceKm", srinagarDistanceKm in 3650.0..3750.0)

    // 2. Verify sensor fusion controller with flat accelerometer vector [0, 0, 9.81] and magnetometer vector
    val controller = com.example.data.prayer.QiblaSensorController(
      context = context,
      initialLatitude = 34.0837,
      initialLongitude = 74.7973,
      initialLocationLabel = "Srinagar, Kashmir"
    )
    // Gravity pointing straight down (+Z on flat phone), magnetic field pointing North (+Y)
    controller.processSensorVectors(
      accelerometerValues = floatArrayOf(0f, 0f, 9.81f),
      magnetometerValues = floatArrayOf(0f, 42f, -20f),
      smooth = false
    )
    assertTrue(controller.state.value.hasLiveFix)
    assertTrue(controller.state.value.isDeviceHeldFlat)
    assertFalse(controller.state.value.isMagneticInterferenceDetected)

    // 3. Verify aligning heading with exact Qibla bearing triggers isAlignedWithQibla
    controller.setManualHeadingOverride(srinagarBearing.toFloat())
    assertTrue(controller.state.value.isAlignedWithQibla)
    assertEquals(0f, controller.state.value.signedAlignmentDeltaDeg, 0.05f)
  }

  @Test
  fun `verify 8 persistent themes Alafasy removal Sunni Shia reciters and Sunni Shia Hadith libraries`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val base = PrayerPreferencesManager.loadConfig(context)

    // 1. Verify all 8 persistent themes
    assertEquals(8, AppThemeMode.entries.size)
    val expectedLabels = listOf(
      "System Default",
      "Light",
      "Dark",
      "Pure Black (AMOLED)",
      "Warm Paper",
      "Sepia",
      "Green",
      "Blue"
    )
    assertEquals(expectedLabels, AppThemeMode.entries.map { it.label })
    for (mode in AppThemeMode.entries) {
      PrayerPreferencesManager.saveConfig(context, base.copy(themeMode = mode))
      assertEquals(mode, PrayerPreferencesManager.loadConfig(context).themeMode)
    }

    // 2. Verify Mishari Rashid Alafasy is completely removed from all reciter lists and download catalogues
    val allReciters = com.example.ui.audio.availableReciters
    assertFalse(allReciters.any { it.id.contains("alafasy", ignoreCase = true) || it.name.contains("Alafasy", ignoreCase = true) })
    assertFalse(com.example.data.download.OfflineCatalogRegistry.audioReciters.any {
      it.id.contains("alafasy", ignoreCase = true) || it.displayName.contains("Alafasy", ignoreCase = true)
    })

    // 3. Verify Sunni and Shia reciter sections and honest availability notes
    val sunniReciters = allReciters.filter { it.tradition == "SUNNI" }
    val shiaReciters = allReciters.filter { it.tradition == "SHIA" }
    assertTrue(sunniReciters.size >= 5)
    assertTrue(shiaReciters.size >= 2)
    assertTrue(sunniReciters.any { it.name.contains("Husary") })
    assertTrue(shiaReciters.any { it.name.contains("Karim Mansouri") })
    assertTrue(shiaReciters.any { it.name.contains("Maytham al-Tammar") })
    assertEquals(3, com.example.ui.audio.reciterAvailabilityNotes.size)

    // 4. Verify separate Sunni and Shia Hadith libraries and newly catalogued works
    val sunniCollections = ExtendedHadithDataProvider.sunniCollections
    val shiaCollections = ExtendedHadithDataProvider.shiaCollections
    assertTrue(sunniCollections.size >= 24)
    assertTrue(shiaCollections.size >= 10)
    assertTrue(sunniCollections.all { it.tradition == "SUNNI" })
    assertTrue(shiaCollections.all { it.tradition == "SHIA" })
    assertTrue(sunniCollections.any { it.key == "daraqutni" })
    assertTrue(sunniCollections.any { it.key == "bayhaqi" })
    assertTrue(sunniCollections.any { it.key == "sunni_commentaries" && it.categoryGroup == "SUNNI_COMMENTARY" })
    assertTrue(shiaCollections.any { it.key == "alkafi" && it.categoryGroup == "AL_KUTUB_AL_ARBAA" })
    assertTrue(shiaCollections.any { it.key == "wasail" })
    assertTrue(shiaCollections.any { it.key == "sajjadiyya" })
    assertTrue(shiaCollections.any { it.key == "saduq_corpus" })
    assertTrue(shiaCollections.any { it.key == "zaydi_ismaili" && it.categoryGroup == "SHIA_ZAYDI_ISMAILI" })

    // 5. Verify Developer Maintenance View gate only authorizes sheikhzulqar@gmail.com
    assertTrue(com.example.data.remote.SecurityIntegrityGuard.isAuthorizedDeveloper("sheikhzulqar@gmail.com"))
    assertFalse(com.example.data.remote.SecurityIntegrityGuard.isAuthorizedDeveloper("guest@example.com"))
    assertFalse(com.example.data.remote.SecurityIntegrityGuard.isAuthorizedDeveloper(null))
  }

  @Test
  fun `verify Daily Duas Special Occasions Friday Bath Reminder and Necessary Sunnah Reminders`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertEquals("duas", BayanNavRoutes.DUAS)

    // 1. Verify Masnoon Duas & Special Occasions Duas data integrity
    val allDuas = com.example.data.model.DailyDuasDataProvider.allDuas
    assertTrue(allDuas.size >= 25)
    assertTrue(allDuas.all {
      it.arabicText.isNotBlank() &&
        it.transliteration.isNotBlank() &&
        it.englishTranslation.isNotBlank() &&
        it.urduTranslation.isNotBlank() &&
        it.authenticReference.isNotBlank()
    })

    val masnoonDuas = allDuas.filter { it.category != com.example.data.model.DuaCategory.SPECIAL_OCCASIONS }
    val specialOccasionDuas = allDuas.filter { it.category == com.example.data.model.DuaCategory.SPECIAL_OCCASIONS }
    assertTrue(masnoonDuas.size >= 14)
    assertTrue(specialOccasionDuas.size >= 11)
    assertTrue(specialOccasionDuas.any { it.occasionType == com.example.data.model.SpecialOccasionType.JUMUAH_FRIDAY })
    assertTrue(specialOccasionDuas.any { it.occasionType == com.example.data.model.SpecialOccasionType.RAMADAN_IFTAR })
    assertTrue(specialOccasionDuas.any { it.occasionType == com.example.data.model.SpecialOccasionType.LAYLATUL_QADR })
    assertTrue(specialOccasionDuas.any { it.occasionType == com.example.data.model.SpecialOccasionType.SAFAR_TRAVEL })
    assertTrue(specialOccasionDuas.any { it.occasionType == com.example.data.model.SpecialOccasionType.ISTIKHARAH })

    // 2. Verify Friday Bath (Jumu'ah Ghusl) Reminder & Necessary Sunnah Reminders
    val reminders = com.example.data.model.DailyDuasDataProvider.defaultNecessaryReminders
    assertTrue(reminders.size >= 8)
    val fridayBathSpec = reminders.firstOrNull { it.id == "rem_friday_bath_ghusl" }
    assertNotNull(fridayBathSpec)
    assertTrue(fridayBathSpec!!.title.contains("Friday Bath", ignoreCase = true))
    assertEquals(com.example.data.model.ReminderFrequency.WEEKLY_FRIDAY, fridayBathSpec.frequency)

    val updatedState = com.example.data.prayer.SunnahReminderManager.setFridayBathReminder(
      context = context,
      enabled = true,
      time24h = "09:00"
    )
    assertTrue(updatedState.fridayBathReminderEnabled)
    assertEquals("09:00", updatedState.fridayBathReminderTime24h)

    val afterCheck = com.example.data.prayer.SunnahReminderManager.toggleFridayChecklistItem(
      context = context,
      itemId = "friday_ghusl_bath"
    )
    assertTrue(afterCheck.completedFridayChecklistIds.contains("friday_ghusl_bath"))

    val afterSalawat = com.example.data.prayer.SunnahReminderManager.incrementFridaySalawat(context, 10)
    assertEquals(10, afterSalawat.fridaySalawatCount)

    val scheduledSummary = com.example.data.prayer.SunnahReminderManager.scheduleAllSunnahReminders(context)
    assertTrue(scheduledSummary.any { it.contains("Friday Bath", ignoreCase = true) })
  }

  @Test
  fun `verify offline Adhan alarm recordings 100 percent default volume preview test keep and lock screen playback`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Verify default 100% hardware-honest volume (0%..100% range, no false >100% software claims)
    val defaultConfig = PrayerAndAlarmConfig()
    assertEquals(100, defaultConfig.alarmVolumePercent)
    assertTrue(defaultConfig.boostSystemAlarmStream)
    assertTrue(defaultConfig.vibrationEnabled)
    assertTrue(defaultConfig.continuousVibration)
    assertEquals(0, AzanAudioController.computeLoudnessGainMb(100))

    // Persist and reload custom volume (e.g. 85%) and favourite Adhan
    val customConfig = defaultConfig.copy(
      alarmVolumePercent = 85,
      selectedMuezzinId = com.example.data.prayer.MuezzinAudioOption.MADINAH_NABAWI.id
    )
    PrayerPreferencesManager.saveConfig(context, customConfig)
    val saved = PrayerPreferencesManager.loadConfig(context)
    assertEquals(85, saved.alarmVolumePercent)
    assertEquals(com.example.data.prayer.MuezzinAudioOption.MADINAH_NABAWI.id, saved.selectedMuezzinId)

    // Save favourite Muezzin and reset back to 100% default
    val favSaved = AzanAudioController.selectAndSaveFavouriteMuezzin(
      context,
      com.example.data.prayer.MuezzinAudioOption.MAKKAH_HARAM
    )
    assertEquals(com.example.data.prayer.MuezzinAudioOption.MAKKAH_HARAM.id, favSaved.selectedMuezzinId)
    AzanAudioController.savePreferredAlarmVolume(context, 100)
    assertEquals(100, PrayerPreferencesManager.loadConfig(context).alarmVolumePercent)

    // 2. Verify all 14 genuine pure-vocal Adhans are stored locally offline in assets/adhan/<id>.wav
    val muezzins = com.example.data.prayer.MuezzinAudioOption.entries
    assertEquals(14, muezzins.size)
    assertTrue(muezzins.all { it.subtitle.contains("No Music", ignoreCase = true) })
    assertTrue(muezzins.any { it.title.contains("Masjid an-Nabawi", ignoreCase = true) || it.originMasjid.contains("Masjid an-Nabawi", ignoreCase = true) })
    assertTrue(muezzins.any { it.title.contains("Ali Ahmed Mulla") })
    assertTrue(muezzins.any { it.title.contains("Essam Bukhari") })
    assertTrue(muezzins.any { it.tradition == "SHIA" })

    val offlineVerification = AzanAudioController.verifyAllOfflineAdhanRecordings(context)
    assertEquals(14, offlineVerification.size)
    assertTrue(
      "All 14 Adhan recordings must exist locally in assets/adhan/ with non-zero bytes",
      offlineVerification.values.all { it }
    )

    // 3. Preview, Test, and Keep offline Adhan playback
    AzanAudioController.savePreferredAlarmVolume(context, 90)
    val madinahAdhan = com.example.data.prayer.MuezzinAudioOption.MADINAH_NABAWI
    AzanAudioController.previewMuezzinAzan(context, madinahAdhan)
    val previewState = AzanAudioController.playbackState.value
    assertTrue(previewState.isActive)
    assertTrue(previewState.isPreviewMode)
    assertTrue(previewState.isPlayingOfflineLocalAsset)
    assertEquals(madinahAdhan.id, previewState.activeMuezzinId)
    assertEquals(90, previewState.currentVolumePercent)

    // Full Alarm Test at 100% + continuous vibration
    AzanAudioController.savePreferredAlarmVolume(context, 100)
    val abdulBasit = com.example.data.prayer.MuezzinAudioOption.EGYPT_ABDUL_BASIT
    AzanAudioController.testMuezzinAzan(context, abdulBasit)
    val stateAfterTest = AzanAudioController.playbackState.value
    assertTrue(stateAfterTest.isActive)
    assertFalse(stateAfterTest.isPreviewMode)
    assertTrue(stateAfterTest.isPlayingOfflineLocalAsset)
    assertEquals(abdulBasit.id, stateAfterTest.activeMuezzinId)
    assertEquals(100, stateAfterTest.currentVolumePercent)
    assertTrue(stateAfterTest.isContinuousVibrationActive)
    assertTrue(stateAfterTest.vibrationPulseCount >= 1)

    // Hardware alarm volume status check
    val hwStatus = AzanAudioController.getHardwareAlarmVolumeStatus(context)
    assertTrue(hwStatus.maxSteps > 0)
    assertTrue(hwStatus.guidanceMessage.isNotBlank())

    // Stop active alarm -> stops both audio and continuous vibration
    AzanAudioController.stopAlarm(context)
    assertFalse(AzanAudioController.playbackState.value.isActive)
    assertFalse(AzanAudioController.playbackState.value.isContinuousVibrationActive)

    // 4. Test standalone continuous vibration test start & stop
    AzanAudioController.startContinuousVibrationTest(context)
    val vibTestState = AzanAudioController.playbackState.value
    assertFalse(vibTestState.isActive)
    assertTrue(vibTestState.isContinuousVibrationActive)
    assertTrue(vibTestState.isStandaloneVibrationTest)
    assertTrue(vibTestState.vibrationPulseCount >= 1)

    AzanAudioController.stopContinuousVibration(context)
    assertFalse(AzanAudioController.playbackState.value.isContinuousVibrationActive)
    assertFalse(AzanAudioController.playbackState.value.isStandaloneVibrationTest)
  }

  @Test
  fun `verify Friday and all other days calculation notification dispatch and Home Screen state`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val tz = TimeZone.getTimeZone("Asia/Kolkata")

    // 1. Verify all 7 days of the week (Friday, Saturday, Sunday, Monday, Tuesday, Wednesday, Thursday) have authentic Sunnah specs
    val weekdaySpecs = com.example.data.model.DailyDuasDataProvider.weekdaySunnahSpecs
    assertEquals(7, weekdaySpecs.size)
    val expectedDays = setOf(
      Calendar.FRIDAY,
      Calendar.SATURDAY,
      Calendar.SUNDAY,
      Calendar.MONDAY,
      Calendar.TUESDAY,
      Calendar.WEDNESDAY,
      Calendar.THURSDAY
    )
    assertEquals(expectedDays, weekdaySpecs.map { it.calendarDayOfWeek }.toSet())
    assertTrue(weekdaySpecs.all {
      it.primaryReminderTitle.isNotBlank() &&
        it.arabicHadithSnippet.isNotBlank() &&
        it.englishGuidance.isNotBlank() &&
        it.urduGuidance.isNotBlank() &&
        it.authenticReference.isNotBlank() &&
        it.keySunnahBullets.isNotEmpty()
    })

    // 2. Calculate on a Friday (e.g. Friday, 23 Oct 2026)
    val fridayCal = Calendar.getInstance(tz).apply {
      set(2026, Calendar.OCTOBER, 23, 9, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val fridayStatus = com.example.data.prayer.SunnahReminderManager.calculateDayAndWeekStatus(
      nowMillis = fridayCal.timeInMillis,
      timeZone = tz,
      fridayBathTime24h = "09:30"
    )
    assertTrue(fridayStatus.isFridayToday)
    assertEquals(Calendar.FRIDAY, fridayStatus.todayDayOfWeek)
    assertEquals(0, fridayStatus.daysUntilNextFriday)
    assertTrue(fridayStatus.nextFridayDateReadable.contains("Today is Blessed Friday", ignoreCase = true))
    assertEquals(7, fridayStatus.upcoming7DaysSchedule.size)
    assertTrue(fridayStatus.upcoming7DaysSchedule.first().isToday)
    assertTrue(fridayStatus.upcoming7DaysSchedule.first().isFridayJumuah)
    assertTrue(fridayStatus.notificationTitle.contains("Jumu'ah Mubarak", ignoreCase = true))

    // 3. Calculate on a Monday (e.g. Monday, 26 Oct 2026) -> 4 days until Friday, Monday Sunnah Fast active
    val mondayCal = Calendar.getInstance(tz).apply {
      set(2026, Calendar.OCTOBER, 26, 8, 30, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val mondayStatus = com.example.data.prayer.SunnahReminderManager.calculateDayAndWeekStatus(
      nowMillis = mondayCal.timeInMillis,
      timeZone = tz,
      fridayBathTime24h = "09:30"
    )
    assertFalse(mondayStatus.isFridayToday)
    assertEquals(Calendar.MONDAY, mondayStatus.todayDayOfWeek)
    assertEquals(4, mondayStatus.daysUntilNextFriday)
    assertTrue(mondayStatus.isSunnahFastingDayToday)
    assertTrue(mondayStatus.nextSunnahFastSummary.contains("Monday Sunnah Fast", ignoreCase = true))
    assertEquals(7, mondayStatus.upcoming7DaysSchedule.size)

    // 4. Calculate on a Thursday (e.g. Thursday, 29 Oct 2026) -> 1 day until Friday (Tomorrow is Blessed Friday)
    val thursdayCal = Calendar.getInstance(tz).apply {
      set(2026, Calendar.OCTOBER, 29, 10, 0, 0)
      set(Calendar.MILLISECOND, 0)
    }
    val thursdayStatus = com.example.data.prayer.SunnahReminderManager.calculateDayAndWeekStatus(
      nowMillis = thursdayCal.timeInMillis,
      timeZone = tz,
      fridayBathTime24h = "09:30"
    )
    assertEquals(Calendar.THURSDAY, thursdayStatus.todayDayOfWeek)
    assertTrue(thursdayStatus.isThursdayJumuahEve)
    assertEquals(1, thursdayStatus.daysUntilNextFriday)
    assertTrue(thursdayStatus.nextFridayDateReadable.contains("Tomorrow is Blessed Friday", ignoreCase = true))

    // 5. Verify Android Notification dispatch for Friday and other calculated days
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val shadowNm = org.robolectric.Shadows.shadowOf(nm)
    nm.cancelAll()

    val stateAfterFridayNotif = com.example.data.prayer.SunnahReminderManager.postCalculatedDayReminderNotification(
      context = context,
      dayOfWeekOverride = Calendar.FRIDAY,
      nowMillis = fridayCal.timeInMillis
    )
    assertEquals(Calendar.FRIDAY, stateAfterFridayNotif.selectedPreviewDayOfWeek)
    assertTrue(stateAfterFridayNotif.lastNotificationBannerMessage.contains("Jumu'ah Mubarak", ignoreCase = true))
    assertTrue("Expected notification to be posted in NotificationManager", shadowNm.allNotifications.isNotEmpty())

    // 6. Verify PrayerAlarmReceiver handles ACTION_SUNNAH_REMINDER_ALARM for rem_calculated_daily_day_sunnah
    nm.cancelAll()
    val alarmReceiver = com.example.ui.prayer.PrayerAlarmReceiver()
    val calcAlarmIntent = Intent(context, com.example.ui.prayer.PrayerAlarmReceiver::class.java).apply {
      action = com.example.data.prayer.SunnahReminderManager.ACTION_SUNNAH_REMINDER_ALARM
      putExtra(com.example.data.prayer.SunnahReminderManager.EXTRA_REMINDER_ID, "rem_calculated_daily_day_sunnah")
    }
    alarmReceiver.onReceive(context, calcAlarmIntent)
    assertTrue("Expected PrayerAlarmReceiver to post calculated day notification", shadowNm.allNotifications.isNotEmpty())
  }

  @Test
  fun `verify Shia and Sunni Hadith and Book libraries pagination Entry Time Retry Coming Soon labels and zero Darussalam in Shia section`() {
    // 1. Verify Hadith library integrity across all Sunni & Shia collections
    val integrityReport = ExtendedHadithDataProvider.validateHadithLibraryIntegrity()
    assertTrue(
      "Expected valid Hadith library integrity report, got duplicates=${integrityReport.duplicateIds}, invalidNums=${integrityReport.invalidNumberIds}, crossContam=${integrityReport.gradingCrossContaminationIds}",
      integrityReport.isValid
    )

    val sunniCols = ExtendedHadithDataProvider.sunniCollections
    val shiaCols = ExtendedHadithDataProvider.shiaCollections
    assertTrue("Expected at least 10 Sunni collections", sunniCols.size >= 10)
    assertTrue("Expected at least 6 Shia collections", shiaCols.size >= 6)

    // 2. Verify zero Darussalam labels, badges, or gradings anywhere in Shia Hadith collections
    val allHadiths = ExtendedHadithDataProvider.getExtendedHadiths()
    val shiaHadiths = allHadiths.filter {
      ExtendedHadithDataProvider.isShiaCollection(it.collectionKey)
    }
    assertTrue("Expected multiple Shia Hadiths across all Shia collections", shiaHadiths.size >= 14)
    shiaHadiths.forEach { h ->
      val primaryBadge = ExtendedHadithDataProvider.formatPrimaryEditionBadge(h)
      val inBookBadge = ExtendedHadithDataProvider.formatInBookBadge(h)
      val methodologyLabel = ExtendedHadithDataProvider.formatGradingMethodologyLabel(h)
      assertFalse(
        "Shia primary badge must never contain Darussalam: $primaryBadge",
        primaryBadge.contains("Darussalam", ignoreCase = true)
      )
      assertFalse(
        "Shia in-book badge must never contain Darussalam: $inBookBadge",
        inBookBadge.contains("Darussalam", ignoreCase = true)
      )
      assertFalse(
        "Shia grading methodology must never contain Darussalam: $methodologyLabel",
        methodologyLabel.contains("Darussalam", ignoreCase = true)
      )
      assertFalse(
        "Shia grade string must never contain Darussalam: ${h.grade}",
        h.grade.contains("Darussalam", ignoreCase = true)
      )
      val entryTime = ExtendedHadithDataProvider.getEntryTimestampForHadith(h)
      assertTrue("Entry Time must be non-blank for ${h.collectionKey}:${h.hadithNumber}", entryTime.isNotBlank())
    }

    // 3. Verify Sunni Hadiths properly display Darussalam where applicable
    val bukhariHadith = allHadiths.first { it.collectionKey == "bukhari" }
    assertTrue(
      ExtendedHadithDataProvider.formatPrimaryEditionBadge(bukhariHadith).contains("Darussalam", ignoreCase = true)
    )

    // 4. Verify "English Translation Coming Soon" and "Urdu Translation Coming Soon" fallback formatters
    assertEquals(
      "English Translation Coming Soon",
      ExtendedHadithDataProvider.formatEnglishTranslation("")
    )
    assertEquals(
      "English Translation Coming Soon",
      ExtendedHadithDataProvider.formatEnglishTranslation("Not available in this source")
    )
    assertEquals(
      "Urdu Translation Coming Soon",
      ExtendedHadithDataProvider.formatUrduTranslation("")
    )
    assertEquals(
      "Urdu Translation Coming Soon",
      ExtendedHadithDataProvider.formatUrduTranslation("Not available in this source")
    )
    assertEquals(
      "English Translation Coming Soon",
      com.example.data.model.IslamicBooksCatalog.formatBookEnglishContent("")
    )
    assertEquals(
      "Urdu Translation Coming Soon",
      com.example.data.model.IslamicBooksCatalog.formatBookUrduContent("")
    )

    // 5. Verify Sunni (37) and Shia (10) Classical & History Books catalog (47 total, 11 History books with all 12 Jilds) & zero Darussalam in Shia books
    assertEquals(32, com.example.data.model.IslamicBooksCatalog.all32Books.size)
    assertEquals(37, com.example.data.model.IslamicBooksCatalog.allSunniBooks.size)
    assertEquals(10, com.example.data.model.IslamicBooksCatalog.allShiaBooks.size)
    assertEquals(47, com.example.data.model.IslamicBooksCatalog.allSunniAndShiaBooks.size)
    assertEquals(11, com.example.data.model.IslamicBooksCatalog.historyBooks.size)
    com.example.data.model.IslamicBooksCatalog.historyBooks.forEach { histBook ->
      assertTrue("Expected History book ${histBook.titleEnglish} to have at least 12 Jilds/sections", histBook.chapters.size >= 12)
      histBook.chapters.forEach { ch ->
        assertTrue(ch.arabicExcerpt.isNotBlank())
        assertTrue(ch.englishContent.isNotBlank() && !ch.englishContent.contains("Coming Soon"))
        assertTrue(ch.urduContent.isNotBlank() && !ch.urduContent.contains("Coming Soon"))
      }
    }
    ExtendedHadithDataProvider.shiaCollections.forEach { shiaMeta ->
      val collHadiths = allHadiths.filter { it.collectionKey == shiaMeta.key }
      val coveredJilds = collHadiths.map { it.bookNumber }.toSet()
      for (jild in 1..shiaMeta.totalBooksCount) {
        assertTrue("Expected Shia collection ${shiaMeta.key} to cover Jild $jild", jild in coveredJilds)
      }
      collHadiths.forEach { h ->
        assertTrue(h.arabicText.isNotBlank())
        assertTrue(h.englishText.isNotBlank())
        assertTrue(h.urduText.isNotBlank() && !h.urduText.contains("Coming Soon"))
      }
    }
    val qadisiyyahMatches = com.example.data.model.IslamicBooksCatalog.searchInsideBooks(
      booksToSearch = com.example.data.model.IslamicBooksCatalog.historyBooks,
      rawQuery = "Qadisiyyah"
    )
    assertTrue("Expected Qadisiyyah search inside History books to return matches", qadisiyyahMatches.isNotEmpty())
    val hurrahMatches = com.example.data.model.IslamicBooksCatalog.searchInsideBooks(
      booksToSearch = com.example.data.model.IslamicBooksCatalog.historyBooks,
      rawQuery = "Waqia Hurrah"
    )
    assertTrue("Expected Waqia Hurrah search inside History books to return matches", hurrahMatches.isNotEmpty())
    val yarmoukMatches = com.example.data.model.IslamicBooksCatalog.searchInsideBooks(
      booksToSearch = com.example.data.model.IslamicBooksCatalog.historyBooks,
      rawQuery = "Battle of Yarmouk"
    )
    assertTrue("Expected Battle of Yarmouk search inside History books to return matches", yarmoukMatches.isNotEmpty())
    com.example.data.model.IslamicBooksCatalog.shiaClassicalBooks.forEach { shiaBook ->
      assertEquals("SHIA", shiaBook.tradition)
      assertFalse(shiaBook.numberingSystem.contains("Darussalam", ignoreCase = true))
      assertFalse(shiaBook.gradingMethodology.contains("Darussalam", ignoreCase = true))
      assertTrue(shiaBook.officialSourceUrl.startsWith("https://"))
      assertTrue(shiaBook.entryTimestamp.isNotBlank())
      assertTrue(shiaBook.chapters.isNotEmpty())
    }

    // 6. Verify official source links registry covers both Sunni and Shia traditions
    val links = ExtendedHadithDataProvider.verifiedOfficialSourceLinks
    assertTrue(links.size >= 9)
    assertTrue(links.any { it.tradition == "SUNNI" && it.officialUrl.contains("sunnah.com") })
    assertTrue(links.any { it.tradition == "SHIA" && it.officialUrl.contains("thaqalayn.net") })
    assertTrue(links.any { it.tradition == "SHIA" && it.officialUrl.contains("al-islam.org") })
    assertTrue(links.all { it.officialUrl.startsWith("http") && it.apiOrRepoUrl.startsWith("http") })

    // 7. Verify Shia Library Smart Offline Search (diacritic-insensitive Arabic, Urdu, multi-word English, cross-collection & ordinal #)
    val alkafiLocal = allHadiths.filter { it.collectionKey == "alkafi" }
    val arabicUnvoweledMatches = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = alkafiLocal,
      rawQuery = "العقل",
      activeCollectionKey = "alkafi",
      traditionFilter = "SHIA",
      includeCrossCollectionMatches = true
    )
    assertTrue("Unvoweled Arabic search 'العقل' should match Shia Hadiths", arabicUnvoweledMatches.isNotEmpty())

    val crossCollectionShiaMatches = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = alkafiLocal,
      rawQuery = "Imam Rida",
      activeCollectionKey = "alkafi",
      traditionFilter = "SHIA",
      includeCrossCollectionMatches = true
    )
    assertTrue("Searching 'Imam Rida' from Al-Kafi tab in Shia mode should find Uyun Akhbar al-Rida", crossCollectionShiaMatches.any { it.collectionKey == "uyun_akhbar_rida" })

    val urduShiaMatches = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = alkafiLocal,
      rawQuery = "عقل",
      activeCollectionKey = "alkafi",
      traditionFilter = "SHIA",
      includeCrossCollectionMatches = true
    )
    assertTrue("Urdu search 'عقل' should match Shia Hadiths", urduShiaMatches.isNotEmpty())

    val exactKafi201Match = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = alkafiLocal,
      rawQuery = "201",
      activeCollectionKey = "alkafi",
      traditionFilter = "SHIA",
      includeCrossCollectionMatches = false
    )
    assertEquals("Searching #201 in Al-Kafi should resolve Hadith #201 strictly by hadithNumber", 1, exactKafi201Match.size)
    assertEquals(201, exactKafi201Match.first().hadithNumber)
  }

  @Test
  fun `scenario 15 - strict Hadith number lookup per book and Audio URL plus green highlight for Fatiha Baqarah Maidah Yasin across all reciters`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // 1. Verify Hadith "Find #" strictly matches inside the active book on hadithNumber (never array index or another book)
    val allCatalog = ExtendedHadithDataProvider.getExtendedHadiths()

    // Tirmidhi + 500 -> Tirmidhi 500
    val tirmidhi500 = ExtendedHadithDataProvider.findExactHadithInCollection(
      localCollectionHadiths = allCatalog.filter { it.collectionKey == "tirmidhi" },
      collectionKey = "tirmidhi",
      hadithNumber = 500
    )
    assertNotNull("Tirmidhi #500 must be found in Tirmidhi", tirmidhi500)
    assertEquals("tirmidhi", tirmidhi500!!.collectionKey)
    assertEquals(500, tirmidhi500.hadithNumber)

    // Bukhari + 10 -> Bukhari 10
    val bukhari10 = ExtendedHadithDataProvider.findExactHadithInCollection(
      localCollectionHadiths = allCatalog.filter { it.collectionKey == "bukhari" },
      collectionKey = "bukhari",
      hadithNumber = 10
    )
    assertNotNull("Bukhari #10 must be found in Bukhari", bukhari10)
    assertEquals("bukhari", bukhari10!!.collectionKey)
    assertEquals(10, bukhari10.hadithNumber)

    // Check #1 across Muslim, Abu Dawud, Tirmidhi, Nasa'i, Ibn Majah, Muwatta, Al-Kafi, and Nahj al-Balagha
    val booksToTestNumber1 = listOf("bukhari", "muslim", "abudawud", "tirmidhi", "nasai", "ibnmajah", "malik", "alkafi", "nahjulbalagha")
    for (bookKey in booksToTestNumber1) {
      val match1 = ExtendedHadithDataProvider.findExactHadithInCollection(
        localCollectionHadiths = allCatalog.filter { it.collectionKey == bookKey },
        collectionKey = bookKey,
        hadithNumber = 1
      )
      assertNotNull("Expected Hadith #1 in $bookKey", match1)
      assertEquals("Hadith #1 lookup in $bookKey must belong to $bookKey", bookKey, match1!!.collectionKey)
      assertEquals("Hadith #1 lookup in $bookKey must have hadithNumber == 1", 1, match1.hadithNumber)
    }

    // Verify Ibn Majah #33 (which is in-book number of #2340) does NOT match #2340 when searching by Hadith #
    val ibnMajahOffline33 = ExtendedHadithDataProvider.findExactHadithInCollection(
      localCollectionHadiths = allCatalog.filter { it.collectionKey == "ibnmajah" },
      collectionKey = "ibnmajah",
      hadithNumber = 33
    )
    assertEquals("In-Book #33 must not masquerade as Hadith #33 in Ibn Majah", null, ibnMajahOffline33)

    // Verify searching a non-existent number in a book returns empty/null and never falls back to another book
    val nonExistentInTirmidhi = ExtendedHadithDataProvider.searchHadithsSmartOffline(
      localCollectionHadiths = allCatalog.filter { it.collectionKey == "tirmidhi" },
      rawQuery = "99999",
      activeCollectionKey = "tirmidhi",
      traditionFilter = "SUNNI",
      includeCrossCollectionMatches = false
    )
    assertTrue("Non-existent Hadith #99999 in Tirmidhi must return empty list", nonExistentInTirmidhi.isEmpty())

    // 2. Verify Fatiha (1), Baqarah (2), Maidah (5), and Yasin (36) across ALL reciters
    val audioManager = com.example.ui.audio.AudioPlayerManager(context)
    val targetSurahs = listOf(1, 2, 5, 36)
    for (reciter in com.example.ui.audio.availableReciters) {
      for (surahNum in targetSurahs) {
        val candidates = audioManager.getCandidateAudioUrls(reciter, surahNum, 1)
        assertTrue("Reciter ${reciter.id} must have verified candidate URLs for Surah $surahNum", candidates.isNotEmpty())
        val primaryUrl = audioManager.resolvePrimaryAudioUrlForTest(reciter, surahNum, 1)
        assertTrue(
          "Primary URL for ${reciter.id} Surah $surahNum ($primaryUrl) must match requested Surah $surahNum",
          audioManager.confirmUrlMatchesRequestedSurah(reciter, surahNum, 1, primaryUrl)
        )
        assertEquals(primaryUrl, audioManager.lastConfirmedAudioUrl)
      }
    }

    // Specifically verify Abdul Rehman Mossad Maidah (5) and Yasin (36) never resolve to playlist track 05 (An-Nisa) or track 36 (Al-Ghashiyah)
    val mossad = com.example.ui.audio.availableReciters.first { it.id == "abdul_rehman_mossad" }
    val mossadMaidahUrl = audioManager.resolvePrimaryAudioUrlForTest(mossad, 5, 1)
    assertTrue("Mossad Surah 5 must resolve to Al-Ma'idah (Track 06), got $mossadMaidahUrl", mossadMaidahUrl.contains("Al-Ma%27idah") || mossadMaidahUrl.endsWith("/005.mp3"))
    assertFalse("Mossad Surah 5 must never resolve to An-Nisa", mossadMaidahUrl.contains("An-Nisa"))

    val mossadYasinUrl = audioManager.resolvePrimaryAudioUrlForTest(mossad, 36, 1)
    assertTrue("Mossad Surah 36 (Yasin) must resolve to 036.mp3, got $mossadYasinUrl", mossadYasinUrl.endsWith("/036.mp3"))
    assertFalse("Mossad Surah 36 (Yasin) must never resolve to Track 36 Al-Ghashiyah", mossadYasinUrl.contains("Al-Ghashiyah"))

    // Verify canonical filename resolver accurately maps playlist tracks by Surah name
    assertEquals(1, com.example.ui.audio.AudioPlayerManager.resolveCanonicalSurahNumberFromFileName("01 Al-Fatihah الفاتحة.mp3"))
    assertEquals(2, com.example.ui.audio.AudioPlayerManager.resolveCanonicalSurahNumberFromFileName("02 Al-Baqarah البقرة.mp3"))
    assertEquals(5, com.example.ui.audio.AudioPlayerManager.resolveCanonicalSurahNumberFromFileName("06 Al-Ma'idah المائدة.mp3"))
    assertEquals(36, com.example.ui.audio.AudioPlayerManager.resolveCanonicalSurahNumberFromFileName("036.mp3"))
    assertEquals(88, com.example.ui.audio.AudioPlayerManager.resolveCanonicalSurahNumberFromFileName("36 Al-Ghashiyah الغاشية.mp3"))

    // 3. Verify Green Text Highlight timing progression and reset for Islam Sobhi & Abdul Rehman Mossad
    val sobhi = com.example.ui.audio.availableReciters.first { it.id == "islam_sobhi" }
    for (surahReciter in listOf(sobhi, mossad)) {
      audioManager.setReciter(surahReciter)
      for (surahNum in targetSurahs) {
        val totalAyahs = com.example.ui.audio.AudioPlayerManager.getSurahTotalAyahs(surahNum)
        val totalDurationMs = totalAyahs * 4000

        // Start of surah -> highlights Ayah 1
        audioManager.applySurahProgressSnapshot(surahNum, positionMs = 100, totalDurationMs = totalDurationMs)
        assertEquals(surahNum, audioManager.audioState.value.surahNumber)
        assertEquals(1, audioManager.audioState.value.ayahNumber)
        assertTrue(audioManager.audioState.value.durationMs > 0)

        // Middle of surah -> highlights middle Ayah
        audioManager.applySurahProgressSnapshot(surahNum, positionMs = totalDurationMs / 2, totalDurationMs = totalDurationMs)
        val midAyah = audioManager.audioState.value.ayahNumber ?: 0
        assertTrue("Expected middle ayah highlighted in Surah $surahNum for ${surahReciter.id}, got $midAyah", midAyah in 2..totalAyahs)

        // End of surah -> highlights final Ayah
        audioManager.applySurahProgressSnapshot(surahNum, positionMs = totalDurationMs - 50, totalDurationMs = totalDurationMs)
        assertEquals(totalAyahs, audioManager.audioState.value.ayahNumber)
      }
    }

    // Verify highlight resets when reciter changes or resetHighlightState() is called
    val husary = com.example.ui.audio.availableReciters.first { it.id == "husary" }
    audioManager.setReciter(husary)
    assertEquals(null, audioManager.audioState.value.ayahNumber)
    assertEquals(0, audioManager.audioState.value.currentPositionMs)
  }

  @Test
  fun `verify Firebase App Check PlayIntegrityAppCheckProviderFactory and DebugAppCheckProviderFactory configuration`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val playIntegrityFactory = com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
    assertNotNull("PlayIntegrityAppCheckProviderFactory instance must be non-null", playIntegrityFactory)

    // Verify configureAppCheck executes cleanly in both default debug mode and Play Integrity mode
    configureAppCheck(context, null, forcePlayIntegrity = false)
    configureAppCheck(context, null, forcePlayIntegrity = true)
  }
}
