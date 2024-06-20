package com.bangkit.naraspeak.data.firebase

import android.util.Log
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
import kotlinx.coroutines.flow.firstOrNull
import java.util.Objects
import java.util.UUID

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
        db.child("login").push().child(username).setValue("").addOnCompleteListener {
            currentUsername = username
            statusListener.onSuccess()
        }
    }

    fun sendData(dataModel: DataModel, statusListener: FirebaseStatusListener) {
        db.child("video_call").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (!isOpponentMatched) {
                        var isMatchFound = false

//                    snapshot.children.forEach { roomSnapshot ->
//                        Log.d(TAG, "Checking room: ${roomSnapshot.key}, user count: ${roomSnapshot.childrenCount}")
//
//                        // Check if the room has exactly one user (available for matching)
//                        if (roomSnapshot.childrenCount == 1L) {
//                            val otherUser = roomSnapshot.children.firstOrNull { it.key != currentUsername }
//
//                            if (otherUser != null) {
//                                db.child("video_call").child(roomSnapshot.key!!)
//                                    .child(currentUsername!!)
//                                    .setValue(gson.toJson(dataModel))
//
//                                dataModel.target = otherUser.key
//                                isMatchFound = true
//                                statusListener.onSuccess()
//                            }
//                        }
//                    }
                        snapshot.children.firstOrNull { roomSnapshot ->
                            roomSnapshot.childrenCount == 1L

                        }?.let { availableRoom ->
                            val otherUser =
                                availableRoom.children.firstOrNull { it.key != currentUsername }

                            db.child("video_call").child(availableRoom.key!!)
                                .child(currentUsername!!)
                                .setValue(gson.toJson(dataModel)).addOnCompleteListener {
                                    dataModel.target = otherUser?.key
                                    isMatchFound = true
                                    statusListener.onSuccess()
                                }

//                            dataModel.target = otherUser?.key
//                            isMatchFound = true
//                            statusListener.onSuccess()

                        }

                        if (!isMatchFound) {
                            db.child("video_call").child(NEW_ROOM_KEY)
                                .child(currentUsername!!)
                                .setValue(gson.toJson(dataModel))
                            dataModel.target =
                                snapshot.children.firstOrNull { it.key != currentUsername }
                                    .toString()
                            statusListener.onSuccess()
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "sendData: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "sendData onCancelled: ${error.message}")
                statusListener.onError()
            }
        })
    }


    fun observeIncomingData(newEventListener: NewEventListener) {
        currentUsername?.let { username ->
            db.child("video_call").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    handleSnapshot(snapshot, username, newEventListener)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Handle if necessary, e.g., notify user that their opponent left
                    if (snapshot.key == username) {
                        Log.d("IncomingData", "The user has left the room: $username")
                    }
                    snapshot.child(NEW_ROOM_KEY).ref.removeValue()
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {
                    Log.e("IncomingData", "onCancelled: ${error.message}, ${error.details}")
                    db.child("video_call").removeValue()
                }
            })

        }
    }

    private fun handleSnapshot(
        snapshot: DataSnapshot,
        username: String,
        newEventListener: NewEventListener
    ) {
        if (snapshot.exists() && snapshot.childrenCount == 2L) {
            snapshot.children.forEach { userSnapshot ->
                if (userSnapshot.key != username) {
                    val json = Objects.requireNonNull(userSnapshot.value)
                        .toString() // Convert snapshot value to string
                    Log.d(TAG, "json $json")

                    try {
                        val dataModel = gson.fromJson(json, DataModel::class.java)
                        newEventListener.onNewEvent(dataModel)
                    } catch (e: Exception) {
                        Log.e(TAG, "error ${e.message}")
                    }
                }


            }
        }
    }

    fun loginGroup(username: String, statusListener: FirebaseStatusListener) {
        db.child("group_video_call").child(username).setValue("").addOnCompleteListener {
            currentUsername = username
            statusListener.onSuccess()
        }
    }


    fun sendDataVideoGroup(dataModel: DataModel, statusListener: FirebaseStatusListener) {
        db.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(dataModel.target.toString()).exists()) {
                        db.child("group_video_call").child(dataModel.groupTarget.toString()).setValue(gson.toJson(dataModel))

                    } else {
                        statusListener.onError()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    fun observeIncomingGroupCall(newEventListener: NewEventListener) {
        db.child(currentUsername.toString()).child("group_video_call").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val data = Objects.requireNonNull(snapshot.value).toString()
                    val json = gson.fromJson(data, DataModel::class.java)
                    newEventListener.onNewEvent(json)
                } catch (e: Exception) {
                    Log.e(TAG, "observeGroupCall: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
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
        private val NEW_ROOM_KEY = "room_${UUID.randomUUID()}"


    }
}
