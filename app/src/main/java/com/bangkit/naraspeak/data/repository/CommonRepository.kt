package com.bangkit.naraspeak.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.bangkit.naraspeak.data.api.response.GrammarResponse
import com.bangkit.naraspeak.data.api.retrofit.ApiService
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.model.UserModel
import com.bangkit.naraspeak.helper.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CommonRepository(
    private val apiService: ApiService,
    private val firebaseClient: FirebaseClient
) {

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


    fun postGrammarPrediction(text: RequestBody): LiveData<Result<GrammarResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.postPredictGrammar(text)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Failed(e.message.toString()))
            Log.e("PostGrammarPrediction", "${e.message}")
        }

    }




    companion object {
        private var instance: CommonRepository? = null
        fun getInstance(
            apiService: ApiService,
            firebaseClient: FirebaseClient
        ): CommonRepository =
            instance ?: synchronized(this) {
                instance ?: CommonRepository(apiService, firebaseClient)
            }.also { instance = it }
    }
}