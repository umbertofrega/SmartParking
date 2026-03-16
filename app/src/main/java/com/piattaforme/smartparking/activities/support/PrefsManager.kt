package com.piattaforme.smartparking.activities.support

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PrefsManager( val context : Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("SmartParkingData", Context.MODE_PRIVATE)


    fun save(lat: Double, lon: Double) {
        val oldPoint = prefs.getStringSet("LIST", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        val newPoint = "$lat,$lon"
        oldPoint.add(newPoint)

        prefs.edit { putStringSet("LIST", oldPoint) }
    }

    fun remove(lat : Double, lon: Double){
        val oldPoints = this.getAll()


        prefs.edit { putStringSet("LIST",oldPoints.minus("$lat,$lon")) }
    }

    fun getAll(): Set<String?> {
        var list = prefs.getStringSet("LIST", emptySet())

        if (list == null)
            list = emptySet<String>()

        return list
    }
}