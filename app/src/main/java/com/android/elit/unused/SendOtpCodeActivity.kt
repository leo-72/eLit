package com.android.elit.unused

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivitySendOtpCodeBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class SendOtpCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySendOtpCodeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendOtpCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        loadingDialog = LoadingDialog(this)

        actionBar()
        editTextListener()
        sendOTP()
    }

    private fun actionBar() {
        binding.apply {
            ivBackToolbar.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun sendOTP() {
        binding.apply {
            btnSendOTP.setOnClickListener {
                val phone = inpPhone.text.toString().trim()

                if (phone.length < 10) {
                    inpPhone.error = getString(R.string.alert_phone)
                    inpPhone.requestFocus()
                    return@setOnClickListener
                } else {
                    otpSend()
                }
            }
        }
    }

    private fun otpSend() {
        val phone = binding.inpPhone.text.toString().trim()
        val countryCode = "+62"
        val countDownOTP: Long = 60

        binding.apply {
            callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@SendOtpCodeActivity,
                        e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Toast.makeText(
                        this@SendOtpCodeActivity,
                        getString(R.string.otp_sent),
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(
                        this@SendOtpCodeActivity,
                        VerifyOtpActivity::class.java
                    )
                    intent.putExtra("countryCode", countryCode)
                    intent.putExtra("phone", phone)
                    intent.putExtra("verificationId", verificationId)
                    intent.putExtra("countDownOTP", countDownOTP)
                    startActivity(intent)
                }
            }
            PhoneAuthProvider.verifyPhoneNumber(
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(countryCode + phone)
                    .setTimeout(countDownOTP, TimeUnit.SECONDS)
                    .setActivity(this@SendOtpCodeActivity)
                    .setCallbacks(callbacks)
                    .build()
            )
        }
    }

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

            btnSendOTP.isEnabled =
                username.isNotEmpty()
        }
    }
}