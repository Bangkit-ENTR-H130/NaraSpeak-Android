package com.bangkit.naraspeak.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.ui.register.RegisterActivity
import com.bangkit.naraspeak.databinding.ActivityLoginBinding
import com.bangkit.naraspeak.helper.AccountViewModelFactory
import com.bangkit.naraspeak.helper.isValidEmail
import com.bangkit.naraspeak.ui.homepage.HomepageActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val accountViewModelFactory = AccountViewModelFactory.getInstance()
    private val viewModel by viewModels<LoginViewModel> { accountViewModelFactory }

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

        binding.apply {
            btnLogin.isEnabled = false

            edEmailLogin.addTextChangedListener(textWatcher)
            edPasswordLogin.addTextChangedListener(textWatcher)

            btnLogin.setOnClickListener {
                auth.signInWithEmailAndPassword(
                    edEmailLogin.text.toString(),
                    edPasswordLogin.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this@LoginActivity, HomepageActivity::class.java)
                        startActivity(intent)
                    }
                }.addOnFailureListener {
                    Toast.makeText(this@LoginActivity, "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }

            btnLoginGoogle.setOnClickListener {
                googleSignInLauncher.launch(googleSignIn.signInIntent)
            }

            btnConfirm.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val email = binding.edEmailLogin.text.toString().trim()
            val password = binding.edPasswordLogin.text.toString().trim()
            binding.btnLogin.isEnabled = isValidEmail(email) && password.length >= 8
        }

        override fun afterTextChanged(s: Editable?) {}
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

    private fun signInWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
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
            Log.d(TAG, "signIngWithCredential: ${it.message}")
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
