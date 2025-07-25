package com.fiveminuteswithme

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AnalizadorEmocional(private val context: Context) {

    fun analizarUltimaSemana(): AnalisisEmocional {
        val entradas = obtenerEntradasUltimaSemana()
        return procesarEntradas(entradas)
    }

    fun analizarUltimoMes(): AnalisisEmocional {
        val entradas = obtenerEntradasUltimoMes()
        return procesarEntradas(entradas)
    }

    fun generarInsightsPersonalizados(): List<InsightPersonalizado> {
        val insights = mutableListOf<InsightPersonalizado>()

        // Analizar patrones de uso
        insights.addAll(analizarPatronesUso())

        // Analizar evolución emocional
        insights.addAll(analizarEvolucionEmocional())

        // Analizar preferencias de actividades
        insights.addAll(analizarPreferenciasActividades())

        return insights.sortedByDescending { it.importancia }.take(5)
    }

    private fun obtenerEntradasUltimaSemana(): List<DiarioEntry> {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, -7)
        val fechaLimite = calendario.time

        return obtenerTodasLasEntradas().filter { entrada ->
            entrada.fecha.after(fechaLimite)
        }
    }

    private fun obtenerEntradasUltimoMes(): List<DiarioEntry> {
        val calendario = Calendar.getInstance()
        calendario.add(Calendar.DAY_OF_YEAR, -30)
        val fechaLimite = calendario.time

        return obtenerTodasLasEntradas().filter { entrada ->
            entrada.fecha.after(fechaLimite)
        }
    }

    private fun obtenerTodasLasEntradas(): List<DiarioEntry> {
        val sharedPref = context.getSharedPreferences("diario_prefs", Context.MODE_PRIVATE)
        val entradasJson = sharedPref.getString("entradas", "[]") ?: "[]"

        val type = object : TypeToken<List<DiarioEntry>>() {}.type
        return try {
            Gson().fromJson(entradasJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun procesarEntradas(entradas: List<DiarioEntry>): AnalisisEmocional {
        if (entradas.isEmpty()) {
            return AnalisisEmocional(
                entradas = emptyList(),
                emocionMasFrecuente = null,
                diasConActividad = 0,
                promedioEstadoAnimo = 0f,
                tendencia = TendenciaEmocional.ESTABLE
            )
        }

        val emocionMasFrecuente = entradas
            .groupingBy { it.emocion }
            .eachCount()
            .maxByOrNull { it.value }?.key

        val diasUnicos = entradas
            .map { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.fecha) }
            .distinct()
            .size

        val promedioEstadoAnimo = calcularPromedioEstadoAnimo(entradas)
        val tendencia = calcularTendencia(entradas)

        return AnalisisEmocional(
            entradas = entradas,
            emocionMasFrecuente = emocionMasFrecuente,
            diasConActividad = diasUnicos,
            promedioEstadoAnimo = promedioEstadoAnimo,
            tendencia = tendencia
        )
    }

    private fun calcularPromedioEstadoAnimo(entradas: List<DiarioEntry>): Float {
        val valoresEmocionales = mapOf(
            Emocion.FELIZ to 5f,
            Emocion.AGRADECIDA to 4.5f,
            Emocion.TRANQUILA to 4f,
            Emocion.NEUTRAL to 3f,
            Emocion.TRISTE to 2f,
            Emocion.ANSIOSA to 2.5f
        )

        val suma = entradas.sumOf { valoresEmocionales[it.emocion]?.toDouble() ?: 3.0 }
        return (suma / entradas.size).toFloat()
    }

    private fun calcularTendencia(entradas: List<DiarioEntry>): TendenciaEmocional {
        if (entradas.size < 3) return TendenciaEmocional.ESTABLE

        val entradasOrdenadas = entradas.sortedBy { it.fecha }
        val mitad = entradasOrdenadas.size / 2

        val primeraMetad = entradasOrdenadas.take(mitad)
        val segundaMetad = entradasOrdenadas.drop(mitad)

        val promedioInicial = calcularPromedioEstadoAnimo(primeraMetad)
        val promedioFinal = calcularPromedioEstadoAnimo(segundaMetad)

        val diferencia = promedioFinal - promedioInicial

        return when {
            diferencia > 0.3 -> TendenciaEmocional.MEJORANDO
            diferencia < -0.3 -> TendenciaEmocional.NECESITA_ATENCION
            else -> TendenciaEmocional.ESTABLE
        }
    }

    private fun analizarPatronesUso(): List<InsightPersonalizado> {
        val insights = mutableListOf<InsightPersonalizado>()

        // Analizar consistencia
        val diasConsecutivos = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
            .getInt("dias_consecutivos", 0)

        if (diasConsecutivos >= 7) {
            insights.add(
                InsightPersonalizado(
                    titulo = "Constancia Admirable",
                    descripcion = "Has mantenido una rutina de autocuidado por $diasConsecutivos días consecutivos. Esto muestra tu compromiso genuino contigo misma.",
                    importancia = 0.9f,
                    recomendacion = "Sigue así. La constancia es la clave del crecimiento personal 🌱"
                )
            )
        }

        // Analizar actividades favoritas
        val actividadesCompletadas = context.getSharedPreferences("actividades_prefs", Context.MODE_PRIVATE)
            .getInt("total_completadas", 0)

        if (actividadesCompletadas >= 10) {
            insights.add(
                InsightPersonalizado(
                    titulo = "Exploradora del Bienestar",
                    descripcion = "Has completado $actividadesCompletadas actividades de 5 minutos. Cada una ha sido un regalo para tu alma.",
                    importancia = 0.7f,
                    recomendacion = "Intenta variar entre diferentes tipos de actividades para una experiencia más rica 🌈"
                )
            )
        }

        return insights
    }

    private fun analizarEvolucionEmocional(): List<InsightPersonalizado> {
        val insights = mutableListOf<InsightPersonalizado>()
        val entradas = obtenerTodasLasEntradas()

        if (entradas.size < 5) return insights

        // Analizar diversidad emocional
        val emocionesUnicas = entradas.map { it.emocion }.distinct()

        if (emocionesUnicas.size >= 4) {
            insights.add(
                InsightPersonalizado(
                    titulo = "Riqueza Emocional",
                    descripcion = "Has registrado ${emocionesUnicas.size} emociones diferentes. Tu capacidad de reconocer y nombrar tus sentimientos es una fortaleza.",
                    importancia = 0.8f,
                    recomendacion = "Continúa explorando el espectro completo de tus emociones con compasión 💖"
                )
            )
        }

        // Analizar evolución temporal
        val tendencia = calcularTendencia(entradas)
        when (tendencia) {
            TendenciaEmocional.MEJORANDO -> {
                insights.add(
                    InsightPersonalizado(
                        titulo = "Evolución Positiva",
                        descripcion = "Tus registros muestran una tendencia emocional ascendente. Tu práctica de autocuidado está dando frutos.",
                        importancia = 0.9f,
                        recomendacion = "Celebra este progreso y mantén las prácticas que te están funcionando ✨"
                    )
                )
            }
            TendenciaEmocional.NECESITA_ATENCION -> {
                insights.add(
                    InsightPersonalizado(
                        titulo = "Momento de Mayor Cuidado",
                        descripcion = "Tus últimos registros sugieren que podrías beneficiarte de más momentos de autocuidado.",
                        importancia = 0.8f,
                        recomendacion = "Considera aumentar la frecuencia de tus actividades de bienestar 🌿"
                    )
                )
            }
            else -> {}
        }

        return insights
    }

    private fun analizarPreferenciasActividades(): List<InsightPersonalizado> {
        val insights = mutableListOf<InsightPersonalizado>()

        // Analizar sonidos favoritos
        val sonidosFavoritos = context.getSharedPreferences("sonidos_prefs", Context.MODE_PRIVATE)
            .getStringSet("favoritos", setOf())?.size ?: 0

        if (sonidosFavoritos >= 3) {
            insights.add(
                InsightPersonalizado(
                    titulo = "Conexión Auditiva",
                    descripcion = "Has marcado $sonidosFavoritos sonidos como favoritos. Los sonidos son una puerta poderosa hacia la calma.",
                    importancia = 0.6f,
                    recomendacion = "Experimenta creando playlists personalizadas para diferentes momentos del día 🎧"
                )
            )
        }

        // Analizar frases personales
        val frasesPersonales = context.getSharedPreferences("perfil_prefs", Context.MODE_PRIVATE)
            .getStringSet("frases_personales", setOf())?.size ?: 0

        if (frasesPersonales >= 3) {
            insights.add(
                InsightPersonalizado(
                    titulo = "Sabiduría Personal",
                    descripcion = "Has creado $frasesPersonales frases de inspiración personal. Esto refleja tu crecimiento en autoconocimiento.",
                    importancia = 0.7f,
                    recomendacion = "Revisa tus frases regularmente. Son recordatorios poderosos de tu sabiduría interior 💫"
                )
            )
        }

        return insights
    }
}