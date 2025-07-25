package com.fiveminuteswithme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color


class FrasesInspiracionAdapter(
    private val frases: List<String>,
    private val onFraseClick: (String, Int) -> Unit
) : RecyclerView.Adapter<FrasesInspiracionAdapter.FraseViewHolder>() {

    inner class FraseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardFrase: CardView = itemView.findViewById(R.id.cardFrase)
        val tvFrase: TextView = itemView.findViewById(R.id.tvFrase)
        val tvNumero: TextView = itemView.findViewById(R.id.tvNumeroFrase)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FraseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_frase_inspiracion, parent, false)
        return FraseViewHolder(view)
    }

    override fun onBindViewHolder(holder: FraseViewHolder, position: Int) {
        val frase = frases[position]

        holder.tvFrase.text = frase
        holder.tvNumero.text = "${position + 1}"

        // Alternar colores suavemente
        val colores = listOf(
            "#FFB6C1", "#E6E6FA", "#F0E68C", "#DDA0DD", "#B0C4DE"
        )
        val colorIndex = position % colores.size

        try {
            holder.cardFrase.setCardBackgroundColor(
                Color.parseColor(colores[colorIndex] + "40") // Transparencia
            )
        } catch (e: Exception) {
            holder.cardFrase.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.surface)
            )
        }

        // Click listener
        holder.cardFrase.setOnLongClickListener {
            onFraseClick(frase, position)
            true
        }

        // Animación de entrada
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay((position * 50).toLong())
            .start()
    }

    override fun getItemCount(): Int = frases.size
}