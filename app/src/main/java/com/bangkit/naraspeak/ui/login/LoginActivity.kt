package com.bangkit.naraspeak.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.ui.datafill.DataFillActivity
import com.bangkit.naraspeak.ui.verification.OtpActivity
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.ui.register.RegisterActivity
import com.bangkit.naraspeak.databinding.ActivityLoginBinding
import com.bangkit.naraspeak.helper.Result
import com.bangkit.naraspeak.helper.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModelFactory = ViewModelFactory.getInstance()
    private val viewModel by viewModels<LoginViewModel> { viewModelFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        binding.apply {
            btnLogin.setOnClickListener {

            viewModel.login(
                edEmailLogin.text.toString(),
                edPasswordLogin.text.toString()
            ).observe(this@LoginActivity) {result ->
                if (result != null) {
                    when (result) {
                        is Result.Failed -> {
                            Toast.makeText(this@LoginActivity, result.error, Toast.LENGTH_SHORT).show()
                        }
                        Result.Loading -> {}
                        is Result.Success -> {
                            Toast.makeText(this@LoginActivity, result.data.message, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, DataFillActivity::class.java)
                            startActivity(intent)
                        }
                        }
                    }
                }

            }

            btnConfirm.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }




            btnLoginGoogle.setOnClickListener {
                val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                startActivity(intent)
            }
        }


    }
}