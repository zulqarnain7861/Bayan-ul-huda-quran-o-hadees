package com.example.ui.prayer

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.local.entities.PrayerScheduleEntity
import com.example.data.prayer.AzanNotificationMode
import com.example.data.prayer.PrayerAndAlarmConfig
import com.example.data.prayer.PrayerPreferencesManager
import com.example.data.prayer.PrayerTimeCalculator
import java.util.Calendar
import java.util.TimeZone

data class AlarmSetupDiagnostics(
  val hasLocationPermission: Boolean,
  val hasNotificationPermission: Boolean,
  val canScheduleExactAlarms: Boolean,
  val isIgnoringBatteryOptimizations: Boolean,
  val alarmStreamVolumePercent: Int,
  val isAlarmStreamMuted: Boolean,
  val scheduledAlarmsSummary: List<String>
)

object PrayerAlarmScheduler {

  const val ACTION_PRAYER_AZAN_ALARM = "com.example.bayan.ACTION_PRAYER_AZAN_ALARM"
  const val ACTION_FAJR_WAKEUP_ALARM = "com.example.bayan.ACTION_FAJR_WAKEUP_ALARM"
  const val ACTION_STOP_ALARM = "com.example.bayan.ACTION_STOP_ALARM"
  const val ACTION_SNOOZE_ALARM = "com.example.bayan.ACTION_SNOOZE_ALARM"

  const val EXTRA_PRAYER_NAME = "extra_prayer_name"
  const val EXTRA_AZAN_MODE = "extra_azan_mode"
  const val EXTRA_IS_SPECIAL_FAJR = "extra_is_special_fajr"
  const val EXTRA_IS_TAHAJJUD = "extra_is_tahajjud"
  const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"

  private const val REQ_FAJR = 1001
  private const val REQ_DHUHR = 1002
  private const val REQ_ASR = 1003
  private const val REQ_MAGHRIB = 1004
  private const val REQ_ISHA = 1005
  private const val REQ_FAJR_SPECIAL_WAKEUP = 2001
  private const val REQ_TAHAJJUD_WAKEUP = 2002
  private const val REQ_SNOOZE = 2003

  /**
   * Schedules or refreshes all daily Salah Azan alarms, the dedicated 15-Minute Fajr Wake-Up Alarm,
   * and optional Tahajjud wake-up alarm. Cancels any existing duplicate alarms first and skips stale times.
   */
  fun scheduleAllDailyAlarms(
    context: Context,
    schedule: PrayerScheduleEntity? = null,
    config: PrayerAndAlarmConfig = PrayerPreferencesManager.loadConfig(context)
  ): List<String> {
    val appCtx = context.applicationContext
    val alarmManager = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return emptyList()

    val effectiveSchedule = schedule ?: PrayerTimeCalculator.calculateOfflineSchedule(
      locationName = config.city,
      districtOrRegion = config.districtOrRegion,
      country = config.country,
      latitude = config.latitude,
      longitude = config.longitude,
      timeZoneId = config.timeZoneId,
      methodId = config.calculationMethodId,
      asrMadhhab = config.asrMadhhab,
      highLatitudeRule = config.highLatitudeRule
    )

    val tz = TimeZone.getTimeZone(effectiveSchedule.timeZoneId)
    val nowMillis = System.currentTimeMillis()
    val summary = mutableListOf<String>()

    val fivePrayers = listOf(
      Triple("Fajr", effectiveSchedule.fajrTime, config.fajrAzanMode to REQ_FAJR),
      Triple("Dhuhr", effectiveSchedule.dhuhrTime, config.dhuhrAzanMode to REQ_DHUHR),
      Triple("Asr", effectiveSchedule.asrTime, config.asrAzanMode to REQ_ASR),
      Triple("Maghrib", effectiveSchedule.maghribTime, config.maghribAzanMode to REQ_MAGHRIB),
      Triple("Isha", effectiveSchedule.ishaTime, config.ishaAzanMode to REQ_ISHA)
    )

    for ((prayerName, time24, modeAndReq) in fivePrayers) {
      val (mode, reqCode) = modeAndReq
      cancelPendingAlarm(appCtx, alarmManager, reqCode, ACTION_PRAYER_AZAN_ALARM)

      if (mode != AzanNotificationMode.OFF) {
        val triggerMillis = computeNextOccurrenceMillis(time24, tz, nowMillis)
        val intent = Intent(appCtx, PrayerAlarmReceiver::class.java).apply {
          action = ACTION_PRAYER_AZAN_ALARM
          putExtra(EXTRA_PRAYER_NAME, prayerName)
          putExtra(EXTRA_AZAN_MODE, mode.name)
          putExtra(EXTRA_IS_SPECIAL_FAJR, false)
        }
        val pi = PendingIntent.getBroadcast(
          appCtx,
          reqCode,
          intent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val useAlarmClockWakeup = mode == AzanNotificationMode.FULL_AZAN || mode == AzanNotificationMode.SHORT_TONE
        setExactOrFallbackAlarm(appCtx, alarmManager, triggerMillis, pi, isWakeUpAlarmClock = useAlarmClockWakeup)
        summary.add("$prayerName (${PrayerTimeCalculator.format24hTo12h(time24)}) — ${mode.label}")
      }
    }

    // Dedicated 15-Minute Fajr Wake-Up Alarm
    cancelPendingAlarm(appCtx, alarmManager, REQ_FAJR_SPECIAL_WAKEUP, ACTION_FAJR_WAKEUP_ALARM)
    if (config.fajrWakeUpAlarmEnabled) {
      val wakeTime24 = PrayerTimeCalculator.calculateWakeUpTimeBeforeFajr(
        effectiveSchedule.fajrTime,
        config.fajrWakeUpOffsetMinutes
      )
      val triggerMillis = computeNextOccurrenceMillis(wakeTime24, tz, nowMillis)
      val intent = Intent(appCtx, PrayerAlarmReceiver::class.java).apply {
        action = ACTION_FAJR_WAKEUP_ALARM
        putExtra(EXTRA_PRAYER_NAME, "Fajr Wake-Up")
        putExtra(EXTRA_IS_SPECIAL_FAJR, true)
        putExtra(EXTRA_IS_TAHAJJUD, false)
      }
      val pi = PendingIntent.getBroadcast(
        appCtx,
        REQ_FAJR_SPECIAL_WAKEUP,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      setExactOrFallbackAlarm(appCtx, alarmManager, triggerMillis, pi, isWakeUpAlarmClock = true)
      summary.add(
        "Fajr 15-Min Wake-Up (${PrayerTimeCalculator.format24hTo12h(wakeTime24)}, ${config.fajrWakeUpOffsetMinutes}m before Fajr)"
      )
    }

    // Optional Tahajjud Final-Third-of-Night Wake-Up
    cancelPendingAlarm(appCtx, alarmManager, REQ_TAHAJJUD_WAKEUP, ACTION_FAJR_WAKEUP_ALARM)
    if (config.tahajjudWakeUpEnabled) {
      val tahajjudTime24 = effectiveSchedule.tahajjudLastThirdStart
      val triggerMillis = computeNextOccurrenceMillis(tahajjudTime24, tz, nowMillis)
      val intent = Intent(appCtx, PrayerAlarmReceiver::class.java).apply {
        action = ACTION_FAJR_WAKEUP_ALARM
        putExtra(EXTRA_PRAYER_NAME, "Tahajjud (Final Third)")
        putExtra(EXTRA_IS_SPECIAL_FAJR, true)
        putExtra(EXTRA_IS_TAHAJJUD, true)
      }
      val pi = PendingIntent.getBroadcast(
        appCtx,
        REQ_TAHAJJUD_WAKEUP,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
      setExactOrFallbackAlarm(appCtx, alarmManager, triggerMillis, pi, isWakeUpAlarmClock = true)
      summary.add("Tahajjud Final Third (${PrayerTimeCalculator.format24hTo12h(tahajjudTime24)})")
    }

    return summary
  }

  fun scheduleSnoozeAlarm(
    context: Context,
    snoozeMinutes: Int,
    isFajrSpecialAlarm: Boolean
  ) {
    val appCtx = context.applicationContext
    val alarmManager = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val triggerMillis = System.currentTimeMillis() + snoozeMinutes * 60_000L

    val intent = Intent(appCtx, PrayerAlarmReceiver::class.java).apply {
      action = ACTION_FAJR_WAKEUP_ALARM
      putExtra(EXTRA_PRAYER_NAME, if (isFajrSpecialAlarm) "Fajr Wake-Up (Snoozed)" else "Prayer Reminder")
      putExtra(EXTRA_IS_SPECIAL_FAJR, isFajrSpecialAlarm)
    }
    val pi = PendingIntent.getBroadcast(
      appCtx,
      REQ_SNOOZE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    setExactOrFallbackAlarm(appCtx, alarmManager, triggerMillis, pi, isWakeUpAlarmClock = true)
  }

  fun getSetupDiagnostics(
    context: Context,
    schedule: PrayerScheduleEntity? = null
  ): AlarmSetupDiagnostics {
    val appCtx = context.applicationContext
    val hasFineLocation = ContextCompat.checkSelfPermission(
      appCtx,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarseLocation = ContextCompat.checkSelfPermission(
      appCtx,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      ContextCompat.checkSelfPermission(
        appCtx,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    } else {
      true
    }

    val alarmManager = appCtx.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      alarmManager?.canScheduleExactAlarms() ?: false
    } else {
      true
    }

    val powerManager = appCtx.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val ignoringBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      powerManager?.isIgnoringBatteryOptimizations(appCtx.packageName) ?: false
    } else {
      true
    }

    val audioManager = appCtx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    val maxVol = (audioManager?.getStreamMaxVolume(AudioManager.STREAM_ALARM) ?: 7).coerceAtLeast(1)
    val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 5
    val volPct = ((curVol * 100) / maxVol).coerceIn(0, 100)

    val scheduled = scheduleAllDailyAlarms(appCtx, schedule)

    return AlarmSetupDiagnostics(
      hasLocationPermission = hasFineLocation || hasCoarseLocation,
      hasNotificationPermission = hasNotification,
      canScheduleExactAlarms = canExact,
      isIgnoringBatteryOptimizations = ignoringBattery,
      alarmStreamVolumePercent = volPct,
      isAlarmStreamMuted = curVol <= 0,
      scheduledAlarmsSummary = scheduled
    )
  }

  fun computeNextOccurrenceMillis(
    hhMm: String,
    timeZone: TimeZone,
    nowMillis: Long = System.currentTimeMillis()
  ): Long {
    val mins = PrayerTimeCalculator.hhMmToMinutes(hhMm)
    val cal = Calendar.getInstance(timeZone).apply {
      timeInMillis = nowMillis
      set(Calendar.HOUR_OF_DAY, mins / 60)
      set(Calendar.MINUTE, mins % 60)
      set(Calendar.SECOND, 0)
      set(Calendar.MILLISECOND, 0)
    }
    // Avoid firing for stale/past prayer times — schedule for tomorrow if already passed
    if (cal.timeInMillis <= nowMillis + 5_000L) {
      cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return cal.timeInMillis
  }

  private fun cancelPendingAlarm(
    context: Context,
    alarmManager: AlarmManager,
    requestCode: Int,
    action: String
  ) {
    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply { this.action = action }
    val existing = PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (existing != null) {
      alarmManager.cancel(existing)
      existing.cancel()
    }
  }

  private fun setExactOrFallbackAlarm(
    context: Context,
    alarmManager: AlarmManager,
    triggerAtMillis: Long,
    operation: PendingIntent,
    isWakeUpAlarmClock: Boolean
  ) {
    try {
      val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
      } else {
        true
      }
      if (canExact) {
        if (isWakeUpAlarmClock) {
          val showIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
          alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
            operation
          )
        } else {
          alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            operation
          )
        }
      } else {
        alarmManager.setAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          operation
        )
      }
    } catch (_: SecurityException) {
      alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
    }
  }
}
