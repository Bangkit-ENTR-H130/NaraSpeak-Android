package com.bangkit.naraspeak.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bangkit.naraspeak.data.api.response.GrammarResponse
import com.bangkit.naraspeak.data.repository.CommonRepository
import com.bangkit.naraspeak.helper.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody

class CompleteSessionViewModel(private val commonRepository: CommonRepository): ViewModel() {

    fun postGrammarPrediction(text: RequestBody): LiveData<Result<GrammarResponse>> = commonRepository.postGrammarPrediction(text)

}