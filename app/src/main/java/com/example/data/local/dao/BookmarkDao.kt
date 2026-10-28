package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
  @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
  fun getAllBookmarks(): Flow<List<BookmarkEntity>>

  @Query("SELECT * FROM bookmarks WHERE itemType = :type ORDER BY timestamp DESC")
  fun getBookmarksByType(type: String): Flow<List<BookmarkEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE itemKey = :itemKey)")
  fun isBookmarked(itemKey: String): Flow<Boolean>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmark(bookmark: BookmarkEntity)

  @Query("DELETE FROM bookmarks WHERE itemKey = :itemKey")
  suspend fun deleteBookmarkByKey(itemKey: String)
}
