package com.android.elit.admin.fragment

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.FragmentAddBooksBinding
import com.android.elit.dataclass.Books
import com.android.elit.repository.BooksRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class AddBooksFragment : Fragment() {

    private lateinit var _binding: FragmentAddBooksBinding
    private val binding get() = _binding
    private lateinit var storageImagesRef: StorageReference
    private lateinit var storagePdfsRef: StorageReference
    private lateinit var bookRepository: BooksRepository
    private lateinit var fs: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var imageUri: Uri
    private lateinit var pdfUri: Uri
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBooksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddBooksBinding.bind(view)

        bookRepository = BooksRepository()

        fs = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        loadingDialog = LoadingDialog(requireContext())

        binding.imageBooks.setBackgroundResource(R.drawable.no_image)

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
        storageImagesRef = storageImagesRef.child("images/${System.currentTimeMillis()}.jpg")
        storagePdfsRef = storagePdfsRef.child("pdfs/${System.currentTimeMillis()}.pdf")
        pdfUri = Uri.parse("")
        binding.apply {
            btnUploadBook.setOnClickListener {
                loadingDialog.show()
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
                                        val data = Books(
                                            "",
                                            imageUri.toString(),
                                            title,
                                            author,
                                            desc,
                                            genre,
                                            pdfUri.toString()
                                        )
                                        fs.collection("books").add(data)
                                            .addOnCompleteListener { task ->
                                                val bookId = task.result?.id
                                                if (task.isSuccessful) {
                                                    bookRepository.getBooks().document(bookId.toString())
                                                        .update("id", bookId.toString())
                                                        .addOnSuccessListener {
                                                            loadingDialog.dismiss()
                                                            Toast.makeText(
                                                                requireContext(),
                                                                getString(R.string.upload_success),
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            clearFillInput()
                                                        }.addOnFailureListener {
                                                            loadingDialog.dismiss()
                                                            Toast.makeText(
                                                                requireContext(),
                                                                getString(R.string.upload_failed),
                                                                Toast.LENGTH_SHORT
                                                            ).show() }
                                                } else {
                                                    loadingDialog.dismiss()
                                                    Toast.makeText(
                                                        requireContext(),
                                                        getString(R.string.upload_failed),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                }else {
                                    validPdf()
                                }
                            }
                        }
                    } else {
                        validImage()
                    }
                }
            }
        }
    }

    private fun clearFillInput(){
        binding.apply {
            tiBookTitle.editText?.setText("")
            tiBookAuthor.editText?.setText("")
            tiBookDesc.editText?.setText("")
            imageBooks.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.no_image
            )
            imageBooks.setImageURI(null)
            namePdf.text = getString(R.string.filename_pdf)
            imageUri = Uri.EMPTY
            pdfUri = Uri.EMPTY
        }
    }

    private fun validPdf() {
        binding.apply {
            if (pdfUri == Uri.parse("")) {
                loadingDialog.dismiss()
                Toast.makeText(
                    requireContext(),
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
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), getString(R.string.image_null), Toast.LENGTH_SHORT)
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
                    requireContext(),
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

    private fun displayFileName(uri: Uri?) {
        try {
            val cursor: Cursor? = requireActivity().contentResolver.query(uri!!, null, null, null, null)
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
        binding.apply {
            val genreArray = resources.getStringArray(R.array.genres)
            val genreAdapter = ArrayAdapter(requireContext(), R.layout.item_genre, genreArray)

            etBookGenre.setAdapter(genreAdapter)
        }
    }

    private val resultImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri!=null){
            imageUri = uri
            binding.imageBooks.setImageURI(imageUri)
        }
    }
}