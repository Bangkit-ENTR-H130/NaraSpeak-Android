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

//        val authorization = Interceptor {
//            val request = it.request()
//            val requestHeader = request.newBuilder()
//                .addHeader("Authorization", "bb674687183144d99c8a336686e8a674")
//                .build()
//            it.proceed(requestHeader)
//        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
//            .addInterceptor(authorization)
            .build()



        val retrofit = Retrofit.Builder()
            .client(client)
            //ganti base url ke api buatan cc
            .baseUrl(com.bangkit.naraspeak.BuildConfig.URL_GRAMMAR)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}