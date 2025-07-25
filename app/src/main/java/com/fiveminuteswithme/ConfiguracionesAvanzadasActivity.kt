package com.fiveminuteswithme

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConfiguracionesAvanzadasActivity : AppCompatActivity() {

    private lateinit var switchModoOscuro: SwitchMaterial
    private lateinit var switchAnimaciones: SwitchMaterial
    private lateinit var switchSonidos: SwitchMaterial
    private lateinit var switchAnalytics: SwitchMaterial
    private lateinit var btnCrearBackup: Button
    private lateinit var btnRestaurarBackup: Button
    private lateinit var btnExportarDatos: Button
    private lateinit var btnRestablecerApp: Button
    private lateinit var btnAcercaDe: Button
    private lateinit var tvVersionApp: TextView
    private lateinit var tvUltimoBackup: TextView
    private lateinit var progressBarBackup: ProgressBar

    private lateinit var backupManager: BackupManager
    private val REQUEST_CODE_RESTORE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuraciones_avanzadas)

        inicializarVistas()
        configurarBackupManager()
        cargarConfiguraciones()
        configurarListeners()
        mostrarInformacionApp()
    }

    private fun inicializarVistas() {
        switchModoOscuro = findViewById(R.id.switchModoOscuro)
        switchAnimaciones = findViewById(R.id.switchAnimaciones)
        switchSonidos = findViewById(R.id.switchSonidos)
        switchAnalytics = findViewById(R.id.switchAnalytics)
        btnCrearBackup = findViewById(R.id.btnCrearBackup)
        btnRestaurarBackup = findViewById(R.id.btnRestaurarBackup)
        btnExportarDatos = findViewById(R.id.btnExportarDatos)
        btnRestablecerApp = findViewById(R.id.btnRestablecerApp)
        btnAcercaDe = findViewById(R.id.btnAcercaDe)
        tvVersionApp = findViewById(R.id.tvVersionApp)
        tvUltimoBackup = findViewById(R.id.tvUltimoBackup)
        progressBarBackup = findViewById(R.id.progressBarBackup)
    }

    private fun configurarBackupManager() {
        backupManager = BackupManager(this)
    }

    private fun cargarConfiguraciones() {
        val sharedPref = getSharedPreferences("configuraciones_avanzadas", MODE_PRIVATE)

        // Cargar configuraciones guardadas
        switchModoOscuro.isChecked = sharedPref.getBoolean("modo_oscuro", false)
        switchAnimaciones.isChecked = sharedPref.getBoolean("animaciones_habilitadas", true)
        switchSonidos.isChecked = sharedPref.getBoolean("sonidos_habilitados", true)
        switchAnalytics.isChecked = sharedPref.getBoolean("analytics_anonimos", false)

        // Mostrar información del último backup
        val ultimoBackup = sharedPref.getLong("ultimo_backup", 0)
        if (ultimoBackup > 0) {
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ultimoBackup))
            tvUltimoBackup.text = "Último backup: $fecha"
        } else {
            tvUltimoBackup.text = "Sin backups creados"
        }
    }

    private fun configurarListeners() {
        switchModoOscuro.setOnCheckedChangeListener { _, isChecked ->
            aplicarModoOscuro(isChecked)
            guardarConfiguracion("modo_oscuro", isChecked)
        }

        switchAnimaciones.setOnCheckedChangeListener { _, isChecked ->
            guardarConfiguracion("animaciones_habilitadas", isChecked)
            mostrarToast(if (isChecked) "Animaciones habilitadas ✨" else "Animaciones deshabilitadas")
        }

        switchSonidos.setOnCheckedChangeListener { _, isChecked ->
            guardarConfiguracion("sonidos_habilitados", isChecked)
            mostrarToast(if (isChecked) "Sonidos habilitados 🔊" else "Sonidos silenciados 🔇")
        }

        switchAnalytics.setOnCheckedChangeListener { _, isChecked ->
            guardarConfiguracion("analytics_anonimos", isChecked)
            mostrarDialogoAnalytics(isChecked)
        }

        btnCrearBackup.setOnClickListener {
            crearBackupCompleto()
        }

        btnRestaurarBackup.setOnClickListener {
            abrirSelectorArchivo()
        }

        btnExportarDatos.setOnClickListener {
            mostrarOpcionesExportacion()
        }

        btnRestablecerApp.setOnClickListener {
            mostrarDialogoRestablecimiento()
        }

        btnAcercaDe.setOnClickListener {
            mostrarDialogoAcercaDe()
        }
    }

    private fun aplicarModoOscuro(activar: Boolean) {
        if (activar) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            mostrarToast("Modo oscuro activado 🌙")
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            mostrarToast("Modo claro activado ☀️")
        }
    }

    private fun crearBackupCompleto() {
        progressBarBackup.visibility = android.view.View.VISIBLE
        btnCrearBackup.isEnabled = false

        lifecycleScope.launch {
            when (val resultado = backupManager.crearBackupCompleto()) {
                is BackupResult.Success -> {
                    progressBarBackup.visibility = android.view.View.GONE
                    btnCrearBackup.isEnabled = true

                    // Actualizar fecha del último backup
                    guardarConfiguracion("ultimo_backup", System.currentTimeMillis())
                    cargarConfiguraciones()

                    mostrarDialogoBackupExitoso(resultado.archivo, resultado.totalEntradas)
                }
                is BackupResult.Error -> {
                    progressBarBackup.visibility = android.view.View.GONE
                    btnCrearBackup.isEnabled = true
                    mostrarToast("Error: ${resultado.mensaje}")
                }
            }
        }
    }

    private fun abrirSelectorArchivo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/json"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            startActivityForResult(
                Intent.createChooser(intent, "Seleccionar archivo de backup"),
                REQUEST_CODE_RESTORE
            )
        } catch (e: Exception) {
            mostrarToast("No se pudo abrir el selector de archivos")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_RESTORE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                restaurarBackup(uri)
            }
        }
    }

    private fun restaurarBackup(uri: Uri) {
        progressBarBackup.visibility = android.view.View.VISIBLE
        btnRestaurarBackup.isEnabled = false

        lifecycleScope.launch {
            when (val resultado = backupManager.restaurarBackup(uri)) {
                is RestoreResult.Success -> {
                    progressBarBackup.visibility = android.view.View.GONE
                    btnRestaurarBackup.isEnabled = true

                    mostrarDialogoRestauracionExitosa(
                        resultado.entradasRestauradas,
                        resultado.fechaBackup
                    )
                }
                is RestoreResult.Error -> {
                    progressBarBackup.visibility = android.view.View.GONE
                    btnRestaurarBackup.isEnabled = true
                    mostrarToast("Error: ${resultado.mensaje}")
                }
            }
        }
    }

    private fun mostrarOpcionesExportacion() {
        val opciones = arrayOf(
            "📝 Solo mi diario (JSON)",
            "📊 Solo mi diario (CSV)",
            "💫 Solo mis frases (JSON)",
            "🏆 Solo mis logros (JSON)",
            "📄 Todo en texto plano",
            "📦 Exportación completa (JSON)"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("¿Qué datos quieres exportar?")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> exportarDatos(incluirDiario = true, formato = FormatoExportacion.JSON)
                    1 -> exportarDatos(incluirDiario = true, formato = FormatoExportacion.CSV)
                    2 -> exportarDatos(incluirFrases = true, formato = FormatoExportacion.JSON)
                    3 -> exportarDatos(incluirLogros = true, formato = FormatoExportacion.JSON)
                    4 -> exportarDatos(incluirDiario = true, incluirFrases = true, formato = FormatoExportacion.TXT)
                    5 -> exportarDatos(incluirDiario = true, incluirFrases = true, incluirLogros = true, formato = FormatoExportacion.JSON)
                }
            }
            .show()
    }

    private fun exportarDatos(
        incluirDiario: Boolean = false,
        incluirFrases: Boolean = false,
        incluirLogros: Boolean = false,
        formato: FormatoExportacion = FormatoExportacion.JSON
    ) {
        progressBarBackup.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            when (val resultado = backupManager.exportarDatosPersonalizados(
                incluirDiario, incluirFrases, incluirLogros, formato
            )) {
                is ExportResult.Success -> {
                    progressBarBackup.visibility = android.view.View.GONE
                    mostrarDialogoExportacionExitosa(resultado.archivo)
                }
                is ExportResult.Error -> {
                    progressBarBackup.visibility = android.view.View.GONE
                    mostrarToast("Error: ${resultado.mensaje}")
                }
            }
        }
    }

    private fun mostrarDialogoRestablecimiento() {
        MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Restablecer aplicación")
            .setMessage("Esto eliminará TODOS tus datos:\n\n• Entradas del diario\n• Sonidos favoritos\n• Frases personales\n• Logros obtenidos\n• Configuraciones\n\n¿Estás segura? Esta acción no se puede deshacer.")
            .setPositiveButton("Sí, restablecer") { _, _ ->
                restablecerAplicacion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun restablecerAplicacion() {
        // Limpiar todas las SharedPreferences
        val prefsNames = listOf(
            "diario_prefs", "sonidos_prefs", "perfil_prefs", "actividades_prefs",
            "estadisticas_emocionales", "checkins_emocionales", "logros_prefs",
            "configuraciones_avanzadas", "main_prefs", "app_prefs"
        )

        prefsNames.forEach { prefsName ->
            getSharedPreferences(prefsName, MODE_PRIVATE).edit().clear().apply()
        }

        // Eliminar archivos de backup y exportación
        try {
            val backupDir = java.io.File(getExternalFilesDir(null), "backups")
            val exportDir = java.io.File(getExternalFilesDir(null), "exports")

            backupDir.deleteRecursively()
            exportDir.deleteRecursively()
        } catch (e: Exception) {
            // Ignorar errores de eliminación de archivos
        }

        mostrarToast("Aplicación restablecida. Reiniciando... 🔄")

        // Reiniciar la aplicación
        val intent = Intent(this, OnboardingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarInformacionApp() {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            tvVersionApp.text = "Versión ${packageInfo.versionName}"
        } catch (e: Exception) {
            tvVersionApp.text = "Versión 1.0.0"
        }
    }

    private fun mostrarDialogoAnalytics(activar: Boolean) {
        if (activar) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Analytics anónimos")
                .setMessage("Esto nos ayuda a mejorar la app recopilando datos completamente anónimos sobre uso (sin contenido personal). Puedes desactivarlo en cualquier momento.")
                .setPositiveButton("Entendido", null)
                .show()
        }
    }

    private fun mostrarDialogoBackupExitoso(archivo: java.io.File, totalEntradas: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("✅ Backup creado")
            .setMessage("Se han respaldado $totalEntradas entradas y todos tus datos.\n\nArchivo: ${archivo.name}")
            .setPositiveButton("Compartir") { _, _ ->
                backupManager.compartirArchivo(archivo)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun mostrarDialogoRestauracionExitosa(entradas: Int, fechaBackup: Date) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(fechaBackup)

        MaterialAlertDialogBuilder(this)
            .setTitle("✅ Datos restaurados")
            .setMessage("Se han restaurado $entradas entradas del diario y todos los datos del backup creado el $fecha.\n\n¡Tu información está de vuelta! 💕")
            .setPositiveButton("¡Perfecto!") { _, _ ->
                // Opcional: reiniciar la actividad principal
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            .show()
    }

    private fun mostrarDialogoExportacionExitosa(archivo: java.io.File) {
        MaterialAlertDialogBuilder(this)
            .setTitle("✅ Datos exportados")
            .setMessage("Tus datos han sido exportados exitosamente.\n\nArchivo: ${archivo.name}")
            .setPositiveButton("Compartir") { _, _ ->
                backupManager.compartirArchivo(archivo)
            }
            .setNegativeButton("Cerrar", null)
            .show()
    }

    private fun mostrarDialogoAcercaDe() {
        MaterialAlertDialogBuilder(this)
            .setTitle("🌸 Acerca de 5 Minutos Conmigo")
            .setMessage("""
                Una app creada con amor para acompañarte en tu viaje de autoconocimiento y bienestar emocional.
                
                ✨ Cada función está diseñada para nutrir tu relación contigo misma
                💕 Tu privacidad es sagrada - todos los datos se guardan localmente
                🌱 Cada pequeño momento de autocuidado cuenta
                
                Versión: ${tvVersionApp.text}
                
                Con amor y gratitud por permitirnos ser parte de tu crecimiento 💖
            """.trimIndent())
            .setPositiveButton("💕 Gracias", null)
            .show()
    }

    private fun guardarConfiguracion(clave: String, valor: Boolean) {
        getSharedPreferences("configuraciones_avanzadas", MODE_PRIVATE)
            .edit()
            .putBoolean(clave, valor)
            .apply()
    }

    private fun guardarConfiguracion(clave: String, valor: Long) {
        getSharedPreferences("configuraciones_avanzadas", MODE_PRIVATE)
            .edit()
            .putLong(clave, valor)
            .apply()
    }

    private fun mostrarToast(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}