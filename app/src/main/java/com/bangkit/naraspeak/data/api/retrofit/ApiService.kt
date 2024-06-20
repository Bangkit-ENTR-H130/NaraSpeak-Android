package com.bangkit.naraspeak.data.api.retrofit

import com.bangkit.naraspeak.data.api.response.GrammarResponse
import com.bangkit.naraspeak.data.api.response.LoginResponse
import com.bangkit.naraspeak.data.api.response.RegisterResponse
import com.bangkit.naraspeak.data.api.response.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ) : LoginResponse

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ) : RegisterResponse


    @Multipart
    @POST("upload")
    suspend fun upload(
        @Part file: MultipartBody.Part,
    ): UploadResponse

    @POST("/predicts")
    suspend fun postPredictGrammar(
        @Body text: RequestBody
    ): GrammarResponse
}