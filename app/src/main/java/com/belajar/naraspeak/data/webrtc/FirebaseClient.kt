package com.belajar.naraspeak.data.webrtc

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

class FirebaseClient {
    private val gson = Gson()
    private var currentUsername: String? = null
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun login(username: String, statusListener: FirebaseStatusListener) {
        db.child("VideoCalls").child("users").child(username).setValue("").addOnCompleteListener {
                currentUsername = username
                statusListener.onSuccess()

        }
    }

    interface FirebaseStatusListener {
        fun onError()
        fun onSuccess()
    }
}