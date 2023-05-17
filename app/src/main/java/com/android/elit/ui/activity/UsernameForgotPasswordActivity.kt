package com.android.elit.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityUsernameForgotPasswordBinding
import com.android.elit.repository.UsersRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class UsernameForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsernameForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var loadingDialog : LoadingDialog
    private val usersRepository = UsersRepository()
    private var storedVerificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsernameForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        loadingDialog = LoadingDialog(this)

        actionBar()
        editTextListener()
        btnSendVerificationCode()
        nextToChangePassword()
//        moveToResetPassword()
    }

    private fun actionBar(){
        binding.apply {
            ivBackToolbar.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun sendVerificationCode(phone: String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun resendVerificationCode(phone: String, token: PhoneAuthProvider.ForceResendingToken){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(token)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private fun verifyVerificationCode(verificationCode: String){
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, verificationCode)
        signInWithPhoneAuthCredential(credential)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.e("VERIF CODE", "onVerificationFailed: ${e.message}")
            Toast.makeText(this@UsernameForgotPasswordActivity, e.message, Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(verifId: String, token: PhoneAuthProvider.ForceResendingToken) {
            storedVerificationId = verifId
            resendToken = token
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    firebaseUser = auth.currentUser!!
                    startActivity(Intent(this, ForgotPasswordActivity::class.java))
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Invalid Code", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

    }

    private fun btnSendVerificationCode(){
        binding.apply {
            btnSendCode.setOnClickListener {
                val phone = inpPhone.text.toString().trim()
                sendVerificationCode(phone)
            }
        }
    }

    private fun nextToChangePassword(){
        binding.apply {
            btnNext.setOnClickListener {
                val verificationCode = inpUsername.text.toString().trim()
                verifyVerificationCode(verificationCode)
            }
        }
    }

//    private fun sendCodePhone(){
//        binding.apply {
//            btnNext.setOnClickListener {
//                loadingDialog.show()
//                val phone = inpPhone.text.toString().trim()
//
//                if (phone.isEmpty()) {
//                    inpPhone.error = getString(R.string.alert_phone)
//                    inpPhone.requestFocus()
//                    return@setOnClickListener
//                }
//
//                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
//                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                        auth.signInWithCredential(credential)
//                            .addOnSuccessListener {
//                                loadingDialog.dismiss()
//                                firebaseUser = auth.currentUser!!
//                                startActivity(
//                                    Intent(
//                                        this@UsernameForgotPasswordActivity,
//                                        ForgotPasswordActivity::class.java
//                                    )
//                                )
//                            } .addOnFailureListener {
//                                loadingDialog.dismiss()
//                                Toast.makeText(
//                                    this@UsernameForgotPasswordActivity,
//                                    getString(R.string.phone_not_found),
//                                    Toast.LENGTH_SHORT
//                                ).show() }
//                    }
//
//                    override fun onVerificationFailed(e: FirebaseException) {
//                        TODO("Not yet implemented")
//                    }
//
//                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
//                        super.onCodeSent(verificationId, token)
//                    }
//
//                }
//            }
//        }
//    }

//    private fun moveToResetPassword(){
//        binding.apply {
//            btnNext.setOnClickListener {
//                loadingDialog.show()
//                val username = inpUsername.text.toString().trim()
//
//                if (username.isEmpty()) {
//                    inpUsername.error = getString(R.string.alert_username)
//                    inpUsername.requestFocus()
//                    return@setOnClickListener
//                }
//
//                usersRepository.getUsers().get().addOnSuccessListener { documents ->
//                    for (document in documents) {
//                        if (document.exists()){
//                            val usernameDoc = document.data["username"].toString()
//                            val emailDoc = document.data["email"].toString()
//                            val passwordDoc = document.data["password"].toString()
//                            if (usernameDoc == username) {
//                                auth.signInWithEmailAndPassword(
//                                    emailDoc,
//                                    passwordDoc
//                                )
//                                    .addOnSuccessListener {
//                                        loadingDialog.dismiss()
//                                        firebaseUser = auth.currentUser!!
//                                        startActivity(
//                                            Intent(
//                                                this@UsernameForgotPasswordActivity,
//                                                ForgotPasswordActivity::class.java
//                                            )
//                                        )
//                                    } .addOnFailureListener {
//                                        loadingDialog.dismiss()
//                                        Toast.makeText(
//                                            this@UsernameForgotPasswordActivity,
//                                            getString(R.string.username_not_found),
//                                            Toast.LENGTH_SHORT
//                                        ).show() }
//                            }
//                        }else{
//                            loadingDialog.dismiss()
//                            Toast.makeText(
//                                this@UsernameForgotPasswordActivity,
//                                getString(R.string.username_not_found),
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                    loadingDialog.dismiss()
//                }.addOnFailureListener {
//                    loadingDialog.dismiss()
//                    Toast.makeText(
//                        this@UsernameForgotPasswordActivity,
//                        getString(R.string.username_not_found),
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
//    }

    private fun editTextListener() {
        setBtnEnable()
        binding.apply {
            inpPhone.addTextChangedListener {
                setBtnEnable()
            }
        }
    }

    private fun setBtnEnable() {
        binding.apply {
            val username = inpPhone.text.toString().trim()

            btnNext.isEnabled =
                username.isNotEmpty()
        }
    }
}