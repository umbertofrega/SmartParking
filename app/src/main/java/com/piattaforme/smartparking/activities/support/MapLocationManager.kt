package com.piattaforme.smartparking.activities.support

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.piattaforme.smartparking.R
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MapLocationManager(val mapView: MapView,val context: Context) : LocationListener {
    val locationPermissionCode = 100
    private lateinit var  locationManager : LocationManager

    private var marker: Marker? = null

    fun getCurrentGeoPoint() : GeoPoint? {
        if(marker == null)
            return null
        return GeoPoint(marker!!.position.latitude, marker!!.position.longitude)
    }
 
    override fun onLocationChanged(location: Location){
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if(marker == null) {
            marker = createMarker(location.latitude, location.longitude, context.getString(R.string.you_are_here))
            mapView.overlays.add(marker)
        }else{
            marker?.position = geoPoint
        }

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


    fun setParkingSpot(lat: Double, lon: Double) {
        val text = context.getString(R.string.parked_spot)
        val newMarker = Marker(mapView)
        newMarker.position = GeoPoint(lat, lon)
        newMarker.title = text
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        mapView.overlays.add(newMarker)
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