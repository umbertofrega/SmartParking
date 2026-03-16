package com.piattaforme.smartparking.activities.support

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
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
    private val prefsManager = PrefsManager(context)

    fun initMap( onPark: (point : GeoPoint) -> Unit) : MapView {
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(18.0)

        this.requestLocationManagerUpdates()

        this.restoreMarkers()

        mapView.overlays.add(getEventReceiver(onPark))

        return this.mapView
    }

    private fun restoreMarkers(){
        val savedMarkers = prefsManager.getAll()

        for (spot in savedMarkers) {
            val strings = spot?.split(",")

            if (strings?.size == 2) {
                val lat = strings[0].toDouble()
                val lon = strings[1].toDouble()

                this.addParkingSpot(lat, lon)
            }
        }
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

    fun drawMarker(lat: Double, lon: Double){
        val newMarker = createMarker(lat, lon, context.getString(R.string.parked_spot))
        newMarker.setOnMarkerClickListener { clickedMarker, _ ->
            createDialog(clickedMarker)
            true
        }

        mapView.overlays.add(newMarker)
        mapView.invalidate()
    }

    fun addParkingSpot(lat: Double, lon: Double) {
        this.drawMarker(lat,lon)
        prefsManager.save(lat,lon)
    }

    private fun createDialog(marker : Marker) {
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle(context.getString(R.string.cancel_marker))
        builder.setMessage("Il punto "+marker.title+" sarà eliminato dalla mappa!")
        builder.setView(LinearLayout(this.context))

        builder.setPositiveButton(context.getString(R.string.cancel)){_, _ ->
            mapView.overlays.remove(marker)
            prefsManager.remove(marker.position.latitude, marker.position.longitude)
            mapView.invalidate()
        }

        builder.setNegativeButton(context.getString(R.string.alert_cancel)){_,_ -> }
        builder.show()
    }

    override fun onLocationChanged(location: Location) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if (userLocationMarker == null) {
            userLocationMarker = createMarker(location.latitude, location.longitude, context.getString(R.string.you_are_here))
            mapView.overlays.add(userLocationMarker)
            mapView.controller.setCenter(geoPoint)
        } else {
            userLocationMarker?.position = geoPoint
        }

        mapView.invalidate()
    }

    private fun createMarker(latitude: Double, longitude: Double, title: String): Marker {
        val newMarker = Marker(mapView)
        newMarker.position = GeoPoint(latitude, longitude)
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        newMarker.title = title
        return newMarker
    }

    fun stopUpdates(){
        locationManager.removeUpdates(this)
    }

    fun focusToPoint(lat: Double, lon: Double) {
        val newMarker = createMarker(lat, lon, "Saved Spot")
        val point = GeoPoint(lat,lon)

        newMarker.setOnMarkerClickListener { clickedMarker, _ ->
            createDialog(clickedMarker)
            true
        }

        mapView.overlays.add(newMarker)
        prefsManager.save(lat,lon)
        mapView.controller.setCenter(point)
        mapView.invalidate()
    }

}
