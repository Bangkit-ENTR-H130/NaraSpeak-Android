package com.bangkit.naraspeak.ui.datafill

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.model.UserModel
import com.bangkit.naraspeak.databinding.ActivityDataFillBinding
import com.bangkit.naraspeak.helper.CommonViewModelFactory
import com.bangkit.naraspeak.ui.homepage.HomepageActivity
import com.bangkit.naraspeak.ui.register.RegisterActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth

class DataFillActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDataFillBinding
    private lateinit var auth: FirebaseAuth
    private val commonViewModelFactory = CommonViewModelFactory.getInstance()
    private val viewModel by viewModels<DataFillViewModel> { commonViewModelFactory }
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
        val user = auth.currentUser

        val withGoogleSignUp = intent.getStringExtra(RegisterActivity.SIGN_UP_GOOGLE)
        Log.d("GoogleSignUp", withGoogleSignUp.toString())

        if (withGoogleSignUp != null) {
            binding.layoutPassword.visibility = View.VISIBLE
            binding.tvPassword.visibility = View.VISIBLE

        }



        binding.btnSubmit.setOnClickListener {

            if (user != null) {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(binding.edName.text.toString())
                    .build()

                user.updateProfile(profileUpdate)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "Name updated")
                        } else {
                            Log.d(TAG, "Name not updated: ${it.exception}")
                        }

                    }
//                viewModel.updateName(binding.edName.text.toString())

                Log.d(
                    TAG, binding.spinnerLevel.selectedItem.toString()
                )

                val password = binding.edPassword.text.toString()
                if (password.isNotEmpty()) {
                    val updatePassword = user.updatePassword(binding.edPassword.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.d(TAG, "password updated")
                            } else {
                                Log.d(TAG, "password not updated ${it.exception}")

                            }
                        }
                    Log.d(TAG, "password updated ${updatePassword.isSuccessful}")
                }
                val gender = when ( binding.rgGender.checkedRadioButtonId) {
                    binding.rbBtnMale.id -> {
                        "Male"
                    }
                    binding.rbBtnFemale.id -> {
                        "Female"
                    }

                    else -> {
                        "Not set yet"
                    }
                }
                binding
                val userModel = UserModel(
                    user.uid,
                    binding.edName.text.toString(),
                    binding.spinnerLevel.selectedItem.toString(),
                    gender,
                )


                viewModel.postAuthToDatabase(binding.edName.text.toString(), userModel)

                user.reload()


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