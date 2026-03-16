package com.piattaforme.smartparking.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.piattaforme.smartparking.activities.support.SpotAlarmManager
import com.piattaforme.smartparking.model.SpotsHistoryViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.Calendar

class MapActivity : AppCompatActivity() {
    val locationPermissionCode = 100
    private lateinit var mapView: MapView
    private var pendingAlarmTime: Long = 0L
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

        mapLocationManager = MapLocationManager(this,this.findViewById(R.id.osmdroid_map))


        this.mapView = mapLocationManager.initMap() { point ->
            parkHere(point)
        }

        val lat = intent.getFloatExtra("LAT",0f)
        val lon = intent.getFloatExtra("LON",0f)

        if (lat != 0f && lon != 0f)
            mapLocationManager.focusToPoint(lat.toDouble(), lon.toDouble())

    }

    fun parkHere(point : GeoPoint) {
        val (layout, textInput, timePicker) = createLayout()

        val alarmManager = SpotAlarmManager(this)

        val director = MapDialogDirector()

        director.makeDialog(layout, this, AlertDialog.Builder(this)) {

            saveSpot(textInput, point)

            val calendar = alarmManager.getCalendar(timePicker)

            this.setAlarm(alarmManager, calendar)

        }
        director.getResult().show()
    }

    @SuppressLint("InlinedApi")
    private fun setAlarm( alarmManager: SpotAlarmManager, calendar: Calendar) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarm(calendar.timeInMillis)
        } else {
            Toast.makeText(this, this.getString(R.string.timer_permission), Toast.LENGTH_LONG)
                .show()
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            pendingAlarmTime = calendar.timeInMillis
            startActivity(intent)
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

    private fun saveSpot(textInput: EditText, spot : GeoPoint) {
        val userNote = textInput.text.toString()

        val finalText = userNote.ifBlank { this.getString(R.string.alert_saved_position) }

        historyViewModel = ViewModelProvider(this)[SpotsHistoryViewModel::class.java]

        mapLocationManager.addParkingSpot(spot.latitude, spot.longitude)

        lifecycleScope.launch {
            val success = historyViewModel.saveSpot(
                spot.latitude.toFloat(),
                spot.longitude.toFloat(),
                finalText
            )
            if (success) {
                Toast.makeText(this@MapActivity, getString(R.string.saved_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MapActivity, getString(R.string.saved_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == locationPermissionCode){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,getString(R.string.access_authorized), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,getString(R.string.access_refused), Toast.LENGTH_SHORT).show()
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
        if (pendingAlarmTime != 0L) {
            val alarmManager = SpotAlarmManager(this)
            if(alarmManager.canScheduleExactAlarms()){
                alarmManager.setAlarm(pendingAlarmTime)
                pendingAlarmTime = 0L
            }
        }
        mapLocationManager.requestLocationManagerUpdates()
    }
}