package org.utl.proyectox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Recibimos el ROL que mandaste desde el Login
        val rol = intent.getStringExtra("ROL")

        // 1. Configuración inicial según el ROL
        if (rol == "RECOLECTOR") {
            bottomNav.menu.clear()
            bottomNav.inflateMenu(R.menu.bottom_menu_collector)
            // CAMBIO: El recolector ahora inicia en la pantalla del MAPA
            cambiarPantalla(HomeRecolectorFragment())
        } else {
            bottomNav.menu.clear()
            bottomNav.inflateMenu(R.menu.bottom_menu_citizen)
            // El ciudadano inicia en su bienvenida con el botón verde
            cambiarPantalla(HomeCiudadanoFragment())
        }

        // 2. Navegación: ¿A dónde ir cuando tocan un icono?
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Inicio para Ciudadano
                R.id.nav_home -> {
                    cambiarPantalla(HomeCiudadanoFragment())
                    true
                }
                // Inicio para Recolector (Mapa)
                R.id.nav_home_collector -> {
                    cambiarPantalla(HomeRecolectorFragment())
                    true
                }
                R.id.nav_report -> {
                    cambiarPantalla(ReporteFragment())
                    true
                }
                R.id.nav_waste -> {
                    cambiarPantalla(ListaResiduosFragment())
                    true
                }
                R.id.nav_profile -> {
                    cambiarPantalla(PerfilFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun cambiarPantalla(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}