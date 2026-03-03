package org.utl.proyectox

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Importante: Implementar OnMapReadyCallback
class HomeRecolectorFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private val LEON_GTO = LatLng(21.1222, -101.6823)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_collector, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Buscamos el fragmento del mapa que pusimos en el XML
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_home) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Centrar en León
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LEON_GTO, 12f))

        // Cargar los residuos de la API para poner los marcadores
        cargarResiduos()
    }

    private fun cargarResiduos() {
        RetrofitClient.instance.obtenerResiduos()
            .enqueue(object : Callback<List<Residuo>> {
                override fun onResponse(call: Call<List<Residuo>>, response: Response<List<Residuo>>) {
                    if (response.isSuccessful) {
                        val lista = response.body()
                        lista?.forEach { residuo ->
                            // Si el objeto tiene coordenadas, las ponemos en el mapa
                            if (residuo.latitud != null && residuo.longitud != null) {
                                val pos = LatLng(residuo.latitud, residuo.longitud)
                                mMap?.addMarker(
                                    MarkerOptions()
                                        .position(pos)
                                        .title(residuo.tipo)
                                        .snippet(residuo.direccion)
                                )
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<List<Residuo>>, t: Throwable) {
                    Log.e("RECOLECTOR", "Error: ${t.message}")
                }
            })
    }
}