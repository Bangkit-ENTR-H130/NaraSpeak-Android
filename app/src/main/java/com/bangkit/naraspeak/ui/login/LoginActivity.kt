package com.bangkit.naraspeak.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.bangkit.naraspeak.ui.homepage.HomepageActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlin.math.sign

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModelFactory = ViewModelFactory.getInstance()
    private val viewModel by viewModels<LoginViewModel> { viewModelFactory }

    private lateinit var googleSignIn: GoogleSignInClient

    private lateinit var auth: FirebaseAuth
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

        val option = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        googleSignIn = GoogleSignIn.getClient(this, option)

        auth = Firebase.auth

//        viewModel.getAuth()


        binding.apply {
            btnLogin.setOnClickListener {
                auth.signInWithEmailAndPassword(
                    edEmailLogin.text.toString(),
                    edPasswordLogin.text.toString()
                )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this@LoginActivity, HomepageActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                it.exception.toString(),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

//            viewModel.login(
//                edEmailLogin.text.toString(),
//                edPasswordLogin.text.toString()
//            ).observe(this@LoginActivity) {result ->
//                if (result != null) {
//                    when (result) {
//                        is Result.Failed -> {
//                            Toast.makeText(this@LoginActivity, result.error, Toast.LENGTH_SHORT).show()
//                        }
//                        Result.Loading -> {}
//                        is Result.Success -> {
//                            Toast.makeText(this@LoginActivity, result.data.message, Toast.LENGTH_SHORT).show()
//
//                            val intent = Intent(this@LoginActivity, DataFillActivity::class.java)
//                            startActivity(intent)
//                        }
//                        }
//                    }
//                }

            }

            btnLoginGoogle.setOnClickListener {
                googleSignInLauncher.launch(googleSignIn.signInIntent)
            }

            btnConfirm.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }


//            btnLoginGoogle.setOnClickListener {
//                val intent = Intent(this@LoginActivity, OtpActivity::class.java)
//                startActivity(intent)
//            }
        }
    }

    private fun googleSignIn() {

    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")

                signInWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed: ${e.message}")
            }
        }
    }

//    private fun checkRegisteredEmail(idToken: String, email: String) {
////        val email = GoogleSignIn.getLastSignedInAccount(this@LoginActivity)?.email
////        if (email != null) {
////        val email = auth.currentUser?.email
//        Log.d(TAG, "checkregisteredEmail $email")
//        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener {
//            if (it.isSuccessful) {
//                val signingMethods = it.result.signInMethods
//                Log.d(TAG, signingMethods.toString())
//
//                    Toast.makeText(
//                        this@LoginActivity,
//                        "Email has not been registered",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                signInWithGoogle(idToken)
//
//            } else {
//                signInWithGoogle(idToken)
//                Log.d(TAG, "login success ${it.isSuccessful}")
//            }
//
//        }.addOnFailureListener {
//            Log.d(TAG, "failure check email : ${it.message}")
//        }
//
//
////            }
////            (idToken).addOnCompleteListener {
////                if (it.isSuccessful) {
////                    val signInMethods = it.result.signInMethods
////                    if (!signInMethods.isNullOrEmpty()) {
////                        signInWithGoogle(idToken)
////                        Log.d(TAG, "Login success")
////                    } else {
////                        Toast.makeText(this, "Email is not registered.", Toast.LENGTH_SHORT).show()
////                        Log.d(TAG, "Login failed")
////
////                    }
////                }
////            }
////        }
//    }

    private fun signInWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {

                if (it.isSuccessful) {
                    Log.d(TAG, "is newuser: ${it.result.additionalUserInfo?.isNewUser}")
                    if (!it.result.additionalUserInfo?.isNewUser!!) {
                        Log.d(TAG, "signInWithCredential::success")
                        startActivity(Intent(this@LoginActivity, HomepageActivity::class.java))
                        Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Email has not registered yet", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.d(TAG, "signInWithCredential: ${it.exception}")


                }
            }.addOnFailureListener {
                //buat penanda gagal
                Log.d(TAG, "signIngWithCredential: ${it.message}")
            }
    }


    companion object {
        private const val TAG = "LoginActivity"

    }
}