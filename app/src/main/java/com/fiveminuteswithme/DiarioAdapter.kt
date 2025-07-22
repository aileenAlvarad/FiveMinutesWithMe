package com.fiveminuteswithme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import com.fiveminuteswithme.R
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DiarioAdapter(
    private val entradas: List<DiarioEntry>,
    private val onItemClick: (DiarioEntry) -> Unit
) : RecyclerView.Adapter<DiarioAdapter.DiarioViewHolder>() {

    inner class DiarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardEntrada)
        val tvEmojiFlor: TextView = itemView.findViewById(R.id.tvEmojiFlor)
        val tvMomento: TextView = itemView.findViewById(R.id.tvMomento)
        val tvFechaHora: TextView = itemView.findViewById(R.id.tvFechaHora)
        val tvEmocionNombre: TextView = itemView.findViewById(R.id.tvEmocionNombre)
        val viewColorAccent: View = itemView.findViewById(R.id.viewColorAccent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diario_entrada, parent, false)
        return DiarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiarioViewHolder, position: Int) {
        val entrada = entradas[position]

        // Configurar emoji/flor
        holder.tvEmojiFlor.text = entrada.emocion.emoji

        // Configurar texto del momento (mostrar primeras 100 caracteres)
        val textoResumido = if (entrada.momento.length > 100) {
            "${entrada.momento.substring(0, 97)}..."
        } else {
            entrada.momento
        }
        holder.tvMomento.text = textoResumido

        // Configurar fecha y hora
        val formatoFecha = when {
            esHoy(entrada.fecha) -> "'Hoy a las' HH:mm"
            esAyer(entrada.fecha) -> "'Ayer a las' HH:mm"
            else -> "d 'de' MMM 'a las' HH:mm"
        }

        val fechaFormateada = SimpleDateFormat(formatoFecha, Locale("es", "ES"))
            .format(entrada.fecha)
        holder.tvFechaHora.text = fechaFormateada

        // Configurar nombre de la emoción
        holder.tvEmocionNombre.text = entrada.emocion.nombre

        // Configurar color del acento
        try {
            holder.viewColorAccent.setBackgroundColor(Color.parseColor(entrada.emocion.color))
        } catch (e: Exception) {
            // Si hay error con el color, usar el color primario por defecto
            holder.viewColorAccent.setBackgroundColor(
                holder.itemView.context.getColor(R.color.primary)
            )
        }

        // Configurar click listener
        holder.cardView.setOnClickListener {
            onItemClick(entrada)
        }

        // Animación suave al aparecer
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay((position * 50).toLong())
            .start()
    }

    override fun getItemCount(): Int = entradas.size

    private fun esHoy(fecha: Date): Boolean {
        val calendario = Calendar.getInstance()
        val calendarioFecha = Calendar.getInstance().apply { time = fecha }

        return calendario.get(Calendar.YEAR) == calendarioFecha.get(Calendar.YEAR) &&
                calendario.get(Calendar.DAY_OF_YEAR) == calendarioFecha.get(Calendar.DAY_OF_YEAR)
    }

    private fun esAyer(fecha: Date): Boolean {
        val calendario = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val calendarioFecha = Calendar.getInstance().apply { time = fecha }

        return calendario.get(Calendar.YEAR) == calendarioFecha.get(Calendar.YEAR) &&
                calendario.get(Calendar.DAY_OF_YEAR) == calendarioFecha.get(Calendar.DAY_OF_YEAR)
    }
}