package org.utl.proyectox.network

import org.utl.proyectox.model.LoginRequest
import org.utl.proyectox.model.Residuo
import org.utl.proyectox.model.UsuarioDTO
import org.utl.proyectox.model.UsuarioRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("usuarios/login")
    fun login(@Body request: LoginRequest): Call<UsuarioDTO>

    @POST("usuarios")
    fun register(@Body usuario: UsuarioRequest): Call<UsuarioDTO>


    @POST("residuos/{usuarioId}")
    fun crearResiduo(
        @Path("usuarioId") usuarioId: Long,
        @Body residuo: Residuo
    ): Call<Residuo>

    @GET("residuos")
    fun obtenerResiduos(): Call<List<Residuo>>

    @GET("residuos/pendientes")
    fun getPendientes(): Call<List<Residuo>>

    @PUT("residuos/{id}/recoger/{usuarioId}")
    fun recogerResiduo(
        @Path("id") residuoId: Long,
        @Path("usuarioId") usuarioId: Long
    ): Call<Residuo>
}

