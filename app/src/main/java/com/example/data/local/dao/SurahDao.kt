package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.SurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {
  @Query("SELECT * FROM surahs ORDER BY number ASC")
  fun getAllSurahs(): Flow<List<SurahEntity>>

  @Query("SELECT * FROM surahs WHERE number = :surahNumber LIMIT 1")
  suspend fun getSurahByNumber(surahNumber: Int): SurahEntity?

  @Query("SELECT * FROM surahs WHERE nameEnglish LIKE '%' || :query || '%' OR nameArabic LIKE '%' || :query || '%' OR nameUrdu LIKE '%' || :query || '%'")
  fun searchSurahs(query: String): Flow<List<SurahEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSurahs(surahs: List<SurahEntity>)

  @Query("SELECT COUNT(*) FROM surahs")
  suspend fun getSurahCount(): Int
}
