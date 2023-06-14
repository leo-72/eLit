package com.android.elit.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.elit.BooksActivity
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.adapter.BooksRecommendAdapter
import com.android.elit.adapter.GenreFictionAdapter
import com.android.elit.adapter.GenreRomanceAdapter
import com.android.elit.databinding.FragmentBooksBinding
import com.android.elit.dataclass.Books
import com.android.elit.dataclass.Fiction
import com.android.elit.dataclass.Romance
import com.android.elit.repository.BooksRepository
import com.android.elit.ui.activity.DetailBookActivity
import com.android.elit.ui.activity.SearchBookActivity
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.auth.FirebaseAuth


class BooksFragment : Fragment() {

    private lateinit var _binding: FragmentBooksBinding
    private val binding get() = _binding
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var recommendAdapter: BooksRecommendAdapter
    private lateinit var fictionAdapter: GenreFictionAdapter
    private lateinit var romanceAdapter: GenreRomanceAdapter
    private lateinit var loadingDialog: LoadingDialog
    private val booksList = ArrayList<Books>()
    private val booksFiction = ArrayList<Fiction>()
    private val booksRomance = ArrayList<Romance>()
    private val booksRepository = BooksRepository()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBooksBinding.bind(view)

        auth = FirebaseAuth.getInstance()

        loadingDialog = LoadingDialog(requireContext())

        binding.apply {
            tvMoreRecommend.setOnClickListener {
                val intent = Intent(activity, BooksActivity::class.java)
                startActivity(intent)
            }
        }

        appBar()
        imgSlider()
        setBooksRecommend()
        setBooksGenreFiction()
        setBooksGenreRomance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    private fun appBar() {
        binding.apply {
            etSearchBook.setOnClickListener {
                val intent = Intent(activity, SearchBookActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun imgSlider() {
        val imageSlider = binding.imageSlider
        val sliderItems = ArrayList<SlideModel>()

        sliderItems.add(SlideModel(R.drawable.img_slider1))
        sliderItems.add(SlideModel(R.drawable.img_slider4))
        sliderItems.add(SlideModel(R.drawable.img_slider3))
        sliderItems.add(SlideModel(R.drawable.img_slider5))
        sliderItems.add(SlideModel(R.drawable.img_slider2))

        imageSlider.setImageList(sliderItems, ScaleTypes.CENTER_CROP)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setBooksRecommend() {
        binding.imageViewRecommend.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.recommendbanner
            )
        )
        binding.imageViewRecommend.scaleType = ImageView.ScaleType.CENTER_CROP

        val onItemClick: (Books) -> Unit = { books ->
            val intent = Intent(requireContext(), DetailBookActivity::class.java)
            intent.putExtra(DetailBookActivity.EXTRA_ID_BOOK, books.id)
            startActivity(intent)
        }

        recommendAdapter = BooksRecommendAdapter(booksList, onItemClick)
        recyclerView = binding.rvRecommendNovel
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = recommendAdapter

        loadingDialog.show()
        booksRepository.getBooks().limit(8).get().addOnSuccessListener { result ->
            if (result != null) {
                val books = result.toObjects(Books::class.java)
                booksList.addAll(books)
                booksList.shuffle()
                recommendAdapter.notifyDataSetChanged()
                loadingDialog.dismiss()
            }
        }.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.failed_to_load_data),
                Toast.LENGTH_SHORT
            ).show()
            loadingDialog.dismiss()
        }
    }

    // Genre Fiction
    @SuppressLint("NotifyDataSetChanged")
    private fun setBooksGenreFiction() {
        binding.imageViewFiction.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.fictionbanner
            )
        )
        binding.imageViewFiction.scaleType = ImageView.ScaleType.CENTER_CROP

        val onItemClick: (Fiction) -> Unit = { fiction ->
            val intent = Intent(requireContext(), DetailBookActivity::class.java)
            intent.putExtra(DetailBookActivity.EXTRA_ID_BOOK, fiction.id)
            startActivity(intent)
        }

        fictionAdapter = GenreFictionAdapter(booksFiction, onItemClick)
        recyclerView = binding.rvGenreFiction
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = fictionAdapter


        booksRepository.getBooksByGenre("Fiction").get().addOnSuccessListener { result ->
            if (result != null) {
                val books = result.toObjects(Fiction::class.java)
                booksFiction.addAll(books)
                booksFiction.shuffle()
                fictionAdapter.notifyDataSetChanged()
            }
        }.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.failed_to_load_data),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Genre Romance
    @SuppressLint("NotifyDataSetChanged")
    private fun setBooksGenreRomance() {
        binding.imageViewRomance.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.romancebanner
            )
        )
        binding.imageViewRomance.scaleType = ImageView.ScaleType.CENTER_CROP

        val onItemClick: (Romance) -> Unit = { romance ->
            val intent = Intent(requireContext(), DetailBookActivity::class.java)
            intent.putExtra(DetailBookActivity.EXTRA_ID_BOOK, romance.id)
            startActivity(intent)
        }

        romanceAdapter = GenreRomanceAdapter(booksRomance, onItemClick)
        recyclerView = binding.rvGenreRomance
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = romanceAdapter


        booksRepository.getBooksByGenre("Romance").limit(8).get().addOnSuccessListener { result ->
            if (result != null) {
                val books = result.toObjects(Romance::class.java)
                booksRomance.addAll(books)
                booksRomance.shuffle()
                romanceAdapter.notifyDataSetChanged()
            }
        }.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                getString(R.string.failed_to_load_data),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}