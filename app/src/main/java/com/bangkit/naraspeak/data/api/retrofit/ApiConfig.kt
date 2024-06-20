package com.bangkit.naraspeak.data.api.retrofit

import com.google.firebase.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {

    fun getApiService(): ApiService {
        val loggingInterceptor = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        } else {
            //change none
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        }


        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()



        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(com.bangkit.naraspeak.BuildConfig.URL_GRAMMAR)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}