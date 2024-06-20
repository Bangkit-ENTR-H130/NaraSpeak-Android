package com.bangkit.naraspeak.ui.homepage.setting

import androidx.lifecycle.ViewModel
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.repository.CommonRepository

class SettingViewModel(private val accountRepository: CommonRepository): ViewModel() {
    fun updateData(name: String, updateDataListener: FirebaseClient.UpdateDataListener) =
            accountRepository.updateAuthData(name, updateDataListener)
}