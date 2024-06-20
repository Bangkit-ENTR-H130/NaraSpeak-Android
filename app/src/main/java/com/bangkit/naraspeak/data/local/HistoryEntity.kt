package com.bangkit.naraspeak.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
class HistoryEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "audio")
    var audio: String? = null,

    @ColumnInfo(name = "isSaved")
    var isSaved: Boolean = false
)