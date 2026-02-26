/*package org.utl.proyectox.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.utl.proyectox.R
import org.utl.proyectox.adapter.ResiduoAdapter
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListaResiduosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResiduoAdapter
    private lateinit var apiService: org.utl.proyectox.network.ApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.layout_waste_list,
            container,
            false
        )

        recyclerView = view.findViewById(R.id.recyclerResiduos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        apiService = RetrofitClient.instance

        cargarResiduosPendientes()

        return view
    }

    private fun cargarResiduosPendientes() {

        apiService.getPendientes().enqueue(object : Callback<List<Residuo>> {

            override fun onResponse(
                call: Call<List<Residuo>>,
                response: Response<List<Residuo>>
            ) {

                if (response.isSuccessful) {

                    val lista = response.body().orEmpty()

                    adapter = ResiduoAdapter(lista) { residuoId ->
                        recogerResiduo(residuoId)
                    }

                    recyclerView.adapter = adapter

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar residuos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Residuo>>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Error de conexión",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun recogerResiduo(residuoId: Long) {

        val usuarioId = requireActivity()
            .getSharedPreferences("sesion", Context.MODE_PRIVATE)
            .getLong("USUARIO_ID", -1L)

        apiService.recogerResiduo(residuoId, usuarioId)
            .enqueue(object : Callback<Residuo> {

                override fun onResponse(
                    call: Call<Residuo>,
                    response: Response<Residuo>
                ) {

                    if (response.isSuccessful) {

                        Toast.makeText(
                            requireContext(),
                            "Residuo recogido",
                            Toast.LENGTH_SHORT
                        ).show()

                        // refrescar lista
                        cargarResiduosPendientes()

                        // enviar broadcast local para que el mapa lo reciba y refresque
                        try {
                            val intent = Intent(MapHeatmapFragment.ACTION_RESIDUO_RECOGIDO)
                            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
                        } catch (e: Exception) {
                            // no bloquear la UI por fallo en el broadcast
                        }

                    } else {

                        Toast.makeText(
                            requireContext(),
                            "No autorizado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Residuo>, t: Throwable) {

                    Toast.makeText(
                        requireContext(),
                        "Error de conexión",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}*/
package org.utl.proyectox.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.utl.proyectox.R
import org.utl.proyectox.RutaRecolectorFragment
import org.utl.proyectox.adapter.ResiduoAdapter
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListaResiduosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResiduoAdapter
    private lateinit var apiService: org.utl.proyectox.network.ApiService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.layout_waste_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerResiduos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        apiService = RetrofitClient.instance

        cargarResiduosPendientes()

        return view
    }

    private fun cargarResiduosPendientes() {
        apiService.getPendientes().enqueue(object : Callback<List<Residuo>> {

            override fun onResponse(
                call: Call<List<Residuo>>,
                response: Response<List<Residuo>>
            ) {
                if (response.isSuccessful) {
                    val lista = response.body().orEmpty()

                    // Al presionar Recoger abre la ruta
                    adapter = ResiduoAdapter(lista) { residuo ->
                        abrirRuta(residuo)
                    }
                    recyclerView.adapter = adapter

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar residuos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Residuo>>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Error de conexión",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Abre RutaRecolectorFragment con el residuo seleccionado
    private fun abrirRuta(residuo: Residuo) {
        val fragment = RutaRecolectorFragment.newInstance(residuo)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}