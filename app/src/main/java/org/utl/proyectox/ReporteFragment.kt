package org.utl.proyectox

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReporteFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_report_form, container, false)

        val etTipo = view.findViewById<TextInputEditText>(R.id.et_waste_type)
        val etDescripcion = view.findViewById<TextInputEditText>(R.id.et_description)
        val etDireccion = view.findViewById<TextInputEditText>(R.id.et_address)
        val btnPublicar = view.findViewById<MaterialButton>(R.id.btn_publish)

        btnPublicar.setOnClickListener {

            val tipo = etTipo.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()

            if (tipo.isEmpty() || descripcion.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // 🔥 Obtener sesión correctamente
            val sharedPref = requireActivity()
                .getSharedPreferences("sesion", Context.MODE_PRIVATE)

            val usuarioId = sharedPref.getLong("USUARIO_ID", -1L)

            Log.d("DEBUG", "Usuario ID enviado: $usuarioId")

            if (usuarioId == -1L) {
                Toast.makeText(
                    requireContext(),
                    "Usuario no autenticado",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val residuo = Residuo(
                tipo = tipo,
                descripcion = descripcion,
                direccion = direccion
            )

            RetrofitClient.instance
                .crearResiduo(usuarioId, residuo)
                .enqueue(object : Callback<Residuo> {

                    override fun onResponse(
                        call: Call<Residuo>,
                        response: Response<Residuo>
                    ) {
                        if (response.isSuccessful) {

                            Toast.makeText(
                                requireContext(),
                                "¡Reporte enviado con éxito!",
                                Toast.LENGTH_SHORT
                            ).show()

                            etTipo.text?.clear()
                            etDescripcion.text?.clear()
                            etDireccion.text?.clear()

                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error: ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Residuo>, t: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }

        return view
    }
}