package com.bignerdranch.android.geo_alarm

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class AlarmActivity: AppCompatActivity() {
    lateinit var rington: Ringtone
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        val ringtone = getSharedPreferences("NAME_PATH", Context.MODE_PRIVATE).getString("NAME", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString())
        var notificationUri: Uri = ringtone!!.toUri()
        rington = RingtoneManager.getRingtone(this, notificationUri)

        if (rington == null){
            notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            rington = RingtoneManager.getRingtone(this, notificationUri)
        }
        if (rington != null){
            rington.play()
        }
    }

    override fun onDestroy() {
        if (rington.isPlaying()){
            rington.stop()
        }
        super.onDestroy()

    }
}