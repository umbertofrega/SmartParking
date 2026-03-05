package com.piattaforme.smartparking.activities.support

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class SpotAlarmScheduler(private val context: Context){

    val alarmPermissionCode = 101

    @SuppressLint("ScheduleExactAlarm")
    @RequiresApi(Build.VERSION_CODES.S)
    fun setAlarm(time : Long){

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmPermissionCode,
            Intent(context, SpotNotificationReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)

    }
}