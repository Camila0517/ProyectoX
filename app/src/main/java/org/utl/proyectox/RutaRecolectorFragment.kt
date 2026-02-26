package org.utl.proyectox

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ── Modelos para Directions API ──────────────────────────────────────────────

data class DirectionsResponse(
    val routes: List<Route>,
    val status: String
)

data class Route(
    val legs: List<Leg>,
    val overview_polyline: PolylinePoint
)

data class Leg(
    val distance: DistanceText,
    val duration: DurationText,
    val start_location: LatLngDirection,
    val end_location: LatLngDirection
)

data class DistanceText(val text: String, val value: Int)
data class DurationText(val text: String, val value: Int)
data class PolylinePoint(val points: String)
data class LatLngDirection(val lat: Double, val lng: Double)

// ── Interfaz Retrofit para Directions API ────────────────────────────────────

interface DirectionsApiService {
    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "driving",
        @Query("key") key: String = BuildConfig.GOOGLE_MAPS_KEY
    ): Call<DirectionsResponse>
}

// ── Fragment principal ───────────────────────────────────────────────────────

class RutaRecolectorFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var residuo: Residuo? = null
    private var ubicacionRecolector: LatLng? = null

    private lateinit var tvDistancia: TextView
    private lateinit var tvDuracion: TextView
    private lateinit var btnIniciarRuta: MaterialButton
    private lateinit var btnRecogerResiduo: MaterialButton

    private val TAG = "RUTA_FRAGMENT"
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    // ── Companion object para crear el fragment con el residuo ───────────────

    companion object {
        private const val ARG_RESIDUO = "residuo"

        fun newInstance(residuo: Residuo): RutaRecolectorFragment {
            val args = Bundle()
            args.putSerializable(ARG_RESIDUO, residuo as java.io.Serializable)
            val fragment = RutaRecolectorFragment()
            fragment.arguments = args
            return fragment
        }
    }

    // ── Ciclo de vida ────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            residuo = it.getSerializable(ARG_RESIDUO) as? Residuo
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ruta_recolector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvDistancia = view.findViewById(R.id.tv_distancia)
        tvDuracion = view.findViewById(R.id.tv_duracion)
        btnIniciarRuta = view.findViewById(R.id.btn_iniciar_ruta)
        btnRecogerResiduo = view.findViewById(R.id.btn_recoger_residuo)

        btnIniciarRuta.setOnClickListener { abrirEnGoogleMaps() }
        btnRecogerResiduo.setOnClickListener { marcarComoRecogido() }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_ruta) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // ── Mapa ─────────────────────────────────────────────────────────────────

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d(TAG, "Mapa listo")

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        verificarPermisos()
    }

    // ── Permisos y ubicación ─────────────────────────────────────────────────

    private fun verificarPermisos() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                obtenerUbicacionActual()
            }
            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    ubicacionRecolector = LatLng(location.latitude, location.longitude)
                    Log.d(TAG, "Ubicación obtenida: $ubicacionRecolector")
                } else {
                    // Fallback: coordenadas por defecto si el GPS no responde
                    ubicacionRecolector = LatLng(19.4326, -99.1332)
                    Toast.makeText(
                        requireContext(),
                        "No se pudo obtener ubicación real, usando ubicación por defecto",
                        Toast.LENGTH_LONG
                    ).show()
                }
                agregarMarcadores()
                obtenerRuta()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo ubicación: ${e.message}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(
                requireContext(),
                "Se necesita permiso de ubicación para trazar la ruta",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ── Marcadores en el mapa ────────────────────────────────────────────────

    private fun agregarMarcadores() {
        // Marcador azul → recolector
        ubicacionRecolector?.let {
            mMap.addMarker(
                MarkerOptions()
                    .position(it)
                    .title("Tu ubicación")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
        }

        // Marcador rojo → residuo
        residuo?.let {
            val lat = it.latitud ?: 0.0
            val lng = it.longitud ?: 0.0
            val pos = LatLng(lat, lng)
            mMap.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(it.tipo)
                    .snippet(it.direccion)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }
    }

    // ── Ruta con Directions API ──────────────────────────────────────────────

    private fun obtenerRuta() {
        if (ubicacionRecolector == null || residuo == null) return

        val origin = "${ubicacionRecolector!!.latitude},${ubicacionRecolector!!.longitude}"
        val lat = residuo!!.latitud ?: 0.0
        val lng = residuo!!.longitud ?: 0.0
        val dest = "$lat,$lng"

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(DirectionsApiService::class.java)

        service.getDirections(origin, dest).enqueue(object : Callback<DirectionsResponse> {

            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                if (response.isSuccessful && response.body()?.status == "OK") {
                    val route = response.body()!!.routes.firstOrNull() ?: return
                    val leg = route.legs.firstOrNull() ?: return

                    // Mostrar distancia y tiempo estimado
                    tvDistancia.text = "📍 Distancia: ${leg.distance.text}"
                    tvDuracion.text = "⏱ Tiempo estimado: ${leg.duration.text}"

                    // Dibujar la polilínea azul en el mapa
                    val puntos = PolyUtil.decode(route.overview_polyline.points)
                    mMap.addPolyline(
                        PolylineOptions()
                            .addAll(puntos)
                            .width(10f)
                            .color(Color.BLUE)
                            .geodesic(true)
                    )

                    // Mover cámara para encuadrar toda la ruta
                    val boundsBuilder = LatLngBounds.builder()
                    puntos.forEach { boundsBuilder.include(it) }
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120)
                    )

                } else {
                    Log.e(TAG, "Error Directions API: ${response.body()?.status}")
                    Toast.makeText(
                        requireContext(),
                        "No se pudo calcular la ruta: ${response.body()?.status}",
                        Toast.LENGTH_SHORT
                    ).show()
                    centrarCamaraSimple()
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e(TAG, "Fallo de red: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Error de conexión al calcular ruta",
                    Toast.LENGTH_SHORT
                ).show()
                centrarCamaraSimple()
            }
        })
    }

    // Fallback si falla la API: centra la cámara entre los dos puntos
    private fun centrarCamaraSimple() {
        val recolector = ubicacionRecolector ?: return
        val lat = residuo?.latitud ?: 0.0
        val lng = residuo?.longitud ?: 0.0
        val boundsBuilder = LatLngBounds.builder()
            .include(recolector)
            .include(LatLng(lat, lng))
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120)
        )
    }

    // ── Botones de acción ────────────────────────────────────────────────────

    // Abre Google Maps con navegación paso a paso
    private fun abrirEnGoogleMaps() {
        val recolector = ubicacionRecolector ?: run {
            Toast.makeText(requireContext(), "Ubicación no disponible", Toast.LENGTH_SHORT).show()
            return
        }
        val lat = residuo?.latitud ?: run {
            Toast.makeText(requireContext(), "Residuo sin coordenadas", Toast.LENGTH_SHORT).show()
            return
        }
        val lng = residuo?.longitud ?: return

        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1" +
                    "&origin=${recolector.latitude},${recolector.longitude}" +
                    "&destination=$lat,$lng" +
                    "&travelmode=driving"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            // Si no tiene Google Maps, abre en el navegador
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }

    // Llama al backend para marcar el residuo como RECOGIDO
    private fun marcarComoRecogido() {
        val r = residuo ?: return

        // Extraer id de forma segura
        val residuoId = r.id ?: run {
            Toast.makeText(requireContext(), "ID de residuo no válido", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = requireActivity()
            .getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val usuarioId = sharedPref.getLong("USUARIO_ID", -1L)

        if (usuarioId == -1L) {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        btnRecogerResiduo.isEnabled = false
        btnRecogerResiduo.text = "Procesando..."

        // ✅ residuoId ya es Long (no nullable)
        RetrofitClient.instance.recogerResiduo(residuoId, usuarioId)
            .enqueue(object : Callback<Residuo> {
                override fun onResponse(call: Call<Residuo>, response: Response<Residuo>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "✅ ¡Residuo recogido exitosamente!",
                            Toast.LENGTH_SHORT
                        ).show()
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al actualizar: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        btnRecogerResiduo.isEnabled = true
                        btnRecogerResiduo.text = "Marcar como Recogido"
                    }
                }

                override fun onFailure(call: Call<Residuo>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Error de conexión: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnRecogerResiduo.isEnabled = true
                    btnRecogerResiduo.text = "Marcar como Recogido"
                }
            })
    }
}