package com.piattaforme.smartparking.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SpotsHistoryDao {

    @Insert
    fun insert(parking: Spots)

    @Query("SELECT * FROM history ORDER BY id")
    suspend fun getAllHistory() : List<Spots>

    @Query("DELETE FROM history")
    fun deleteAllHistory()
}