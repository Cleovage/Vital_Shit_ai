package com.example.vitaai.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VitaPermissionManager @Inject constructor() {
    val activityRecognitionPermissions = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)
    val bodySensorPermissions = arrayOf(Manifest.permission.BODY_SENSORS)
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    fun hasLocationPermission(context: Context): Boolean {
        return locationPermissions.any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
