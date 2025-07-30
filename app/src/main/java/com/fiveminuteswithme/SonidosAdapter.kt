package com.fiveminuteswithme

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoriaSonido)
        val ivFavorito: ImageView = itemView.findViewById(R.id.ivFavoritoSonido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SonidoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sonido, parent, false)
        return SonidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SonidoViewHolder, position: Int) {
        val sonido = sonidos[position]
        val context = holder.itemView.context

        // Configurar contenido
        holder.tvEmoji.text = sonido.emoji
        holder.tvNombre.text = sonido.nombre
        holder.tvDescripcion.text = sonido.descripcion
        holder.tvCategoria.text = sonido.categoria.nombreCategoria

        // Configurar color de la tarjeta con transparencia
        try {
            val colorWithAlpha = Color.parseColor(sonido.color + "30") // 30 = alfa para transparencia sutil
            holder.cardView.setCardBackgroundColor(colorWithAlpha)
        } catch (e: Exception) {
            // Color por defecto si hay error al parsear el color
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(context, android.R.color.white)
            )
        }

        // Configurar ícono de favorito
        if (sonido.esFavorito) {
            holder.ivFavorito.setImageResource(R.drawable.ic_star_filled)
            holder.ivFavorito.setColorFilter(
                ContextCompat.getColor(context, R.color.accent)
            )
            holder.ivFavorito.visibility = View.VISIBLE
        } else {
            holder.ivFavorito.visibility = View.GONE
        }

        // Click listener para toda la tarjeta
        holder.cardView.setOnClickListener {
            onSonidoClick(sonido)
        }

        // Animación de entrada suave
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .setStartDelay((position * 80).toLong()) // Animación escalonada
            .start()

        // Efecto de escala al tocar
        holder.cardView.setOnTouchListener { _, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    holder.cardView.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setDuration(100)
                        .start()
                }
                android.view.MotionEvent.ACTION_UP,
                android.view.MotionEvent.ACTION_CANCEL -> {
                    holder.cardView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
            }
            false // Permitir que el click se procese normalmente
        }
    }

    override fun getItemCount(): Int = sonidos.size

    // Función para actualizar el estado de favoritos
    fun actualizarFavoritos() {
        notifyDataSetChanged()
    }

    // Función para filtrar sonidos por categoría (opcional)
    fun filtrarPorCategoria(categoria: SonidoCategoria): List<Sonido> {
        return sonidos.filter { it.categoria == categoria }
    }
}