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

    private val booksRepository = BooksRepository()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemFavouriteBinding.bind(itemView)

        fun bind(favBooks: FavUsers) {
            itemView.setOnClickListener {
                onItemClick(favBooks)
            }

            val bookId = favBooks.id
            val bookRef = booksRepository.getBooksById(bookId.toString())
            bookRef.get().addOnSuccessListener {
                if (it != null) {
                    val title = it.data?.get("title").toString()
                    val author = it.data?.get("author").toString()
                    val description = it.data?.get("description").toString()
                    val genre = it.data?.get("genre").toString()
                    val pdfUrl = it.data?.get("pdfUrl").toString()
                    val image = it.data?.get("image").toString()

                    binding.apply {
                        tvTitle.text = title
                        tvAuthor.text = author
                        tvDescription.text = description
                        tvGenre.text = genre
                        tvPdfUrl.text = pdfUrl
                        Glide.with(itemView.context)
                            .load(image)
                            .into(imageBooks)
                    }
                }
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