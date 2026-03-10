package org.utl.proyectox.ui

import android.content.Context
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
            override fun onResponse(call: Call<List<Residuo>>, response: Response<List<Residuo>>) {
                if (response.isSuccessful) {
                    val lista = response.body().orEmpty()
                    // Configuramos el click para que primero marque como recogido
                    adapter = ResiduoAdapter(lista) { residuo ->
                        marcarYAbrirRuta(residuo)
                    }
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(requireContext(), "Error al cargar", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Residuo>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun marcarYAbrirRuta(residuo: Residuo) {
        val sharedPref = requireActivity().getSharedPreferences("sesion", Context.MODE_PRIVATE)
        val usuarioId = sharedPref.getLong("USUARIO_ID", -1L)

        // El error estaba aquí: residuo.id puede ser null, por eso usamos ?: 0L
        apiService.recogerResiduo(residuo.id ?: 0L, usuarioId).enqueue(object : Callback<Residuo> {
            override fun onResponse(call: Call<Residuo>, response: Response<Residuo>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Residuo recogido correctamente", Toast.LENGTH_SHORT).show()
                    abrirRuta(residuo)
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Residuo>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun abrirRuta(residuo: Residuo) {
        val fragment = RutaRecolectorFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}