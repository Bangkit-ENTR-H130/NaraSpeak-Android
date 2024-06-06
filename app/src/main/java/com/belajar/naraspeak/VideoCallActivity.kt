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
import com.belajar.naraspeak.databinding.ActivityVideoCallBinding

class VideoCallActivity : AppCompatActivity(), VideoCallRepository.WebRTCConnectionListener {

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var videoCallRepository: VideoCallRepository
    private val firebaseClient: FirebaseClient = FirebaseClient()

    private var isMuted: Boolean = false
    private val isOffCam: Boolean = false
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


        initiate()


    }

    private fun initiate() {
        videoCallRepository.setLocalView(binding.vcUser1)
        videoCallRepository.setRemoteView(binding.vcUser2)

        videoCallRepository.connectionListener = this

        binding.callBtn.setOnClickListener {
            videoCallRepository.sendCallRequest(
                binding.targetUserNameEt.text.toString(),
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
                        binding.incomingCallLayout.visibility = View.VISIBLE

                        binding.acceptButton.setOnClickListener {
                            //receive the call
//                        videoCallRepository.st(dataModel.sender.toString())
                            videoCallRepository.startCall(dataModel.sender.toString())
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
            binding.vcUser2.clearImage()
            binding.vcUser1.clearImage()
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
            videoCallRepository.setLocalView(binding.vcUser1)
            videoCallRepository.setRemoteView(binding.vcUser2)

        }

    }

    override fun webRtcClosed() {
        runOnUiThread {
            finish()

        }
    }



}