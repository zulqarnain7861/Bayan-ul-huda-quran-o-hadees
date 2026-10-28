package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entities.ContentLicenceEntity
import com.example.data.local.entities.DownloadItemEntity
import com.example.data.local.entities.DownloadPackageManifestEntity
import com.example.data.local.entities.IncorrectReferenceReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadManifestDao {

  // --- Package Manifests ---
  @Query("SELECT * FROM download_package_manifests ORDER BY updatedAt DESC")
  fun getAllPackageManifestsFlow(): Flow<List<DownloadPackageManifestEntity>>

  @Query("SELECT * FROM download_package_manifests ORDER BY updatedAt DESC")
  suspend fun getAllPackageManifestsSnapshot(): List<DownloadPackageManifestEntity>

  @Query("SELECT * FROM download_package_manifests WHERE category = :category ORDER BY title ASC")
  fun getPackagesByCategoryFlow(category: String): Flow<List<DownloadPackageManifestEntity>>

  @Query("SELECT * FROM download_package_manifests WHERE packageId = :packageId LIMIT 1")
  suspend fun getPackageById(packageId: String): DownloadPackageManifestEntity?

  @Query("SELECT * FROM download_package_manifests WHERE packageId = :packageId LIMIT 1")
  fun getPackageByIdFlow(packageId: String): Flow<DownloadPackageManifestEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPackageManifest(manifest: DownloadPackageManifestEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPackageManifests(manifests: List<DownloadPackageManifestEntity>)

  @Query("DELETE FROM download_package_manifests WHERE packageId = :packageId")
  suspend fun deletePackageManifest(packageId: String)

  // --- Manifest Items ---
  @Query("SELECT * FROM download_manifest_items WHERE packageId = :packageId ORDER BY itemId ASC")
  suspend fun getItemsForPackage(packageId: String): List<DownloadItemEntity>

  @Query("SELECT * FROM download_manifest_items WHERE packageId = :packageId ORDER BY itemId ASC")
  fun getItemsForPackageFlow(packageId: String): Flow<List<DownloadItemEntity>>

  @Query("SELECT * FROM download_manifest_items WHERE resourceIdentifier = :resourceIdentifier AND status = 'COMPLETE' LIMIT 1")
  suspend fun getCompletedItemByResourceId(resourceIdentifier: String): DownloadItemEntity?

  @Query("SELECT * FROM download_manifest_items WHERE resourceIdentifier LIKE :prefix || '%' AND status = 'COMPLETE'")
  suspend fun getCompletedItemsByResourcePrefix(prefix: String): List<DownloadItemEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertDownloadItem(item: DownloadItemEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertDownloadItems(items: List<DownloadItemEntity>)

  @Query("DELETE FROM download_manifest_items WHERE packageId = :packageId")
  suspend fun deleteItemsForPackage(packageId: String)

  @Transaction
  suspend fun atomicSavePackageAndItems(
    manifest: DownloadPackageManifestEntity,
    items: List<DownloadItemEntity>
  ) {
    upsertDownloadItems(items)
    upsertPackageManifest(manifest)
  }

  // --- Content Licences & Source Permissions ---
  @Query("SELECT * FROM content_licence_permissions ORDER BY provider ASC, title ASC")
  fun getAllLicencesFlow(): Flow<List<ContentLicenceEntity>>

  @Query("SELECT * FROM content_licence_permissions ORDER BY provider ASC, title ASC")
  suspend fun getAllLicencesSnapshot(): List<ContentLicenceEntity>

  @Query("SELECT * FROM content_licence_permissions WHERE resourceId = :resourceId LIMIT 1")
  suspend fun getLicenceById(resourceId: String): ContentLicenceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertLicences(licences: List<ContentLicenceEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertLicence(licence: ContentLicenceEntity)

  // --- Priority 12: Incorrect Reference Reports ---
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertIncorrectReferenceReport(report: IncorrectReferenceReportEntity): Long

  @Query("SELECT * FROM incorrect_reference_reports ORDER BY createdAt DESC")
  fun getAllIncorrectReferenceReportsFlow(): Flow<List<IncorrectReferenceReportEntity>>

  @Query("SELECT * FROM incorrect_reference_reports ORDER BY createdAt DESC")
  suspend fun getAllIncorrectReferenceReportsSnapshot(): List<IncorrectReferenceReportEntity>
}
