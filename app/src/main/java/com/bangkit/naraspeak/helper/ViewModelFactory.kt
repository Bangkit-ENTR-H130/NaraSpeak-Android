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
import com.bangkit.naraspeak.ui.videocall.VideoCallViewModel

class ViewModelFactory(
    private val accountRepository: AccountRepository? = null,
    private val videoCallRepository: VideoCallRepository? = null,
    private val historyRepository: HistoryRepository? = null
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return accountRepository?.let { LoginViewModel(it) } as T
        }
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return accountRepository?.let { RegisterViewModel(it) } as T
        }
        if (modelClass.isAssignableFrom(DataFillViewModel::class.java)) {
            return accountRepository?.let { DataFillViewModel(it) } as T
        }
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            return accountRepository?.let { SettingViewModel(it) } as T
        }
        if (modelClass.isAssignableFrom(VideoCallViewModel::class.java)) {
            return historyRepository?.let { VideoCallViewModel(it) } as T
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

        fun getHistoryInstance(
            context: Context
        ): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(accountRepository = null,
                    videoCallRepository = null,
                    Injection.provideHistoryRepository(context))
            }.also { instance = it }
    }

}