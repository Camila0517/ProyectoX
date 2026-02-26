package org.utl.proyectox

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_start)

        // Usamos los IDs exactos de tu XML
        val btnLogin = findViewById<MaterialButton>(R.id.btn_to_login)
        val btnRegister = findViewById<MaterialButton>(R.id.btn_to_register)

        btnLogin.setOnClickListener {
            // Salto a la pantalla de Login que ya funciona
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            // Salto a la pantalla de Registro
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}