package com.piattaforme.smartparking.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData


class ParkingHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val parkingHistoryDao: ParkingHistoryDao
    private val allHistory: LiveData<List<ParkingHistory>>
    init {
        val database  = AppDatabase.getDatabase(application)
        parkingHistoryDao = database.parkingDao()
        allHistory = parkingHistoryDao.getAllHistory()
    }


    fun getAllHistory(): LiveData<List<ParkingHistory>> {
        return parkingHistoryDao.getAllHistory()
    }

     fun insertParking(parking: ParkingHistory) {
         Thread {
             parkingHistoryDao.insert(parking)
         }.start()
     }

     fun clearHistory() {
        parkingHistoryDao.deleteAllHistory()
    }
}