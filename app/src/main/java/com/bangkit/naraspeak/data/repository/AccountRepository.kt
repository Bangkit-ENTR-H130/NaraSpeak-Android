package com.bangkit.naraspeak.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.bangkit.naraspeak.data.api.retrofit.ApiService
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.model.UserModel
import com.bangkit.naraspeak.helper.Result
import com.bangkit.naraspeak.helper.UserResult
import com.google.firebase.database.ValueEventListener

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

    fun postAuthToDatabase(username: String, userModel: UserModel) {
        firebaseClient.postAuthToDatabase(username, userModel)
    }

    fun updateAuthData(username: String, newDataListener: FirebaseClient.UpdateDataListener) {
        firebaseClient.updateAuthData(username, object : FirebaseClient.UpdateDataListener {
            override fun onUpdate(userModel: UserModel) {
                newDataListener.onUpdate(userModel)
            }

        })
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