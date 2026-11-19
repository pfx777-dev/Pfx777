package com.example.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CreationDao {
    @Query("SELECT * FROM creations ORDER BY createdAtMillis DESC")
    fun observeAllCreations(): Flow<List<CreationEntity>>

    @Query("SELECT * FROM creations WHERE id = :id LIMIT 1")
    suspend fun getCreationById(id: String): CreationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(entity: CreationEntity)

    @Query("UPDATE creations SET isSaved = :isSaved WHERE id = :id")
    suspend fun updateSavedState(id: String, isSaved: Boolean)

    @Query("UPDATE creations SET isDownloaded = 1, downloadedDisplayPath = :displayPath WHERE id = :id")
    suspend fun markCreationDownloaded(id: String, displayPath: String)

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteCreationById(id: String)

    @Query("DELETE FROM creations")
    suspend fun clearAllCreations()

    @Query("SELECT * FROM downloads ORDER BY downloadedAtMillis DESC")
    fun observeAllDownloads(): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(entity: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE fileName = :fileName LIMIT 1")
    suspend fun findDownloadByFileName(fileName: String): DownloadEntity?

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownloadById(id: String)
}
