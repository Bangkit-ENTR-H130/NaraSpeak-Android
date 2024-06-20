package com.bangkit.naraspeak.helper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.bangkit.naraspeak.data.repository.AccountRepository
import com.bangkit.naraspeak.data.repository.HistoryRepository
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.di.Injection
import com.bangkit.naraspeak.ui.datafill.DataFillViewModel
import com.bangkit.naraspeak.ui.homepage.setting.SettingViewModel
import com.bangkit.naraspeak.ui.login.LoginViewModel
import com.bangkit.naraspeak.ui.register.RegisterViewModel
import com.bangkit.naraspeak.ui.result.CompleteSessionViewModel
import com.bangkit.naraspeak.ui.videocall.VideoCallViewModel

class AccountViewModelFactory(
    private val accountRepository: AccountRepository,
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(accountRepository) as T
        }
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(accountRepository) as T
        }
        if (modelClass.isAssignableFrom(DataFillViewModel::class.java)) {
            return DataFillViewModel(accountRepository) as T
        }
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            return SettingViewModel(accountRepository) as T
        }
        if (modelClass.isAssignableFrom(CompleteSessionViewModel::class.java)) {
            return CompleteSessionViewModel(accountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }


    companion object {
        private var instance: AccountViewModelFactory? = null

        //tambahkan context kalau sudah lengkap
        fun getInstance(

        ): AccountViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: AccountViewModelFactory(Injection.provideAccountRepository())
            }.also { instance = it }


    }
}