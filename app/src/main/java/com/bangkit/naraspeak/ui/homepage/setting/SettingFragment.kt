package com.bangkit.naraspeak.ui.homepage.setting

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.model.UserModel
import com.bangkit.naraspeak.databinding.FragmentSettingBinding
import com.bangkit.naraspeak.helper.ViewModelFactory
import com.bangkit.naraspeak.ui.login.LoginActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private val viewModelFactory = ViewModelFactory.getInstance()
    private val viewModel by viewModels<SettingViewModel> { viewModelFactory }
    val db = FirebaseDatabase.getInstance()




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        val user = auth.currentUser

        if (user != null) {


            binding.tvName.text = user.displayName
            if (user.photoUrl != null) {
                Glide.with(requireContext())
                    .load(user.photoUrl)
                    .apply(RequestOptions())
                    .into(binding.imgPhotoProfile)
            }


            binding.cardAbout.apply {
                tvCardSetting.text = "About"
                Glide.with(this@SettingFragment)
                    .load(R.drawable.baseline_info_24)
                    .into(imgCardSetting)
            }

            binding.cardChangeLanguage.apply {
                tvCardSetting.text = "Change the language"
                Glide.with(this@SettingFragment)
                    .load(R.drawable.baseline_language_24)
                    .into(imgCardSetting)
            }

            binding.cardHelp.apply {
                tvCardSetting.text = "Help"
                Glide.with(this@SettingFragment)
                    .load(R.drawable.baseline_live_help_24)
                    .into(imgCardSetting)
            }
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().finish()
        }

        binding.cardChangeProfile.root.setOnClickListener {
            binding.cardSetProfile.root.visibility = View.VISIBLE
            binding.cardSetProfile.btnConfirm.setOnClickListener {
                viewModel.updateData(auth.currentUser?.displayName.toString(),
                    object : FirebaseClient.UpdateDataListener {
                        override fun onUpdate(userModel: UserModel) {
                            
                            userModel.name = binding.cardSetProfile.edName.text.toString()
                            userModel.gender = "Female"
                            userModel.level = binding.cardSetProfile.level.text.toString()

                        }

                    })
                binding.cardSetProfile.root.visibility = View.GONE

            }

        }


    }
}