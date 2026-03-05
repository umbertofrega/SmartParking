package com.piattaforme.smartparking.activities

import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.piattaforme.smartparking.R
import com.piattaforme.smartparking.activities.support.MapDialogDirector
import com.piattaforme.smartparking.activities.support.MapLocationManager
import com.piattaforme.smartparking.activities.support.SpotAlarmScheduler
import com.piattaforme.smartparking.model.SpotsHistoryViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView

class MapActivity : AppCompatActivity() {
    val locationPermissionCode = 100
    private lateinit var mapView: MapView
    private lateinit var mapLocationManager : MapLocationManager
    private lateinit var historyViewModel: SpotsHistoryViewModel

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(applicationContext, applicationContext.getSharedPreferences("osmdroid_prefs", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = applicationContext.packageName

        enableEdgeToEdge()
        setContentView(R.layout.activity_map)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initMap()

        val parkListener: Button = findViewById(R.id.btn_park_here)
        
        parkListener.setOnClickListener{ parkHere() }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun parkHere() {
        if (mapLocationManager.getCurrentMarker() != null) {
            val (layout, textInput, timePicker) = createLayout()

            val director = MapDialogDirector()

            director.makeDialog(layout, this, AlertDialog.Builder(this)) {

                saveSpot(textInput)

                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, timePicker.hour)
                    set(java.util.Calendar.MINUTE, timePicker.minute)
                    set(java.util.Calendar.SECOND, 0)
                }

                if (calendar.before(java.util.Calendar.getInstance())) {
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
                }

                val alarmManager = this.getSystemService(ALARM_SERVICE) as AlarmManager
                if(alarmManager.canScheduleExactAlarms()){
                    SpotAlarmScheduler(this).setAlarm(calendar.timeInMillis)

                }else{
                    Toast.makeText(this, this.getString(R.string.timer_permission), Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }
            }
            director.getResult().show()

        } else {
            Toast.makeText(this, this.getString(R.string.alert_wait), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createLayout(): Triple<LinearLayout, EditText, TimePicker> {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val textView = TextView(this)
        textView.text = this.getString(R.string.timer_text)

        val textInput = EditText(this)
        textInput.hint = this.getString(R.string.alert_hint)

        val timePicker = TimePicker(this)
        timePicker.setIs24HourView(true)

        layout.addView(textInput)
        layout.addView(textView)
        layout.addView(timePicker)

        return Triple(layout, textInput, timePicker)
    }

    private fun saveSpot(textInput: EditText) {
        val userNote = textInput.text.toString()

        val finalText = userNote.ifBlank { this.getString(R.string.alert_saved_position) }

        historyViewModel = ViewModelProvider(this)[SpotsHistoryViewModel::class.java]

        val currentLocation = mapLocationManager.getCurrentMarker()
        if (currentLocation != null) {
            mapLocationManager.setParkingSpot(currentLocation.latitude, currentLocation.longitude)
            lifecycleScope.launch {
                val success = historyViewModel.saveSpot(
                    currentLocation.latitude.toFloat(),
                    currentLocation.longitude.toFloat(),
                    finalText
                )
                if (success) {
                    Toast.makeText(this@MapActivity, getString(R.string.saved_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MapActivity, getString(R.string.saved_fail), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun initMap() {
        mapView = findViewById(R.id.osmdroid_map)
        mapLocationManager = MapLocationManager(mapView,this)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(18.0)

        mapLocationManager.requestLocationManagerUpdates()

        val prefs = applicationContext.getSharedPreferences("SmartParkingData", MODE_PRIVATE)
        if(prefs.getBoolean("IS_PARKED", false)){
            val lat = prefs.getFloat("PARK_LAT", 0f).toDouble()
            val lon = prefs.getFloat("PARK_LON", 0f).toDouble()
            mapLocationManager.setParkingSpot(lat, lon)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == locationPermissionCode){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Access Authorized", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"Access Refused", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()

        mapLocationManager.stopUpdates()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        mapLocationManager.requestLocationManagerUpdates()
    }
}