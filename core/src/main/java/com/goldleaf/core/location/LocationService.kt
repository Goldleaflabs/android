// LocationService.kt
package com.goldleaf.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class LocationService(
    private val context: Context
) {

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Flow<Pair<Double, Double>> = callbackFlow {

        val timeoutJob = launch {
            delay(10_000)
            close()
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        )
            .setWaitForAccurateLocation(true)
            .setMaxUpdates(1)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                timeoutJob.cancel()

                result.lastLocation?.let {
                    trySend(it.latitude to it.longitude)
                }

                close()
            }
        }

        fusedClient.requestLocationUpdates(request, callback, context.mainLooper)

        awaitClose {
            timeoutJob.cancel()
            fusedClient.removeLocationUpdates(callback)
        }
    }
}
