package com.bangkit.naraspeak.data.model

data class DataModel(
    var target: String? = null,
    val sender: String? = null,
    val data: String? = null,
    val dataModelType: DataModelType? = null,
    val groupTarget: ArrayList<String>? = ArrayList()
) {




}