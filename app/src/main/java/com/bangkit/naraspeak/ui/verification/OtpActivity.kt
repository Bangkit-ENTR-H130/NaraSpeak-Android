package com.bangkit.naraspeak.ui.verification

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.databinding.ActivityOtpBinding
import com.bangkit.naraspeak.ui.datafill.DataFillActivity
import com.bangkit.naraspeak.ui.homepage.HomepageActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: OtpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        val user = auth.currentUser

        checkVerification(user)

        binding.btnResend.setOnClickListener {
            checkVerification(user)
        }


        viewModel.remainingTime.observe(this) {
//            binding.btnResend.isEnabled = false
            binding.tvTimer.text = it.toString()



            if (it == 0L) {
//                binding.btnResend.isEnabled = true

            }
        }
    }

    private fun checkVerification(user: FirebaseUser?) {
        if (user != null) {
            if (!user.isEmailVerified) {
                Log.d("VerificationACtivity", "${user.isEmailVerified}")
                Toast.makeText(this, "Email not verified yet.", Toast.LENGTH_SHORT).show()
                user.reload()
            }
            if (user.isEmailVerified) {
                Log.d("VerificationACtivity", "${user.isEmailVerified}")
                Toast.makeText(this, "${user.isEmailVerified}", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DataFillActivity::class.java))
                finish()
            }
        }
    }

    companion object {
        private const val TAG = "OTP"
    }
}