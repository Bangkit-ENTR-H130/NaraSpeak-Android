package com.bangkit.naraspeak.data.api.retrofit

import com.bangkit.naraspeak.data.api.response.GrammarResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("/predicts")
    suspend fun postPredictGrammar(
        @Body text: RequestBody
    ): GrammarResponse
}