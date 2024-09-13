package com.example.autonomo3googlemaps

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.autonomo3googlemaps.databinding.ActivityMapsBinding
import android.location.Location

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private val LOCATION_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtener el fragmento del mapa y configurarlo
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar el botón para buscar la ubicación ingresada
        binding.searchLocationButton.setOnClickListener {
            val lat = binding.latitudeInput.text.toString().toDoubleOrNull()
            val lng = binding.longitudeInput.text.toString().toDoubleOrNull()

            if (lat != null && lng != null) {
                val searchedLocation = LatLng(lat, lng)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, 15f))
                mMap.addMarker(MarkerOptions().position(searchedLocation).title("Ubicación buscada"))
            } else {
                Toast.makeText(this, "Por favor ingresa latitud y longitud válidas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        enableUserLocation()
    }

    // Función para habilitar la ubicación del usuario y mover la cámara
    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Pedir permisos de ubicación si no están concedidos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Habilitar la ubicación del usuario en el mapa
        mMap.isMyLocationEnabled = true

        // Obtener la última ubicación conocida del usuario
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)

                // Mover la cámara a la última ubicación conocida
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f), 4000, null)

                // Añadir un marcador en la ubicación actual del usuario
                mMap.addMarker(MarkerOptions().position(userLatLng).title("Mi ubicación"))
            }
        }
    }

    // Manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
