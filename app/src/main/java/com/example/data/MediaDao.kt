package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY timestamp DESC")
    fun getAllMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getMediaById(id: Long): Flow<MediaEntity?>

    @Query("SELECT * FROM media_items WHERE cloudSyncStatus = 'PENDING' ORDER BY timestamp DESC")
    fun getPendingSyncMedia(): Flow<List<MediaEntity>>

    @Query("SELECT COUNT(*) FROM media_items WHERE cloudSyncStatus = 'SYNCED'")
    fun getSyncedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM media_items")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaEntity): Long

    @Update
    suspend fun updateMedia(item: MediaEntity)

    @Delete
    suspend fun deleteMedia(item: MediaEntity)

    @Query("UPDATE media_items SET cloudSyncStatus = :status, cloudSyncTimestamp = :timestamp WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: String, timestamp: Long)

    @Query("UPDATE media_items SET cloudSyncStatus = 'SYNCED', cloudSyncTimestamp = :timestamp WHERE cloudSyncStatus != 'SYNCED'")
    suspend fun markAllAsSynced(timestamp: Long)
}
