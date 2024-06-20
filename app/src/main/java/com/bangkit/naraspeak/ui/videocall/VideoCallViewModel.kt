package com.bangkit.naraspeak.ui.videocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.naraspeak.data.local.HistoryEntity
import com.bangkit.naraspeak.data.repository.HistoryRepository
import kotlinx.coroutines.launch

class VideoCallViewModel(private val historyRepository: HistoryRepository): ViewModel() {
    fun getHistory() = historyRepository.getHistory()

    fun delete(history: HistoryEntity) {
        viewModelScope.launch {
            historyRepository.delete(history)
        }
    }

    fun insert(history: HistoryEntity) {
        viewModelScope.launch {
            historyRepository.insert(history)
        }
    }

}