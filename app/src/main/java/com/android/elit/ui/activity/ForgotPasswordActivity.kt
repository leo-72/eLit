package com.android.elit.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        loadingDialog = LoadingDialog(this)

        actionBar()
        editTextListener()
        changePassword()
    }

    private fun actionBar(){
        binding.apply {
            ivBackToolbar.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun changePassword() {
        binding.apply {
            btnUpdatePassword.setOnClickListener {
                val newPass = inpNewPass.text.toString().trim()
                val confirmPass = inpConfPass.text.toString().trim()

                if (newPass.isEmpty()) {
                    inpNewPass.error = getString(R.string.alert_new_password)
                    inpNewPass.requestFocus()
                    return@setOnClickListener
                }

                if (confirmPass.isEmpty()) {
                    inpConfPass.error = getString(R.string.alert_confirm_password)
                    inpConfPass.requestFocus()
                    return@setOnClickListener
                }

                if (newPass != confirmPass) {
                    inpConfPass.error = getString(R.string.alert_password_not_match)
                    inpConfPass.requestFocus()
                    return@setOnClickListener
                }

                val userId = auth.currentUser?.uid
                val userDocRef = fs.collection("users").document(userId ?: "")

                loadingDialog.show()

                userDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = auth.currentUser
                        val currentEmail = document.getString("email")
                        val currentPass = document.getString("password")
                        val credentials: AuthCredential = EmailAuthProvider.getCredential(currentEmail ?: "", currentPass ?: "")

                        user?.reauthenticate(credentials)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    user.updatePassword(newPass)
                                        .addOnSuccessListener {
                                            userDocRef.update("password", newPass)
                                                .addOnSuccessListener {
                                                    loadingDialog.dismiss()
                                                    Toast.makeText(
                                                        this@ForgotPasswordActivity,
                                                        getString(R.string.password_has_been_updated),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    startActivity(
                                                        Intent(
                                                            this@ForgotPasswordActivity,
                                                            LoginActivity::class.java
                                                        )
                                                    )
                                                    auth.signOut()
                                                    finish()
                                                }
                                                .addOnFailureListener {
                                                    loadingDialog.dismiss()
                                                    Toast.makeText(
                                                        this@ForgotPasswordActivity,
                                                        getString(R.string.password_failed_to_updated),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        }
                                        .addOnFailureListener {
                                            loadingDialog.dismiss()
                                            Toast.makeText(
                                                this@ForgotPasswordActivity,
                                                getString(R.string.password_failed_to_updated),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                } else {
                                    loadingDialog.dismiss()
                                    Toast.makeText(
                                        this@ForgotPasswordActivity,
                                        getString(R.string.password_failed_to_updated),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }
            }
        }
    }

    private fun editTextListener() {
        setBtnEnable()
        binding.apply {
            inpNewPass.addTextChangedListener {
                setBtnEnable()
            }
            inpConfPass.addTextChangedListener {
                setBtnEnable()
            }
        }
    }

    private fun setBtnEnable() {
        binding.apply {
            val newPass = inpNewPass.text.toString().trim()
            val confPass = inpConfPass.text.toString().trim()

            btnUpdatePassword.isEnabled =
                newPass.isNotEmpty() && confPass.isNotEmpty()
        }
    }
}