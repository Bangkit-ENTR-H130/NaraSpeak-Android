package com.bangkit.naraspeak.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.bangkit.naraspeak.data.repository.AccountRepository
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.di.Injection
import com.bangkit.naraspeak.ui.login.LoginViewModel
import com.bangkit.naraspeak.ui.register.RegisterViewModel

class ViewModelFactory(
    private val accountRepository: AccountRepository? = null,
    private val videoCallRepository: VideoCallRepository? = null
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return accountRepository?.let { LoginViewModel(it) } as T
        }
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return accountRepository?.let { RegisterViewModel::class.java } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }


    companion object {
        private var instance: ViewModelFactory? = null

        //tambahkan context kalau sudah lengkap
        fun getInstance(

        ): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(Injection.provideAccountRepository())
            }.also { instance = it }
    }

}