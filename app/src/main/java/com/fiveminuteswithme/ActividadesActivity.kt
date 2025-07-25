package com.fiveminuteswithme

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)

        inicializarVistas()
        configurarListeners()
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
                TipoActividad.SONIDOS -> {
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
                else -> {}
            }

            Toast.makeText(this, "¡Tu momento especial ha comenzado! 🌸", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detenerActividad() {
        timer?.cancel()
        respiracionAnimator?.cancel()

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
}