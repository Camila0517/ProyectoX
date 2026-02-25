package org.utl.proyectox.model

data class UsuarioRequest(
    val username: String,
    val email: String,
    val password: String,
    val rol: String
)