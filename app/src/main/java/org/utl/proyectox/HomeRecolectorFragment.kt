package org.utl.proyectox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeRecolectorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Inflamos el diseño que tiene el placeholder del mapa
        val view = inflater.inflate(R.layout.fragment_home_collector, container, false)

        // -------------------------------------------------------------------------
        // TODO IT/Software:
        // 1. Configurar el SDK de Google Maps (API Key en el Manifest).
        // 2. Implementar 'OnMapReadyCallback' para cargar el mapa en el contenedor.
        // 3. Consultar Firestore para traer los puntos (marcadores) de basura reportada.
        // 4. Centrar la cámara en la ubicación actual del recolector.
        // -------------------------------------------------------------------------

        return view
    }
}