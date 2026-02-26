package org.utl.proyectox.model

data class Residuo(
    val id: Long? = null,
    val tipo: String,
    val descripcion: String,
    val direccion: String,
    val latitud: Double? = null,
    val longitud: Double? = null
)