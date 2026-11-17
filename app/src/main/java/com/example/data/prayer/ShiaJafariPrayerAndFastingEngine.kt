package com.example.data.prayer

import android.content.Context
import com.example.data.remote.SecurityIntegrityGuard
import java.net.HttpURLConnection
import java.net.URL
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Independent calculation conventions for Ja'fari (Ithna-Ashari) Prayer and Fasting timings.
 *
 * Crucial Jurisprudential & Astronomical Note:
 * In Ja'fari jurisprudence (e.g., Ayatollah Sistani, Islamic Laws, Rulings 719 & 1554;
 * Shaykh al-Tusi, Tahdhib al-Ahkam Vol. 2; Al-Kafi Vol. 3 & Vol. 4), Maghrib and Iftari
 * do NOT occur at astronomical sunset (Ghurub al-Shams, -0.833°). Instead, Maghrib and Iftari
 * enter when the eastern redness (al-Humrah al-Mashriqiyyah) that appears after sunset passes
 * overhead / disappears from the eastern sky, corresponding to a solar depression angle of
 * 4.0° to 4.5° below the western horizon (typically 13–18 minutes after astronomical sunset).
 */
enum class ShiaJafariCalculationMethod(
  val id: String,
  val alAdhanMethodId: Int,
  val title: String,
  val shortLabel: String,
  val fajrAngle: Double,
  val maghribRednessAngle: Double,
  val ishaAngle: Double,
  val defaultSehriImsakPrecautionMinutes: Int,
  val sourceAuthority: String,
  val jurisprudentialSummary: String
) {
  LEVA_INSTITUTE_QUM(
    id = "leva_qum",
    alAdhanMethodId = 0,
    title = "Leva Research Institute, Qum (AlAdhan Method 0 / TheShia Standard)",
    shortLabel = "Ja'fari (Leva Qum 16°/4°)",
    fajrAngle = 16.0,
    maghribRednessAngle = 4.0,
    ishaAngle = 14.0,
    defaultSehriImsakPrecautionMinutes = 0,
    sourceAuthority = "Leva Institute, Qum • AlAdhan API Method 0 & TheShia.org",
    jurisprudentialSummary = "Fajr at 16.0° solar depression; Maghrib & Iftari at 4.0° solar depression (disappearance of eastern redness); Isha Fadilah at 14.0°."
  ),
  SISTANI_NAJAF_IHTIYAT(
    id = "sistani_najaf",
    alAdhanMethodId = 0,
    title = "Najaf Seminary / Ayatollah Sistani Convention (With Eastern Redness & Fajr Precaution)",
    shortLabel = "Najaf / Sistani (16°/4.2°)",
    fajrAngle = 16.0,
    maghribRednessAngle = 4.2,
    ishaAngle = 14.0,
    defaultSehriImsakPrecautionMinutes = 5,
    sourceAuthority = "sistani.org • Islamic Laws (Minhaj al-Salihin), Rulings 718–731 & 1554",
    jurisprudentialSummary = "True dawn (Al-Fajr al-Sadiq) at 16.0° with optional 5-min precautionary Imsak for Sehri ending; Maghrib/Iftari after certainty of eastern redness passing overhead (4.2°)."
  ),
  TEHRAN_GEOPHYSICS(
    id = "tehran_geophysics",
    alAdhanMethodId = 7,
    title = "Institute of Geophysics, University of Tehran (AlAdhan Method 7)",
    shortLabel = "Tehran Geophysics (17.7°/4.5°)",
    fajrAngle = 17.7,
    maghribRednessAngle = 4.5,
    ishaAngle = 14.0,
    defaultSehriImsakPrecautionMinutes = 0,
    sourceAuthority = "Institute of Geophysics, University of Tehran • AlAdhan API Method 7",
    jurisprudentialSummary = "Fajr at 17.7° solar depression; Maghrib & Iftari at 4.5° solar depression; Isha at 14.0°."
  ),
  THESHIA_ORG_JAFARI(
    id = "theshia_org",
    alAdhanMethodId = 0,
    title = "TheShia.org Global Ja'fari Astronomical Engine",
    shortLabel = "TheShia.org (16°/4°)",
    fajrAngle = 16.0,
    maghribRednessAngle = 4.0,
    ishaAngle = 14.0,
    defaultSehriImsakPrecautionMinutes = 2,
    sourceAuthority = "theshia.org/en/api/prayer-times & theshia.org/en/api/methods",
    jurisprudentialSummary = "Standard Ja'fari calculation with 2-minute precautionary Imsak before Fajr and 4.0° eastern-redness depression for Maghrib/Iftari."
  );

  companion object {
    fun fromId(id: String?): ShiaJafariCalculationMethod {
      return entries.find { it.id == id } ?: LEVA_INSTITUTE_QUM
    }
  }
}

/**
 * Independent user configuration for Shia Ja'fari Prayer & Sehri/Iftari calculations.
 * Kept logically separate from Sunni PrayerAndAlarmConfig while sharing the user's geographic location.
 */
data class ShiaPrayerAndFastingConfig(
  val method: ShiaJafariCalculationMethod = ShiaJafariCalculationMethod.LEVA_INSTITUTE_QUM,
  val sehriImsakPrecautionMinutes: Int = 0, // Additional minutes before Shia Fajr for Sehri ending
  val maghribManualAdjustmentMinutes: Int = 0, // Additional manual minutes for Shia Maghrib/Iftari
  val fajrManualAdjustmentMinutes: Int = 0,
  val shiaSehriNotificationEnabled: Boolean = true,
  val shiaIftariNotificationEnabled: Boolean = true,
  val shiaPrayerReminderEnabled: Boolean = true,
  val showCombinedSunniShiaTableOnHome: Boolean = true
)

/**
 * Complete daily Shia Ja'fari Prayer & Fasting schedule for a given date and location.
 */
data class ShiaJafariDailySchedule(
  val dateIso: String,
  val gregorianReadable: String,
  val hijriReadable: String,
  val locationName: String,
  val country: String,
  val latitude: Double,
  val longitude: Double,
  val timeZoneId: String,
  val method: ShiaJafariCalculationMethod,
  val sehriEndsTime: String, // Shia Fajr-based Sehri ending time (with Imsak precaution if configured)
  val fajrTime: String, // Shia True Dawn (Al-Fajr al-Sadiq)
  val sunriseTime: String, // Sunrise (ulu' al-Shams)
  val dhuhrTime: String, // Shar'i Zawal (Solar Noon + precaution)
  val asrTime: String, // Asr Fadilah time (Standard shadow factor = 1; valid to combine after Dhuhr)
  val astronomicalSunsetTime: String, // Disk sunset (Ghurub al-Shams, -0.833°) — NOT yet Maghrib in Ja'fari fiqh
  val maghribTime: String, // Ja'fari Maghrib (after disappearance of eastern redness, -4.0° to -4.5°)
  val iftarTime: String, // Ja'fari Iftari time (at Ja'fari Maghrib after eastern redness)
  val ishaTime: String, // Ja'fari Isha Fadilah time (-14.0°; valid to combine after Maghrib)
  val shiaMidnightTime: String, // Muntasaf al-Layl (Midpoint between Sunset and Fajr Dawn)
  val sourceDescription: String,
  val isLiveApiSynced: Boolean = false
)

/**
 * Represents a single day row in the 30-day Shia Ramadan / Monthly Fasting Timetable.
 */
data class ShiaFastingCalendarDay(
  val dayNumber: Int,
  val gregorianDateIso: String,
  val gregorianReadable: String,
  val hijriReadable: String,
  val sehriEnds24: String,
  val fajrDawn24: String,
  val sunrise24: String,
  val dhuhr24: String,
  val astronomicalSunset24: String,
  val maghribIftari24: String,
  val shiaMidnight24: String,
  val methodLabel: String
)

/**
 * Live status for Shia daily fasting (countdown to Shia Sehri Ending or Shia Maghrib/Iftari).
 */
data class ShiaFastingCountdownStatus(
  val currentPhaseTitle: String,
  val targetEventLabel: String,
  val targetTime24: String,
  val targetTime12h: String,
  val countdownHhMmSs: String,
  val remainingSeconds: Long,
  val astronomicalSunsetNote: String
)

object ShiaJafariPrayerAndFastingEngine {

  private const val PREFS_NAME = "bayan_shia_jafari_engine_prefs_v1"
  private const val CACHE_PREFS_NAME = "bayan_shia_jafari_api_cache_v1"

  fun loadShiaConfig(context: Context): ShiaPrayerAndFastingConfig {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val method = ShiaJafariCalculationMethod.fromId(
      prefs.getString("shia_method_id", ShiaJafariCalculationMethod.LEVA_INSTITUTE_QUM.id)
    )
    return ShiaPrayerAndFastingConfig(
      method = method,
      sehriImsakPrecautionMinutes = prefs.getInt(
        "shia_sehri_imsak_mins",
        method.defaultSehriImsakPrecautionMinutes
      ).coerceIn(-30, 30),
      maghribManualAdjustmentMinutes = prefs.getInt("shia_maghrib_adj_mins", 0).coerceIn(-30, 30),
      fajrManualAdjustmentMinutes = prefs.getInt("shia_fajr_adj_mins", 0).coerceIn(-30, 30),
      shiaSehriNotificationEnabled = prefs.getBoolean("shia_sehri_notif_enabled", true),
      shiaIftariNotificationEnabled = prefs.getBoolean("shia_iftari_notif_enabled", true),
      shiaPrayerReminderEnabled = prefs.getBoolean("shia_prayer_notif_enabled", true),
      showCombinedSunniShiaTableOnHome = prefs.getBoolean("show_combined_table_home", true)
    )
  }

  fun saveShiaConfig(context: Context, config: ShiaPrayerAndFastingConfig) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      .edit()
      .putString("shia_method_id", config.method.id)
      .putInt("shia_sehri_imsak_mins", config.sehriImsakPrecautionMinutes.coerceIn(-30, 30))
      .putInt("shia_maghrib_adj_mins", config.maghribManualAdjustmentMinutes.coerceIn(-30, 30))
      .putInt("shia_fajr_adj_mins", config.fajrManualAdjustmentMinutes.coerceIn(-30, 30))
      .putBoolean("shia_sehri_notif_enabled", config.shiaSehriNotificationEnabled)
      .putBoolean("shia_iftari_notif_enabled", config.shiaIftariNotificationEnabled)
      .putBoolean("shia_prayer_notif_enabled", config.shiaPrayerReminderEnabled)
      .putBoolean("show_combined_table_home", config.showCombinedSunniShiaTableOnHome)
      .apply()
  }

  /**
   * Independent astronomical calculation for Shia Ja'fari prayer and fasting times.
   * Never copies Sunni timings; computes Fajr, Sunrise, Dhuhr, Asr, Astronomical Sunset,
   * Eastern-Redness Maghrib/Iftari, Isha, and Shia Midnight (Sunset-to-Fajr midpoint) directly
   * for the user's exact coordinates, date, and timezone.
   */
  fun calculateShiaSchedule(
    calendar: Calendar = Calendar.getInstance(),
    locationName: String,
    country: String = "",
    latitude: Double,
    longitude: Double,
    timeZoneId: String = TimeZone.getDefault().id,
    shiaConfig: ShiaPrayerAndFastingConfig = ShiaPrayerAndFastingConfig()
  ): ShiaJafariDailySchedule {
    val tz = TimeZone.getTimeZone(timeZoneId)
    val localCal = (calendar.clone() as Calendar).apply { timeZone = tz }

    val year = localCal.get(Calendar.YEAR)
    val month = localCal.get(Calendar.MONTH) + 1
    val day = localCal.get(Calendar.DAY_OF_MONTH)

    val method = shiaConfig.method
    val tzOffsetHours = tz.getOffset(localCal.timeInMillis) / (1000.0 * 3600.0)

    val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)
    val (decl, eqTime) = sunPosition(jd)
    val solarNoon = 12.0 + tzOffsetHours - longitude / 15.0 - eqTime

    // 1. Dhuhr (Shar'i Zawal: Solar noon + 2 minutes precaution for sun passing meridian)
    val dhuhrHours = normalizeHours(solarNoon + (2.0 / 60.0))

    // 2. Sunrise & Astronomical Sunset (-0.833° refraction + solar semi-diameter)
    val horizonDelta = sunHourAngle(latitude, decl, -0.833) ?: 6.0
    val sunriseHours = normalizeHours(solarNoon - horizonDelta)
    val astroSunsetHours = normalizeHours(solarNoon + horizonDelta)

    val nightDuration = normalizeDurationHours(sunriseHours - astroSunsetHours)

    // 3. Shia Fajr / True Dawn (Al-Fajr al-Sadiq using selected Ja'fari angle: 16.0° or 17.7°)
    val fajrDelta = sunHourAngle(latitude, decl, -method.fajrAngle)
      ?: (nightDuration * (method.fajrAngle / 60.0))
    val rawFajrHours = normalizeHours(solarNoon - fajrDelta)

    // 4. Shia Asr (Fadilah time: shadow factor = 1 in Ja'fari jurisprudence; note: permissible to combine after Dhuhr)
    val asrAltitudeRad = atan(1.0 / (1.0 + tan(degToRad(abs(latitude - decl)))))
    val asrAngleDeg = radToDeg(asrAltitudeRad)
    val asrDelta = sunHourAngle(latitude, decl, asrAngleDeg) ?: 3.1
    val asrHours = normalizeHours(solarNoon + asrDelta)

    // 5. Shia Maghrib & Iftari (Disappearance of Eastern Redness / Zawal al-Humrah al-Mashriqiyyah: -4.0° to -4.5°)
    val maghribDelta = sunHourAngle(latitude, decl, -method.maghribRednessAngle)
      ?: (horizonDelta + 0.27) // ~16 min after sunset fallback at extreme high latitudes
    val rawMaghribHours = normalizeHours(solarNoon + maghribDelta)

    // 6. Shia Isha (Fadilah time at -14.0°; note: permissible to combine after Maghrib)
    val ishaDelta = sunHourAngle(latitude, decl, -method.ishaAngle)
      ?: (nightDuration * (method.ishaAngle / 60.0))
    val ishaHours = normalizeHours(solarNoon + ishaDelta)

    val rawFajrStr = hoursToHhMm(rawFajrHours)
    val fajrStr = applyMinutesOffset(rawFajrStr, shiaConfig.fajrManualAdjustmentMinutes)

    // Shia Sehri Ends: Fajr minus precautionary Imsak minutes (if configured)
    val sehriEndsStr = applyMinutesOffset(fajrStr, -shiaConfig.sehriImsakPrecautionMinutes)

    val sunriseStr = hoursToHhMm(sunriseHours)
    val dhuhrStr = hoursToHhMm(dhuhrHours)
    val asrStr = hoursToHhMm(asrHours)
    val astroSunsetStr = hoursToHhMm(astroSunsetHours)

    val rawMaghribStr = hoursToHhMm(rawMaghribHours)
    val maghribStr = applyMinutesOffset(rawMaghribStr, shiaConfig.maghribManualAdjustmentMinutes)
    val iftarStr = maghribStr
    val ishaStr = hoursToHhMm(ishaHours)

    // 7. ShiaShar'i Midnight (Muntasaf al-Layl: midpoint between Sunset and Fajr Dawn)
    val shiaMidnightStr = calculateMidpointBetweenTimes(astroSunsetStr, fajrStr)

    val dateIso = "%04d-%02d-%02d".format(year, month, day)
    val gregorianFmt = SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH).apply { timeZone = tz }
    val gregorianReadable = gregorianFmt.format(localCal.time)
    val hijriReadable = PrayerTimeCalculator.formatHijriDate(localCal.time, tz)

    return ShiaJafariDailySchedule(
      dateIso = dateIso,
      gregorianReadable = gregorianReadable,
      hijriReadable = hijriReadable,
      locationName = locationName,
      country = country,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      method = method,
      sehriEndsTime = sehriEndsStr,
      fajrTime = fajrStr,
      sunriseTime = sunriseStr,
      dhuhrTime = dhuhrStr,
      asrTime = asrStr,
      astronomicalSunsetTime = astroSunsetStr,
      maghribTime = maghribStr,
      iftarTime = iftarStr,
      ishaTime = ishaStr,
      shiaMidnightTime = shiaMidnightStr,
      sourceDescription = "${method.shortLabel} • ${method.sourceAuthority}",
      isLiveApiSynced = false
    )
  }

  /**
   * Optional network sync with AlAdhan API (`method=0` Leva Institute Qum or `method=7` Tehran)
   * and TheShia.org JSON format, with local SharedPreferences caching and immediate fallback to
   * the astronomical Ja'fari engine when offline.
   */
  suspend fun fetchOrCalculateShiaSchedule(
    context: Context,
    calendar: Calendar = Calendar.getInstance(),
    locationName: String,
    country: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    shiaConfig: ShiaPrayerAndFastingConfig
  ): ShiaJafariDailySchedule = withContext(Dispatchers.IO) {
    val offlineBase = calculateShiaSchedule(
      calendar = calendar,
      locationName = locationName,
      country = country,
      latitude = latitude,
      longitude = longitude,
      timeZoneId = timeZoneId,
      shiaConfig = shiaConfig
    )
    val cacheKey = "${offlineBase.dateIso}_${"%.3f".format(Locale.US, latitude)}_${"%.3f".format(Locale.US, longitude)}_${shiaConfig.method.id}"
    val cachePrefs = context.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE)

    val cachedJson = cachePrefs.getString(cacheKey, null)
    if (!cachedJson.isNullOrBlank()) {
      parseCachedShiaSchedule(cachedJson, offlineBase, shiaConfig)?.let { return@withContext it }
    }

    // Query AlAdhan v1 timings endpoint with method=0 (Shia Ithna-Ashari, Leva Institute, Qum) or method=7 (Tehran)
    val dateDdMmYyyy = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply {
      timeZone = TimeZone.getTimeZone(timeZoneId)
    }.format(calendar.time)

    val apiUrl = "https://api.aladhan.com/v1/timings/$dateDdMmYyyy" +
      "?latitude=${"%.4f".format(Locale.US, latitude)}" +
      "&longitude=${"%.4f".format(Locale.US, longitude)}" +
      "&method=${shiaConfig.method.alAdhanMethodId}" +
      "&school=0"

    if (!SecurityIntegrityGuard.isUrlTrustedAndAllowed(apiUrl)) {
      return@withContext offlineBase
    }

    return@withContext runCatching {
      val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 6000
        readTimeout = 6000
        setRequestProperty("Accept", "application/json")
      }
      if (conn.responseCode == HttpURLConnection.HTTP_OK) {
        val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(body)
        val timings = root.optJSONObject("data")?.optJSONObject("timings")
        if (timings != null) {
          cachePrefs.edit().putString(cacheKey, timings.toString()).apply()
          parseCachedShiaSchedule(timings.toString(), offlineBase, shiaConfig) ?: offlineBase
        } else {
          offlineBase
        }
      } else {
        offlineBase
      }
    }.getOrDefault(offlineBase)
  }

  private fun parseCachedShiaSchedule(
    timingsJsonStr: String,
    base: ShiaJafariDailySchedule,
    shiaConfig: ShiaPrayerAndFastingConfig
  ): ShiaJafariDailySchedule? {
    return runCatching {
      val timings = JSONObject(timingsJsonStr)
      fun cleanTime(key: String, fallback: String): String {
        val raw = timings.optString(key, fallback).substringBefore(" ").trim()
        return if (raw.matches(Regex("\\d{2}:\\d{2}"))) raw else fallback
      }
      val apiFajr = applyMinutesOffset(cleanTime("Fajr", base.fajrTime), shiaConfig.fajrManualAdjustmentMinutes)
      val apiSunrise = cleanTime("Sunrise", base.sunriseTime)
      val apiDhuhr = cleanTime("Dhuhr", base.dhuhrTime)
      val apiAsr = cleanTime("Asr", base.asrTime)
      val apiSunset = cleanTime("Sunset", base.astronomicalSunsetTime)
      val apiMaghrib = applyMinutesOffset(cleanTime("Maghrib", base.maghribTime), shiaConfig.maghribManualAdjustmentMinutes)
      val apiIsha = cleanTime("Isha", base.ishaTime)
      val apiMidnight = cleanTime("Midnight", base.shiaMidnightTime)

      // Ensure Shia Maghrib is never earlier than astronomical sunset + eastern redness margin
      val finalMaghrib = if (PrayerTimeCalculator.hhMmToMinutes(apiMaghrib) <= PrayerTimeCalculator.hhMmToMinutes(apiSunset) + 8) {
        base.maghribTime
      } else {
        apiMaghrib
      }
      val finalSehriEnds = applyMinutesOffset(apiFajr, -shiaConfig.sehriImsakPrecautionMinutes)

      base.copy(
        sehriEndsTime = finalSehriEnds,
        fajrTime = apiFajr,
        sunriseTime = apiSunrise,
        dhuhrTime = apiDhuhr,
        asrTime = apiAsr,
        astronomicalSunsetTime = apiSunset,
        maghribTime = finalMaghrib,
        iftarTime = finalMaghrib,
        ishaTime = apiIsha,
        shiaMidnightTime = apiMidnight,
        sourceDescription = "Verified AlAdhan Method ${shiaConfig.method.alAdhanMethodId} & ${shiaConfig.method.shortLabel}",
        isLiveApiSynced = true
      )
    }.getOrNull()
  }

  /**
   * Computes which prayer is next in the Shia Ja'fari schedule without confusing it with Sunni timings.
   */
  fun computeNextShiaPrayerStatus(
    schedule: ShiaJafariDailySchedule,
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

    var currentActive = "Isha"
    for ((name, timeStr) in prayers) {
      val pCal = calendarForHhMm(nowCal, timeStr)
      if (pCal.timeInMillis > nowMillis) {
        val diffSec = ((pCal.timeInMillis - nowMillis) / 1000L).coerceAtLeast(0L)
        return NextPrayerStatus(
          nextPrayerName = name,
          nextPrayerTime24 = timeStr,
          countdownFormatted = formatSecondsToHhMmSs(diffSec),
          remainingSeconds = diffSec,
          currentActivePrayer = currentActive
        )
      }
      currentActive = name
    }

    val tomorrowFajr = calendarForHhMm(nowCal, schedule.fajrTime).apply {
      add(Calendar.DAY_OF_YEAR, 1)
    }
    val diffSec = ((tomorrowFajr.timeInMillis - nowMillis) / 1000L).coerceAtLeast(0L)
    return NextPrayerStatus(
      nextPrayerName = "Fajr",
      nextPrayerTime24 = schedule.fajrTime,
      countdownFormatted = formatSecondsToHhMmSs(diffSec),
      remainingSeconds = diffSec,
      currentActivePrayer = "Isha"
    )
  }

  /**
   * Computes live Shia Fasting Countdown (to Shia Sehri Ending before dawn, or to Shia Maghrib/Iftari during the day).
   */
  fun computeShiaFastingCountdown(
    schedule: ShiaJafariDailySchedule,
    nowMillis: Long = System.currentTimeMillis()
  ): ShiaFastingCountdownStatus {
    val tz = TimeZone.getTimeZone(schedule.timeZoneId)
    val nowCal = Calendar.getInstance(tz).apply { timeInMillis = nowMillis }

    val sehriCal = calendarForHhMm(nowCal, schedule.sehriEndsTime)
    val iftarCal = calendarForHhMm(nowCal, schedule.iftarTime)

    val sunsetNote = "Note: Astronomical Sunset is ${PrayerTimeCalculator.format24hTo12h(schedule.astronomicalSunsetTime)}; " +
      "Ja'fari Maghrib & Iftari is at ${PrayerTimeCalculator.format24hTo12h(schedule.iftarTime)} (after eastern redness disappears)."

    return when {
      nowMillis < sehriCal.timeInMillis -> {
        val diffSec = ((sehriCal.timeInMillis - nowMillis) / 1000L).coerceAtLeast(0L)
        ShiaFastingCountdownStatus(
          currentPhaseTitle = "Pre-Dawn Sehri Window (Ja'fari)",
          targetEventLabel = "Shia Sehri Ends In",
          targetTime24 = schedule.sehriEndsTime,
          targetTime12h = PrayerTimeCalculator.format24hTo12h(schedule.sehriEndsTime),
          countdownHhMmSs = formatSecondsToHhMmSs(diffSec),
          remainingSeconds = diffSec,
          astronomicalSunsetNote = sunsetNote
        )
      }
      nowMillis < iftarCal.timeInMillis -> {
        val diffSec = ((iftarCal.timeInMillis - nowMillis) / 1000L).coerceAtLeast(0L)
        ShiaFastingCountdownStatus(
          currentPhaseTitle = "Fasting in Progress (Ja'fari Sawm)",
          targetEventLabel = "Shia Maghrib & Iftari In",
          targetTime24 = schedule.iftarTime,
          targetTime12h = PrayerTimeCalculator.format24hTo12h(schedule.iftarTime),
          countdownHhMmSs = formatSecondsToHhMmSs(diffSec),
          remainingSeconds = diffSec,
          astronomicalSunsetNote = sunsetNote
        )
      }
      else -> {
        val nextDaySehri = (sehriCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val diffSec = ((nextDaySehri.timeInMillis - nowMillis) / 1000L).coerceAtLeast(0L)
        ShiaFastingCountdownStatus(
          currentPhaseTitle = "Night / Post-Iftar Period",
          targetEventLabel = "Next Day Shia Sehri Ends In",
          targetTime24 = schedule.sehriEndsTime,
          targetTime12h = PrayerTimeCalculator.format24hTo12h(schedule.sehriEndsTime),
          countdownHhMmSs = formatSecondsToHhMmSs(diffSec),
          remainingSeconds = diffSec,
          astronomicalSunsetNote = sunsetNote
        )
      }
    }
  }

  /**
   * Generates a 30-day Shia Ja'fari Ramadan / Monthly Fasting Calendar for the user's location.
   */
  fun generateShia30DayFastingCalendar(
    startCalendar: Calendar = Calendar.getInstance(),
    daysCount: Int = 30,
    locationName: String,
    country: String,
    latitude: Double,
    longitude: Double,
    timeZoneId: String,
    shiaConfig: ShiaPrayerAndFastingConfig
  ): List<ShiaFastingCalendarDay> {
    val tz = TimeZone.getTimeZone(timeZoneId)
    val workingCal = (startCalendar.clone() as Calendar).apply {
      timeZone = tz
      set(Calendar.HOUR_OF_DAY, 12)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
    }

    return (1..daysCount.coerceIn(1, 31)).map { daySeq ->
      val daily = calculateShiaSchedule(
        calendar = workingCal,
        locationName = locationName,
        country = country,
        latitude = latitude,
        longitude = longitude,
        timeZoneId = timeZoneId,
        shiaConfig = shiaConfig
      )
      val entry = ShiaFastingCalendarDay(
        dayNumber = daySeq,
        gregorianDateIso = daily.dateIso,
        gregorianReadable = daily.gregorianReadable,
        hijriReadable = daily.hijriReadable,
        sehriEnds24 = daily.sehriEndsTime,
        fajrDawn24 = daily.fajrTime,
        sunrise24 = daily.sunriseTime,
        dhuhr24 = daily.dhuhrTime,
        astronomicalSunset24 = daily.astronomicalSunsetTime,
        maghribIftari24 = daily.iftarTime,
        shiaMidnight24 = daily.shiaMidnightTime,
        methodLabel = shiaConfig.method.shortLabel
      )
      workingCal.add(Calendar.DAY_OF_YEAR, 1)
      entry
    }
  }

  fun applyMinutesOffset(hhMm: String, offsetMinutes: Int): String {
    if (offsetMinutes == 0) return hhMm
    val baseMins = PrayerTimeCalculator.hhMmToMinutes(hhMm)
    val adjusted = ((baseMins + offsetMinutes) % (24 * 60) + (24 * 60)) % (24 * 60)
    return "%02d:%02d".format(adjusted / 60, adjusted % 60)
  }

  private fun calculateMidpointBetweenTimes(startHhMm: String, endHhMm: String): String {
    val startMins = PrayerTimeCalculator.hhMmToMinutes(startHhMm)
    val endMins = PrayerTimeCalculator.hhMmToMinutes(endHhMm)
    val duration = if (endMins >= startMins) {
      endMins - startMins
    } else {
      (24 * 60 - startMins) + endMins
    }
    val midMins = (startMins + duration / 2) % (24 * 60)
    return "%02d:%02d".format(midMins / 60, midMins % 60)
  }

  private fun calendarForHhMm(baseCal: Calendar, hhMm: String): Calendar {
    val mins = PrayerTimeCalculator.hhMmToMinutes(hhMm)
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
