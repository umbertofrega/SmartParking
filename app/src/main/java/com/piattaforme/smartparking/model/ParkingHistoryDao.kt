package com.piattaforme.smartparking.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ParkingHistoryDao {

    @Insert
    fun insert(parking: Park)

    @Query("SELECT * FROM history ORDER BY id")
    fun getAllHistory() : LiveData<List<Park>>

    @Query("DELETE FROM history")
    fun deleteAllHistory()
}