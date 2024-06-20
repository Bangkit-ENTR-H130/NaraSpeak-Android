package com.bangkit.naraspeak.ui.groupcall

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.model.DataModel
import com.bangkit.naraspeak.data.model.DataModelType
import com.bangkit.naraspeak.data.repository.VideoCallRepository
import com.bangkit.naraspeak.databinding.ActivityGroupCallBinding

class GroupCallActivity : AppCompatActivity(), VideoCallRepository.WebRTCConnectionListener {
    private lateinit var binding: ActivityGroupCallBinding
    private lateinit var repository: VideoCallRepository
    private val firebaseClient = FirebaseClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGroupCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        repository = VideoCallRepository.getInstance(firebaseClient)

        repository.setLocalView(binding.vcUser4)
        repository.setRemoteView(binding.vcUser2)
        repository.setRemoteView(binding.vcUser3)
        repository.setRemoteView(binding.vcUser1)



        repository.connectionListener = this


        repository.detectCallGroup(object : FirebaseClient.NewEventListener {
            override fun onNewEvent(dataModel: DataModel) {
                if (dataModel.dataModelType == DataModelType.StartGroup) {
                    runOnUiThread {
                        binding.incomingNameTV.text = dataModel.sender + " is Calling you"

                        binding.acceptButton.setOnClickListener {
                            dataModel.groupTarget?.let { it1 -> repository.startGroupCall(it1) }

                            binding.incomingCallLayout.visibility = View.GONE
                        }





                        }
                }
            }

        })

    }

    override fun webRtcConnected() {
        runOnUiThread {
            binding.callLayout.visibility = View.VISIBLE
            binding.incomingCallLayout.visibility = View.GONE

        }

    }

    override fun webRtcClosed() {
        runOnUiThread {
            finish()

        }
    }

    companion object {
        private const val TAG = "GroupCallActivity"
    }
}