package com.bangkit.naraspeak.data.model

data class UserModel(
    val uid: String? = null,
    var name: String? = null,
    var level: String? = null,
    var gender: String? = null
)

data class PhotoModel(
    val photoUrl: String? = null
)
