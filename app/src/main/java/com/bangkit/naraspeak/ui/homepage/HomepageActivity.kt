package com.bangkit.naraspeak.ui.homepage

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.databinding.ActivityHomepageBinding
import com.bangkit.naraspeak.ui.groupcall.GroupCallActivity
import com.bangkit.naraspeak.ui.videocall.VideoCallActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class HomepageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomepageBinding


    private lateinit var repository: VideoCallRepository
    private val firebaseClient: FirebaseClient = FirebaseClient()
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        auth = Firebase.auth

        repository = VideoCallRepository.getInstance(firebaseClient)




        setupBottomNavigation()

        binding.fab.setOnClickListener {
            fabAction()
        }



    }

    private fun fabAction() {
        when (binding.cardOverlayStart.root.visibility) {
            View.GONE -> {
                binding.cardOverlayStart.root.visibility = View.VISIBLE
                binding.polygon.visibility = View.VISIBLE


                    startVideoCall()
//                testDatabase(username)


                binding.cardOverlayStart.tvStartGroup.setOnClickListener {
                    val intent = Intent(this, GroupCallActivity::class.java)
                    startActivity(intent)
                }
            }
            else -> {
                binding.cardOverlayStart.root.visibility = View.GONE
                binding.polygon.visibility = View.GONE
            }
        }
    }

    private fun setupBottomNavigation() {
        val navController = findNavController(R.id.nav_host_fragment_activity_homepage)
        val appBarConfig = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.settingFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun startVideoCall() {
        binding.cardOverlayStart.tvStartNormal.setOnClickListener {
//            if (user == null || user.displayName.isNullOrEmpty()) {
//                Log.e("startVideoCall", "User or displayName is null")
//
//            }


            repository.login(
                auth.currentUser?.displayName.toString(),
                object : FirebaseClient.FirebaseStatusListener {
                    override fun onError() {
                        Toast.makeText(
                            this@HomepageActivity,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onSuccess() {
                        Log.d("USERSUCCESS", auth.currentUser?.displayName.toString())
                        getPermission()
                    }
                },
                this@HomepageActivity
            )

        }
        Log.d("username", auth.currentUser?.displayName.toString())


    }

    private fun getPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            val intent = Intent(this, VideoCallActivity::class.java)
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





//    private fun testDatabase(user: FirebaseUser?) {
//        binding.cardOverlayStart.tvStartNormal.setOnClickListener {
//            val loginFun = repository.login(
//                user?.displayName.toString(),
//                object : FirebaseClient.FirebaseStatusListener {
//                    override fun onError() {
//                        Toast.makeText(
//                            this@HomepageActivity,
//                            "Something went wrong",
//                            Toast.LENGTH_SHORT
//                        ).show()
//
//                    }
//
//                    override fun onSuccess() {
//                        Log.d("USERSUCCESS", user?.displayName.toString())
//                        getPermission()
//                    }
//
//                },
//                applicationContext
//            )
//
//            Log.d("loginFun", loginFun.toString())
//
//            Log.d("username", user?.displayName.toString())
//
//
//        }
    }

