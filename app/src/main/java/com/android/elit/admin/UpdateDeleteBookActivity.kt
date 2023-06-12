package com.android.elit.admin

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityUpdateDeleteBookBinding
import com.android.elit.repository.BooksRepository
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UpdateDeleteBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateDeleteBookBinding
    private lateinit var storageImagesRef: StorageReference
    private lateinit var storagePdfsRef: StorageReference
    private lateinit var bookRepository: BooksRepository
    private lateinit var fs: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var imageUri: Uri
    private lateinit var pdfUri: Uri
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDeleteBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        bookRepository = BooksRepository()
        loadingDialog = LoadingDialog(this)
        imageUri = Uri.parse("")
        pdfUri = Uri.parse("")

        actionBar()
        retrieveBook()
        editTextListener()
        initVarsImages()
        initVarsPdf()
        selectImage()
        selectPdf()
        updateBook()
        deleteBook()
    }

    private fun actionBar() {
        binding.apply {
            ivBackToolbar.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun retrieveBook() {
        val bookId = intent.getStringExtra(EXTRA_ID_BOOK)
        val bookRef = bookRepository.getBooksById(bookId ?: "")
        bookRef.get().addOnSuccessListener {
            if (it != null) {
                val title = it.data?.get("title").toString()
                val author = it.data?.get("author").toString()
                val genre = it.data?.get("genre").toString()
                val desc = it.data?.get("description").toString()
                val image = it.data?.get("image").toString()
                val pdf = it.data?.get("pdfUrl").toString()
                val genreArray = resources.getStringArray(R.array.genres)
                val genreAdapter =
                    ArrayAdapter(this@UpdateDeleteBookActivity, R.layout.item_genre, genreArray)

                binding.apply {
                    tiBookTitle.editText?.setText(title)
                    tiBookAuthor.editText?.setText(author)
                    tiBookDesc.editText?.setText(desc)
                    tiBookGenre.editText?.setText(genre)
                    imageUri = Uri.parse(image)
                    pdfUri = Uri.parse(pdf)
                    Glide.with(this@UpdateDeleteBookActivity)
                        .load(imageUri)
                        .into(imageBooks)
                    namePdf.text = pdfUri.lastPathSegment
                    etBookGenre.setAdapter(genreAdapter)
                }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editTextListener() {
        setBtnEnable()
        binding.apply {
            tiBookTitle.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    setBtnEnable()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
            tiBookAuthor.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    setBtnEnable()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
            tiBookDesc.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    setBtnEnable()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
            tiBookGenre.editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    setBtnEnable()
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }
    }

    private fun setBtnEnable() {
        binding.apply {
            val title = tiBookTitle.editText?.text.toString().trim()
            val author = tiBookAuthor.editText?.text.toString().trim()
            val desc = tiBookDesc.editText?.text.toString().trim()
            val genre = tiBookGenre.editText?.text.toString().trim()

            btnUpdateBook.isEnabled =
                title.isNotEmpty() && author.isNotEmpty() && desc.isNotEmpty() && genre.isNotEmpty()
            btnDeleteBook.isEnabled =
                title.isNotEmpty() && author.isNotEmpty() && desc.isNotEmpty() && genre.isNotEmpty()
        }
    }

    private fun initVarsImages() {
        storageImagesRef = FirebaseStorage.getInstance().reference
        storageImagesRef.child("Images")
    }

    private fun initVarsPdf() {
        storagePdfsRef = FirebaseStorage.getInstance().reference
        storagePdfsRef.child("Pdf")
    }

    private fun updateBook() {
        binding.apply {

            btnUpdateBook.setOnClickListener {
                val bookId = intent.getStringExtra(EXTRA_ID_BOOK)
                val title = tiBookTitle.editText?.text.toString().trim()
                val author = tiBookAuthor.editText?.text.toString().trim()
                val desc = tiBookDesc.editText?.text.toString().trim()
                val genre = tiBookGenre.editText?.text.toString().trim()

                val bookRef = bookRepository.getBooksById(bookId ?: "")
                val updateData = HashMap<String, Any>()
                updateData["title"] = title
                updateData["author"] = author
                updateData["description"] = desc
                updateData["genre"] = genre

                loadingDialog.show()

                if ((imageUri != Uri.parse("") && imageUri != Uri.parse(
                        intent.getStringExtra(
                            EXTRA_IMAGE_BOOK
                        )
                    )) ||
                    (pdfUri != Uri.parse("") && pdfUri != Uri.parse(
                        intent.getStringExtra(
                            EXTRA_PDF_BOOK
                        )
                    ))
                ) {
                    if (imageUri != Uri.parse("") && imageUri != Uri.parse(
                            intent.getStringExtra(
                                EXTRA_IMAGE_BOOK
                            )
                        )
                    ) {
                        uploadImageAndUpdateBook(bookRef, updateData)
                    } else {
                        uploadPdfAndUpdateBook(bookRef, updateData)
                    }
                } else {
                    updateBookDetails(bookRef, updateData)
                }
            }
        }
    }

    private fun uploadImageAndUpdateBook(
        bookRef: DocumentReference,
        updateData: HashMap<String, Any>
    ) {
        val imageFilename = "images/${getFileName(imageUri)}"
        val imageRef = storageImagesRef.child(imageFilename)

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                updateData["image"] = downloadUri.toString()

                if (pdfUri != Uri.parse("") && pdfUri != Uri.parse(
                        intent.getStringExtra(
                            EXTRA_PDF_BOOK
                        )
                    )
                ) {
                    uploadPdfAndUpdateBook(bookRef, updateData)
                } else {
                    updateBookDetails(bookRef, updateData)
                }
            } else {
                loadingDialog.dismiss()
                Toast.makeText(
                    this@UpdateDeleteBookActivity,
                    "Failed to upload image.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uploadPdfAndUpdateBook(
        bookRef: DocumentReference,
        updateData: HashMap<String, Any>
    ) {
        val pdfFilename = "pdfs/${getFileName(pdfUri)}"
        val pdfRef = storagePdfsRef.child(pdfFilename)

        val uploadTask = pdfRef.putFile(pdfUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            pdfRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                updateData["pdfUrl"] = downloadUri.toString()

                updateBookDetails(bookRef, updateData)
            } else {
                loadingDialog.dismiss()
                Toast.makeText(
                    this@UpdateDeleteBookActivity,
                    "Failed to upload PDF.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateBookDetails(bookRef: DocumentReference, updateData: HashMap<String, Any>) {
        bookRef.update(updateData)
            .addOnSuccessListener {
                loadingDialog.dismiss()
                Toast.makeText(
                    this@UpdateDeleteBookActivity,
                    "Book updated successfully.",
                    Toast.LENGTH_SHORT
                ).show()
                onBackPressedDispatcher.onBackPressed()
            }
            .addOnFailureListener { e ->
                loadingDialog.dismiss()
                Toast.makeText(this@UpdateDeleteBookActivity, "Error: $e", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun deleteBook() {
        binding.apply {
            btnDeleteBook.setOnClickListener {
                val bookId = intent.getStringExtra(EXTRA_ID_BOOK)
                bookRepository.getBooksById(bookId.toString()).delete()
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Toast.makeText(
                            this@UpdateDeleteBookActivity,
                            getString(R.string.book_deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressedDispatcher.onBackPressed()
                    }.addOnFailureListener { e ->
                        loadingDialog.dismiss()
                        Toast.makeText(
                            this@UpdateDeleteBookActivity,
                            "Error: $e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun selectImage() {
        imageUri = Uri.parse("")
        binding.apply {
            btnSelectImage.setOnClickListener {
                resultImage.launch("image/*")
                imageBooks.background = ContextCompat.getDrawable(
                    this@UpdateDeleteBookActivity,
                    R.drawable.no_image
                )
            }
        }
    }

    private val pdfPickerActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedPdfUri: Uri? = data.data

                    if (selectedPdfUri != null) {
                        pdfUri = selectedPdfUri
                        displayFileName(pdfUri)
                    }
                }
            }
        }

    private fun selectPdf() {
        binding.apply {
            btnSelectPdf.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                pdfPickerActivityResult.launch(intent)
            }
        }
    }

    private fun getFileName(uri: Uri?): String? {
        var fileName: String? = null
        val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex: Int = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    fileName = it.getString(displayNameIndex)
                }
            }
        }
        return fileName
    }

    private fun displayFileName(uri: Uri?) {
        try {
            val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
            cursor?.moveToFirst()
            val nameIndex: Int = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)!!
            val fileName: String = cursor.getString(nameIndex)
            binding.namePdf.text = fileName
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val resultImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it!!
        binding.imageBooks.setImageURI(imageUri)
    }

    companion object {
        const val EXTRA_ID_BOOK = "id_book"
        const val EXTRA_IMAGE_BOOK = "image_book"
        const val EXTRA_PDF_BOOK = "pdf_book"
    }
}