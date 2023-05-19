package com.android.elit.ui.activity

import android.R.color
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.widget.NestedScrollView
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityDetailBookBinding
import com.android.elit.dataclass.Books
import com.android.elit.repository.BooksRepository
import com.android.elit.repository.UsersRepository
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs

class DetailBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBookBinding
    private lateinit var auth: FirebaseAuth
    private val loadDialog = LoadingDialog(this)
    private val bookRepository = BooksRepository()
    private val userRepository = UsersRepository()

    companion object {
        const val EXTRA_ID_BOOK = "id_book"
        const val DETAIL_TITLE = "detail_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Load Dialog
        loadDialog.show()

        actionBar()
        getDetailBook()
        checkedFav()
        toggleFav()
        downloadButton()
    }

    private fun actionBar() {

        fun TextView.setTextEllipsis(text: String, maxLength: Int) {
            if (text.length > maxLength) {
                val truncatedText = text.substring(0, maxLength - 3) + "..."
                this.text = truncatedText
            } else {
                this.text = text
            }
        }

        val startColor = ContextCompat.getColor(this, color.transparent)
        val endColor = ContextCompat.getColor(this, R.color.second_color)
        val startColorText = ContextCompat.getColor(this, color.black)
        val endColorText = ContextCompat.getColor(this, color.white)
        val startColorButton = ContextCompat.getColor(this, R.color.black)
        val endColorButton = ContextCompat.getColor(this, R.color.white)
        val toolbar: Toolbar = binding.mainToolbar
        val scrollView: NestedScrollView = binding.nestedScrollView
        val backButton = binding.ivBackToolbar
        val textView = binding.tvTitleToolbar

        val bookId = intent.getStringExtra(EXTRA_ID_BOOK)
        val bookRef = bookRepository.getBooksById(bookId ?: "")
        bookRef.get().addOnSuccessListener {
            if (it != null) {
                val title = it.data?.get("title").toString()
                textView.setTextEllipsis(title, 40)
                textView.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            val alpha = abs(1f.coerceAtMost(scrollY / toolbar.height.toFloat()))
            toolbar.setBackgroundColor(ColorUtils.blendARGB(startColor, endColor, alpha))
            textView.setTextColor(ColorUtils.blendARGB(startColorText, endColorText, alpha))
            backButton.setColorFilter(ColorUtils.blendARGB(startColorButton, endColorButton, alpha))
        })
    }

    private fun getDetailBook() {
        val bookId = intent.getStringExtra(EXTRA_ID_BOOK)
        binding.detailImage.scaleType = ImageView.ScaleType.FIT_XY

        val bookRef = bookRepository.getBooksById(bookId ?: "")
        bookRef.get().addOnSuccessListener {
            if (it != null) {
                val title = it.data?.get("title").toString()
                val author = it.data?.get("author").toString()
                val genre = it.data?.get("genre").toString()
                val desc = it.data?.get("description").toString()
                val image = it.data?.get("image").toString()

                binding.apply {
                    detailTitle.text = title
                    detailAuthor.text = author
                    detailGenre.text = genre
                    detailDescription.text = desc
                    Glide.with(this@DetailBookActivity)
                        .load(image)
                        .into(detailImage)
                }
            }
        }
    }

    private fun downloadButton() {
        binding.btnDownloadBook.setOnClickListener {
            downloadPdfManager()
        }
    }

    private fun downloadPdfManager() {
        val bookId = intent.getStringExtra(EXTRA_ID_BOOK)

        loadDialog.show()
        val bookRef = bookRepository.getBooksById(bookId ?: "")
        bookRef.get().addOnSuccessListener {
            if (it != null){
                val pdfUrl = it.data?.get("pdfUrl").toString()
                bookRepository.downloadPdf(bookId.toString(), pdfUrl).get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            for (doc in document) {
                                val url = doc.getString("pdfUrl")
                                downloadPdfUrlManager(url)
                            }
                        } else {
                            Log.d("TAG", "No such document")
                        }
                    }.addOnFailureListener { exception ->
                        Log.d("TAG", "get failed with ", exception)
                    }
            }
        }

    }

    private fun downloadPdfUrlManager(uri: String?) {
        val title = intent.getStringExtra(DETAIL_TITLE)
        val request = DownloadManager.Request(Uri.parse(uri))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle(title)
        request.setDescription("Downloading Novel $title")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalFilesDir(
            this,
            Environment.DIRECTORY_DOWNLOADS,
            "$title.pdf"
        )

        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val download = manager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (download == intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                    Log.d("TAG", "onReceive: Download Complete")
                    loadDialog.dismiss()
                }
            }
        }

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun toggleFav() {
        binding.toggleFav.setOnClickListener {

            val userId = auth.currentUser?.uid
            val bookId = intent.getStringExtra(EXTRA_ID_BOOK)

            val favRef = userRepository.getFavoriteBooks(userId.toString()).document(bookId.toString())

            favRef.get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        removeFromFav()
                        binding.toggleFav.background =
                            ContextCompat.getDrawable(this, R.drawable.ic_fav_inactive)
                    } else {
                        addToFav()
                        binding.toggleFav.background =
                            ContextCompat.getDrawable(this, R.drawable.ic_fav_active)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        getString(R.string.failed_to_check_fav),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun checkedFav() {
        val userId = auth.currentUser?.uid
        val bookId = intent.getStringExtra(EXTRA_ID_BOOK)

        val favRef = userRepository.getFavoriteBooks(userId.toString()).document(bookId.toString())

        favRef.get()
            .addOnSuccessListener {
                if (it.exists()) {
                    loadDialog.dismiss()
                    binding.toggleFav.background =
                        ContextCompat.getDrawable(this, R.drawable.ic_fav_active)
                } else {
                    loadDialog.dismiss()
                    binding.toggleFav.background =
                        ContextCompat.getDrawable(this, R.drawable.ic_fav_inactive)
                }
            }
            .addOnFailureListener {
                loadDialog.dismiss()
                Toast.makeText(this, getString(R.string.failed_to_check_fav), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun addToFav() {
        val userId = auth.currentUser?.uid
        val bookId = intent.getStringExtra(EXTRA_ID_BOOK)

        val favRef = userRepository.getFavoriteBooks(userId.toString()).document(bookId.toString())

        val bookRef = bookRepository.getBooksById(bookId ?: "")
        bookRef.get().addOnSuccessListener {
            if (it != null) {
                val title = it.data?.get("title").toString()
                val author = it.data?.get("author").toString()
                val genre = it.data?.get("genre").toString()
                val desc = it.data?.get("description").toString()
                val image = it.data?.get("image").toString()
                val pdfUrl = it.data?.get("pdfUrl").toString()

                val book = Books(
                    bookId,
                    image,
                    title,
                    author,
                    desc,
                    genre,
                    pdfUrl,
                    true
                )

                favRef.set(book)
                    .addOnSuccessListener {
                        loadDialog.dismiss()
                        Toast.makeText(
                            this,
                            getString(R.string.success_add_to_fav),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    .addOnFailureListener {
                        loadDialog.dismiss()
                        Toast.makeText(
                            this,
                            getString(R.string.failed_add_to_fav),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
            }
        }
    }

    private fun removeFromFav() {
        val userId = auth.currentUser?.uid
        val bookId = intent.getStringExtra(EXTRA_ID_BOOK)

        val favRef = userRepository.getFavoriteBooks(userId.toString()).document(bookId.toString())

        favRef.delete()
            .addOnSuccessListener {
                loadDialog.dismiss()
                Toast.makeText(this, getString(R.string.sucess_remove_from_fav), Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                loadDialog.dismiss()
                Toast.makeText(this, getString(R.string.failed_remove_from_fav), Toast.LENGTH_SHORT)
                    .show()
            }
    }
}