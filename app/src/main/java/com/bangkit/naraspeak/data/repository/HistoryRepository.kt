package com.bangkit.naraspeak.data.repository

import androidx.lifecycle.LiveData
import com.bangkit.naraspeak.data.local.HistoryDao
import com.bangkit.naraspeak.data.local.HistoryEntity

class HistoryRepository(
    private val historyDao: HistoryDao
) {

    fun getHistory() =
        historyDao.getHistory()


    suspend fun insert(history: HistoryEntity) {
        historyDao.insert(history)
    }

    suspend fun delete(history: HistoryEntity) {
        historyDao.delete(history)
    }

    companion object {
        @Volatile
        private var instance: HistoryRepository? = null
        fun getInstance(historyDao: HistoryDao): HistoryRepository =
            instance ?: kotlin.synchronized(this) {
                instance ?: HistoryRepository(historyDao)

            }.also { instance = it }
    }
}