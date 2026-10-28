package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.ImportStatus
import com.example.data.local.SourceRegistry
import com.example.data.local.VerificationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceRegistryDao {
  @Query("SELECT * FROM source_registry ORDER BY title ASC")
  fun getAllSources(): Flow<List<SourceRegistry>>

  @Query("SELECT * FROM source_registry ORDER BY title ASC")
  suspend fun getAllSourcesSnapshot(): List<SourceRegistry>

  @Query("SELECT * FROM source_registry WHERE sourceId = :sourceId LIMIT 1")
  fun getSourceById(sourceId: String): Flow<SourceRegistry?>

  @Query("SELECT * FROM source_registry WHERE sourceId = :sourceId LIMIT 1")
  suspend fun getSourceByIdSnapshot(sourceId: String): SourceRegistry?

  @Query("SELECT * FROM source_registry WHERE verificationStatus = :status ORDER BY title ASC")
  fun getSourcesByVerificationStatus(status: VerificationStatus): Flow<List<SourceRegistry>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSource(source: SourceRegistry)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSources(sources: List<SourceRegistry>)

  @Update
  suspend fun updateSource(source: SourceRegistry)

  @Query("""
    UPDATE source_registry 
    SET totalImportedRecords = :totalImported,
        lastSyncTime = :lastSyncTime,
        importStatus = :importStatus
    WHERE sourceId = :sourceId
  """)
  suspend fun updateSyncStats(
    sourceId: String,
    totalImported: Int,
    lastSyncTime: Long = System.currentTimeMillis(),
    importStatus: ImportStatus = ImportStatus.SUCCESS
  )

  @Query("UPDATE source_registry SET verificationStatus = :status, lastSyncTime = :timestamp WHERE sourceId = :sourceId")
  suspend fun updateVerificationStatus(
    sourceId: String,
    status: VerificationStatus,
    timestamp: Long = System.currentTimeMillis()
  )

  @Query("DELETE FROM source_registry WHERE sourceId = :sourceId")
  suspend fun deleteSource(sourceId: String)

  @Query("SELECT COUNT(*) FROM source_registry")
  suspend fun getSourceCount(): Int
}
