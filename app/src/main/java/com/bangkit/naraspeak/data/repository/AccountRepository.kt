package com.bangkit.naraspeak.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.bangkit.naraspeak.data.api.retrofit.ApiService
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.helper.Result

class AccountRepository(
    private val apiService: ApiService,
    private val firebaseClient: FirebaseClient
) {

    suspend fun postLogin(email: String, password: String) =
        apiService.login(email, password)

    suspend fun postRegister(name: String, email: String, password: String) =
        apiService.register(name, email, password)

    fun updateName(displayName: String): LiveData<Result<Unit>>  = liveData {
        emit(Result.Loading)
        try {
            val client = firebaseClient.updateDisplayName(displayName)
            emit(Result.Success(client))
        } catch (e: Exception) {
            emit(Result.Failed(e.message.toString()))
            Log.e("UpdateName", "${e.message}")
        }

    }





    companion object {
        private var instance: AccountRepository? = null
        fun getInstance(
            apiService: ApiService,
            firebaseClient: FirebaseClient
        ): AccountRepository =
            instance ?: synchronized(this) {
                instance ?: AccountRepository(apiService, firebaseClient)
            }.also { instance = it }
    }
}