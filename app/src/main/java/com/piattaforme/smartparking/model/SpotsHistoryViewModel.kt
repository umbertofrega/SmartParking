package com.piattaforme.smartparking.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SpotsHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val spotsHistoryDao: SpotsHistoryDao
    private var allHistory : LiveData<List<Spots>>
    init {
        val database  = AppDatabase.getDatabase(application)
        spotsHistoryDao = database.parkingDao()
        this.allHistory = spotsHistoryDao.getAllHistory().asLiveData()
    }


    fun getAllHistory(): LiveData<List<Spots>> {
        return this.allHistory
    }

    suspend fun saveSpot(lat: Float, lon : Float, note : String) : Boolean{

        val parking = Spots(
            latitude = lat,
            longitude = lon,
            note = note
        )

        return withContext(Dispatchers.IO) {
            try {
                spotsHistoryDao.insert(parking)
                true
            } catch (_: Exception) {
                System.err.println("Error in inserting the spot in db")
                false
            }
        }
}
     fun clearHistory() {
         viewModelScope.launch(Dispatchers.IO) {
             spotsHistoryDao.deleteAllHistory()
         }
     }
}