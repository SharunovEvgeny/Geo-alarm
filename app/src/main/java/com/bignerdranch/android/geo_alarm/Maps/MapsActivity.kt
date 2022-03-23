package com.bignerdranch.android.geo_alarm.Maps

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bignerdranch.android.geo_alarm.MainActivity
import com.bignerdranch.android.geo_alarm.R
import com.bignerdranch.android.geo_alarm.data.Alarm
import com.bignerdranch.android.geo_alarm.data.AlarmDAO
import com.bignerdranch.android.geo_alarm.AlarmService
import com.bignerdranch.android.geo_alarm.data.AppDatabase
import com.bignerdranch.android.geo_alarm.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val zoomSize = 17.0f

    private val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10001
    private var locationPermissionGranted = false

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var pointAddress: String

    private var coordinates = LatLng(55.751244, 37.618423) //Moscow

    private lateinit var pointCoordinates: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {//manipulate with map here
        mMap = googleMap
        setCurrentLocation()
        mMap.setOnMapClickListener {
            createDialog(it)
        }
    }

    private fun setCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            Toast.makeText(
                this,
                "Пожалуйста, установите разрешение и перезагрузите карту",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val locationResult = fusedLocationProviderClient.lastLocation

        locationResult.addOnCompleteListener(this) { task ->
            setMapsSettings(task)
        }
    }

    private fun setMapsSettings(task: Task<Location>) {
        if (task.isSuccessful) {
            val lastKnownLocation = task.result
            if (lastKnownLocation != null) {
                coordinates =
                    LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, zoomSize))

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
    }

    private fun createDialog(it: LatLng) {
        pointCoordinates = it
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(it.latitude, it.longitude, 1)
        pointAddress = addresses[0].getAddressLine(0)
        val mapsDialogFragment = MapsDialogFragment(pointAddress)

        val manager = supportFragmentManager
        mapsDialogFragment.show(manager, "mapDialog")
    }

    fun onPositiveDialogButton(boolList: MutableList<Boolean>) {
        val materialTimePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Выберите время для будильника")
                .build()//timepicker builder

        materialTimePicker.show(supportFragmentManager, "tag_picker")
        materialTimePicker.addOnPositiveButtonClickListener {
            var hasTrue = false
            val calendar: Calendar = Calendar.getInstance()

            for (bool in boolList) {
                if (bool) hasTrue = true
            }
            if (hasTrue) {
                for (i in 2..7) {
                    if (boolList[i - 2]) {
                        calendar.set(Calendar.DAY_OF_WEEK, i)
                        calendar.set(Calendar.MILLISECOND, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MINUTE, materialTimePicker.minute)
                        calendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
                        setAlarmTime(calendar)
                    }
                }
                if (boolList[6]) {
                    calendar.set(Calendar.DAY_OF_WEEK, 1)
                    calendar.set(Calendar.MILLISECOND, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MINUTE, materialTimePicker.minute)
                    calendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
                    setAlarmTime(calendar)
                }
            } else {
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MINUTE, materialTimePicker.minute)
                calendar.set(Calendar.HOUR_OF_DAY, materialTimePicker.hour)
                setAlarmTime(calendar)
            }
        }
    }

    private fun setAlarmTime(calendar: Calendar) {
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmClockInfo: AlarmManager.AlarmClockInfo = AlarmManager.AlarmClockInfo(
            calendar.timeInMillis, getAlarmInfoPendingInfo()
        )

        alarmManager.setAlarmClock(alarmClockInfo, getAlarmActionPendingInfo(calendar.timeInMillis))
    }

    private fun getAlarmInfoPendingInfo(): PendingIntent {
        val alarmInfoIntent = Intent(this, MainActivity::class.java)
        alarmInfoIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK
        val _id = System.currentTimeMillis().toInt()
        return PendingIntent.getActivity(
            this,
            _id,
            alarmInfoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getAlarmActionPendingInfo(time: Long): PendingIntent {
        val intent = Intent(this, AlarmService::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("coordinate_latitude", pointCoordinates.latitude)
        intent.putExtra("coordinate_longitude", pointCoordinates.longitude)
        val _id = System.currentTimeMillis().toInt()
        CoroutineScope(Dispatchers.IO).launch {
            insertAlarm(time, _id, pointAddress)
        }
        return PendingIntent.getService(this, _id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private suspend fun insertAlarm(time: Long, _id: Int, pointAddress: String) {
        val db: AppDatabase = AppDatabase.getInstance(applicationContext)
        val alarmDAO: AlarmDAO? = db.alarmDao()
        val alarm = Alarm()
        alarm.time = sdf.format(time)
        alarm.millisId = _id
        alarm.locationAddress = pointAddress
        alarmDAO?.insert(alarm)
    }
}