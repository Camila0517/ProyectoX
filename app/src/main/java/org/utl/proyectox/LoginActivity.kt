package org.utl.proyectox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)

        // Buscamos tus componentes de diseño por su ID
        val btnEntrar = findViewById<MaterialButton>(R.id.btn_login_submit)
        val etUsuario = findViewById<TextInputEditText>(R.id.et_login_user)

        btnEntrar.setOnClickListener {
            val usuario = etUsuario.text.toString().lowercase()

            // El Intent es el "salto" a la siguiente pantalla (MainActivity)
            val intent = Intent(this, MainActivity::class.java)

            // Pasamos el tipo de usuario a la siguiente pantalla
            if (usuario == "recolector") {
                intent.putExtra("ROL", "RECOLECTOR")
            } else {
                intent.putExtra("ROL", "CIUDADANO")
            }

            startActivity(intent)
            finish() // Cerramos el login para que no puedan regresar con el botón 'atrás'
        }
    }
}