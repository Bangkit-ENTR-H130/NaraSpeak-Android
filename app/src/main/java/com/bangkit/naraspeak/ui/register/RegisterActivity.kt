package com.bangkit.naraspeak.ui.register

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.ui.videocall.VideoCallActivity
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.databinding.ActivityRegisterBinding
import com.bangkit.naraspeak.ui.datafill.DataFillActivity
import com.bangkit.naraspeak.ui.verification.OtpActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignIn: GoogleSignInClient

    //move
    private lateinit var repository: VideoCallRepository
    private val firebaseClient: FirebaseClient = FirebaseClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = VideoCallRepository.getInstance(firebaseClient)

//        testDatabase()

        initializeAuth()

        val email = binding.edEmailRegister.text.toString()
        val password = binding.edPasswordRegister.text.toString()
        val confirmPassword = binding.edConfirmPassword.text.toString()

        binding.btnRegisterGoogle.setOnClickListener {
            googleSignUpLauncher.launch(googleSignIn.signInIntent)
            Log.d("user", "${auth.currentUser?.displayName}")

        }

        binding.btnRegister.setOnClickListener {
            signUpManually(binding.edEmailRegister.text.toString(), binding.edPasswordRegister.text.toString())
            Log.d(TAG, "onCreate: $email $password")
        }

        binding.btnLoginHere.setOnClickListener {
            Log.d(TAG, "onCreate: $email $password")

        }



    }

    private fun signUpManually(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Verification sent", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, OtpActivity::class.java)
                            startActivity(intent)
                        }
                    }?.addOnFailureListener {
                        Toast.makeText(this@RegisterActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "signUpManually: ${it.message}")
                    }
                }
            }
    }

    private fun initializeAuth() {
        val option = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignIn = GoogleSignIn.getClient(this, option)

        auth = Firebase.auth

    }

    private val googleSignUpLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")
                checkRegisteredEmail(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed: ${e.message}")
            }
        }
    }

    private fun checkRegisteredEmail(idToken: String) {
//        val email = GoogleSignIn.getLastSignedInAccount(this@LoginActivity)?.email
//        if (email != null) {
//        auth.addAuthStateListener {
        val email = auth.currentUser?.email


        auth.fetchSignInMethodsForEmail(email.toString()).addOnCompleteListener {
            if (email != null) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Email has already been registered",
                    Toast.LENGTH_SHORT
                ).show()


            } else {
                signUpWithGoogle(idToken)


//            }
//            }
//            (idToken).addOnCompleteListener {
//                if (it.isSuccessful) {
//                    val signInMethods = it.result.signInMethods
//                    if (!signInMethods.isNullOrEmpty()) {
//                        signInWithGoogle(idToken)
//                        Log.d(TAG, "Login success")
//                    } else {
//                        Toast.makeText(this, "Email is not registered.", Toast.LENGTH_SHORT).show()
//                        Log.d(TAG, "Login failed")
//
//                    }
//                }
//            }
            }
        }
    }

    private fun signUpWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val intent = Intent(this@RegisterActivity, DataFillActivity::class.java)
                    intent.putExtra(SIGN_UP_GOOGLE, "with_google_sign_up")
                    startActivity(intent)
                    finish()

                } else {
                    Log.w(TAG, "signInWithCredential: ${it.exception}")

                }
            }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        when {
            it[android.Manifest.permission.CAMERA] ?: false -> {
                getPermission()
            }

            it[android.Manifest.permission.RECORD_AUDIO] ?: false -> {
                getPermission()
            }

            else -> {
            }
        }
    }

    private fun getPermission() {
        if (ActivityCompat.checkSelfPermission(
                this@RegisterActivity,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(
                this@RegisterActivity,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            val intent = Intent(this@RegisterActivity, VideoCallActivity::class.java)
            startActivity(intent)

        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    private fun testDatabase() {
        binding.btnRegister.setOnClickListener {
            repository.login(
                binding.edEmailRegister.text.toString(),
                object : FirebaseClient.FirebaseStatusListener {
                    override fun onError() {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    override fun onSuccess() {
                        Log.d("USERSUCCESS", binding.edEmailRegister.text.toString())
                        getPermission()
                    }

                },
                this@RegisterActivity
            )
            Log.d("username", binding.edEmailRegister.text.toString())


        }
    }



    companion object {
        private const val TAG = "RegisterActivity"
        const val SIGN_UP_GOOGLE = "sign_up_google"
    }
}