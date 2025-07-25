package com.fiveminuteswithme

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SonidosActivity : AppCompatActivity() {

    private lateinit var rvSonidos: RecyclerView
    private lateinit var tvTiempo: TextView
    private lateinit var tvSonidoActual: TextView
    private lateinit var fabPlayPause: FloatingActionButton
    private lateinit var seekBarTiempo: SeekBar
    private lateinit var layoutReproductor: LinearLayout
    private lateinit var btnFavorito: ImageButton

    private lateinit var sonidosAdapter: SonidosAdapter
    private val sonidos = mutableListOf<Sonido>()

    private var mediaPlayer: MediaPlayer? = null
    private var timer: CountDownTimer? = null
    private var sonidoSeleccionado: Sonido? = null
    private var isPlaying = false
    private var tiempoSeleccionado = 5 // minutos por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sonidos)

        inicializarVistas()
        configurarSonidos()
        configurarRecyclerView()
        configurarListeners()
        cargarFavoritos()
    }

    private fun inicializarVistas() {
        rvSonidos = findViewById(R.id.rvSonidos)
        tvTiempo = findViewById(R.id.tvTiempo)
        tvSonidoActual = findViewById(R.id.tvSonidoActual)
        fabPlayPause = findViewById(R.id.fabPlayPause)
        seekBarTiempo = findViewById(R.id.seekBarTiempo)
        layoutReproductor = findViewById(R.id.layoutReproductor)
        btnFavorito = findViewById(R.id.btnFavorito)
    }

    private fun configurarSonidos() {
        sonidos.addAll(listOf(
            Sonido(
                id = 1,
                nombre = "Lluvia Suave",
                emoji = "🌧️",
                descripcion = "Gotas suaves que calman el alma",
                color = "#B0C4DE",
                archivo = "lluvia_suave", // Raw resource
                categoria = SonidoCategoria.NATURALEZA
            ),
            Sonido(
                id = 2,
                nombre = "Bosque Sereno",
                emoji = "🌲",
                descripcion = "Pájaros y viento entre los árboles",
                color = "#90EE90",
                archivo = "bosque_sereno",
                categoria = SonidoCategoria.NATURALEZA
            ),
            Sonido(
                id = 3,
                nombre = "Olas del Mar",
                emoji = "🌊",
                descripcion = "El ritmo hipnótico del océano",
                color = "#87CEEB",
                archivo = "olas_mar",
                categoria = SonidoCategoria.NATURALEZA
            ),
            Sonido(
                id = 4,
                nombre = "Piano Dulce",
                emoji = "🎹",
                descripcion = "Melodías que abrazan el corazón",
                color = "#DDA0DD",
                archivo = "piano_dulce",
                categoria = SonidoCategoria.MUSICAL
            ),
            Sonido(
                id = 5,
                nombre = "Café y Lluvia",
                emoji = "☕",
                descripcion = "Ambiente acogedor para reflexionar",
                color = "#D2B48C",
                archivo = "cafe_lluvia",
                categoria = SonidoCategoria.AMBIENTAL
            ),
            Sonido(
                id = 6,
                nombre = "Cuencos Tibetanos",
                emoji = "🔔",
                descripcion = "Vibraciones que equilibran la energía",
                color = "#FFD700",
                archivo = "cuencos_tibetanos",
                categoria = SonidoCategoria.MEDITACION
            ),
            Sonido(
                id = 7,
                nombre = "Fuego Crepitante",
                emoji = "🔥",
                descripcion = "La calidez de una chimenea",
                color = "#FF6347",
                archivo = "fuego_crepitante",
                categoria = SonidoCategoria.AMBIENTAL
            ),
            Sonido(
                id = 8,
                nombre = "Noche de Verano",
                emoji = "🌙",
                descripcion = "Grillos y brisa nocturna",
                color = "#191970",
                archivo = "noche_verano",
                categoria = SonidoCategoria.NATURALEZA
            )
        ))
    }

    private fun configurarRecyclerView() {
        sonidosAdapter = SonidosAdapter(sonidos) { sonido ->
            seleccionarSonido(sonido)
        }

        rvSonidos.apply {
            layoutManager = GridLayoutManager(this@SonidosActivity, 2)
            adapter = sonidosAdapter
        }
    }

    private fun configurarListeners() {
        fabPlayPause.setOnClickListener {
            if (isPlaying) {
                pausarSonido()
            } else {
                reproducirSonido()
            }
        }

        seekBarTiempo.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Convertir de 0-100 a 5-60 minutos
                    tiempoSeleccionado = 5 + (progress * 55 / 100)
                    actualizarTextoTiempo()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnFavorito.setOnClickListener {
            sonidoSeleccionado?.let { sonido ->
                toggleFavorito(sonido)
            }
        }
    }

    private fun seleccionarSonido(sonido: Sonido) {
        detenerSonido()

        sonidoSeleccionado = sonido
        tvSonidoActual.text = "${sonido.emoji} ${sonido.nombre}"

        layoutReproductor.visibility = View.VISIBLE
        actualizarBotonFavorito()

        // Auto-reproducir
        reproducirSonido()
    }

    private fun reproducirSonido() {
        sonidoSeleccionado?.let { sonido ->
            try {
                // En un proyecto real, cargarías el archivo desde res/raw
                // mediaPlayer = MediaPlayer.create(this, resources.getIdentifier(sonido.archivo, "raw", packageName))

                // Por ahora simulamos la reproducción
                isPlaying = true
                fabPlayPause.setImageResource(R.drawable.ic_pause)

                iniciarTimer()

                Toast.makeText(
                    this,
                    "Reproduciendo ${sonido.nombre} por $tiempoSeleccionado minutos 🎧",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Toast.makeText(this, "Error al reproducir sonido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pausarSonido() {
        mediaPlayer?.pause()
        timer?.cancel()

        isPlaying = false
        fabPlayPause.setImageResource(R.drawable.ic_play)
    }

    private fun detenerSonido() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null

        timer?.cancel()
        isPlaying = false
        fabPlayPause.setImageResource(R.drawable.ic_play)
    }

    private fun iniciarTimer() {
        timer?.cancel()

        val tiempoEnMilis = tiempoSeleccionado * 60 * 1000L

        timer = object : CountDownTimer(tiempoEnMilis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutosRestantes = millisUntilFinished / 1000 / 60
                val segundosRestantes = (millisUntilFinished / 1000) % 60
                tvTiempo.text = String.format("%02d:%02d", minutosRestantes, segundosRestantes)
            }

            override fun onFinish() {
                detenerSonido()
                tvTiempo.text = "00:00"

                Toast.makeText(
                    this@SonidosActivity,
                    "Tu momento de paz ha terminado 🌸",
                    Toast.LENGTH_LONG
                ).show()
            }
        }.start()
    }

    private fun actualizarTextoTiempo() {
        if (!isPlaying) {
            tvTiempo.text = String.format("%02d:00", tiempoSeleccionado)
        }
    }

    private fun toggleFavorito(sonido: Sonido) {
        val sharedPref = getSharedPreferences("sonidos_prefs", MODE_PRIVATE)
        val favoritos = sharedPref.getStringSet("favoritos", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (favoritos.contains(sonido.id.toString())) {
            favoritos.remove(sonido.id.toString())
            Toast.makeText(this, "Removido de favoritos", Toast.LENGTH_SHORT).show()
        } else {
            favoritos.add(sonido.id.toString())
            Toast.makeText(this, "Agregado a favoritos 💕", Toast.LENGTH_SHORT).show()
        }

        sharedPref.edit().putStringSet("favoritos", favoritos).apply()
        actualizarBotonFavorito()
    }

    private fun actualizarBotonFavorito() {
        sonidoSeleccionado?.let { sonido ->
            val sharedPref = getSharedPreferences("sonidos_prefs", MODE_PRIVATE)
            val favoritos = sharedPref.getStringSet("favoritos", setOf()) ?: setOf()

            if (favoritos.contains(sonido.id.toString())) {
                btnFavorito.setImageResource(R.drawable.ic_star_filled)
                btnFavorito.setColorFilter(ContextCompat.getColor(this, R.color.accent))
            } else {
                btnFavorito.setImageResource(R.drawable.ic_star)
                btnFavorito.setColorFilter(ContextCompat.getColor(this, R.color.text_secondary))
            }
        }
    }

    private fun cargarFavoritos() {
        val sharedPref = getSharedPreferences("sonidos_prefs", MODE_PRIVATE)
        val favoritos = sharedPref.getStringSet("favoritos", setOf()) ?: setOf()

        sonidos.forEach { sonido ->
            sonido.esFavorito = favoritos.contains(sonido.id.toString())
        }

        sonidosAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerSonido()
    }

    override fun onPause() {
        super.onPause()
        // Opcional: pausar cuando la app va a background
        // pausarSonido()
    }
}