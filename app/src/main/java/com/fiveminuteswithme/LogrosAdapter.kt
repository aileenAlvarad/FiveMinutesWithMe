package com.fiveminuteswithme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class LogrosAdapter(
    private val onLogroClick: (LogroConProgreso) -> Unit
) : RecyclerView.Adapter<LogrosAdapter.LogroViewHolder>() {

    private var logros = listOf<LogroConProgreso>()

    fun actualizarLogros(nuevosLogros: List<LogroConProgreso>) {
        logros = nuevosLogros
        notifyDataSetChanged()
    }

    inner class LogroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardLogro: CardView = itemView.findViewById(R.id.cardLogro)
        val tvEmojiLogro: TextView = itemView.findViewById(R.id.tvEmojiLogro)
        val tvTituloLogro: TextView = itemView.findViewById(R.id.tvTituloLogro)
        val tvDescripcionLogro: TextView = itemView.findViewById(R.id.tvDescripcionLogro)
        val tvCategoriaLogro: TextView = itemView.findViewById(R.id.tvCategoriaLogro)
        val progressBarLogro: ProgressBar = itemView.findViewById(R.id.progressBarLogro)
        val tvProgresoLogro: TextView = itemView.findViewById(R.id.tvProgresoLogro)
        val tvEstadoLogro: TextView = itemView.findViewById(R.id.tvEstadoLogro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logro, parent, false)
        return LogroViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogroViewHolder, position: Int) {
        val logroConProgreso = logros[position]
        val logro = logroConProgreso.logro
        val progreso = logroConProgreso.progreso

        // Configurar contenido básico
        holder.tvEmojiLogro.text = logro.emoji
        holder.tvTituloLogro.text = logro.titulo
        holder.tvDescripcionLogro.text = logro.descripcion
        holder.tvCategoriaLogro.text = logro.categoria.nombre

        // Configurar estado y progreso
        if (logroConProgreso.estaDesbloqueado) {
            configurarLogroDesbloqueado(holder, logro)
        } else {
            configurarLogroEnProgreso(holder, logro, progreso)
        }

        // Configurar color de categoría
        try {
            val colorCategoria = Color.parseColor(logro.categoria.color + "40")
            holder.cardLogro.setCardBackgroundColor(colorCategoria)
        } catch (e: Exception) {
            holder.cardLogro.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.surface)
            )
        }

        // Click listener
        holder.cardLogro.setOnClickListener {
            onLogroClick(logroConProgreso)
        }

        // Animación de entrada
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay((position * 100).toLong())
            .start()
    }

    private fun configurarLogroDesbloqueado(holder: LogroViewHolder, logro: Logro) {
        holder.tvEstadoLogro.text = "✅ Completado"
        holder.tvEstadoLogro.setTextColor(
            holder.itemView.context.getColor(R.color.success)
        )

        holder.progressBarLogro.progress = 100
        holder.tvProgresoLogro.text = "${logro.objetivo}/${logro.objetivo}"

        // Efecto visual para logros completados
        holder.tvEmojiLogro.textSize = 28f
        holder.tvTituloLogro.setTextColor(
            holder.itemView.context.getColor(R.color.text_primary)
        )
        holder.tvDescripcionLogro.setTextColor(
            holder.itemView.context.getColor(R.color.text_secondary)
        )
    }

    private fun configurarLogroEnProgreso(holder: LogroViewHolder, logro: Logro, progreso: ProgresoLogro) {
        if (progreso.actual == 0) {
            holder.tvEstadoLogro.text = "⭕ Sin empezar"
            holder.tvEstadoLogro.setTextColor(
                holder.itemView.context.getColor(R.color.text_hint)
            )
        } else {
            holder.tvEstadoLogro.text = "🔄 En progreso"
            holder.tvEstadoLogro.setTextColor(
                holder.itemView.context.getColor(R.color.primary)
            )
        }

        holder.progressBarLogro.progress = progreso.porcentaje.toInt()
        holder.tvProgresoLogro.text = "${progreso.actual}/${logro.objetivo}"

        // Efecto visual para logros no completados
        holder.tvEmojiLogro.textSize = 24f
        holder.tvTituloLogro.setTextColor(
            holder.itemView.context.getColor(R.color.text_secondary)
        )
        holder.tvDescripcionLogro.setTextColor(
            holder.itemView.context.getColor(R.color.text_hint)
        )

        // Aplicar transparencia si no ha empezado
        val alpha = if (progreso.actual == 0) 0.6f else 1.0f
        holder.itemView.alpha = alpha
    }

    override fun getItemCount(): Int = logros.size
}