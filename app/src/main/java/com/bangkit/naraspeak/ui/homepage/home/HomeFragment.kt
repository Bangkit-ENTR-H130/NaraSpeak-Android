package com.bangkit.naraspeak.ui.homepage.home

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.databinding.FragmentHomeBinding
import com.bangkit.naraspeak.ui.homepage.HomepageActivity
import com.bangkit.naraspeak.ui.result.CompleteSessionActivity
import com.bangkit.naraspeak.ui.videocall.VideoCallActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private lateinit var repository: VideoCallRepository
    private val firebaseClient = FirebaseClient()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth

        val user = auth.currentUser

        repository = VideoCallRepository.getInstance(firebaseClient)

        binding.toolbar.apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.btn_shop -> {
                        findNavController().navigate(R.id.action_homeFragment_to_shopActivity)
                    }
                }
                true
            }
        }

        binding.cardFeature2.apply {
            Glide.with(requireActivity())
                .load(R.drawable.homepage_menu_3)
                .into(featureIcon)

            featureIcon.maxHeight = 130
            featureTitle.text = "Group call"
            featureDesc.text = "Talk with someone you know in group"

        }

        binding.tvDisplayName.text = user?.displayName
        if (user?.photoUrl != null) {
            Glide.with(requireContext())
                .load(user.photoUrl)
                .into(binding.ivProfilePicture)
        }

        binding.cardMain.setOnClickListener {
//            startVideoCall()
            val intent = Intent(requireActivity(), CompleteSessionActivity::class.java)
            requireActivity().startActivity(intent)
        }

        binding.cardFeature1.root.setOnClickListener {
            startVideoCall()
        }

    }

    private fun startVideoCall() {
        repository.login(
            auth.currentUser?.uid.toString(),
            object : FirebaseClient.FirebaseStatusListener {
                override fun onError() {
                    Toast.makeText(requireActivity(), "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                    Log.e(TAG, "Login failed")
                }

                override fun onSuccess() {
                    Log.d(TAG, "Login successful")
                    repository.sendCallRequest(object : FirebaseClient.FirebaseStatusListener {
                        override fun onError() {
                            Toast.makeText(
                                requireActivity(),
                                "Couldn't make the call",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(TAG, "Call request failed")
                        }

                        override fun onSuccess() {
                            Log.d(TAG, "Call request successful")
                            getPermission()
                        }
                    })
                }
            },
            requireActivity()
        )

    }

    private fun getPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            findNavController().navigate(R.id.action_homeFragment_to_videoCallActivity)
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
                Toast.makeText(requireActivity(), "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        private const val TAG = "HomeFragment"
    }


}