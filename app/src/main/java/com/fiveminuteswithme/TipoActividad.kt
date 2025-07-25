package com.fiveminuteswithme

enum class TipoActividad(
    val titulo: String,
    val descripcion: String,
    val instrucciones: String,
    val duracionMinutos: Int,
    val emoji: String
) {
    RESPIRACION(
        titulo = "Respiración Consciente",
        descripcion = "Un momento para reconectar con tu respiración y encontrar calma",
        instrucciones = "Sigue el ritmo del círculo: inhala cuando crezca, exhala cuando se contraiga. Permite que tu respiración te ancle al presente.",
        duracionMinutos = 5,
        emoji = "🌬️"
    ),

    AFIRMACION(
        titulo = "Afirmaciones de Amor Propio",
        descripcion = "Palabras gentiles para nutrir tu alma",
        instrucciones = "Lee cada afirmación en silencio o en voz alta. Permítete sentir estas palabras en tu corazón. Repite las que más resuenen contigo.",
        duracionMinutos = 4,
        emoji = "💕"
    ),

    JOURNALING(
        titulo = "Mini Journaling",
        descripcion = "Un espacio seguro para explorar tus pensamientos",
        instrucciones = "Responde la pregunta desde el corazón, sin juzgar lo que escribes. No hay respuestas correctas o incorrectas.",
        duracionMinutos = 6,
        emoji = "📝"
    ),

    SONIDOS(
        titulo = "Pausa Sonora",
        descripcion = "Deja que los sonidos relajantes te envuelvan",
        instrucciones = "Elige un sonido que resuene contigo hoy. Cierra los ojos y permítete ser llevada por la experiencia auditiva.",
        duracionMinutos = 5,
        emoji = "🎧"
    ),

    MEDITACION(
        titulo = "Visualización Guiada",
        descripcion = "Un viaje interno hacia la paz y el autoconocimiento",
        instrucciones = "Lee la guía y luego cierra los ojos. Permite que tu imaginación te lleve a ese lugar de calma interior.",
        duracionMinutos = 7,
        emoji = "🌟"
    )
}