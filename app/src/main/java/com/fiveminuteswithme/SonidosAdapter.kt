package com.fiveminuteswithme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class SonidosAdapter(
    private val sonidos: List<Sonido>,
    private val onSonidoClick: (Sonido) -> Unit
) : RecyclerView.Adapter<SonidosAdapter.SonidoViewHolder>() {

    inner class SonidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardSonido)
        val tvEmoji: TextView = itemView.findViewById(R.id.tvEmojiSonido)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreSonido)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcionSonido)
        val ivFavorito: ImageView = itemView.findViewById(R.id.ivFavoritoSonido)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaSonido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SonidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sonido, parent, false)
        return SonidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SonidoViewHolder, position: Int) {
        val sonido = sonidos[position]

        // Configurar contenido
        holder.tvEmoji.text = sonido.emoji
        holder.tvNombre.text = sonido.nombre
        holder.tvDescripcion.text = sonido.descripcion
        holder.tvCategoria.text = sonido.categoria.nombreCategoria

        // Configurar color de la tarjeta
        try {
            holder.cardView.setCardBackgroundColor(Color.parseColor(sonido.color + "40")) // 40 = alfa para transparencia
        } catch (e: Exception) {
            holder.cardView.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.surface)
            )
        }

        // Configurar ícono de favorito
        if (sonido.esFavorito) {
            holder.ivFavorito.setImageResource(R.drawable.ic_star_filled)
            holder.ivFavorito.visibility = View.VISIBLE
        } else {
            holder.ivFavorito.visibility = View.GONE
        }

        // Click listener
        holder.cardView.setOnClickListener {
            onSonidoClick(sonido)
        }

        // Animación de entrada
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay((position * 100).toLong())
            .start()
    }

    override fun getItemCount(): Int = sonidos.size
}