package org.utl.proyectox.model

data class Residuo(
    val id: Long,
    val tipo: String,
    val descripcion: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val estado: String
)
