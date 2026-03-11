package org.utl.proyectox.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import org.utl.proyectox.R
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapHeatmapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val ACTION_RESIDUO_RECOGIDO = "org.utl.proyectox.ACTION_RESIDUO_RECOGIDO"

        private val HEATMAP_COLORS = intArrayOf(
            Color.argb(0, 0, 255, 255),
            Color.argb(255, 0, 255, 255),
            Color.argb(255, 0, 191, 255),
            Color.argb(255, 0, 128, 255),
            Color.argb(255, 0, 228, 0),
            Color.argb(255, 255, 255, 0),
            Color.argb(255, 255, 128, 0),
            Color.argb(255, 255, 0, 0)
        )

        private val HEATMAP_START_POINTS = floatArrayOf(
            0.0f, 0.10f, 0.20f, 0.35f, 0.50f, 0.70f, 0.85f, 1.0f
        )

        private val GRADIENT = Gradient(HEATMAP_COLORS, HEATMAP_START_POINTS)
    }

    private var googleMap: GoogleMap? = null
    private var tileOverlay: TileOverlay? = null
    private var heatmapProvider: HeatmapTileProvider? = null

    private val CENTER_LEON = LatLng(21.1220, -101.6750)

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshHeatmap()
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map_heatmap, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(updateReceiver, IntentFilter(ACTION_RESIDUO_RECOGIDO))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(updateReceiver)
    }

    // ─── Map Ready ────────────────────────────────────────────────────────────

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d("MapHeatmap", "onMapReady ejecutado")

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER_LEON, 13f))
        map.uiSettings.isZoomControlsEnabled = true

        habilitarUbicacion()
        agregarPuntosAcopioFicticios()
        cargarPendientesYCrearHeatmap()
    }

    // ─── Ubicación ────────────────────────────────────────────────────────────

    private fun habilitarUbicacion() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    // ─── Puntos de Acopio (Markers estáticos) ────────────────────────────────

    private fun agregarPuntosAcopioFicticios() {
        val puntos = listOf(
            Triple(LatLng(21.1245, -101.6697), "Punto de Acopio 1", "Col. Centro"),
            Triple(LatLng(21.1382, -101.6767), "Punto de Acopio 2", "Col. Medina"),
            Triple(LatLng(21.1098, -101.6730), "Punto de Acopio 3", "Col. San Francisco")
        )

        // ✅ Conversión correcta de vector drawable a bitmap
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_punto_acopio)!!
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)

        puntos.forEach { (latLng, titulo, snippet) ->
            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(titulo)
                    .snippet(snippet)
                    .icon(icon)
                    .zIndex(1.0f)
            )
        }
    }

    // ─── Heatmap de Residuos ──────────────────────────────────────────────────

    private fun cargarPendientesYCrearHeatmap() {
        Log.d("MapHeatmap", "Llamando a getPendientes...")
        RetrofitClient.instance.getPendientes().enqueue(object : Callback<List<Residuo>> {

            override fun onResponse(
                call: Call<List<Residuo>>,
                response: Response<List<Residuo>>
            ) {
                if (!response.isSuccessful) {
                    Log.w("MapHeatmap", "Respuesta no exitosa: ${response.code()}")
                    return
                }

                val residuos = response.body().orEmpty()

                Log.d("MapHeatmap", "Total residuos recibidos: ${residuos.size}")
                residuos.forEach {
                    Log.d("MapHeatmap", "Residuo → lat: ${it.latitud}, lng: ${it.longitud}")
                }

                val puntosAgrupados = mutableMapOf<String, Pair<LatLng, Double>>()

                residuos.forEach { residuo ->
                    if (residuo.latitud != null && residuo.longitud != null) {
                        val keyLat = "%.4f".format(residuo.latitud)
                        val keyLng = "%.4f".format(residuo.longitud)
                        val key = "$keyLat,$keyLng"

                        val existing = puntosAgrupados[key]
                        if (existing == null) {
                            puntosAgrupados[key] = Pair(
                                LatLng(residuo.latitud, residuo.longitud), 1.0
                            )
                        } else {
                            puntosAgrupados[key] = Pair(existing.first, existing.second + 1.0)
                        }
                    }
                }

                val weightedPoints = puntosAgrupados.values.map { (latLng, weight) ->
                    WeightedLatLng(latLng, weight)
                }

                if (weightedPoints.isNotEmpty()) {
                    crearHeatmap(weightedPoints)
                } else {
                    Log.w("MapHeatmap", "No hay puntos para el heatmap")
                }
            }

            override fun onFailure(call: Call<List<Residuo>>, t: Throwable) {
                Log.e("MapHeatmap", "Error cargando residuos: ${t.message}")
            }
        })
    }

    private fun crearHeatmap(points: List<WeightedLatLng>) {
        tileOverlay?.remove()
        tileOverlay = null

        if (heatmapProvider == null) {
            heatmapProvider = HeatmapTileProvider.Builder()
                .weightedData(points)
                .gradient(GRADIENT)
                .radius(50)
                .opacity(0.8)
                .build()
        } else {
            heatmapProvider!!.setWeightedData(points)
        }

        tileOverlay = googleMap?.addTileOverlay(
            TileOverlayOptions()
                .tileProvider(heatmapProvider!!)
                .zIndex(0.5f)
        )
    }

    // ─── Refresh externo ─────────────────────────────────────────────────────

    fun refreshHeatmap() {
        tileOverlay?.clearTileCache()
        cargarPendientesYCrearHeatmap()
    }
}