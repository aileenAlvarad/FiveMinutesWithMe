package com.fiveminuteswithme

data class EstadisticasUsuario(
    val diasActivos: Int,
    val actividadesCompletadas: Int,
    val entradasDiario: Int,
    val sonidosFavoritos: Int,
    val fechaRegistro: Long,
    val ultimaActividad: Long
)

// Configuración de tema
enum class TemaApp(
    val nombre: String,
    val colorPrimario: String,
    val colorSecundario: String,
    val emoji: String
) {
    ROSA_PASTEL(
        nombre = "Rosa Pastel",
        colorPrimario = "#E6B8D4",
        colorSecundario = "#FFB6C1",
        emoji = "🌸"
    ),
    LAVANDA_NOCTURNO(
        nombre = "Lavanda Nocturno",
        colorPrimario = "#DDA0DD",
        colorSecundario = "#E6E6FA",
        emoji = "🌙"
    ),
    AMARILLO_SUAVE(
        nombre = "Amarillo Suave",
        colorPrimario = "#F0E68C",
        colorSecundario = "#FFFACD",
        emoji = "🌻"
    ),
    VERDE_SERENO(
        nombre = "Verde Sereno",
        colorPrimario = "#90EE90",
        colorSecundario = "#F0FFF0",
        emoji = "🌿"
    )
}