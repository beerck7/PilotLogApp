package com.example.pilotlog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aircraft")
data class Aircraft(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val registration: String,
    val model: String,
    val type: String, // e.g., "GLD", "SEP"
    val imagePath: String? = null
)

