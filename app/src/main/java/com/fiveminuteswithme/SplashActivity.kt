package com.fiveminuteswithme

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView
    private lateinit var tvAppName: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvLoading: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        inicializarVistas()
        iniciarAnimaciones()
        verificarEstadoApp()
    }

    private fun inicializarVistas() {
        ivLogo = findViewById(R.id.ivLogoSplash)
        tvAppName = findViewById(R.id.tvAppNameSplash)
        tvSubtitle = findViewById(R.id.tvSubtitleSplash)
        tvLoading = findViewById(R.id.tvLoadingSplash)
    }

    private fun iniciarAnimaciones() {
        lifecycleScope.launch {
            // Animación del logo
            delay(300)
            val fadeInScale = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.fade_in_scale)
            ivLogo.startAnimation(fadeInScale)
            ivLogo.visibility = android.view.View.VISIBLE

            // Animación del título
            delay(600)
            val slideInUp = AnimationUtils.loadAnimation(this@SplashActivity, R.anim.slide_in_up)
            tvAppName.startAnimation(slideInUp)
            tvAppName.visibility = android.view.View.VISIBLE

            // Animación del subtítulo
            delay(900)
            tvSubtitle.startAnimation(slideInUp)
            tvSubtitle.visibility = android.view.View.VISIBLE

            // Texto de carga
            delay(1200)
            tvLoading.visibility = android.view.View.VISIBLE
            tvLoading.alpha = 0f
            tvLoading.animate()
                .alpha(1f)
                .setDuration(500)
                .start()
        }
    }

    private fun verificarEstadoApp() {
        lifecycleScope.launch {
            // Simular tiempo de carga mientras verificamos el estado
            delay(2500)

            val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val onboardingCompletado = sharedPref.getBoolean("onboarding_completado", false)

            // Verificar si hay datos existentes (para usuarios que actualizan)
            val tieneEntradas = verificarDatosExistentes()

            val destinoIntent = when {
                !onboardingCompletado && !tieneEntradas -> {
                    // Usuario nuevo - mostrar onboarding
                    Intent(this@SplashActivity, OnboardingActivity::class.java)
                }
                !onboardingCompletado && tieneEntradas -> {
                    // Usuario existente actualizando - saltar onboarding
                    marcarOnboardingComoCompletado()
                    Intent(this@SplashActivity, MainActivity::class.java)
                }
                else -> {
                    // Usuario existente - ir directo a main
                    Intent(this@SplashActivity, MainActivity::class.java)
                }
            }

            // Verificar si debe mostrar check-in emocional
            if (shouldShowCheckinEmocional()) {
                destinoIntent.putExtra("mostrar_checkin", true)
            }

            startActivity(destinoIntent)
            finish()

            // Animación de salida
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun verificarDatosExistentes(): Boolean {
        // Verificar si existen datos de versiones anteriores
        val diarioPrefs = getSharedPreferences("diario_prefs", MODE_PRIVATE)
        val entradasJson = diarioPrefs.getString("entradas", "[]") ?: "[]"

        val actividadesPrefs = getSharedPreferences("actividades_prefs", MODE_PRIVATE)
        val totalActividades = actividadesPrefs.getInt("total_completadas", 0)

        return entradasJson != "[]" || totalActividades > 0
    }

    private fun marcarOnboardingComoCompletado() {
        val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
        sharedPref.edit()
            .putBoolean("onboarding_completado", true)
            .putLong("fecha_primer_uso", System.currentTimeMillis())
            .apply()
    }

    private fun shouldShowCheckinEmocional(): Boolean {
        val checkinsPrefs = getSharedPreferences("checkins_emocionales", MODE_PRIVATE)
        val ultimoCheckin = checkinsPrefs.getLong("ultimo_checkin_timestamp", 0)
        val ahora = System.currentTimeMillis()
        val seisHoras = 6 * 60 * 60 * 1000L

        // Mostrar check-in si han pasado más de 6 horas desde el último
        return (ahora - ultimoCheckin) > seisHoras
    }

    override fun onBackPressed() {
        // Prevenir que el usuario regrese durante el splash
        // No llamar super.onBackPressed()
    }
}