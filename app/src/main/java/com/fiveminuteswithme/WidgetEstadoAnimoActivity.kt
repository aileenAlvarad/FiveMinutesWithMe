package com.fiveminuteswithme

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class WidgetEstadoAnimoActivity : AppCompatActivity() {

    private lateinit var tvFechaHora: TextView
    private lateinit var tvPregunta: TextView
    private lateinit var rgEstadoAnimo: RadioGroup
    private lateinit var etNotasRapidas: EditText
    private lateinit var btnGuardarCheckin: Button
    private lateinit var btnSaltarCheckin: Button
    private lateinit var layoutSugerencia: LinearLayout
    private lateinit var tvSugerencia: TextView
    private lateinit var btnAceptarSugerencia: Button

    private var estadoSeleccionado: EstadoAnimoRapido? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_estado_animo)

        inicializarVistas()
        configurarFechaHora()
        configurarListeners()
        mostrarPreguntaPersonalizada()
    }

    private fun inicializarVistas() {
        tvFechaHora = findViewById(R.id.tvFechaHoraCheckin)
        tvPregunta = findViewById(R.id.tvPreguntaCheckin)
        rgEstadoAnimo = findViewById(R.id.rgEstadoAnimoRapido)
        etNotasRapidas = findViewById(R.id.etNotasRapidas)
        btnGuardarCheckin = findViewById(R.id.btnGuardarCheckin)
        btnSaltarCheckin = findViewById(R.id.btnSaltarCheckin)
        layoutSugerencia = findViewById(R.id.layoutSugerencia)
        tvSugerencia = findViewById(R.id.tvSugerencia)
        btnAceptarSugerencia = findViewById(R.id.btnAceptarSugerencia)
    }

    private fun configurarFechaHora() {
        val ahora = Date()
        val formatoCompleto = SimpleDateFormat("EEEE, d 'de' MMMM - HH:mm", Locale("es", "ES"))
        val fechaFormateada = formatoCompleto.format(ahora)
            .replaceFirstChar { it.uppercase() }

        tvFechaHora.text = fechaFormateada
    }

    private fun mostrarPreguntaPersonalizada() {
        val preguntas = listOf(
            "¿Cómo te encuentras en este momento?",
            "¿Qué tal está tu corazón ahora?",
            "¿Cómo se siente tu alma hoy?",
            "¿Qué necesita tu espíritu ahora?",
            "¿Cómo estás realmente?"
        )
        tvPregunta.text = preguntas.random()
    }

    private fun configurarListeners() {
        rgEstadoAnimo.setOnCheckedChangeListener { _, checkedId ->
            estadoSeleccionado = when (checkedId) {
                R.id.rbRadiante -> EstadoAnimoRapido.RADIANTE
                R.id.rbTranquila -> EstadoAnimoRapido.TRANQUILA
                R.id.rbNeutral -> EstadoAnimoRapido.NEUTRAL
                R.id.rbSensible -> EstadoAnimoRapido.SENSIBLE
                R.id.rbAbrumada -> EstadoAnimoRapido.ABRUMADA
                else -> null
            }

            estadoSeleccionado?.let { estado ->
                actualizarColorBoton(estado)
                mostrarSugerenciaPersonalizada(estado)
            }
        }

        btnGuardarCheckin.setOnClickListener {
            guardarCheckinEmocional()
        }

        btnSaltarCheckin.setOnClickListener {
            finish()
        }

        btnAceptarSugerencia.setOnClickListener {
            ejecutarSugerencia()
        }
    }

    private fun actualizarColorBoton(estado: EstadoAnimoRapido) {
        val colorRes = when (estado) {
            EstadoAnimoRapido.RADIANTE -> R.color.emocion_feliz
            EstadoAnimoRapido.TRANQUILA -> R.color.emocion_tranquila
            EstadoAnimoRapido.NEUTRAL -> R.color.emocion_neutral
            EstadoAnimoRapido.SENSIBLE -> R.color.emocion_triste
            EstadoAnimoRapido.ABRUMADA -> R.color.emocion_ansiosa
        }
        btnGuardarCheckin.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
    }

    private fun mostrarSugerenciaPersonalizada(estado: EstadoAnimoRapido) {
        val sugerencia = when (estado) {
            EstadoAnimoRapido.RADIANTE -> SugerenciaActividad(
                texto = "¡Qué hermoso que te sientas radiante! ✨ ¿Te gustaría compartir esa energía con una afirmación de gratitud?",
                accion = "gratitud"
            )
            EstadoAnimoRapido.TRANQUILA -> SugerenciaActividad(
                texto = "Me encanta que estés en calma 🌙 Un momento de respiración consciente podría mantener esa paz.",
                accion = "respiracion"
            )
            EstadoAnimoRapido.NEUTRAL -> SugerenciaActividad(
                texto = "Está bien estar en neutral ☁️ ¿Qué tal una pequeña pausa para conectar contigo?",
                accion = "checkin"
            )
            EstadoAnimoRapido.SENSIBLE -> SugerenciaActividad(
                texto = "Tu sensibilidad es hermosa 🌧️ ¿Te gustaría escribir lo que sientes en tu diario?",
                accion = "diario"
            )
            EstadoAnimoRapido.ABRUMADA -> SugerenciaActividad(
                texto = "Te comprendo, a veces es mucho 🌊 Un sonido relajante podría ayudarte a encontrar calma.",
                accion = "sonidos"
            )
        }

        tvSugerencia.text = sugerencia.texto
        btnAceptarSugerencia.tag = sugerencia.accion

        layoutSugerencia.visibility = View.VISIBLE
        layoutSugerencia.alpha = 0f
        layoutSugerencia.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun guardarCheckinEmocional() {
        estadoSeleccionado?.let { estado ->
            val notas = etNotasRapidas.text.toString().trim()

            val checkin = CheckinEmocional(
                fecha = Date(),
                estado = estado,
                notas = notas,
                timestamp = System.currentTimeMillis()
            )

            guardarCheckinEnPreferences(checkin)
            actualizarRachaEmocional()

            Toast.makeText(
                this,
                "Check-in guardado con amor 💕",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        } ?: run {
            Toast.makeText(
                this,
                "Por favor, selecciona cómo te sientes 🌸",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun ejecutarSugerencia() {
        val accion = btnAceptarSugerencia.tag as? String ?: return

        when (accion) {
            "gratitud" -> {
                // Abrir actividades con enfoque en gratitud
                val intent = android.content.Intent(this, ActividadesActivity::class.java)
                intent.putExtra("tipo_sugerido", "AFIRMACION")
                intent.putExtra("contexto", "gratitud")
                startActivity(intent)
            }
            "respiracion" -> {
                val intent = android.content.Intent(this, ActividadesActivity::class.java)
                intent.putExtra("tipo_sugerido", "RESPIRACION")
                startActivity(intent)
            }
            "checkin" -> {
                val intent = android.content.Intent(this, ActividadesActivity::class.java)
                intent.putExtra("tipo_sugerido", "JOURNALING")
                intent.putExtra("contexto", "reflexion")
                startActivity(intent)
            }
            "diario" -> {
                val intent = android.content.Intent(this, DiarioActivity::class.java)
                startActivity(intent)
            }
            "sonidos" -> {
                val intent = android.content.Intent(this, SonidosActivity::class.java)
                startActivity(intent)
            }
        }

        finish()
    }

    private fun guardarCheckinEnPreferences(checkin: CheckinEmocional) {
        val sharedPref = getSharedPreferences("checkins_emocionales", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Guardar el último check-in
        editor.putString("ultimo_checkin_fecha", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(checkin.fecha))
        editor.putString("ultimo_checkin_estado", checkin.estado.name)
        editor.putString("ultimo_checkin_notas", checkin.notas)
        editor.putLong("ultimo_checkin_timestamp", checkin.timestamp)

        // Incrementar contador de check-ins
        val totalCheckins = sharedPref.getInt("total_checkins", 0)
        editor.putInt("total_checkins", totalCheckins + 1)

        editor.apply()
    }

    private fun actualizarRachaEmocional() {
        val sharedPref = getSharedPreferences("estadisticas_emocionales", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val ultimaFecha = sharedPref.getString("ultima_fecha_checkin", "")

        if (ultimaFecha != hoy) {
            // Es un día nuevo
            val diasConsecutivos = sharedPref.getInt("dias_consecutivos", 0)
            editor.putInt("dias_consecutivos", diasConsecutivos + 1)
            editor.putString("ultima_fecha_checkin", hoy)
        }

        editor.apply()
    }
}

// Enums y clases de datos para el check-in emocional
enum class EstadoAnimoRapido(
    val emoji: String,
    val descripcion: String,
    val color: String
) {
    RADIANTE("✨", "Radiante y llena de energía", "#FFB6C1"),
    TRANQUILA("🌙", "En calma y centrada", "#E6E6FA"),
    NEUTRAL("☁️", "Neutral, ni bien ni mal", "#F5F5DC"),
    SENSIBLE("🌧️", "Sensible y reflexiva", "#B0C4DE"),
    ABRUMADA("🌊", "Abrumada o ansiosa", "#DDA0DD")
}

data class CheckinEmocional(
    val fecha: Date,
    val estado: EstadoAnimoRapido,
    val notas: String,
    val timestamp: Long
)

data class SugerenciaActividad(
    val texto: String,
    val accion: String // "gratitud", "respiracion", "checkin", "diario", "sonidos"
)