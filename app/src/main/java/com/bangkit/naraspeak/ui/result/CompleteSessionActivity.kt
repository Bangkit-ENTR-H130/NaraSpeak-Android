package com.bangkit.naraspeak.ui.result

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.local.HistoryEntity
import com.bangkit.naraspeak.databinding.ActivityCompleteSessionBinding
import com.bangkit.naraspeak.helper.ViewModelFactory
import com.bangkit.naraspeak.ui.videocall.VideoCallViewModel
import java.io.File
import java.io.IOException

class CompleteSessionActivity : AppCompatActivity() {
    private var mMediaPlayer: MediaPlayer? = null
    private lateinit var binding: ActivityCompleteSessionBinding
    private lateinit var currentAudioPath: String
    private var isReady: Boolean = false

    private val viewModel: VideoCallViewModel by viewModels {
        ViewModelFactory.getHistoryInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompleteSessionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Example usage to get audio path (replace with your logic)
        currentAudioPath = "/storage/emulated/0/Android/data/com.bangkit.naraspeak/cache/2024Jun20278161433841704290.mp3"

        binding.cardRecord.placeholder.setOnClickListener {
            initMediaPlayer(currentAudioPath)
        }

        binding.cardRecord.btnYes.setOnClickListener {
            releaseMediaPlayer()
        }

        viewModel.getHistory().observe(this) {
            binding.tvFinishDesc.text = it.audio
        }
    }

    private fun initMediaPlayer(filePath: String) {
        releaseMediaPlayer() // Release existing MediaPlayer instance

        mMediaPlayer = MediaPlayer()
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        mMediaPlayer?.setAudioAttributes(attributes)

        try {
            mMediaPlayer?.setDataSource(this@CompleteSessionActivity, Uri.parse("file://$filePath"))
            mMediaPlayer?.setOnPreparedListener {
                isReady = true
                mMediaPlayer?.start()
            }
            mMediaPlayer?.setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                false
            }
            mMediaPlayer?.prepareAsync()
        } catch (e: IOException) {
            Log.e(TAG, "Error setting data source", e)
        }
    }

    private fun releaseMediaPlayer() {
        mMediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mMediaPlayer = null
        isReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    companion object {
        private const val TAG = "CompleteSessionActivity"
    }
}
