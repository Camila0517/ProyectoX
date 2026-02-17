package org.utl.proyectox

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class ListaResiduosFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Conectamos con tu lista para el recolector
        return inflater.inflate(R.layout.layout_waste_list, container, false)
    }
}