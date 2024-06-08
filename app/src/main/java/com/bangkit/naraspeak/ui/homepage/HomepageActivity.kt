package com.bangkit.naraspeak.ui.homepage

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.databinding.ActivityHomepageBinding

class HomepageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomepageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()


        setupBottomNavigation()

        binding.fab.setOnClickListener {
            when (binding.cardOverlayStart.root.visibility) {
                View.GONE -> {
                    binding.cardOverlayStart.root.visibility = View.VISIBLE
                    binding.polygon.visibility = View.VISIBLE
                }
                else -> {
                    binding.cardOverlayStart.root.visibility = View.GONE
                    binding.polygon.visibility = View.GONE
                }
            }
        }

    }

    private fun isCardDisplayed() {
        when (binding.cardOverlayStart.root.visibility) {
            View.GONE -> {
                binding.cardOverlayStart.root.visibility = View.VISIBLE
            }
            else -> {
                binding.cardOverlayStart.root.visibility = View.GONE
            }
        }
    }

    private fun setupBottomNavigation() {
        val navController = findNavController(R.id.nav_host_fragment_activity_homepage)
        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.settingFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNavigation.setupWithNavController(navController)
    }
}