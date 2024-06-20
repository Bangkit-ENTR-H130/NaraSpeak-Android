package com.bangkit.naraspeak.helper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.bangkit.naraspeak.data.repository.CommonRepository
import com.bangkit.naraspeak.di.Injection
import com.bangkit.naraspeak.ui.datafill.DataFillViewModel
import com.bangkit.naraspeak.ui.homepage.setting.SettingViewModel
import com.bangkit.naraspeak.ui.login.LoginViewModel
import com.bangkit.naraspeak.ui.register.RegisterViewModel
import com.bangkit.naraspeak.ui.result.CompleteSessionViewModel

class CommonViewModelFactory(
    private val commonRepository: CommonRepository,
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(commonRepository) as T
        }
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(commonRepository) as T
        }
        if (modelClass.isAssignableFrom(DataFillViewModel::class.java)) {
            return DataFillViewModel(commonRepository) as T
        }
        if (modelClass.isAssignableFrom(SettingViewModel::class.java)) {
            return SettingViewModel(commonRepository) as T
        }
        if (modelClass.isAssignableFrom(CompleteSessionViewModel::class.java)) {
            return CompleteSessionViewModel(commonRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }


    companion object {
        private var instance: CommonViewModelFactory? = null

        fun getInstance(
        ): CommonViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: CommonViewModelFactory(Injection.provideAccountRepository())
            }.also { instance = it }


    }
}