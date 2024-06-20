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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
                binding.cardOverlayStart.tvStartGroup.setOnClickListener {
                    startGroupCall()
                }
            }
            else -> {
                binding.cardOverlayStart.root.visibility = View.GONE
                binding.polygon.visibility = View.GONE
            }
        }
    }

    private fun startGroupCall() {
//        when (binding.cardGroup.root.visibility) {
//            View.GONE -> {
//                binding.cardGroup.root.visibility = View.VISIBLE
//                binding.cardGroup.btnGroupConfirm.setOnClickListener {
//                    handleGroupConfirm()
//                }
//            }
//            View.VISIBLE -> {
//                binding.cardGroup.root.visibility = View.GONE
//            }
//        }
        Toast.makeText(this@HomepageActivity, "Coming Soon", Toast.LENGTH_SHORT).show()
    }

    private fun handleGroupConfirm() {
        val array = arrayListOf(
            binding.cardGroup.tv1.text.toString(),
            binding.cardGroup.tv2.text.toString(),
            binding.cardGroup.tv3.text.toString()
        )

        repository.loginGroup(auth.currentUser?.displayName.toString(), object : FirebaseClient.FirebaseStatusListener {
            override fun onError() {
                Toast.makeText(this@HomepageActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Login failed")
            }

            override fun onSuccess() {
                Log.d(TAG, "Login successful")
                repository.sendGroupRequest(array, object : FirebaseClient.FirebaseStatusListener {
                    override fun onError() {
                        Toast.makeText(this@HomepageActivity, "Couldn't make the call", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Group request failed")
                    }

                    override fun onSuccess() {
                        Log.d(TAG, "Group request successful")
                        val intent = Intent(this@HomepageActivity, GroupCallActivity::class.java)
                        startActivity(intent)
                    }
                })
            }
        }, this@HomepageActivity)
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
            repository.login(auth.currentUser?.uid.toString(), object : FirebaseClient.FirebaseStatusListener {
                override fun onError() {
                    Toast.makeText(this@HomepageActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Login failed")
                }

                override fun onSuccess() {
                    Log.d(TAG, "Login successful")
                    repository.sendCallRequest(object : FirebaseClient.FirebaseStatusListener {
                        override fun onError() {
                            Toast.makeText(this@HomepageActivity, "Couldn't make the call", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Call request failed")
                        }

                        override fun onSuccess() {
                            Log.d(TAG, "Call request successful")
                            getPermission()
                        }
                    })
                }
            }, this@HomepageActivity)
        }
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
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "HomepageActivity"
    }
}
