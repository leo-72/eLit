package com.android.elit.ui.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityRegisterBinding
import com.android.elit.repository.UsersRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var database: DatabaseReference
    private val loadingDialog = LoadingDialog(this)
    private val usersRepository = UsersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        binding.apply {
            txtHaveAcc.setOnClickListener {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java)) }
        }

        editTextListener()
        registrationUser()

    }

    private fun registrationUser() {
        binding.apply {
            btnRegister.setOnClickListener {
                val fullname = inpFullname.text.toString().trim()
                val email = inpEmail.text.toString().trim()
                val phone = inpPhone.text.toString().trim()
                val username = inpUsername.text.toString().trim()
                val password = inpPass.text.toString().trim()
                val confirmPassword = inpConfirmPass.text.toString().trim()
                val role = "user"

                if (fullname.isEmpty()) {
                    inpFullname.error = getString(R.string.alert_fullname)
                    inpFullname.requestFocus()
                    return@setOnClickListener
                }
                if (email.isEmpty()) {
                    inpEmail.error = getString(R.string.alert_email)
                    inpEmail.requestFocus()
                    return@setOnClickListener
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    inpEmail.error = getString(R.string.alert_valid_email)
                    inpEmail.requestFocus()
                    return@setOnClickListener
                }
                if (phone.isEmpty()) {
                    inpPhone.error = getString(R.string.alert_phone)
                    inpPhone.requestFocus()
                    return@setOnClickListener
                }
                if (!Patterns.PHONE.matcher(phone).matches()) {
                    inpPhone.error = getString(R.string.alert_valid_phone)
                    inpPhone.requestFocus()
                    return@setOnClickListener
                }
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
                if (confirmPassword.isEmpty()) {
                    inpConfirmPass.error = getString(R.string.alert_confirm_password)
                    inpConfirmPass.requestFocus()
                    return@setOnClickListener
                }
                if (confirmPassword != password) {
                    inpConfirmPass.error = getString(R.string.alert_password_not_match)
                    inpConfirmPass.requestFocus()
                    return@setOnClickListener
                }

                loadingDialog.show()

                checkEmail(email) { isEmailAvailable ->
                    if (isEmailAvailable) {
                        checkPhone(phone) { isPhoneAvailable ->
                            if (isPhoneAvailable) {
                                checkUsername(username) { isUsernameAvailable ->
                                    if (isUsernameAvailable) {
                                        createUserWithEmailAndPassword(
                                            email,
                                            password,
                                            fullname,
                                            phone,
                                            username,
                                            role
                                        )
                                    } else {
                                        inpUsername.error = getString(R.string.username_already_registered)
                                        inpUsername.requestFocus()
                                        loadingDialog.dismiss()
                                    }
                                }
                            } else {
                                inpPhone.error = getString(R.string.phone_already_registered)
                                inpPhone.requestFocus()
                                loadingDialog.dismiss()
                            }
                        }
                    } else {
                        inpEmail.error = getString(R.string.email_already_registered)
                        inpEmail.requestFocus()
                        loadingDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        fullname: String,
        phone: String,
        username: String,
        role: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@RegisterActivity) { task ->
                if (task.isSuccessful) {
                    firebaseUser = auth.currentUser!!
                    val user = hashMapOf(
                        "id" to firebaseUser.uid,
                        "fullname" to fullname,
                        "email" to email,
                        "phone" to phone,
                        "username" to username,
                        "password" to password,
                        "role" to role
                    )
                    usersRepository.addUsers(firebaseUser.uid, user)
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.toast_registration_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.toast_registration_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun checkUsername(username: String, callback: (Boolean) -> Unit) {
        usersRepository.getUsers().whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { result ->
                val isUsernameAvailable = result.isEmpty
                callback(isUsernameAvailable)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
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
                Log.w(TAG, "Error getting documents: ", exception)
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
            inpUsername.addTextChangedListener {
                setBtnEnable()
            }
            inpPass.addTextChangedListener {
                setBtnEnable()
            }
            inpConfirmPass.addTextChangedListener {
                setBtnEnable()
            }
        }
    }

    private fun setBtnEnable() {
        binding.apply {
            val fullname = inpFullname.text.toString().trim()
            val email = inpEmail.text.toString().trim()
            val phone = inpPhone.text.toString().trim()
            val username = inpUsername.text.toString().trim()
            val password = inpPass.text.toString().trim()
            val confirmPassword = inpConfirmPass.text.toString().trim()

            btnRegister.isEnabled =
                fullname.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
        }
    }
}