package com.bangkit.naraspeak.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY id DESC")
    fun getHistory(): LiveData<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(historyEntity: HistoryEntity)

    @Delete
    suspend fun delete(historyEntity: HistoryEntity)
}