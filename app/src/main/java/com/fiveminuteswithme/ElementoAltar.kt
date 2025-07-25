package com.fiveminuteswithme

// Elemento del Altar Digital
data class ElementoAltar(
    val id: Int,
    val tipo: TipoElementoAltar,
    var contenido: String,
    var descripcion: String,
    val esPersonalizable: Boolean = true
)

