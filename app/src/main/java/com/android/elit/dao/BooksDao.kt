package com.android.elit.dao

import com.android.elit.dataclass.Books
import com.google.firebase.firestore.FirebaseFirestore

class BooksDao{
    private val db = FirebaseFirestore.getInstance()
    private val booksCollection = db.collection("books")

    fun addBook(book: Books) {
        booksCollection.add(book)
    }

    fun updateBook(book: Books) {
        booksCollection.document(book.id.toString()).set(book)
    }

    fun deleteBook(id: String) {
        booksCollection.document(id).delete()
    }

    fun getBooks() = booksCollection

    fun getBooksByGenre(genre: String) = booksCollection.whereEqualTo("genre", genre)

    fun getBooksById(id: String) = booksCollection.document(id)

    fun downloadPdf(id: String, pdf: String) = booksCollection.whereEqualTo("id", id).whereEqualTo("pdfUrl", pdf)
}