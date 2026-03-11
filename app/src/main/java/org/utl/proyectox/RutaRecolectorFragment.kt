package org.utl.proyectox

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.maps.android.PolyUtil
import org.utl.proyectox.model.Residuo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RutaRecolectorFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var residuoSeleccionado: Residuo? = null

    private lateinit var tvDistancia: TextView
    private lateinit var tvDuracion: TextView

    companion object {
        fun newInstance(residuo: Residuo): RutaRecolectorFragment {
            val fragment = RutaRecolectorFragment()
            val args = Bundle()
            args.putSerializable("residuo_data", residuo)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        residuoSeleccionado = arguments?.getSerializable("residuo_data") as? Residuo
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ruta_recolector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDistancia = view.findViewById(R.id.tv_distancia)
        tvDuracion = view.findViewById(R.id.tv_duracion)
        val btnIniciar = view.findViewById<MaterialButton>(R.id.btn_iniciar_ruta)
        btnIniciar.setOnClickListener { abrirEnGoogleMaps() }

        // IMPORTANTE: Buscar el fragmento del mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_ruta) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Si aparece en blanco, a veces es porque falta mover la cámara a un punto inicial
        val leonCentro = LatLng(21.1222, -101.6823)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(leonCentro, 12f))

        obtenerUbicacionYCalcularRuta()
    }

    private fun obtenerUbicacionYCalcularRuta() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        mMap?.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null && residuoSeleccionado != null) {
                val miPos = LatLng(location.latitude, location.longitude)
                val destLat = residuoSeleccionado?.latitud ?: 0.0
                val destLng = residuoSeleccionado?.longitud ?: 0.0
                val destinoPos = LatLng(destLat, destLng)

                mMap?.addMarker(MarkerOptions().position(destinoPos).title("Destino Residuo"))

                // Ajustar la cámara para que se vean ambos puntos
                val bounds = LatLngBounds.Builder().include(miPos).include(destinoPos).build()
                mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))

                // Aquí llamarías a la función de Retrofit para dibujar la línea
            }
        }
    }

    private fun abrirEnGoogleMaps() {
        residuoSeleccionado?.let { residuo ->
            val uriStr = if (residuo.latitud != null && residuo.latitud != 0.0) {
                "google.navigation:q=${residuo.latitud},${residuo.longitud}"
            } else {
                "google.navigation:q=${Uri.encode(residuo.direccion)}"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr))
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }
}