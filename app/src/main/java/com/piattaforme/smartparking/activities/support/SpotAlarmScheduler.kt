package com.piattaforme.smartparking.activities.support

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent

class SpotAlarmScheduler(private val context: Context) {

    @SuppressLint("ScheduleExactAlarm")
    fun setAlarm(time : Long){
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager


        val pendingIntent = PendingIntent.getBroadcast(
            context, 101,
            Intent(context, SpotNotificationReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)

    }
}