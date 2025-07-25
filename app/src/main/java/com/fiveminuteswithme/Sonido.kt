package com.fiveminuteswithme

data class Sonido(
    val id: Int,
    val nombre: String,
    val emoji: String,
    val descripcion: String,
    val color: String,
    val archivo: String,
    val categoria: SonidoCategoria,
    var esFavorito: Boolean = false
)