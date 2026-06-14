package com.example.vitaai.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.example.vitaai.data.VitaPermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: VitaPermissionManager
) {
    @SuppressLint("MissingPermission")
    fun locationFlow(): Flow<Location> = callbackFlow {
        if (!permissionManager.hasLocationPermission(context)) {
            close()
            return@callbackFlow
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasFine = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val provider = when {
            hasFine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            close()
            return@callbackFlow
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }

        manager.requestLocationUpdates(provider, 2_000L, 2f, listener, Looper.getMainLooper())
        awaitClose { manager.removeUpdates(listener) }
    }
}
