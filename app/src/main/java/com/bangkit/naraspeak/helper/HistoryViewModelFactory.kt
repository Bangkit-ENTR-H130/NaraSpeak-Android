package com.bangkit.naraspeak.helper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.bangkit.naraspeak.data.repository.HistoryRepository
import com.bangkit.naraspeak.di.Injection
import com.bangkit.naraspeak.ui.videocall.VideoCallViewModel

class HistoryViewModelFactory(
    private val historyRepository: HistoryRepository
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(VideoCallViewModel::class.java)) {
            return VideoCallViewModel(historyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {
        private var instance: HistoryViewModelFactory? = null
        fun getHistoryInstance(
            context: Context
        ): HistoryViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: HistoryViewModelFactory(
                    Injection.provideHistoryRepository(context))
            }.also { instance = it }
    }
    }
