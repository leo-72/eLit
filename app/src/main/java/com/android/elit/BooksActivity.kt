package com.android.elit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.adapter.BookAdapter
import com.android.elit.databinding.ActivityBooksBinding
import com.android.elit.dataclass.Books
import com.android.elit.repository.BooksRepository
import com.android.elit.ui.activity.DetailBookActivity
import com.google.firebase.auth.FirebaseAuth

class BooksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBooksBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var booksRepository: BooksRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val bookList = ArrayList<Books>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this)
        auth = FirebaseAuth.getInstance()
        booksRepository = BooksRepository()

        actionBar()
        setContent()
    }

    private fun actionBar() {
        binding.apply {
            ivBackToolbar.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setContent() {
        loadingDialog.show()

        val onItemClick: (Books) -> Unit = { books ->
            val intent = Intent(this, DetailBookActivity::class.java)
            intent.putExtra(DetailBookActivity.EXTRA_ID_BOOK, books.id)
            startActivity(intent)
        }

        bookAdapter = BookAdapter(bookList, onItemClick)
        recyclerView = binding.rvBooks
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = bookAdapter

        booksRepository.getBooks().get().addOnSuccessListener { result ->
            if (result != null) {
                val books = result.toObjects(Books::class.java)
                bookList.addAll(books)
                bookAdapter.notifyDataSetChanged()
                loadingDialog.dismiss()
            }
            loadingDialog.dismiss()
        }.addOnFailureListener {
            Toast.makeText(
                this,
                getString(R.string.failed_to_load_data),
                Toast.LENGTH_SHORT
            ).show()
            loadingDialog.dismiss()
        }
    }
}