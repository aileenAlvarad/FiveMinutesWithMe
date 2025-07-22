package com.fiveminuteswithme

enum class Emocion(
    val nombre: String,
    val emoji: String,
    val color: String,
    val descripcion: String
) {
    FELIZ(
        nombre = "Feliz",
        emoji = "🌸",
        color = "#FFB6C1",
        descripcion = "Me siento radiante y llena de alegría"
    ),
    TRANQUILA(
        nombre = "Tranquila",
        emoji = "🌙",
        color = "#E6E6FA",
        descripcion = "En paz conmigo misma"
    ),
    TRISTE(
        nombre = "Triste",
        emoji = "🌧️",
        color = "#B0C4DE",
        descripcion = "Un día gris, pero está bien sentirlo"
    ),
    ANSIOSA(
        nombre = "Ansiosa",
        emoji = "🌊",
        color = "#DDA0DD",
        descripcion = "Mis pensamientos van como olas"
    ),
    AGRADECIDA(
        nombre = "Agradecida",
        emoji = "✨",
        color = "#F0E68C",
        descripcion = "Apreciando los pequeños momentos"
    ),
    NEUTRAL(
        nombre = "Neutral",
        emoji = "☁️",
        color = "#F5F5DC",
        descripcion = "Simplemente siendo"
    )
}