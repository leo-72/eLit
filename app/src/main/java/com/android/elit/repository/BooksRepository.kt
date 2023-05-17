package com.android.elit.repository

import com.android.elit.dao.BooksDao
import com.android.elit.dataclass.Books

class BooksRepository {
    private val booksDao = BooksDao()

    fun addBook(book: Books) {
        booksDao.addBook(book)
    }

    fun updateBook(book: Books) {
        booksDao.updateBook(book)
    }

    fun deleteBook(id: String) {
        booksDao.deleteBook(id)
    }

    fun getBooks() = booksDao.getBooks()

    fun getBooksByGenre(genre: String) = booksDao.getBooksByGenre(genre)

    fun getBooksById(id: String) = booksDao.getBooksById(id)

    fun downloadPdf(id: String, pdf: String) = booksDao.downloadPdf(id, pdf)
}