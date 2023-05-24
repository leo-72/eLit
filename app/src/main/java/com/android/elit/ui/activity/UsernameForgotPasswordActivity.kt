package com.android.elit.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityUsernameForgotPasswordBinding
import com.android.elit.repository.UsersRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class UsernameForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsernameForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var loadingDialog: LoadingDialog
    private val usersRepository = UsersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsernameForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        loadingDialog = LoadingDialog(this)

        actionBar()
        editTextListener()
        moveToResetPassword()


        binding.apply {
            tvHaveAcc.setOnClickListener {
                startActivity(Intent(this@UsernameForgotPasswordActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun actionBar() {
        binding.apply {
            ivBackToolbar.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun moveToResetPassword() {
        binding.apply {
            btnNext.setOnClickListener {
                loadingDialog.show()
                val username = inpUsername.text.toString().trim()

                if (username.isEmpty()) {
                    inpUsername.error = getString(R.string.alert_username)
                    inpUsername.requestFocus()
                    return@setOnClickListener
                }

                usersRepository.getUsers().get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        if (document.exists()) {
                            val usernameDoc = document.data["username"].toString()
                            val emailDoc = document.data["email"].toString()
                            val passwordDoc = document.data["password"].toString()
                            if (usernameDoc == username) {
                                auth.signInWithEmailAndPassword(
                                    emailDoc,
                                    passwordDoc
                                )
                                    .addOnSuccessListener {
                                        loadingDialog.dismiss()
                                        firebaseUser = auth.currentUser!!
                                        startActivity(
                                            Intent(
                                                this@UsernameForgotPasswordActivity,
                                                ForgotPasswordActivity::class.java
                                            )
                                        )
                                    }.addOnFailureListener {
                                        loadingDialog.dismiss()
                                        Toast.makeText(
                                            this@UsernameForgotPasswordActivity,
                                            getString(R.string.username_not_found),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            loadingDialog.dismiss()
                            Toast.makeText(
                                this@UsernameForgotPasswordActivity,
                                getString(R.string.username_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    loadingDialog.dismiss()
                }.addOnFailureListener {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@UsernameForgotPasswordActivity,
                        getString(R.string.username_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun editTextListener() {
        setBtnEnable()
        binding.apply {
            inpUsername.addTextChangedListener {
                setBtnEnable()
            }
        }
    }

    private fun setBtnEnable() {
        binding.apply {
            val username = inpUsername.text.toString().trim()

            btnNext.isEnabled =
                username.isNotEmpty()
        }
    }
}