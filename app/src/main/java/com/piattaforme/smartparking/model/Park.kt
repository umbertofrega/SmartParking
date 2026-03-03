package com.piattaforme.smartparking.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class Park(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val latitude: Float,
    var longitude: Float,
    val note: String
)