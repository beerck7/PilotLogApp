package com.example.pilotlog.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationResult: (Location?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocationResult(location)
                } else {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                        .addOnSuccessListener { freshLocation: Location? ->
                            onLocationResult(freshLocation)
                        }
                        .addOnFailureListener {
                            onLocationResult(null)
                        }
                }
            }
            .addOnFailureListener {
                 fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
                    .addOnSuccessListener { freshLocation: Location? ->
                        onLocationResult(freshLocation)
                    }
                    .addOnFailureListener {
                        onLocationResult(null)
                    }
            }
    }
}
