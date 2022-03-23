package com.bignerdranch.android.geo_alarm

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.geo_alarm.Maps.MapsActivity
import com.bignerdranch.android.geo_alarm.data.Alarm
import com.bignerdranch.android.geo_alarm.data.AlarmDAO
import com.bignerdranch.android.geo_alarm.data.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var setAlarmButton: FloatingActionButton
    private lateinit var setRingtoneButton: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setAlarmButton = findViewById(R.id.alarm_button)
        setRingtoneButton = findViewById(R.id.ringtone_button)


        setAlarmButton.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        setRingtoneButton.setOnClickListener {
            val intent = Intent(this, RingtoneActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onRestart() {
        super.onRestart()
        recyclerView.removeAllViews()
    }

    override fun onStart() {
        super.onStart()
        CoroutineScope(Dispatchers.Main).launch {
            recyclerView = findViewById(R.id.recyclerView)//create adapter
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)

            recyclerView.adapter = CustomRecyclerAdapter(loadList())
        }
    }


    suspend fun loadList(): MutableList<Alarm> {
        val data: MutableList<Alarm>
        val db: AppDatabase = AppDatabase.getInstance(applicationContext)
        val alarmDAO: AlarmDAO? = db.alarmDao()
        return if (alarmDAO != null) {
            data = alarmDAO.getAll()
            data
        } else{
            data = arrayListOf()
            data
        }
    }


}