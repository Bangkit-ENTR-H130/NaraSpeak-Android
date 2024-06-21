package com.bangkit.naraspeak.ui.videocall

import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.model.DataModel
import com.bangkit.naraspeak.data.model.DataModelType
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.local.HistoryEntity
import com.bangkit.naraspeak.databinding.ActivityVideoCallBinding
import com.bangkit.naraspeak.helper.HistoryViewModelFactory
import com.bangkit.naraspeak.helper.createTemptFile
import com.bangkit.naraspeak.ui.result.CompleteSessionActivity
import org.webrtc.RendererCommon
import java.io.IOException
import java.util.Locale
import java.util.Random

class VideoCallActivity : AppCompatActivity(), VideoCallRepository.WebRTCConnectionListener {

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var videoCallRepository: VideoCallRepository
    private val firebaseClient: FirebaseClient = FirebaseClient()

    private var isMuted: Boolean = false
    private val isCameraDisabled: Boolean = false

    private lateinit var mSpeechRecognizer: SpeechRecognizer
    private lateinit var mSpeechRecognizerIntent: Intent
    private var isListening = false

    private var generateRandomTopic: String? = null

    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFilePath: String? = null
    private val history = HistoryEntity()

    private val viewModel by viewModels<VideoCallViewModel> {
        HistoryViewModelFactory.getHistoryInstance(this@VideoCallActivity)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        videoCallRepository = VideoCallRepository.getInstance(firebaseClient)

        val randomTopic = arrayOf("Weather", "Sport", "E-Sports", "Economy", "Business", "Invest", "Technology", "Boys Talk", "Girl Talk")
        val random = Random()
        val randomIndex = random.nextInt(randomTopic.size)
        generateRandomTopic = randomTopic[randomIndex]


        initiateVideoCall()



    }

    private fun initiateVideoCall() {
        videoCallRepository.setLocalView(binding.vcUser1)
        videoCallRepository.setRemoteView(binding.vcUser2)

        binding.vcUser1.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        binding.vcUser2.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)


        videoCallRepository.connectionListener = this

        binding.btnRetry.setOnClickListener {
            videoCallRepository.sendCallRequest(
                object : FirebaseClient.FirebaseStatusListener {
                    override fun onError() {
                        Toast.makeText(
                            this@VideoCallActivity,
                            "Couldnt make the call",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    override fun onSuccess() {
                    }
                })


        }



        videoCallRepository.detectCallRequest(object : FirebaseClient.NewEventListener {
            override fun onNewEvent(dataModel: DataModel) {
                if (dataModel.dataModelType == DataModelType.StartCall) {
                    runOnUiThread {
                        binding.tvRecommendedTopic.text = "Recommended topic: $generateRandomTopic"

                        binding.btnRetry.setOnClickListener {
                            //receive the call
                            Log.d("VideoCallActivity", "Call accepted from ${dataModel.sender}")
                            videoCallRepository.startCall(dataModel.sender.toString())
                            startRecordAudio()


//                            videoCallRepository.on("receive_audio_text"
//                            ) { args ->
//                                for (i in args) {
//                                    Log.d(TAG, i.toString())
//                                    videoCallRepository.on("send_audio_data"
//                                    ) {
//                                        Log.d(TAG, "sendAudio $it")
//                                    }
//                                    runOnUiThread {
//                                        binding.tvRecommendedTopic.text =
//                                            i.toString()
//                                    }
//                                }
//
//
//
//                            }


//                                            val buffer = ByteArray(minBufferSize)
//                                            while (isRecording) {
//                                                val read = audioRecord.read(buffer, 0, buffer.size)
//                                                if (read > 0) {
//                                                    socketManager.sendMessage("send_audio_data", buffer.toString(Charsets.ISO_8859_1))
//                                                }
//                                            }



                        }

                    }

                }
            }

        })
        binding.cardOverlayCall.btnCameraSwitch.setOnClickListener {
            videoCallRepository.switchCamera()
        }

        binding.cardOverlayCall.btnHungUp.setOnClickListener {
            videoCallRepository.disconnect()
            stopRecordAudio()
            val intent = Intent(this@VideoCallActivity, CompleteSessionActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.cardOverlayCall.btnMute.setOnClickListener {
            videoCallRepository.muteMicrophone(isMuted)
            isMuted = !isMuted
        }




    }

    override fun webRtcConnected() {
        runOnUiThread {
            binding.callLayout.visibility = View.VISIBLE
            binding.layoutLoading.visibility = View.GONE
            binding.cardOverlayCall.root.visibility = View.VISIBLE
            binding.tvRecommendedTopic.visibility = View.VISIBLE
//            binding.tvTimer.visibility = View.VISIBLE
            startRecordAudio()

        }

    }

    private fun startRecordAudio() {
        val fileLocation = createTemptFile(this@VideoCallActivity)
        currentAudioFilePath = fileLocation.path

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                setOutputFile(fileLocation)
            } else {
                setOutputFile(fileLocation.absolutePath)
            }
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
        Log.d(TAG, "recordingLocation: ${fileLocation.path}")
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        } catch (e: IOException) {
            Log.e(TAG, "startRecording: ${e.message}")
        } catch (e: IllegalStateException) {
            Log.e(TAG, "startRecording: ${e.message}")
        }

    }

    private fun stopRecordAudio() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null

        currentAudioFilePath?.let {
            history.audio = it
            Log.d(TAG, "stopRecording: $it")
            viewModel.insert(history)

        }



    }

    override fun webRtcClosed() {
        runOnUiThread {
            val intent = Intent(this@VideoCallActivity, CompleteSessionActivity::class.java)
            startActivity(intent)
            finish()

        }
        stopRecordAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecordAudio()
    }

    override fun onPause() {
        super.onPause()
        stopRecordAudio()
    }

    override fun onStop() {
        super.onStop()
        stopRecordAudio()
    }

    companion object {
        private const val TAG = "VideoCallActivity"
        const val EXTRA_AUDIO = "extra_audio"
    }



}