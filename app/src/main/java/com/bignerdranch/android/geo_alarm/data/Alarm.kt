package com.bignerdranch.android.geo_alarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity

class Alarm {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var time: String? = null
    var millisId = 0
    var locationAddress: String? = null
}