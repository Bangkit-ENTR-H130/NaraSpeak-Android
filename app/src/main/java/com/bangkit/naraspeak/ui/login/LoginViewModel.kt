package com.bangkit.naraspeak.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.bangkit.naraspeak.data.api.response.LoginResponse
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.repository.AccountRepository
import com.bangkit.naraspeak.helper.Result
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel(private val accountRepository: AccountRepository): ViewModel() {
    fun login(username: String, password: String): LiveData<Result<LoginResponse>> = liveData {
    emit(Result.Loading)
        try {
            val client = accountRepository.postLogin(username, password)
            if (client.error == false) {
                emit(Result.Success(client))
            } else {
                emit(Result.Failed(client.message.toString()))
            }
        } catch (e: HttpException) {
            emit(Result.Failed(e.message.toString()))
            Log.e("PostLogin", e.message.toString())
        }
    }

//    fun getAuth() : LiveData<Result<FirebaseClient>> {
//        return accountRepository.auth().
//    }


}