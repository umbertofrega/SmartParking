package com.piattaforme.smartparking.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SpotsHistoryDao {

    @Insert
    fun insert(parking: Spots)

    @Query("SELECT * FROM history ORDER BY id")
    fun getAllHistory() : LiveData<List<Spots>>

    @Query("DELETE FROM history")
    fun deleteAllHistory()
}