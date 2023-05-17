package com.android.elit.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityChangePasswordActvityBinding
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordActvityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordActvityBinding.inflate(layoutInflater)
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

    private fun changePassword(){
        binding.apply {
            btnUpdatePassword.setOnClickListener {
                loadingDialog.show()
                val userId = auth.currentUser?.uid
                val userDocRef = fs.collection("users").document(userId ?: "")
                val oldPassword = inpOldPass.text.toString().trim()
                val newPassword = inpNewPass.text.toString().trim()
                val confirmPassword = inpConfPass.text.toString().trim()

                if (oldPassword.isEmpty()){
                    loadingDialog.dismiss()
                    inpOldPass.error = getString(R.string.alert_old_password)
                    inpOldPass.requestFocus()
                    return@setOnClickListener
                }

                if (newPassword.isEmpty()){
                    loadingDialog.dismiss()
                    inpNewPass.error = getString(R.string.alert_new_password)
                    inpNewPass.requestFocus()
                    return@setOnClickListener
                }

                if (confirmPassword.isEmpty()){
                    loadingDialog.dismiss()
                    inpConfPass.error = getString(R.string.alert_confirm_password)
                    inpConfPass.requestFocus()
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword){
                    loadingDialog.dismiss()
                    inpConfPass.error = getString(R.string.alert_password_not_match)
                    inpConfPass.requestFocus()
                    return@setOnClickListener
                }

                userDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()){
                        val user = auth.currentUser
                        val currentEmail = document.getString("email")
                        val oldPass = document.getString("password")

                        if (oldPass == oldPassword){
                            val credentials : AuthCredential = EmailAuthProvider.getCredential(currentEmail!!, oldPass)
                            user?.reauthenticate(credentials)
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful){
                                        user.updatePassword(newPassword)
                                            .addOnSuccessListener {
                                                userDocRef.update("password", newPassword)
                                                    .addOnSuccessListener {
                                                        loadingDialog.dismiss()
                                                        Toast.makeText(this@ChangePasswordActivity, getString(
                                                            R.string.password_has_been_updated
                                                        ), Toast.LENGTH_SHORT).show()
                                                        auth.signOut()
                                                        finishAffinity()
                                                        Intent(this@ChangePasswordActivity, LoginActivity::class.java).also {
                                                            startActivity(it)
                                                        }
                                                    }.addOnFailureListener {
                                                        loadingDialog.dismiss()
                                                        Toast.makeText(this@ChangePasswordActivity, getString(
                                                            R.string.password_failed_to_updated
                                                        ), Toast.LENGTH_SHORT).show()
                                                    }
                                            }.addOnFailureListener {
                                                loadingDialog.dismiss()
                                                Toast.makeText(this@ChangePasswordActivity, getString(
                                                    R.string.password_failed_to_updated
                                                ), Toast.LENGTH_SHORT).show()
                                            }
                                    }else{
                                        loadingDialog.dismiss()
                                        Toast.makeText(this@ChangePasswordActivity, getString(R.string.password_failed_to_updated), Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }else{
                            loadingDialog.dismiss()
                            Toast.makeText(this@ChangePasswordActivity, getString(R.string.old_password_not_match), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }
    }

    private fun editTextListener() {
        setBtnEnable()
        binding.apply {
            inpOldPass.addTextChangedListener {
                setBtnEnable()
            }
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
            val oldPass = inpOldPass.text.toString().trim()
            val newPass = inpNewPass.text.toString().trim()
            val confPass = inpConfPass.text.toString().trim()

            btnUpdatePassword.isEnabled =
                oldPass.isNotEmpty() && newPass.isNotEmpty() && confPass.isNotEmpty()
        }
    }
}