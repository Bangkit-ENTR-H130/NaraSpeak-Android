package com.bangkit.naraspeak.data.firebase

import android.util.Log
import com.bangkit.naraspeak.data.model.DataModel
import com.bangkit.naraspeak.ui.datafill.DataFillActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
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
    private lateinit var auth: FirebaseAuth

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
                    if (snapshot.child("video_calls").child("users").child(dataModel.target).exists()) {
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
            db.child("video_calls").child("users").child(it).child("video_call_data")
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

    fun updateDisplayName(displayName: String){
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser!= null) {
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            currentUser.updateProfile(profileUpdate).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("UpdateName", "Name updated ${it.isSuccessful}")
                } else {
                    Log.d("UpdateName", "Name not updated: ${it.exception}")

                }
            }.addOnFailureListener {
                Log.d("UpdateName", "Failed to update Name: ${it.message}")
            }

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