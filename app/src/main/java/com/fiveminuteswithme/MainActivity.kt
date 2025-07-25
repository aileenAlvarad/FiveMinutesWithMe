package com.fiveminuteswithme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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
        mostrarMensajeBienvenida()
    }

    private fun inicializarVistas() {
        btnMomentoParaMi = findViewById(R.id.btnMomentoParaMi)
        btnDiario = findViewById(R.id.btnDiario)
        btnSonidos = findViewById(R.id.btnSonidos)
        btnFavoritos = findViewById(R.id.btnFavoritos)
        btnPerfil = findViewById(R.id.btnPerfil)
    }

    private fun configurarListeners() {
        // Botón principal - abre las actividades aleatorias
        btnMomentoParaMi.setOnClickListener {
            startActivity(Intent(this, ActividadesActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Navegación inferior
        btnDiario.setOnClickListener {
            startActivity(Intent(this, DiarioActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnSonidos.setOnClickListener {
            startActivity(Intent(this, SonidosActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnFavoritos.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun mostrarProximamente(seccion: String) {
        Toast.makeText(
            this,
            "$seccion estará disponible pronto 💕",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun mostrarMensajeBienvenida() {
        // Verificar si es la primera vez que abre la app hoy
        val sharedPref = getSharedPreferences("main_prefs", MODE_PRIVATE)
        val ultimaVez = sharedPref.getLong("ultima_apertura", 0)
        val ahora = System.currentTimeMillis()
        val unDia = 24 * 60 * 60 * 1000L

        if (ahora - ultimaVez > unDia) {
            // Es un nuevo día
            val mensajes = listOf(
                "¡Qué hermoso tenerte aquí hoy! 🌸",
                "Tu bienestar es importante, gracias por dedicarte este tiempo 💕",
                "Cada momento que te dedicas cuenta 🌟",
                "¡Bienvenida a tu espacio de paz! 🌙",
                "Hoy es perfecto para conectar contigo 🌺"
            )

            Toast.makeText(this, mensajes.random(), Toast.LENGTH_LONG).show()

            sharedPref.edit().putLong("ultima_apertura", ahora).apply()

            // Verificar logros después del mensaje de bienvenida
            verificarLogrosDelDia()
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar estadísticas si existen
        actualizarEstadisticas()
        // Verificar y mostrar logros nuevos
        verificarLogrosDelDia()
    }

    private fun actualizarEstadisticas() {
        val sharedPref = getSharedPreferences("actividades_prefs", MODE_PRIVATE)
        val actividadesCompletadas = sharedPref.getInt("total_completadas", 0)

        // Opcional: mostrar contador en la UI
        if (actividadesCompletadas > 0) {
            // btnMomentoParaMi.text = "Quiero un momento para mí ($actividadesCompletadas completadas)"
        }
    }

    private fun verificarLogrosDelDia() {
        val sistemaLogros = SistemaLogros(this)
        val nuevosLogros = sistemaLogros.verificarYDesbloquearLogros()

        if (nuevosLogros.isNotEmpty()) {
            mostrarNotificacionLogros(nuevosLogros)
        }
    }

    private fun mostrarNotificacionLogros(logros: List<Logro>) {
        if (logros.size == 1) {
            val logro = logros.first()
            Toast.makeText(
                this,
                "🎉 ¡Nuevo logro desbloqueado! ${logro.emoji} ${logro.titulo}",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this,
                "🎉 ¡${logros.size} logros nuevos desbloqueados! Revisa tu progreso",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}