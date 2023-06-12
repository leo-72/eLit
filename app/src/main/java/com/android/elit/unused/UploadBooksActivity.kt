package com.android.elit.unused

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityUploadBooksBinding
import com.android.elit.repository.BooksRepository
import com.android.elit.ui.activity.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*


@Suppress("DEPRECATION")
class UploadBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBooksBinding
    private lateinit var storageImagesRef: StorageReference
    private lateinit var storagePdfsRef: StorageReference
    private lateinit var bookRepository: BooksRepository
    private lateinit var fs: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var imageUri: Uri
    private lateinit var pdfUri: Uri
    private val loadDialog = LoadingDialog(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookRepository = BooksRepository()

        fs = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.imageBooks.setBackgroundResource(R.drawable.no_image)

        actionBar()
        editTextListener()
        initVarsImages()
        initVarsPdf()
        selectImage()
        selectPdf()
        selectGenre()
        uploadBook()
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

            btnUploadBook.isEnabled =
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

    private fun uploadBook() {
        storageImagesRef = storageImagesRef.child(System.currentTimeMillis().toString())
        storagePdfsRef = storagePdfsRef.child("${UUID.randomUUID()}")
        pdfUri = Uri.parse("")
        binding.apply {
            btnUploadBook.setOnClickListener {
                loadDialog.show()
                storageImagesRef.putFile(imageUri).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageImagesRef.downloadUrl.addOnSuccessListener { imageUri ->
                            storagePdfsRef.putFile(pdfUri).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    storagePdfsRef.downloadUrl.addOnSuccessListener { pdfUri ->
                                        val title = tiBookTitle.editText?.text.toString().trim()
                                        val author = tiBookAuthor.editText?.text.toString().trim()
                                        val desc = tiBookDesc.editText?.text.toString().trim()
                                        val genre = tiBookGenre.editText?.text.toString().trim()
                                        val bookId = generateRandomId()
                                        val data = hashMapOf(
                                            "id" to bookId,
                                            "title" to title,
                                            "author" to author,
                                            "description" to desc,
                                            "genre" to genre,
                                            "image" to imageUri.toString(),
                                            "pdfUrl" to pdfUri.toString()
                                        )
                                        fs.collection("books").add(data)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    loadDialog.dismiss()
                                                    Toast.makeText(
                                                        this@UploadBooksActivity,
                                                        getString(R.string.upload_success),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    updateUI()
                                                } else {
                                                    loadDialog.dismiss()
                                                    Toast.makeText(
                                                        this@UploadBooksActivity,
                                                        getString(R.string.upload_failed),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    updateUI()
                                                }
                                            }
                                    }
                                }
                            }
                        }
                    } else {
                        loadDialog.dismiss()
                        Toast.makeText(
                            this@UploadBooksActivity,
                            getString(R.string.image_null),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                validImage()
                validPdf()
            }
        }
    }

    private fun generateRandomId(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..20)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun validPdf() {
        binding.apply {
            if (pdfUri == Uri.parse("")) {
                loadDialog.dismiss()
                Toast.makeText(
                    this@UploadBooksActivity,
                    getString(R.string.please_select_pdf),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun validImage() {
        binding.apply {
            if (imageUri == Uri.parse("")) {
                loadDialog.dismiss()
                Toast.makeText(
                    this@UploadBooksActivity,
                    getString(R.string.image_null),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun selectImage() {
        imageUri = Uri.parse("")
        binding.apply {
            btnSelectImage.setOnClickListener {
                resultImage.launch("image/*")
                imageBooks.background = ContextCompat.getDrawable(
                    this@UploadBooksActivity,
                    R.drawable.no_image
                )
            }
        }
    }

    private fun selectPdf() {
        binding.apply {
            btnSelectPdf.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/pdf"
                startActivityForResult(intent, 99)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 && resultCode == RESULT_OK && data != null) {
            pdfUri = data.data!!
            displayFileName(pdfUri)
        }
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

    private fun selectGenre() {
        val inputGenre: AutoCompleteTextView = binding.etBookGenre
        val genre = listOf(
            "Romance",
            "Fiction",
            "Religion"

        )
        val adapter = ArrayAdapter(this, R.layout.item_genre, genre)
        inputGenre.setAdapter(adapter)
    }

    private fun updateUI() {
        binding.apply {
            val intent = Intent(this@UploadBooksActivity, UploadBooksActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private val resultImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it!!
        binding.imageBooks.setImageURI(imageUri)
    }

    private fun actionBar() {
        val toolbar = binding.mainToolbar
        val logout = binding.ivLogoutToolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        logout.setOnClickListener {
            val user = auth.currentUser
            if (user != null) {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.no_user), Toast.LENGTH_SHORT).show()
            }
        }
    }
}