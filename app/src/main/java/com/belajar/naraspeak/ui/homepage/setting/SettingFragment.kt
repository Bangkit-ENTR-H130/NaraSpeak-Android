package com.belajar.naraspeak.ui.homepage.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.belajar.naraspeak.R
import com.belajar.naraspeak.databinding.FragmentSettingBinding
import com.bumptech.glide.Glide


class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get()= _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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