package com.bangkit.naraspeak.di

import android.content.Context
import com.bangkit.naraspeak.data.api.retrofit.ApiConfig
import com.bangkit.naraspeak.data.repository.CommonRepository
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.local.HistoryDatabase
import com.bangkit.naraspeak.data.repository.HistoryRepository

object Injection {
    fun provideAccountRepository(): CommonRepository {
        val apiConfig = ApiConfig.getApiService()
        val firebaseClient = FirebaseClient()
        return CommonRepository.getInstance(apiConfig, firebaseClient)
    }

    fun provideVideoCallRepository(): VideoCallRepository {
        //buat instance firebase client
        val firebaseClient = FirebaseClient()

       return VideoCallRepository.getInstance(firebaseClient)
    }

    fun provideHistoryRepository(context: Context): HistoryRepository {
        val database = HistoryDatabase.getInstance(context)
        val dao = database.historyDao()

        return HistoryRepository.getInstance(dao)
    }
}