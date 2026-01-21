package com.example.pilotlog.data

import androidx.room.Embedded
import androidx.room.Relation

data class FlightWithAircraft(
    @Embedded val flight: Flight,
    @Relation(
        parentColumn = "aircraftId",
        entityColumn = "id"
    )
    val aircraft: Aircraft?
)
