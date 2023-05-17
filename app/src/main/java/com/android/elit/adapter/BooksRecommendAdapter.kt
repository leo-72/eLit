package com.android.elit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.R
import com.android.elit.databinding.ItemBooksBinding
import com.android.elit.dataclass.Books
import com.bumptech.glide.Glide

class BooksRecommendAdapter(private val itemList: ArrayList<Books>, private val onItemClick: (Books) -> Unit): RecyclerView.Adapter<BooksRecommendAdapter.BooksViewHolder>(){

    inner class BooksViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val binding = ItemBooksBinding.bind(view)

        fun bind(books: Books){
            fun TextView.setTextEllipsis(text: String, maxLength: Int) {
                if (text.length > maxLength) {
                    val truncatedText = text.substring(0, maxLength - 3) + "..."
                    this.text = truncatedText
                } else {
                    this.text = text
                }
            }

            itemView.setOnClickListener{
                onItemClick(books)
            }
            binding.itemId.text = books.id.toString()
            binding.itemTitle.setTextEllipsis(books.title.toString(), 30)
            binding.itemAuthor.setTextEllipsis(books.author.toString(), 30)
            binding.itemDescription.text = books.description
            binding.itemGenre.text = books.genre
            binding.itemPdf.text = books.pdfUrl
            Glide.with(itemView.context)
                .load(books.image)
                .into(binding.itemImage)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_books, parent, false)
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