package org.utl.proyectox

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import org.utl.proyectox.model.UsuarioDTO
import org.utl.proyectox.model.UsuarioRequest
import org.utl.proyectox.network.RetrofitClient

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_register)

        // 1. Configuración del menú desplegable
        val opciones = arrayOf("Ciudadano", "Recolector")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.autoComplete_user_type)
        autoComplete.setAdapter(adapter)

        val btnRegistrar = findViewById<MaterialButton>(R.id.btn_register_submit)


        // 2. Activamos el botón de CREAR CUENTA
        btnRegistrar.setOnClickListener {

            val username = findViewById<android.widget.EditText>(R.id.et_register_user).text.toString()
            val email = findViewById<android.widget.EditText>(R.id.et_register_email).text.toString()
            val password = findViewById<android.widget.EditText>(R.id.et_register_pass).text.toString()

            val rol = autoComplete.text.toString().uppercase()


            // 🔎 Validación básica
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || rol.isEmpty()) {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioRequest = UsuarioRequest(
                username = username,
                email = email,
                password = password,
                rol = rol
            )

            val api = RetrofitClient.instance

            api.register(usuarioRequest).enqueue(object : retrofit2.Callback<UsuarioDTO> {
                override fun onResponse(
                    call: retrofit2.Call<UsuarioDTO>,
                    response: retrofit2.Response<UsuarioDTO>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Error al registrar", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<UsuarioDTO>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

    }
}