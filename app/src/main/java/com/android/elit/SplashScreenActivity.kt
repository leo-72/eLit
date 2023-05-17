package com.android.elit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.android.elit.admin.MainAdminActivity
import com.android.elit.databinding.ActivitySplashScreenBinding
import com.android.elit.ui.activity.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fs: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        fs =  FirebaseFirestore.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            val user = auth.currentUser
            if (user != null) {
                updateUI(user)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        },2000)
    }

    private fun updateUI(user: FirebaseUser?) {
        val roleUser = fs.collection("users").whereEqualTo("role", "user")
        val roleAdmin = fs.collection("users").whereEqualTo("role", "admin")

        roleUser.get().addOnSuccessListener { documents ->
            for (document in documents) {
                if (document.data["email"] == user?.email) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
        roleAdmin.get().addOnSuccessListener { documents ->
            for (document in documents) {
                if (document.data["email"] == user?.email) {
                    startActivity(Intent(this, MainAdminActivity::class.java))
                    finish()
                }
            }
        }
    }
}