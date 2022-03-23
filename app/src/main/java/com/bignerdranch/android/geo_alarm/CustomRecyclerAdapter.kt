package com.bignerdranch.android.geo_alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.geo_alarm.data.Alarm
import com.bignerdranch.android.geo_alarm.data.AlarmDAO
import com.bignerdranch.android.geo_alarm.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomRecyclerAdapter(private var alarms: MutableList<Alarm>) : RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var idTextView: TextView? = null
        var timeTextView: TextView? = null
        var addressTextView: TextView? = null
        var buttonDelete: ImageButton? = null

        init {
            idTextView = itemView.findViewById(R.id.textViewId)
            timeTextView = itemView.findViewById(R.id.textViewTime)
            addressTextView = itemView.findViewById(R.id.textViewAddress)
            buttonDelete = itemView.findViewById(R.id.deleteButton)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.recyclerview_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.idTextView?.text = alarms[position].millisId.toString()
        holder.timeTextView?.text = alarms[position].time
        holder.addressTextView?.text = alarms[position].locationAddress
        holder.buttonDelete?.setOnClickListener {
            cancelAlarmAction(holder)
            deleteFromList(position)

            CoroutineScope(Dispatchers.IO).launch {
                deleteFromDb(holder.idTextView!!.text.toString().toInt(), holder.buttonDelete!!)
            }
        }
    }

    private fun deleteFromDb(millisId: Int, buttonDelete: ImageButton) {
        val db: AppDatabase = AppDatabase.getInstance(buttonDelete.context)
        val alarmDAO: AlarmDAO? = db.alarmDao()
        alarmDAO?.deleteByMillisId(millisId)
    }

    private fun deleteFromList(position: Int) {
        alarms.removeAt(position)
        notifyDataSetChanged()
    }

    private fun cancelAlarmAction(holder: MyViewHolder) {
        val context = holder.itemView.context
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmService::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_NEW_TASK
        val cancelPendingIntent = PendingIntent.getService(
            context,
            holder.idTextView!!.text.toString().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(cancelPendingIntent)
    }

    override fun getItemCount() = alarms.size
}