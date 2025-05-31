package com.example.kickup.Activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.kickup.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class GeolocationActivity : AppCompatActivity(), OnMapReadyCallback {

    //SE GUARDA LA INSTANCIA DEL MAPA
    private lateinit var mMap: GoogleMap

    //CLIENTE DE UBICACIÓN PARA ACCEDER A LA UBICACIÓN DEL DISPOSITIVO
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //SOLICITUD DE PERMISO DE UBICACIÓN
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geolocation)

        //INICIALIZA EL CLIENTE DE UBICACIÓN
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //OBTIENE EL FRAGMENTO DEL MAPA Y SOLICITA NOTIFICACIÓN CUANDO ESTÉ LISTO
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //SE VERIFICA EL PERMISO DE UBICACIÓN
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()
    }

    //SE VERIFICA EL PERMISO DE UBICACIÓN
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableUserLocation()
        }
    }

    //SI EL PERMISO ESTÁ CONCEDIDO, ACTIVA LA UBICACIÓN
    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        mMap.isMyLocationEnabled = true

        //OBTIENE LA ÚLTIMA UBICACIÓN Y COLOCA UN MARCADOR
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(userLatLng).title("Tu ubicación"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            }
        }
    }

    //GESTIONA LA RESPUESTA DEL USUARIO
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        }
    }
}