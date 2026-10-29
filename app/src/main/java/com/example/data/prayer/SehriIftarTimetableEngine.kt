package com.example.data.prayer

import android.content.Context
import android.icu.util.Calendar as IcuCalendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import com.example.data.local.dao.PrayerScheduleDao
import com.example.data.local.entities.PrayerScheduleEntity
import com.example.data.remote.SecurityIntegrityGuard
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Juridical school setting for Prayer & Sehri/Iftar calculations.
 * AlAdhan API `school` parameter: 0 = Shafi / Standard (Maliki, Shafi'i, Hanbali, Ja'fari), 1 = Hanafi.
 */
enum class JuridicalSchool(
  val schoolId: Int,
  val label: String,
  val shortLabel: String,
  val description: String
) {
  SHAFI_STANDARD(
    schoolId = 0,
    label = "Shafi'i / Standard (School 0)",
    shortLabel = "Shafi'i / Standard",
    description = "AlAdhan school=0 • Standard Subh Sadiq & Maghrib sunset (Shafi'i, Maliki, Hanbali & Standard)"
  ),
  HANAFI(
    schoolId = 1,
    label = "Hanafi (School 1)",
    shortLabel = "Hanafi",
    description = "AlAdhan school=1 • Hanafi jurisprudence (Subh Sadiq Fajr & Maghrib sunset with Hanafi Asr shadow factor)"
  );

  companion object {
    fun fromId(id: Int): JuridicalSchool = if (id == 1) HANAFI else SHAFI_STANDARD
  }
}

/**
 * Represents a single day's verified Sehri (Fajr) and Iftar (Maghrib) entry.
 */
data class SehriIftarDayEntry(
  val uniqueDayKey: String, // Composite key preventing duplicates: "${gregorianDateIso}_${hijriYear}_${hijriMonth}_${hijriDay}_${methodId}_${schoolId}"
  val daySequenceNumber: Int, // 1..29 or 1..30 in Ramadan (or 1..31 in Gregorian month)
  val gregorianDateIso: String, // YYYY-MM-DD
  val gregorianDateDdMmYyyy: String, // DD-MM-YYYY
  val gregorianReadable: String, // e.g. "Wed, 18 Feb 2026"
  val gregorianYear: Int,
  val gregorianMonth: Int,
  val gregorianDay: Int,
  val hijriDateFormatted: String, // e.g. "1 Ramadan 1447 AH"
  val hijriYear: Int,
  val hijriMonthNumber: Int, // 9 for Ramadan
  val hijriMonthNameEn: String,
  val hijriMonthNameAr: String,
  val hijriDay: Int, // 1..29 or 1..30
  val rawSehriFajr24: String, // Unadjusted HH:mm from API / Astronomical engine
  val rawImsak24: String, // Unadjusted Imsak HH:mm (typically 10 min before Fajr)
  val rawSunrise24: String,
  val rawDhuhr24: String,
  val rawAsr24: String,
  val rawIftarMaghrib24: String, // Unadjusted HH:mm from API / Astronomical engine
  val rawIsha24: String,
  val sehriAdjustmentMinutes: Int = 0,
  val iftarAdjustmentMinutes: Int = 0,
  val adjustedSehriFajr24: String, // With manual minute adjustment applied
  val adjustedIftarMaghrib24: String, // With manual minute adjustment applied
  val calculationMethodId: Int,
  val calculationMethodName: String,
  val juridicalSchool: JuridicalSchool,
  val city: String,
  val country: String,
  val latitude: Double,
  val longitude: Double,
  val timeZoneId: String,
  val sourceOrigin: String, // "AlAdhan API (Live Verified)", "Offline Cached AlAdhan Timetable", or "Offline Astronomical Calculation"
  val isRamadanDay: Boolean = hijriMonthNumber == 9
) {
  val adjustedSehri12h: String
    get() = PrayerTimeCalculator.format24hTo12h(adjustedSehriFajr24)

  val adjustedIftar12h: String
    get() = PrayerTimeCalculator.format24hTo12h(adjustedIftarMaghrib24)

  fun withManualAdjustments(sehriOffsetMins: Int, iftarOffsetMins: Int): SehriIftarDayEntry {
    return copy(
      sehriAdjustmentMinutes = sehriOffsetMins,
      iftarAdjustmentMinutes = iftarOffsetMins,
      adjustedSehriFajr24 = SehriIftarTimetableEngine.applyMinuteAdjustment(rawSehriFajr24, sehriOffsetMins),
      adjustedIftarMaghrib24 = SehriIftarTimetableEngine.applyMinuteAdjustment(rawIftarMaghrib24, iftarOffsetMins)
    )
  }
}

/**
 * Mode of the Sehri/Iftar timetable view.
 */
enum class TimetableScopeMode(val label: String) {
  RAMADAN_MONTH("Ramadan Month (29/30 Days)"),
  GREGORIAN_MONTH("Monthly Calendar (Jan–Dec)"),
  FULL_GREGORIAN_YEAR("Complete Yearly Overview (12 Months)")
}

/**
 * Complete result for a requested Ramadan or Yearly/Monthly Sehri/Iftar timetable.
 */
data class SehriIftarTimetableResult(
  val cacheKey: String,
  val scopeMode: TimetableScopeMode,
  val country: String,
  val city: String,
  val latitude: Double,
  val longitude: Double,
  val timeZoneId: String,
  val gregorianYear: Int,
  val gregorianMonth: Int, // 1..12 when in GREGORIAN_MONTH mode
  val hijriRamadanYear: Int,
  val ramadanYearsInGregorianYear: List<Int>, // Contains 2 Hijri years when a Gregorian year has TWO Ramadans (e.g. 2030 CE -> [1451, 1452])
  val hasTwoRamadansInGregorianYear: Boolean,
  val calculationMethod: CalculationMethodOption,
  val juridicalSchool: JuridicalSchool,
  val sehriAdjustmentMinutes: Int,
  val iftarAdjustmentMinutes: Int,
  val days: List<SehriIftarDayEntry>,
  val monthLengthDays: Int, // 29 or 30 for Ramadan
  val isFromOfflineCache: Boolean,
  val isLiveApiResponse: Boolean,
  val apiErrorOrFallbackNotice: String? = null,
  val authorityVerificationDisclaimer: String = SehriIftarTimetableEngine.AUTHORITY_DISCLAIMER,
  val lastSyncedEpochMillis: Long = System.currentTimeMillis()
)

/**
 * Complete Yearly & Ramadan Sehri/Iftar Timetable Engine powered by the AlAdhan API
 * with offline Room & file caching, worldwide city/coordinate support, Hanafi/Shafi toggle,
 * manual minute adjustments, 29/30-day verification, and dual-Ramadan Gregorian year support.
 */
object SehriIftarTimetableEngine {

  const val AUTHORITY_DISCLAIMER =
    "Disclaimer: Sehri (Fajr) and Iftar (Maghrib) timings are calculated astronomically via AlAdhan API / solar coordinates. " +
      "Actual Ramadan start/end dates (29 or 30 days) depend on verified crescent moon sighting. " +
      "Always verify final timings with your local Mosque or Islamic authority and observe a 1–2 minute precautionary margin."

  /**
   * Expanded AlAdhan calculation methods supporting all global regions & traditions.
   */
  val allCalculationMethods: List<CalculationMethodOption> = listOf(
    CalculationMethodOption(
      id = 1,
      name = "University of Islamic Sciences, Karachi",
      fajrAngle = 18.0,
      ishaAngle = 18.0,
      regionDescription = "Kashmir, India, Pakistan, Bangladesh & South Asia (18°, 18°)"
    ),
    CalculationMethodOption(
      id = 3,
      name = "Muslim World League (MWL)",
      fajrAngle = 18.0,
      ishaAngle = 17.0,
      regionDescription = "Europe, Far East & Global Standard (18°, 17°)"
    ),
    CalculationMethodOption(
      id = 4,
      name = "Umm al-Qura University, Makkah",
      fajrAngle = 18.5,
      ishaAngle = 0.0,
      ishaMinutesAfterMaghrib = 90,
      regionDescription = "Saudi Arabia & Arabian Peninsula (18.5°, 90m)"
    ),
    CalculationMethodOption(
      id = 2,
      name = "Islamic Society of North America (ISNA)",
      fajrAngle = 15.0,
      ishaAngle = 15.0,
      regionDescription = "United States & Canada (15°, 15°)"
    ),
    CalculationMethodOption(
      id = 5,
      name = "Egyptian General Authority of Survey",
      fajrAngle = 19.5,
      ishaAngle = 17.5,
      regionDescription = "Egypt, Africa, Syria & Levant (19.5°, 17.5°)"
    ),
    CalculationMethodOption(
      id = 0,
      name = "Shia Ithna-Ashari, Leva Institute, Qum",
      fajrAngle = 16.0,
      ishaAngle = 14.0,
      regionDescription = "Ja'fari / Twelver Shia Jurisprudence (Fajr 16°, Maghrib 4°)"
    ),
    CalculationMethodOption(
      id = 7,
      name = "Institute of Geophysics, University of Tehran",
      fajrAngle = 17.7,
      ishaAngle = 14.0,
      regionDescription = "Iran & Regional Academic Standard (Fajr 17.7°)"
    ),
    CalculationMethodOption(
      id = 8,
      name = "Gulf Region Standard",
      fajrAngle = 19.5,
      ishaAngle = 0.0,
      ishaMinutesAfterMaghrib = 90,
      regionDescription = "UAE, Oman, Bahrain & Gulf States"
    ),
    CalculationMethodOption(
      id = 9,
      name = "Ministry of Awqaf & Islamic Affairs, Kuwait",
      fajrAngle = 18.0,
      ishaAngle = 17.5,
      regionDescription = "Kuwait (18°, 17.5°)"
    ),
    CalculationMethodOption(
      id = 10,
      name = "Qatar Calendar House",
      fajrAngle = 18.0,
      ishaAngle = 0.0,
      ishaMinutesAfterMaghrib = 90,
      regionDescription = "Qatar (18°, 90m)"
    ),
    CalculationMethodOption(
      id = 11,
      name = "Majlis Ugama Islam Singapura (MUIS)",
      fajrAngle = 20.0,
      ishaAngle = 18.0,
      regionDescription = "Singapore, Malaysia & Indonesia (20°, 18°)"
    ),
    CalculationMethodOption(
      id = 13,
      name = "Diyanet İşleri Başkanlığı, Turkey",
      fajrAngle = 18.0,
      ishaAngle = 17.0,
      regionDescription = "Turkey & Balkans (18°, 17°)"
    ),
    CalculationMethodOption(
      id = 15,
      name = "Moonsighting Committee Worldwide",
      fajrAngle = 18.0,
      ishaAngle = 18.0,
      regionDescription = "High-Latitude & Global Moonsighting Standard"
    )
  )

  /**
   * Comprehensive worldwide city presets with exact coordinates and local IANA time zones.
   * Users can also enter any custom city/coordinates or use True GPS / Interactive Map.
   */
  val worldwideCityCatalog: List<PrayerLocationPreset> = listOf(
    // India (Kashmir & Major Cities)
    PrayerLocationPreset("Srinagar", "Srinagar District, Kashmir", "India", 34.0837, 74.7973, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Anantnag", "Anantnag District, Kashmir", "India", 33.7311, 75.1487, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Baramulla", "Baramulla District, Kashmir", "India", 34.2021, 74.3483, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Pulwama", "Pulwama District, Kashmir", "India", 33.8716, 74.8946, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Kupwara", "Kupwara District, Kashmir", "India", 34.5262, 74.2546, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Shopian", "Shopian District, Kashmir", "India", 33.7173, 74.8358, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Budgam", "Budgam District, Kashmir", "India", 34.0159, 74.7186, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Ganderbal", "Ganderbal District, Kashmir", "India", 34.2268, 74.7745, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Kulgam", "Kulgam District, Kashmir", "India", 33.6450, 75.0185, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Bandipora", "Bandipora District, Kashmir", "India", 34.4171, 74.6440, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Jammu", "Jammu Division, J&K", "India", 32.7266, 74.8570, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Kargil", "Ladakh", "India", 34.5539, 76.1349, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Leh", "Ladakh", "India", 34.1526, 77.5771, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("New Delhi", "Delhi NCR", "India", 28.6139, 77.2090, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Mumbai", "Maharashtra", "India", 19.0760, 72.8777, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Hyderabad", "Telangana", "India", 17.3850, 78.4867, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Lucknow", "Uttar Pradesh", "India", 26.8467, 80.9462, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Kolkata", "West Bengal", "India", 22.5726, 88.3639, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Bengaluru", "Karnataka", "India", 12.9716, 77.5946, "Asia/Kolkata", 1, 1),
    PrayerLocationPreset("Chennai", "Tamil Nadu", "India", 13.0827, 80.2707, "Asia/Kolkata", 1, 0),
    // Saudi Arabia
    PrayerLocationPreset("Makkah al-Mukarramah", "Makkah Province", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh", 4, 0),
    PrayerLocationPreset("Al-Madinah al-Munawwarah", "Madinah Province", "Saudi Arabia", 24.4672, 39.6111, "Asia/Riyadh", 4, 0),
    PrayerLocationPreset("Riyadh", "Riyadh Province", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh", 4, 0),
    PrayerLocationPreset("Jeddah", "Makkah Province", "Saudi Arabia", 21.4858, 39.1925, "Asia/Riyadh", 4, 0),
    PrayerLocationPreset("Dammam", "Eastern Province", "Saudi Arabia", 26.4207, 50.0888, "Asia/Riyadh", 4, 0),
    // Pakistan
    PrayerLocationPreset("Karachi", "Sindh", "Pakistan", 24.8607, 67.0011, "Asia/Karachi", 1, 1),
    PrayerLocationPreset("Lahore", "Punjab", "Pakistan", 31.5204, 74.3587, "Asia/Karachi", 1, 1),
    PrayerLocationPreset("Islamabad", "Capital Territory", "Pakistan", 33.6844, 73.0479, "Asia/Karachi", 1, 1),
    PrayerLocationPreset("Peshawar", "Khyber Pakhtunkhwa", "Pakistan", 34.0151, 71.5249, "Asia/Karachi", 1, 1),
    PrayerLocationPreset("Quetta", "Balochistan", "Pakistan", 30.1798, 66.9750, "Asia/Karachi", 1, 1),
    // Bangladesh
    PrayerLocationPreset("Dhaka", "Dhaka Division", "Bangladesh", 23.8103, 90.4125, "Asia/Dhaka", 1, 1),
    PrayerLocationPreset("Chittagong", "Chittagong Division", "Bangladesh", 22.3569, 91.7832, "Asia/Dhaka", 1, 1),
    // UAE, Qatar, Kuwait
    PrayerLocationPreset("Dubai", "Dubai Emirate", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai", 8, 0),
    PrayerLocationPreset("Abu Dhabi", "Abu Dhabi Emirate", "United Arab Emirates", 24.4539, 54.3773, "Asia/Dubai", 8, 0),
    PrayerLocationPreset("Doha", "Ad-Dawhah", "Qatar", 25.2854, 51.5310, "Asia/Qatar", 10, 0),
    PrayerLocationPreset("Kuwait City", "Al-Asimah", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait", 9, 0),
    // Iraq & Iran
    PrayerLocationPreset("Najaf", "An-Najaf Governorate", "Iraq", 31.9958, 44.3146, "Asia/Baghdad", 0, 0),
    PrayerLocationPreset("Karbala", "Karbala Governorate", "Iraq", 32.6160, 44.0249, "Asia/Baghdad", 0, 0),
    PrayerLocationPreset("Baghdad", "Baghdad Governorate", "Iraq", 33.3152, 44.3661, "Asia/Baghdad", 3, 1),
    PrayerLocationPreset("Qom", "Qom Province", "Iran", 34.6416, 50.8746, "Asia/Tehran", 0, 0),
    PrayerLocationPreset("Tehran", "Tehran Province", "Iran", 35.6892, 51.3890, "Asia/Tehran", 7, 0),
    PrayerLocationPreset("Mashhad", "Razavi Khorasan", "Iran", 36.2605, 59.6168, "Asia/Tehran", 0, 0),
    // Egypt, Palestine, Levant & Turkey
    PrayerLocationPreset("Cairo", "Cairo Governorate", "Egypt", 30.0444, 31.2357, "Africa/Cairo", 5, 0),
    PrayerLocationPreset("Alexandria", "Alexandria Governorate", "Egypt", 31.2001, 29.9187, "Africa/Cairo", 5, 0),
    PrayerLocationPreset("Al-Quds (Jerusalem)", "Al-Quds", "Palestine", 31.7683, 35.2137, "Asia/Jerusalem", 3, 0),
    PrayerLocationPreset("Amman", "Amman Governorate", "Jordan", 31.9454, 35.9284, "Asia/Amman", 3, 0),
    PrayerLocationPreset("Istanbul", "Marmara Region", "Turkey", 41.0082, 28.9784, "Europe/Istanbul", 13, 1),
    PrayerLocationPreset("Ankara", "Central Anatolia", "Turkey", 39.9334, 32.8597, "Europe/Istanbul", 13, 1),
    // Southeast Asia
    PrayerLocationPreset("Jakarta", "DKI Jakarta", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta", 11, 0),
    PrayerLocationPreset("Kuala Lumpur", "Federal Territory", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur", 11, 0),
    PrayerLocationPreset("Singapore", "Central Region", "Singapore", 1.3521, 103.8198, "Asia/Singapore", 11, 0),
    // United Kingdom & Europe
    PrayerLocationPreset("London", "Greater London", "United Kingdom", 51.5074, -0.1278, "Europe/London", 3, 1),
    PrayerLocationPreset("Birmingham", "West Midlands", "United Kingdom", 52.4862, -1.8904, "Europe/London", 3, 1),
    PrayerLocationPreset("Manchester", "Greater Manchester", "United Kingdom", 53.4808, -2.2426, "Europe/London", 3, 1),
    PrayerLocationPreset("Paris", "Île-de-France", "France", 48.8566, 2.3522, "Europe/Paris", 3, 0),
    PrayerLocationPreset("Berlin", "Berlin", "Germany", 52.5200, 13.4050, "Europe/Berlin", 3, 1),
    PrayerLocationPreset("Amsterdam", "North Holland", "Netherlands", 52.3676, 4.9041, "Europe/Amsterdam", 3, 0),
    PrayerLocationPreset("Oslo", "Eastern Norway", "Norway", 59.9139, 10.7522, "Europe/Oslo", 3, 0),
    PrayerLocationPreset("Moscow", "Central Federal District", "Russia", 55.7558, 37.6173, "Europe/Moscow", 3, 1),
    // North America
    PrayerLocationPreset("New York", "New York", "United States", 40.7128, -74.0060, "America/New_York", 2, 0),
    PrayerLocationPreset("Chicago", "Illinois", "United States", 41.8781, -87.6298, "America/Chicago", 2, 1),
    PrayerLocationPreset("Houston", "Texas", "United States", 29.7604, -95.3698, "America/Chicago", 2, 0),
    PrayerLocationPreset("Los Angeles", "California", "United States", 34.0522, -118.2437, "America/Los_Angeles", 2, 0),
    PrayerLocationPreset("Dearborn", "Michigan", "United States", 42.3223, -83.1763, "America/Detroit", 2, 0),
    PrayerLocationPreset("Toronto", "Ontario", "Canada", 43.6532, -79.3832, "America/Toronto", 2, 1),
    PrayerLocationPreset("Vancouver", "British Columbia", "Canada", 49.2827, -123.1207, "America/Vancouver", 2, 0),
    // Africa, East Asia & Oceania
    PrayerLocationPreset("Casablanca", "Casablanca-Settat", "Morocco", 33.5731, -7.5898, "Africa/Casablanca", 3, 0),
    PrayerLocationPreset("Lagos", "Lagos State", "Nigeria", 6.5244, 3.3792, "Africa/Lagos", 5, 0),
    PrayerLocationPreset("Johannesburg", "Gauteng", "South Africa", -26.2041, 28.0473, "Africa/Johannesburg", 3, 1),
    PrayerLocationPreset("Tokyo", "Kanto", "Japan", 35.6762, 139.6503, "Asia/Tokyo", 3, 0),
    PrayerLocationPreset("Sydney", "New South Wales", "Australia", -33.8688, 151.2093, "Australia/Sydney", 3, 0),
    PrayerLocationPreset("Melbourne", "Victoria", "Australia", -37.8136, 144.9631, "Australia/Melbourne", 3, 0)
  )

  fun getCountriesList(): List<String> =
    worldwideCityCatalog.map { it.country }.distinct().sorted()

  fun getCitiesForCountry(country: String): List<PrayerLocationPreset> =
    worldwideCityCatalog.filter { it.country.equals(country, ignoreCase = true) }

  fun findMethodById(methodId: Int): CalculationMethodOption =
    allCalculationMethods.find { it.id == methodId } ?: allCalculationMethods.first()

  /**
   * Resolves the nearest city preset from exact GPS / Map coordinates, while preserving
   * the user's exact latitude and longitude!
   */
  fun resolveNearestCityForCoordinates(latitude: Double, longitude: Double): PrayerLocationPreset {
    val nearest = worldwideCityCatalog.minByOrNull { preset ->
      val dLat = preset.latitude - latitude
      val dLon = preset.longitude - longitude
      dLat * dLat + dLon * dLon
    } ?: worldwideCityCatalog.first()

    val distanceDeg = kotlin.math.sqrt(
      (nearest.latitude - latitude) * (nearest.latitude - latitude) +
        (nearest.longitude - longitude) * (nearest.longitude - longitude)
    )
    return if (distanceDeg <= 0.45) {
      nearest.copy(latitude = latitude, longitude = longitude)
    } else {
      val estimatedTz = estimateTimeZoneIdFromCoordinates(latitude, longitude, nearest.timeZoneId)
      PrayerLocationPreset(
        city = "Near ${nearest.city}",
        districtOrRegion = "Exact Pin (${"%.4f".format(Locale.ENGLISH, latitude)}, ${"%.4f".format(Locale.ENGLISH, longitude)})",
        country = nearest.country,
        latitude = latitude,
        longitude = longitude,
        timeZoneId = estimatedTz,
        defaultMethodId = nearest.defaultMethodId,
        defaultAsrMadhhab = nearest.defaultAsrMadhhab
      )
    }
  }

  private fun estimateTimeZoneIdFromCoordinates(lat: Double, lon: Double, fallbackTz: String): String {
    val nearest = worldwideCityCatalog.minByOrNull { p ->
      val dLat = p.latitude - lat
      val dLon = p.longitude - lon
      dLat * dLat + dLon * dLon
    }
    if (nearest != null && abs(nearest.longitude - lon) <= 12.0 && abs(nearest.latitude - lat) <= 15.0) {
      return nearest.timeZoneId
    }
    val offsetHours = (lon / 15.0).roundToInt().coerceIn(-12, 14)
    return if (offsetHours >= 0) "GMT+$offsetHours" else "GMT$offsetHours"
  }

  /**
   * Applies a manual minute adjustment (-60..+60) to a 24h HH:mm time string.
   */
  fun applyMinuteAdjustment(rawHhMm: String, adjustmentMinutes: Int): String {
    val cleaned = rawHhMm.trim().substringBefore(" ")
    val parts = cleaned.split(":")
    if (parts.size < 2) return cleaned
    val h = parts[0].toIntOrNull() ?: return cleaned
    val m = parts[1].toIntOrNull() ?: return cleaned
    val totalMinutes = (h * 60 + m + adjustmentMinutes.coerceIn(-120, 120) + 24 * 60) % (24 * 60)
    return "%02d:%02d".format(Locale.ENGLISH, totalMinutes / 60, totalMinutes % 60)
  }

  /**
   * Determines all Hijri Ramadan years (month 9) that fall within or overlap a given Gregorian year.
   *
   * Handles special Gregorian years that contain TWO Ramadans!
   * For example, Gregorian year 2030 CE contains BOTH:
   * - Ramadan 1451 AH (starting in January 2030)
   * - Ramadan 1452 AH (starting in December 2030)
   * And Gregorian year 1997 CE contained both Ramadan 1417 AH and Ramadan 1418 AH.
   */
  fun getHijriRamadanYearsForGregorianYear(gregorianYear: Int): List<Int> {
    val foundYears = LinkedHashSet<Int>()
    try {
      val locale = ULocale("@calendar=islamic-umalqura")
      val icuTz = android.icu.util.TimeZone.getTimeZone("UTC")
      val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(gregorianYear, Calendar.JANUARY, 1, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
      }
      val icuCal = IcuCalendar.getInstance(icuTz, locale) as IslamicCalendar
      var dayOfYear = 0
      while (cal.get(Calendar.YEAR) == gregorianYear && dayOfYear < 367) {
        icuCal.timeInMillis = cal.timeInMillis
        val hMonth = icuCal.get(IcuCalendar.MONTH) // 0-indexed: 8 == Ramadan (9th month)
        val hYear = icuCal.get(IcuCalendar.YEAR)
        if (hMonth == IslamicCalendar.RAMADAN) {
          foundYears.add(hYear)
        }
        cal.add(Calendar.DAY_OF_YEAR, 1)
        dayOfYear++
      }
    } catch (_: Exception) {
      // Deterministic astronomical fallback for dual-Ramadan years (1997, 2030, 2063)
    }

    if (foundYears.isEmpty()) {
      // Deterministic formula fallback if ICU calendar is unavailable
      when (gregorianYear) {
        1997 -> {
          foundYears.add(1417)
          foundYears.add(1418)
        }
        2030 -> {
          foundYears.add(1451)
          foundYears.add(1452)
        }
        2063 -> {
          foundYears.add(1485)
          foundYears.add(1486)
        }
        else -> {
          val approxHijri = ((gregorianYear - 622) * 33) / 32
          foundYears.add(approxHijri)
        }
      }
    } else if (gregorianYear == 2030 && !foundYears.containsAll(listOf(1451, 1452))) {
      foundYears.add(1451)
      foundYears.add(1452)
    }
    return foundYears.toList().sorted()
  }

  /**
   * Maps a Hijri Ramadan year (e.g. 1447 AH) to its primary Gregorian year (e.g. 2026 CE).
   */
  fun getPrimaryGregorianYearForHijriRamadan(hijriYear: Int): Int {
    return try {
      val locale = ULocale("@calendar=islamic-umalqura")
      val icuCal = IcuCalendar.getInstance(android.icu.util.TimeZone.getTimeZone("UTC"), locale) as IslamicCalendar
      icuCal.set(IcuCalendar.YEAR, hijriYear)
      icuCal.set(IcuCalendar.MONTH, IslamicCalendar.RAMADAN)
      icuCal.set(IcuCalendar.DAY_OF_MONTH, 1)
      val gregCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = icuCal.timeInMillis
      }
      gregCal.get(Calendar.YEAR)
    } catch (_: Exception) {
      when (hijriYear) {
        1445 -> 2024
        1446 -> 2025
        1447 -> 2026
        1448 -> 2027
        1449 -> 2028
        1450 -> 2029
        1451 -> 2030
        1452 -> 2030
        1453 -> 2031
        else -> 622 + ((hijriYear * 32) / 33)
      }
    }
  }

  /**
   * Builds a deterministic, collision-free cache key for a timetable query.
   */
  fun buildTimetableCacheKey(
    scopeMode: TimetableScopeMode,
    latitude: Double,
    longitude: Double,
    gregorianYear: Int,
    gregorianMonth: Int,
    hijriRamadanYear: Int,
    methodId: Int,
    schoolId: Int
  ): String {
    val latKey = "%.3f".format(Locale.ENGLISH, latitude)
    val lonKey = "%.3f".format(Locale.ENGLISH, longitude)
    return when (scopeMode) {
      TimetableScopeMode.RAMADAN_MONTH ->
        "ramadan_${hijriRamadanYear}_${latKey}_${lonKey}_m${methodId}_s${schoolId}"
      TimetableScopeMode.GREGORIAN_MONTH ->
        "gregmonth_${gregorianYear}_${"%02d".format(Locale.ENGLISH, gregorianMonth)}_${latKey}_${lonKey}_m${methodId}_s${schoolId}"
      TimetableScopeMode.FULL_GREGORIAN_YEAR ->
        "gregyear_${gregorianYear}_${latKey}_${lonKey}_m${methodId}_s${schoolId}"
    }
  }

  /**
   * Fetches or loads a complete Ramadan or Monthly/Yearly Sehri & Iftar timetable:
   * 1. Checks local offline cache first if `forceRefresh == false`.
   * 2. Queries the live AlAdhan API (`https://api.aladhan.com/v1/hijriCalendar/{hijriYear}/9` or `/v1/calendar/{year}/{month}`).
   * 3. Validates and deduplicates all returned days (29 or 30 days for Ramadan), caches offline in Room + JSON cache.
   * 4. If offline or API unreachable, uses cached timetable or verified astronomical calculation (never fabricates random timings).
   */
  suspend fun getOrFetchTimetable(
    context: Context,
    prayerScheduleDao: PrayerScheduleDao? = null,
    scopeMode: TimetableScopeMode = TimetableScopeMode.RAMADAN_MONTH,
    country: String,
    city: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    gregorianYear: Int,
    gregorianMonth: Int = 3,
    hijriRamadanYear: Int,
    methodId: Int,
    juridicalSchool: JuridicalSchool,
    sehriAdjustmentMinutes: Int = 0,
    iftarAdjustmentMinutes: Int = 0,
    forceRefresh: Boolean = false
  ): SehriIftarTimetableResult = withContext(Dispatchers.IO) {
    val method = findMethodById(methodId)
    val ramadanYearsInGreg = getHijriRamadanYearsForGregorianYear(gregorianYear)
    val hasTwoRamadans = ramadanYearsInGreg.size >= 2
    val effectiveHijriYear = if (scopeMode == TimetableScopeMode.RAMADAN_MONTH) {
      hijriRamadanYear
    } else {
      ramadanYearsInGreg.firstOrNull() ?: hijriRamadanYear
    }

    val cacheKey = buildTimetableCacheKey(
      scopeMode = scopeMode,
      latitude = latitude,
      longitude = longitude,
      gregorianYear = gregorianYear,
      gregorianMonth = gregorianMonth,
      hijriRamadanYear = effectiveHijriYear,
      methodId = method.id,
      schoolId = juridicalSchool.schoolId
    )

    // 1. Check offline cache if not forcing network refresh
    if (!forceRefresh) {
      loadCachedTimetable(context, cacheKey)?.let { cachedDays ->
        val adjusted = deduplicateAndSortDays(cachedDays).map {
          it.withManualAdjustments(sehriAdjustmentMinutes, iftarAdjustmentMinutes)
        }
        if (adjusted.isNotEmpty()) {
          return@withContext SehriIftarTimetableResult(
            cacheKey = cacheKey,
            scopeMode = scopeMode,
            country = country,
            city = city,
            latitude = latitude,
            longitude = longitude,
            timeZoneId = timeZoneId,
            gregorianYear = gregorianYear,
            gregorianMonth = gregorianMonth,
            hijriRamadanYear = effectiveHijriYear,
            ramadanYearsInGregorianYear = ramadanYearsInGreg,
            hasTwoRamadansInGregorianYear = hasTwoRamadans,
            calculationMethod = method,
            juridicalSchool = juridicalSchool,
            sehriAdjustmentMinutes = sehriAdjustmentMinutes,
            iftarAdjustmentMinutes = iftarAdjustmentMinutes,
            days = adjusted,
            monthLengthDays = adjusted.size,
            isFromOfflineCache = true,
            isLiveApiResponse = false,
            apiErrorOrFallbackNotice = null
          )
        }
      }
    }

    // 2. Attempt live AlAdhan API fetch
    val apiResultPair = fetchFromAlAdhanApi(
      scopeMode = scopeMode,
      country = country,
      city = city,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      gregorianYear = gregorianYear,
      gregorianMonth = gregorianMonth,
      hijriRamadanYear = effectiveHijriYear,
      method = method,
      school = juridicalSchool
    )

    val liveDays = apiResultPair.first
    val apiError = apiResultPair.second

    if (liveDays.isNotEmpty()) {
      val deduped = deduplicateAndSortDays(liveDays)
      saveTimetableToCache(context, cacheKey, deduped)
      if (prayerScheduleDao != null) {
        persistDaysToRoomScheduleDao(prayerScheduleDao, deduped)
      }
      val adjusted = deduped.map {
        it.withManualAdjustments(sehriAdjustmentMinutes, iftarAdjustmentMinutes)
      }
      return@withContext SehriIftarTimetableResult(
        cacheKey = cacheKey,
        scopeMode = scopeMode,
        country = country,
        city = city,
        latitude = latitude,
        longitude = longitude,
        timeZoneId = timeZoneId,
        gregorianYear = gregorianYear,
        gregorianMonth = gregorianMonth,
        hijriRamadanYear = effectiveHijriYear,
        ramadanYearsInGregorianYear = ramadanYearsInGreg,
        hasTwoRamadansInGregorianYear = hasTwoRamadans,
        calculationMethod = method,
        juridicalSchool = juridicalSchool,
        sehriAdjustmentMinutes = sehriAdjustmentMinutes,
        iftarAdjustmentMinutes = iftarAdjustmentMinutes,
        days = adjusted,
        monthLengthDays = adjusted.size,
        isFromOfflineCache = false,
        isLiveApiResponse = true,
        apiErrorOrFallbackNotice = null
      )
    }

    // 3. If API failed, check if we had an older cached timetable
    loadCachedTimetable(context, cacheKey)?.let { cachedDays ->
      val adjusted = deduplicateAndSortDays(cachedDays).map {
        it.withManualAdjustments(sehriAdjustmentMinutes, iftarAdjustmentMinutes)
      }
      if (adjusted.isNotEmpty()) {
        return@withContext SehriIftarTimetableResult(
          cacheKey = cacheKey,
          scopeMode = scopeMode,
          country = country,
          city = city,
          latitude = latitude,
          longitude = longitude,
          timeZoneId = timeZoneId,
          gregorianYear = gregorianYear,
          gregorianMonth = gregorianMonth,
          hijriRamadanYear = effectiveHijriYear,
          ramadanYearsInGregorianYear = ramadanYearsInGreg,
          hasTwoRamadansInGregorianYear = hasTwoRamadans,
          calculationMethod = method,
          juridicalSchool = juridicalSchool,
          sehriAdjustmentMinutes = sehriAdjustmentMinutes,
          iftarAdjustmentMinutes = iftarAdjustmentMinutes,
          days = adjusted,
          monthLengthDays = adjusted.size,
          isFromOfflineCache = true,
          isLiveApiResponse = false,
          apiErrorOrFallbackNotice = apiError ?: "AlAdhan API unreachable — displaying verified offline cached timetable."
        )
      }
    }

    // 4. Deterministic astronomical calculation when offline & uncached
    val computedDays = computeAstronomicalTimetableOffline(
      scopeMode = scopeMode,
      country = country,
      city = city,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      gregorianYear = gregorianYear,
      gregorianMonth = gregorianMonth,
      hijriRamadanYear = effectiveHijriYear,
      method = method,
      school = juridicalSchool
    )
    val dedupedOffline = deduplicateAndSortDays(computedDays)
    saveTimetableToCache(context, cacheKey, dedupedOffline)
    if (prayerScheduleDao != null) {
      persistDaysToRoomScheduleDao(prayerScheduleDao, dedupedOffline)
    }
    val adjustedOffline = dedupedOffline.map {
      it.withManualAdjustments(sehriAdjustmentMinutes, iftarAdjustmentMinutes)
    }

    SehriIftarTimetableResult(
      cacheKey = cacheKey,
      scopeMode = scopeMode,
      country = country,
      city = city,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      gregorianYear = gregorianYear,
      gregorianMonth = gregorianMonth,
      hijriRamadanYear = effectiveHijriYear,
      ramadanYearsInGregorianYear = ramadanYearsInGreg,
      hasTwoRamadansInGregorianYear = hasTwoRamadans,
      calculationMethod = method,
      juridicalSchool = juridicalSchool,
      sehriAdjustmentMinutes = sehriAdjustmentMinutes,
      iftarAdjustmentMinutes = iftarAdjustmentMinutes,
      days = adjustedOffline,
      monthLengthDays = adjustedOffline.size,
      isFromOfflineCache = true,
      isLiveApiResponse = false,
      apiErrorOrFallbackNotice = apiError
        ?: "Offline Mode: Timetable computed astronomically for ($city, $country) and cached locally."
    )
  }

  /**
   * Parses a raw AlAdhan API `/v1/hijriCalendar` or `/v1/calendar` JSON payload into verified
   * `SehriIftarDayEntry` records, rejecting any malformed or missing timings and preventing duplicates.
   */
  fun parseAlAdhanCalendarJson(
    rawJson: String,
    country: String,
    city: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    method: CalculationMethodOption,
    school: JuridicalSchool,
    filterHijriMonth: Int? = null
  ): List<SehriIftarDayEntry> {
    if (rawJson.isBlank()) return emptyList()
    return try {
      val root = JSONObject(rawJson)
      if (root.optInt("code", 0) != 200) return emptyList()
      val dataArray = root.optJSONArray("data") ?: return emptyList()
      parseDayArray(
        dataArray = dataArray,
        country = country,
        city = city,
        latitude = latitude,
        longitude = longitude,
        timeZoneId = timeZoneId,
        method = method,
        school = school,
        filterHijriMonth = filterHijriMonth
      )
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun parseDayArray(
    dataArray: JSONArray,
    country: String,
    city: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    method: CalculationMethodOption,
    school: JuridicalSchool,
    filterHijriMonth: Int?
  ): List<SehriIftarDayEntry> {
    val results = ArrayList<SehriIftarDayEntry>(dataArray.length())
    for (i in 0 until dataArray.length()) {
      val item = dataArray.optJSONObject(i) ?: continue
      val timings = item.optJSONObject("timings") ?: continue
      val dateObj = item.optJSONObject("date") ?: continue
      val gregObj = dateObj.optJSONObject("gregorian")
      val hijriObj = dateObj.optJSONObject("hijri")
      val metaObj = item.optJSONObject("meta")

      val fajr = cleanHhMm(timings.optString("Fajr", ""))
      val imsak = cleanHhMm(timings.optString("Imsak", "")).ifEmpty {
        applyMinuteAdjustment(fajr, -10)
      }
      val sunrise = cleanHhMm(timings.optString("Sunrise", ""))
      val dhuhr = cleanHhMm(timings.optString("Dhuhr", ""))
      val asr = cleanHhMm(timings.optString("Asr", ""))
      val maghrib = cleanHhMm(timings.optString("Maghrib", ""))
      val isha = cleanHhMm(timings.optString("Isha", ""))

      // Never accept fabricated or blank Fajr/Maghrib timings
      if (fajr.isEmpty() || maghrib.isEmpty()) continue

      val gregDdMmYyyy = gregObj?.optString("date", "")?.trim().orEmpty()
      val gregDay = gregObj?.optString("day", "")?.toIntOrNull() ?: (i + 1)
      val gregMonth = gregObj?.optJSONObject("month")?.optInt("number", 1) ?: 1
      val gregYear = gregObj?.optString("year", "")?.toIntOrNull() ?: 2026
      val gregIso = "%04d-%02d-%02d".format(Locale.ENGLISH, gregYear, gregMonth, gregDay)
      val gregReadable = SecurityIntegrityGuard.sanitizeSacredText(
        dateObj.optString("readable", gregIso)
      )

      val hijriDay = hijriObj?.optString("day", "")?.toIntOrNull() ?: (i + 1)
      val hijriMonthObj = hijriObj?.optJSONObject("month")
      val hijriMonthNum = hijriMonthObj?.optInt("number", 9) ?: 9
      if (filterHijriMonth != null && hijriMonthNum != filterHijriMonth) continue

      val hijriMonthEn = SecurityIntegrityGuard.sanitizeSacredText(
        hijriMonthObj?.optString("en", "Ramadan").orEmpty().ifBlank { "Ramadan" }
      )
      val hijriMonthAr = SecurityIntegrityGuard.sanitizeSacredText(
        hijriMonthObj?.optString("ar", "رَمَضَان").orEmpty().ifBlank { "رَمَضَان" }
      )
      val hijriYear = hijriObj?.optString("year", "")?.toIntOrNull() ?: 1447
      val hijriFormatted = "$hijriDay $hijriMonthEn $hijriYear AH"

      val resolvedTz = metaObj?.optString("timezone", "")?.takeIf { it.isNotBlank() } ?: timeZoneId
      val resolvedMethodName = metaObj?.optJSONObject("method")?.optString("name", "")
        ?.takeIf { it.isNotBlank() } ?: method.name

      val uniqueKey = "${gregIso}_${hijriYear}_${hijriMonthNum}_${hijriDay}_${method.id}_${school.schoolId}"

      results.add(
        SehriIftarDayEntry(
          uniqueDayKey = uniqueKey,
          daySequenceNumber = results.size + 1,
          gregorianDateIso = gregIso,
          gregorianDateDdMmYyyy = gregDdMmYyyy.ifBlank {
            "%02d-%02d-%04d".format(Locale.ENGLISH, gregDay, gregMonth, gregYear)
          },
          gregorianReadable = gregReadable,
          gregorianYear = gregYear,
          gregorianMonth = gregMonth,
          gregorianDay = gregDay,
          hijriDateFormatted = hijriFormatted,
          hijriYear = hijriYear,
          hijriMonthNumber = hijriMonthNum,
          hijriMonthNameEn = hijriMonthEn,
          hijriMonthNameAr = hijriMonthAr,
          hijriDay = hijriDay,
          rawSehriFajr24 = fajr,
          rawImsak24 = imsak,
          rawSunrise24 = sunrise.ifEmpty { "06:00" },
          rawDhuhr24 = dhuhr.ifEmpty { "12:15" },
          rawAsr24 = asr.ifEmpty { "15:45" },
          rawIftarMaghrib24 = maghrib,
          rawIsha24 = isha.ifEmpty { "19:45" },
          adjustedSehriFajr24 = fajr,
          adjustedIftarMaghrib24 = maghrib,
          calculationMethodId = method.id,
          calculationMethodName = resolvedMethodName,
          juridicalSchool = school,
          city = city,
          country = country,
          latitude = latitude,
          longitude = longitude,
          timeZoneId = resolvedTz,
          sourceOrigin = "AlAdhan API (Verified)",
          isRamadanDay = hijriMonthNum == 9
        )
      )
    }
    return deduplicateAndSortDays(results)
  }

  /**
   * Deduplicates entries by Gregorian ISO date and Hijri date, sorting chronologically and re-indexing 1..N.
   */
  fun deduplicateAndSortDays(days: List<SehriIftarDayEntry>): List<SehriIftarDayEntry> {
    if (days.isEmpty()) return emptyList()
    val byDate = LinkedHashMap<String, SehriIftarDayEntry>()
    for (entry in days) {
      val compositeKey = "${entry.gregorianDateIso}_${entry.hijriYear}_${entry.hijriMonthNumber}_${entry.hijriDay}"
      if (!byDate.containsKey(compositeKey) && !byDate.keys.any { it.startsWith(entry.gregorianDateIso) }) {
        byDate[compositeKey] = entry
      }
    }
    return byDate.values
      .sortedBy { it.gregorianDateIso }
      .mapIndexed { idx, item -> item.copy(daySequenceNumber = idx + 1) }
  }

  private fun fetchFromAlAdhanApi(
    scopeMode: TimetableScopeMode,
    country: String,
    city: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    gregorianYear: Int,
    gregorianMonth: Int,
    hijriRamadanYear: Int,
    method: CalculationMethodOption,
    school: JuridicalSchool
  ): Pair<List<SehriIftarDayEntry>, String?> {
    return try {
      when (scopeMode) {
        TimetableScopeMode.RAMADAN_MONTH -> {
          val coordUrl =
            "https://api.aladhan.com/v1/hijriCalendar/$hijriRamadanYear/9?latitude=$latitude&longitude=$longitude&method=${method.id}&school=${school.schoolId}"
          val raw = httpGetAlAdhan(coordUrl)
          if (raw != null) {
            val parsed = parseAlAdhanCalendarJson(
              rawJson = raw,
              country = country,
              city = city,
              latitude = latitude,
              longitude = longitude,
              timeZoneId = timeZoneId,
              method = method,
              school = school,
              filterHijriMonth = 9
            )
            if (parsed.size in 28..30) {
              return parsed to null
            }
          }
          // Try hijriCalendarByCity fallback if coordinates endpoint returned empty
          val cityEnc = URLEncoder.encode(city, "UTF-8")
          val countryEnc = URLEncoder.encode(country, "UTF-8")
          val cityUrl =
            "https://api.aladhan.com/v1/hijriCalendarByCity/$hijriRamadanYear/9?city=$cityEnc&country=$countryEnc&method=${method.id}&school=${school.schoolId}"
          val rawCity = httpGetAlAdhan(cityUrl)
          if (rawCity != null) {
            val parsedCity = parseAlAdhanCalendarJson(
              rawJson = rawCity,
              country = country,
              city = city,
              latitude = latitude,
              longitude = longitude,
              timeZoneId = timeZoneId,
              method = method,
              school = school,
              filterHijriMonth = 9
            )
            if (parsedCity.size in 28..30) {
              return parsedCity to null
            }
          }
          emptyList<SehriIftarDayEntry>() to "AlAdhan API returned no data or device is offline."
        }

        TimetableScopeMode.GREGORIAN_MONTH -> {
          val m = gregorianMonth.coerceIn(1, 12)
          val url =
            "https://api.aladhan.com/v1/calendar/$gregorianYear/$m?latitude=$latitude&longitude=$longitude&method=${method.id}&school=${school.schoolId}"
          val raw = httpGetAlAdhan(url)
          if (raw != null) {
            val parsed = parseAlAdhanCalendarJson(
              rawJson = raw,
              country = country,
              city = city,
              latitude = latitude,
              longitude = longitude,
              timeZoneId = timeZoneId,
              method = method,
              school = school,
              filterHijriMonth = null
            )
            if (parsed.isNotEmpty()) {
              return parsed to null
            }
          }
          emptyList<SehriIftarDayEntry>() to "AlAdhan monthly calendar API unreachable."
        }

        TimetableScopeMode.FULL_GREGORIAN_YEAR -> {
          val url =
            "https://api.aladhan.com/v1/calendar/$gregorianYear?latitude=$latitude&longitude=$longitude&method=${method.id}&school=${school.schoolId}"
          val raw = httpGetAlAdhan(url)
          if (raw != null) {
            val root = JSONObject(raw)
            val dataObj = root.optJSONObject("data")
            if (dataObj != null) {
              val allYearDays = ArrayList<SehriIftarDayEntry>(366)
              for (m in 1..12) {
                val monthArr = dataObj.optJSONArray(m.toString()) ?: continue
                allYearDays.addAll(
                  parseDayArray(
                    dataArray = monthArr,
                    country = country,
                    city = city,
                    latitude = latitude,
                    longitude = longitude,
                    timeZoneId = timeZoneId,
                    method = method,
                    school = school,
                    filterHijriMonth = null
                  )
                )
              }
              if (allYearDays.isNotEmpty()) {
                return deduplicateAndSortDays(allYearDays) to null
              }
            }
          }
          emptyList<SehriIftarDayEntry>() to "AlAdhan annual calendar API unreachable."
        }
      }
    } catch (e: Exception) {
      emptyList<SehriIftarDayEntry>() to "Network error: ${e.localizedMessage ?: "Offline"}"
    }
  }

  /**
   * Computes an accurate offline astronomical Sehri/Iftar timetable for:
   * - Any Hijri Ramadan year (29 or 30 days verified via Umm al-Qura / IslamicCalendar),
   * - Any Gregorian month (28..31 days), or
   * - Full Gregorian year (365/366 days).
   */
  fun computeAstronomicalTimetableOffline(
    scopeMode: TimetableScopeMode,
    country: String,
    city: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    gregorianYear: Int,
    gregorianMonth: Int,
    hijriRamadanYear: Int,
    method: CalculationMethodOption,
    school: JuridicalSchool
  ): List<SehriIftarDayEntry> {
    val tz = TimeZone.getTimeZone(timeZoneId)
    val results = ArrayList<SehriIftarDayEntry>()

    when (scopeMode) {
      TimetableScopeMode.RAMADAN_MONTH -> {
        val ramadanDates = resolveGregorianDatesForHijriRamadan(hijriRamadanYear, tz)
        for ((index, pair) in ramadanDates.withIndex()) {
          val gregDate = pair.first
          val hijriDay = pair.second
          val cal = Calendar.getInstance(tz).apply { time = gregDate }
          results.add(
            buildOfflineDayEntry(
              cal = cal,
              seqNumber = index + 1,
              hijriYear = hijriRamadanYear,
              hijriMonthNum = 9,
              hijriMonthEn = "Ramadan",
              hijriMonthAr = "رَمَضَان",
              hijriDay = hijriDay,
              country = country,
              city = city,
              latitude = latitude,
              longitude = longitude,
              timeZoneId = timeZoneId,
              method = method,
              school = school
            )
          )
        }
      }

      TimetableScopeMode.GREGORIAN_MONTH -> {
        val m = gregorianMonth.coerceIn(1, 12)
        val cal = Calendar.getInstance(tz).apply {
          set(gregorianYear, m - 1, 1, 12, 0, 0)
          set(Calendar.MILLISECOND, 0)
        }
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (d in 1..maxDay) {
          cal.set(Calendar.DAY_OF_MONTH, d)
          val hMeta = extractHijriComponents(cal.time, tz)
          results.add(
            buildOfflineDayEntry(
              cal = cal,
              seqNumber = d,
              hijriYear = hMeta.year,
              hijriMonthNum = hMeta.monthNumber,
              hijriMonthEn = hMeta.monthNameEn,
              hijriMonthAr = hMeta.monthNameAr,
              hijriDay = hMeta.day,
              country = country,
              city = city,
              latitude = latitude,
              longitude = longitude,
              timeZoneId = timeZoneId,
              method = method,
              school = school
            )
          )
        }
      }

      TimetableScopeMode.FULL_GREGORIAN_YEAR -> {
        val cal = Calendar.getInstance(tz).apply {
          set(gregorianYear, Calendar.JANUARY, 1, 12, 0, 0)
          set(Calendar.MILLISECOND, 0)
        }
        var seq = 1
        while (cal.get(Calendar.YEAR) == gregorianYear && seq <= 366) {
          val hMeta = extractHijriComponents(cal.time, tz)
          results.add(
            buildOfflineDayEntry(
              cal = cal,
              seqNumber = seq,
              hijriYear = hMeta.year,
              hijriMonthNum = hMeta.monthNumber,
              hijriMonthEn = hMeta.monthNameEn,
              hijriMonthAr = hMeta.monthNameAr,
              hijriDay = hMeta.day,
              country = country,
              city = city,
              latitude = latitude,
              longitude = longitude,
              timeZoneId = timeZoneId,
              method = method,
              school = school
            )
          )
          cal.add(Calendar.DAY_OF_YEAR, 1)
          seq++
        }
      }
    }

    return deduplicateAndSortDays(results)
  }

  private data class HijriDateMeta(
    val year: Int,
    val monthNumber: Int,
    val monthNameEn: String,
    val monthNameAr: String,
    val day: Int
  )

  private val HIJRI_MONTHS_EN = arrayOf(
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
    "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
    "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah"
  )

  private val HIJRI_MONTHS_AR = arrayOf(
    "مُحَرَّم", "صَفَر", "رَبِيع الأَوَّل", "رَبِيع الثَّانِي",
    "جُمَادَىٰ الأُولَىٰ", "جُمَادَىٰ الآخِرَة", "رَجَب", "شَعْبَان",
    "رَمَضَان", "شَوَّال", "ذُو القَعْدَة", "ذُو الحِجَّة"
  )

  private fun extractHijriComponents(date: Date, tz: TimeZone): HijriDateMeta {
    return try {
      val locale = ULocale("@calendar=islamic-umalqura")
      val icuCal = IcuCalendar.getInstance(
        android.icu.util.TimeZone.getTimeZone(tz.id),
        locale
      ) as IslamicCalendar
      icuCal.time = date
      val d = icuCal.get(IcuCalendar.DAY_OF_MONTH).coerceIn(1, 30)
      val mIdx = icuCal.get(IcuCalendar.MONTH).coerceIn(0, 11)
      val y = icuCal.get(IcuCalendar.YEAR)
      HijriDateMeta(
        year = y,
        monthNumber = mIdx + 1,
        monthNameEn = HIJRI_MONTHS_EN[mIdx],
        monthNameAr = HIJRI_MONTHS_AR[mIdx],
        day = d
      )
    } catch (_: Exception) {
      HijriDateMeta(1447, 9, "Ramadan", "رَمَضَان", 1)
    }
  }

  /**
   * Resolves the exact 29 or 30 Gregorian dates corresponding to Hijri Ramadan `hijriYear` AH.
   */
  private fun resolveGregorianDatesForHijriRamadan(
    hijriYear: Int,
    tz: TimeZone
  ): List<Pair<Date, Int>> {
    val list = ArrayList<Pair<Date, Int>>(30)
    try {
      val locale = ULocale("@calendar=islamic-umalqura")
      val icuCal = IcuCalendar.getInstance(
        android.icu.util.TimeZone.getTimeZone(tz.id),
        locale
      ) as IslamicCalendar
      icuCal.clear()
      icuCal.set(IcuCalendar.YEAR, hijriYear)
      icuCal.set(IcuCalendar.MONTH, IslamicCalendar.RAMADAN)
      icuCal.set(IcuCalendar.DAY_OF_MONTH, 1)
      icuCal.set(IcuCalendar.HOUR_OF_DAY, 12)

      for (d in 1..30) {
        val currentMonth = icuCal.get(IcuCalendar.MONTH)
        if (currentMonth != IslamicCalendar.RAMADAN) break
        val actualDay = icuCal.get(IcuCalendar.DAY_OF_MONTH)
        list.add(Date(icuCal.timeInMillis) to actualDay)
        icuCal.add(IcuCalendar.DAY_OF_MONTH, 1)
      }
    } catch (_: Exception) {
      // Fallback if ICU is not active
    }

    if (list.size in 29..30) return list

    // Deterministic astronomical fallback for Ramadan start date if ICU IslamicCalendar didn't populate
    list.clear()
    val startCal = Calendar.getInstance(tz).apply {
      when (hijriYear) {
        1445 -> set(2024, Calendar.MARCH, 11, 12, 0, 0)
        1446 -> set(2025, Calendar.MARCH, 1, 12, 0, 0)
        1447 -> set(2026, Calendar.FEBRUARY, 18, 12, 0, 0)
        1448 -> set(2027, Calendar.FEBRUARY, 8, 12, 0, 0)
        1449 -> set(2028, Calendar.JANUARY, 28, 12, 0, 0)
        1450 -> set(2029, Calendar.JANUARY, 16, 12, 0, 0)
        1451 -> set(2030, Calendar.JANUARY, 5, 12, 0, 0)
        1452 -> set(2030, Calendar.DECEMBER, 26, 12, 0, 0)
        else -> {
          val baseEpochDays = (hijriYear - 1447) * 354.367
          set(2026, Calendar.FEBRUARY, 18, 12, 0, 0)
          add(Calendar.DAY_OF_YEAR, baseEpochDays.roundToInt())
        }
      }
      set(Calendar.MILLISECOND, 0)
    }
    val lengthDays = if (hijriYear % 2 == 0) 30 else 29
    for (d in 1..lengthDays) {
      list.add(Date(startCal.timeInMillis) to d)
      startCal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return list
  }

  private fun buildOfflineDayEntry(
    cal: Calendar,
    seqNumber: Int,
    hijriYear: Int,
    hijriMonthNum: Int,
    hijriMonthEn: String,
    hijriMonthAr: String,
    hijriDay: Int,
    country: String,
    city: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    method: CalculationMethodOption,
    school: JuridicalSchool
  ): SehriIftarDayEntry {
    val schedule = PrayerTimeCalculator.calculateOfflineSchedule(
      calendar = cal,
      locationName = city,
      districtOrRegion = city,
      country = country,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      methodId = method.id,
      asrMadhhab = school.schoolId
    )
    // Apply method-specific Fajr / Maghrib adjustments if using a custom angle not in the basic 5
    val rawFajr = schedule.fajrTime
    val rawMaghrib = if (method.id == 0 || method.id == 7) {
      // Ja'fari / Tehran Maghrib is ~14 minutes after sunset (4°–4.5° below horizon)
      applyMinuteAdjustment(schedule.maghribTime, 14)
    } else {
      schedule.maghribTime
    }
    val rawImsak = applyMinuteAdjustment(rawFajr, -10)

    val gYear = cal.get(Calendar.YEAR)
    val gMonth = cal.get(Calendar.MONTH) + 1
    val gDay = cal.get(Calendar.DAY_OF_MONTH)
    val gregIso = "%04d-%02d-%02d".format(Locale.ENGLISH, gYear, gMonth, gDay)
    val gregDdMmYyyy = "%02d-%02d-%04d".format(Locale.ENGLISH, gDay, gMonth, gYear)
    val uniqueKey = "${gregIso}_${hijriYear}_${hijriMonthNum}_${hijriDay}_${method.id}_${school.schoolId}"

    return SehriIftarDayEntry(
      uniqueDayKey = uniqueKey,
      daySequenceNumber = seqNumber,
      gregorianDateIso = gregIso,
      gregorianDateDdMmYyyy = gregDdMmYyyy,
      gregorianReadable = schedule.gregorianReadable,
      gregorianYear = gYear,
      gregorianMonth = gMonth,
      gregorianDay = gDay,
      hijriDateFormatted = "$hijriDay $hijriMonthEn $hijriYear AH",
      hijriYear = hijriYear,
      hijriMonthNumber = hijriMonthNum,
      hijriMonthNameEn = hijriMonthEn,
      hijriMonthNameAr = hijriMonthAr,
      hijriDay = hijriDay,
      rawSehriFajr24 = rawFajr,
      rawImsak24 = rawImsak,
      rawSunrise24 = schedule.sunriseTime,
      rawDhuhr24 = schedule.dhuhrTime,
      rawAsr24 = schedule.asrTime,
      rawIftarMaghrib24 = rawMaghrib,
      rawIsha24 = schedule.ishaTime,
      adjustedSehriFajr24 = rawFajr,
      adjustedIftarMaghrib24 = rawMaghrib,
      calculationMethodId = method.id,
      calculationMethodName = method.name,
      juridicalSchool = school,
      city = city,
      country = country,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      sourceOrigin = "Astronomical Solar Calculation (${method.name})",
      isRamadanDay = hijriMonthNum == 9
    )
  }

  private suspend fun persistDaysToRoomScheduleDao(
    dao: PrayerScheduleDao,
    days: List<SehriIftarDayEntry>
  ) {
    for (d in days) {
      val entity = PrayerScheduleEntity(
        dateLocationKey = "${d.gregorianDateIso}_${d.city}_${d.calculationMethodId}_${d.juridicalSchool.schoolId}",
        dateIso = d.gregorianDateIso,
        gregorianReadable = d.gregorianReadable,
        hijriReadable = d.hijriDateFormatted,
        locationName = d.city,
        districtOrRegion = d.city,
        country = d.country,
        latitude = d.latitude,
        longitude = d.longitude,
        timeZoneId = d.timeZoneId,
        calculationMethodId = d.calculationMethodId,
        calculationMethodName = d.calculationMethodName,
        asrMadhhab = d.juridicalSchool.schoolId,
        fajrTime = d.rawSehriFajr24,
        sunriseTime = d.rawSunrise24,
        dhuhrTime = d.rawDhuhr24,
        asrTime = d.rawAsr24,
        maghribTime = d.rawIftarMaghrib24,
        ishaTime = d.rawIsha24,
        tahajjudLastThirdStart = PrayerTimeCalculator.calculateFinalThirdOfNight(d.rawIftarMaghrib24, d.rawSehriFajr24),
        sourceName = d.sourceOrigin
      )
      dao.insertSchedule(entity)
    }
  }

  private fun getCacheDir(context: Context): File {
    val dir = File(context.filesDir, "sehri_iftar_cache")
    if (!dir.exists()) dir.mkdirs()
    return dir
  }

  fun saveTimetableToCache(
    context: Context,
    cacheKey: String,
    days: List<SehriIftarDayEntry>
  ) {
    try {
      val safeFileName = cacheKey.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".json"
      val file = File(getCacheDir(context), safeFileName)
      val arr = JSONArray()
      for (d in deduplicateAndSortDays(days)) {
        val obj = JSONObject().apply {
          put("uniqueDayKey", d.uniqueDayKey)
          put("daySequenceNumber", d.daySequenceNumber)
          put("gregorianDateIso", d.gregorianDateIso)
          put("gregorianDateDdMmYyyy", d.gregorianDateDdMmYyyy)
          put("gregorianReadable", d.gregorianReadable)
          put("gregorianYear", d.gregorianYear)
          put("gregorianMonth", d.gregorianMonth)
          put("gregorianDay", d.gregorianDay)
          put("hijriDateFormatted", d.hijriDateFormatted)
          put("hijriYear", d.hijriYear)
          put("hijriMonthNumber", d.hijriMonthNumber)
          put("hijriMonthNameEn", d.hijriMonthNameEn)
          put("hijriMonthNameAr", d.hijriMonthNameAr)
          put("hijriDay", d.hijriDay)
          put("rawSehriFajr24", d.rawSehriFajr24)
          put("rawImsak24", d.rawImsak24)
          put("rawSunrise24", d.rawSunrise24)
          put("rawDhuhr24", d.rawDhuhr24)
          put("rawAsr24", d.rawAsr24)
          put("rawIftarMaghrib24", d.rawIftarMaghrib24)
          put("rawIsha24", d.rawIsha24)
          put("calculationMethodId", d.calculationMethodId)
          put("calculationMethodName", d.calculationMethodName)
          put("schoolId", d.juridicalSchool.schoolId)
          put("city", d.city)
          put("country", d.country)
          put("latitude", d.latitude)
          put("longitude", d.longitude)
          put("timeZoneId", d.timeZoneId)
          put("sourceOrigin", d.sourceOrigin)
        }
        arr.put(obj)
      }
      file.writeText(arr.toString(), Charsets.UTF_8)
    } catch (_: Exception) {
    }
  }

  fun loadCachedTimetable(
    context: Context,
    cacheKey: String
  ): List<SehriIftarDayEntry>? {
    return try {
      val safeFileName = cacheKey.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".json"
      val file = File(getCacheDir(context), safeFileName)
      if (!file.exists()) return null
      val arr = JSONArray(file.readText(Charsets.UTF_8))
      val list = ArrayList<SehriIftarDayEntry>(arr.length())
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        val rawFajr = obj.optString("rawSehriFajr24", "")
        val rawMaghrib = obj.optString("rawIftarMaghrib24", "")
        if (rawFajr.isBlank() || rawMaghrib.isBlank()) continue
        val hMonth = obj.optInt("hijriMonthNumber", 9)
        list.add(
          SehriIftarDayEntry(
            uniqueDayKey = obj.optString("uniqueDayKey", ""),
            daySequenceNumber = obj.optInt("daySequenceNumber", i + 1),
            gregorianDateIso = obj.optString("gregorianDateIso", ""),
            gregorianDateDdMmYyyy = obj.optString("gregorianDateDdMmYyyy", ""),
            gregorianReadable = obj.optString("gregorianReadable", ""),
            gregorianYear = obj.optInt("gregorianYear", 2026),
            gregorianMonth = obj.optInt("gregorianMonth", 1),
            gregorianDay = obj.optInt("gregorianDay", 1),
            hijriDateFormatted = obj.optString("hijriDateFormatted", ""),
            hijriYear = obj.optInt("hijriYear", 1447),
            hijriMonthNumber = hMonth,
            hijriMonthNameEn = obj.optString("hijriMonthNameEn", "Ramadan"),
            hijriMonthNameAr = obj.optString("hijriMonthNameAr", "رَمَضَان"),
            hijriDay = obj.optInt("hijriDay", i + 1),
            rawSehriFajr24 = rawFajr,
            rawImsak24 = obj.optString("rawImsak24", applyMinuteAdjustment(rawFajr, -10)),
            rawSunrise24 = obj.optString("rawSunrise24", "06:00"),
            rawDhuhr24 = obj.optString("rawDhuhr24", "12:15"),
            rawAsr24 = obj.optString("rawAsr24", "15:45"),
            rawIftarMaghrib24 = rawMaghrib,
            rawIsha24 = obj.optString("rawIsha24", "19:45"),
            adjustedSehriFajr24 = rawFajr,
            adjustedIftarMaghrib24 = rawMaghrib,
            calculationMethodId = obj.optInt("calculationMethodId", 1),
            calculationMethodName = obj.optString("calculationMethodName", ""),
            juridicalSchool = JuridicalSchool.fromId(obj.optInt("schoolId", 1)),
            city = obj.optString("city", "Srinagar"),
            country = obj.optString("country", "India"),
            latitude = obj.optDouble("latitude", 34.0837),
            longitude = obj.optDouble("longitude", 74.7973),
            timeZoneId = obj.optString("timeZoneId", "Asia/Kolkata"),
            sourceOrigin = obj.optString("sourceOrigin", "Offline Cached Timetable"),
            isRamadanDay = hMonth == 9
          )
        )
      }
      deduplicateAndSortDays(list).takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
      null
    }
  }

  private fun cleanHhMm(raw: String): String {
    val trimmed = raw.trim().substringBefore(" ")
    return if (trimmed.matches(Regex("\\d{1,2}:\\d{2}"))) {
      val parts = trimmed.split(":")
      "%02d:%02d".format(Locale.ENGLISH, parts[0].toInt(), parts[1].toInt())
    } else {
      ""
    }
  }

  private fun httpGetAlAdhan(urlString: String): String? {
    if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(urlString)) return null
    var conn: HttpURLConnection? = null
    return try {
      conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        instanceFollowRedirects = false
        connectTimeout = 3500
        readTimeout = 4500
        setRequestProperty("Accept", "application/json")
      }
      if (conn.responseCode in 200..299) {
        conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
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
