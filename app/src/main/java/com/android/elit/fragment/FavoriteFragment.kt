package com.android.elit.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.LoadingDialog
import com.android.elit.adapter.BooksFavAdapter
import com.android.elit.databinding.FragmentFavouriteBinding
import com.android.elit.dataclass.FavUsers
import com.android.elit.repository.UsersRepository
import com.android.elit.ui.activity.DetailBookActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteFragment : Fragment() {

    private lateinit var _binding: FragmentFavouriteBinding
    private val binding get() = _binding
    private lateinit var recyclerView: RecyclerView
    private lateinit var fs: FirebaseFirestore
    private lateinit var favAdapter: BooksFavAdapter
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var auth: FirebaseAuth
    private val favList = ArrayList<FavUsers>()
    private val usersRepository = UsersRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFavouriteBinding.bind(view)

        fs = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        loadingDialog = LoadingDialog(requireContext())

        with(binding) {
            rvFavourite.setHasFixedSize(true)
            noData.visibility = View.GONE
        }

        actionBar()
        setFavBooks()
    }

    private fun actionBar() {
        binding.apply {
            ivBackToolbar.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setFavBooks() {
        val onItemClick: (FavUsers) -> Unit = { fav ->
            val intent = Intent(requireContext(), DetailBookActivity::class.java)
            intent.putExtra(DetailBookActivity.EXTRA_ID_BOOK, fav.id)
            intent.putExtra(DetailBookActivity.DETAIL_TITLE, fav.title)
            startActivity(intent)
        }

        favAdapter = BooksFavAdapter(favList, onItemClick)
        recyclerView = binding.rvFavourite
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = favAdapter

        loadingDialog.show()
        binding.rvFavourite.visibility = View.GONE

        val userId = auth.currentUser?.uid
        usersRepository.getFavoriteBooks(userId.toString()).get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                loadingDialog.dismiss()
                binding.rvFavourite.visibility = View.GONE
                binding.noData.visibility = View.VISIBLE
                return@addOnSuccessListener
            }

            usersRepository.getFavoriteBooks(userId.toString()).addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("FavBooks", "Error: $error")
                    loadingDialog.dismiss()
                    return@addSnapshotListener
                }

                if (value != null) {
                    favList.clear()
                    for (document in value) {
                        val fav = document.toObject(FavUsers::class.java)
                        favList.add(fav)
                    }
                    favAdapter.notifyDataSetChanged()
                    loadingDialog.dismiss()
                }
                binding.rvFavourite.visibility = View.VISIBLE
                if (recyclerView.adapter?.itemCount == 0) {
                    binding.noData.visibility = View.VISIBLE
                } else {
                    binding.noData.visibility = View.GONE
                }
            }
        }.addOnFailureListener { e ->
            Log.e("TAG", "onFailure: ${e.message}")
            loadingDialog.dismiss()
        }
    }
}