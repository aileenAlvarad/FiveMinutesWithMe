package com.fiveminuteswithme

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AnalisisEmocionalActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var tvResumenSemanal: TextView
    private lateinit var tvInsightPrincipal: TextView
    private lateinit var rvPatrones: RecyclerView
    private lateinit var tvNoData: TextView

    private lateinit var patronesAdapter: PatronesAdapter
    private lateinit var analizador: AnalizadorEmocional

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analisis_emocional)

        inicializarVistas()
        configurarAnalizador()
        configurarTabs()
        configurarRecyclerView()
        mostrarAnalisisSemanal()
    }

    private fun inicializarVistas() {
        tabLayout = findViewById(R.id.tabLayoutAnalisis)
        tvResumenSemanal = findViewById(R.id.tvResumenSemanal)
        tvInsightPrincipal = findViewById(R.id.tvInsightPrincipal)
        rvPatrones = findViewById(R.id.rvPatrones)
        tvNoData = findViewById(R.id.tvNoDataAnalisis)
    }

    private fun configurarAnalizador() {
        analizador = AnalizadorEmocional(this)
    }

    private fun configurarTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("📊 Esta Semana"))
        tabLayout.addTab(tabLayout.newTab().setText("📈 Este Mes"))
        tabLayout.addTab(tabLayout.newTab().setText("💡 Insights"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> mostrarAnalisisSemanal()
                    1 -> mostrarAnalisisMensual()
                    2 -> mostrarInsightsPersonalizados()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun configurarRecyclerView() {
        patronesAdapter = PatronesAdapter()

        rvPatrones.apply {
            layoutManager = LinearLayoutManager(this@AnalisisEmocionalActivity)
            adapter = patronesAdapter
        }
    }

    private fun mostrarAnalisisSemanal() {
        val analisis = analizador.analizarUltimaSemana()

        if (analisis.entradas.isEmpty()) {
            mostrarMensajeSinDatos("Esta semana aún no tienes datos suficientes\n¡Sigue registrando tus momentos para ver patrones! 🌸")
            return
        }

        mostrarDatos()

        // Resumen semanal
        val resumen = generarResumenSemanal(analisis)
        tvResumenSemanal.text = resumen

        // Insight principal
        val insight = generarInsightSemanal(analisis)
        tvInsightPrincipal.text = insight

        // Patrones detallados
        val patrones = generarPatronesSemana(analisis)
        patronesAdapter.actualizarPatrones(patrones)
    }

    private fun mostrarAnalisisMensual() {
        val analisis = analizador.analizarUltimoMes()

        if (analisis.entradas.isEmpty()) {
            mostrarMensajeSinDatos("Este mes aún no tienes datos suficientes\n¡Continúa tu práctica de autocuidado! 💕")
            return
        }

        mostrarDatos()

        val resumen = generarResumenMensual(analisis)
        tvResumenSemanal.text = resumen

        val insight = generarInsightMensual(analisis)
        tvInsightPrincipal.text = insight

        val patrones = generarPatronesMes(analisis)
        patronesAdapter.actualizarPatrones(patrones)
    }

    private fun mostrarInsightsPersonalizados() {
        val insights = analizador.generarInsightsPersonalizados()

        if (insights.isEmpty()) {
            mostrarMensajeSinDatos("Necesitamos más datos para generar insights personalizados\n¡Sigue usando la app para descubrir patrones únicos! ✨")
            return
        }

        mostrarDatos()

        tvResumenSemanal.text = "💡 Insights Personalizados"
        tvInsightPrincipal.text = insights.first().descripcion

        val patronesInsights = insights.map { insight ->
            PatronEmocional(
                titulo = insight.titulo,
                descripcion = insight.descripcion,
                tipo = TipoPatron.INSIGHT,
                intensidad = insight.importancia,
                recomendacion = insight.recomendacion
            )
        }

        patronesAdapter.actualizarPatrones(patronesInsights)
    }

    private fun generarResumenSemanal(analisis: AnalisisEmocional): String {
        val emocionMasFrec = analisis.emocionMasFrecuente
        val promedioDiario = analisis.entradas.size / 7f
        val diasActivos = analisis.diasConActividad

        return """
            📅 Últimos 7 días
            🎭 Emoción predominante: ${emocionMasFrec?.emoji} ${emocionMasFrec?.nombre ?: "Sin datos"}
            📝 Promedio de registros: ${String.format("%.1f", promedioDiario)} por día
            ✨ Días activos: $diasActivos de 7
        """.trimIndent()
    }

    private fun generarInsightSemanal(analisis: AnalisisEmocional): String {
        val insights = listOf(
            "Tu consistencia en el autocuidado esta semana ha sido admirable 🌟",
            "Has mostrado una hermosa variedad emocional esta semana 🌈",
            "Tus momentos de reflexión están creando patrones positivos 💖",
            "La frecuencia de tus registros muestra tu compromiso contigo misma ✨"
        )
        return insights.random()
    }

    private fun generarPatronesSemana(analisis: AnalisisEmocional): List<PatronEmocional> {
        val patrones = mutableListOf<PatronEmocional>()

        // Patrón de consistencia
        val consistencia = analisis.diasConActividad / 7f
        patrones.add(
            PatronEmocional(
                titulo = "Consistencia",
                descripcion = "Has estado activa ${analisis.diasConActividad} de 7 días (${String.format("%.0f", consistencia * 100)}%)",
                tipo = TipoPatron.CONSISTENCIA,
                intensidad = consistencia,
                recomendacion = if (consistencia > 0.7) "¡Excelente ritmo! Sigue así 🌟" else "Intenta ser más constante, poco a poco 🌱"
            )
        )

        // Patrón emocional
        analisis.emocionMasFrecuente?.let { emocion ->
            patrones.add(
                PatronEmocional(
                    titulo = "Estado Emocional",
                    descripcion = "Tu emoción más frecuente fue ${emocion.emoji} ${emocion.nombre}",
                    tipo = TipoPatron.EMOCIONAL,
                    intensidad = 0.8f,
                    recomendacion = obtenerRecomendacionPorEmocion(emocion)
                )
            )
        }

        // Patrón temporal (si hay datos de hora)
        if (analisis.entradas.isNotEmpty()) {
            val horaFavorita = analisis.entradas
                .map { Calendar.getInstance().apply { time = it.fecha }.get(Calendar.HOUR_OF_DAY) }
                .groupingBy { it }
                .eachCount()
                .maxByOrNull { it.value }?.key

            horaFavorita?.let { hora ->
                patrones.add(
                    PatronEmocional(
                        titulo = "Momento Favorito",
                        descripcion = "Sueles conectar contigo alrededor de las $hora:00",
                        tipo = TipoPatron.TEMPORAL,
                        intensidad = 0.6f,
                        recomendacion = "Aprovecha esa hora para tus momentos de autocuidado 🕐"
                    )
                )
            }
        }

        return patrones
    }

    private fun generarResumenMensual(analisis: AnalisisEmocional): String {
        val totalEntradas = analisis.entradas.size
        val diasActivos = analisis.diasConActividad
        val emocionesUnicas = analisis.entradas.map { it.emocion }.distinct().size

        return """
            📅 Último mes
            📊 Total de registros: $totalEntradas
            🌟 Días activos: $diasActivos
            🎭 Emociones experimentadas: $emocionesUnicas diferentes
        """.trimIndent()
    }

    private fun generarInsightMensual(analisis: AnalisisEmocional): String {
        return when {
            analisis.entradas.size > 20 -> "¡Increíble! Has mantenido una práctica muy consistente este mes 🌟"
            analisis.entradas.size > 10 -> "Estás desarrollando una hermosa rutina de autocuidado 💕"
            analisis.entradas.size > 5 -> "Cada registro es un paso hacia mayor autoconocimiento ✨"
            else -> "Estás comenzando tu viaje de autoconocimiento. ¡Cada momento cuenta! 🌱"
        }
    }

    private fun generarPatronesMes(analisis: AnalisisEmocional): List<PatronEmocional> {
        // Similar a los patrones semanales pero con métricas mensuales
        return generarPatronesSemana(analisis).map { patron ->
            patron.copy(
                descripcion = patron.descripcion.replace("esta semana", "este mes")
                    .replace("7 días", "30 días")
            )
        }
    }

    private fun obtenerRecomendacionPorEmocion(emocion: Emocion): String {
        return when (emocion) {
            Emocion.FELIZ -> "¡Qué hermoso! Aprovecha esta energía positiva para crear más momentos especiales 🌸"
            Emocion.TRANQUILA -> "Tu serenidad es un regalo. Sigue cultivando esos momentos de paz 🌙"
            Emocion.TRISTE -> "Tus emociones son válidas. Considera actividades de compasión hacia ti misma 🌧️"
            Emocion.ANSIOSA -> "Respira profundo. Las técnicas de relajación pueden ayudarte 🌊"
            Emocion.AGRADECIDA -> "La gratitud es medicina para el alma. ¡Sigue cultivándola! ✨"
            Emocion.NEUTRAL -> "Está bien estar en calma. Puedes explorar qué te trae más alegría ☁️"
        }
    }

    private fun mostrarDatos() {
        rvPatrones.visibility = View.VISIBLE
        tvNoData.visibility = View.GONE
    }

    private fun mostrarMensajeSinDatos(mensaje: String) {
        rvPatrones.visibility = View.GONE
        tvNoData.visibility = View.VISIBLE
        tvNoData.text = mensaje
    }
}

// Clases de datos para el análisis
data class AnalisisEmocional(
    val entradas: List<DiarioEntry>,
    val emocionMasFrecuente: Emocion?,
    val diasConActividad: Int,
    val promedioEstadoAnimo: Float,
    val tendencia: TendenciaEmocional
)

data class PatronEmocional(
    val titulo: String,
    val descripcion: String,
    val tipo: TipoPatron,
    val intensidad: Float, // 0.0 a 1.0
    val recomendacion: String
)

data class InsightPersonalizado(
    val titulo: String,
    val descripcion: String,
    val importancia: Float,
    val recomendacion: String
)

enum class TipoPatron {
    CONSISTENCIA,
    EMOCIONAL,
    TEMPORAL,
    INSIGHT
}

enum class TendenciaEmocional {
    MEJORANDO,
    ESTABLE,
    NECESITA_ATENCION
}