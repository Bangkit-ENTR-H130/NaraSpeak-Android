package com.bangkit.naraspeak.ui.videocall

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.model.DataModel
import com.bangkit.naraspeak.data.model.DataModelType
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.databinding.ActivityVideoCallBinding
import org.webrtc.RendererCommon

class VideoCallActivity : AppCompatActivity(), VideoCallRepository.WebRTCConnectionListener {

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var videoCallRepository: VideoCallRepository
    private val firebaseClient: FirebaseClient = FirebaseClient()

    private var isMuted: Boolean = false
    private val isCameraDisabled: Boolean = false
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


        initiateVideoCall()


    }

    private fun initiateVideoCall() {
        videoCallRepository.setLocalView(binding.vcUser1)
        videoCallRepository.setRemoteView(binding.vcUser2)

        binding.vcUser1.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        binding.vcUser2.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)




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
                            Log.d("VideoCallActivity", "Call accepted from ${dataModel.sender}")
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

        }

    }

    override fun webRtcClosed() {
        runOnUiThread {
            finish()

        }
    }



}