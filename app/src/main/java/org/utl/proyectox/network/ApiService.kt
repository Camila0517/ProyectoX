package org.utl.proyectox.network

import org.utl.proyectox.model.LoginRequest
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.model.UsuarioDTO
import org.utl.proyectox.model.UsuarioRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
interface ApiService {

    @POST("usuarios/login")
    fun login(@Body request: LoginRequest): Call<UsuarioDTO>

    @POST("usuarios")
    fun register(@Body usuario: UsuarioRequest): Call<UsuarioDTO>


    @GET("residuos/pendientes")
    suspend fun getPendientes(): List<Residuo>

    @GET("residuos")
    fun getResiduos(): Call<List<Residuo>>
}

