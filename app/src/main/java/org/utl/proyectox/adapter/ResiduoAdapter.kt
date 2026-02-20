package org.utl.proyectox.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.utl.proyectox.R
import org.utl.proyectox.model.Residuo

class ResiduoAdapter(
    private val lista: List<Residuo>,
    private val onRecogerClick: (Long) -> Unit
) : RecyclerView.Adapter<ResiduoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val descripcion: TextView = view.findViewById(R.id.tv_descripcion)
        val direccion: TextView = view.findViewById(R.id.tv_direccion)
        val btnRecoger: Button = view.findViewById(R.id.btn_recoger)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_residuo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val residuo = lista[position]

        holder.descripcion.text = residuo.descripcion
        holder.direccion.text = residuo.direccion

        holder.btnRecoger.setOnClickListener {
            residuo.id?.let { id ->
                onRecogerClick(id)
            }
        }
    }
}