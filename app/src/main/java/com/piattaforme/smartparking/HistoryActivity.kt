package com.piattaforme.smartparking

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.piattaforme.smartparking.model.DatabaseHelper

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showHistory(){
        val dbHelper = DatabaseHelper(this)
        val cursor = dbHelper.getAllHistory()
        var count = 0
        if(cursor.moveToFirst()) {
            do {
                ++count
                val latitudeIndex = cursor.getColumnIndex("latitude")
                val latitude = cursor.getFloat(latitudeIndex)

                val longitudeIndex = cursor.getColumnIndex("latitude")
                val longitude = cursor.getFloat(longitudeIndex)

                val noteIndex = cursor.getColumnIndex( "note")
                val note = cursor.getString(noteIndex)
            } while (cursor.moveToNext())
        }
    }

}