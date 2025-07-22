package com.fiveminuteswithme

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class DiarioActivity : AppCompatActivity() {

    private lateinit var etMomento: EditText
    private lateinit var btnGuardar: Button
    private lateinit var rvDiario: RecyclerView
    private lateinit var tvFecha: TextView
    private lateinit var tvNoEntradas: TextView
    private lateinit var fabNuevaEntrada: FloatingActionButton
    private lateinit var layoutNuevaEntrada: LinearLayout
    private lateinit var rgEmociones: RadioGroup

    private var emocionSeleccionada: Emocion = Emocion.NEUTRAL
    private lateinit var diarioAdapter: DiarioAdapter
    private val entradas = mutableListOf<DiarioEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diario)

        inicializarVistas()
        configurarFecha()
        configurarEmociones()
        configurarRecyclerView()
        cargarEntradas()
        configurarListeners()
    }

    private fun inicializarVistas() {
        etMomento = findViewById(R.id.etMomento)
        btnGuardar = findViewById(R.id.btnGuardar)
        rvDiario = findViewById(R.id.rvDiario)
        tvFecha = findViewById(R.id.tvFecha)
        tvNoEntradas = findViewById(R.id.tvNoEntradas)
        fabNuevaEntrada = findViewById(R.id.fabNuevaEntrada)
        layoutNuevaEntrada = findViewById(R.id.layoutNuevaEntrada)
        rgEmociones = findViewById(R.id.rgEmociones)
    }

    private fun configurarFecha() {
        val fechaActual = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
            .format(Date())
            .replaceFirstChar { it.uppercase() }
        tvFecha.text = fechaActual
    }

    private fun configurarEmociones() {
        // Configurar los RadioButtons con las emociones
        rgEmociones.setOnCheckedChangeListener { _, checkedId ->
            emocionSeleccionada = when (checkedId) {
                R.id.rbFeliz -> Emocion.FELIZ
                R.id.rbTranquila -> Emocion.TRANQUILA
                R.id.rbTriste -> Emocion.TRISTE
                R.id.rbAnsiosa -> Emocion.ANSIOSA
                R.id.rbAgradecida -> Emocion.AGRADECIDA
                else -> Emocion.NEUTRAL
            }
            actualizarColorBotonGuardar()
        }
    }

    private fun configurarRecyclerView() {
        diarioAdapter = DiarioAdapter(entradas) { entrada ->
            // Callback para cuando se toca una entrada
            mostrarDetalleEntrada(entrada)
        }

        rvDiario.apply {
            layoutManager = LinearLayoutManager(this@DiarioActivity)
            adapter = diarioAdapter
        }
    }

    private fun configurarListeners() {
        fabNuevaEntrada.setOnClickListener {
            toggleFormularioEntrada()
        }

        btnGuardar.setOnClickListener {
            guardarEntrada()
        }
    }

    private fun toggleFormularioEntrada() {
        if (layoutNuevaEntrada.visibility == View.VISIBLE) {
            layoutNuevaEntrada.visibility = View.GONE
            fabNuevaEntrada.setImageResource(R.drawable.ic_add)
            limpiarFormulario()
        } else {
            layoutNuevaEntrada.visibility = View.VISIBLE
            fabNuevaEntrada.setImageResource(R.drawable.ic_close)
            etMomento.requestFocus()
        }
    }

    private fun guardarEntrada() {
        val textoMomento = etMomento.text.toString().trim()

        // Validaciones
        if (textoMomento.isEmpty()) {
            etMomento.error = "Por favor, escribe algo sobre tu momento 💭"
            return
        }

        if (textoMomento.length < 10) {
            etMomento.error = "Cuéntame un poquito más... 🌸"
            return
        }

        // Crear nueva entrada
        val nuevaEntrada = DiarioEntry(
            id = System.currentTimeMillis(),
            fecha = Date(),
            momento = textoMomento,
            emocion = emocionSeleccionada
        )

        // Agregar al inicio de la lista
        entradas.add(0, nuevaEntrada)

        // Guardar en SharedPreferences
        guardarEntradasEnPreferences()

        // Actualizar UI
        diarioAdapter.notifyItemInserted(0)
        rvDiario.smoothScrollToPosition(0)

        // Ocultar mensaje de no entradas
        if (entradas.isNotEmpty()) {
            tvNoEntradas.visibility = View.GONE
        }

        // Limpiar y ocultar formulario
        toggleFormularioEntrada()

        // Mostrar mensaje de confirmación
        Toast.makeText(
            this,
            "Tu momento ha sido guardado con amor 💕",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun limpiarFormulario() {
        etMomento.text.clear()
        rgEmociones.check(R.id.rbNeutral)
        emocionSeleccionada = Emocion.NEUTRAL
    }

    private fun actualizarColorBotonGuardar() {
        val colorRes = when (emocionSeleccionada) {
            Emocion.FELIZ -> R.color.emocion_feliz
            Emocion.TRANQUILA -> R.color.emocion_tranquila
            Emocion.TRISTE -> R.color.emocion_triste
            Emocion.ANSIOSA -> R.color.emocion_ansiosa
            Emocion.AGRADECIDA -> R.color.emocion_agradecida
            Emocion.NEUTRAL -> R.color.primary
        }
        btnGuardar.backgroundTintList = ContextCompat.getColorStateList(this, colorRes)
    }

    private fun cargarEntradas() {
        val sharedPref = getSharedPreferences("diario_prefs", MODE_PRIVATE)
        val entradasJson = sharedPref.getString("entradas", "[]")

        val type = object : TypeToken<List<DiarioEntry>>() {}.type
        val entradasGuardadas: List<DiarioEntry> = Gson().fromJson(entradasJson, type)

        entradas.clear()
        entradas.addAll(entradasGuardadas)

        // Actualizar UI
        if (entradas.isEmpty()) {
            tvNoEntradas.visibility = View.VISIBLE
        } else {
            tvNoEntradas.visibility = View.GONE
        }

        diarioAdapter.notifyDataSetChanged()
    }

    private fun guardarEntradasEnPreferences() {
        val sharedPref = getSharedPreferences("diario_prefs", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val entradasJson = Gson().toJson(entradas)
        editor.putString("entradas", entradasJson)
        editor.apply()
    }

    private fun mostrarDetalleEntrada(entrada: DiarioEntry) {
        // Aquí podrías mostrar un diálogo con los detalles completos
        // Por ahora solo mostramos un Toast
        val fechaFormateada = SimpleDateFormat("d 'de' MMMM, HH:mm", Locale("es", "ES"))
            .format(entrada.fecha)

        Toast.makeText(
            this,
            "Momento del $fechaFormateada\n${entrada.emocion.emoji}",
            Toast.LENGTH_LONG
        ).show()
    }
}