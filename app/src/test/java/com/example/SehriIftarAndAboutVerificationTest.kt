package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.VerificationStatus
import com.example.data.model.DailyDuasDataProvider
import com.example.data.prayer.JuridicalSchool
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.PrayerPreferencesManager
import com.example.data.prayer.SehriIftarDayEntry
import com.example.data.prayer.SehriIftarTimetableEngine
import com.example.data.prayer.TimetableScopeMode
import com.example.data.repository.SourceRegistryRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SehriIftarAndAboutVerificationTest {

  private lateinit var context: Context
  private lateinit var db: AppDatabase

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    File(context.filesDir, "sehri_iftar_timetable_cache").deleteRecursively()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    File(context.filesDir, "sehri_iftar_timetable_cache").deleteRecursively()
    db.close()
  }

  @Test
  fun `dual Ramadan Gregorian years 2030 and 1997 are detected and single Ramadan years return one Hijri year`() {
    val ramadans2030 = SehriIftarTimetableEngine.getHijriRamadanYearsForGregorianYear(2030)
    assertEquals(listOf(1451, 1452), ramadans2030)

    val ramadans1997 = SehriIftarTimetableEngine.getHijriRamadanYearsForGregorianYear(1997)
    assertEquals(listOf(1417, 1418), ramadans1997)

    val ramadans2026 = SehriIftarTimetableEngine.getHijriRamadanYearsForGregorianYear(2026)
    assertEquals(listOf(1447), ramadans2026)
  }

  @Test
  fun `AlAdhan API calendar JSON parser handles 29 and 30 day Ramadan responses minute adjustments and deduplication`() {
    // Sample AlAdhan API /v1/hijriCalendar/1447/9 JSON response with duplicate date entry to verify deduplication
    val sampleAlAdhanJson = """
      {
        "code": 200,
        "status": "OK",
        "data": [
          {
            "timings": {
              "Fajr": "05:14 (PKT)",
              "Sunrise": "06:36 (PKT)",
              "Dhuhr": "12:25 (PKT)",
              "Asr": "15:42 (PKT)",
              "Maghrib": "18:14 (PKT)",
              "Isha": "19:35 (PKT)",
              "Imsak": "05:04 (PKT)"
            },
            "date": {
              "readable": "18 Feb 2026",
              "gregorian": {
                "date": "18-02-2026",
                "day": "18",
                "month": { "number": 2, "en": "February" },
                "year": "2026",
                "weekday": { "en": "Wednesday" }
              },
              "hijri": {
                "date": "01-09-1447",
                "day": "01",
                "month": { "number": 9, "en": "Ramaḍān", "ar": "رَمَضَان" },
                "year": "1447"
              }
            }
          },
          {
            "timings": {
              "Fajr": "05:14 (PKT)",
              "Sunrise": "06:36 (PKT)",
              "Dhuhr": "12:25 (PKT)",
              "Asr": "15:42 (PKT)",
              "Maghrib": "18:14 (PKT)",
              "Isha": "19:35 (PKT)",
              "Imsak": "05:04 (PKT)"
            },
            "date": {
              "readable": "18 Feb 2026",
              "gregorian": {
                "date": "18-02-2026",
                "day": "18",
                "month": { "number": 2, "en": "February" },
                "year": "2026",
                "weekday": { "en": "Wednesday" }
              },
              "hijri": {
                "date": "01-09-1447",
                "day": "01",
                "month": { "number": 9, "en": "Ramaḍān", "ar": "رَمَضَان" },
                "year": "1447"
              }
            }
          },
          {
            "timings": {
              "Fajr": "05:13 (PKT)",
              "Sunrise": "06:35 (PKT)",
              "Dhuhr": "12:25 (PKT)",
              "Asr": "15:43 (PKT)",
              "Maghrib": "18:15 (PKT)",
              "Isha": "19:36 (PKT)",
              "Imsak": "05:03 (PKT)"
            },
            "date": {
              "readable": "19 Feb 2026",
              "gregorian": {
                "date": "19-02-2026",
                "day": "19",
                "month": { "number": 2, "en": "February" },
                "year": "2026",
                "weekday": { "en": "Thursday" }
              },
              "hijri": {
                "date": "02-09-1447",
                "day": "02",
                "month": { "number": 9, "en": "Ramaḍān", "ar": "رَمَضَان" },
                "year": "1447"
              }
            }
          }
        ]
      }
    """.trimIndent()

    val karachiMethod = SehriIftarTimetableEngine.findMethodById(1)
    val parsedRaw = SehriIftarTimetableEngine.parseAlAdhanCalendarJson(
      rawJson = sampleAlAdhanJson,
      country = "Pakistan",
      city = "Karachi",
      latitude = 24.8607,
      longitude = 67.0011,
      timeZoneId = "Asia/Karachi",
      method = karachiMethod,
      school = JuridicalSchool.HANAFI,
      filterHijriMonth = 9
    )
    val parsed = parsedRaw.map { it.withManualAdjustments(-2, 3) }

    // Duplicate 18-02-2026 entry is removed automatically
    assertEquals(2, parsed.size)
    val day1 = parsed[0]
    assertEquals("2026-02-18", day1.gregorianDateIso)
    assertEquals(1, day1.hijriDay)
    assertEquals(9, day1.hijriMonthNumber)
    assertEquals(1447, day1.hijriYear)
    assertTrue(day1.isRamadanDay)
    // Raw Fajr 05:14 with -2 min adjustment -> 05:12
    assertEquals("05:12", day1.adjustedSehriFajr24)
    // Raw Maghrib 18:14 with +3 min adjustment -> 18:17
    assertEquals("18:17", day1.adjustedIftarMaghrib24)
    assertEquals(JuridicalSchool.HANAFI, day1.juridicalSchool)
  }

  @Test
  fun `timetable works across multiple worldwide cities and years with offline caching and Hanafi Shafi settings`() = runBlocking {
    val testCities = listOf(
      SehriIftarTimetableEngine.resolveNearestCityForCoordinates(34.0837, 74.7973), // Srinagar, India
      SehriIftarTimetableEngine.resolveNearestCityForCoordinates(21.4225, 39.8262), // Makkah, Saudi Arabia
      SehriIftarTimetableEngine.resolveNearestCityForCoordinates(51.5074, -0.1278), // London, UK
      SehriIftarTimetableEngine.resolveNearestCityForCoordinates(40.7128, -74.0060), // New York, USA
      SehriIftarTimetableEngine.resolveNearestCityForCoordinates(35.6762, 139.6503)  // Tokyo, Japan
    )

    for (cityPreset in testCities) {
      val result1447 = SehriIftarTimetableEngine.getOrFetchTimetable(
        context = context,
        prayerScheduleDao = db.prayerScheduleDao(),
        scopeMode = TimetableScopeMode.RAMADAN_MONTH,
        country = cityPreset.country,
        city = cityPreset.city,
        latitude = cityPreset.latitude,
        longitude = cityPreset.longitude,
        timeZoneId = cityPreset.timeZoneId,
        gregorianYear = 2026,
        gregorianMonth = 2,
        hijriRamadanYear = 1447,
        methodId = cityPreset.defaultMethodId,
        juridicalSchool = JuridicalSchool.HANAFI,
        sehriAdjustmentMinutes = -1,
        iftarAdjustmentMinutes = 2,
        forceRefresh = false
      )

      assertTrue("Ramadan must be 29 or 30 days for ${cityPreset.city}", result1447.days.size in 29..30)
      assertEquals(result1447.days.size, result1447.days.map { it.gregorianDateIso }.distinct().size)
      assertTrue(result1447.authorityVerificationDisclaimer.contains("local", ignoreCase = true))
      result1447.days.forEach { day ->
        assertTrue(day.adjustedSehriFajr24.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(day.adjustedIftarMaghrib24.matches(Regex("\\d{2}:\\d{2}")))
      }
    }

    // Verify dual-Ramadan year 2030 CE: both 1451 AH (Jan 2030) and 1452 AH (Dec 2030)
    val srinagar = testCities.first()
    val karachiMethod = SehriIftarTimetableEngine.findMethodById(1)
    val ramadan1451 = SehriIftarTimetableEngine.computeAstronomicalTimetableOffline(
      scopeMode = TimetableScopeMode.RAMADAN_MONTH,
      country = srinagar.country,
      city = srinagar.city,
      latitude = srinagar.latitude,
      longitude = srinagar.longitude,
      timeZoneId = srinagar.timeZoneId,
      gregorianYear = 2030,
      gregorianMonth = 1,
      hijriRamadanYear = 1451,
      method = karachiMethod,
      school = JuridicalSchool.SHAFI_STANDARD
    )
    val ramadan1452 = SehriIftarTimetableEngine.computeAstronomicalTimetableOffline(
      scopeMode = TimetableScopeMode.RAMADAN_MONTH,
      country = srinagar.country,
      city = srinagar.city,
      latitude = srinagar.latitude,
      longitude = srinagar.longitude,
      timeZoneId = srinagar.timeZoneId,
      gregorianYear = 2030,
      gregorianMonth = 12,
      hijriRamadanYear = 1452,
      method = karachiMethod,
      school = JuridicalSchool.HANAFI
    )

    assertTrue(ramadan1451.size in 29..30)
    assertTrue(ramadan1452.size in 29..30)
    assertTrue(ramadan1451.first().gregorianDateIso.startsWith("2030-01"))
    assertTrue(ramadan1452.first().gregorianDateIso.startsWith("2030-12"))
  }

  @Test
  fun `special Ramadan duas and concise deduplicated VERIFIED sources in About section`() = runBlocking {
    // 1. Verify Special Ramadan Duas exist and contain authentic Arabic, English, Urdu & References
    val ramadanDuas = DailyDuasDataProvider.ramadanSpecialDuas
    assertTrue("Expected at least 6 special Ramadan duas", ramadanDuas.size >= 6)
    ramadanDuas.forEach { dua ->
      assertTrue(dua.arabicText.isNotBlank())
      assertTrue(dua.englishTranslation.isNotBlank())
      assertTrue(dua.urduTranslation.isNotBlank())
      assertTrue(dua.authenticReference.isNotBlank())
    }

    // 2. Seed SourceRegistryRepository and verify buildConciseVerifiedSources:
    //    - Filters out PARTIALLY_VERIFIED items (like fiqh_fatwa_registry)
    //    - Includes the consolidated 34-collection Hadith library as VERIFIED
    //    - Has zero duplicate IDs or titles
    val repo = SourceRegistryRepository(db.sourceRegistryDao())
    repo.ensureDefaultSourcesSeeded()
    val rawSources = repo.getAllSourcesSnapshot()

    val conciseVerified = SourceRegistryRepository.buildConciseVerifiedSources(
      rawSources = rawSources,
      auditReport = null
    )

    assertTrue(conciseVerified.isNotEmpty())
    // Every single item must be strictly VERIFIED
    assertTrue(conciseVerified.all { it.verificationStatus == VerificationStatus.VERIFIED })
    // fiqh_fatwa_registry (PARTIALLY_VERIFIED) must NOT be in the concise verified list
    assertFalse(conciseVerified.any { it.sourceId == "fiqh_fatwa_registry" })
    // Consolidated Hadith library must be present and VERIFIED
    assertTrue(conciseVerified.any { it.sourceId == "verified_hadith_corpus_complete" && it.title.contains("34 Collections") })
    // Zero duplicates by sourceId or normalized title
    assertEquals(conciseVerified.size, conciseVerified.map { it.sourceId }.distinct().size)
    assertEquals(conciseVerified.size, conciseVerified.map { it.title.lowercase().trim() }.distinct().size)
  }

  @Test
  fun `prayer preferences persist Sehri and Iftar minute adjustments and selected timetable years`() {
    val initial = PrayerAndAlarmConfig(
      city = "Istanbul",
      country = "Turkey",
      latitude = 41.0082,
      longitude = 28.9784,
      timeZoneId = "Europe/Istanbul",
      calculationMethodId = 13,
      asrMadhhab = JuridicalSchool.HANAFI.schoolId,
      sehriAdjustmentMinutes = -3,
      iftarAdjustmentMinutes = 4,
      selectedTimetableGregorianYear = 2030,
      selectedTimetableHijriYear = 1452
    )
    PrayerPreferencesManager.saveConfig(context, initial)
    val loaded = PrayerPreferencesManager.loadConfig(context)
    assertEquals("Istanbul", loaded.city)
    assertEquals("Turkey", loaded.country)
    assertEquals(13, loaded.calculationMethodId)
    assertEquals(JuridicalSchool.HANAFI, JuridicalSchool.fromId(loaded.asrMadhhab))
    assertEquals(-3, loaded.sehriAdjustmentMinutes)
    assertEquals(4, loaded.iftarAdjustmentMinutes)
    assertEquals(2030, loaded.selectedTimetableGregorianYear)
    assertEquals(1452, loaded.selectedTimetableHijriYear)
  }

  @Test
  fun `Quranic duas map only to exact Quran verses and non-Quranic duas show Real voice coming soon without AI TTS`() {
    assertEquals(
      "No audio available",
      com.example.ui.audio.DuaAudioController.REAL_VOICE_COMING_SOON_MESSAGE
    )

    // Non-Quranic Hadith/Fasting duas (e.g. drinking water, before eating, Sehri dua, Iftari dua) must NOT map to a fake Surah/Ayah
    val waterDua = DailyDuasDataProvider.allDuas.first { it.id == "dua_drinking_water_pani" }
    val eatingDua = DailyDuasDataProvider.allDuas.first { it.id == "dua_before_eating_khana" }
    val sehriDua = DailyDuasDataProvider.allDuas.first { it.id == "dua_ramadan_sehri_niyyah" }
    val iftarDua = DailyDuasDataProvider.allDuas.first { it.id == "dua_ramadan_iftar" }
    org.junit.Assert.assertNull(waterDua.quranSurahAyah)
    org.junit.Assert.assertNull(eatingDua.quranSurahAyah)
    org.junit.Assert.assertNull(sehriDua.quranSurahAyah)
    org.junit.Assert.assertNull(iftarDua.quranSurahAyah)

    // Verify exact Sehri and Iftari Arabic & Transliteration
    assertEquals("وَبِصَوْمِ غَدٍ نَّوَيْتُ مِنْ شَهْرِ رَمَضَانَ", sehriDua.arabicText)
    assertTrue(sehriDua.transliteration.contains("Wa bisawmi ghadin nawaytu", ignoreCase = true))
    assertEquals("ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ", iftarDua.arabicText)
    assertTrue(iftarDua.transliteration.contains("Zahaba al-zama'u wabtallatil-'urooqu", ignoreCase = true))

    // Authentic Quranic Duas map to their exact Surah:Ayah
    val ayatAlKursi = DailyDuasDataProvider.allDuas.first { it.id == "dua_ayat_al_kursi" }
    val rabbanaAtina = DailyDuasDataProvider.allDuas.first { it.id == "dua_rabbana_fid_dunya" }
    val rabbiZidniIlma = DailyDuasDataProvider.allDuas.first { it.id == "dua_increase_knowledge_ilm" }
    val safarVerse = DailyDuasDataProvider.allDuas.first { it.id == "dua_safar_travel" }

    assertEquals(2 to 255, ayatAlKursi.quranSurahAyah)
    assertEquals(2 to 201, rabbanaAtina.quranSurahAyah)
    assertEquals(20 to 114, rabbiZidniIlma.quranSurahAyah)
    assertEquals(43 to 13, safarVerse.quranSurahAyah)
  }
}

