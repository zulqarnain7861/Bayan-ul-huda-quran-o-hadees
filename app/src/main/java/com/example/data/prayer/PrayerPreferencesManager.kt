package com.example.data.prayer

import android.content.Context

enum class AzanNotificationMode(val label: String) {
  FULL_AZAN("Full Vocal Azan (No Music)"),
  SHORT_TONE("Short Vocal Takbir (No Music)"),
  SILENT("Silent Reminder"),
  OFF("Disabled")
}

enum class MuezzinAudioOption(
  val id: String,
  val title: String,
  val subtitle: String,
  val streamUrl: String,
  val includesFajrTathwib: Boolean = false,
  val originMasjid: String = "Masjid al-Haram, Makkah",
  val maqamOrStyle: String = "Traditional Hijazi Maqam",
  val tradition: String = "SUNNI",
  val fallbackUrls: List<String> = emptyList(),
  val localAssetPath: String = "adhan/$id.mp3",
  val isOfflineBundled: Boolean = true
) {
  MADINAH_NABAWI(
    id = "madinah_nabawi",
    title = "Masjid an-Nabawi — Sheikh Essam Bukhari (Madinah)",
    subtitle = "Masjid an-Nabawi • Verified Offline Pure Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan2.mp3",
    originMasjid = "Masjid an-Nabawi, Al-Madinah",
    maqamOrStyle = "Madani Maqam Rast (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/madinah_nabawi.mp3"
  ),
  MAKKAH_HARAM(
    id = "makkah_haram",
    title = "Masjid al-Haram — Sheikh Ali Ahmed Mulla (Makkah)",
    subtitle = "Bilal of Masjid al-Haram • Verified Offline Pure Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan1.mp3",
    originMasjid = "Masjid al-Haram, Makkah",
    maqamOrStyle = "Maqam Hijaz • Grand Mosque (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/makkah_haram.mp3"
  ),
  MADINAH_SURAYHI(
    id = "madinah_surayhi",
    title = "Masjid an-Nabawi — Sheikh Abdul Majeed al-Surayhi",
    subtitle = "Masjid an-Nabawi • Verified Offline Prophet's Mosque Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan7.mp3",
    originMasjid = "Masjid an-Nabawi, Al-Madinah",
    maqamOrStyle = "Madani Maqam Sikah (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/madinah_surayhi.mp3"
  ),
  MADINAH_OMAR_SUNBUL(
    id = "madinah_omar_sunbul",
    title = "Masjid an-Nabawi — Sheikh Omar Sunbul",
    subtitle = "Masjid an-Nabawi • Traditional Madani Offline Pure Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan8.mp3",
    originMasjid = "Masjid an-Nabawi, Al-Madinah",
    maqamOrStyle = "Madani Traditional (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/madinah_omar_sunbul.mp3"
  ),
  MAKKAH_NAIF_SALEH(
    id = "makkah_naif_saleh",
    title = "Masjid al-Haram — Sheikh Naif Saleh Faidah",
    subtitle = "Masjid al-Haram Senior Muezzin • Offline Melodious Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan4.mp3",
    originMasjid = "Masjid al-Haram, Makkah",
    maqamOrStyle = "Maqam Bayati & Hijaz (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/makkah_naif_saleh.mp3"
  ),
  MAKKAH_FAROOQ_HADRAWI(
    id = "makkah_farooq_hadrawi",
    title = "Masjid al-Haram — Sheikh Farooq Abdur-Rahman Hadrawi",
    subtitle = "Masjid al-Haram • Reverent Offline Makkah Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan5.mp3",
    originMasjid = "Masjid al-Haram, Makkah",
    maqamOrStyle = "Classical Haramain Vocal (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/makkah_farooq_hadrawi.mp3"
  ),
  MAKKAH_HAMAD_DEGHRERI(
    id = "makkah_hamad_deghreri",
    title = "Makkah — Sheikh Hamad Deghreri",
    subtitle = "Heart-Touching Haramain Style Offline Pure Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan6.mp3",
    originMasjid = "Makkah / Riyadh",
    maqamOrStyle = "Maqam Saba & Hijaz (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/makkah_hamad_deghreri.mp3"
  ),
  AL_AQSA(
    id = "al_aqsa",
    title = "Al-Quds — Masjid al-Aqsa (Sheikh Naji Qazzaz)",
    subtitle = "Historic Jerusalem Sanctuary Offline Pure Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan3.mp3",
    originMasjid = "Masjid al-Aqsa, Al-Quds",
    maqamOrStyle = "Levantine Maqam Bayati (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/al_aqsa.mp3"
  ),
  EGYPT_ABDUL_BASIT(
    id = "egypt_abdul_basit",
    title = "Cairo — Sheikh Abdul Basit Abdus Samad",
    subtitle = "Legendary Egyptian Qari • Golden Voice Offline Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan9.mp3",
    originMasjid = "Imam Shafi'i Mosque, Cairo",
    maqamOrStyle = "Egyptian Maqam Nahawand & Rast (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/egypt_abdul_basit.mp3"
  ),
  EGYPT_HUSARY(
    id = "egypt_husary",
    title = "Al-Azhar Cairo — Sheikh Mahmoud Khalil Al-Hussary",
    subtitle = "Al-Azhar Mosque • Classical Mujawwad Offline Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan10.mp3",
    originMasjid = "Al-Azhar Mosque, Cairo",
    maqamOrStyle = "Classical Azhari Maqam (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/egypt_husary.mp3"
  ),
  EGYPT_MINSHAWI(
    id = "egypt_minshawi",
    title = "Cairo — Sheikh Muhammad Siddiq Al-Minshawi",
    subtitle = "Deeply Spiritual Cairo Offline Pure Vocal Adhan (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan11.mp3",
    originMasjid = "Cairo Traditional",
    maqamOrStyle = "Maqam Bayati & Saba (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/egypt_minshawi.mp3"
  ),
  FAJR_TATHWIB(
    id = "fajr_tathwib",
    title = "Makkah Fajr Special Adhan (With As-salatu khayrun minan-nawm)",
    subtitle = "Offline Haramain Fajr Adhan with الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan12.mp3",
    includesFajrTathwib = true,
    originMasjid = "Masjid al-Haram, Makkah (Dawn)",
    maqamOrStyle = "Dawn Fajr Tathwib (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/fajr_tathwib.mp3"
  ),
  MADINAH_FAJR_TATHWIB(
    id = "madinah_fajr_tathwib",
    title = "Masjid an-Nabawi Fajr Special Adhan (Madinah Dawn)",
    subtitle = "Offline Madinah Dawn Adhan with الصَّلَاةُ خَيْرٌ مِنَ النَّوْمِ (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan13.mp3",
    includesFajrTathwib = true,
    originMasjid = "Masjid an-Nabawi, Al-Madinah (Dawn)",
    maqamOrStyle = "Madani Dawn Tathwib (Offline Local Recording)",
    tradition = "SUNNI",
    localAssetPath = "adhan/madinah_fajr_tathwib.mp3"
  ),
  SHIA_KARBALA_NAJAF(
    id = "shia_karbala_najaf",
    title = "Karbala & Najaf Sanctuary Adhan (Ja'fari Tradition)",
    subtitle = "Offline Pure Vocal Shia Adhan with حَيَّ عَلَىٰ خَيْرِ الْعَمَلِ • High-Fidelity Recording (No Music)",
    streamUrl = "https://www.islamcan.com/audio/adhan/azan14.mp3",
    includesFajrTathwib = false,
    originMasjid = "Imam Ali & Imam Hussain Shrines",
    maqamOrStyle = "Ja'fari Classical Vocal (Offline Local Recording)",
    tradition = "SHIA",
    localAssetPath = "adhan/shia_karbala_najaf.mp3"
  )
}

enum class AppThemeMode(val label: String, val description: String) {
  AUTO("System Default", "Automatically switches based on system & time"),
  LIGHT("Light", "Warm Cream & Emerald for daytime clarity"),
  DARK("Dark", "Deep Night Obsidian & Soft Gold for nighttime"),
  PURE_BLACK("Pure Black (AMOLED)", "True #000000 OLED black for zero-glare night reading"),
  MADINAH_PARCHMENT("Warm Paper", "Soft Warm Paper & Illuminated Manuscript Ivory"),
  EYE_COMFORT("Sepia", "Warm Sepia Low-Glare Amber for extended reading"),
  ROYAL_EMERALD("Green", "Deep Haramain Sanctuary Emerald & Burnished Gold"),
  SAPPHIRE_BLUE("Blue", "Classical Islamic Lapis Blue & Warm Gold");

  companion object {
    val SYSTEM_DEFAULT = AUTO
    val WARM_PAPER = MADINAH_PARCHMENT
    val SEPIA = EYE_COMFORT
    val GREEN = ROYAL_EMERALD
    val BLUE = SAPPHIRE_BLUE
  }
}

data class PrayerAndAlarmConfig(
  val city: String = "Srinagar",
  val districtOrRegion: String = "Srinagar District, Kashmir",
  val country: String = "India",
  val latitude: Double = 34.0837,
  val longitude: Double = 74.7973,
  val timeZoneId: String = "Asia/Kolkata",
  val useAutoLocation: Boolean = false,
  val calculationMethodId: Int = 1, // Karachi (18°, 18°) default for Kashmir/India
  val asrMadhhab: Int = 1, // 1 = Hanafi, 0 = Shafi'i
  val highLatitudeRule: Int = 0,
  // Per-prayer Azan modes
  val fajrAzanMode: AzanNotificationMode = AzanNotificationMode.FULL_AZAN,
  val dhuhrAzanMode: AzanNotificationMode = AzanNotificationMode.FULL_AZAN,
  val asrAzanMode: AzanNotificationMode = AzanNotificationMode.FULL_AZAN,
  val maghribAzanMode: AzanNotificationMode = AzanNotificationMode.FULL_AZAN,
  val ishaAzanMode: AzanNotificationMode = AzanNotificationMode.FULL_AZAN,
  val selectedMuezzinId: String = MuezzinAudioOption.MADINAH_NABAWI.id,
  val prePrayerReminderMinutes: Int = 10, // 0, 5, 10, 15, 20
  // Dedicated 15-Minute Fajr Wake-Up Alarm
  val fajrWakeUpAlarmEnabled: Boolean = true,
  val fajrWakeUpOffsetMinutes: Int = 30, // 60, 45, 30, 20, 15, 10 or custom
  val tahajjudWakeUpEnabled: Boolean = false,
  val wakeUpLanguage: String = "ALL", // "ALL", "AR", "UR", "EN"
  val progressiveSequence: Boolean = true, // Gentle tone -> Fajr Azan -> Progressive Wake-Up
  val alarmVolumePercent: Int = 100, // 0..100% (Default 100% of permitted device alarm volume)
  val boostSystemAlarmStream: Boolean = true, // Uses Android STREAM_ALARM at highest permitted device volume
  val vibrationEnabled: Boolean = true,
  val continuousVibration: Boolean = true,
  val defaultSnoozeMinutes: Int = 5, // 5 or 10
  // Quran reader preferences
  val sunniQuranReciterId: String = "husary",
  val shiaQuranReciterId: String = "karim_mansouri",
  val sunniTranslationKey: String = "en.sahih",
  val shiaTranslationKey: String = "en.qarai",
  // Reading Progress Tracker (Last Read Surah & Verse Position)
  val lastReadSurahNumber: Int = 1,
  val lastReadVerseNumber: Int = 1,
  val lastReadSurahNameEn: String = "Al-Fatihah",
  val lastReadSurahNameAr: String = "الفاتحة",
  val lastReadTotalVerses: Int = 7,
  val lastReadTradition: String = "SUNNI",
  val lastReadTimestamp: Long = 0L,
  // App Interface Language & First-Launch Onboarding (Separate from Quran/Hadith Content Language)
  val appInterfaceLanguageCode: String = "en",
  val hasCompletedLanguageOnboarding: Boolean = false,
  // Manual minute adjustments for Sehri (Fajr) and Iftar (Maghrib) Timetable
  val sehriAdjustmentMinutes: Int = 0,
  val iftarAdjustmentMinutes: Int = 0,
  val selectedTimetableGregorianYear: Int = 2026,
  val selectedTimetableHijriYear: Int = 1447,
  // Appearance theme
  val themeMode: AppThemeMode = AppThemeMode.LIGHT
) {
  val is200PercentBoostActive: Boolean
    get() = alarmVolumePercent >= 100
}

object PrayerPreferencesManager {

  private const val PREFS_NAME = "bayan_prayer_and_alarm_prefs"

  fun loadConfig(context: Context): PrayerAndAlarmConfig {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return PrayerAndAlarmConfig(
      city = prefs.getString("city", "Srinagar") ?: "Srinagar",
      districtOrRegion = prefs.getString("district", "Srinagar District, Kashmir") ?: "Srinagar District, Kashmir",
      country = prefs.getString("country", "India") ?: "India",
      latitude = prefs.getString("latitude", "34.0837")?.toDoubleOrNull() ?: 34.0837,
      longitude = prefs.getString("longitude", "74.7973")?.toDoubleOrNull() ?: 74.7973,
      timeZoneId = prefs.getString("timeZoneId", "Asia/Kolkata") ?: "Asia/Kolkata",
      useAutoLocation = prefs.getBoolean("useAutoLocation", false),
      calculationMethodId = prefs.getInt("calculationMethodId", 1),
      asrMadhhab = prefs.getInt("asrMadhhab", 1),
      highLatitudeRule = prefs.getInt("highLatitudeRule", 0),
      fajrAzanMode = parseAzanMode(prefs.getString("fajrAzanMode", AzanNotificationMode.FULL_AZAN.name)),
      dhuhrAzanMode = parseAzanMode(prefs.getString("dhuhrAzanMode", AzanNotificationMode.FULL_AZAN.name)),
      asrAzanMode = parseAzanMode(prefs.getString("asrAzanMode", AzanNotificationMode.FULL_AZAN.name)),
      maghribAzanMode = parseAzanMode(prefs.getString("maghribAzanMode", AzanNotificationMode.FULL_AZAN.name)),
      ishaAzanMode = parseAzanMode(prefs.getString("ishaAzanMode", AzanNotificationMode.FULL_AZAN.name)),
      selectedMuezzinId = prefs.getString("selectedMuezzinId", MuezzinAudioOption.MADINAH_NABAWI.id)
        ?: MuezzinAudioOption.MADINAH_NABAWI.id,
      prePrayerReminderMinutes = prefs.getInt("prePrayerReminderMinutes", 10),
      fajrWakeUpAlarmEnabled = prefs.getBoolean("fajrWakeUpAlarmEnabled", true),
      fajrWakeUpOffsetMinutes = prefs.getInt("fajrWakeUpOffsetMinutes", 30),
      tahajjudWakeUpEnabled = prefs.getBoolean("tahajjudWakeUpEnabled", false),
      wakeUpLanguage = prefs.getString("wakeUpLanguage", "ALL") ?: "ALL",
      progressiveSequence = prefs.getBoolean("progressiveSequence", true),
      alarmVolumePercent = prefs.getInt("alarmVolumePercent", 100).coerceIn(0, 100),
      boostSystemAlarmStream = prefs.getBoolean("boostSystemAlarmStream", true),
      vibrationEnabled = prefs.getBoolean("vibrationEnabled", false),
      continuousVibration = prefs.getBoolean("continuousVibration", false),
      defaultSnoozeMinutes = prefs.getInt("defaultSnoozeMinutes", 5),
      sunniQuranReciterId = prefs.getString("sunniQuranReciterId", "husary") ?: "husary",
      shiaQuranReciterId = prefs.getString("shiaQuranReciterId", "karim_mansouri") ?: "karim_mansouri",
      sunniTranslationKey = prefs.getString("sunniTranslationKey", "en.sahih") ?: "en.sahih",
      shiaTranslationKey = prefs.getString("shiaTranslationKey", "en.qarai") ?: "en.qarai",
      lastReadSurahNumber = prefs.getInt("lastReadSurahNumber", 1).coerceIn(1, 114),
      lastReadVerseNumber = prefs.getInt("lastReadVerseNumber", 1).coerceAtLeast(1),
      lastReadSurahNameEn = prefs.getString("lastReadSurahNameEn", "Al-Fatihah") ?: "Al-Fatihah",
      lastReadSurahNameAr = prefs.getString("lastReadSurahNameAr", "الفاتحة") ?: "الفاتحة",
      lastReadTotalVerses = prefs.getInt("lastReadTotalVerses", 7).coerceAtLeast(1),
      lastReadTradition = prefs.getString("lastReadTradition", "SUNNI") ?: "SUNNI",
      lastReadTimestamp = prefs.getLong("lastReadTimestamp", 0L),
      appInterfaceLanguageCode = prefs.getString("appInterfaceLanguageCode", "en") ?: "en",
      hasCompletedLanguageOnboarding = prefs.getBoolean("hasCompletedLanguageOnboarding", false),
      sehriAdjustmentMinutes = prefs.getInt("sehriAdjustmentMinutes", 0).coerceIn(-60, 60),
      iftarAdjustmentMinutes = prefs.getInt("iftarAdjustmentMinutes", 0).coerceIn(-60, 60),
      selectedTimetableGregorianYear = prefs.getInt("selectedTimetableGregorianYear", 2026),
      selectedTimetableHijriYear = prefs.getInt("selectedTimetableHijriYear", 1447),
      themeMode = parseThemeMode(prefs.getString("themeMode", AppThemeMode.LIGHT.name))
    )
  }

  fun saveConfig(context: Context, config: PrayerAndAlarmConfig) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit()
      .putString("city", config.city)
      .putString("district", config.districtOrRegion)
      .putString("country", config.country)
      .putString("latitude", config.latitude.toString())
      .putString("longitude", config.longitude.toString())
      .putString("timeZoneId", config.timeZoneId)
      .putBoolean("useAutoLocation", config.useAutoLocation)
      .putInt("calculationMethodId", config.calculationMethodId)
      .putInt("asrMadhhab", config.asrMadhhab)
      .putInt("highLatitudeRule", config.highLatitudeRule)
      .putString("fajrAzanMode", config.fajrAzanMode.name)
      .putString("dhuhrAzanMode", config.dhuhrAzanMode.name)
      .putString("asrAzanMode", config.asrAzanMode.name)
      .putString("maghribAzanMode", config.maghribAzanMode.name)
      .putString("ishaAzanMode", config.ishaAzanMode.name)
      .putString("selectedMuezzinId", config.selectedMuezzinId)
      .putInt("prePrayerReminderMinutes", config.prePrayerReminderMinutes)
      .putBoolean("fajrWakeUpAlarmEnabled", config.fajrWakeUpAlarmEnabled)
      .putInt("fajrWakeUpOffsetMinutes", config.fajrWakeUpOffsetMinutes)
      .putBoolean("tahajjudWakeUpEnabled", config.tahajjudWakeUpEnabled)
      .putString("wakeUpLanguage", config.wakeUpLanguage)
      .putBoolean("progressiveSequence", config.progressiveSequence)
      .putInt("alarmVolumePercent", config.alarmVolumePercent.coerceIn(0, 100))
      .putBoolean("boostSystemAlarmStream", config.boostSystemAlarmStream)
      .putBoolean("vibrationEnabled", config.vibrationEnabled)
      .putBoolean("continuousVibration", config.continuousVibration)
      .putInt("defaultSnoozeMinutes", config.defaultSnoozeMinutes)
      .putString("sunniQuranReciterId", config.sunniQuranReciterId)
      .putString("shiaQuranReciterId", config.shiaQuranReciterId)
      .putString("sunniTranslationKey", config.sunniTranslationKey)
      .putString("shiaTranslationKey", config.shiaTranslationKey)
      .putInt("lastReadSurahNumber", config.lastReadSurahNumber.coerceIn(1, 114))
      .putInt("lastReadVerseNumber", config.lastReadVerseNumber.coerceAtLeast(1))
      .putString("lastReadSurahNameEn", config.lastReadSurahNameEn)
      .putString("lastReadSurahNameAr", config.lastReadSurahNameAr)
      .putInt("lastReadTotalVerses", config.lastReadTotalVerses.coerceAtLeast(1))
      .putString("lastReadTradition", config.lastReadTradition)
      .putLong("lastReadTimestamp", config.lastReadTimestamp)
      .putString("appInterfaceLanguageCode", config.appInterfaceLanguageCode.ifBlank { "en" })
      .putBoolean("hasCompletedLanguageOnboarding", config.hasCompletedLanguageOnboarding)
      .putInt("sehriAdjustmentMinutes", config.sehriAdjustmentMinutes.coerceIn(-60, 60))
      .putInt("iftarAdjustmentMinutes", config.iftarAdjustmentMinutes.coerceIn(-60, 60))
      .putInt("selectedTimetableGregorianYear", config.selectedTimetableGregorianYear)
      .putInt("selectedTimetableHijriYear", config.selectedTimetableHijriYear)
      .putString("themeMode", config.themeMode.name)
      .apply()
  }

  fun saveSelectedMuezzin(context: Context, muezzinId: String): PrayerAndAlarmConfig {
    val updated = loadConfig(context).copy(selectedMuezzinId = muezzinId)
    saveConfig(context, updated)
    return updated
  }

  fun saveAlarmVolumePercent(context: Context, volumePercent: Int): PrayerAndAlarmConfig {
    val updated = loadConfig(context).copy(alarmVolumePercent = volumePercent.coerceIn(0, 100))
    saveConfig(context, updated)
    return updated
  }

  private fun parseAzanMode(raw: String?): AzanNotificationMode {
    return AzanNotificationMode.entries.find { it.name == raw } ?: AzanNotificationMode.FULL_AZAN
  }

  private fun parseThemeMode(raw: String?): AppThemeMode {
    return AppThemeMode.entries.find { it.name == raw } ?: AppThemeMode.LIGHT
  }
}
