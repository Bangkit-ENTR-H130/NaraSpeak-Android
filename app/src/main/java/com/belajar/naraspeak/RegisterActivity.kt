package com.belajar.naraspeak

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.belajar.naraspeak.data.repository.VideoCallRepository
import com.belajar.naraspeak.data.webrtc.FirebaseClient
import com.belajar.naraspeak.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
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


    }
    private fun testDatabase() {
        binding.btnRegister.setOnClickListener {
            repository.login(binding.edEmailRegister.text.toString(), object : FirebaseClient.FirebaseStatusListener{
                override fun onError() {

                }

                override fun onSuccess() {
                    Log.d("USERSUCCESS", binding.edEmailRegister.text.toString())


                }

            })
            Log.d("username", binding.edEmailRegister.text.toString())


        }
    }
}