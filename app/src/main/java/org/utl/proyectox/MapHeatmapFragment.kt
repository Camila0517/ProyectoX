package org.utl.proyectox.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import com.google.maps.android.heatmaps.HeatmapTileProvider
import org.utl.proyectox.R
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapHeatmapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val ACTION_RESIDUO_RECOGIDO = "org.utl.proyectox.ACTION_RESIDUO_RECOGIDO"
    }

    private var googleMap: GoogleMap? = null
    private var tileOverlay: TileOverlay? = null

    private val CENTER_LEON = LatLng(21.1220, -101.6750)

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshHeatmap()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map_heatmap, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(updateReceiver, IntentFilter(ACTION_RESIDUO_RECOGIDO))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(updateReceiver)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER_LEON, 12f))
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        agregarPuntosAcopioFicticios()
        habilitarUbicacion()
        cargarPendientesYCrearHeatmap()
    }

    private fun habilitarUbicacion() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    private fun agregarPuntosAcopioFicticios() {
        val puntos = listOf(
            LatLng(21.1245, -101.6697),
            LatLng(21.1382, -101.6767),
            LatLng(21.1098, -101.6730)
        )

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_punto_acopio)
        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)

        puntos.forEachIndexed { i, latLng ->
            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Punto de acopio ${i + 1}")
                    .icon(icon)
            )
        }
    }

    private fun cargarPendientesYCrearHeatmap() {
        RetrofitClient.instance.getPendientes().enqueue(object : Callback<List<Residuo>> {
            override fun onResponse(call: Call<List<Residuo>>, response: Response<List<Residuo>>) {
                if (!response.isSuccessful) return

                val puntos = response.body().orEmpty().mapNotNull {
                    if (it.latitud != null && it.longitud != null)
                        LatLng(it.latitud, it.longitud)
                    else null
                }

                crearHeatmap(puntos)
            }

            override fun onFailure(call: Call<List<Residuo>>, t: Throwable) {
                Log.e("MapHeatmap", "Error: ${t.message}")
            }
        })
    }

    private fun crearHeatmap(points: List<LatLng>) {
        tileOverlay?.remove()

        val provider = HeatmapTileProvider.Builder()
            .data(points)
            .build()

        tileOverlay = googleMap?.addTileOverlay(
            TileOverlayOptions().tileProvider(provider)
        )
    }

    fun refreshHeatmap() {
        cargarPendientesYCrearHeatmap()
    }
}