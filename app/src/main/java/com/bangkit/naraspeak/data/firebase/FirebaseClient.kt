package com.bangkit.naraspeak.data.firebase

import android.util.Log
import android.view.View
import androidx.core.graphics.component2
import com.bangkit.naraspeak.data.model.DataModel
import com.bangkit.naraspeak.data.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.Transaction.Handler
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.childEvents
import com.google.firebase.database.snapshots
import com.google.gson.Gson
import java.util.Objects

class FirebaseClient {
    private val gson = Gson()
    private var currentUsername: String? = null
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    var mMatchmaker: DatabaseReference = database.getReference("video_call")
    var mGamesReference: DatabaseReference = database.getReference("games")

    private var isFound: Boolean = false
    private var isOpponentMatched: Boolean = false
    private var status: String = "waiting"

    fun login(username: String, statusListener: FirebaseStatusListener) {
        db.child("video_call").push().child(username).setValue("").addOnCompleteListener {
            currentUsername = username
            statusListener.onSuccess()

        }
    }

    fun sendData(dataModel: DataModel, statusListener: FirebaseStatusListener) {
        val dbVideoCall = db.child("video_call")
        dbVideoCall.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isOpponentMatched) {
                    var isMatchFound = false

                    snapshot.children.forEach {
                        if (it.childrenCount.toInt() == 2) {
                            db.child("video_call").child(it.key!!).child("video_call_data")
                                .setValue(gson.toJson(dataModel))
                            isMatchFound = true
                        } else if (it.childrenCount.toInt() < 2) {
                            db.child("video_call").child(it.key!!).child(currentUsername.toString())
                                .child("video_call_data")
                                .setValue(gson.toJson(dataModel))
                            isMatchFound = false
                        }
                    }

                    if (!isMatchFound) {
                        db.child("video_call").push().child(currentUsername.toString())
                            .child("video_call_data")
                            .setValue(gson.toJson(dataModel))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "sendData onCancelled: ${error.message}")
                statusListener.onError()
            }
        })
    }

    fun observeIncomingData(newEventListener: NewEventListener) {
        currentUsername?.let {
            db.child("video_call").orderByChild(it).addChildEventListener(object :
                ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val data = snapshot.child("video_call_data").getValue(String::class.java)
                    if (data != null) {
                        val dataModel = gson.fromJson(data, DataModel::class.java)
                        newEventListener.onNewEvent(dataModel)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val data = snapshot.child("video_call_data").getValue(String::class.java)
                    if (data != null) {
                        val dataModel = gson.fromJson(data, DataModel::class.java)
                        newEventListener.onNewEvent(dataModel)
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("IncomingData", "onCancelled: ${error.message}, ${error.details}")
                }
            })
        }
    }

    fun updateDisplayName(displayName: String) {
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
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
        currentUser?.reload()

    }

    fun postAuthToDatabase(username: String, userModel: UserModel) {
        try {
            db.child("user_data").child(username).setValue(userModel).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("PostAuth", "PostAuth to database:  ${it.isSuccessful} ${it.result}")
                } else {
                    Log.d("PostAuth", "PostAuth to database:  ${it.isSuccessful} ${it.exception}")
                }
            }
        } catch (e: Exception) {
            Log.d("PostAuth", "PostAuth to database:  ${e.message}")
        }

    }

    fun updateAuthData(
        username: String,
        updateDataListener: UpdateDataListener
    ) {
        db.child("user_data").child(username).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val data = Objects.requireNonNull(snapshot.value).toString()
                    val user = gson.fromJson(data, UserModel::class.java)
                    updateDataListener.onUpdate(user)
                    Log.d("UpdateAuthData", "onDataChange: $user")
                } catch (e: Exception) {
                    Log.e("UpdateAuthData", "onDataChange: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


//    fun findMatch(dataModel: DataModel, statusListener: FirebaseStatusListener) {
//        db.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val matchmaker = snapshot.value.toString()
//                Log.d(TAG, "matchmaker: $matchmaker")
//
//                if (matchmaker == NOT_FOUND) {
////                    findMatchFirstArriver()
//                    val dbReference = mGamesReference.push()
//                    dbReference.push().setValue(gson.toJson(dataModel))
//                    val matchmakers = dbReference.key
//
//                    mMatchmaker.runTransaction(object : Handler {
//                        override fun doTransaction(currentData: MutableData): Transaction.Result {
//                            if (currentData.value == NOT_FOUND) {
//                                currentData.value = matchmakers
//                                Log.d(TAG, "${currentData.value} + ${currentData.key}")
//                                return Transaction.success(currentData)
//                            }
//                            return Transaction.abort()
//                        }
//
//                        override fun onComplete(
//                            error: DatabaseError?,
//                            committed: Boolean,
//                            currentData: DataSnapshot?
//                        ) {
//                            Log.d(TAG, "onComplete: $error")
//                            if (!committed) {
//                                dbReference.removeValue()
//                            }
//                        }
//
//                    })
//
//                } else {
////                    findMatchSecondArriver(matchmaker)
//                    mMatchmaker.runTransaction(object : Handler {
//                        override fun doTransaction(currentData: MutableData): Transaction.Result {
//                            if (currentData.value != matchmaker) {
//                                currentData.value = NOT_FOUND
//                                return Transaction.success(currentData)
//                            } else {
//                                return Transaction.abort()
//                            }
//                        }
//
//                        override fun onComplete(
//                            error: DatabaseError?,
//                            committed: Boolean,
//                            currentData: DataSnapshot?
//                        ) {
//                            if (committed) {
//                                val gameReference = mGamesReference.child(matchmaker.toString())
//                                gameReference.push().setValue(gson.toJson(dataModel))
//                                mMatchmaker.setValue(NOT_FOUND)
//                            }
//                        }
//
//                    })
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//
//        })
//    }

    fun findMatchFirstArriver() {
        val dbReference = mGamesReference.push()
//        dbReference.push().setValue(gson.toJson())
        val matchmaker = dbReference.key

        mMatchmaker.runTransaction(object : Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.value == NOT_FOUND) {
                    currentData.value = matchmaker
                    Log.d(TAG, "${currentData.value} + ${currentData.key}")
                    return Transaction.success(currentData)
                }
                return Transaction.abort()
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                Log.d(TAG, "onComplete: $error")
                if (!committed) {
                    dbReference.removeValue()
                }
            }

        })

    }

    fun findMatchSecondArriver(matchmaker: String) {
        mMatchmaker.runTransaction(object : Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.value != matchmaker) {
                    currentData.value = NOT_FOUND
                    return Transaction.success(currentData)
                } else {
                    return Transaction.abort()
                }
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed) {
                    val gameReference = mGamesReference.child(matchmaker)
                    gameReference.push().setValue("player1")
                    mMatchmaker.setValue(NOT_FOUND)
                }
            }

        })

    }

    interface FirebaseStatusListener {
        fun onError()
        fun onSuccess()
    }

    interface NewEventListener {
        fun onNewEvent(dataModel: DataModel)
    }

    interface UpdateDataListener {
        fun onUpdate(userModel: UserModel)
    }

    companion object {
        private val TAG = FirebaseClient::class.java.simpleName
        private const val NOT_FOUND = "none"

    }
}
