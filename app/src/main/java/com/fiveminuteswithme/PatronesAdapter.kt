package com.fiveminuteswithme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class PatronesAdapter : RecyclerView.Adapter<PatronesAdapter.PatronViewHolder>() {

    private var patrones = listOf<PatronEmocional>()

    fun actualizarPatrones(nuevosPatrones: List<PatronEmocional>) {
        patrones = nuevosPatrones
        notifyDataSetChanged()
    }

    inner class PatronViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardPatron: CardView = itemView.findViewById(R.id.cardPatron)
        val tvTituloPatron: TextView = itemView.findViewById(R.id.tvTituloPatron)
        val tvDescripcionPatron: TextView = itemView.findViewById(R.id.tvDescripcionPatron)
        val tvRecomendacionPatron: TextView = itemView.findViewById(R.id.tvRecomendacionPatron)
        val progressBarPatron: ProgressBar = itemView.findViewById(R.id.progressBarPatron)
        val tvIconoTipo: TextView = itemView.findViewById(R.id.tvIconoTipoPatron)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatronViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patron_emocional, parent, false)
        return PatronViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatronViewHolder, position: Int) {
        val patron = patrones[position]

        // Configurar contenido
        holder.tvTituloPatron.text = patron.titulo
        holder.tvDescripcionPatron.text = patron.descripcion
        holder.tvRecomendacionPatron.text = patron.recomendacion

        // Configurar icono según tipo
        val (icono, color) = when (patron.tipo) {
            TipoPatron.CONSISTENCIA -> "📊" to "#4CAF50"
            TipoPatron.EMOCIONAL -> "💭" to "#E91E63"
            TipoPatron.TEMPORAL -> "🕐" to "#FF9800"
            TipoPatron.INSIGHT -> "💡" to "#2196F3"
        }

        holder.tvIconoTipo.text = icono

        // Configurar barra de progreso
        val progreso = (patron.intensidad * 100).toInt()
        holder.progressBarPatron.progress = progreso

        // Configurar color de la tarjeta
        try {
            val colorCard = Color.parseColor(color + "20") // Transparencia
            holder.cardPatron.setCardBackgroundColor(colorCard)
        } catch (e: Exception) {
            holder.cardPatron.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.surface)
            )
        }

        // Mostrar u ocultar barra de progreso según el tipo
        if (patron.tipo == TipoPatron.INSIGHT) {
            holder.progressBarPatron.visibility = View.GONE
        } else {
            holder.progressBarPatron.visibility = View.VISIBLE
        }

        // Animación de entrada
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay((position * 100).toLong())
            .start()
    }

    override fun getItemCount(): Int = patrones.size
}