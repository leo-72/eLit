package com.android.elit.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.R
import com.android.elit.admin.UpdateDeleteBookActivity
import com.android.elit.databinding.ItemBookUpdateDeleteBinding
import com.android.elit.dataclass.Books
import com.android.elit.repository.BooksRepository
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ListenerRegistration

class BooksAdapter (private val itemList: ArrayList<Books>): RecyclerView.Adapter<BooksAdapter.BooksViewHolder>(){
    private lateinit var listenerRegistration: ListenerRegistration
    private val booksRepository = BooksRepository()

    inner class BooksViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val binding = ItemBookUpdateDeleteBinding.bind(view)

        fun bind(books: Books){
            fun TextView.setTextEllipsis(text: String, maxLength: Int) {
                if (text.length > maxLength) {
                    val truncatedText = text.substring(0, maxLength - 3) + "..."
                    this.text = truncatedText
                } else {
                    this.text = text
                }
            }

            binding.btnEdit.setOnClickListener {
                val position = adapterPosition
                val data = itemList[position]

                val intent = Intent(itemView.context, UpdateDeleteBookActivity::class.java)
                intent.putExtra(UpdateDeleteBookActivity.EXTRA_ID_BOOK, data.id)
                intent.putExtra(UpdateDeleteBookActivity.EXTRA_IMAGE_BOOK, data.image)
                intent.putExtra(UpdateDeleteBookActivity.EXTRA_PDF_BOOK, data.pdfUrl)
                itemView.context.startActivity(intent)
            }

            binding.tvId.text = books.id.toString()
            binding.tvTitle.setTextEllipsis(books.title.toString(), 30)
            binding.tvAuthor.setTextEllipsis(books.author.toString(), 30)
            binding.tvDescription.text = books.description
            binding.tvGenre.text = books.genre
            binding.tvPdfUrl.text = books.pdfUrl
            Glide.with(itemView.context)
                .load(books.image)
                .into(binding.imageBooks)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_update_delete, parent, false)
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