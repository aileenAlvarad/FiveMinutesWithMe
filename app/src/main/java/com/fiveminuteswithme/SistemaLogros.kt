package com.fiveminuteswithme

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class SistemaLogros(private val context: Context) {

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences("logros_prefs", Context.MODE_PRIVATE)

    fun verificarYDesbloquearLogros(): List<Logro> {
        val logrosDesbloqueados = mutableListOf<Logro>()

        Logro.values().forEach { logro ->
            if (!estaDesbloqueado(logro) && cumpleCondicion(logro)) {
                desbloquearLogro(logro)
                logrosDesbloqueados.add(logro)
            }
        }

        return logrosDesbloqueados
    }

    private fun cumpleCondicion(logro: Logro): Boolean {
        return when (logro) {
            Logro.PRIMER_MOMENTO -> {
                val totalActividades = context.getSharedPreferences("actividades_prefs", Context.MODE_PRIVATE)
                    .getInt("total_completadas", 0)
                totalActividades >= 1
            }

            Logro.PRIMERA_SEMANA -> {
                val diasConsecutivos = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
                    .getInt("dias_consecutivos", 0)
                diasConsecutivos >= 7
            }

            Logro.CORAZON_ABIERTO -> {
                val totalEntradas = obtenerTotalEntradasDiario()
                totalEntradas >= 5
            }

            Logro.EXPLORADORA_SONIDOS -> {
                val sonidosFavoritos = context.getSharedPreferences("sonidos_prefs", Context.MODE_PRIVATE)
                    .getStringSet("favoritos", setOf())?.size ?: 0
                sonidosFavoritos >= 3
            }

            Logro.MAESTRA_RESPIRACION -> {
                val actividadesRespiracion = sharedPref.getInt("actividades_respiracion", 0)
                actividadesRespiracion >= 10
            }

            Logro.ALMA_CONSTANTE -> {
                val diasConsecutivos = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
                    .getInt("dias_consecutivos", 0)
                diasConsecutivos >= 30
            }

            Logro.JARDINERA_EMOCIONES -> {
                val estadosUnicos = obtenerEstadosEmocionalesUnicos()
                estadosUnicos >= 4
            }

            Logro.MINUTOS_DORADOS -> {
                val tiempoTotal = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
                    .getLong("tiempo_total_minutos", 0)
                tiempoTotal >= 100 // 100 minutos de actividades
            }

            Logro.COLECCIONISTA_MOMENTOS -> {
                val totalCheckins = context.getSharedPreferences("checkins_emocionales", Context.MODE_PRIVATE)
                    .getInt("total_checkins", 0)
                totalCheckins >= 15
            }

            Logro.SABIDURIA_INTERIOR -> {
                val frasesPersonales = context.getSharedPreferences("perfil_prefs", Context.MODE_PRIVATE)
                    .getStringSet("frases_personales", setOf())?.size ?: 0
                frasesPersonales >= 5
            }
        }
    }

    private fun desbloquearLogro(logro: Logro) {
        sharedPref.edit()
            .putBoolean("logro_${logro.name}", true)
            .putLong("logro_${logro.name}_fecha", System.currentTimeMillis())
            .apply()
    }

    private fun estaDesbloqueado(logro: Logro): Boolean {
        return sharedPref.getBoolean("logro_${logro.name}", false)
    }

    fun obtenerProgreso(logro: Logro): ProgresoLogro {
        val progreso = when (logro) {
            Logro.PRIMER_MOMENTO -> {
                val completadas = context.getSharedPreferences("actividades_prefs", Context.MODE_PRIVATE)
                    .getInt("total_completadas", 0)
                minOf(completadas, 1)
            }

            Logro.PRIMERA_SEMANA -> {
                val dias = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
                    .getInt("dias_consecutivos", 0)
                minOf(dias, 7)
            }

            Logro.CORAZON_ABIERTO -> {
                val entradas = obtenerTotalEntradasDiario()
                minOf(entradas, 5)
            }

            Logro.EXPLORADORA_SONIDOS -> {
                val favoritos = context.getSharedPreferences("sonidos_prefs", Context.MODE_PRIVATE)
                    .getStringSet("favoritos", setOf())?.size ?: 0
                minOf(favoritos, 3)
            }

            Logro.MAESTRA_RESPIRACION -> {
                val actividades = sharedPref.getInt("actividades_respiracion", 0)
                minOf(actividades, 10)
            }

            Logro.ALMA_CONSTANTE -> {
                val dias = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
                    .getInt("dias_consecutivos", 0)
                minOf(dias, 30)
            }

            Logro.JARDINERA_EMOCIONES -> {
                val estados = obtenerEstadosEmocionalesUnicos()
                minOf(estados, 4)
            }

            Logro.MINUTOS_DORADOS -> {
                val tiempo = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
                    .getLong("tiempo_total_minutos", 0).toInt()
                minOf(tiempo, 100)
            }

            Logro.COLECCIONISTA_MOMENTOS -> {
                val checkins = context.getSharedPreferences("checkins_emocionales", Context.MODE_PRIVATE)
                    .getInt("total_checkins", 0)
                minOf(checkins, 15)
            }

            Logro.SABIDURIA_INTERIOR -> {
                val frases = context.getSharedPreferences("perfil_prefs", Context.MODE_PRIVATE)
                    .getStringSet("frases_personales", setOf())?.size ?: 0
                minOf(frases, 5)
            }
        }

        return ProgresoLogro(
            actual = progreso,
            objetivo = logro.objetivo,
            porcentaje = (progreso * 100 / logro.objetivo).toFloat()
        )
    }

    fun obtenerLogrosDesbloqueados(): List<Logro> {
        return Logro.values().filter { estaDesbloqueado(it) }
    }

    fun registrarActividadCompletada(tipo: TipoActividad) {
        when (tipo) {
            TipoActividad.RESPIRACION -> {
                val actual = sharedPref.getInt("actividades_respiracion", 0)
                sharedPref.edit().putInt("actividades_respiracion", actual + 1).apply()
            }
            TipoActividad.AFIRMACION -> {
                val actual = sharedPref.getInt("actividades_afirmacion", 0)
                sharedPref.edit().putInt("actividades_afirmacion", actual + 1).apply()
            }
            // ... otros tipos
            else -> {}
        }
    }

    // Métodos auxiliares
    private fun obtenerTotalEntradasDiario(): Int {
        val entradasJson = context.getSharedPreferences("diario_prefs", Context.MODE_PRIVATE)
            .getString("entradas", "[]")
        return entradasJson?.split("\"id\":")?.size?.minus(1) ?: 0
    }

    private fun obtenerEstadosEmocionalesUnicos(): Int {
        // Simplificado: contaría estados únicos registrados
        return context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
            .getInt("estados_unicos", 0)
    }
}

enum class Logro(
    val titulo: String,
    val descripcion: String,
    val emoji: String,
    val objetivo: Int,
    val categoria: CategoriaLogro
) {
    PRIMER_MOMENTO(
        titulo = "Primer Momento",
        descripcion = "Completaste tu primera actividad de 5 minutos",
        emoji = "🌸",
        objetivo = 1,
        categoria = CategoriaLogro.PRIMEROS_PASOS
    ),

    PRIMERA_SEMANA(
        titulo = "Una Semana Especial",
        descripcion = "7 días consecutivos cuidando tu bienestar",
        emoji = "✨",
        objetivo = 7,
        categoria = CategoriaLogro.CONSTANCIA
    ),

    CORAZON_ABIERTO(
        titulo = "Corazón Abierto",
        descripcion = "Escribiste 5 entradas en tu diario emocional",
        emoji = "💕",
        objetivo = 5,
        categoria = CategoriaLogro.AUTOEXPRESION
    ),

    EXPLORADORA_SONIDOS(
        titulo = "Exploradora de Sonidos",
        descripcion = "Marcaste 3 sonidos como favoritos",
        emoji = "🎧",
        objetivo = 3,
        categoria = CategoriaLogro.EXPLORACION
    ),

    MAESTRA_RESPIRACION(
        titulo = "Maestra de la Respiración",
        descripcion = "Completaste 10 sesiones de respiración consciente",
        emoji = "🌬️",
        objetivo = 10,
        categoria = CategoriaLogro.MAESTRIA
    ),

    ALMA_CONSTANTE(
        titulo = "Alma Constante",
        descripcion = "30 días consecutivos de autocuidado",
        emoji = "🌟",
        objetivo = 30,
        categoria = CategoriaLogro.CONSTANCIA
    ),

    JARDINERA_EMOCIONES(
        titulo = "Jardinera de Emociones",
        descripcion = "Registraste 4 estados emocionales diferentes",
        emoji = "🌺",
        objetivo = 4,
        categoria = CategoriaLogro.AUTOCONOCIMIENTO
    ),

    MINUTOS_DORADOS(
        titulo = "100 Minutos Dorados",
        descripcion = "Acumulaste 100 minutos de actividades de bienestar",
        emoji = "⏰",
        objetivo = 100,
        categoria = CategoriaLogro.TIEMPO
    ),

    COLECCIONISTA_MOMENTOS(
        titulo = "Coleccionista de Momentos",
        descripcion = "Realizaste 15 check-ins emocionales",
        emoji = "📝",
        objetivo = 15,
        categoria = CategoriaLogro.AUTOCONOCIMIENTO
    ),

    SABIDURIA_INTERIOR(
        titulo = "Sabiduría Interior",
        descripcion = "Creaste 5 frases de inspiración personal",
        emoji = "💫",
        objetivo = 5,
        categoria = CategoriaLogro.CREATIVIDAD
    )
}

enum class CategoriaLogro(val nombre: String, val color: String) {
    PRIMEROS_PASOS("Primeros Pasos", "#FFB6C1"),
    CONSTANCIA("Constancia", "#E6E6FA"),
    AUTOEXPRESION("Autoexpresión", "#F0E68C"),
    EXPLORACION("Exploración", "#DDA0DD"),
    MAESTRIA("Maestría", "#B0C4DE"),
    AUTOCONOCIMIENTO("Autoconocimiento", "#FFB6C1"),
    TIEMPO("Tiempo", "#90EE90"),
    CREATIVIDAD("Creatividad", "#F0E68C")
}

data class ProgresoLogro(
    val actual: Int,
    val objetivo: Int,
    val porcentaje: Float
)