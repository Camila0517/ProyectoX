package org.utl.proyectox

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ---------------- MODELOS ----------------

data class DirectionsResponse(val routes: List<Route>, val status: String)
data class Route(val legs: List<Leg>, val overview_polyline: PolylinePoint)
data class Leg(val distance: DistanceText, val duration: DurationText)
data class DistanceText(val text: String, val value: Int)
data class DurationText(val text: String, val value: Int)
data class PolylinePoint(val points: String)

// ---------------- API ----------------

interface DirectionsApiService {
    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "driving",
        @Query("region") region: String = "mx",
        @Query("key") key: String = "AIzaSyDdPpdigFWN3C2pUHq5XGL25oTWkPWuTvo"
    ): Call<DirectionsResponse>
}

// ---------------- FRAGMENT ----------------

class RutaRecolectorFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tvDistancia: TextView
    private lateinit var tvDuracion: TextView
    private lateinit var btnIniciarRuta: MaterialButton

    // 📍 PUNTO DE PARTIDA FIJO
    private val puntoPartida = LatLng(21.146517527135277, -101.65007931970511)

    // 📍 LISTA DE DESTINOS PARA ALTERNAR
    private val listaDestinos = listOf(
        LatLng(21.150204361211284, -101.64124148960092), // Destino 1 (Deportiva)
        LatLng(21.1378, -101.6702)                      // Destino 2 (Cerca del Centro)
    )

    private lateinit var destinoActual: LatLng

    companion object {
        // Variable estática que persiste entre cambios de fragmento
        var contadorRuta = 0
        fun newInstance() = RutaRecolectorFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Seleccionamos el destino basado en el contador actual
        destinoActual = listaDestinos[contadorRuta % listaDestinos.size]
        // Incrementamos para la siguiente vez
        contadorRuta++
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ruta_recolector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvDistancia = view.findViewById(R.id.tv_distancia)
        tvDuracion = view.findViewById(R.id.tv_duracion)
        btnIniciarRuta = view.findViewById(R.id.btn_iniciar_ruta)

        btnIniciarRuta.setOnClickListener { abrirEnGoogleMaps() }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_ruta) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoPartida, 14f))

        agregarMarcadores()
        obtenerRuta()
    }

    private fun agregarMarcadores() {
        mMap.clear() // Limpiar rutas anteriores
        mMap.addMarker(MarkerOptions().position(puntoPartida).title("Mi Ubicación")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))

        mMap.addMarker(MarkerOptions().position(destinoActual).title("Destino Residuo")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
    }

    private fun obtenerRuta() {
        val origin = "${puntoPartida.latitude},${puntoPartida.longitude}"
        val dest = "${destinoActual.latitude},${destinoActual.longitude}"

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(DirectionsApiService::class.java)

        service.getDirections(origin, dest).enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                val body = response.body()
                if (response.isSuccessful && body?.status == "OK") {
                    val route = body.routes.first()
                    val leg = route.legs.first()

                    // Mostrar los datos en los TextViews
                    tvDistancia.text = "📍 Distancia: ${leg.distance.text}"
                    tvDuracion.text = "⏱ Tiempo: ${leg.duration.text}"

                    val puntos = PolyUtil.decode(route.overview_polyline.points)
                    mMap.addPolyline(PolylineOptions().addAll(puntos).width(12f).color(Color.parseColor("#4A90E2")))

                    val bounds = LatLngBounds.builder()
                    puntos.forEach { bounds.include(it) }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150))
                } else {
                    Toast.makeText(requireContext(), "Error en ruta: ${body?.status}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun abrirEnGoogleMaps() {
        // Navegación GPS a las coordenadas del destino actual
        val gmmIntentUri = Uri.parse("google.navigation:q=${destinoActual.latitude},${destinoActual.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(requireContext(), "Instala Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
}