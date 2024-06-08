package com.belajar.naraspeak.data.repository

import com.belajar.naraspeak.data.api.retrofit.ApiService
import com.belajar.naraspeak.data.webrtc.FirebaseClient
import com.belajar.naraspeak.di.Injection

class AccountRepository(
    private val apiService: ApiService
) {

    suspend fun postLogin(email: String, password: String) =
        apiService.login(email, password)

    suspend fun postRegister(name: String, email: String, password: String) =
        apiService.register(name, email, password)


    companion object {
        private var instance: AccountRepository? = null
        fun getInstance(
            apiService: ApiService
        ): AccountRepository =
            instance ?: synchronized(this) {
                instance ?: AccountRepository(apiService)
            }.also { instance = it }
    }
}