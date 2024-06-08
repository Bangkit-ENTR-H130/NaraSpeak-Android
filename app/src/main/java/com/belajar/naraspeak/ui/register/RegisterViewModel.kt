package com.belajar.naraspeak.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.belajar.naraspeak.data.api.response.RegisterResponse
import com.belajar.naraspeak.data.repository.AccountRepository
import com.belajar.naraspeak.helper.Result
import retrofit2.HttpException

class RegisterViewModel(private val accountRepository: AccountRepository): ViewModel() {

    fun postRegister(name: String, email: String, password: String): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        try {
            val client = accountRepository.postRegister(name, email, password)
            if (client.error == false) {
                emit(Result.Success(client))
            } else {
                emit(Result.Failed(client.message.toString()))
                Log.e("PostRegister", "Error: ${client.message.toString()}")
            }

        } catch (e: HttpException) {
            Log.e("PostRegister", "Error: ${e.message.toString()}")
            emit(Result.Failed(e.message.toString()))
        }
    }
}