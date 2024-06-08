package com.bangkit.naraspeak.data.webrtc

import android.util.Log
import com.bangkit.naraspeak.data.model.DataModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import java.util.Objects

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

    fun sendData(dataModel: DataModel, statusListener: FirebaseStatusListener) {
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (dataModel.target != null) {
                    if (snapshot.child("VideoCalls").child("users").child(dataModel.target).exists()) {
                        db.child("VideoCalls").child("users").child(dataModel.target).child("video_call_data")
                            .setValue(gson.toJson(dataModel))
                    } else {
                        statusListener.onError()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    fun observeIncomingData(newEventListener: NewEventListener) {
        currentUsername?.let {
            db.child("VideoCalls").child("users").child(it).child("video_call_data")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        try {
                            val data = Objects.requireNonNull(snapshot.value).toString()
                            val dataModel = gson.fromJson(data, DataModel::class.java)
                            newEventListener.onNewEvent(dataModel)
                        } catch (e: Exception) {
                            Log.e("IncomingData", "onDataChange: ${e.message}")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("IncomingData", "onCancelled: ${error.message}, ${error.details}")
                    }

                })
        }
    }

    interface FirebaseStatusListener {
        fun onError()
        fun onSuccess()
    }

    interface NewEventListener {
        fun onNewEvent(dataModel: DataModel)
    }
}