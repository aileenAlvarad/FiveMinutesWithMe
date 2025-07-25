package com.fiveminuteswithme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

// Adapter para el Altar Digital
class AltarDigitalAdapter(
    private val elementos: List<ElementoAltar>,
    private val onElementoClick: (ElementoAltar) -> Unit
) : RecyclerView.Adapter<AltarDigitalAdapter.AltarViewHolder>() {

    inner class AltarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardElemento: CardView = itemView.findViewById(R.id.cardElementoAltar)
        val tvContenido: TextView = itemView.findViewById(R.id.tvContenidoElemento)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionElemento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AltarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_elemento_altar, parent, false)
        return AltarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AltarViewHolder, position: Int) {
        val elemento = elementos[position]

        when (elemento.tipo) {
            TipoElementoAltar.EMOJI -> {
                holder.tvContenido.text = elemento.contenido
                holder.tvContenido.textSize = 32f
            }
            TipoElementoAltar.FRASE -> {
                holder.tvContenido.text = elemento.contenido
                holder.tvContenido.textSize = 14f
            }
            TipoElementoAltar.COLOR -> {
                holder.tvContenido.text = "●"
                holder.tvContenido.textSize = 40f
                try {
                    holder.tvContenido.setTextColor(Color.parseColor(elemento.contenido))
                } catch (e: Exception) {
                    holder.tvContenido.setTextColor(Color.parseColor("#E6B8D4"))
                }
            }
            TipoElementoAltar.IMAGEN -> {
                holder.tvContenido.text = "🖼️"
                holder.tvContenido.textSize = 32f
            }
        }

        holder.tvDescripcion.text = elemento.descripcion

        // Click listener
        holder.cardElemento.setOnClickListener {
            onElementoClick(elemento)
        }

        // Animación de entrada
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay((position * 100).toLong())
            .start()
    }

    override fun getItemCount(): Int = elementos.size
}