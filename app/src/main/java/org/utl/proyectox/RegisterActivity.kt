package org.utl.proyectox

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_register)

        // 1. Configuración del menú desplegable
        val opciones = arrayOf("Ciudadano", "Recolector")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
        val autoComplete = findViewById<AutoCompleteTextView>(R.id.autoComplete_user_type)
        autoComplete.setAdapter(adapter)

        // 2. Activamos el botón de CREAR CUENTA
        val btnRegistrar = findViewById<MaterialButton>(R.id.btn_register_submit)

        btnRegistrar.setOnClickListener {
            // -------------------------------------------------------------------------
            // TODO IT/Software:
            // 1. Validar que los campos (et_register_user, email, pass) no estén vacíos.
            // 2. Implementar Firebase Auth para crear el usuario.
            // 3. Guardar el "Tipo de usuario" seleccionado en la base de datos (Firestore/Realtime).
            // -------------------------------------------------------------------------

            // Simulación de éxito para el prototipo
            Toast.makeText(this, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()

            // Navegación de regreso al Login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}