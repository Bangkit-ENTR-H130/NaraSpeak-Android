package com.bangkit.naraspeak.ui.datafill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.databinding.ActivityDataFillBinding
import com.bangkit.naraspeak.ui.homepage.HomepageActivity
import com.bangkit.naraspeak.ui.register.RegisterActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth

class DataFillActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataFillBinding
    private lateinit var auth: FirebaseAuth
    private val withGoogleSignUp = intent.getStringExtra(RegisterActivity.SIGN_UP_GOOGLE)
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

        auth = Firebase.auth

        binding.btnSubmit.setOnClickListener {
            val user = auth.currentUser


            if (user!= null) {
                if (binding.edName.text.toString().isNotEmpty()) {
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(binding.edName.toString())
                        .build()

                    user.updateProfile(profileUpdate)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d(TAG, "Name updated")
                            } else {
                                Log.d(TAG, "Name not updated: ${it.exception}")
                            }
                        }
                }

                if (withGoogleSignUp != null) {
                    binding.layoutPassword.visibility = View.VISIBLE
                    binding.tvPassword.visibility = View.VISIBLE

                    user.updatePassword(binding.edPassword.text.toString())
                }

                val intent = Intent(this@DataFillActivity, HomepageActivity::class.java)
                startActivity(intent)

            }

        }

        setupSpinner()
    }

    private fun setupSpinner() {
        val levels = arrayOf("Beginner", "Intermediate", "Fluent", "Native")
        val adapter = ArrayAdapter(this@DataFillActivity, R.layout.spinner_item, levels)
        binding.spinnerLevel.adapter = adapter
    }

    companion object {
        private val TAG = DataFillActivity::class.java.simpleName
    }
}