package com.piattaforme.smartparking.activities

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.piattaforme.smartparking.R
import com.piattaforme.smartparking.model.SpotAdapter
import com.piattaforme.smartparking.model.SpotsHistoryViewModel

class HistoryActivity : AppCompatActivity() {
    private lateinit var viewModel: SpotsHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[SpotsHistoryViewModel::class.java]

        val btnEmpty : Button = findViewById(R.id.btn_empty)
        btnEmpty.setOnClickListener {
            viewModel.clearHistory()
        }

        showHistory()
    }

    fun showHistory(){

        val recyclerView = findViewById<RecyclerView>(R.id.rv_history)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = SpotAdapter()

        recyclerView.adapter = adapter

        adapter.setData(viewModel.getAllHistory())
    }
}