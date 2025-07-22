package com.fiveminuteswithme

import java.util.Date

data class DiarioEntry(
    val id: Long,
    val fecha: Date,
    val momento: String,
    val emocion: Emocion
)