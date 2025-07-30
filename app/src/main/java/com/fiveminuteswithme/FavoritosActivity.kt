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

class FavoritosActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var rvFavoritos: RecyclerView
    private lateinit var tvNoFavoritos: TextView

    private lateinit var diarioAdapter: DiarioAdapter
    private lateinit var sonidosAdapter: SonidosAdapter

    private val entradasFavoritas = mutableListOf<DiarioEntry>()
    private val sonidosFavoritos = mutableListOf<Sonido>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        inicializarVistas()
        configurarTabs()
        cargarFavoritos()
        configurarRecyclerView()
    }

    private fun inicializarVistas() {
        tabLayout = findViewById(R.id.tabLayout)
        rvFavoritos = findViewById(R.id.rvFavoritos)
        tvNoFavoritos = findViewById(R.id.tvNoFavoritos)
    }

    private fun configurarTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("💕 Momentos"))
        tabLayout.addTab(tabLayout.newTab().setText("🎧 Sonidos"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> mostrarMomentosFavoritos()
                    1 -> mostrarSonidosFavoritos()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun configurarRecyclerView() {
        rvFavoritos.layoutManager = LinearLayoutManager(this)
        mostrarMomentosFavoritos() // Mostrar por defecto
    }

    private fun cargarFavoritos() {
        cargarMomentosFavoritos()
        cargarSonidosFavoritos()
    }

    private fun cargarMomentosFavoritos() {
        // Cargar todas las entradas del diario
        val sharedPref = getSharedPreferences("diario_prefs", MODE_PRIVATE)
        val entradasJson = sharedPref.getString("entradas", "[]")

        val type = object : TypeToken<List<DiarioEntry>>() {}.type
        val todasLasEntradas: List<DiarioEntry> = Gson().fromJson(entradasJson, type)

        // Filtrar las más recientes o especiales (ejemplo: últimas 10 entradas positivas)
        entradasFavoritas.clear()
        entradasFavoritas.addAll(
            todasLasEntradas
                .filter { entrada ->
                    entrada.emocion in listOf(
                        Emocion.FELIZ,
                        Emocion.AGRADECIDA,
                        Emocion.TRANQUILA
                    ) || entrada.momento.length > 50 // Entradas más elaboradas
                }
                .take(20) // Máximo 20 favoritas
        )
    }

    private fun cargarSonidosFavoritos() {
        // Cargar sonidos marcados como favoritos
        val sharedPref = getSharedPreferences("sonidos_prefs", MODE_PRIVATE)
        val favoritosIds = sharedPref.getStringSet("favoritos", setOf()) ?: setOf()

        // Crear lista completa de sonidos (igual que en SonidosActivity)
        val todosSonidos = listOf(
            Sonido(1, "Lluvia Suave", "🌧️", "Gotas suaves que calman el alma", "#B0C4DE", "audi1", SonidoCategoria.NATURALEZA),
            Sonido(2, "Bosque Sereno", "🌲", "Pájaros y viento entre los árboles", "#90EE90", "audi2", SonidoCategoria.NATURALEZA),
            Sonido(3, "Olas del Mar", "🌊", "El ritmo hipnótico del océano", "#87CEEB", "audi3", SonidoCategoria.NATURALEZA),
            Sonido(4, "Piano Dulce", "🎹", "Melodías que abrazan el corazón", "#DDA0DD", "audi4", SonidoCategoria.MUSICAL),
            Sonido(5, "Café y Lluvia", "☕", "Ambiente acogedor para reflexionar", "#D2B48C", "audi5", SonidoCategoria.AMBIENTAL),
            Sonido(6, "Cuencos Tibetanos", "🔔", "Vibraciones que equilibran la energía", "#FFD700", "audi6", SonidoCategoria.MEDITACION),
            Sonido(7, "Fuego Crepitante", "🔥", "La calidez de una chimenea", "#FF6347", "audi7", SonidoCategoria.AMBIENTAL),
            Sonido(8, "Noche de Verano", "🌙", "Grillos y brisa nocturna", "#191970", "audi8", SonidoCategoria.NATURALEZA)
        )

        sonidosFavoritos.clear()
        sonidosFavoritos.addAll(
            todosSonidos.filter { sonido ->
                favoritosIds.contains(sonido.id.toString())
            }.also { sonidos ->
                sonidos.forEach { it.esFavorito = true }
            }
        )
    }

    private fun mostrarMomentosFavoritos() {
        if (entradasFavoritas.isEmpty()) {
            rvFavoritos.visibility = View.GONE
            tvNoFavoritos.visibility = View.VISIBLE
            tvNoFavoritos.text = "Aún no tienes momentos especiales\n¡Escribe en tu diario para crear recuerdos hermosos! 💕"
        } else {
            rvFavoritos.visibility = View.VISIBLE
            tvNoFavoritos.visibility = View.GONE

            diarioAdapter = DiarioAdapter(entradasFavoritas) { entrada ->
                // Mostrar detalle de la entrada
                mostrarDetalleEntrada(entrada)
            }
            rvFavoritos.adapter = diarioAdapter
        }
    }

    private fun mostrarSonidosFavoritos() {
        if (sonidosFavoritos.isEmpty()) {
            rvFavoritos.visibility = View.GONE
            tvNoFavoritos.visibility = View.VISIBLE
            tvNoFavoritos.text = "Aún no tienes sonidos favoritos\n¡Explora la sección de sonidos y marca tus preferidos! 🎧"
        } else {
            rvFavoritos.visibility = View.VISIBLE
            tvNoFavoritos.visibility = View.GONE

            sonidosAdapter = SonidosAdapter(sonidosFavoritos) { sonido ->
                // Regresar a SonidosActivity con este sonido seleccionado
                // Por ahora solo mostrar mensaje
                android.widget.Toast.makeText(
                    this,
                    "Reproduciendo ${sonido.nombre} 🎧",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            rvFavoritos.adapter = sonidosAdapter
        }
    }

    private fun mostrarDetalleEntrada(entrada: DiarioEntry) {
        val fechaFormateada = java.text.SimpleDateFormat("d 'de' MMMM, HH:mm", java.util.Locale("es", "ES"))
            .format(entrada.fecha)

        android.app.AlertDialog.Builder(this)
            .setTitle("${entrada.emocion.emoji} ${entrada.emocion.nombre}")
            .setMessage("$fechaFormateada\n\n${entrada.momento}")
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Recargar favoritos por si han cambiado
        cargarFavoritos()

        // Refrescar la vista actual
        when (tabLayout.selectedTabPosition) {
            0 -> mostrarMomentosFavoritos()
            1 -> mostrarSonidosFavoritos()
        }
    }
}