package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "prayer_schedules",
  indices = [
    Index(value = ["dateIso", "locationName"])
  ]
)
data class PrayerScheduleEntity(
  @PrimaryKey val dateLocationKey: String, // e.g. "2026-10-21_Srinagar_1_1"
  val dateIso: String, // YYYY-MM-DD
  val gregorianReadable: String,
  val hijriReadable: String,
  val locationName: String,
  val districtOrRegion: String = "",
  val country: String = "",
  val latitude: Double,
  val longitude: Double,
  val timeZoneId: String,
  val calculationMethodId: Int,
  val calculationMethodName: String,
  val asrMadhhab: Int, // 0 = Shafi'i/Standard, 1 = Hanafi
  val highLatitudeRule: Int = 0,
  val fajrTime: String, // HH:mm (24h)
  val sunriseTime: String,
  val dhuhrTime: String,
  val asrTime: String,
  val maghribTime: String,
  val ishaTime: String,
  val tahajjudLastThirdStart: String,
  val sourceName: String, // "AlAdhan API + Astronomical Verification" or "Offline Astronomical Calculation"
  val lastUpdatedEpochMillis: Long = System.currentTimeMillis()
)
