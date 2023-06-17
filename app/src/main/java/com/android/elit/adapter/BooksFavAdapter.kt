package com.android.elit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.R
import com.android.elit.databinding.ItemFavouriteBinding
import com.android.elit.dataclass.FavUsers
import com.android.elit.repository.BooksRepository
import com.bumptech.glide.Glide

class BooksFavAdapter(
    private val itemList: List<FavUsers>,
    private val onItemClick: (FavUsers) -> Unit
) : RecyclerView.Adapter<BooksFavAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemFavouriteBinding.bind(itemView)

        fun bind(favBooks: FavUsers) {
            itemView.setOnClickListener {
                onItemClick(favBooks)
            }
            binding.apply {
                tvTitle.text = favBooks.title
                tvAuthor.text = favBooks.author
                tvDescription.text = favBooks.description
                tvGenre.text = favBooks.genre
                tvPdfUrl.text = favBooks.pdfUrl
                Glide.with(itemView.context)
                    .load(favBooks.image)
                    .into(imageBooks)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_favourite, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favBooks = itemList[position]
        holder.bind(favBooks)
    }
}