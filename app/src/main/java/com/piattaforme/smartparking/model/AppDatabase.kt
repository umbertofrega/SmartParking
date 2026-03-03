package com.piattaforme.smartparking.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver

@Database(entities = [Spots::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun parkingDao(): SpotsHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE?: synchronized(this){
                val driver = AndroidSQLiteDriver()
                val instance = Room.databaseBuilder<AppDatabase>(context.applicationContext,"history_database").setDriver(driver).build()
                INSTANCE = instance
                instance
            }
        }
    }
}