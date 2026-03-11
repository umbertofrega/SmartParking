package com.piattaforme.smartparking.activities.support

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.piattaforme.smartparking.R
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

class MapLocationManager(val context: Context, val mapView : MapView) : LocationListener {
    private val locationPermissionCode = 100
    private var userLocationMarker: Marker? = null
    private lateinit var  locationManager : LocationManager

    private val parkingMarkers = mutableListOf<Marker>()

    fun initMap( onPark: (point : GeoPoint) -> Unit) : MapView {
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(18.0)

        this.requestLocationManagerUpdates()

        mapView.overlays.add(getEventReceiver(onPark))

        val prefs = context.getSharedPreferences("SmartParkingData", MODE_PRIVATE)
        if(prefs.getBoolean("IS_PARKED", false)){
            val lat = prefs.getFloat("PARK_LAT", 0f).toDouble()
            val lon = prefs.getFloat("PARK_LON", 0f).toDouble()
            this.setParkingSpot(lat, lon)
        }

        return this.mapView
    }

    fun getEventReceiver( onPark: (point : GeoPoint) -> Unit ): MapEventsOverlay{
        val mapEventReceiver : MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p0: GeoPoint?): Boolean {
                return false
            }

            override fun longPressHelper(p0: GeoPoint?): Boolean {
                if( p0 != null){
                    onPark(p0)
                    return true
                }
                return false
            }
        }

        return MapEventsOverlay(mapEventReceiver)
    }


    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if (userLocationMarker == null) {
            userLocationMarker = createMarker(location.latitude, location.longitude, context.getString(R.string.you_are_here))
            mapView.overlays.add(userLocationMarker)
        } else {
            userLocationMarker?.position = geoPoint
        }

        mapView.controller.setCenter(geoPoint)
        mapView.invalidate()
    }

    private fun createDialog(marker : Marker) {
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle(context.getString(R.string.cancel_marker))
        builder.setView(LinearLayout(this.context))

        builder.setPositiveButton(context.getString(R.string.cancel)){_, _ ->
            mapView.overlays.remove(marker)
            parkingMarkers.remove(marker)
            mapView.invalidate()
        }

        builder.setNegativeButton(context.getString(R.string.alert_cancel)){_,_ -> }
        builder.show()
    }

    private fun createMarker(latitude: Double, longitude: Double, title: String): Marker {
        val newMarker = Marker(mapView)
        newMarker.position = GeoPoint(latitude, longitude)
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        newMarker.title = title
        return newMarker
    }


    fun setParkingSpot(lat: Double, lon: Double) {
        val newMarker = createMarker(lat, lon, context.getString(R.string.parked_spot))

        newMarker.setOnMarkerClickListener { clickedMarker, _ ->
            createDialog(clickedMarker)
            true
        }

        mapView.overlays.add(newMarker)
        parkingMarkers.add(newMarker)

        mapView.invalidate()
    }

    fun stopUpdates(){
        locationManager.removeUpdates(this)
    }

    fun requestLocationManagerUpdates(){
        locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager

        val requestLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

        if (requestLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
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
}
