package com.android.elit.admin.Fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.adapter.BooksAdapter
import com.android.elit.databinding.FragmentBooksAdminBinding
import com.android.elit.dataclass.Books
import com.android.elit.repository.BooksRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class BooksAdminFragment : Fragment() {
    private lateinit var _binding: FragmentBooksAdminBinding
    private val binding get() = _binding
    private lateinit var recyclerView: RecyclerView
    private lateinit var fs: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var auth: FirebaseAuth
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var listenerRegistration: ListenerRegistration
    private val booksList = ArrayList<Books>()
    private val booksRepository = BooksRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBooksAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fs = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        loadingDialog = LoadingDialog(requireContext())

        with(binding) {
            rvListBook.setHasFixedSize(true)
            noData.visibility = View.GONE

            recyclerView = rvListBook
            recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            booksAdapter = BooksAdapter(booksList)
            recyclerView.adapter = booksAdapter
        }

        listBook()
        updateDataFs()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listBook() {
        loadingDialog.show()
        booksRepository.getBooks().get().addOnSuccessListener { result ->
            loadingDialog.dismiss()
            if (!result.isEmpty) {
                booksList.clear()
                val books = result.toObjects(Books::class.java)
                booksList.addAll(books)
                booksAdapter.notifyDataSetChanged()
                binding.noData.visibility = View.GONE
            } else {
                binding.noData.visibility = View.VISIBLE
            }
        }.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.failed_to_load_data),
                Toast.LENGTH_SHORT
            ).show()
            binding.noData.visibility = View.VISIBLE
            loadingDialog.dismiss()
        }
    }

    private fun updateDataFs() {
        val query = booksRepository.getBooks()
        listenerRegistration = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.failed_to_load_data),
                    Toast.LENGTH_SHORT
                ).show()
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val books = snapshot.toObjects(Books::class.java)
                booksAdapter.updateData(books)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        booksAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        booksAdapter.stopListening()
    }

}