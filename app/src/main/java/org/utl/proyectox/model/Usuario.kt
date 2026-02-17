package org.utl.proyectox.model

data class Usuario(
    val id: Int? = null,
    val nombre: String,
    val email: String,
    val password: String? = null,
    val rol: String
)
