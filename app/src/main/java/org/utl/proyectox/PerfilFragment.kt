package org.utl.proyectox

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class PerfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val etUsuario = view.findViewById<TextInputEditText>(R.id.et_profile_user)
        val etPass = view.findViewById<TextInputEditText>(R.id.et_profile_pass)

        val btnCerrarSesion = view.findViewById<MaterialButton>(R.id.btn_logout)

        // obtener datos guardados
        val prefs = requireActivity().getSharedPreferences("sesion", 0)

        val correo = prefs.getString("CORREO", "")
        val password = prefs.getString("PASSWORD", "")

        // mostrar datos
        etUsuario.setText(correo)
        etPass.setText(password)

        btnCerrarSesion.setOnClickListener {

            // borrar sesión
            prefs.edit().clear().apply()

            val intent = Intent(requireContext(), StartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        return view
    }
}