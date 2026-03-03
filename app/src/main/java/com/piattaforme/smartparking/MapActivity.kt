package com.piattaforme.smartparking

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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.piattaforme.smartparking.model.Spots
import com.piattaforme.smartparking.model.SpotsHistoryViewModel

class MapActivity : AppCompatActivity(), LocationListener {
    val locationPermissionCode = 100
    private lateinit var mapView: MapView
    private lateinit var  locationManager : LocationManager

    private lateinit var marker : Marker

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
        
        parkListener.setOnClickListener{ parkHere()}
    }

    fun initMap() {
        mapView = findViewById(R.id.osmdroid_map)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(18.0)

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

        val prefs = applicationContext.getSharedPreferences("SmartParkingData", MODE_PRIVATE)
        if(prefs.getBoolean("IS_PARKED", false)){
            val parkingLatitude = prefs.getFloat("PARK_LAT",0.toFloat()).toDouble()
            val parkingLongitude = prefs.getFloat("PARK_LON",0.toFloat()).toDouble()
            val parking = createMarker(parkingLatitude,parkingLongitude,"Parked Here")
            mapView.overlays.add(parking)
            mapView.invalidate()
        }
    }

    override fun onLocationChanged(location: Location){
        val geoPoint = GeoPoint(location.latitude,location.longitude)

        if (::marker.isInitialized) {
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

    fun parkHere() {
        if (::marker.isInitialized) {
            val inputTesto = EditText(this)
            inputTesto.hint = "Es: Via Roma 15, strisce blu..."

            val layout = LinearLayout(this)
            layout.setPadding(50, 20, 50, 20)
            layout.addView(inputTesto)

            AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.alert_title))
                .setMessage(this.getString(R.string.alert_message))
                .setView(layout)
                .setPositiveButton(this.getString(R.string.alert_button)) { dialog, _ ->

                    val userNote = inputTesto.text.toString()

                    val finalText = userNote.ifBlank { this.getString(R.string.alert_saved_position) }

                    val parking = Spots(latitude = marker.position.latitude.toFloat(), longitude = marker.position.longitude.toFloat(), note = finalText)

                    val prefs = applicationContext.getSharedPreferences("SmartParkingData", MODE_PRIVATE)
                    prefs.edit {
                        putFloat("PARK_LAT", parking.latitude)
                        putFloat("PARK_LON", parking.longitude)
                        putBoolean("IS_PARKED", true)
                    }

                    val historyViewModel = ViewModelProvider(this)[SpotsHistoryViewModel::class.java]
                    val success = historyViewModel.insertParking(parking)

                    if(success) {
                        Toast.makeText(this, this.getString(R.string.saved_success), Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(this.getString(R.string.alert_cancel)) { dialog, _ ->
                    dialog.cancel()
                }
                .show()

        } else {
            Toast.makeText(this, this.getString(R.string.alert_wait), Toast.LENGTH_SHORT).show()
        }
    }
}