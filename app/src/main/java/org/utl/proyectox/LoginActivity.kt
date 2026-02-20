package org.utl.proyectox

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.utl.proyectox.model.LoginRequest
import org.utl.proyectox.model.UsuarioDTO
import org.utl.proyectox.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)

        val btnEntrar = findViewById<MaterialButton>(R.id.btn_login_submit)
        val etEmail = findViewById<TextInputEditText>(R.id.et_login_user)
        val etPassword = findViewById<TextInputEditText>(R.id.et_login_pass)

        btnEntrar.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)

            RetrofitClient.instance.login(request)
                .enqueue(object : Callback<UsuarioDTO> {

                    override fun onResponse(
                        call: Call<UsuarioDTO>,
                        response: Response<UsuarioDTO>
                    ) {
                        if (response.isSuccessful) {

                            val usuario = response.body()

                            if (usuario != null) {

                                val sharedPref = getSharedPreferences("sesion", MODE_PRIVATE)
                                val editor = sharedPref.edit()

                                editor.putLong("USUARIO_ID", usuario.id)
                                editor.putString("ROL", usuario.rol)
                                editor.apply()

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Credenciales incorrectas",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<UsuarioDTO>, t: Throwable) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error de conexión con el servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}