package com.bangkit.naraspeak.ui.landingpage

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.ui.homepage.HomepageActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContentView(R.layout.activity_splash)

        enableEdgeToEdge()
        auth = Firebase.auth
        val currentUser = auth.currentUser

        //tambahkan kalau sudah tersimpan data login langsung ke activity homepage
        //tambahkan animasi
        Handler().postDelayed({
            if (currentUser != null && currentUser.isEmailVerified) {
                val intent = Intent(this, HomepageActivity::class.java)
                startActivity(intent)
                finish()
            } else  {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 2000L)

    }
}