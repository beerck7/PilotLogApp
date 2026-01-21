package com.example.pilotlog.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "flight",
    foreignKeys = [
        ForeignKey(
            entity = Aircraft::class,
            parentColumns = ["id"],
            childColumns = ["aircraftId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["aircraftId"])]
)
data class Flight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Date,
    val aircraftId: Int,
    val departureCode: String,
    val arrivalCode: String,
    val durationMinutes: Int,
    val remarks: String?,
    val photoPath: String? = null,
    val launchMethod: String? = null,
    val releaseHeight: Int? = null,
    
    val gpsTrackPath: String? = null,
    val maxGForce: Float? = null,
    val avgGForce: Float? = null,
    val landingGForce: Float? = null
)
