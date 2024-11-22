package com.santaananortemetapan.uberclone.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.santaananortemetapan.uberclone.R
import com.santaananortemetapan.uberclone.databinding.ActivityMapBinding
import com.santaananortemetapan.uberclone.providers.GeoProvider
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.santaananortemetapan.uberclone.models.DriverLocation
import com.santaananortemetapan.uberclone.providers.AuthProvider
import com.santaananortemetapan.uberclone.utils.CarMoveAnim
import org.imperiumlabs.geofirestore.listeners.GeoQueryEventListener

class MapActivity : AppCompatActivity(), OnMapReadyCallback, Listener {

    private lateinit var binding: ActivityMapBinding
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private var isLocationEnabled = false

    private val driverMarkers = ArrayList<Marker>()
    private val driversLocation = ArrayList<DriverLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(
            FLAG_LAYOUT_NO_LIMITS,
            FLAG_LAYOUT_NO_LIMITS
        )

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        easyWayLocation = EasyWayLocation(this, locationRequest, false, false, this)

        locationPermissions.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))


    }

    val locationPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
        when {
            permission.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Log.d("LOCALIZACION", "permiso aceptado")
                 easyWayLocation?.startLocation()

            }
            permission.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d("LOCALIZACION", "permiso concedido con limitacion")
                 easyWayLocation?.startLocation()

            }
            else -> {
                Log.d("LOCALIZACION", "permiso no aceptado")
            }
        }
    }


    private fun getNearbyDrivers(){

        if (myLocationLatLng == null) return

        geoProvider.getNearbyDrivers(myLocationLatLng!!, 20.0).addGeoQueryEventListener(object : GeoQueryEventListener {

            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                for(marker in driverMarkers){
                    if(marker.tag != null){
                        if(marker.tag == documentID){
                            return
                        }
                    }
                }

                // CREAMOS UN NUEVO MARCADOR PARA EL CONDUCTOR CONECTADO
                val driverLatLng = LatLng(location.latitude, location.longitude)
                val marker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(driverLatLng).title("Conductor disponible").icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.uber_car)
                        )
                )

                marker?.tag = documentID
                driverMarkers.add(marker!!)

                val dl = DriverLocation()
                dl.id = documentID
                driversLocation.add(dl)

            }

            override fun onKeyExited(documentID: String) {
                for (marker in driverMarkers){
                    if(marker.tag != null) {
                        if(marker.tag == documentID){
                            marker.remove()
                            driverMarkers.remove(marker)
                            driversLocation.removeAt(getPositionDriver(documentID))
                        }
                    }
                }
            }


            override fun onKeyMoved(documentID: String, location: GeoPoint) {
                for (marker in driverMarkers){

                    val start = LatLng(location.latitude, location.longitude)
                    var end: LatLng? = null
                    val position = getPositionDriver(marker.tag.toString())

                    if(marker.tag != null){
                        if(marker.tag == documentID){
                           // marker.position = LatLng(location.latitude, location.longitude)

                            if(driversLocation[position].latlng != null){
                                end = driversLocation[position].latlng
                            }
                            driversLocation[position].latlng = LatLng(location.latitude, location.longitude)
                            if(end != null) {
                                CarMoveAnim.carAnim(marker, end, start)
                            }
                        }
                    }
                }
            }

            override fun onGeoQueryError(exception: Exception) {

            }

            override fun onGeoQueryReady() {

            }





        })
    }





    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        easyWayLocation?.endUpdates()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        //  easyWayLocation?.startLocation()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap?.isMyLocationEnabled = true

    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location) { // ACTUALIZCION POSICION TIEMPO REAL
        myLocationLatLng = LatLng(location.latitude, location.longitude) // POSICION ACTUAL

        if (!isLocationEnabled) { // UNA SOLA VEZ
            isLocationEnabled = false
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(myLocationLatLng!!).zoom(17f).build()
            ))
            getNearbyDrivers()
        }
    }

    override fun locationCancelled() {

    }


    private fun getPositionDriver(id: String): Int {
        var position = 0
        for (i in driversLocation.indices) {
            if(id == driversLocation[i].id) {
                position = i
                break
            }
        }
        return position
    }


}