package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.HadithCollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithCollectionDao {
  @Query("SELECT * FROM hadith_collections ORDER BY collectionKey ASC")
  fun getAllCollections(): Flow<List<HadithCollectionEntity>>

  @Query("SELECT * FROM hadith_collections")
  suspend fun getAllCollectionsSnapshot(): List<HadithCollectionEntity>

  @Query("SELECT * FROM hadith_collections WHERE collectionKey = :key LIMIT 1")
  suspend fun getCollectionByKey(key: String): HadithCollectionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCollections(collections: List<HadithCollectionEntity>)

  @Query("UPDATE hadith_collections SET locallyCachedCount = :cachedCount, lastSyncedTimestamp = :timestamp WHERE collectionKey = :key")
  suspend fun updateCollectionSyncStats(key: String, cachedCount: Int, timestamp: Long = System.currentTimeMillis())

  @Query("SELECT COUNT(*) FROM hadith_collections")
  suspend fun getCollectionCount(): Int
}
