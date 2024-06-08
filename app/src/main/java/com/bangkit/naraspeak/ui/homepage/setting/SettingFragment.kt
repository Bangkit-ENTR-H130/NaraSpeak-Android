package com.bangkit.naraspeak.ui.homepage.setting

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.databinding.FragmentSettingBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get()= _binding!!

    private lateinit var auth: FirebaseAuth


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


        binding.tvName.text = user?.displayName
        Glide.with(requireContext())
            .load(user?.photoUrl)
            .apply(RequestOptions())
            .into(binding.imgPhotoProfile)

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
}