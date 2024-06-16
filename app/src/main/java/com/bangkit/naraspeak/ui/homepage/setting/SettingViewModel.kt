package com.bangkit.naraspeak.ui.homepage.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.repository.AccountRepository
import kotlinx.coroutines.launch

class SettingViewModel(private val accountRepository: AccountRepository): ViewModel() {
    fun updateData(name: String, updateDataListener: FirebaseClient.UpdateDataListener) =
            accountRepository.updateAuthData(name, updateDataListener)
}