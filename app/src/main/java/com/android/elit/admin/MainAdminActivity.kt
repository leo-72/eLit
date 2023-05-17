package com.android.elit.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.elit.R
import com.android.elit.admin.Fragment.AddBooksFragment
import com.android.elit.admin.Fragment.BooksAdminFragment
import com.android.elit.databinding.ActivityMainAdminBinding
import com.android.elit.fragment.AccountFragment

class MainAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BooksAdminFragment(), BooksAdminFragment::class.java.simpleName)
            .commit()
        bottomNav()
    }

    private fun bottomNav() {
        binding.apply {
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_admin_books -> {
                        val fragment = BooksAdminFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragment_container,
                                fragment,
                                fragment.javaClass.simpleName
                            )
                            .commit()
                        true
                    }
                    R.id.navigation_add_books -> {
                        val fragment = AddBooksFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragment_container,
                                fragment,
                                fragment.javaClass.simpleName
                            )
                            .commit()
                        true
                    }
                    R.id.navigation_admin_account -> {
                        val fragment = AccountFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragment_container,
                                fragment,
                                fragment.javaClass.simpleName
                            )
                            .commit()
                        true
                    }
                    else -> false
                }
            }
        }
    }
}