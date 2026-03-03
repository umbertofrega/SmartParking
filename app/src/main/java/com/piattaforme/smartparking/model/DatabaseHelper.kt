package com.piattaforme.smartparking.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "history.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE history (_id INTEGER PRIMARY KEY AUTOINCREMENT, latitude FLOAT, longitude FLOAT, note VARCHAR);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Aggiornamento del database quando cambia la versione
    }

    fun insertParking(latitude: Float, longitude: Float, finalText: String): Boolean {
        val db = this.writableDatabase

        val values  = ContentValues().apply{
            put("latitude",latitude)
            put("longitude", longitude)
            put("note",finalText)
        }

        val result = db.insert("history",null,values)
        return result != -1L
    }

    fun getAllHistory(): Cursor {
        val db = this.readableDatabase

        return db.rawQuery("SELECT * FROM history",null)
    }

    fun deleteAllHistory() {
        val db = this.writableDatabase

        db.delete( "history", null,null)
        db.close()
        onCreate(this.readableDatabase)
    }
}