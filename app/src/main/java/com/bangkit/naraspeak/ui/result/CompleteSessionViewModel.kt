package com.bangkit.naraspeak.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bangkit.naraspeak.data.api.response.GrammarResponse
import com.bangkit.naraspeak.data.api.response.UploadResponse
import com.bangkit.naraspeak.data.repository.AccountRepository
import com.bangkit.naraspeak.helper.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CompleteSessionViewModel(private val accountRepository: AccountRepository): ViewModel() {
    fun uploadAudio(audio: MultipartBody.Part): LiveData<Result<UploadResponse>> = accountRepository.uploadAudio(audio)

    fun postGrammarPrediction(text: RequestBody): LiveData<Result<GrammarResponse>> = accountRepository.postGrammarPrediction(text)

}