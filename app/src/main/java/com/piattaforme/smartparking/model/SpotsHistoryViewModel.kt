package com.piattaforme.smartparking.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData


class SpotsHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val spotsHistoryDao: SpotsHistoryDao
    private val allHistory: LiveData<List<Spots>>
    init {
        val database  = AppDatabase.getDatabase(application)
        spotsHistoryDao = database.parkingDao()
        allHistory = spotsHistoryDao.getAllHistory()
    }


    fun getAllHistory(): LiveData<List<Spots>> {
        return spotsHistoryDao.getAllHistory()
    }

     fun insertParking(parking: Spots): Boolean{
         Thread {
            try {
                spotsHistoryDao.insert(parking)
            }  catch (e : Exception){

            }
         }.start()
         return true
     }

     fun clearHistory() {
        spotsHistoryDao.deleteAllHistory()
    }
}