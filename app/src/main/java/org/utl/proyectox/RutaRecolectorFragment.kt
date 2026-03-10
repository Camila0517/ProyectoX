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

// --- MODELOS API DIRECTIONS ---
data class DirectionsResponse(val routes: List<Route>, val status: String)
data class Route(val legs: List<Leg>, val overview_polyline: PolylinePoint)
data class Leg(val distance: DistanceText, val duration: DurationText)
data class DistanceText(val text: String, val value: Int)
data class DurationText(val text: String, val value: Int)
data class PolylinePoint(val points: String)

interface DirectionsApiService {
    @GET("maps/api/directions/json")
    fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "driving",
        @Query("key") key: String = "TU_API_KEY_AQUI" // Reemplaza con tu llave real
    ): Call<DirectionsResponse>
}

class RutaRecolectorFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var tvDistancia: TextView
    private lateinit var tvDuracion: TextView
    private lateinit var btnIniciarRuta: MaterialButton

    private val puntoPartida = LatLng(21.146517527135277, -101.65007931970511)
    private val listaDestinos = listOf(
        LatLng(21.150204361211284, -101.64124148960092),
        LatLng(21.1378, -101.6702)
    )
    private lateinit var destinoActual: LatLng

    companion object {
        var contadorRuta = 0
        fun newInstance() = RutaRecolectorFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        destinoActual = listaDestinos[contadorRuta % listaDestinos.size]
        contadorRuta++
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(puntoPartida).title("Mi Ubicación")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        mMap.addMarker(MarkerOptions().position(destinoActual).title("Destino Residuo"))
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
                    val route = body.routes[0]
                    val leg = route.legs[0]

                    tvDistancia.text = "📍 Distancia: ${leg.distance.text}"
                    tvDuracion.text = "⏱ Tiempo: ${leg.duration.text}"

                    val puntos = PolyUtil.decode(route.overview_polyline.points)
                    mMap.addPolyline(PolylineOptions().addAll(puntos).width(12f).color(Color.BLUE))

                    val bounds = LatLngBounds.builder()
                    puntos.forEach { bounds.include(it) }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
                }
            }
            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun abrirEnGoogleMaps() {
        val gmmIntentUri = Uri.parse("google.navigation:q=${destinoActual.latitude},${destinoActual.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }
}