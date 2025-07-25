package com.fiveminuteswithme

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object ContenidoManager {

    // Afirmaciones expandidas categorizadas
    private val afirmacionesPorCategoria = mapOf(
        "amor_propio" to listOf(
            "Merezco amor y cuidado, especialmente el mío propio 💕",
            "Mi sensibilidad es mi fortaleza, no mi debilidad 🌸",
            "Soy suficiente tal como soy en este momento 🌺",
            "Mi corazón es sabio y me guía hacia lo que necesito 💖",
            "Tengo el derecho de ocupar espacio en este mundo ✨",
            "Mis errores no definen mi valor como persona 🌟",
            "Está bien no ser perfecta, soy humana y hermosa 🌙",
            "Mi voz y mis opiniones importan 🦋",
            "Elijo tratarme con la misma compasión que daría a mi mejor amiga 💝"
        ),
        "autocuidado" to listOf(
            "Cada pequeño paso que doy cuenta y tiene valor ✨",
            "Puedo descansar sin sentir culpa 🕊️",
            "Mis límites son sagrados y los honro 🛡️",
            "Está bien decir 'no' para cuidar mi energía 🌿",
            "Mi bienestar es una prioridad, no un lujo 🌺",
            "Escucho a mi cuerpo y respeto sus necesidades 💫",
            "Tomar tiempo para mí no es egoísmo, es necesario 🌸",
            "Mi paz mental vale más que la aprobación de otros 🕯️"
        ),
        "emociones" to listOf(
            "Está bien no estar bien todo el tiempo 🌙",
            "Mis emociones son válidas y merecen ser sentidas 💭",
            "Puedo sentir tristeza y seguir siendo fuerte 🌧️",
            "Mi sensibilidad me conecta profundamente con el mundo 🌊",
            "Cada emoción tiene un mensaje y la escucho con amor 💜",
            "Llorar es una forma hermosa de liberar y sanar 💧",
            "Mi ansiedad no me define, soy más grande que mis miedos 🦋",
            "Puedo sentir miedo y aún así ser valiente 🔥"
        ),
        "gratitud" to listOf(
            "Encuentro belleza en los pequeños momentos del día 🌻",
            "Agradezco a mi cuerpo por todo lo que hace por mí 🙏",
            "Cada día es un regalo lleno de posibilidades 🎁",
            "Soy afortunada de poder sentir tanto 💖",
            "Mi respiración me recuerda que estoy viva y presente 🌬️",
            "Agradezco mi capacidad de amar profundamente 💕",
            "Hoy elijo ver las bendiciones que me rodean ✨",
            "Mi corazón está lleno de gratitud por este momento 🌟"
        )
    )

    // Preguntas de journaling por categorías
    private val preguntasJournalingPorCategoria = mapOf(
        "reflexion_diaria" to listOf(
            "¿Qué tres cosas puedo agradecer ahora mismo?",
            "¿Cómo me siento en este momento y qué necesito?",
            "¿Qué me ha hecho sonreír hoy?",
            "¿Cuál es mi intención para el resto del día?",
            "¿Qué pequeño acto de amor propio puedo hacer hoy?",
            "¿Cómo puedo ser más gentil conmigo misma?",
            "¿Qué emoción necesita más atención ahora?",
            "¿Qué me está enseñando este día?"
        ),
        "autoconocimiento" to listOf(
            "¿Qué mensaje le daría a mi yo de ayer?",
            "¿Cuándo me siento más yo misma?",
            "¿Qué actividad me hace perder la noción del tiempo?",
            "¿Cuál es mi mayor fortaleza emocional?",
            "¿Qué necesito soltar para sentirme más libre?",
            "¿Cómo ha cambiado mi perspectiva este mes?",
            "¿Qué parte de mí necesita más amor y atención?",
            "¿Cuál es mi manera favorita de expresar creatividad?"
        ),
        "relaciones" to listOf(
            "¿Cómo puedo mostrarme más amor propio hoy?",
            "¿Qué límites necesito establecer para mi bienestar?",
            "¿Cómo me gustaría que me trataran otros?",
            "¿Qué tipo de energía quiero atraer a mi vida?",
            "¿Cómo puedo ser más comprensiva conmigo misma?",
            "¿Qué palabras de aliento necesito escuchar hoy?",
            "¿Cómo puedo honrar mis emociones sin juzgarlas?",
            "¿Qué me haría sentir más conectada conmigo misma?"
        ),
        "suenos_metas" to listOf(
            "¿Qué pequeño paso puedo dar hoy hacia mis sueños?",
            "¿Cómo quiero sentirme al final de este día?",
            "¿Qué versión de mí misma quiero nutrir?",
            "¿Qué me inspira y me da energía?",
            "¿Cuál es una meta que me emociona pensar?",
            "¿Cómo puedo alinear mis acciones con mis valores?",
            "¿Qué aspecto de mi vida quiero celebrar más?",
            "¿Qué legado emocional quiero dejar en mi día?"
        )
    )

    // Visualizaciones guiadas expandidas
    private val visualizacionesGuiadas = listOf(
        VisualizacionGuiada(
            titulo = "El Jardín de tu Corazón",
            duracion = 5,
            texto = """
                Imagina que hay un jardín secreto en tu corazón. Es un lugar donde solo tú puedes entrar.
                
                Camina por el sendero suave. Observa las flores que crecen allí: cada una representa una cualidad hermosa que tienes.
                
                Ve esa rosa rosada: es tu compasión. Esa margarita blanca: tu pureza de intención. Esas violetas: tu sensibilidad única.
                
                Siéntate en el banco de madera bajo el árbol de la sabiduría. Respira el aire perfumado de amor propio.
                
                Aquí estás completamente a salvo. Aquí eres perfectamente amada. Este lugar siempre estará aquí para ti.
            """.trimIndent()
        ),
        VisualizacionGuiada(
            titulo = "El Abrazo de Luz Dorada",
            duracion = 4,
            texto = """
                Imagina una luz dorada y cálida que comienza a brillar en tu corazón.
                
                Esta luz se expande lentamente, llenando todo tu pecho con calidez y amor.
                
                La luz continúa creciendo, envolviendo todo tu cuerpo como el abrazo más amoroso que hayas recibido.
                
                Siente cómo esta luz sana cada parte de ti que necesita sanación, abraza cada parte que necesita amor.
                
                Eres digna de este amor infinito. Eres luz, eres amor, eres perfecta tal como eres.
            """.trimIndent()
        ),
        VisualizacionGuiada(
            titulo = "La Playa de la Paz Interior",
            duracion = 6,
            texto = """
                Te encuentras en una playa hermosa al atardecer. La arena es suave bajo tus pies descalzos.
                
                Las olas llegan gentilmente a la orilla, llevándose con ellas todas tus preocupaciones.
                
                Con cada ola que se retira, sientes cómo se va un peso de tus hombros. Respira profundo.
                
                El sol se pone pintando el cielo de colores pastel, como los que más te gustan.
                
                Aquí, en este momento, no hay nada que tengas que hacer, nada que tengas que ser excepto tú misma.
                
                Permítete simplemente existir en esta paz perfecta.
            """.trimIndent()
        )
    )

    // Mensajes motivacionales por momentos del día
    private val mensajesPorHora = mapOf(
        6 to listOf("Buenos días, hermosa alma 🌅", "Un nuevo día lleno de posibilidades te espera ✨"),
        7 to listOf("¿Qué tal empezar el día con una sonrisa? 😊", "Tu energía matutina es preciosa 🌸"),
        12 to listOf("¿Cómo va tu día? Mereces un momento de pausa 💫", "Hora de mimarte un poquito 💕"),
        18 to listOf("¿Qué tal una pausa antes de que termine el día? 🌙", "Tu tarde merece un momento de calma 🌺"),
        20 to listOf("Tiempo de relajarte y conectar contigo 🕯️", "La noche es perfecta para la introspección ✨"),
        22 to listOf("¿Qué tal prepararte para un descanso reparador? 🌙", "Tu día fue importante, mereces descansar 💤")
    )

    // Función para obtener afirmación según contexto
    fun obtenerAfirmacion(contexto: String = "general"): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when {
            contexto == "matutino" && hora < 12 -> afirmacionesPorCategoria["autocuidado"]?.random()
            contexto == "vespertino" && hora >= 18 -> afirmacionesPorCategoria["gratitud"]?.random()
            contexto == "emocional" -> afirmacionesPorCategoria["emociones"]?.random()
            else -> afirmacionesPorCategoria.values.flatten().random()
        } ?: "Eres exactamente quien necesitas ser en este momento 💕"
    }

    // Función para obtener pregunta de journaling según contexto
    fun obtenerPreguntaJournaling(contexto: String = "general"): String {
        return when (contexto) {
            "reflexion" -> preguntasJournalingPorCategoria["reflexion_diaria"]?.random()
            "autoconocimiento" -> preguntasJournalingPorCategoria["autoconocimiento"]?.random()
            "relaciones" -> preguntasJournalingPorCategoria["relaciones"]?.random()
            "metas" -> preguntasJournalingPorCategoria["suenos_metas"]?.random()
            else -> preguntasJournalingPorCategoria.values.flatten().random()
        } ?: "¿Cómo estás realmente sintiendo este momento?"
    }

    // Función para obtener visualización guiada
    fun obtenerVisualizacionGuiada(): VisualizacionGuiada {
        return visualizacionesGuiadas.random()
    }

    // Función para obtener mensaje según la hora
    fun obtenerMensajePorHora(): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val horaCercana = mensajesPorHora.keys.minByOrNull { kotlin.math.abs(it - hora) } ?: 12
        return mensajesPorHora[horaCercana]?.random() ?: "Tu alma merece un momento de paz 🌸"
    }

    // Función para obtener datos de progreso emocional
    fun obtenerEstadisticasEmocionales(context: Context): EstadisticasEmocionales {
        val sharedPref = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)

        // Simular datos por ahora, en implementación real esto vendría de la base de datos
        return EstadisticasEmocionales(
            emocionMasFrecuente = "Tranquila 🌙",
            diasConsecutivos = sharedPref.getInt("dias_consecutivos", 1),
            totalActividades = sharedPref.getInt("total_actividades", 0),
            tiempoTotal = sharedPref.getLong("tiempo_total_minutos", 0)
        )
    }
}

// Clases de datos para el sistema de contenido
data class VisualizacionGuiada(
    val titulo: String,
    val duracion: Int, // en minutos
    val texto: String
)

data class EstadisticasEmocionales(
    val emocionMasFrecuente: String,
    val diasConsecutivos: Int,
    val totalActividades: Int,
    val tiempoTotal: Long // en minutos
)