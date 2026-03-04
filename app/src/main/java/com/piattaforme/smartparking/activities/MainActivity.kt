package com.piattaforme.smartparking.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.piattaforme.smartparking.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnPark : Button = findViewById(R.id.btn_park)
        val btnHistory : Button = findViewById(R.id.btn_history)

        btnPark.setOnClickListener {
            val mapIntent = Intent(this, MapActivity::class.java)

            startActivity(mapIntent)
        }

        btnHistory.setOnClickListener {
            val histIntent = Intent(this, HistoryActivity::class.java)

            startActivity(histIntent)
        }
    }
}