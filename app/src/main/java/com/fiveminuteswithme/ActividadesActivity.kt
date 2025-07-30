package com.fiveminuteswithme

import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar

class ActividadesActivity : AppCompatActivity() {

    private lateinit var tvTituloActividad: TextView
    private lateinit var tvDescripcionActividad: TextView
    private lateinit var tvInstrucciones: TextView
    private lateinit var tvTiempo: TextView
    private lateinit var btnComenzar: Button
    private lateinit var btnSiguiente: Button
    private lateinit var layoutAnimacion: LinearLayout
    private lateinit var viewRespiracion: View
    private lateinit var tvAfirmacion: TextView
    private lateinit var etJournaling: EditText
    private lateinit var progressBar: ProgressBar

    private var actividadActual: TipoActividad? = null
    private var timer: CountDownTimer? = null
    private var respiracionAnimator: ObjectAnimator? = null
    private var actividadEnCurso = false

    // Variables para manejo de sonidos
    private var mediaPlayer: MediaPlayer? = null
    private var sonidoSeleccionado: Sonido? = null
    private var isPlayingSound = false
    private val sonidos = mutableListOf<Sonido>()

    companion object {
        private const val TAG = "ActividadesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)

        inicializarVistas()
        configurarListeners()
        configurarSonidos() // Configurar los sonidos disponibles
        seleccionarActividadAleatoria()
    }

    private fun inicializarVistas() {
        tvTituloActividad = findViewById(R.id.tvTituloActividad)
        tvDescripcionActividad = findViewById(R.id.tvDescripcionActividad)
        tvInstrucciones = findViewById(R.id.tvInstrucciones)
        tvTiempo = findViewById(R.id.tvTiempo)
        btnComenzar = findViewById(R.id.btnComenzar)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        layoutAnimacion = findViewById(R.id.layoutAnimacion)
        viewRespiracion = findViewById(R.id.viewRespiracion)
        tvAfirmacion = findViewById(R.id.tvAfirmacion)
        etJournaling = findViewById(R.id.etJournaling)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun configurarListeners() {
        btnComenzar.setOnClickListener {
            if (actividadEnCurso) {
                detenerActividad()
            } else {
                comenzarActividad()
            }
        }

        btnSiguiente.setOnClickListener {
            seleccionarActividadAleatoria()
        }
    }

    private fun configurarSonidos() {
        sonidos.addAll(listOf(
            Sonido(
                id = 1,
                nombre = "Lluvia Suave",
                emoji = "🌧️",
                descripcion = "Gotas suaves que calman el alma",
                color = "#B0C4DE",
                archivo = "audi1",
                categoria = SonidoCategoria.NATURALEZA
            ),
            Sonido(
                id = 2,
                nombre = "Bosque Sereno",
                emoji = "🌲",
                descripcion = "Pájaros y viento entre los árboles",
                color = "#90EE90",
                archivo = "audi2",
                categoria = SonidoCategoria.NATURALEZA
            ),
            Sonido(
                id = 3,
                nombre = "Olas del Mar",
                emoji = "🌊",
                descripcion = "El ritmo hipnótico del océano",
                color = "#87CEEB",
                archivo = "audi3",
                categoria = SonidoCategoria.NATURALEZA
            ),
            Sonido(
                id = 4,
                nombre = "Piano Dulce",
                emoji = "🎹",
                descripcion = "Melodías que abrazan el corazón",
                color = "#DDA0DD",
                archivo = "audi4",
                categoria = SonidoCategoria.MUSICAL
            ),
            Sonido(
                id = 5,
                nombre = "Café y Lluvia",
                emoji = "☕",
                descripcion = "Ambiente acogedor para reflexionar",
                color = "#D2B48C",
                archivo = "audi5",
                categoria = SonidoCategoria.AMBIENTAL
            ),
            Sonido(
                id = 6,
                nombre = "Cuencos Tibetanos",
                emoji = "🔔",
                descripcion = "Vibraciones que equilibran la energía",
                color = "#FFD700",
                archivo = "audi6",
                categoria = SonidoCategoria.MEDITACION
            ),
            Sonido(
                id = 7,
                nombre = "Fuego Crepitante",
                emoji = "🔥",
                descripcion = "La calidez de una chimenea",
                color = "#FF6347",
                archivo = "audi7",
                categoria = SonidoCategoria.AMBIENTAL
            ),
            Sonido(
                id = 8,
                nombre = "Noche de Verano",
                emoji = "🌙",
                descripcion = "Grillos y brisa nocturna",
                color = "#191970",
                archivo = "audi8",
                categoria = SonidoCategoria.NATURALEZA
            )
        ))
    }

    private fun reproducirSonidoAleatorio() {
        if (sonidos.isNotEmpty()) {
            val sonidoAleatorio = sonidos.random()
            sonidoSeleccionado = sonidoAleatorio

            Toast.makeText(
                this,
                "Reproduciendo ${sonidoAleatorio.emoji} ${sonidoAleatorio.nombre}",
                Toast.LENGTH_LONG
            ).show()

            // Iniciar reproducción
            iniciarReproduccionSonido(sonidoAleatorio)

            Log.d(TAG, "Sonido aleatorio seleccionado: ${sonidoAleatorio.nombre}")
        }
    }

    private fun iniciarReproduccionSonido(sonido: Sonido) {
        try {
            // Liberar MediaPlayer anterior si existe
            mediaPlayer?.release()

            // Obtener el ID del recurso
            val resourceId = resources.getIdentifier(sonido.archivo, "raw", packageName)

            if (resourceId == 0) {
                Log.e(TAG, "No se encontró el archivo: ${sonido.archivo}")
                Toast.makeText(this, "Archivo de audio no encontrado: ${sonido.archivo}", Toast.LENGTH_SHORT).show()
                return
            }

            // Crear y configurar MediaPlayer
            mediaPlayer = MediaPlayer.create(this, resourceId)

            mediaPlayer?.let { player ->
                // Configurar para loop continuo
                player.isLooping = true

                // Configurar listener para errores
                player.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Error en MediaPlayer: what=$what, extra=$extra")
                    Toast.makeText(this@ActividadesActivity, "Error al reproducir audio", Toast.LENGTH_SHORT).show()
                    detenerSonido()
                    true
                }

                // Configurar listener para cuando está preparado
                player.setOnPreparedListener {
                    Log.d(TAG, "MediaPlayer preparado para: ${sonido.nombre}")
                }

                // Iniciar reproducción
                player.start()
                isPlayingSound = true

                Log.d(TAG, "Reproduciendo: ${sonido.nombre}")

            } ?: run {
                Log.e(TAG, "No se pudo crear MediaPlayer")
                Toast.makeText(this, "Error al crear reproductor", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al reproducir sonido", e)
            Toast.makeText(this, "Error al reproducir sonido: ${e.message}", Toast.LENGTH_SHORT).show()
            detenerSonido()
        }
    }

    private fun detenerSonido() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                Log.d(TAG, "MediaPlayer liberado")
            }
            mediaPlayer = null
            isPlayingSound = false

        } catch (e: Exception) {
            Log.e(TAG, "Error al detener sonido", e)
        }
    }

    private fun seleccionarActividadAleatoria() {
        detenerActividad()

        val actividades = TipoActividad.values()
        actividadActual = actividades.random()

        configurarActividad()
    }

    private fun configurarActividad() {
        actividadActual?.let { tipo ->
            tvTituloActividad.text = tipo.titulo
            tvDescripcionActividad.text = tipo.descripcion
            tvInstrucciones.text = tipo.instrucciones
            tvTiempo.text = String.format("%02d:00", tipo.duracionMinutos)

            // Resetear visibilidad de elementos
            ocultarTodosLosElementos()

            // Configurar elementos específicos según el tipo
            when (tipo) {
                TipoActividad.RESPIRACION -> {
                    layoutAnimacion.visibility = View.VISIBLE
                    viewRespiracion.visibility = View.VISIBLE
                }
                TipoActividad.AFIRMACION -> {
                    tvAfirmacion.visibility = View.VISIBLE
                    tvAfirmacion.text = obtenerAfirmacionAleatoria()
                }
                TipoActividad.JOURNALING -> {
                    etJournaling.visibility = View.VISIBLE
                    etJournaling.hint = obtenerPreguntaJournaling()
                }
                TipoActividad.SONIDOS1 -> {
                    // Mostrar controles de sonido
                    Toast.makeText(this, "Reproduce un sonido relajante 🎧", Toast.LENGTH_LONG).show()
                }
                TipoActividad.MEDITACION -> {
                    tvAfirmacion.visibility = View.VISIBLE
                    tvAfirmacion.text = obtenerVisualizacionGuiada()
                }
            }

            btnComenzar.text = "Comenzar"
            btnSiguiente.visibility = View.VISIBLE
            progressBar.progress = 0
        }
    }

    private fun comenzarActividad() {
        actividadActual?.let { tipo ->
            actividadEnCurso = true
            btnComenzar.text = "Detener"
            btnSiguiente.visibility = View.GONE

            val duracionMilis = tipo.duracionMinutos * 60 * 1000L

            // Iniciar timer
            timer = object : CountDownTimer(duracionMilis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val minutosRestantes = millisUntilFinished / 1000 / 60
                    val segundosRestantes = (millisUntilFinished / 1000) % 60
                    tvTiempo.text = String.format("%02d:%02d", minutosRestantes, segundosRestantes)

                    // Actualizar progress bar
                    val progreso = ((duracionMilis - millisUntilFinished) * 100 / duracionMilis).toInt()
                    progressBar.progress = progreso
                }

                override fun onFinish() {
                    finalizarActividad()
                }
            }.start()

            // Iniciar animaciones específicas
            when (tipo) {
                TipoActividad.RESPIRACION -> iniciarAnimacionRespiracion()
                TipoActividad.AFIRMACION -> mostrarAfirmacionesProgresivas()
                TipoActividad.SONIDOS1 -> reproducirSonidoAleatorio()
                else -> {}
            }

            Toast.makeText(this, "¡Tu momento especial ha comenzado! 🌸", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detenerActividad() {
        timer?.cancel()
        respiracionAnimator?.cancel()
        detenerSonido() // Detener sonido si está reproduciéndose

        actividadEnCurso = false
        btnComenzar.text = "Comenzar"
        btnSiguiente.visibility = View.VISIBLE
        progressBar.progress = 0

        actividadActual?.let { tipo ->
            tvTiempo.text = String.format("%02d:00", tipo.duracionMinutos)
        }
    }

    private fun finalizarActividad() {
        detenerActividad()

        Toast.makeText(
            this,
            "¡Momento completado! Tu alma te lo agradece 💕",
            Toast.LENGTH_LONG
        ).show()

        // Guardar estadística
        guardarActividadCompletada()
    }

    private fun iniciarAnimacionRespiracion() {
        respiracionAnimator = ObjectAnimator.ofFloat(viewRespiracion, "scaleX", 0.5f, 1.5f).apply {
            duration = 4000 // 4 segundos para inhalar/exhalar
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(viewRespiracion, "scaleY", 0.5f, 1.5f).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun mostrarAfirmacionesProgresivas() {
        // Cambiar afirmación cada 30 segundos
        timer?.let {
            // Implementar cambio de afirmaciones durante la actividad
            val handler = android.os.Handler(mainLooper)
            handler.postDelayed({
                if (actividadEnCurso) {
                    tvAfirmacion.text = obtenerAfirmacionAleatoria()
                    // Animación suave
                    tvAfirmacion.alpha = 0f
                    tvAfirmacion.animate().alpha(1f).setDuration(1000).start()
                }
            }, 30000)
        }
    }

    private fun ocultarTodosLosElementos() {
        layoutAnimacion.visibility = View.GONE
        viewRespiracion.visibility = View.GONE
        tvAfirmacion.visibility = View.GONE
        etJournaling.visibility = View.GONE
    }

    private fun obtenerAfirmacionAleatoria(): String {
        // Usar el sistema de contenido dinámico
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val contexto = when {
            hora < 12 -> "matutino"
            hora >= 18 -> "vespertino"
            else -> "general"
        }
        return ContenidoManager.obtenerAfirmacion(contexto)
    }

    private fun obtenerPreguntaJournaling(): String {
        // Usar sistema dinámico con contexto aleatorio
        val contextos = listOf("reflexion", "autoconocimiento", "relaciones", "metas")
        val contextoAleatorio = contextos.random()
        return ContenidoManager.obtenerPreguntaJournaling(contextoAleatorio)
    }

    private fun obtenerVisualizacionGuiada(): String {
        val visualizacion = ContenidoManager.obtenerVisualizacionGuiada()
        return "${visualizacion.titulo}\n\n${visualizacion.texto}"
    }

    private fun guardarActividadCompletada() {
        val sharedPref = getSharedPreferences("actividades_prefs", MODE_PRIVATE)
        val actividadesCompletadas = sharedPref.getInt("total_completadas", 0)

        sharedPref.edit()
            .putInt("total_completadas", actividadesCompletadas + 1)
            .putLong("ultima_actividad", System.currentTimeMillis())
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerActividad()
    }

    override fun onPause() {
        super.onPause()
        // Opcional: pausar sonido cuando la app va a background
        if (isPlayingSound) {
            mediaPlayer?.pause()
        }
        Log.d(TAG, "Activity pausada")
    }

    override fun onResume() {
        super.onResume()
        // Opcional: reanudar sonido cuando la app vuelve a foreground
        if (isPlayingSound && actividadEnCurso) {
            mediaPlayer?.start()
        }
        Log.d(TAG, "Activity resumida")
    }
}