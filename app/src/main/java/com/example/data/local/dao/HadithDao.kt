package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.HadithEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HadithDao {
  @Query("SELECT * FROM hadiths WHERE collectionKey = :collectionKey ORDER BY hadithNumber ASC")
  fun getHadithsByCollection(collectionKey: String): Flow<List<HadithEntity>>

  @Query("SELECT * FROM hadiths WHERE collectionKey = :collectionKey ORDER BY hadithNumber ASC")
  suspend fun getHadithsByCollectionSnapshot(collectionKey: String): List<HadithEntity>

  @Query("SELECT * FROM hadiths WHERE collectionKey = :collectionKey AND bookNumber = :bookNumber ORDER BY hadithNumber ASC")
  fun getHadithsByBook(collectionKey: String, bookNumber: Int): Flow<List<HadithEntity>>

  @Query("SELECT * FROM hadiths WHERE collectionKey = :collectionKey AND hadithNumber = :hadithNumber LIMIT 1")
  suspend fun getHadith(collectionKey: String, hadithNumber: Int): HadithEntity?

  @Query("""
    SELECT * FROM hadiths 
    WHERE linkedQuranVerses LIKE '%' || :verseRef || '%'
    ORDER BY hadithNumber ASC
  """)
  suspend fun getHadithsForVerse(verseRef: String): List<HadithEntity>

  @Query("""
    SELECT h.* FROM hadiths h
    JOIN hadiths_fts fts ON h.rowid = fts.rowid
    WHERE hadiths_fts MATCH :query
    ORDER BY h.collectionKey ASC, h.hadithNumber ASC
    LIMIT 200
  """)
  fun searchHadiths(query: String): Flow<List<HadithEntity>>

  @Query("SELECT * FROM hadiths ORDER BY RANDOM() LIMIT 1")
  suspend fun getRandomHadith(): HadithEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHadiths(hadiths: List<HadithEntity>)

  @Query("SELECT COUNT(*) FROM hadiths")
  suspend fun getHadithCount(): Int

  @Query("SELECT * FROM hadiths WHERE collectionKey = :collectionKey AND hadithNumber = :hadithNumber LIMIT 1")
  suspend fun getHadithByNumber(collectionKey: String, hadithNumber: Int): HadithEntity?

  @Query("""
    SELECT h.* FROM hadiths h
    JOIN hadiths_fts fts ON h.rowid = fts.rowid
    WHERE hadiths_fts MATCH :query
    ORDER BY h.collectionKey ASC, h.hadithNumber ASC
    LIMIT 200
  """)
  suspend fun searchHadithsSnapshot(query: String): List<HadithEntity>

  @Query("SELECT COUNT(*) FROM hadiths WHERE collectionKey = :collectionKey")
  suspend fun getHadithCountForCollection(collectionKey: String): Int

  @Query("SELECT * FROM hadiths ORDER BY collectionKey ASC, hadithNumber ASC")
  suspend fun getAllHadithsSnapshot(): List<HadithEntity>
}
