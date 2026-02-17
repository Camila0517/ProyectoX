package org.utl.proyectox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ReporteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Inflamos el diseño del formulario
        val view = inflater.inflate(R.layout.fragment_report_form, container, false)

        // 2. Buscamos el botón de publicar (asegúrate que el ID en el XML sea btn_publish)
        val btnPublicar = view.findViewById<MaterialButton>(R.id.btn_publish)

        btnPublicar?.setOnClickListener {
            // -------------------------------------------------------------------------
            // TODO IT/Software:
            // 1. Extraer los datos de los EditText (Tipo de residuo, Descripción, Dirección).
            // 2. Enviar los datos a la colección "Reportes" en Firestore.
            // 3. Obtener la ubicación GPS actual para anexarla al reporte automáticamente.
            // -------------------------------------------------------------------------

            // Simulación visual para la presentación
            Toast.makeText(requireContext(), "¡Reporte enviado con éxito!", Toast.LENGTH_SHORT).show()

            // Opcional: Limpiar los campos después de enviar
        }

        return view
    }
}