package com.example.data.prayer

import android.icu.util.Calendar as IcuCalendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import com.example.data.local.entities.PrayerScheduleEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class PrayerLocationPreset(
  val city: String,
  val districtOrRegion: String,
  val country: String,
  val latitude: Double,
  val longitude: Double,
  val timeZoneId: String,
  val defaultMethodId: Int = 1,
  val defaultAsrMadhhab: Int = 1
)

data class CalculationMethodOption(
  val id: Int,
  val name: String,
  val fajrAngle: Double,
  val ishaAngle: Double,
  val ishaMinutesAfterMaghrib: Int = 0,
  val regionDescription: String
)

data class NextPrayerStatus(
  val nextPrayerName: String,
  val nextPrayerTime24: String,
  val countdownFormatted: String,
  val remainingSeconds: Long,
  val currentActivePrayer: String
)

object PrayerTimeCalculator {

  const val FAJR_TATHWIB_ARABIC = "الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ"
  const val FAJR_TATHWIB_TRANSLITERATION = "As-salatu khayrun minan-nawm."
  const val FAJR_TATHWIB_ENGLISH = "Prayer is better than sleep."
  const val FAJR_TATHWIB_URDU = "نماز نیند سے بہتر ہے۔"

  val locationPresets: List<PrayerLocationPreset> = listOf(
    PrayerLocationPreset(
      city = "Srinagar",
      districtOrRegion = "Srinagar District, Kashmir",
      country = "India",
      latitude = 34.0837,
      longitude = 74.7973,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "Anantnag",
      districtOrRegion = "Anantnag District, Kashmir",
      country = "India",
      latitude = 33.7311,
      longitude = 75.1487,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "Baramulla",
      districtOrRegion = "Baramulla District, Kashmir",
      country = "India",
      latitude = 34.2021,
      longitude = 74.3483,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "Pulwama",
      districtOrRegion = "Pulwama District, Kashmir",
      country = "India",
      latitude = 33.8716,
      longitude = 74.8946,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "Kupwara",
      districtOrRegion = "Kupwara District, Kashmir",
      country = "India",
      latitude = 34.5262,
      longitude = 74.2546,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "Jammu",
      districtOrRegion = "Jammu Division, J&K",
      country = "India",
      latitude = 32.7266,
      longitude = 74.8570,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "New Delhi",
      districtOrRegion = "Delhi NCR",
      country = "India",
      latitude = 28.6139,
      longitude = 77.2090,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "Mumbai",
      districtOrRegion = "Maharashtra",
      country = "India",
      latitude = 19.0760,
      longitude = 72.8777,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "Hyderabad",
      districtOrRegion = "Telangana",
      country = "India",
      latitude = 17.3850,
      longitude = 78.4867,
      timeZoneId = "Asia/Kolkata",
      defaultMethodId = 1,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "Makkah al-Mukarramah",
      districtOrRegion = "Makkah Province",
      country = "Saudi Arabia",
      latitude = 21.4225,
      longitude = 39.8262,
      timeZoneId = "Asia/Riyadh",
      defaultMethodId = 4,
      defaultAsrMadhhab = 0
    ),
    PrayerLocationPreset(
      city = "Al-Madinah al-Munawwarah",
      districtOrRegion = "Madinah Province",
      country = "Saudi Arabia",
      latitude = 24.4672,
      longitude = 39.6111,
      timeZoneId = "Asia/Riyadh",
      defaultMethodId = 4,
      defaultAsrMadhhab = 0
    ),
    PrayerLocationPreset(
      city = "Cairo",
      districtOrRegion = "Cairo Governorate",
      country = "Egypt",
      latitude = 30.0444,
      longitude = 31.2357,
      timeZoneId = "Africa/Cairo",
      defaultMethodId = 5,
      defaultAsrMadhhab = 0
    ),
    PrayerLocationPreset(
      city = "Istanbul",
      districtOrRegion = "Marmara",
      country = "Turkey",
      latitude = 41.0082,
      longitude = 28.9784,
      timeZoneId = "Europe/Istanbul",
      defaultMethodId = 3,
      defaultAsrMadhhab = 1
    ),
    PrayerLocationPreset(
      city = "London",
      districtOrRegion = "Greater London",
      country = "United Kingdom",
      latitude = 51.5074,
      longitude = -0.1278,
      timeZoneId = "Europe/London",
      defaultMethodId = 3,
      defaultAsrMadhhab = 0
    ),
    PrayerLocationPreset(
      city = "New York",
      districtOrRegion = "New York",
      country = "United States",
      latitude = 40.7128,
      longitude = -74.0060,
      timeZoneId = "America/New_York",
      defaultMethodId = 2,
      defaultAsrMadhhab = 0
    )
  )

  val calculationMethods: List<CalculationMethodOption> = listOf(
    CalculationMethodOption(
      id = 1,
      name = "University of Islamic Sciences, Karachi",
      fajrAngle = 18.0,
      ishaAngle = 18.0,
      regionDescription = "Kashmir, India, Pakistan, Bangladesh & South Asia"
    ),
    CalculationMethodOption(
      id = 3,
      name = "Muslim World League (MWL)",
      fajrAngle = 18.0,
      ishaAngle = 17.0,
      regionDescription = "Europe, Far East & Global Standard"
    ),
    CalculationMethodOption(
      id = 4,
      name = "Umm al-Qura University, Makkah",
      fajrAngle = 18.5,
      ishaAngle = 0.0,
      ishaMinutesAfterMaghrib = 90,
      regionDescription = "Arabian Peninsula & Gulf"
    ),
    CalculationMethodOption(
      id = 5,
      name = "Egyptian General Authority of Survey",
      fajrAngle = 19.5,
      ishaAngle = 17.5,
      regionDescription = "Africa, Syria, Lebanon & Middle East"
    ),
    CalculationMethodOption(
      id = 2,
      name = "Islamic Society of North America (ISNA)",
      fajrAngle = 15.0,
      ishaAngle = 15.0,
      regionDescription = "North America & Canada"
    )
  )

  /**
   * Offline astronomical solar position prayer time calculator (Jean Meeus / PrayTimes standard equations).
   * Guarantees accurate 5 daily Salah times + Sunrise + Tahajjud final third of the night even without internet.
   */
  fun calculateOfflineSchedule(
    calendar: Calendar = Calendar.getInstance(),
    locationName: String,
    districtOrRegion: String = "",
    country: String = "",
    latitude: Double,
    longitude: Double,
    timeZoneId: String = TimeZone.getDefault().id,
    methodId: Int = 1,
    asrMadhhab: Int = 1, // 0 = Standard (Shafi'i), 1 = Hanafi
    highLatitudeRule: Int = 0 // 0 = AngleBased, 1 = MidNight, 2 = OneSeventh
  ): PrayerScheduleEntity {
    val tz = TimeZone.getTimeZone(timeZoneId)
    val localCal = (calendar.clone() as Calendar).apply { timeZone = tz }

    val year = localCal.get(Calendar.YEAR)
    val month = localCal.get(Calendar.MONTH) + 1
    val day = localCal.get(Calendar.DAY_OF_MONTH)

    val method = calculationMethods.find { it.id == methodId } ?: calculationMethods.first()

    // Time zone offset in hours including DST
    val tzOffsetHours = tz.getOffset(localCal.timeInMillis) / (1000.0 * 3600.0)

    // Julian Date at noon
    val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)
    val sunParams = sunPosition(jd)
    val decl = sunParams.first
    val eqTime = sunParams.second

    // Solar Noon (Dhuhr) in hours (0..24)
    val dhuhrHours = normalizeHours(12.0 + tzOffsetHours - longitude / 15.0 - eqTime + (2.0 / 60.0)) // +2 min precaution post-zenith

    val sunriseAngle = -0.833
    val sunriseDelta = sunHourAngle(latitude, decl, sunriseAngle) ?: 6.0
    val sunriseHours = normalizeHours(12.0 + tzOffsetHours - longitude / 15.0 - eqTime - sunriseDelta)
    val maghribHours = normalizeHours(12.0 + tzOffsetHours - longitude / 15.0 - eqTime + sunriseDelta + (2.0 / 60.0))

    val nightDuration = normalizeDurationHours(sunriseHours - maghribHours)

    // Fajr
    val fajrDeltaRaw = sunHourAngle(latitude, decl, -method.fajrAngle)
    val fajrDelta = fajrDeltaRaw ?: adjustHighLatitudePortion(method.fajrAngle, nightDuration, highLatitudeRule)
    val fajrHours = normalizeHours((12.0 + tzOffsetHours - longitude / 15.0 - eqTime) - fajrDelta)

    // Asr (shadow factor = 1 for Shafi'i/Standard, 2 for Hanafi)
    val shadowFactor = if (asrMadhhab == 1) 2.0 else 1.0
    val asrAltitudeRad = atan(1.0 / (shadowFactor + tan(degToRad(abs(latitude - decl)))))
    val asrAngleDeg = radToDeg(asrAltitudeRad)
    val asrDelta = sunHourAngle(latitude, decl, asrAngleDeg) ?: 3.2
    val asrHours = normalizeHours((12.0 + tzOffsetHours - longitude / 15.0 - eqTime) + asrDelta)

    // Isha
    val ishaHours = if (method.ishaMinutesAfterMaghrib > 0) {
      normalizeHours(maghribHours + method.ishaMinutesAfterMaghrib / 60.0)
    } else {
      val ishaDeltaRaw = sunHourAngle(latitude, decl, -method.ishaAngle)
      val ishaDelta = ishaDeltaRaw ?: adjustHighLatitudePortion(method.ishaAngle, nightDuration, highLatitudeRule)
      normalizeHours((12.0 + tzOffsetHours - longitude / 15.0 - eqTime) + ishaDelta)
    }

    val fajrStr = hoursToHhMm(fajrHours)
    val sunriseStr = hoursToHhMm(sunriseHours)
    val dhuhrStr = hoursToHhMm(dhuhrHours)
    val asrStr = hoursToHhMm(asrHours)
    val maghribStr = hoursToHhMm(maghribHours)
    val ishaStr = hoursToHhMm(ishaHours)

    val tahajjudStr = calculateFinalThirdOfNight(maghribStr, fajrStr)

    val dateIso = "%04d-%02d-%02d".format(year, month, day)
    val gregorianFmt = SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH).apply { timeZone = tz }
    val gregorianReadable = gregorianFmt.format(localCal.time)
    val hijriReadable = formatHijriDate(localCal.time, tz)

    return PrayerScheduleEntity(
      dateLocationKey = "${dateIso}_${locationName}_${method.id}_$asrMadhhab",
      dateIso = dateIso,
      gregorianReadable = gregorianReadable,
      hijriReadable = hijriReadable,
      locationName = locationName,
      districtOrRegion = districtOrRegion,
      country = country,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      calculationMethodId = method.id,
      calculationMethodName = method.name,
      asrMadhhab = asrMadhhab,
      highLatitudeRule = highLatitudeRule,
      fajrTime = fajrStr,
      sunriseTime = sunriseStr,
      dhuhrTime = dhuhrStr,
      asrTime = asrStr,
      maghribTime = maghribStr,
      ishaTime = ishaStr,
      tahajjudLastThirdStart = tahajjudStr,
      sourceName = "Offline Astronomical Calculation (${method.name})",
      lastUpdatedEpochMillis = System.currentTimeMillis()
    )
  }

  /**
   * Calculates the start of the final third of the night from the Maghrib-to-Fajr interval.
   */
  fun calculateFinalThirdOfNight(maghribHhMm: String, fajrHhMm: String): String {
    val maghribMins = hhMmToMinutes(maghribHhMm)
    val fajrMins = hhMmToMinutes(fajrHhMm)
    val totalNightMins = if (fajrMins >= maghribMins) {
      fajrMins - maghribMins
    } else {
      (24 * 60 - maghribMins) + fajrMins
    }
    val oneThirdMins = totalNightMins / 3
    val lastThirdStartMins = (fajrMins - oneThirdMins + 24 * 60) % (24 * 60)
    return "%02d:%02d".format(lastThirdStartMins / 60, lastThirdStartMins % 60)
  }

  /**
   * Calculates the wake-up time relative to Fajr (e.g. 60, 45, 30, 20, 15, or 10 minutes before Fajr).
   */
  fun calculateWakeUpTimeBeforeFajr(fajrHhMm: String, minutesBefore: Int): String {
    val fajrMins = hhMmToMinutes(fajrHhMm)
    val targetMins = (fajrMins - minutesBefore.coerceIn(1, 240) + 24 * 60) % (24 * 60)
    return "%02d:%02d".format(targetMins / 60, targetMins % 60)
  }

  /**
   * Computes which of the five daily prayers is next and a live HH:mm:ss countdown.
   */
  fun computeNextPrayerAndCountdown(
    schedule: PrayerScheduleEntity,
    nowMillis: Long = System.currentTimeMillis()
  ): NextPrayerStatus {
    val tz = TimeZone.getTimeZone(schedule.timeZoneId)
    val nowCal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }

    val prayers = listOf(
      "Fajr" to schedule.fajrTime,
      "Dhuhr" to schedule.dhuhrTime,
      "Asr" to schedule.asrTime,
      "Maghrib" to schedule.maghribTime,
      "Isha" to schedule.ishaTime
    )

    var currentPrayer = "Isha"
    for ((name, timeStr) in prayers) {
      val prayerCal = calendarForHhMm(nowCal, timeStr)
      if (prayerCal.timeInMillis > nowMillis) {
        val diffSec = ((prayerCal.timeInMillis - nowMillis) / 1000L).coerceAtLeast(0L)
        return NextPrayerStatus(
          nextPrayerName = name,
          nextPrayerTime24 = timeStr,
          countdownFormatted = formatSecondsToHhMmSs(diffSec),
          remainingSeconds = diffSec,
          currentActivePrayer = currentPrayer
        )
      }
      currentPrayer = name
    }

    // All 5 prayers for today have passed -> next prayer is tomorrow's Fajr
    val tomorrowFajrCal = calendarForHhMm(nowCal, schedule.fajrTime).apply {
      add(Calendar.DAY_OF_YEAR, 1)
    }
    val diffSec = ((tomorrowFajrCal.timeInMillis - nowMillis) / 1000L).coerceAtLeast(0L)
    return NextPrayerStatus(
      nextPrayerName = "Fajr",
      nextPrayerTime24 = schedule.fajrTime,
      countdownFormatted = formatSecondsToHhMmSs(diffSec),
      remainingSeconds = diffSec,
      currentActivePrayer = "Isha"
    )
  }

  fun formatHijriDate(date: Date, timeZone: TimeZone = TimeZone.getDefault()): String {
    return try {
      val locale = ULocale("@calendar=islamic-umalqura")
      val icuCal = IcuCalendar.getInstance(
        android.icu.util.TimeZone.getTimeZone(timeZone.id),
        locale
      ) as IslamicCalendar
      icuCal.time = date
      val day = icuCal.get(IcuCalendar.DAY_OF_MONTH)
      val monthIdx = icuCal.get(IcuCalendar.MONTH)
      val year = icuCal.get(IcuCalendar.YEAR)
      val monthNames = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah"
      )
      val mName = monthNames.getOrElse(monthIdx) { "Hijri" }
      "$day $mName $year AH"
    } catch (_: Exception) {
      "Hijri Calendar Active"
    }
  }

  fun hhMmToMinutes(hhMm: String): Int {
    val parts = hhMm.trim().split(":")
    if (parts.size < 2) return 0
    val h = parts[0].toIntOrNull() ?: 0
    val m = parts[1].toIntOrNull() ?: 0
    return (h * 60 + m).coerceIn(0, 24 * 60 - 1)
  }

  fun format24hTo12h(hhMm: String): String {
    val parts = hhMm.trim().split(":")
    if (parts.size < 2) return hhMm
    val h24 = parts[0].toIntOrNull() ?: return hhMm
    val m = parts[1].toIntOrNull() ?: return hhMm
    val amPm = if (h24 < 12) "AM" else "PM"
    val h12 = when {
      h24 == 0 -> 12
      h24 > 12 -> h24 - 12
      else -> h24
    }
    return "%d:%02d %s".format(h12, m, amPm)
  }

  private fun calendarForHhMm(baseCal: Calendar, hhMm: String): Calendar {
    val mins = hhMmToMinutes(hhMm)
    return (baseCal.clone() as Calendar).apply {
      set(Calendar.HOUR_OF_DAY, mins / 60)
      set(Calendar.MINUTE, mins % 60)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
  }

  private fun formatSecondsToHhMmSs(totalSeconds: Long): String {
    val hrs = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return "%02d:%02d:%02d".format(hrs, mins, secs)
  }

  private fun julianDate(year: Int, month: Int, day: Int): Double {
    var y = year
    var m = month
    if (m <= 2) {
      y -= 1
      m += 12
    }
    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
  }

  private fun sunPosition(jd: Double): Pair<Double, Double> {
    val d = jd - 2451545.0
    val g = normalizeDegrees(357.529 + 0.98560028 * d)
    val q = normalizeDegrees(280.459 + 0.98564736 * d)
    val l = normalizeDegrees(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))
    val e = 23.439 - 0.00000036 * d
    val ra = radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))) / 15.0
    val decl = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))
    var eqTime = q / 15.0 - normalizeHours(ra)
    if (eqTime > 12.0) eqTime -= 24.0
    if (eqTime < -12.0) eqTime += 24.0
    return decl to eqTime
  }

  private fun sunHourAngle(latitude: Double, decl: Double, altitudeDeg: Double): Double? {
    val num = sin(degToRad(altitudeDeg)) - sin(degToRad(latitude)) * sin(degToRad(decl))
    val den = cos(degToRad(latitude)) * cos(degToRad(decl))
    val ratio = num / den
    if (ratio < -1.0 || ratio > 1.0) return null
    return radToDeg(acos(ratio)) / 15.0
  }

  private fun adjustHighLatitudePortion(angle: Double, nightDuration: Double, rule: Int): Double {
    val portion = when (rule) {
      1 -> 0.5 // Middle of the night
      2 -> 1.0 / 7.0 // One-seventh of the night
      else -> angle / 60.0 // Angle-based (standard default)
    }
    return nightDuration * portion
  }

  private fun hoursToHhMm(hours: Double): String {
    val normalized = normalizeHours(hours + 0.5 / 60.0)
    val h = floor(normalized).toInt().coerceIn(0, 23)
    val m = floor((normalized - h) * 60.0).toInt().coerceIn(0, 59)
    return "%02d:%02d".format(h, m)
  }

  private fun normalizeDegrees(d: Double): Double {
    var r = d % 360.0
    if (r < 0) r += 360.0
    return r
  }

  private fun normalizeHours(h: Double): Double {
    var r = h % 24.0
    if (r < 0) r += 24.0
    return r
  }

  private fun normalizeDurationHours(d: Double): Double {
    var r = d
    while (r < 0) r += 24.0
    while (r >= 24.0) r -= 24.0
    return r
  }

  private fun degToRad(d: Double): Double = d * PI / 180.0
  private fun radToDeg(r: Double): Double = r * 180.0 / PI
}
