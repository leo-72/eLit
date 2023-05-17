package com.android.elit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.elit.databinding.ActivityMainBinding
import com.android.elit.fragment.AccountFragment
import com.android.elit.fragment.BooksFragment
import com.android.elit.fragment.FavoriteFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BooksFragment(), BooksFragment::class.java.simpleName)
            .commit()
        bottomNav()
    }

    private fun bottomNav() {
        binding.apply {
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_books -> {
                        val fragment = BooksFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragment_container,
                                fragment,
                                fragment.javaClass.simpleName
                            )
                            .commit()
                        true
                    }
                    R.id.navigation_favourite -> {
                        val fragment = FavoriteFragment()
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.fragment_container,
                                fragment,
                                fragment.javaClass.simpleName
                            )
                            .commit()
                        true
                    }
                    R.id.navigation_account -> {
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