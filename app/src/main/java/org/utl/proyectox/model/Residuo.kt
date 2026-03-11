package org.utl.proyectox.model
import java.io.Serializable
data class Residuo(
    val id: Long? = null,
    val usuarioId: Long? = null,
    val tipo: String,
    val descripcion: String,
    val direccion: String,
    val latitud: Double? = null,
    val longitud: Double? = null
) : Serializable
