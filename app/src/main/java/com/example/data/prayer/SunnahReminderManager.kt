package com.example.data.prayer

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar as IcuCalendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.DailyDuasDataProvider
import com.example.data.model.NecessaryReminderSpec
import com.example.data.model.ReminderFrequency
import com.example.data.model.WeekdaySunnahSpec
import com.example.ui.prayer.PrayerAlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

data class ReminderItemState(
  val id: String,
  val enabled: Boolean,
  val time24h: String
)

data class CalculatedHijriDateInfo(
  val dayOfMonth: Int,
  val monthIndex: Int,
  val monthNameEnglish: String,
  val monthNameArabic: String,
  val yearAh: Int,
  val formattedReadable: String,
  val isAyyamAlBeed: Boolean,
  val specialOccasionBadge: String?
)

data class CalculatedUpcomingDayItem(
  val dayOffsetFromToday: Int,
  val calendarDayOfWeek: Int,
  val isToday: Boolean,
  val isFridayJumuah: Boolean,
  val isSunnahFastingDay: Boolean,
  val isAyyamAlBeed: Boolean,
  val gregorianShortDate: String,
  val gregorianFullDate: String,
  val hijriReadable: String,
  val specialBadge: String,
  val weekdaySpec: WeekdaySunnahSpec
)

data class CalculatedDayAndWeekStatus(
  val todayDayOfWeek: Int,
  val todayWeekdaySpec: WeekdaySunnahSpec,
  val gregorianDateIso: String,
  val gregorianReadable: String,
  val hijriInfo: CalculatedHijriDateInfo,
  val isFridayToday: Boolean,
  val isThursdayJumuahEve: Boolean,
  val isSunnahFastingDayToday: Boolean,
  val isAyyamAlBeedToday: Boolean,
  val daysUntilNextFriday: Int,
  val nextFridayDateReadable: String,
  val nextFridayBathAlertSummary: String,
  val nextSunnahFastSummary: String,
  val upcoming7DaysSchedule: List<CalculatedUpcomingDayItem>,
  val notificationTitle: String,
  val notificationBody: String,
  val notificationReference: String
)

data class SunnahRemindersUiState(
  val fridayBathReminderEnabled: Boolean = true,
  val fridayBathReminderTime24h: String = "09:30",
  val dailyCalculatedNotifEnabled: Boolean = true,
  val dailyCalculatedNotifTime24h: String = "08:00",
  val completedFridayChecklistIds: Set<String> = emptySet(),
  val fridaySalawatCount: Int = 0,
  val favoriteDuaIds: Set<String> = emptySet(),
  val duaTasbihCounts: Map<String, Int> = emptyMap(),
  val reminderStates: Map<String, ReminderItemState> = emptyMap(),
  val calculatedDayStatus: CalculatedDayAndWeekStatus = SunnahReminderManager.calculateDayAndWeekStatus(),
  val selectedPreviewDayOfWeek: Int = calculatedDayStatus.todayDayOfWeek,
  val lastNotificationBannerMessage: String = ""
)

object SunnahReminderManager {

  private const val PREFS_NAME = "bayan_daily_duas_and_sunnah_reminders_prefs"
  const val ACTION_SUNNAH_REMINDER_ALARM = "com.example.bayan.ACTION_SUNNAH_REMINDER_ALARM"
  const val EXTRA_REMINDER_ID = "extra_sunnah_reminder_id"
  const val EXTRA_REMINDER_TITLE = "extra_sunnah_reminder_title"
  const val EXTRA_REMINDER_BODY = "extra_sunnah_reminder_body"
  const val EXTRA_REMINDER_REF = "extra_sunnah_reminder_ref"

  const val SUNNAH_REMINDER_CHANNEL_ID = "bayan_sunnah_reminders_silent_channel_v1"
  private const val BASE_REMINDER_REQ_CODE = 5100
  private const val BASE_REMINDER_NOTIF_ID = 5500

  private val HIJRI_MONTHS_ENGLISH = arrayOf(
    "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
    "Jumada al-Ula", "Jumada al-Akhirah", "Rajab", "Sha'ban",
    "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah"
  )

  private val HIJRI_MONTHS_ARABIC = arrayOf(
    "مُحَرَّم", "صَفَر", "رَبِيع الأَوَّل", "رَبِيع الثَّانِي",
    "جُمَادَى الأُولَى", "جُمَادَى الآخِرَة", "رَجَب", "شَعْبَان",
    "رَمَضَان", "شَوَّال", "ذُو القَعْدَة", "ذُو الحِجَّة"
  )

  /**
   * Calculates exact Hijri calendar fields using Android ICU Umm al-Qura calendar with a deterministic
   * astronomical/tabular fallback so it always succeeds on device and in JVM/Robolectric tests.
   */
  fun calculateHijriDateInfo(
    timeMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault()
  ): CalculatedHijriDateInfo {
    var day = 1
    var monthIdx = 0
    var year = 1447

    val icuSucceeded = try {
      val locale = ULocale("@calendar=islamic-umalqura")
      val icuCal = IcuCalendar.getInstance(
        android.icu.util.TimeZone.getTimeZone(timeZone.id),
        locale
      ) as IslamicCalendar
      icuCal.time = Date(timeMillis)
      val d = icuCal.get(IcuCalendar.DAY_OF_MONTH)
      val m = icuCal.get(IcuCalendar.MONTH)
      val y = icuCal.get(IcuCalendar.YEAR)
      if (d in 1..30 && m in 0..11 && y >= 1400) {
        day = d
        monthIdx = m
        year = y
        true
      } else {
        false
      }
    } catch (_: Throwable) {
      false
    }

    if (!icuSucceeded) {
      val cal = Calendar.getInstance(timeZone).apply { timeInMillis = timeMillis }
      val gy = cal.get(Calendar.YEAR)
      val gm = cal.get(Calendar.MONTH) + 1
      val gd = cal.get(Calendar.DAY_OF_MONTH)
      val fallback = gregorianToHijriTabular(gy, gm, gd)
      day = fallback.first
      monthIdx = fallback.second
      year = fallback.third
    }

    val mEng = HIJRI_MONTHS_ENGLISH.getOrElse(monthIdx) { "Hijri" }
    val mAr = HIJRI_MONTHS_ARABIC.getOrElse(monthIdx) { "هجري" }
    val isAyyamAlBeed = day in 13..15

    val specialOccasionBadge = when {
      monthIdx == 8 && day >= 21 && day % 2 == 1 -> "Laylatul Qadr Odd Night ($day $mEng)"
      monthIdx == 8 -> "Blessed Month of Ramadan ($day $mEng)"
      monthIdx == 9 && day == 1 -> "Eid al-Fitr Mubarak (1 Shawwal)"
      monthIdx == 9 && day in 2..7 -> "Six Sunnah Fasts of Shawwal ($day Shawwal)"
      monthIdx == 11 && day == 9 -> "Day of 'Arafah Sunnah Fast (9 Dhu al-Hijjah)"
      monthIdx == 11 && day == 10 -> "Eid al-Adha Mubarak (10 Dhu al-Hijjah)"
      monthIdx == 11 && day in 1..8 -> "Blessed First 10 Days of Dhu al-Hijjah ($day $mEng)"
      monthIdx == 0 && (day == 9 || day == 10) -> "Tasu'a & 'Ashura Sunnah Fast ($day Muharram)"
      isAyyamAlBeed -> "Ayyam al-Beed White Day ($day $mEng)"
      else -> null
    }

    return CalculatedHijriDateInfo(
      dayOfMonth = day,
      monthIndex = monthIdx,
      monthNameEnglish = mEng,
      monthNameArabic = mAr,
      yearAh = year,
      formattedReadable = "$day $mEng $year AH",
      isAyyamAlBeed = isAyyamAlBeed,
      specialOccasionBadge = specialOccasionBadge
    )
  }

  private fun gregorianToHijriTabular(year: Int, month: Int, day: Int): Triple<Int, Int, Int> {
    var y = year
    var m = month
    if (m < 3) {
      y -= 1
      m += 12
    }
    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.0
    val l = (jd - 1948440.0 + 10632.0).toInt()
    val n = (l - 1) / 10631
    val l2 = l - 10631 * n + 354
    val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
    val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
    val hMonth = (24 * l3) / 709
    val hDay = l3 - (709 * hMonth) / 24
    val hYear = 30 * n + j - 30
    return Triple(
      hDay.coerceIn(1, 30),
      (hMonth - 1).coerceIn(0, 11),
      hYear.coerceAtLeast(1400)
    )
  }

  /**
   * Dynamically calculates Today's weekday, Friday status, countdown to next Friday, next Sunnah fasting day,
   * Hijri Ayyam al-Beed status, and the 7-day rolling schedule for the Home Screen and Notification engine.
   */
  fun calculateDayAndWeekStatus(
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault(),
    fridayBathTime24h: String = "09:30"
  ): CalculatedDayAndWeekStatus {
    val cal = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
    val todayDow = cal.get(Calendar.DAY_OF_WEEK)
    val todaySpec = DailyDuasDataProvider.getWeekdaySpec(todayDow)
    val hijriInfo = calculateHijriDateInfo(nowMillis, timeZone)

    val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply { this.timeZone = timeZone }
    val fullFmt = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH).apply { this.timeZone = timeZone }
    val shortFmt = SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH).apply { this.timeZone = timeZone }

    val gregorianIso = isoFmt.format(cal.time)
    val gregorianReadable = fullFmt.format(cal.time)

    val isFridayToday = todayDow == Calendar.FRIDAY
    val isThursdayJumuahEve = todayDow == Calendar.THURSDAY
    val isSunnahFastToday = todaySpec.isSunnahFastingDay || hijriInfo.isAyyamAlBeed

    val daysUntilFriday = if (isFridayToday) {
      0
    } else {
      ((Calendar.FRIDAY - todayDow + 7) % 7).let { if (it == 0) 7 else it }
    }

    val nextFridayCal = (cal.clone() as Calendar).apply {
      add(Calendar.DAY_OF_YEAR, daysUntilFriday)
    }
    val nextFridayDateStr = shortFmt.format(nextFridayCal.time)
    val formattedBathTime = PrayerTimeCalculator.format24hTo12h(fridayBathTime24h)

    val nextFridayDateReadable = when (daysUntilFriday) {
      0 -> "Today is Blessed Friday (Yawm al-Jumu'ah)!"
      1 -> "Tomorrow is Blessed Friday ($nextFridayDateStr)"
      else -> "Next Friday (Jumu'ah): $nextFridayDateStr (in $daysUntilFriday days)"
    }

    val nextFridayBathAlertSummary = if (isFridayToday) {
      "Today (Jumu'ah) • Friday Bath (Ghusl) Alert at $formattedBathTime"
    } else {
      "Next Friday Ghusl Alert: $nextFridayDateStr at $formattedBathTime"
    }

    // Calculate next Monday / Thursday / Ayyam al-Beed Sunnah Fast
    var nextFastOffset = 0
    var nextFastLabel = ""
    for (offset in 0..7) {
      val checkCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
      val dow = checkCal.get(Calendar.DAY_OF_WEEK)
      val hInfo = calculateHijriDateInfo(checkCal.timeInMillis, timeZone)
      if (dow == Calendar.MONDAY || dow == Calendar.THURSDAY || hInfo.isAyyamAlBeed) {
        nextFastOffset = offset
        val dayName = SimpleDateFormat("EEEE", Locale.ENGLISH).apply { this.timeZone = timeZone }.format(checkCal.time)
        val fastReason = when {
           (dow == Calendar.MONDAY || dow == Calendar.THURSDAY) && hInfo.isAyyamAlBeed ->
            "$dayName Sunnah Fast + Ayyam al-Beed (${hInfo.dayOfMonth} ${hInfo.monthNameEnglish})"
          hInfo.isAyyamAlBeed ->
            "Ayyam al-Beed White Day (${hInfo.dayOfMonth} ${hInfo.monthNameEnglish})"
          else -> "$dayName Sunnah Fast"
        }
        nextFastLabel = when (offset) {
          0 -> "Today: $fastReason"
          1 -> "Tomorrow: $fastReason (${shortFmt.format(checkCal.time)})"
          else -> "Next Sunnah Fast: $fastReason on ${shortFmt.format(checkCal.time)} (in $offset days)"
        }
        break
      }
    }
    if (nextFastLabel.isEmpty()) {
      nextFastLabel = "Monday & Thursday Sunnah Fasting Active"
    }

    // Build 7-day calculated schedule starting from Today (offsets 0..6)
    val upcoming7Days = (0..6).map { offset ->
      val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
      val dow = dayCal.get(Calendar.DAY_OF_WEEK)
      val spec = DailyDuasDataProvider.getWeekdaySpec(dow)
      val dayHijri = calculateHijriDateInfo(dayCal.timeInMillis, timeZone)
      val isFri = dow == Calendar.FRIDAY
      val isFast = spec.isSunnahFastingDay || dayHijri.isAyyamAlBeed

      val badge = when {
        offset == 0 && isFri -> "TODAY • BLESSED JUMU'AH"
        offset == 0 && dayHijri.isAyyamAlBeed -> "TODAY • AYYAM AL-BEED"
        offset == 0 && isFast -> "TODAY • SUNNAH FAST"
        offset == 0 -> "TODAY (${spec.dayNameEnglish.uppercase(Locale.ENGLISH)})"
        isFri -> "JUMU'AH (IN ${offset}D)"
        dayHijri.isAyyamAlBeed -> "AYYAM AL-BEED (${dayHijri.dayOfMonth} ${dayHijri.monthNameEnglish})"
        isFast -> "SUNNAH FAST"
        else -> spec.shortDayLabel.uppercase(Locale.ENGLISH)
      }

      CalculatedUpcomingDayItem(
        dayOffsetFromToday = offset,
        calendarDayOfWeek = dow,
        isToday = offset == 0,
        isFridayJumuah = isFri,
        isSunnahFastingDay = isFast,
        isAyyamAlBeed = dayHijri.isAyyamAlBeed,
        gregorianShortDate = shortFmt.format(dayCal.time),
        gregorianFullDate = fullFmt.format(dayCal.time),
        hijriReadable = dayHijri.formattedReadable,
        specialBadge = badge,
        weekdaySpec = spec
      )
    }

    // Build tailored Notification Title & Body for the calculated day
    val hijriSuffix = hijriInfo.specialOccasionBadge?.let { " • $it" } ?: " • ${hijriInfo.formattedReadable}"
    val notifTitle = if (isFridayToday) {
      "Jumu'ah Mubarak! Friday Bath (Ghusl), Surah Al-Kahf & Salawat Reminder"
    } else if (hijriInfo.isAyyamAlBeed) {
      "${todaySpec.dayNameEnglish} & Ayyam al-Beed (${hijriInfo.dayOfMonth} ${hijriInfo.monthNameEnglish}) Reminder"
    } else {
      "${todaySpec.dayNameEnglish} (${todaySpec.arabicDayName}) — ${todaySpec.islamicDayTitle}"
    }

    val notifBody = "${todaySpec.primaryReminderTitle}$hijriSuffix. ${todaySpec.englishGuidance}"

    return CalculatedDayAndWeekStatus(
      todayDayOfWeek = todayDow,
      todayWeekdaySpec = todaySpec,
      gregorianDateIso = gregorianIso,
      gregorianReadable = gregorianReadable,
      hijriInfo = hijriInfo,
      isFridayToday = isFridayToday,
      isThursdayJumuahEve = isThursdayJumuahEve,
      isSunnahFastingDayToday = isSunnahFastToday,
      isAyyamAlBeedToday = hijriInfo.isAyyamAlBeed,
      daysUntilNextFriday = daysUntilFriday,
      nextFridayDateReadable = nextFridayDateReadable,
      nextFridayBathAlertSummary = nextFridayBathAlertSummary,
      nextSunnahFastSummary = nextFastLabel,
      upcoming7DaysSchedule = upcoming7Days,
      notificationTitle = notifTitle,
      notificationBody = notifBody,
      notificationReference = todaySpec.authenticReference
    )
  }

  fun loadState(context: Context, nowMillis: Long = System.currentTimeMillis()): SunnahRemindersUiState {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val fridayBathEnabled = prefs.getBoolean("friday_bath_enabled", true)
    val fridayBathTime = prefs.getString("friday_bath_time", "09:30") ?: "09:30"
    val dailyCalcEnabled = prefs.getBoolean("rem_enabled_rem_calculated_daily_day_sunnah", true)
    val dailyCalcTime = prefs.getString("rem_time_rem_calculated_daily_day_sunnah", "08:00") ?: "08:00"
    val completedChecklist = prefs.getStringSet("friday_checklist_completed", emptySet())?.toSet() ?: emptySet()
    val salawatCount = prefs.getInt("friday_salawat_count", 0)
    val favoriteIds = prefs.getStringSet("favorite_dua_ids", emptySet())?.toSet() ?: emptySet()
    val lastBanner = prefs.getString("last_notif_banner_msg", "") ?: ""

    val tasbihMap = mutableMapOf<String, Int>()
    for (dua in DailyDuasDataProvider.allDuas) {
      val c = prefs.getInt("tasbih_${dua.id}", 0)
      if (c > 0) tasbihMap[dua.id] = c
    }

    val reminderStates = mutableMapOf<String, ReminderItemState>()
    for (spec in DailyDuasDataProvider.defaultNecessaryReminders) {
      val isFridayBath = spec.id == "rem_friday_bath_ghusl"
      val enabled = if (isFridayBath) {
        fridayBathEnabled
      } else {
        prefs.getBoolean("rem_enabled_${spec.id}", spec.defaultEnabled)
      }
      val time24h = if (isFridayBath) {
        fridayBathTime
      } else {
        prefs.getString("rem_time_${spec.id}", spec.defaultTime24h) ?: spec.defaultTime24h
      }
      reminderStates[spec.id] = ReminderItemState(
        id = spec.id,
        enabled = enabled,
        time24h = time24h
      )
    }

    val prayerConfig = PrayerPreferencesManager.loadConfig(context)
    val tz = TimeZone.getTimeZone(prayerConfig.timeZoneId)
    val calcStatus = calculateDayAndWeekStatus(
      nowMillis = nowMillis,
      timeZone = tz,
      fridayBathTime24h = fridayBathTime
    )
    val savedPreviewDow = prefs.getInt("selected_preview_dow", calcStatus.todayDayOfWeek)

    return SunnahRemindersUiState(
      fridayBathReminderEnabled = fridayBathEnabled,
      fridayBathReminderTime24h = fridayBathTime,
      dailyCalculatedNotifEnabled = dailyCalcEnabled,
      dailyCalculatedNotifTime24h = dailyCalcTime,
      completedFridayChecklistIds = completedChecklist,
      fridaySalawatCount = salawatCount,
      favoriteDuaIds = favoriteIds,
      duaTasbihCounts = tasbihMap,
      reminderStates = reminderStates,
      calculatedDayStatus = calcStatus,
      selectedPreviewDayOfWeek = savedPreviewDow,
      lastNotificationBannerMessage = lastBanner
    )
  }

  fun selectPreviewDayOfWeek(context: Context, calendarDayOfWeek: Int): SunnahRemindersUiState {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt("selected_preview_dow", calendarDayOfWeek).apply()
    return loadState(context)
  }

  fun setFridayBathReminder(context: Context, enabled: Boolean, time24h: String): SunnahRemindersUiState {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putBoolean("friday_bath_enabled", enabled)
      .putString("friday_bath_time", time24h)
      .putBoolean("rem_enabled_rem_friday_bath_ghusl", enabled)
      .putString("rem_time_rem_friday_bath_ghusl", time24h)
      .apply()
    scheduleAllSunnahReminders(context)
    return loadState(context)
  }

  fun setDailyCalculatedReminder(context: Context, enabled: Boolean, time24h: String): SunnahRemindersUiState {
    return updateReminderConfig(
      context = context,
      reminderId = "rem_calculated_daily_day_sunnah",
      enabled = enabled,
      time24h = time24h
    )
  }

  fun updateReminderConfig(context: Context, reminderId: String, enabled: Boolean, time24h: String): SunnahRemindersUiState {
    if (reminderId == "rem_friday_bath_ghusl") {
      return setFridayBathReminder(context, enabled, time24h)
    }
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putBoolean("rem_enabled_$reminderId", enabled)
      .putString("rem_time_$reminderId", time24h)
      .apply()
    scheduleAllSunnahReminders(context)
    return loadState(context)
  }

  fun toggleFridayChecklistItem(context: Context, itemId: String): SunnahRemindersUiState {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet("friday_checklist_completed", emptySet())?.toMutableSet() ?: mutableSetOf()
    if (current.contains(itemId)) {
      current.remove(itemId)
    } else {
      current.add(itemId)
    }
    prefs.edit().putStringSet("friday_checklist_completed", current).apply()
    return loadState(context)
  }

  fun incrementFridaySalawat(context: Context, delta: Int = 1): SunnahRemindersUiState {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val next = if (delta == 0) 0 else (prefs.getInt("friday_salawat_count", 0) + delta).coerceAtLeast(0)
    prefs.edit().putInt("friday_salawat_count", next).apply()
    return loadState(context)
  }

  fun toggleFavoriteDua(context: Context, duaId: String): SunnahRemindersUiState {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val current = prefs.getStringSet("favorite_dua_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    if (current.contains(duaId)) {
      current.remove(duaId)
    } else {
      current.add(duaId)
    }
    prefs.edit().putStringSet("favorite_dua_ids", current).apply()
    return loadState(context)
  }

  fun incrementDuaTasbih(context: Context, duaId: String, reset: Boolean = false): SunnahRemindersUiState {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val next = if (reset) 0 else (prefs.getInt("tasbih_$duaId", 0) + 1)
    prefs.edit().putInt("tasbih_$duaId", next).apply()
    return loadState(context)
  }

  fun scheduleAllSunnahReminders(context: Context, nowMillis: Long = System.currentTimeMillis()): List<String> {
    val appCtx = context.applicationContext
    val alarmManager = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return emptyList()
    val state = loadState(appCtx, nowMillis)
    val prayerConfig = PrayerPreferencesManager.loadConfig(appCtx)
    val tz = TimeZone.getTimeZone(prayerConfig.timeZoneId)
    val scheduledDescriptions = mutableListOf<String>()

    DailyDuasDataProvider.defaultNecessaryReminders.forEachIndexed { index, spec ->
      val reqCode = BASE_REMINDER_REQ_CODE + index
      val itemState = state.reminderStates[spec.id] ?: ReminderItemState(
        id = spec.id,
        enabled = spec.defaultEnabled,
        time24h = spec.defaultTime24h
      )

      val existingIntent = Intent(appCtx, PrayerAlarmReceiver::class.java).apply {
        action = ACTION_SUNNAH_REMINDER_ALARM
      }
      val existingPi = PendingIntent.getBroadcast(
        appCtx,
        reqCode,
        existingIntent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (existingPi != null) {
        alarmManager.cancel(existingPi)
        existingPi.cancel()
      }

      if (itemState.enabled) {
        val triggerMillis = computeNextReminderTriggerMillis(
          time24h = itemState.time24h,
          frequency = spec.frequency,
          timeZone = tz,
          nowMillis = nowMillis
        )
        val isDynamicDayReminder = spec.id == "rem_calculated_daily_day_sunnah"
        val calcForTrigger = if (isDynamicDayReminder) {
          calculateDayAndWeekStatus(triggerMillis, tz, state.fridayBathReminderTime24h)
        } else {
          null
        }
        val resolvedTitle = calcForTrigger?.notificationTitle ?: spec.title
        val resolvedBody = calcForTrigger?.notificationBody ?: spec.subtitle
        val resolvedRef = calcForTrigger?.notificationReference ?: spec.authenticReference

        val alarmIntent = Intent(appCtx, PrayerAlarmReceiver::class.java).apply {
          action = ACTION_SUNNAH_REMINDER_ALARM
          putExtra(EXTRA_REMINDER_ID, spec.id)
          putExtra(EXTRA_REMINDER_TITLE, resolvedTitle)
          putExtra(EXTRA_REMINDER_BODY, resolvedBody)
          putExtra(EXTRA_REMINDER_REF, resolvedRef)
        }
        val pi = PendingIntent.getBroadcast(
          appCtx,
          reqCode,
          alarmIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
          } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pi)
          }
        } catch (_: Exception) {
        }
        scheduledDescriptions.add("${spec.title} (${PrayerTimeCalculator.format24hTo12h(itemState.time24h)} • ${spec.frequency.label})")
      }
    }
    return scheduledDescriptions
  }

  fun computeNextReminderTriggerMillis(
    time24h: String,
    frequency: ReminderFrequency,
    timeZone: TimeZone,
    nowMillis: Long = System.currentTimeMillis()
  ): Long {
    val totalMins = PrayerTimeCalculator.hhMmToMinutes(time24h)
    val hour = totalMins / 60
    val minute = totalMins % 60

    val cal = Calendar.getInstance(timeZone).apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, hour)
      set(Calendar.MINUTE, minute)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }

    when (frequency) {
      ReminderFrequency.WEEKLY_FRIDAY -> {
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY || cal.timeInMillis <= nowMillis + 5_000L) {
          cal.add(Calendar.DAY_OF_YEAR, 1)
        }
      }
      ReminderFrequency.SUNNAH_FASTING_DAYS -> {
        // Sunday & Wednesday evenings (before Monday & Thursday Sunnah fasts)
        while (
          (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && cal.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) ||
          cal.timeInMillis <= nowMillis + 5_000L
        ) {
          cal.add(Calendar.DAY_OF_YEAR, 1)
        }
      }
      ReminderFrequency.MONTHLY_HIJRI -> {
        // Advance until Ayyam al-Beed (13th, 14th, or 15th of the Hijri month)
        var guard = 0
        while (
          (!calculateHijriDateInfo(cal.timeInMillis, timeZone).isAyyamAlBeed || cal.timeInMillis <= nowMillis + 5_000L) &&
          guard < 40
        ) {
          cal.add(Calendar.DAY_OF_YEAR, 1)
          guard++
        }
      }
      ReminderFrequency.DAILY -> {
        if (cal.timeInMillis <= nowMillis + 5_000L) {
          cal.add(Calendar.DAY_OF_YEAR, 1)
        }
      }
    }
    return cal.timeInMillis
  }

  fun ensureReminderNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
      val channel = NotificationChannel(
        SUNNAH_REMINDER_CHANNEL_ID,
        "Friday Bath (Ghusl) & Daily Sunnah Reminders (No Music)",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Purely non-musical reminders for Friday Jumu'ah Ghusl, Surah Al-Kahf, and Calculated Daily Sunnahs"
        setSound(null, null)
        enableVibration(true)
        lockscreenVisibility = Notification.VISIBILITY_PUBLIC
      }
      nm.createNotificationChannel(channel)
    }
  }

  fun postSunnahReminderNotification(
    context: Context,
    reminderId: String,
    title: String,
    body: String,
    authenticReference: String
  ) {
    runCatching {
      ensureReminderNotificationChannel(context)
      val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
      val openIntent = PendingIntent.getActivity(
        context,
        reminderId.hashCode(),
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val notification = NotificationCompat.Builder(context, SUNNAH_REMINDER_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_bayan)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(
          NotificationCompat.BigTextStyle()
            .bigText("$body\nSource: $authenticReference • Silent / Non-Musical Sunnah Reminder")
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_REMINDER)
        .setSound(null)
        .setSilent(true)
        .setAutoCancel(true)
        .setContentIntent(openIntent)
        .build()

      val notifId = BASE_REMINDER_NOTIF_ID + (reminderId.hashCode() and 0x7FF)
      nm.notify(notifId, notification)

      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit()
        .putString("last_notif_banner_msg", "$title — Sent to Notification Bar")
        .apply()
    }
  }

  /**
   * Dynamically calculates the reminder for Today (or a specific weekday override such as Friday)
   * and immediately dispatches it as an Android system notification.
   */
  fun postCalculatedDayReminderNotification(
    context: Context,
    dayOfWeekOverride: Int? = null,
    nowMillis: Long = System.currentTimeMillis()
  ): SunnahRemindersUiState {
    val prayerConfig = PrayerPreferencesManager.loadConfig(context)
    val tz = TimeZone.getTimeZone(prayerConfig.timeZoneId)
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val fridayBathTime = prefs.getString("friday_bath_time", "09:30") ?: "09:30"

    val calcStatus = calculateDayAndWeekStatus(
      nowMillis = nowMillis,
      timeZone = tz,
      fridayBathTime24h = fridayBathTime
    )

    val targetSpec = if (dayOfWeekOverride != null) {
      DailyDuasDataProvider.getWeekdaySpec(dayOfWeekOverride)
    } else {
      calcStatus.todayWeekdaySpec
    }

    val title = if (targetSpec.isFridayJumuah) {
      "Jumu'ah Mubarak! Friday Bath (Ghusl), Surah Al-Kahf & Salawat Reminder"
    } else if (dayOfWeekOverride == null && calcStatus.isAyyamAlBeedToday) {
      "${targetSpec.dayNameEnglish} & Ayyam al-Beed (${calcStatus.hijriInfo.formattedReadable}) Reminder"
    } else {
      "${targetSpec.dayNameEnglish} (${targetSpec.arabicDayName}) — ${targetSpec.islamicDayTitle}"
    }

    val body = "${targetSpec.primaryReminderTitle} (${calcStatus.hijriInfo.formattedReadable}). ${targetSpec.englishGuidance}"

    postSunnahReminderNotification(
      context = context,
      reminderId = "calculated_day_${targetSpec.calendarDayOfWeek}",
      title = title,
      body = body,
      authenticReference = targetSpec.authenticReference
    )

    prefs.edit()
      .putString("last_auto_notified_date_iso", calcStatus.gregorianDateIso)
      .putInt("selected_preview_dow", targetSpec.calendarDayOfWeek)
      .apply()

    return loadState(context, nowMillis)
  }

  /**
   * Automatically dispatches today's calculated day notification once per day when enabled,
   * ensuring Friday and other days' calculated reminders appear in the Android notification bar.
   */
  fun ensureTodayCalculatedNotificationSentIfEnabled(
    context: Context,
    nowMillis: Long = System.currentTimeMillis()
  ): SunnahRemindersUiState {
    val state = loadState(context, nowMillis)
    if (!state.dailyCalculatedNotifEnabled) return state

    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val lastIso = prefs.getString("last_auto_notified_date_iso", "") ?: ""
    if (lastIso != state.calculatedDayStatus.gregorianDateIso) {
      return postCalculatedDayReminderNotification(
        context = context,
        dayOfWeekOverride = null,
        nowMillis = nowMillis
      )
    }
    return state
  }

  fun triggerTestReminder(context: Context, spec: NecessaryReminderSpec) {
    if (spec.id == "rem_calculated_daily_day_sunnah") {
      postCalculatedDayReminderNotification(context)
      return
    }
    postSunnahReminderNotification(
      context = context,
      reminderId = spec.id,
      title = spec.title,
      body = spec.subtitle,
      authenticReference = spec.authenticReference
    )
  }
}
