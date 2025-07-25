package com.fiveminuteswithme


import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import java.text.SimpleDateFormat
import java.util.*

class PerfilActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var tvBienvenida: TextView
    private lateinit var tvEstadisticas: TextView
    private lateinit var rvAltarDigital: RecyclerView
    private lateinit var rvFrasesInspiracion: RecyclerView
    private lateinit var switchRecordatorios: SwitchMaterial
    private lateinit var tvHoraRecordatorio: TextView
    private lateinit var btnAgregarFrase: Button
    private lateinit var btnCambiarTema: Button
    private lateinit var btnExportarDatos: Button

    private lateinit var altarAdapter: AltarDigitalAdapter
    private lateinit var frasesAdapter: FrasesInspiracionAdapter

    private val elementosAltar = mutableListOf<ElementoAltar>()
    private val frasesPersonales = mutableListOf<String>()
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        inicializarVistas()
        configurarSharedPreferences()
        cargarDatosPersonales()
        configurarRecyclerViews()
        configurarListeners()
        actualizarEstadisticas()
        cargarAltarDigital()
        cargarFrasesPersonales()
    }

    private fun inicializarVistas() {
        etNombre = findViewById(R.id.etNombre)
        tvBienvenida = findViewById(R.id.tvBienvenida)
        tvEstadisticas = findViewById(R.id.tvEstadisticas)
        rvAltarDigital = findViewById(R.id.rvAltarDigital)
        rvFrasesInspiracion = findViewById(R.id.rvFrasesInspiracion)
        switchRecordatorios = findViewById(R.id.switchRecordatorios)
        tvHoraRecordatorio = findViewById(R.id.tvHoraRecordatorio)
        btnAgregarFrase = findViewById(R.id.btnAgregarFrase)
        btnCambiarTema = findViewById(R.id.btnCambiarTema)
        btnExportarDatos = findViewById(R.id.btnExportarDatos)
    }

    private fun configurarSharedPreferences() {
        sharedPref = getSharedPreferences("perfil_prefs", MODE_PRIVATE)
    }

    private fun cargarDatosPersonales() {
        val nombre = sharedPref.getString("nombre_usuario", "")
        val horaRecordatorio = sharedPref.getString("hora_recordatorio", "20:00")
        val recordatoriosActivos = sharedPref.getBoolean("recordatorios_activos", true)

        etNombre.setText(nombre)
        tvHoraRecordatorio.text = horaRecordatorio
        switchRecordatorios.isChecked = recordatoriosActivos

        if (nombre.isNullOrEmpty()) {
            tvBienvenida.text = "¡Hola, hermosa alma! 💕"
        } else {
            tvBienvenida.text = "¡Hola, $nombre! ✨"
        }
    }

    private fun configurarRecyclerViews() {
        // Altar Digital
        altarAdapter = AltarDigitalAdapter(elementosAltar) { elemento ->
            mostrarOpcionesElemento(elemento)
        }
        rvAltarDigital.apply {
            layoutManager = GridLayoutManager(this@PerfilActivity, 3)
            adapter = altarAdapter
        }

        // Frases de Inspiración
        frasesAdapter = FrasesInspiracionAdapter(frasesPersonales) { frase, position ->
            mostrarOpcionesFrase(frase, position)
        }
        rvFrasesInspiracion.apply {
            layoutManager = LinearLayoutManager(this@PerfilActivity)
            adapter = frasesAdapter
        }
    }

    private fun configurarListeners() {
        // Guardar nombre cuando cambie
        etNombre.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                guardarNombre()
            }
        }

        // Configurar recordatorios
        switchRecordatorios.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.edit().putBoolean("recordatorios_activos", isChecked).apply()
            if (isChecked) {
                configurarNotificaciones()
            } else {
                cancelarNotificaciones()
            }
        }

        // Cambiar hora de recordatorio
        tvHoraRecordatorio.setOnClickListener {
            mostrarSelectorHora()
        }

        // Agregar frase personal
        btnAgregarFrase.setOnClickListener {
            mostrarDialogoAgregarFrase()
        }

        // Cambiar tema
        btnCambiarTema.setOnClickListener {
            mostrarSelectorTema()
        }

        // Exportar datos
        btnExportarDatos.setOnClickListener {
            exportarDatos()
        }
    }

    private fun actualizarEstadisticas() {
        val diasActivos = calcularDiasActivos()
        val actividadesCompletadas = obtenerActividadesCompletadas()
        val entradasDiario = obtenerEntradasDiario()
        val sonidosFavoritos = obtenerSonidosFavoritos()

        val estadisticas = """
            🌟 Días activos: $diasActivos
            ✨ Actividades completadas: $actividadesCompletadas
            📝 Entradas en tu diario: $entradasDiario
            🎧 Sonidos favoritos: $sonidosFavoritos
            
            ¡Cada pequeño paso cuenta! 💕
        """.trimIndent()

        tvEstadisticas.text = estadisticas
    }

    private fun cargarAltarDigital() {
        // Cargar elementos guardados o crear los predeterminados
        val elementosGuardados = cargarElementosGuardados()

        if (elementosGuardados.isEmpty()) {
            elementosAltar.addAll(crearElementosPredeterminados())
        } else {
            elementosAltar.addAll(elementosGuardados)
        }

        altarAdapter.notifyDataSetChanged()
    }

    private fun crearElementosPredeterminados(): List<ElementoAltar> {
        return listOf(
            ElementoAltar(
                id = 1,
                tipo = TipoElementoAltar.EMOJI,
                contenido = "🌸",
                descripcion = "Mi flor favorita",
                esPersonalizable = true
            ),
            ElementoAltar(
                id = 2,
                tipo = TipoElementoAltar.EMOJI,
                contenido = "🌙",
                descripcion = "Mi luna protectora",
                esPersonalizable = true
            ),
            ElementoAltar(
                id = 3,
                tipo = TipoElementoAltar.FRASE,
                contenido = "Soy suficiente",
                descripcion = "Mi mantra personal",
                esPersonalizable = true
            ),
            ElementoAltar(
                id = 4,
                tipo = TipoElementoAltar.EMOJI,
                contenido = "☕",
                descripcion = "Mi momento café",
                esPersonalizable = true
            ),
            ElementoAltar(
                id = 5,
                tipo = TipoElementoAltar.EMOJI,
                contenido = "📚",
                descripcion = "Mi amor por leer",
                esPersonalizable = true
            ),
            ElementoAltar(
                id = 6,
                tipo = TipoElementoAltar.COLOR,
                contenido = "#FFB6C1",
                descripcion = "Mi color de paz",
                esPersonalizable = true
            )
        )
    }

    private fun cargarFrasesPersonales() {
        val frasesGuardadas = sharedPref.getStringSet("frases_personales", setOf()) ?: setOf()

        frasesPersonales.clear()
        if (frasesGuardadas.isEmpty()) {
            frasesPersonales.addAll(obtenerFrasesInspiracionPredeterminadas())
        } else {
            frasesPersonales.addAll(frasesGuardadas)
        }

        frasesAdapter.notifyDataSetChanged()
    }

    private fun obtenerFrasesInspiracionPredeterminadas(): List<String> {
        return listOf(
            "Mi sensibilidad es mi superpoder 💕",
            "Merezco amor, especialmente el mío",
            "Cada día es una nueva oportunidad para ser gentil conmigo",
            "Mis emociones son válidas y hermosas",
            "Estoy exactamente donde necesito estar",
            "Mi corazón sabe el camino 🌟"
        )
    }

    private fun guardarNombre() {
        val nombre = etNombre.text.toString().trim()
        sharedPref.edit().putString("nombre_usuario", nombre).apply()

        if (nombre.isNotEmpty()) {
            tvBienvenida.text = "¡Hola, $nombre! ✨"
        } else {
            tvBienvenida.text = "¡Hola, hermosa alma! 💕"
        }
    }

    private fun mostrarSelectorHora() {
        val horaActual = tvHoraRecordatorio.text.toString().split(":")
        val hora = horaActual[0].toInt()
        val minuto = horaActual[1].toInt()

        TimePickerDialog(this, { _, hourOfDay, minute ->
            val nuevaHora = String.format("%02d:%02d", hourOfDay, minute)
            tvHoraRecordatorio.text = nuevaHora
            sharedPref.edit().putString("hora_recordatorio", nuevaHora).apply()

            if (switchRecordatorios.isChecked) {
                configurarNotificaciones()
            }
        }, hora, minuto, true).show()
    }

    private fun mostrarDialogoAgregarFrase() {
        val input = EditText(this).apply {
            hint = "Escribe una frase que te inspire..."
            maxLines = 3
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("✨ Nueva Frase de Inspiración")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val nuevaFrase = input.text.toString().trim()
                if (nuevaFrase.isNotEmpty()) {
                    agregarFrasePersonal(nuevaFrase)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarFrasePersonal(frase: String) {
        frasesPersonales.add(0, frase)
        frasesAdapter.notifyItemInserted(0)
        rvFrasesInspiracion.smoothScrollToPosition(0)

        guardarFrasesPersonales()

        Toast.makeText(this, "Frase agregada con amor 💕", Toast.LENGTH_SHORT).show()
    }

    private fun guardarFrasesPersonales() {
        sharedPref.edit()
            .putStringSet("frases_personales", frasesPersonales.toSet())
            .apply()
    }

    private fun mostrarOpcionesFrase(frase: String, position: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("💫 $frase")
            .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                when (which) {
                    0 -> editarFrase(frase, position)
                    1 -> eliminarFrase(position)
                }
            }
            .show()
    }

    private fun editarFrase(fraseActual: String, position: Int) {
        val input = EditText(this).apply {
            setText(fraseActual)
            hint = "Edita tu frase..."
            maxLines = 3
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("✨ Editar Frase")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val fraseEditada = input.text.toString().trim()
                if (fraseEditada.isNotEmpty()) {
                    frasesPersonales[position] = fraseEditada
                    frasesAdapter.notifyItemChanged(position)
                    guardarFrasesPersonales()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarFrase(position: Int) {
        android.app.AlertDialog.Builder(this)
            .setTitle("¿Eliminar esta frase?")
            .setMessage("Esta acción no se puede deshacer")
            .setPositiveButton("Eliminar") { _, _ ->
                frasesPersonales.removeAt(position)
                frasesAdapter.notifyItemRemoved(position)
                guardarFrasesPersonales()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarSelectorTema() {
        val temas = arrayOf("🌸 Rosa Pastel", "🌙 Lavanda Nocturno", "🌻 Amarillo Suave", "🌿 Verde Sereno")

        android.app.AlertDialog.Builder(this)
            .setTitle("Elige tu tema favorito")
            .setItems(temas) { _, which ->
                aplicarTema(which)
            }
            .show()
    }

    private fun aplicarTema(temaSeleccionado: Int) {
        sharedPref.edit().putInt("tema_seleccionado", temaSeleccionado).apply()
        Toast.makeText(this, "Tema aplicado 🌟 Reinicia la app para ver todos los cambios", Toast.LENGTH_LONG).show()
    }

    private fun exportarDatos() {
        try {
            val datosExportacion = crearDatosExportacion()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, datosExportacion)
                putExtra(Intent.EXTRA_SUBJECT, "Mis datos de 5 Minutos Conmigo")
            }
            startActivity(Intent.createChooser(intent, "Compartir mis datos"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error al exportar datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun crearDatosExportacion(): String {
        val fechaExportacion = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        return """
            🌸 MIS DATOS DE "5 MINUTOS CONMIGO" 🌸
            Exportado el: $fechaExportacion
            
            📊 ESTADÍSTICAS:
            ${tvEstadisticas.text}
            
            💫 MIS FRASES DE INSPIRACIÓN:
            ${frasesPersonales.joinToString("\n• ") { "• $it" }}
            
            ---
            Con amor, desde mi espacio personal 💕
        """.trimIndent()
    }

    // Métodos auxiliares para estadísticas
    private fun calcularDiasActivos(): Int {
        val sharedPrefMain = getSharedPreferences("main_prefs", MODE_PRIVATE)
        val sharedPrefDiario = getSharedPreferences("diario_prefs", MODE_PRIVATE)
        val sharedPrefActividades = getSharedPreferences("actividades_prefs", MODE_PRIVATE)

        // Lógica simplificada: contar días únicos donde hay actividad
        val diasConActividad = mutableSetOf<String>()

        // Agregar días de cada tipo de actividad
        // (En una implementación real, guardarías timestamps de cada actividad)
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val hoy = formatoFecha.format(Date())

        if (sharedPrefMain.getLong("ultima_apertura", 0) > 0) diasConActividad.add(hoy)
        if (sharedPrefDiario.getString("entradas", "[]") != "[]") diasConActividad.add(hoy)
        if (sharedPrefActividades.getInt("total_completadas", 0) > 0) diasConActividad.add(hoy)

        return diasConActividad.size
    }

    private fun obtenerActividadesCompletadas(): Int {
        return getSharedPreferences("actividades_prefs", MODE_PRIVATE)
            .getInt("total_completadas", 0)
    }

    private fun obtenerEntradasDiario(): Int {
        val entradasJson = getSharedPreferences("diario_prefs", MODE_PRIVATE)
            .getString("entradas", "[]")
        // Contar entradas (simplificado)
        return entradasJson?.split("\"id\":")?.size?.minus(1) ?: 0
    }

    private fun obtenerSonidosFavoritos(): Int {
        return getSharedPreferences("sonidos_prefs", MODE_PRIVATE)
            .getStringSet("favoritos", setOf())?.size ?: 0
    }

    private fun mostrarOpcionesElemento(elemento: ElementoAltar) {
        android.app.AlertDialog.Builder(this)
            .setTitle("${elemento.contenido} ${elemento.descripcion}")
            .setItems(arrayOf("Personalizar", "Cambiar")) { _, which ->
                when (which) {
                    0 -> personalizarElemento(elemento)
                    1 -> cambiarElemento(elemento)
                }
            }
            .show()
    }

    private fun personalizarElemento(elemento: ElementoAltar) {
        // Implementar personalización según el tipo
        Toast.makeText(this, "Personalización disponible pronto 💕", Toast.LENGTH_SHORT).show()
    }

    private fun cambiarElemento(elemento: ElementoAltar) {
        // Implementar cambio de elemento
        Toast.makeText(this, "Cambio de elemento disponible pronto 🌟", Toast.LENGTH_SHORT).show()
    }

    private fun cargarElementosGuardados(): List<ElementoAltar> {
        // Por ahora retornar lista vacía, implementar guardado completo después
        return emptyList()
    }

    private fun configurarNotificaciones() {
        val notificacionesManager = NotificacionesManager(this)
        val hora = sharedPref.getString("hora_recordatorio", "20:00") ?: "20:00"

        // Verificar permisos
        if (!NotificacionesPermission.verificarPermisos(this)) {
            NotificacionesPermission.solicitarPermisos(this)
        }

        notificacionesManager.programarRecordatorio(hora)
        Toast.makeText(this, "Recordatorios configurados para las $hora 🔔", Toast.LENGTH_SHORT).show()
    }

    private fun cancelarNotificaciones() {
        val notificacionesManager = NotificacionesManager(this)
        notificacionesManager.cancelarRecordatorio()
        Toast.makeText(this, "Recordatorios desactivados", Toast.LENGTH_SHORT).show()
    }
}