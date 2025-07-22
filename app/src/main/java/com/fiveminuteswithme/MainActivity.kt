package com.fiveminuteswithme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fiveminuteswithme.R

class MainActivity : AppCompatActivity() {

    private lateinit var btnMomentoParaMi: Button
    private lateinit var btnDiario: ImageButton
    private lateinit var btnSonidos: ImageButton
    private lateinit var btnFavoritos: ImageButton
    private lateinit var btnPerfil: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inicializarVistas()
        configurarListeners()
    }

    private fun inicializarVistas() {
        btnMomentoParaMi = findViewById(R.id.btnMomentoParaMi)
        btnDiario = findViewById(R.id.btnDiario)
        btnSonidos = findViewById(R.id.btnSonidos)
        btnFavoritos = findViewById(R.id.btnFavoritos)
        btnPerfil = findViewById(R.id.btnPerfil)
    }

    private fun configurarListeners() {
        // Botón principal - por ahora abre una actividad aleatoria
        btnMomentoParaMi.setOnClickListener {
            val actividades = listOf(
                DiarioActivity::class.java
                // Próximamente: SonidosActivity, MeditacionActivity, etc.
            )

            val actividadAleatoria = actividades.random()
            startActivity(Intent(this, actividadAleatoria))

            // Animación de transición suave
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Navegación inferior
        btnDiario.setOnClickListener {
            startActivity(Intent(this, DiarioActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnSonidos.setOnClickListener {
            // Por implementar
            mostrarProximamente("Sonidos Relajantes")
        }

        btnFavoritos.setOnClickListener {
            // Por implementar
            mostrarProximamente("Tus Favoritos")
        }

        btnPerfil.setOnClickListener {
            // Por implementar
            mostrarProximamente("Tu Espacio Personal")
        }
    }

    private fun mostrarProximamente(seccion: String) {
        Toast.makeText(
            this,
            "$seccion estará disponible pronto 💕",
            Toast.LENGTH_SHORT
        ).show()
    }
}
