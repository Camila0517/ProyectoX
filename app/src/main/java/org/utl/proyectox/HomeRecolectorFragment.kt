package org.utl.proyectox

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeRecolectorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home_collector, container, false)

        cargarResiduos()

        return view
    }

    private fun cargarResiduos() {

        RetrofitClient.instance.obtenerResiduos()
            .enqueue(object : Callback<List<Residuo>> {

                override fun onResponse(
                    call: Call<List<Residuo>>,
                    response: Response<List<Residuo>>
                ) {

                    if (response.isSuccessful) {

                        val lista = response.body()

                        Log.d("RECOLECTOR", "Residuos recibidos: ${lista?.size}")

                        lista?.forEach {
                            Log.d("RECOLECTOR", "Tipo: ${it.tipo} - Dirección: ${it.direccion}")
                        }

                    } else {
                        Log.e("RECOLECTOR", "Error ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<Residuo>>, t: Throwable) {
                    Log.e("RECOLECTOR", "Error conexión: ${t.message}")
                }
            })
    }
}