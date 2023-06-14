package com.android.elit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.R
import com.android.elit.databinding.ItemListSearchBookBinding
import com.android.elit.dataclass.Books
import com.android.elit.repository.BooksRepository
import com.bumptech.glide.Glide

class BookSearchAdapter(
    private val itemList: ArrayList<Books>,
    private val onItemClick: (Books) -> Unit
) : RecyclerView.Adapter<BookSearchAdapter.BooksViewHolder>() {

    inner class BooksViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemListSearchBookBinding.bind(view)

        fun bind(books: Books) {
            itemView.setOnClickListener {
                onItemClick(books)
            }

            binding.apply {
                tvTitle.text = books.title
                tvAuthor.text = books.author
                tvDescription.text = books.description
                tvGenre.text = books.genre
                tvPdfUrl.text = books.pdfUrl
                Glide.with(itemView.context)
                    .load(books.image)
                    .into(imageBooks)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_search_book, parent, false)
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