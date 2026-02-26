package org.utl.proyectox

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class PerfilFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Inflamos el diseño y lo guardamos en una variable 'view'
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 2. Buscamos el botón de cerrar sesión por su ID
        val btnCerrarSesion = view.findViewById<MaterialButton>(R.id.btn_logout)

        // 3. Programamos la acción del clic
        btnCerrarSesion?.setOnClickListener {
            // Creamos el salto hacia StartActivity
            val intent = Intent(requireContext(), StartActivity::class.java)

            // Estas banderas limpian el historial para que no se pueda regresar al perfil con el botón "atrás"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        return view
    }
}