package com.android.elit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.R
import com.android.elit.databinding.ItemBooksBinding
import com.android.elit.dataclass.Books
import com.android.elit.dataclass.Fiction
import com.bumptech.glide.Glide

class GenreFictionAdapter(private val listItem: ArrayList<Fiction>, private val onItemClick: (Fiction) -> Unit) : RecyclerView.Adapter<GenreFictionAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemBooksBinding.bind(itemView)

        fun bind(fiction: Fiction){

            fun TextView.setTextEllipsis(text: String, maxLength: Int) {
                if (text.length > maxLength) {
                    val truncatedText = text.substring(0, maxLength - 3) + "..."
                    this.text = truncatedText
                } else {
                    this.text = text
                }
            }

            itemView.setOnClickListener{
                onItemClick(fiction)
            }
            binding.itemId.text = fiction.id.toString()
            binding.itemTitle.setTextEllipsis(fiction.title.toString(), 30)
            binding.itemAuthor.setTextEllipsis(fiction.author.toString(), 30)
            binding.itemDescription.text = fiction.description
            binding.itemGenre.text = fiction.genre
            binding.itemPdf.text = fiction.pdfUrl
            Glide.with(itemView.context)
                .load(fiction.image)
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