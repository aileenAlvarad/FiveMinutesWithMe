package com.fiveminuteswithme

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*

class NotificacionesManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "recordatorios_channel"
        const val NOTIFICATION_ID = 1001
        const val ALARM_REQUEST_CODE = 2001
    }

    init {
        crearCanalNotificacion()
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Recordatorios de Amor Propio"
            val descripcion = "Notificaciones gentiles para tu bienestar"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT

            val canal = NotificationChannel(CHANNEL_ID, nombre, importancia).apply {
                description = descripcion
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#E6B8D4")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }

    fun programarRecordatorio(hora: String) {
        val (horaInt, minutoInt) = hora.split(":").map { it.toInt() }

        val calendario = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, horaInt)
            set(Calendar.MINUTE, minutoInt)
            set(Calendar.SECOND, 0)

            // Si la hora ya pasó hoy, programar para mañana
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val intent = Intent(context, RecordatorioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendario.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelarRecordatorio() {
        val intent = Intent(context, RecordatorioReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}

class RecordatorioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        mostrarNotificacion(context)
    }

    private fun mostrarNotificacion(context: Context) {
        val mensajes = listOf(
            "Tu alma merece un momento de paz 🌸",
            "¿Qué tal 5 minutos para ti? 💕",
            "Tu bienestar es importante ✨",
            "Un pequeño momento de amor propio 🌙",
            "Tiempo de reconectar contigo 🌺",
            "Tu corazón te espera 💖",
            "Un respiro para tu alma 🕊️",
            "¿Cómo estás sintiendo este momento? 🌻"
        )

        val mensaje = mensajes.random()

        val intentMain = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intentMain,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, NotificacionesManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("5 Minutos Conmigo")
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(android.graphics.Color.parseColor("#E6B8D4"))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NotificacionesManager.NOTIFICATION_ID,
                notificacion
            )
        } catch (e: SecurityException) {
            // Manejar cuando no hay permisos de notificación
        }
    }
}

// Utilidad para manejar permisos de notificación
object NotificacionesPermission {

    fun verificarPermisos(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true
        }
    }

    fun solicitarPermisos(activity: android.app.Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }
    }
}