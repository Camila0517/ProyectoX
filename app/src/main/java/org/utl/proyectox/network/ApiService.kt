package org.utl.proyectox.network

import org.utl.proyectox.model.Residuo
import org.utl.proyectox.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
interface ApiService {

    @GET("residuos/pendientes")
    suspend fun getPendientes(): List<Residuo>
    @GET("residuos")
    fun getResiduos(): Call<List<Residuo>>

    @POST("usuarios/registro")
    fun registrar(@Body usuario: Usuario): Call<Usuario>

    @POST("usuarios/login")
    fun login(@Body usuario: Usuario): Call<Usuario>
}
