package com.bangkit.naraspeak.ui.result

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.assemblyai.api.AssemblyAI
import com.assemblyai.api.resources.transcripts.types.TranscriptOptionalParams
import com.bangkit.naraspeak.BuildConfig
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.local.HistoryEntity
import com.bangkit.naraspeak.databinding.ActivityCompleteSessionBinding
import com.bangkit.naraspeak.helper.Result
import com.bangkit.naraspeak.helper.CommonViewModelFactory
import com.bangkit.naraspeak.helper.HistoryViewModelFactory
import com.bangkit.naraspeak.helper.showLoading
import com.bangkit.naraspeak.ui.homepage.HomepageActivity
import com.bangkit.naraspeak.ui.videocall.VideoCallViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class CompleteSessionActivity : AppCompatActivity() {
    private var mMediaPlayer: MediaPlayer? = null
    private lateinit var binding: ActivityCompleteSessionBinding
    private lateinit var currentAudioPath: String
    private var isReady: Boolean = false

    private val viewModel: VideoCallViewModel by viewModels {
        HistoryViewModelFactory.getHistoryInstance(this)
    }

    private val uploadViewModel: CompleteSessionViewModel by viewModels {
        CommonViewModelFactory.getInstance()
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

        viewModel.getHistory().observe(this) {
            currentAudioPath = "${it.audio}"

            binding.cardRecord.placeholder.setOnClickListener {
                if (isReady) {
                    handleMediaPlayer()
                } else {
                    initMediaPlayer(currentAudioPath)
                }
            }

            binding.cardRecord.btnYes.setOnClickListener {
                lifecycleScope.launch {
                    val aai: AssemblyAI = AssemblyAI.builder()
                        .apiKey(BuildConfig.KEY)
                        .build()

                    val params = TranscriptOptionalParams.builder()
                        .speakerLabels(false)
                        .build()

                    val transcript = withContext(Dispatchers.IO) {
                        aai.transcripts().transcribe(File(currentAudioPath), params)
                    }

                    val jsonText = """
                        {
                            "text": "${transcript.text.get()}"
                        }
                    """.trimIndent()
                    val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonText)

                    withContext(Dispatchers.Main) {
                        uploadViewModel.postGrammarPrediction(requestBody).observe(this@CompleteSessionActivity) { result ->
                            when (result) {
                                is Result.Failed -> {
                                    showLoading(false, binding.progressBar)
                                    Toast.makeText(this@CompleteSessionActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                }
                                Result.Loading -> {
                                    showLoading(true, binding.progressBar)
                                }
                                is Result.Success -> {
                                    showLoading(false, binding.progressBar)
                                    val intent = Intent(this@CompleteSessionActivity, ResultActivity::class.java)
                                    intent.putExtra(EXTRA_CORRECTION, result.data.predictions?.first())
                                    intent.putExtra(EXTRA_ORIGINAL, transcript.text.get())

                                    Log.d(TAG, "grammarCheck: ${result.data.predictions?.first()}"
                                    )
                                    Toast.makeText(this@CompleteSessionActivity, "Grammar Check Success", Toast.LENGTH_SHORT).show()

                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
            }

            binding.cardRecord.btnNo.setOnClickListener {
                val intent = Intent(this@CompleteSessionActivity, HomepageActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun initMediaPlayer(filePath: String) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            mMediaPlayer?.setAudioAttributes(attributes)

            try {
                mMediaPlayer?.setDataSource(this@CompleteSessionActivity, Uri.parse(filePath))
                mMediaPlayer?.setOnPreparedListener {
                    isReady = true
                    mMediaPlayer?.start()
                    binding.cardRecord.placeholder.setBackgroundColor(getColor(R.color.black))
                }
                mMediaPlayer?.setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    false
                }
                mMediaPlayer?.prepareAsync()
            } catch (e: IOException) {
                Log.e(TAG, "Error setting data source", e)
            }
        } else {
            handleMediaPlayer()
        }
    }

    private fun handleMediaPlayer() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                binding.cardRecord.placeholder.setBackgroundColor(getColor(R.color.white)) // Set to original color or whatever color you prefer
            } else {
                it.start()
                binding.cardRecord.placeholder.setBackgroundColor(getColor(R.color.black))
            }
        }
    }

    private fun releaseMediaPlayer() {
        mMediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
            binding.cardRecord.placeholder.background = resources.getDrawable(R.drawable.frame_121, null)
        }
        mMediaPlayer = null
        isReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    override fun onStop() {
        super.onStop()
        releaseMediaPlayer()
    }

    override fun onPause() {
        super.onPause()
        releaseMediaPlayer()
    }

    companion object {
        private const val TAG = "CompleteSessionActivity"
        const val EXTRA_ORIGINAL = "extra_original"
        const val EXTRA_CORRECTION = "extra_correction"
    }
}
