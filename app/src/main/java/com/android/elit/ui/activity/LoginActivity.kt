package com.android.elit.ui.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.elit.*
import com.android.elit.admin.MainAdminActivity
import com.android.elit.databinding.ActivityLoginBinding
import com.android.elit.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private val loadingDialog = LoadingDialog(this)
    private val usersRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        changeLanguage()
        editTextListener()
        loginUser()
        moveToNewAcc()
        moveToForgotPassword()
    }

    private fun changeLanguage(){
        binding.apply {
            translate.setOnClickListener {
                val languageSettingsIntent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                languageSettingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(languageSettingsIntent)
            }
        }
    }

    private fun loginUser() {
        binding.apply {
            btnLogin.setOnClickListener {
                val username = inpUsername.text.toString().trim()
                val password = inpPass.text.toString().trim()

                if (username.isEmpty()) {
                    inpUsername.error = getString(R.string.alert_username)
                    inpUsername.requestFocus()
                    return@setOnClickListener
                }
                if (password.isEmpty()) {
                    inpPass.error = getString(R.string.alert_password)
                    inpPass.requestFocus()
                    return@setOnClickListener
                }
                if (password.length < 8) {
                    inpPass.error = getString(R.string.error_pass_char)
                    inpPass.requestFocus()
                    return@setOnClickListener
                }

                loadingDialog.show()
                usersRepository.getUsers().get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        if (document.data["username"] == username) {
                            if (document.data["role"] == "user") {
                                auth.signInWithEmailAndPassword(
                                    document.data["email"].toString(),
                                    password
                                )
                                    .addOnCompleteListener(this@LoginActivity) { task ->
                                        if (task.isSuccessful) {
                                            clearInput()
                                            firebaseUser = auth.currentUser!!
                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    MainActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else if (document.data["password"] != password) {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                getString(R.string.password_doesnt_match),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                getString(R.string.login_failed),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else if (document.data["role"] == "admin") {
                                auth.signInWithEmailAndPassword(
                                    document.data["email"].toString(),
                                    password
                                )
                                    .addOnCompleteListener(this@LoginActivity) { task ->
                                        if (task.isSuccessful) {
                                            clearInput()
                                            firebaseUser = auth.currentUser!!
                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    MainAdminActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else if (document.data["password"] != password) {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                getString(R.string.password_doesnt_match),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                getString(R.string.login_failed),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
                        }
                    }
                    loadingDialog.dismiss()
                }.addOnFailureListener {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    loadingDialog.dismiss()
                }
            }
        }
    }

    private fun editTextListener() {
        setBtnEnable()
        binding.apply {
            inpUsername.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //do nothing
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    setBtnEnable()
                }

                override fun afterTextChanged(p0: Editable?) {
                    //do nothing
                }
            })
            inpPass.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    //do nothing
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    setBtnEnable()
                }

                override fun afterTextChanged(p0: Editable?) {
                    //do nothing
                }
            })
        }
    }

    private fun setBtnEnable() {
        binding.apply {
            val email = inpUsername.text.toString()
            val pass = inpPass.text.toString()

            btnLogin.isEnabled =
                email.isNotEmpty() && pass.isNotEmpty() && pass.length >= 8
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.getOnBackPressedDispatcher().onBackPressed()
        finishAffinity()
    }

    private fun clearInput() {
        binding.apply {
            inpUsername.text?.clear()
            inpPass.text?.clear()
        }
    }

    private fun moveToNewAcc() {
        binding.btnNewAcc.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun moveToForgotPassword(){
        binding.txtForgotPass.setOnClickListener {
            startActivity(Intent(this, UsernameForgotPasswordActivity::class.java)) }
    }
}