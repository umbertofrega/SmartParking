package com.piattaforme.smartparking.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotsHistoryDao {

    @Insert
    fun insert(parking: Spots)

    @Query("SELECT * FROM history ORDER BY id DESC")
    fun getAllHistory(): Flow<List<Spots>>

    @Query("DELETE FROM history")
    fun deleteAllHistory()
}