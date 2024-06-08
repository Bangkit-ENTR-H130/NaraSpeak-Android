package com.bangkit.naraspeak.di

import com.bangkit.naraspeak.data.api.retrofit.ApiConfig
import com.bangkit.naraspeak.data.repository.AccountRepository
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.data.webrtc.FirebaseClient

object Injection {
    fun provideAccountRepository(): AccountRepository {
        val apiConfig = ApiConfig.getApiService()
        return AccountRepository.getInstance(apiConfig)
    }

    fun provideVideoCallRepository(): VideoCallRepository {
        //buat instance firebase client
        val firebaseClient = FirebaseClient()

       return VideoCallRepository.getInstance(firebaseClient)
    }
}