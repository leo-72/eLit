package com.android.elit.unused

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.android.elit.LoadingDialog
import com.android.elit.R
import com.android.elit.databinding.ActivityVerifyOtpBinding
import com.android.elit.ui.activity.ForgotPasswordActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class VerifyOtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyOtpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs = FirebaseFirestore.getInstance()
        loadingDialog = LoadingDialog(this)

        binding.apply {
            val tvDescOTP = binding.tvDescOTP
            val descOTP = getString(R.string.description_otp)
            val countryCode = intent.getStringExtra("countryCode")
            val phone = intent.getStringExtra("phone")
            val updatedDescOTP = descOTP.replace(
                "%phone",
                "(" + countryCode.toString() + ")" + "-" + phone.toString()
            )
            tvDescOTP.text = updatedDescOTP

        }
        countDownOTP()
        editTextListener()
        verifyOTP()


    }

    private fun countDownOTP() {
        val initialCountdown: Long = 60 * 1000 // Waktu countdown awal dalam milidetik
        val countdownInterval: Long = 1000 // Interval pembaruan countdown dalam milidetik

        val countDownTimer = object : CountDownTimer(initialCountdown, countdownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                val tvResendOTP = binding.resendOTP
                val resendOTP = getString(R.string.resend_otp)
                val updatedResendOTP = resendOTP.replace("%resend_otp", secondsLeft.toString())
                tvResendOTP.text = updatedResendOTP
            }

            override fun onFinish() {
                val tvResendOTP = binding.resendOTP
                tvResendOTP.text = getString(R.string.tv_resend_otp)
            }
        }

        countDownTimer.start()
    }

    private fun resendOTP() {
        binding.apply {
            resendOTP.setOnClickListener {

            }
        }
    }

    private fun verifyOTP() {
        val verificationId = intent.getStringExtra("verificationId")
        binding.apply {
            btnVerifyOTP.setOnClickListener {
                val code1 = code1.text.toString().trim()
                val code2 = code2.text.toString().trim()
                val code3 = code3.text.toString().trim()
                val code4 = code4.text.toString().trim()
                val code5 = code5.text.toString().trim()
                val code6 = code6.text.toString().trim()

                if (verificationId != null) {
                    val code = "$code1$code2$code3$code4$code5$code6"
                    val credential = PhoneAuthProvider.getCredential(verificationId, code)

                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(
                                    this@VerifyOtpActivity,
                                    ForgotPasswordActivity::class.java
                                )
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                val message = task.exception!!.toString()
                                Toast.makeText(
                                    this@VerifyOtpActivity,
                                    "Error: $message",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }
    }

    private fun editTextListener() {
        setBtnEnable()
        binding.apply {
            code1.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            code1.addTextChangedListener {
                setBtnEnable()
                if (it?.length == 1) {
                    code2.requestFocus()
                }
            }
            code2.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            code2.addTextChangedListener {
                setBtnEnable()
                if (it?.length == 1) {
                    code3.requestFocus()
                }
            }
            code3.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            code3.addTextChangedListener {
                setBtnEnable()
                if (it?.length == 1) {
                    code4.requestFocus()
                }
            }
            code4.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            code4.addTextChangedListener {
                setBtnEnable()
                if (it?.length == 1) {
                    code5.requestFocus()
                }
            }
            code5.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            code5.addTextChangedListener {
                setBtnEnable()
                if (it?.length == 1) {
                    code6.requestFocus()
                }
            }
            code6.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
            code6.addTextChangedListener {
                setBtnEnable()
            }
        }
    }

    private fun setBtnEnable() {
        binding.apply {
            val code1 = code1.text.toString().trim()
            val code2 = code2.text.toString().trim()
            val code3 = code3.text.toString().trim()
            val code4 = code4.text.toString().trim()
            val code5 = code5.text.toString().trim()
            val code6 = code6.text.toString().trim()

            btnVerifyOTP.isEnabled =
                code1.isNotEmpty()
                        && code2.isNotEmpty()
                        && code3.isNotEmpty()
                        && code4.isNotEmpty()
                        && code5.isNotEmpty()
                        && code6.isNotEmpty()
        }
    }
}