package com.android.elit.ui.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityUpdateProfileBinding
import com.android.elit.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog
    private val usersRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        loadingDialog = LoadingDialog(this)
        loadingDialog.show()

        actionBar()
        setProfile()
        editTextListener()
        updateProfile()
    }

    private fun actionBar() {
        binding.apply {
            ivBackToolbar.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setProfile() {
        val userId = auth.currentUser?.uid
        val userDocRef = fs.collection("users").document(userId!!)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val name = document.getString("fullname")
                val email = document.getString("email")
                val phone = document.getString("phone")

                binding.inpFullname.setText(name)
                binding.inpEmail.setText(email)
                binding.inpPhone.setText(phone)
                loadingDialog.dismiss()
            }
        }
    }

    private fun updateProfile() {
        binding.apply {
            btnUpdateNow.setOnClickListener {
                loadingDialog.show()
                val fullname = inpFullname.text.toString().trim()
                val email = inpEmail.text.toString().trim()
                val phone = inpPhone.text.toString().trim()

                if (fullname.isEmpty()) {
                    inpFullname.error = getString(R.string.alert_fullname)
                    inpFullname.requestFocus()
                    loadingDialog.dismiss()
                    return@setOnClickListener
                }

                // Get the current user data
                val user = auth.currentUser
                val userId = auth.currentUser?.uid
                val userDocRef = usersRepository.getUserById(userId!!)

                userDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentEmail = document.getString("email")
                        val currentPhone = document.getString("phone")

                        // Check if email and phone are unchanged
                        val isEmailUnchanged = email == currentEmail
                        val isPhoneUnchanged = phone == currentPhone

                        // If email and phone are unchanged, update other fields and finish the update
                        if (isEmailUnchanged && isPhoneUnchanged) {
                            userDocRef.update("fullname", fullname)
                                .addOnSuccessListener {
                                    loadingDialog.dismiss()
                                    Toast.makeText(
                                        this@UpdateProfileActivity,
                                        getString(R.string.profile_has_been_updated),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onBackPressedDispatcher.onBackPressed()
                                }
                                .addOnFailureListener {
                                    loadingDialog.dismiss()
                                    Toast.makeText(
                                        this@UpdateProfileActivity,
                                        getString(R.string.failed_to_update_profile),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            return@addOnSuccessListener
                        }

                        // Check email availability if it has changed
                        if (!isEmailUnchanged) {
                            checkEmail(email) { isEmailAvailable ->
                                if (!isEmailAvailable) {
                                    inpEmail.error = getString(R.string.email_already_registered)
                                    inpEmail.requestFocus()
                                    loadingDialog.dismiss()
                                    return@checkEmail
                                }

                                // Email is available, proceed with updating the profile
                                updateUserProfile(user, userDocRef, fullname, email, phone)
                            }
                        } else {
                            // Check phone availability if it has changed
                            if (!isPhoneUnchanged) {
                                checkPhone(phone) { isPhoneAvailable ->
                                    if (!isPhoneAvailable) {
                                        inpPhone.error =
                                            getString(R.string.phone_already_registered)
                                        inpPhone.requestFocus()
                                        loadingDialog.dismiss()
                                        return@checkPhone
                                    }

                                    // Phone is available, proceed with updating the profile
                                    updateUserProfile(user, userDocRef, fullname, email, phone)
                                }
                            } else {
                                // No changes in email and phone, update other fields and finish the update
                                userDocRef.update("fullname", fullname)
                                    .addOnSuccessListener {
                                        loadingDialog.dismiss()
                                        Toast.makeText(
                                            this@UpdateProfileActivity,
                                            getString(R.string.profile_has_been_updated),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onBackPressedDispatcher.onBackPressed()
                                    }
                                    .addOnFailureListener {
                                        loadingDialog.dismiss()
                                        Toast.makeText(
                                            this@UpdateProfileActivity,
                                            getString(R.string.failed_to_update_profile),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUserProfile(
        user: FirebaseUser?,
        userDocRef: DocumentReference,
        fullname: String,
        email: String,
        phone: String
    ) {
        // Update email and phone
        user?.updateEmail(email)
            ?.addOnSuccessListener {
                userDocRef.update("fullname", fullname, "email", email, "phone", phone)
                    .addOnSuccessListener {
                        loadingDialog.dismiss()
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            getString(R.string.profile_has_been_updated),
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressedDispatcher.onBackPressed()
                    }
                    .addOnFailureListener {
                        loadingDialog.dismiss()
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            getString(R.string.failed_to_update_profile),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            ?.addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(
                    this@UpdateProfileActivity,
                    getString(R.string.failed_to_update_profile),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkEmail(email: String, callback: (Boolean) -> Unit) {
        usersRepository.getUsers().whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                val isEmailAvailable = result.isEmpty
                callback(isEmailAvailable)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun checkPhone(phone: String, callback: (Boolean) -> Unit) {
        usersRepository.getUsers().whereEqualTo("phone", phone)
            .get()
            .addOnSuccessListener { result ->
                val isPhoneAvailable = result.isEmpty
                callback(isPhoneAvailable)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun editTextListener() {
        setBtnEnable()
        binding.apply {
            inpFullname.addTextChangedListener {
                setBtnEnable()
            }
            inpEmail.addTextChangedListener {
                setBtnEnable()
            }
            inpPhone.addTextChangedListener {
                setBtnEnable()
            }
        }
    }

    private fun setBtnEnable() {
        binding.apply {
            val fullname = inpFullname.text.toString().trim()
            val email = inpEmail.text.toString().trim()
            val phone = inpPhone.text.toString().trim()

            btnUpdateNow.isEnabled =
                fullname.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty()
        }
    }
}