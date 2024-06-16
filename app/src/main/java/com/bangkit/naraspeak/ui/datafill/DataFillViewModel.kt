package com.bangkit.naraspeak.ui.datafill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.naraspeak.data.model.UserModel
import com.bangkit.naraspeak.data.repository.AccountRepository
import kotlinx.coroutines.launch

class DataFillViewModel(private val accountRepository: AccountRepository) : ViewModel() {

    fun updateName(name: String) = accountRepository.updateName(name)

    fun postAuthToDatabase(username: String, userModel: UserModel) {
        viewModelScope.launch {
            accountRepository.postAuthToDatabase(username, userModel)
        }
    }

}