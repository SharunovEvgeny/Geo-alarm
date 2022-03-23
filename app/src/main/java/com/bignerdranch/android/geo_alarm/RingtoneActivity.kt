package com.bignerdranch.android.geo_alarm

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bignerdranch.android.geo_alarm.databinding.ActivityRingtoneBinding
import com.bignerdranch.android.geo_alarm.network.RingtoneDao
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*

class RingtoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRingtoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRingtoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 23)

        binding.button1.setOnClickListener {
            download(1)
        }
        binding.button2.setOnClickListener {
            download(2)
        }
        binding.button3.setOnClickListener {
            download(3)
        }
    }

    private fun download(id: Int) {
        Toast.makeText(this, "Пожалуйста, подождите", Toast.LENGTH_SHORT).show()
        val retrofit = Retrofit.Builder().baseUrl(RingtoneDao.BASE_URL).build()
        val creator = retrofit.create(RingtoneDao::class.java)
        lateinit var response: Call<ResponseBody>
        when (id) {
            1 -> response = creator.downloadFirst()
            2 -> response = creator.downloadSecond()
            3 -> response = creator.downloadThird()
        }

        response
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    val success = response.body()?.let { writeToStorage(it, id) }
                    if (success == true) {
                        Toast.makeText(this@RingtoneActivity, "ok", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RingtoneActivity, "fail when save", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@RingtoneActivity, "fail", Toast.LENGTH_SHORT).show()
                }

            })
    }

    fun writeToStorage(body: ResponseBody, id: Int): Boolean {
        try {
            val file = File(Environment.getExternalStorageDirectory().path.plus("/Music"), "track".plus(id).plus(".mp3"))
            lateinit var inStream: InputStream
            lateinit var outStream: OutputStream
            try {
                val fileReader = ByteArray(4096)
                val fileSize: Long = body.contentLength()
                var fileSizeDownload: Long = 0

                inStream = body.byteStream()
                outStream = FileOutputStream(file)

                while (true) {
                    val read: Int = inStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outStream.write(fileReader, 0, read)
                    fileSizeDownload += read
                    Log.d("MYTAG", "$fileSizeDownload of $fileSize")
                }
                outStream.flush()
                putToShared(Environment.getExternalStorageDirectory().path.plus("/Music").plus("/").plus("track").plus(id).plus(".mp3"))
                return true
            } catch (ex: IOException) {
                return false
            } finally {
                if (inStream != null) {
                    inStream.close()
                }
                if (outStream != null) {
                    outStream.close()
                }
            }
        } catch (ex: IOException) {
            return false
        }
    }

    private fun putToShared(path: String) {
        val sharedPreference =  getSharedPreferences("NAME_PATH", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putString("NAME",path)
        editor.apply()
    }
}