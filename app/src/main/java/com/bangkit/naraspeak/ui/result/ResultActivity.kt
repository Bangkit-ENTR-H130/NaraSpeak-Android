package com.bangkit.naraspeak.ui.result

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.model.CorrectionModel
import com.bangkit.naraspeak.databinding.ActivityResultBinding
import com.bangkit.naraspeak.ui.homepage.HomepageActivity

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private val results = mutableListOf<CorrectionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val original = intent.getStringExtra(CompleteSessionActivity.EXTRA_ORIGINAL)
        val grammarChecker = intent.getStringExtra(CompleteSessionActivity.EXTRA_CORRECTION)

        val originalSentences = original?.split(".")?.map { it.trim() }?.filter { it.isNotEmpty() }
        val correctedSentences = grammarChecker?.split(".")?.map { it.trim() }?.filter { it.isNotEmpty() }

//        binding.cardGrammar.wrong.text = original
//        binding.cardGrammar.correct.text = grammarChecker

        val maxLength = maxOf(originalSentences?.size ?: 0, correctedSentences?.size ?: 0)
        for (i in 0 until maxLength) {
            val originalSentence = if (i < originalSentences?.size!!) originalSentences[i] else ""
            val correctedSentence = if (i < correctedSentences?.size!!) correctedSentences[i] else ""
            results.add(CorrectionModel(originalSentence, correctedSentence))
        }

        binding.cardGrammar.adapter = ResultAdapter(results)
        binding.cardGrammar.layoutManager = LinearLayoutManager(this)



        binding.btnHome.setOnClickListener {
            val intent = Intent(this@ResultActivity, HomepageActivity::class.java)
            startActivity(intent)
        }

        binding.btnNew.setOnClickListener {
            val intent = Intent(this@ResultActivity, HomepageActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CorrectionModel(null, null)

    }
}