package com.example.ui.prayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.prayer.AzanNotificationMode
import com.example.data.prayer.PrayerPreferencesManager
import com.example.data.prayer.PrayerTimeCalculator

class PrayerAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action ?: return
    val appCtx = context.applicationContext

    when (action) {
      PrayerAlarmScheduler.ACTION_STOP_ALARM -> {
        AzanAudioController.stopAlarm(appCtx)
        AzanForegroundService.releaseAlarmWakeLock()
        appCtx.stopService(Intent(appCtx, AzanForegroundService::class.java))
        val nm = appCtx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(AzanForegroundService.NOTIFICATION_ID)
      }

      PrayerAlarmScheduler.ACTION_SNOOZE_ALARM -> {
        val snoozeMins = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_SNOOZE_MINUTES, 5)
        AzanAudioController.snoozeActiveAlarm(appCtx, snoozeMins)
        AzanForegroundService.releaseAlarmWakeLock()
        appCtx.stopService(Intent(appCtx, AzanForegroundService::class.java))
        val nm = appCtx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        nm?.cancel(AzanForegroundService.NOTIFICATION_ID)
      }

      PrayerAlarmScheduler.ACTION_FAJR_WAKEUP_ALARM,
      PrayerAlarmScheduler.ACTION_PRAYER_AZAN_ALARM -> {
        AzanForegroundService.acquireAlarmWakeLock(appCtx)
        val prayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "Fajr"
        val isSpecialFajr = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_SPECIAL_FAJR, false)
        val isTahajjud = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_TAHAJJUD, false)
        val modeName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_AZAN_MODE)
          ?: AzanNotificationMode.FULL_AZAN.name

        if (!isSpecialFajr && modeName == AzanNotificationMode.SILENT.name) {
          AzanForegroundService.postSilentPrayerNotification(appCtx, prayerName)
          AzanForegroundService.releaseAlarmWakeLock()
        } else {
          val serviceIntent = Intent(appCtx, AzanForegroundService::class.java).apply {
            putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(PrayerAlarmScheduler.EXTRA_IS_SPECIAL_FAJR, isSpecialFajr)
            putExtra(PrayerAlarmScheduler.EXTRA_IS_TAHAJJUD, isTahajjud)
            putExtra(PrayerAlarmScheduler.EXTRA_AZAN_MODE, modeName)
          }
          try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              appCtx.startForegroundService(serviceIntent)
            } else {
              appCtx.startService(serviceIntent)
            }
          } catch (_: Exception) {
            // If OS restricts background foreground-service start, start audio & post high-priority notification directly
            AzanAudioController.startAzanOrFajrAlarm(
              context = appCtx,
              prayerName = prayerName,
              isSpecial15MinFajrAlarm = isSpecialFajr,
              isTahajjudAlarm = isTahajjud,
              azanModeName = modeName
            )
            AzanForegroundService.postHighPriorityAlarmNotification(
              context = appCtx,
              prayerName = prayerName,
              isSpecialFajr = isSpecialFajr
            )
          }
        }

        // Automatically reschedule tomorrow's prayer alarms
        PrayerAlarmScheduler.scheduleAllDailyAlarms(appCtx)
      }

      com.example.data.prayer.SunnahReminderManager.ACTION_SUNNAH_REMINDER_ALARM -> {
        val remId = intent.getStringExtra(com.example.data.prayer.SunnahReminderManager.EXTRA_REMINDER_ID)
          ?: "rem_friday_bath_ghusl"

        if (remId == "rem_calculated_daily_day_sunnah") {
          com.example.data.prayer.SunnahReminderManager.postCalculatedDayReminderNotification(appCtx)
        } else {
          val remTitle = intent.getStringExtra(com.example.data.prayer.SunnahReminderManager.EXTRA_REMINDER_TITLE)
            ?: "Friday Bath (Jumu'ah Ghusl) & Sunnah Reminder"
          val remBody = intent.getStringExtra(com.example.data.prayer.SunnahReminderManager.EXTRA_REMINDER_BODY)
            ?: "Perform Jumu'ah Ghusl, use Miswak, wear clean garments, and recite Surah Al-Kahf."
          val remRef = intent.getStringExtra(com.example.data.prayer.SunnahReminderManager.EXTRA_REMINDER_REF)
            ?: "Sahih al-Bukhari 877"

          com.example.data.prayer.SunnahReminderManager.postSunnahReminderNotification(
            context = appCtx,
            reminderId = remId,
            title = remTitle,
            body = remBody,
            authenticReference = remRef
          )
        }
        com.example.data.prayer.SunnahReminderManager.scheduleAllSunnahReminders(appCtx)
      }
    }
  }
}

/**
 * Reschedules all five daily Salah alarms, the 15-minute Fajr wake-up alarm, and Sunnah/Friday Ghusl reminders after:
 * - Device reboot (BOOT_COMPLETED)
 * - Time-zone changes (TIMEZONE_CHANGED)
 * - Manual clock changes (TIME_SET)
 * - Date changes (DATE_CHANGED)
 * - App update (MY_PACKAGE_REPLACED)
 * - Exact alarm permission restoration (ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
 */
class BootAndTimeChangeReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val action = intent?.action ?: return
    if (action == Intent.ACTION_BOOT_COMPLETED ||
      action == Intent.ACTION_TIME_CHANGED ||
      action == Intent.ACTION_TIMEZONE_CHANGED ||
      action == Intent.ACTION_DATE_CHANGED ||
      action == Intent.ACTION_MY_PACKAGE_REPLACED ||
      action == "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
    ) {
      val appCtx = context.applicationContext
      val config = PrayerPreferencesManager.loadConfig(appCtx)
      PrayerAlarmScheduler.scheduleAllDailyAlarms(appCtx, null, config)
      com.example.data.prayer.SunnahReminderManager.scheduleAllSunnahReminders(appCtx)
      if (action == Intent.ACTION_DATE_CHANGED) {
        com.example.data.prayer.SunnahReminderManager.ensureTodayCalculatedNotificationSentIfEnabled(appCtx)
      }
    }
  }
}

class AzanForegroundService : Service() {

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    acquireAlarmWakeLock(this)
    val prayerName = intent?.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "Fajr"
    val isSpecialFajr = intent?.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_SPECIAL_FAJR, false) ?: false
    val isTahajjud = intent?.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_TAHAJJUD, false) ?: false
    val modeName = intent?.getStringExtra(PrayerAlarmScheduler.EXTRA_AZAN_MODE)
      ?: AzanNotificationMode.FULL_AZAN.name

    ensureNotificationChannel(this)
    val notification = buildAlarmNotification(this, prayerName, isSpecialFajr)
    try {
      startForeground(NOTIFICATION_ID, notification)
    } catch (_: Exception) {
    }

    AzanAudioController.startAzanOrFajrAlarm(
      context = this,
      prayerName = prayerName,
      isSpecial15MinFajrAlarm = isSpecialFajr,
      isTahajjudAlarm = isTahajjud,
      azanModeName = modeName
    )

    return START_NOT_STICKY
  }

  override fun onDestroy() {
    AzanAudioController.stopAlarm(this)
    releaseAlarmWakeLock()
    super.onDestroy()
  }

  companion object {
    private const val LEGACY_CHANNEL_ID = "bayan_azan_and_fajr_alarm_channel"
    const val CHANNEL_ID = "bayan_pure_vocal_azan_alarm_channel_v2"
    const val NOTIFICATION_ID = 4001
    private var activeWakeLock: PowerManager.WakeLock? = null

    fun acquireAlarmWakeLock(context: Context) {
      try {
        val pm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return
        val wl = activeWakeLock ?: pm.newWakeLock(
          PowerManager.PARTIAL_WAKE_LOCK,
          "bayan:adhan_alarm_wakelock"
        ).apply {
          setReferenceCounted(false)
        }
        wl.acquire(15 * 60 * 1000L)
        activeWakeLock = wl
      } catch (_: Exception) {
      }
    }

    fun releaseAlarmWakeLock() {
      try {
        if (activeWakeLock?.isHeld == true) {
          activeWakeLock?.release()
        }
      } catch (_: Exception) {
      }
    }

    fun ensureNotificationChannel(context: Context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        runCatching { nm.deleteNotificationChannel(LEGACY_CHANNEL_ID) }
        val channel = NotificationChannel(
          CHANNEL_ID,
          "Salah Azan & 15-Minute Fajr Wake-Up Alarm (Vocal Only, No Music)",
          NotificationManager.IMPORTANCE_HIGH
        ).apply {
          description = "Pure vocal Azan notifications and 15-minute Fajr wake-up alarm without system ringtone music"
          setSound(null, null)
          enableVibration(false)
          lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(channel)
      }
    }

    fun postHighPriorityAlarmNotification(
      context: Context,
      prayerName: String,
      isSpecialFajr: Boolean
    ) {
      runCatching {
        ensureNotificationChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        nm.notify(NOTIFICATION_ID, buildAlarmNotification(context, prayerName, isSpecialFajr))
      }
    }

    fun postSilentPrayerNotification(context: Context, prayerName: String) {
      runCatching {
        ensureNotificationChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val openIntent = PendingIntent.getActivity(
          context,
          0,
          Intent(context, MainActivity::class.java),
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
          .setSmallIcon(R.drawable.ic_stat_bayan)
          .setContentTitle("Time for $prayerName Prayer")
          .setContentText("Hayya 'alas-Salah — Come to Prayer ($prayerName)")
          .setContentIntent(openIntent)
          .setAutoCancel(true)
          .setSound(null)
          .setSilent(true)
          .build()
        nm.notify(NOTIFICATION_ID + 1, notif)
      }
    }

    fun buildAlarmNotification(
      context: Context,
      prayerName: String,
      isSpecialFajr: Boolean
    ): Notification {
      val openAppIntent = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val stopIntent = PendingIntent.getBroadcast(
        context,
        10,
        Intent(context, PrayerAlarmReceiver::class.java).apply {
          action = PrayerAlarmScheduler.ACTION_STOP_ALARM
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val snooze5Intent = PendingIntent.getBroadcast(
        context,
        11,
        Intent(context, PrayerAlarmReceiver::class.java).apply {
          action = PrayerAlarmScheduler.ACTION_SNOOZE_ALARM
          putExtra(PrayerAlarmScheduler.EXTRA_SNOOZE_MINUTES, 5)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val snooze10Intent = PendingIntent.getBroadcast(
        context,
        12,
        Intent(context, PrayerAlarmReceiver::class.java).apply {
          action = PrayerAlarmScheduler.ACTION_SNOOZE_ALARM
          putExtra(PrayerAlarmScheduler.EXTRA_SNOOZE_MINUTES, 10)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      val title = if (isSpecialFajr || prayerName.contains("Fajr", ignoreCase = true)) {
        "Fajr Wake-Up • ${PrayerTimeCalculator.FAJR_TATHWIB_ARABIC}"
      } else {
        "$prayerName Pure Vocal Azan — Hayya 'alas-Salah"
      }

      val body = if (isSpecialFajr || prayerName.contains("Fajr", ignoreCase = true)) {
        "${PrayerTimeCalculator.FAJR_TATHWIB_TRANSLITERATION} (${PrayerTimeCalculator.FAJR_TATHWIB_ENGLISH}) • Pure Vocal Azan (No Music)"
      } else {
        "It is time for $prayerName prayer (Pure Vocal Azan, No Music). Tap Stop or Snooze."
      }

      return NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_bayan)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText("$body\nنماز نیند سے بہتر ہے"))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setSound(null)
        .setSilent(true)
        .setOngoing(true)
        .setContentIntent(openAppIntent)
        .setFullScreenIntent(openAppIntent, true)
        .addAction(0, "Stop Alarm", stopIntent)
        .addAction(0, "Snooze 5m", snooze5Intent)
        .addAction(0, "Snooze 10m", snooze10Intent)
        .build()
    }
  }
}
