package com.bignerdranch.android.geo_alarm.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Alarm::class], version = 1)
abstract class AppDatabase : RoomDatabase() {//singleton class
    abstract fun alarmDao(): AlarmDAO?

    companion object{
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase{
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "db").build()
            }

            return INSTANCE as AppDatabase
        }
    }
}