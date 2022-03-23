package com.bignerdranch.android.geo_alarm.data

import androidx.room.*


@Dao
interface AlarmDAO {
    @Query("SELECT * FROM alarm")
    suspend fun getAll(): MutableList<Alarm>

    @Query("SELECT * FROM alarm WHERE id = :id")
    fun getById(id: Long): Alarm?

    @Query("DELETE FROM alarm WHERE id = :id")
    fun deleteById(id: Long)

    @Query("DELETE FROM alarm WHERE millisId = :millisId")
    fun deleteByMillisId(millisId: Int)

    @Insert
    suspend fun insert(alarm: Alarm)

    @Update
    fun update(alarm: Alarm)

    @Delete
    fun delete(alarm: Alarm)

}