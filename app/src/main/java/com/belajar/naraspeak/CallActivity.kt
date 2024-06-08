package com.belajar.naraspeak

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.belajar.naraspeak.data.model.DataModel
import com.belajar.naraspeak.data.model.DataModelType
import com.belajar.naraspeak.data.repository.VideoCallRepository
import com.belajar.naraspeak.data.webrtc.FirebaseClient
import com.belajar.naraspeak.databinding.ActivityCallBinding

class CallActivity : AppCompatActivity(), VideoCallRepository.WebRTCConnectionListener {

    private lateinit var binding: ActivityCallBinding
    private lateinit var repository: VideoCallRepository
    private val firebaseClient: FirebaseClient = FirebaseClient()

    private var isMicrophoneMuted: Boolean = false
    private var isCameraDisabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = VideoCallRepository.getInstance(firebaseClient)
        init()


    }

    private fun init() {
        repository.setRemoteView(binding.remoteView)
        repository.setLocalView(binding.localView)

        repository.connectionListener = this

        binding.callBtn.setOnClickListener {
            repository.sendCallRequest(
                binding.targetUserNameEt.text.toString(),
                object : FirebaseClient.FirebaseStatusListener {
                    override fun onError() {
                        Toast.makeText(this@CallActivity, "Couldnt make the call", Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess() {
                    }

                })
        }

        repository.detectCallRequest(object : FirebaseClient.NewEventListener {
            override fun onNewEvent(dataModel: DataModel) {
                if (dataModel.dataModelType == DataModelType.StartCall) {
                    runOnUiThread {
                        binding.incomingNameTV.text = dataModel.sender + " is Calling you"
                        binding.incomingCallLayout.visibility = View.VISIBLE

                        binding.acceptButton.setOnClickListener {
                            //receive the call
                            repository.startCall(dataModel.sender.toString())

                            binding.incomingCallLayout.visibility = View.GONE
                        }
                        binding.rejectButton.setOnClickListener {
                            binding.incomingCallLayout.visibility = View.GONE
                        }
                    }
                }
            }

        })
        binding.switchCameraButton.setOnClickListener {
            repository.switchCamera()
        }

        binding.micButton.setOnClickListener {

            repository.muteMicrophone(isMicrophoneMuted)
            isMicrophoneMuted = !isMicrophoneMuted
        }

        binding.videoButton.setOnClickListener {

            repository.disableCamera(isCameraDisabled)
            isCameraDisabled = !isCameraDisabled
        }

        binding.endCallButton.setOnClickListener {
            repository.disconnect()
            finish()
        }
    }

    override fun webRtcConnected() {
        runOnUiThread {
            binding.incomingCallLayout.visibility = View.GONE
            binding.whoToCallLayout.visibility = View.GONE
            binding.callLayout.visibility = View.VISIBLE
        }
    }

    override fun webRtcClosed() {
        runOnUiThread {
            finish()
        }
    }


}