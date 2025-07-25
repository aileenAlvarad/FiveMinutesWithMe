package com.fiveminuteswithme

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class LogrosActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var rvLogros: RecyclerView
    private lateinit var tvEstadisticasGenerales: TextView
    private lateinit var tvNoLogros: TextView

    private lateinit var logrosAdapter: LogrosAdapter
    private lateinit var sistemaLogros: SistemaLogros

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logros)

        inicializarVistas()
        configurarSistema()
        configurarTabs()
        configurarRecyclerView()
        mostrarEstadisticasGenerales()
        verificarNuevosLogros()
    }

    private fun inicializarVistas() {
        tabLayout = findViewById(R.id.tabLayoutLogros)
        rvLogros = findViewById(R.id.rvLogros)
        tvEstadisticasGenerales = findViewById(R.id.tvEstadisticasGenerales)
        tvNoLogros = findViewById(R.id.tvNoLogros)
    }

    private fun configurarSistema() {
        sistemaLogros = SistemaLogros(this)
    }

    private fun configurarTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("🏆 Desbloqueados"))
        tabLayout.addTab(tabLayout.newTab().setText("🎯 En Progreso"))
        tabLayout.addTab(tabLayout.newTab().setText("📊 Todas"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> mostrarLogrosDesbloqueados()
                    1 -> mostrarLogrosEnProgreso()
                    2 -> mostrarTodosLosLogros()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun configurarRecyclerView() {
        logrosAdapter = LogrosAdapter { logro ->
            mostrarDetalleLogro(logro)
        }

        rvLogros.apply {
            layoutManager = LinearLayoutManager(this@LogrosActivity)
            adapter = logrosAdapter
        }

        // Mostrar desbloqueados por defecto
        mostrarLogrosDesbloqueados()
    }

    private fun mostrarLogrosDesbloqueados() {
        val desbloqueados = sistemaLogros.obtenerLogrosDesbloqueados()

        if (desbloqueados.isEmpty()) {
            rvLogros.visibility = View.GONE
            tvNoLogros.visibility = View.VISIBLE
            tvNoLogros.text = "Aún no has desbloqueado ningún logro\n¡Sigue cuidando tu bienestar para ganar el primero! 🌸"
        } else {
            rvLogros.visibility = View.VISIBLE
            tvNoLogros.visibility = View.GONE

            val logrosConProgreso = desbloqueados.map { logro ->
                LogroConProgreso(
                    logro = logro,
                    progreso = ProgresoLogro(logro.objetivo, logro.objetivo, 100f),
                    estaDesbloqueado = true
                )
            }

            logrosAdapter.actualizarLogros(logrosConProgreso)
        }
    }

    private fun mostrarLogrosEnProgreso() {
        val enProgreso = Logro.values().mapNotNull { logro ->
            val progreso = sistemaLogros.obtenerProgreso(logro)
            if (progreso.actual > 0 && progreso.actual < logro.objetivo) {
                LogroConProgreso(
                    logro = logro,
                    progreso = progreso,
                    estaDesbloqueado = false
                )
            } else null
        }

        if (enProgreso.isEmpty()) {
            rvLogros.visibility = View.GONE
            tvNoLogros.visibility = View.VISIBLE
            tvNoLogros.text = "No tienes logros en progreso actualmente\n¡Explora las diferentes secciones de la app para empezar! ✨"
        } else {
            rvLogros.visibility = View.VISIBLE
            tvNoLogros.visibility = View.GONE
            logrosAdapter.actualizarLogros(enProgreso)
        }
    }

    private fun mostrarTodosLosLogros() {
        val todosLosLogros = Logro.values().map { logro ->
            val progreso = sistemaLogros.obtenerProgreso(logro)
            LogroConProgreso(
                logro = logro,
                progreso = progreso,
                estaDesbloqueado = progreso.actual >= logro.objetivo
            )
        }

        rvLogros.visibility = View.VISIBLE
        tvNoLogros.visibility = View.GONE
        logrosAdapter.actualizarLogros(todosLosLogros)
    }

    private fun mostrarEstadisticasGenerales() {
        val desbloqueados = sistemaLogros.obtenerLogrosDesbloqueados().size
        val total = Logro.values().size
        val porcentaje = if (total > 0) (desbloqueados * 100 / total) else 0

        val estadisticas = """
            🏆 Logros desbloqueados: $desbloqueados de $total
            📈 Progreso general: $porcentaje%
            
            ¡Cada logro celebra tu dedicación al autocuidado! 💕
        """.trimIndent()

        tvEstadisticasGenerales.text = estadisticas
    }

    private fun verificarNuevosLogros() {
        val nuevosLogros = sistemaLogros.verificarYDesbloquearLogros()

        if (nuevosLogros.isNotEmpty()) {
            mostrarDialogoNuevosLogros(nuevosLogros)
        }
    }

    private fun mostrarDialogoNuevosLogros(logros: List<Logro>) {
        val mensaje = if (logros.size == 1) {
            val logro = logros.first()
            "¡Felicidades! Has desbloqueado:\n\n${logro.emoji} ${logro.titulo}\n${logro.descripcion}"
        } else {
            "¡Increíble! Has desbloqueado ${logros.size} logros nuevos:\n\n" +
                    logros.joinToString("\n") { "${it.emoji} ${it.titulo}" }
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("🎉 ¡Nuevo logro!")
            .setMessage(mensaje)
            .setPositiveButton("¡Qué emoción! 💕") { dialog, _ ->
                dialog.dismiss()
                // Actualizar la vista
                when (tabLayout.selectedTabPosition) {
                    0 -> mostrarLogrosDesbloqueados()
                    1 -> mostrarLogrosEnProgreso()
                    2 -> mostrarTodosLosLogros()
                }
                mostrarEstadisticasGenerales()
            }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDetalleLogro(logro: LogroConProgreso) {
        val titulo = "${logro.logro.emoji} ${logro.logro.titulo}"
        val estado = if (logro.estaDesbloqueado) {
            "✅ Desbloqueado"
        } else {
            "Progreso: ${logro.progreso.actual}/${logro.logro.objetivo} (${logro.progreso.porcentaje.toInt()}%)"
        }

        val mensaje = """
            ${logro.logro.descripcion}
            
            Estado: $estado
            Categoría: ${logro.logro.categoria.nombre}
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

// Clase para combinar logro con su progreso
data class LogroConProgreso(
    val logro: Logro,
    val progreso: ProgresoLogro,
    val estaDesbloqueado: Boolean
)