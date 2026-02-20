package org.utl.proyectox

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.utl.proyectox.ui.ListaResiduosFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // 🔥 LEEMOS EL ROL DESDE SHARED PREFERENCES
        val sharedPref = getSharedPreferences("sesion", MODE_PRIVATE)
        val rol = sharedPref.getString("ROL", "CIUDADANO")

        // 🔥 Normalizamos por si viene en minúsculas
        val rolNormalizado = rol?.uppercase()

        if (rolNormalizado == "RECOLECTOR") {

            bottomNav.menu.clear()
            bottomNav.inflateMenu(R.menu.bottom_menu_collector)

            cambiarPantalla(HomeRecolectorFragment())

        } else {

            bottomNav.menu.clear()
            bottomNav.inflateMenu(R.menu.bottom_menu_citizen)

            cambiarPantalla(HomeCiudadanoFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    cambiarPantalla(HomeCiudadanoFragment())
                    true
                }

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