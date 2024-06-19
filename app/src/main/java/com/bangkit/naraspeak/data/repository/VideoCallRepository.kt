package com.bangkit.naraspeak.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.bangkit.naraspeak.data.model.DataModel
import com.bangkit.naraspeak.data.model.DataModelType
import com.bangkit.naraspeak.data.firebase.FirebaseClient
import com.bangkit.naraspeak.data.webrtc.PeerConnectionObserver
import com.bangkit.naraspeak.data.webrtc.WebRtcClient
import com.google.gson.Gson
import io.socket.emitter.Emitter
import io.socket.emitter.Emitter.Listener
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import javax.inject.Inject

class VideoCallRepository(
    private val firebaseClient: FirebaseClient
) {

    private var currentUsername: String? = null
    private var currentTarget: String? = null
    private var remoteView: SurfaceViewRenderer? = null
    private lateinit var webRtcClient: WebRtcClient

    var connectionListener: WebRTCConnectionListener? = null


    fun login(
        username: String,
        statusListener: FirebaseClient.FirebaseStatusListener,
        context: Context
    ) {
        webRtcClient = WebRtcClient(
            context,
            username,
            object : PeerConnectionObserver() {
                override fun onAddStream(mediaStream: MediaStream?) {
                    super.onAddStream(mediaStream)
                    try {
                        mediaStream?.videoTracks?.get(0)?.addSink(remoteView)
                    } catch (e: Exception) {
                        Log.e(TAG, e.message.toString())
                    }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                    super.onConnectionChange(newState)
                        if (newState == PeerConnection.PeerConnectionState.DISCONNECTED) {
                            connectionListener?.webRtcClosed()
                        }
                        if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                            connectionListener?.webRtcConnected()
                        }

                }

                override fun onIceCandidate(iceCandidate: IceCandidate?) {
                    super.onIceCandidate(iceCandidate)
                    iceCandidate?.let { webRtcClient.sendIceCandidate(it, currentTarget.toString()) }

                }
            }
        )
        firebaseClient.login(username, object : FirebaseClient.FirebaseStatusListener,
            WebRtcClient.PeerListener {
            override fun onError() {
                statusListener.onError()

            }

            override fun onSuccess() {
                statusListener.onSuccess()
                currentUsername = username
                webRtcClient.peerListener = this
            }

            override fun onTransferDataToOtherPeer(model: DataModel) {
                firebaseClient.sendData(
                    model,
                    object : FirebaseClient.FirebaseStatusListener {
                        override fun onError() {

                        }

                        override fun onSuccess() {

                        }

                    }
                )
            }
        })
    }

    fun sendCallRequest( statusListener: FirebaseClient.FirebaseStatusListener) {
        firebaseClient.sendData(
            DataModel(
                sender = currentUsername,
                data = null,
                dataModelType = DataModelType.StartCall
            ), statusListener
        )
    }

    fun detectCallRequest(newEventListener: FirebaseClient.NewEventListener) {
        firebaseClient.observeIncomingData(object : FirebaseClient.NewEventListener {
            override fun onNewEvent(dataModel: DataModel) {
                when (dataModel.dataModelType) {
                    DataModelType.StartCall -> {
                        currentTarget = dataModel.sender
                        newEventListener.onNewEvent(dataModel)
                    }

                    DataModelType.Offer -> {
                        currentTarget = dataModel.sender

                        webRtcClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.OFFER,
                                dataModel.data
                            )
                        )

                        webRtcClient.answer(dataModel.sender.toString())
                    }

                    DataModelType.Answer -> {
                        currentTarget = dataModel.sender

                        webRtcClient.onRemoteSessionReceived(
                            SessionDescription(
                                SessionDescription.Type.ANSWER,
                                dataModel.data
                            )
                        )

                    }

                    DataModelType.IceCandidate -> {
                        val gson = Gson()
                        val iceCandidate = gson.fromJson(dataModel.data, IceCandidate::class.java)
                        webRtcClient.addIceCandidate(iceCandidate)

                    }

                    null -> {}
                }
            }

        })
    }

    fun setRemoteView(surfaceViewRenderer: SurfaceViewRenderer) {
        webRtcClient.initRemoteViewRenderer(surfaceViewRenderer)
        remoteView = surfaceViewRenderer
    }

    fun setLocalView(surfaceViewRenderer: SurfaceViewRenderer) {
        webRtcClient.initLocalViewRenderer(surfaceViewRenderer)

    }

    fun startCall(target: String, context: Context) {
        webRtcClient.call(target)
//        webRtcClient.socketConnect()
//        webRtcClient.startRecordAudio(context)
    }

    fun switchCamera() {
        webRtcClient.switchCamera()
    }

    fun muteMicrophone(isEnabled: Boolean) {
        webRtcClient.muteMicrophone(isEnabled)
    }

    fun disableCamera(isEnabled: Boolean) {
        webRtcClient.disableVideo(isEnabled)
    }

    fun disconnect() {
        webRtcClient.disconnect()
//        webRtcClient.stopRecordAudio()
//        webRtcClient.socketDisconnect()
    }

    fun findMatch() {
//        firebaseClient.sendData()
    }

    fun sConnect() {
    }

    fun sDisconnect() {
    }

    fun sendMessageSTT(event: String, message: String) {
        webRtcClient.sendMessage(event, message)
    }

    fun on(event: String, listener: Emitter.Listener) {
        webRtcClient.on(event, listener)
    }

    fun off(event: String, listener: Emitter.Listener) {
        webRtcClient.off(event, listener)
    }

    interface WebRTCConnectionListener {
        fun webRtcConnected()
        fun webRtcClosed()
    }


    companion object {
        private const val TAG = "VideoCallRepository"
        private var instance: VideoCallRepository? = null
        fun getInstance(
            firebaseClient: FirebaseClient
        ): VideoCallRepository =
            instance ?: synchronized(this) {
                instance ?: VideoCallRepository(firebaseClient)
            }.also { instance = it }
    }

}