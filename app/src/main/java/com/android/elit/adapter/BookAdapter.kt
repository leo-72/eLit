package com.android.elit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.R
import com.android.elit.databinding.ItemListBookBinding
import com.android.elit.dataclass.Books
import com.squareup.picasso.Picasso

class BookAdapter(
    private val itemList: ArrayList<Books>,
    private val onItemClick: (Books) -> Unit
) : RecyclerView.Adapter<BookAdapter.BooksViewHolder>() {
    inner class BooksViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemListBookBinding.bind(view)

        fun bind(books: Books) {
            fun TextView.setTextEllipsis(text: String, maxLength: Int) {
                if (text.length > maxLength) {
                    val truncatedText = text.substring(0, maxLength - 3) + "..."
                    this.text = truncatedText
                } else {
                    this.text = text
                }
            }
            itemView.setOnClickListener {
                onItemClick(books)
            }
            binding.apply {
                itemTitle.setTextEllipsis(books.title.toString(), 30)
                itemAuthor.setTextEllipsis(books.author.toString(), 30)
                itemDescription.text = books.description
                itemGenre.text = books.genre
                itemPdf.text = books.pdfUrl
                Picasso.get().load(books.image).into(itemImage)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_book, parent, false)
        return BooksViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        val books = itemList[position]
        holder.bind(books)
    }
}