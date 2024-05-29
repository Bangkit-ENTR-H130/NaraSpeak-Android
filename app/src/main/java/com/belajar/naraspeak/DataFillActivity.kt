package com.belajar.naraspeak

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.belajar.naraspeak.databinding.ActivityDataFillBinding

class DataFillActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataFillBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDataFillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSpinner()
    }

    private fun setupSpinner() {
        val levels = arrayOf("Beginner", "Intermediate", "Fluent", "Native")
        val adapter = ArrayAdapter(this@DataFillActivity, R.layout.spinner_item, levels)
        binding.spinnerLevel.adapter = adapter
    }
}