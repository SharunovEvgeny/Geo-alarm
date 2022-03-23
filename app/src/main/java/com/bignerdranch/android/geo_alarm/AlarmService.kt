package com.bignerdranch.android.geo_alarm

import android.Manifest
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class AlarmService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val DISTANCE_IN_METERS = 100
    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { //get coordinates from intent
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val lat = intent!!.getDoubleExtra("coordinate_latitude", 1.0)
        val lng = intent.getDoubleExtra("coordinate_longitude", 1.0)
        getLastKnownLocation(lat, lng)

        stopSelf()
        return START_STICKY
    }

    private fun getLastKnownLocation(lat: Double, lng: Double) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    if (DISTANCE_IN_METERS > distFrom2LocationsInMeter(location.latitude, location.longitude, lat, lng)){
                        val alarmIntent = Intent(this, AlarmActivity::class.java)
                        alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(alarmIntent)
                    }
                }
                else {
                    Log.d(TAG, "getLastKnownLocation: ")
                }

            }

    }

    private fun distFrom2LocationsInMeter(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Float{
        val earthRadius = 3958.75
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist = earthRadius * c

        val meterConversion = 1609

        return java.lang.Float.valueOf((dist * meterConversion).toFloat())

    }

}