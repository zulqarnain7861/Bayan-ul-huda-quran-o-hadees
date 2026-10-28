package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.PrayerScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerScheduleDao {
  @Query("SELECT * FROM prayer_schedules WHERE dateLocationKey = :key LIMIT 1")
  suspend fun getScheduleByKey(key: String): PrayerScheduleEntity?

  @Query("SELECT * FROM prayer_schedules WHERE dateIso = :dateIso AND locationName = :locationName LIMIT 1")
  suspend fun getScheduleForDateAndLocation(dateIso: String, locationName: String): PrayerScheduleEntity?

  @Query("SELECT * FROM prayer_schedules ORDER BY lastUpdatedEpochMillis DESC LIMIT 1")
  fun observeLatestSchedule(): Flow<PrayerScheduleEntity?>

  @Query("SELECT * FROM prayer_schedules ORDER BY lastUpdatedEpochMillis DESC LIMIT 1")
  suspend fun getLatestScheduleSnapshot(): PrayerScheduleEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSchedule(schedule: PrayerScheduleEntity)

  @Query("SELECT COUNT(*) FROM prayer_schedules")
  suspend fun getScheduleCount(): Int
}
