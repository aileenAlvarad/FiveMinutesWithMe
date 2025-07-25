package com.fiveminuteswithme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var btnAnterior: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnComenzar: Button

    private lateinit var onboardingAdapter: OnboardingAdapter
    private var paginaActual = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        inicializarVistas()
        configurarViewPager()
        configurarListeners()
        configurarIndicadores()
    }

    private fun inicializarVistas() {
        viewPager = findViewById(R.id.viewPagerOnboarding)
        tabLayout = findViewById(R.id.tabLayoutOnboarding)
        btnAnterior = findViewById(R.id.btnAnterior)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        btnComenzar = findViewById(R.id.btnComenzar)
    }

    private fun configurarViewPager() {
        onboardingAdapter = OnboardingAdapter(this)
        viewPager.adapter = onboardingAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                paginaActual = position
                actualizarBotones()
            }
        })
    }

    private fun configurarIndicadores() {
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }

    private fun configurarListeners() {
        btnAnterior.setOnClickListener {
            if (paginaActual > 0) {
                viewPager.currentItem = paginaActual - 1
            }
        }

        btnSiguiente.setOnClickListener {
            if (paginaActual < onboardingAdapter.itemCount - 1) {
                viewPager.currentItem = paginaActual + 1
            }
        }

        btnComenzar.setOnClickListener {
            completarOnboarding()
        }
    }

    private fun actualizarBotones() {
        val totalPaginas = onboardingAdapter.itemCount

        btnAnterior.visibility = if (paginaActual > 0) View.VISIBLE else View.INVISIBLE

        if (paginaActual == totalPaginas - 1) {
            btnSiguiente.visibility = View.GONE
            btnComenzar.visibility = View.VISIBLE
        } else {
            btnSiguiente.visibility = View.VISIBLE
            btnComenzar.visibility = View.GONE
        }
    }

    private fun completarOnboarding() {
        // Marcar onboarding como completado
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit()
            .putBoolean("onboarding_completado", true)
            .putLong("fecha_primer_uso", System.currentTimeMillis())
            .apply()

        // Ir a la pantalla principal
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        // Animación de transición
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onBackPressed() {
        if (paginaActual > 0) {
            viewPager.currentItem = paginaActual - 1
        } else {
            super.onBackPressed()
        }
    }
}

// Adapter para las páginas de onboarding
class OnboardingAdapter(private val activity: OnboardingActivity) :
    androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    private val paginas = listOf(
        PaginaOnboarding(
            titulo = "Bienvenida a tu refugio digital",
            subtitulo = "Un espacio diseñado especialmente para ti",
            descripcion = "5 Minutos Conmigo es tu compañera en el viaje hacia el autoconocimiento y el amor propio. Aquí encontrarás herramientas gentiles para cuidar tu bienestar emocional.",
            imagen = "🌸",
            colorFondo = "#FFB6C140"
        ),
        PaginaOnboarding(
            titulo = "Momentos de 5 minutos",
            subtitulo = "Pequeñas pausas, grandes cambios",
            descripcion = "Actividades diseñadas para caber en tu rutina: respiración consciente, afirmaciones amorosas, mini journaling y sonidos relajantes.",
            imagen = "⏰",
            colorFondo = "#E6E6FA40"
        ),
        PaginaOnboarding(
            titulo = "Tu diario emocional",
            subtitulo = "Un espacio para tus pensamientos",
            descripcion = "Registra tus momentos, emociones y reflexiones. Con el tiempo, descubrirás patrones hermosos sobre tu crecimiento personal.",
            imagen = "📝",
            colorFondo = "#F0E68C40"
        ),
        PaginaOnboarding(
            titulo = "Personaliza tu experiencia",
            subtitulo = "Crea tu altar digital",
            descripcion = "Agrega elementos que te inspiren, frases que te motiven, y configura recordatorios que te cuiden sin presionarte.",
            imagen = "✨",
            colorFondo = "#DDA0DD40"
        ),
        PaginaOnboarding(
            titulo = "¿Cómo te gustaría que te llamemos?",
            subtitulo = "Opcional - puedes cambiarlo después",
            descripcion = "",
            imagen = "💕",
            colorFondo = "#FFB6C140",
            tieneInput = true
        )
    )

    inner class OnboardingViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloOnboarding)
        val tvSubtitulo: TextView = itemView.findViewById(R.id.tvSubtituloOnboarding)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionOnboarding)
        val tvImagen: TextView = itemView.findViewById(R.id.tvImagenOnboarding)
        val etNombre: EditText = itemView.findViewById(R.id.etNombreOnboarding)
        val layoutInput: View = itemView.findViewById(R.id.layoutInputOnboarding)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val pagina = paginas[position]

        holder.tvTitulo.text = pagina.titulo
        holder.tvSubtitulo.text = pagina.subtitulo
        holder.tvImagen.text = pagina.imagen

        if (pagina.tieneInput) {
            holder.tvDescripcion.visibility = View.GONE
            holder.layoutInput.visibility = View.VISIBLE

            // Configurar el input para el nombre
            holder.etNombre.hint = "Tu nombre o como te gusta que te llamen..."
            holder.etNombre.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    guardarNombre(holder.etNombre.text.toString().trim())
                }
            }
        } else {
            holder.tvDescripcion.text = pagina.descripcion
            holder.tvDescripcion.visibility = View.VISIBLE
            holder.layoutInput.visibility = View.GONE
        }

        // Configurar color de fondo
        try {
            val color = android.graphics.Color.parseColor(pagina.colorFondo)
            holder.itemView.setBackgroundColor(color)
        } catch (e: Exception) {
            holder.itemView.setBackgroundColor(
                holder.itemView.context.getColor(R.color.background)
            )
        }

        // Animación de entrada
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(600)
            .start()
    }

    private fun guardarNombre(nombre: String) {
        if (nombre.isNotEmpty()) {
            val sharedPref = activity.getSharedPreferences("perfil_prefs", android.content.Context.MODE_PRIVATE)
            sharedPref.edit().putString("nombre_usuario", nombre).apply()
        }
    }

    override fun getItemCount(): Int = paginas.size
}

// Clase de datos para las páginas
data class PaginaOnboarding(
    val titulo: String,
    val subtitulo: String,
    val descripcion: String,
    val imagen: String,
    val colorFondo: String,
    val tieneInput: Boolean = false
)