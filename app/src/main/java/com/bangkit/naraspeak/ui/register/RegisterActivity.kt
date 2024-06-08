package com.bangkit.naraspeak.ui.register

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
import com.bangkit.naraspeak.data.webrtc.FirebaseClient
import com.bangkit.naraspeak.databinding.ActivityRegisterBinding
import com.bangkit.naraspeak.ui.datafill.DataFillActivity
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

        testDatabase()

        initializeAuth()

        val email = binding.edEmailRegister.text.toString()
        val password = binding.edPasswordRegister.text.toString()
        val confirmPassword = binding.edConfirmPassword.text.toString()

        binding.btnRegisterGoogle.setOnClickListener {
            googleSignUpLauncher.launch(googleSignIn.signInIntent)
            Log.d("user", "${auth.currentUser?.displayName}")

        }



    }

    private fun initializeAuth() {

        val option = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(R.string.default_web_client_id.toString())
            .requestEmail()
            .build()
        googleSignIn = GoogleSignIn.getClient(this, option)

        auth = Firebase.auth



    }

    private val googleSignUpLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle: ${account.id}")
                signUpWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed: ${e.message}")
            }
        }
    }

    private fun signUpWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    if (auth.currentUser != null) {
                        val intent = Intent(this@RegisterActivity, DataFillActivity::class.java)
                        intent.putExtra(SIGN_UP_GOOGLE, "with_google_sign_up")
                        startActivity(intent)
                        finish()
                    }

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
        private val TAG = RegisterActivity::class.java.simpleName
        const val SIGN_UP_GOOGLE = "sign_up_google"
    }
}