package org.utl.proyectox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class HomeCiudadanoFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_citizen, container, false)

        // Buscamos el botón verde redondeado
        val btnNewPost = view.findViewById<MaterialButton>(R.id.btn_new_post)

        btnNewPost.setOnClickListener {
            // Navegamos al formulario de reporte
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReporteFragment())
                .addToBackStack(null)
                .commit()
        }
        return view
    }
}