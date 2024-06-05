package com.belajar.naraspeak.data.repository

import com.belajar.naraspeak.data.webrtc.FirebaseClient

class VideoCallRepository(
    private val firebaseClient: FirebaseClient
) {

    private var currentUsername: String? = null

    fun login(username: String, statusListener: FirebaseClient.FirebaseStatusListener) {
        firebaseClient.login(username, object : FirebaseClient.FirebaseStatusListener  {
            override fun onError() {

            }

            override fun onSuccess() {
                statusListener.onSuccess()
                currentUsername = username
            }


        })
    }


    companion object {
        private var instance: VideoCallRepository? = null
        fun getInstance(
            firebaseClient: FirebaseClient
        ): VideoCallRepository =
            instance ?: synchronized(this) {
                instance ?: VideoCallRepository(firebaseClient)
            }.also { instance = it }
    }

}