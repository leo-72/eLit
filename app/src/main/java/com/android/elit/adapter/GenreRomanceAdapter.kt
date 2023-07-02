package com.android.elit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.R
import com.android.elit.databinding.ItemBooksBinding
import com.android.elit.dataclass.Romance
import com.bumptech.glide.Glide

class GenreRomanceAdapter(
    private val listItem: ArrayList<Romance>,
    private val onItemClick: (Romance) -> Unit
) :
    RecyclerView.Adapter<GenreRomanceAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemBooksBinding.bind(itemView)

        fun bind(romance: Romance) {
            fun TextView.setTextEllipsis(text: String, maxLength: Int) {
                if (text.length > maxLength) {
                    val truncatedText = text.substring(0, maxLength - 3) + "..."
                    this.text = truncatedText
                } else {
                    this.text = text
                }
            }

            itemView.setOnClickListener {
                onItemClick(romance)
            }

            binding.itemTitle.setTextEllipsis(romance.title.toString(), 30)
            binding.itemAuthor.setTextEllipsis(romance.author.toString(), 30)
            Glide.with(itemView.context)
                .load(romance.image)
                .into(binding.itemImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_books, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listRomance = listItem[position]
        holder.bind(listRomance)
    }
}