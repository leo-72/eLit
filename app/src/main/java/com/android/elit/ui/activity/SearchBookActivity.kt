package com.android.elit.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.adapter.*
import com.android.elit.databinding.ActivitySearchBookBinding
import com.android.elit.dataclass.Books
import com.android.elit.repository.BooksRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class SearchBookActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBookBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var booksRepository: BooksRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookSearchAdapter: BookSearchAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val bookList = ArrayList<Books>()
    private var previousQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize
        auth = FirebaseAuth.getInstance()
        booksRepository = BooksRepository()
        loadingDialog = LoadingDialog(this)

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchBook, InputMethodManager.SHOW_IMPLICIT)

        searchBook()
        setContent()

    }


    private fun searchBook() {
        binding.apply {
            etSearchBook.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                    val query = charSequence.toString().trim()
                    if (query.isNotEmpty()) {
                        if (query.length >= 3) {
                            if (query != previousQuery) {
                                previousQuery = query
                                booksRepository.getBooks().addSnapshotListener { value, error ->
                                    if (error != null) {
                                        showToast(error.message.toString())
                                        return@addSnapshotListener
                                    }

                                    if (value != null) {
                                        bookList.clear()
                                        for (document in value) {
                                            val book = document.toObject(Books::class.java)
                                            if (book.title!!.lowercase(Locale.ROOT)
                                                    .contains(query.lowercase(Locale.ROOT))
                                            ) {
                                                bookList.add(book)
                                            }
                                        }
                                        if (bookList.isNotEmpty()) {
                                            binding.bookNotFound.visibility = View.GONE
                                        } else {
                                            binding.bookNotFound.visibility = View.VISIBLE
                                        }
                                        bookSearchAdapter.notifyDataSetChanged()
                                    }
                                }
                                val imm =
                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.showSoftInput(etSearchBook, InputMethodManager.SHOW_IMPLICIT)
                            }
                        }
                    } else {
                        binding.bookNotFound.visibility = View.GONE
                        setContent()
                    }
                }

                override fun afterTextChanged(editable: Editable) {}
            })
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setContent() {
        loadingDialog.show()

        val onItemClick: (Books) -> Unit = { books ->
            val intent = Intent(this, DetailBookActivity::class.java)
            intent.putExtra(DetailBookActivity.EXTRA_ID_BOOK, books.id)
            startActivity(intent)
        }

        bookSearchAdapter = BookSearchAdapter(bookList, onItemClick)
        recyclerView = binding.rvSearch
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = bookSearchAdapter

        booksRepository.getBooks().get().addOnSuccessListener { result ->
            if (result != null) {
                val books = result.toObjects(Books::class.java)
                bookList.addAll(books)
                bookSearchAdapter.notifyDataSetChanged()
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