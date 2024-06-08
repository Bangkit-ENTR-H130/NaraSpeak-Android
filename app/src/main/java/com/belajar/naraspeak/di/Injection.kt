package com.belajar.naraspeak.di

import com.belajar.naraspeak.data.api.retrofit.ApiConfig
import com.belajar.naraspeak.data.repository.AccountRepository
import com.belajar.naraspeak.data.repository.VideoCallRepository
import com.belajar.naraspeak.data.webrtc.FirebaseClient

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