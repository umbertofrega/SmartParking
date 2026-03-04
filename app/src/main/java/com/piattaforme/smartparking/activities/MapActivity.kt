package com.piattaforme.smartparking.activities

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.piattaforme.smartparking.R
import com.piattaforme.smartparking.activities.support.MapDialogDirector
import com.piattaforme.smartparking.activities.support.SpotAlarmScheduler
import com.piattaforme.smartparking.model.SpotsHistoryViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapActivity : AppCompatActivity(), LocationListener {
    val locationPermissionCode = 100
    private lateinit var mapView: MapView
    private lateinit var  locationManager : LocationManager
    private var marker : Marker? = null
    private lateinit var historyViewModel: SpotsHistoryViewModel

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

    fun parkHere() {
        if (marker != null) {
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

                SpotAlarmScheduler(this).setAlarm(calendar.timeInMillis)
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

        if(marker != null) {
            lifecycleScope.launch {
                val success = historyViewModel.saveSpot(
                    marker!!.position.latitude.toFloat(),
                    marker!!.position.longitude.toFloat(),
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
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(18.0)

        requestLocationManagerUpdates()

        val prefs = applicationContext.getSharedPreferences("SmartParkingData", MODE_PRIVATE)
        if(prefs.getBoolean("IS_PARKED", false)){
            val parkingLatitude = prefs.getFloat("PARK_LAT",0.toFloat()).toDouble()
            val parkingLongitude = prefs.getFloat("PARK_LON",0.toFloat()).toDouble()
            val parking = createMarker(parkingLatitude,parkingLongitude,"Parked Here")
            mapView.overlays.add(parking)
            mapView.invalidate()
        }
    }

    private fun requestLocationManagerUpdates(){
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val requestLocationPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        if (requestLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000L,
                10f,
                this
            )
        }
    }

    override fun onLocationChanged(location: Location){
        val geoPoint = GeoPoint(location.latitude,location.longitude)

        if (marker != null) {
            mapView.overlays.remove(marker)
        }

        marker = createMarker(location.latitude,location.longitude, "Sei qui")

        mapView.overlays.add(marker)
        mapView.controller.setCenter(geoPoint)
        mapView.invalidate()
    }

    private fun createMarker(latitude : Double, longitude: Double, title : String): Marker {
        val newMarker = Marker(mapView)

        val geoPoint = GeoPoint(latitude,longitude)

        newMarker.position = geoPoint

        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        newMarker.title = title

        return newMarker
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

        locationManager.removeUpdates(this)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()

        requestLocationManagerUpdates()
    }
}