package com.piattaforme.smartparking.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SpotsHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val spotsHistoryDao: SpotsHistoryDao
    private lateinit var allHistory : List<Spots>
    init {
        val database  = AppDatabase.getDatabase(application)
        spotsHistoryDao = database.parkingDao()
        viewModelScope.launch(Dispatchers.IO) {
            allHistory = spotsHistoryDao.getAllHistory()
        }
    }


    fun getAllHistory(): List<Spots> {
        viewModelScope.launch(Dispatchers.IO) {
            allHistory = spotsHistoryDao.getAllHistory()
        }
        return allHistory
    }

     fun insertParking(parking: Spots): Boolean{
         viewModelScope.launch(Dispatchers.IO) {
            try {
                spotsHistoryDao.insert(parking)
            }  catch (e : Exception){
                //TODO
            }
         }
         return true
     }

     fun clearHistory() {
        spotsHistoryDao.deleteAllHistory()
    }
}