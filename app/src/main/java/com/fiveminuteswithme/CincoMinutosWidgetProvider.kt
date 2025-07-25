package com.fiveminuteswithme

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class CincoMinutosWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_cinco_minutos)

        // Configurar fecha actual
        val fechaHoy = SimpleDateFormat("EEEE, d MMM", Locale("es", "ES"))
            .format(Date())
            .replaceFirstChar { it.uppercase() }
        views.setTextViewText(R.id.tvFechaWidget, fechaHoy)

        // Obtener mensaje motivacional según la hora
        val mensaje = obtenerMensajePorHora(context)
        views.setTextViewText(R.id.tvMensajeWidget, mensaje)

        // Obtener estadísticas rápidas
        val estadisticas = obtenerEstadisticasWidget(context)
        views.setTextViewText(R.id.tvEstadisticasWidget, estadisticas)

        // Configurar botones de acción
        configurarBotonesWidget(context, views, appWidgetId)

        // Actualizar el widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun obtenerMensajePorHora(context: Context): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when {
            hora < 6 -> "Dulces sueños 🌙"
            hora < 12 -> "Buenos días, hermosa alma ☀️"
            hora < 18 -> "¿Qué tal una pausa para ti? 💫"
            hora < 22 -> "Tiempo de relajarte 🌸"
            else -> "Preparándote para descansar 💤"
        }
    }

    private fun obtenerEstadisticasWidget(context: Context): String {
        val sharedPref = context.getSharedPreferences("estadisticas_emocionales", Context.MODE_PRIVATE)
        val diasConsecutivos = sharedPref.getInt("dias_consecutivos", 0)

        val sharedPrefActividades = context.getSharedPreferences("actividades_prefs", Context.MODE_PRIVATE)
        val actividadesHoy = sharedPrefActividades.getInt("actividades_hoy", 0)

        return when {
            diasConsecutivos > 0 -> "$diasConsecutivos días consecutivos ✨"
            actividadesHoy > 0 -> "$actividadesHoy actividades hoy 🌟"
            else -> "¡Tu primer momento te espera! 💕"
        }
    }

    private fun configurarBotonesWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int
    ) {
        // Botón principal - Actividad rápida
        val intentActividad = Intent(context, ActividadesActivity::class.java)
        val pendingIntentActividad = PendingIntent.getActivity(
            context, 0, intentActividad,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btnMomentoWidget, pendingIntentActividad)

        // Botón check-in emocional
        val intentCheckin = Intent(context, WidgetEstadoAnimoActivity::class.java)
        val pendingIntentCheckin = PendingIntent.getActivity(
            context, 1, intentCheckin,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btnCheckinWidget, pendingIntentCheckin)

        // Botón diario
        val intentDiario = Intent(context, DiarioActivity::class.java)
        val pendingIntentDiario = PendingIntent.getActivity(
            context, 2, intentDiario,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btnDiarioWidget, pendingIntentDiario)

        // Clic en toda el área del widget para abrir la app
        val intentMain = Intent(context, MainActivity::class.java)
        val pendingIntentMain = PendingIntent.getActivity(
            context, 3, intentMain,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.layoutPrincipalWidget, pendingIntentMain)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Configurar actualizaciones periódicas del widget
        WidgetUpdateService.programarActualizaciones(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancelar actualizaciones periódicas
        WidgetUpdateService.cancelarActualizaciones(context)
    }

    companion object {
        fun actualizarTodosLosWidgets(context: Context) {
            val intent = Intent(context, CincoMinutosWidgetProvider::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, CincoMinutosWidgetProvider::class.java)
            )

            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
            context.sendBroadcast(intent)
        }
    }
}

// Servicio para actualizar el widget periódicamente
class WidgetUpdateService {

    companion object {
        fun programarActualizaciones(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, WidgetUpdateReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Actualizar cada hora
            val intervalo = 60 * 60 * 1000L // 1 hora
            val proximaActualizacion = System.currentTimeMillis() + intervalo

            alarmManager.setRepeating(
                android.app.AlarmManager.RTC,
                proximaActualizacion,
                intervalo,
                pendingIntent
            )
        }

        fun cancelarActualizaciones(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val intent = Intent(context, WidgetUpdateReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 100, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}

// Receiver para actualizar el widget
class WidgetUpdateReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        CincoMinutosWidgetProvider.actualizarTodosLosWidgets(context)
    }
}