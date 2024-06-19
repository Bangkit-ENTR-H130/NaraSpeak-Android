package com.bangkit.naraspeak.ui.videocall

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.model.DataModel
import com.bangkit.naraspeak.data.model.DataModelType
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.databinding.ActivityVideoCallBinding
import io.socket.emitter.Emitter
import org.webrtc.RendererCommon
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

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

        transcribingEvent()


    }

    private fun transcribingEvent() {
        mSpeechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {
                Log.d("SpeechRecognizer", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "Speech started")
            }

            override fun onRmsChanged(v: Float) {
                Log.d("SpeechRecognizer", "RMS changed: $v")
            }

            override fun onBufferReceived(bytes: ByteArray) {
                Log.d("SpeechRecognizer", "Buffer received")
            }

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognizer", "Speech ended")

// if (isListening) {
// restartListening()
// }
            }


            override fun onError(i: Int) {
                Log.e("SpeechRecognizer", "Error: $i")

                if (i == SpeechRecognizer.ERROR_NO_MATCH ) {
                    restartListening()
                }
            }

            override fun onResults(bundle: Bundle) {
                val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Log.d("TRANSCRIPT", "${matches?.get(0)}")
                Log.d("SpeechRecognizer", "Results received: $matches")
                if (matches != null) {
//                    editText.append(matches[0] + "\n")
                }
                if (isListening) {
                    restartListening()
                }
            }

            override fun onPartialResults(bundle: Bundle) {
                Log.d("SpeechRecognizer", "Partial results received")
            }

            override fun onEvent(i: Int, bundle: Bundle) {
                Log.d("SpeechRecognizer", "Event: $i")
            }
        })

    }

    private fun startListening() {
            isListening = true
            Log.d("SpeechRecognizer", "Starting to listen")
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent)

    }

    private fun stopListening() {
        isListening = false
        Log.d("SpeechRecognizer", "Stopping listening")
        mSpeechRecognizer.stopListening()
    }

    private fun restartListening() {
        Log.d("SpeechRecognizer", "Restarting listening")
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent)
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
                        binding.incomingNameTV.text = dataModel.sender + " is Calling you"
//                        binding.incomingCallLayout.visibility = View.VISIBLE
                        binding.tvRecommendedTopic.text = generateRandomTopic

                        binding.btnRetry.setOnClickListener {
                            //receive the call
                            Log.d("VideoCallActivity", "Call accepted from ${dataModel.sender}")
                            videoCallRepository.startCall(dataModel.sender.toString(), this@VideoCallActivity)
                            startListening()


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

                            binding.incomingCallLayout.visibility = View.GONE
                            binding.whoToCallLayout.visibility = View.GONE

                        }
                        binding.rejectButton.setOnClickListener {
                            binding.incomingCallLayout.visibility = View.GONE
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
            videoCallRepository.off("receive_audio_text"
            ) { }
            stopListening()
            finish()
        }

        binding.cardOverlayCall.btnMute.setOnClickListener {
            videoCallRepository.muteMicrophone(isMuted)
            isMuted = !isMuted
        }




    }

    override fun webRtcConnected() {
        runOnUiThread {
            binding.incomingCallLayout.visibility = View.GONE
            binding.whoToCallLayout.visibility = View.GONE
            binding.callLayout.visibility = View.VISIBLE
            binding.layoutLoading.visibility = View.GONE
            binding.cardOverlayCall.root.visibility = View.VISIBLE
            binding.tvRecommendedTopic.visibility = View.VISIBLE
            binding.tvTimer.visibility = View.VISIBLE

        }

    }

    override fun webRtcClosed() {
        runOnUiThread {
            finish()

        }
    }

    companion object {
        private const val TAG = "VideoCallActivity"
    }



}