package com.fiveminuteswithme

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class BackupManager(private val context: Context) {

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .setPrettyPrinting()
        .create()

    data class BackupData(
        val version: String = "1.0",
        val fechaCreacion: Date = Date(),
        val dispositivo: String = android.os.Build.MODEL,
        val perfilUsuario: PerfilUsuario,
        val entradasDiario: List<DiarioEntry>,
        val sonidosFavoritos: List<Int>,
        val frasesPersonales: List<String>,
        val logrosDesbloqueados: List<String>,
        val estadisticas: EstadisticasBackup,
        val configuraciones: ConfiguracionesBackup
    )

    data class PerfilUsuario(
        val nombre: String,
        val fechaRegistro: Date,
        val altarDigital: List<ElementoAltar>
    )

    data class EstadisticasBackup(
        val diasConsecutivos: Int,
        val totalActividades: Int,
        val totalCheckins: Int,
        val tiempoTotalMinutos: Long,
        val ultimaActividad: Date?
    )

    data class ConfiguracionesBackup(
        val recordatoriosActivos: Boolean,
        val horaRecordatorio: String,
        val temaSeleccionado: Int,
        val notificacionesHabilitadas: Boolean
    )

    suspend fun crearBackupCompleto(): BackupResult {
        return try {
            val backupData = recopilarDatos()
            val jsonBackup = gson.toJson(backupData)

            val nombreArchivo = generarNombreArchivo()
            val archivo = crearArchivoBackup(nombreArchivo, jsonBackup)

            BackupResult.Success(archivo, backupData.entradasDiario.size)
        } catch (e: Exception) {
            BackupResult.Error("Error al crear backup: ${e.message}")
        }
    }

    suspend fun restaurarBackup(uri: Uri): RestoreResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonContent = inputStream?.bufferedReader()?.use { it.readText() }
                ?: return RestoreResult.Error("No se pudo leer el archivo")

            val backupData = gson.fromJson<BackupData>(jsonContent, BackupData::class.java)

            // Validar versión de backup
            if (!esVersionCompatible(backupData.version)) {
                return RestoreResult.Error("Versión de backup no compatible")
            }

            // Crear backup de datos actuales antes de restaurar
            val backupAnterior = crearBackupCompleto()

            // Restaurar datos
            restaurarDatos(backupData)

            RestoreResult.Success(
                entradasRestauradas = backupData.entradasDiario.size,
                fechaBackup = backupData.fechaCreacion
            )
        } catch (e: Exception) {
            RestoreResult.Error("Error al restaurar: ${e.message}")
        }
    }

    suspend fun exportarDatosPersonalizados(
        incluirDiario: Boolean = true,
        incluirFrases: Boolean = true,
        incluirLogros: Boolean = true,
        formato: FormatoExportacion = FormatoExportacion.JSON
    ): ExportResult {
        return try {
            val datos = when (formato) {
                FormatoExportacion.JSON -> crearExportacionJSON(incluirDiario, incluirFrases, incluirLogros)
                FormatoExportacion.CSV -> crearExportacionCSV(incluirDiario)
                FormatoExportacion.TXT -> crearExportacionTexto(incluirDiario, incluirFrases)
            }

            val nombreArchivo = generarNombreExportacion(formato)
            val archivo = crearArchivoExportacion(nombreArchivo, datos, formato)

            ExportResult.Success(archivo)
        } catch (e: Exception) {
            ExportResult.Error("Error en exportación: ${e.message}")
        }
    }

    private fun recopilarDatos(): BackupData {
        // Perfil de usuario
        val perfilPrefs = context.getSharedPreferences("perfil_prefs", Context.MODE_PRIVATE)
        val nombre = perfilPrefs.getString("nombre_usuario", "") ?: ""
        val fechaRegistro = Date(perfilPrefs.getLong("fecha_registro", System.currentTimeMillis()))

        // Entradas del diario
        val diarioPrefs = context.getSharedPreferences("diario_prefs", Context.MODE_PRIVATE)
        val entradasJson = diarioPrefs.getString("entradas", "[]") ?: "[]"
        val entradasType = object : TypeToken<List<DiarioEntry>>() {}.type
        val entradas: List<DiarioEntry> = try {
            gson.fromJson(entradasJson, entradasType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        // Sonidos favoritos
        val sonidosPrefs = context.getSharedPreferences("sonidos_prefs", Context.MODE_PRIVATE)
        val favoritos = sonidosPrefs.getStringSet("favoritos", setOf()) ?: setOf()
        val sonidosFavoritos = favoritos.mapNotNull { it.toIntOrNull() }

        // Frases personales
        val frasesPersonales = perfilPrefs.getStringSet("frases_personales", setOf())?.toList() ?: emptyList()

        // Logros desbloqueados
        val logrosPrefs = context.getSharedPreferences("logros_prefs", Context.MODE_PRIVATE)
        val logrosDesbloqueados = Logro.values()
            .filter { logro -> logrosPrefs.getBoolean("logro_${logro.name}", false) }
            .map { it.name }

        // Estadísticas
        val estadisticasPrefs = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
        val actividadesPrefs = context.getSharedPreferences("actividades_prefs", Context.MODE_PRIVATE)
        val checkinsPrefs = context.getSharedPreferences("checkins_emocionales", Context.MODE_PRIVATE)

        val estadisticas = EstadisticasBackup(
            diasConsecutivos = estadisticasPrefs.getInt("dias_consecutivos", 0),
            totalActividades = actividadesPrefs.getInt("total_completadas", 0),
            totalCheckins = checkinsPrefs.getInt("total_checkins", 0),
            tiempoTotalMinutos = estadisticasPrefs.getLong("tiempo_total_minutos", 0),
            ultimaActividad = if (actividadesPrefs.getLong("ultima_actividad", 0) > 0)
                Date(actividadesPrefs.getLong("ultima_actividad", 0)) else null
        )

        // Configuraciones
        val configuraciones = ConfiguracionesBackup(
            recordatoriosActivos = perfilPrefs.getBoolean("recordatorios_activos", true),
            horaRecordatorio = perfilPrefs.getString("hora_recordatorio", "20:00") ?: "20:00",
            temaSeleccionado = perfilPrefs.getInt("tema_seleccionado", 0),
            notificacionesHabilitadas = true // Por defecto true
        )

        return BackupData(
            perfilUsuario = PerfilUsuario(nombre, fechaRegistro, emptyList()), // Altar digital por implementar
            entradasDiario = entradas,
            sonidosFavoritos = sonidosFavoritos,
            frasesPersonales = frasesPersonales,
            logrosDesbloqueados = logrosDesbloqueados,
            estadisticas = estadisticas,
            configuraciones = configuraciones
        )
    }

    private fun restaurarDatos(backupData: BackupData) {
        // Restaurar perfil
        val perfilPrefs = context.getSharedPreferences("perfil_prefs", Context.MODE_PRIVATE)
        perfilPrefs.edit()
            .putString("nombre_usuario", backupData.perfilUsuario.nombre)
            .putLong("fecha_registro", backupData.perfilUsuario.fechaRegistro.time)
            .putStringSet("frases_personales", backupData.frasesPersonales.toSet())
            .putBoolean("recordatorios_activos", backupData.configuraciones.recordatoriosActivos)
            .putString("hora_recordatorio", backupData.configuraciones.horaRecordatorio)
            .putInt("tema_seleccionado", backupData.configuraciones.temaSeleccionado)
            .apply()

        // Restaurar entradas del diario
        val diarioPrefs = context.getSharedPreferences("diario_prefs", Context.MODE_PRIVATE)
        val entradasJson = gson.toJson(backupData.entradasDiario)
        diarioPrefs.edit()
            .putString("entradas", entradasJson)
            .apply()

        // Restaurar sonidos favoritos
        val sonidosPrefs = context.getSharedPreferences("sonidos_prefs", Context.MODE_PRIVATE)
        val favoritosSet = backupData.sonidosFavoritos.map { it.toString() }.toSet()
        sonidosPrefs.edit()
            .putStringSet("favoritos", favoritosSet)
            .apply()

        // Restaurar logros
        val logrosPrefs = context.getSharedPreferences("logros_prefs", Context.MODE_PRIVATE)
        val editor = logrosPrefs.edit()
        backupData.logrosDesbloqueados.forEach { nombreLogro ->
            editor.putBoolean("logro_$nombreLogro", true)
        }
        editor.apply()

        // Restaurar estadísticas
        val estadisticasPrefs = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
        estadisticasPrefs.edit()
            .putInt("dias_consecutivos", backupData.estadisticas.diasConsecutivos)
            .putLong("tiempo_total_minutos", backupData.estadisticas.tiempoTotalMinutos)
            .apply()

        val actividadesPrefs = context.getSharedPreferences("actividades_prefs", Context.MODE_PRIVATE)
        actividadesPrefs.edit()
            .putInt("total_completadas", backupData.estadisticas.totalActividades)
            .apply()

        val checkinsPrefs = context.getSharedPreferences("checkins_emocionales", Context.MODE_PRIVATE)
        checkinsPrefs.edit()
            .putInt("total_checkins", backupData.estadisticas.totalCheckins)
            .apply()
    }

    private fun crearExportacionJSON(incluirDiario: Boolean, incluirFrases: Boolean, incluirLogros: Boolean): String {
        val datos = mutableMapOf<String, Any>()
        datos["fechaExportacion"] = Date()
        datos["aplicacion"] = "5 Minutos Conmigo"

        if (incluirDiario) {
            val entradas = obtenerEntradasDiario()
            datos["diarioEmocional"] = entradas
        }

        if (incluirFrases) {
            val frases = obtenerFrasesPersonales()
            datos["frasesInspiracion"] = frases
        }

        if (incluirLogros) {
            val logros = obtenerLogrosDesbloqueados()
            datos["logrosObtenidos"] = logros
        }

        return gson.toJson(datos)
    }

    private fun crearExportacionCSV(incluirDiario: Boolean): String {
        if (!incluirDiario) return "Exportación CSV solo disponible para entradas del diario"

        val entradas = obtenerEntradasDiario()
        val csv = StringBuilder()

        // Headers
        csv.appendLine("Fecha,Hora,Emocion,Momento")

        // Datos
        entradas.forEach { entrada ->
            val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(entrada.fecha)
            val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(entrada.fecha)
            val emocion = entrada.emocion.nombre
            val momento = entrada.momento.replace(",", ";").replace("\n", " ")

            csv.appendLine("$fecha,$hora,$emocion,\"$momento\"")
        }

        return csv.toString()
    }

    private fun crearExportacionTexto(incluirDiario: Boolean, incluirFrases: Boolean): String {
        val texto = StringBuilder()

        texto.appendLine("🌸 MIS DATOS DE \"5 MINUTOS CONMIGO\" 🌸")
        texto.appendLine("=" * 50)
        texto.appendLine("Exportado el: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
        texto.appendLine()

        if (incluirDiario) {
            texto.appendLine("📝 MI DIARIO EMOCIONAL")
            texto.appendLine("-" * 30)

            val entradas = obtenerEntradasDiario()
            entradas.sortedByDescending { it.fecha }.forEach { entrada ->
                val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(entrada.fecha)
                texto.appendLine("$fecha - ${entrada.emocion.emoji} ${entrada.emocion.nombre}")
                texto.appendLine(entrada.momento)
                texto.appendLine()
            }
        }

        if (incluirFrases) {
            texto.appendLine("💫 MIS FRASES DE INSPIRACIÓN")
            texto.appendLine("-" * 30)

            val frases = obtenerFrasesPersonales()
            frases.forEachIndexed { index, frase ->
                texto.appendLine("${index + 1}. $frase")
            }
            texto.appendLine()
        }

        texto.appendLine("---")
        texto.appendLine("Con amor, desde mi espacio personal 💕")

        return texto.toString()
    }

    private fun generarNombreArchivo(): String {
        val fecha = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        return "5MinutosConmigo_Backup_$fecha.json"
    }

    private fun generarNombreExportacion(formato: FormatoExportacion): String {
        val fecha = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val extension = when (formato) {
            FormatoExportacion.JSON -> "json"
            FormatoExportacion.CSV -> "csv"
            FormatoExportacion.TXT -> "txt"
        }
        return "5MinutosConmigo_Datos_$fecha.$extension"
    }

    private fun crearArchivoBackup(nombreArchivo: String, contenido: String): File {
        val directorioBackup = File(context.getExternalFilesDir(null), "backups")
        if (!directorioBackup.exists()) {
            directorioBackup.mkdirs()
        }

        val archivo = File(directorioBackup, nombreArchivo)
        FileOutputStream(archivo).use { output ->
            output.write(contenido.toByteArray())
        }

        return archivo
    }

    private fun crearArchivoExportacion(nombreArchivo: String, contenido: String, formato: FormatoExportacion): File {
        val directorioExport = File(context.getExternalFilesDir(null), "exports")
        if (!directorioExport.exists()) {
            directorioExport.mkdirs()
        }

        val archivo = File(directorioExport, nombreArchivo)
        FileOutputStream(archivo).use { output ->
            output.write(contenido.toByteArray())
        }

        return archivo
    }

    private fun obtenerEntradasDiario(): List<DiarioEntry> {
        val diarioPrefs = context.getSharedPreferences("diario_prefs", Context.MODE_PRIVATE)
        val entradasJson = diarioPrefs.getString("entradas", "[]") ?: "[]"
        val type = object : TypeToken<List<DiarioEntry>>() {}.type
        return try {
            gson.fromJson(entradasJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun obtenerFrasesPersonales(): List<String> {
        val perfilPrefs = context.getSharedPreferences("perfil_prefs", Context.MODE_PRIVATE)
        return perfilPrefs.getStringSet("frases_personales", setOf())?.toList() ?: emptyList()
    }

    private fun obtenerLogrosDesbloqueados(): List<String> {
        val logrosPrefs = context.getSharedPreferences("logros_prefs", Context.MODE_PRIVATE)
        return Logro.values()
            .filter { logro -> logrosPrefs.getBoolean("logro_${logro.name}", false) }
            .map { "${it.emoji} ${it.titulo}" }
    }

    private fun esVersionCompatible(version: String): Boolean {
        // Por ahora solo soportamos versión 1.0
        return version == "1.0"
    }

    // Función para compartir archivo
    fun compartirArchivo(archivo: File) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = when {
            archivo.name.endsWith(".json") -> "application/json"
            archivo.name.endsWith(".csv") -> "text/csv"
            archivo.name.endsWith(".txt") -> "text/plain"
            else -> "application/octet-stream"
        }

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_SUBJECT, "Mis datos de 5 Minutos Conmigo")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Compartir mis datos")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

// Enums y clases sealed para resultados
enum class FormatoExportacion {
    JSON, CSV, TXT
}

sealed class BackupResult {
    data class Success(val archivo: File, val totalEntradas: Int) : BackupResult()
    data class Error(val mensaje: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val entradasRestauradas: Int, val fechaBackup: Date) : RestoreResult()
    data class Error(val mensaje: String) : RestoreResult()
}

sealed class ExportResult {
    data class Success(val archivo: File) : ExportResult()
    data class Error(val mensaje: String) : ExportResult()
}

// Operador para repetir strings (usado en el formato de texto)
private operator fun String.times(count: Int): String {
    return this.repeat(count)
}