package com.bignerdranch.android.geo_alarm.network
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
interface RingtoneDao {
    companion object{
        val BASE_URL = "https://muzati.net/"
    }
    @GET("/music/0-0-1-16353-20")
    fun downloadFirst(): Call<ResponseBody>
    @GET("/music/0-0-1-9459-20")
    fun downloadSecond(): Call<ResponseBody>
    @GET("/music/0-0-1-8941-20")
    fun downloadThird(): Call<ResponseBody>
}