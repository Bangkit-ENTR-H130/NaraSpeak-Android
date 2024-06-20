package com.bangkit.naraspeak.ui.videocall

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.naraspeak.data.api.response.UploadResponse
import com.bangkit.naraspeak.data.local.HistoryEntity
import com.bangkit.naraspeak.data.repository.HistoryRepository
import com.bangkit.naraspeak.helper.Result
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

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