package com.android.elit.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.R
import com.android.elit.databinding.ItemBooksBinding
import com.android.elit.databinding.ItemListBookBinding
import com.android.elit.databinding.ItemListSearchBookBinding
import com.android.elit.dataclass.Books
import com.android.elit.dataclass.FavUsers
import com.android.elit.repository.BooksRepository
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ListenerRegistration

class BookSearchAdapter (private val itemList: ArrayList<Books>, private val onItemClick: (Books) -> Unit): RecyclerView.Adapter<BookSearchAdapter.BooksViewHolder>(){
    private lateinit var listenerRegistration: ListenerRegistration
    private val booksRepository = BooksRepository()

    inner class BooksViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val binding = ItemListSearchBookBinding.bind(view)

        fun bind(books: Books){
            itemView.setOnClickListener{
                onItemClick(books)
            }

            val bookId = books.id
            val bookRef = booksRepository.getBooksById(bookId.toString())
            bookRef.get().addOnSuccessListener {
                if (it != null){
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_search_book, parent, false)
        return BooksViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        val books = itemList[position]
        holder.bind(books)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<Books>){
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun startListening(){
        val query = booksRepository.getBooks()
        listenerRegistration = query.addSnapshotListener{snapshot,exception ->
            if (exception != null){
                return@addSnapshotListener
            }

            if (snapshot != null){
                val updateData = snapshot.toObjects(Books::class.java)
                itemList.clear()
                itemList.addAll(updateData)
                notifyDataSetChanged()
            }
        }
    }

    fun stopListening() = listenerRegistration.remove()
}