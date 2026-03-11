package com.piattaforme.smartparking.activities.support

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.widget.TimePicker
import java.util.Calendar

class SpotAlarmManager(private val context: Context){

    val alarmPermissionCode = 101
    val alarmScheduler = context.getSystemService(ALARM_SERVICE) as AlarmManager

    fun getCalendar(timePicker: TimePicker): Calendar {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timePicker.hour)
            set(Calendar.MINUTE, timePicker.minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar
    }

    fun canScheduleExactAlarms() : Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmScheduler.canScheduleExactAlarms()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun setAlarm(time : Long){
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmPermissionCode,
            Intent(context, SpotNotificationReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmScheduler.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
    }
}