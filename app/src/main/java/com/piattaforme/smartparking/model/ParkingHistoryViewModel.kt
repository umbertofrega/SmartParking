package com.piattaforme.smartparking.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData


class ParkingHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val parkingHistoryDao: ParkingHistoryDao
    private val allHistory: LiveData<List<Park>>
    init {
        val database  = AppDatabase.getDatabase(application)
        parkingHistoryDao = database.parkingDao()
        allHistory = parkingHistoryDao.getAllHistory()
    }


    fun getAllHistory(): LiveData<List<Park>> {
        return parkingHistoryDao.getAllHistory()
    }

     fun insertParking(parking: Park): Boolean{
         Thread {
            try {
                parkingHistoryDao.insert(parking)
            }  catch (e : Exception){

            }
         }.start()
         return true
     }

     fun clearHistory() {
        parkingHistoryDao.deleteAllHistory()
    }
}