package com.belajar.naraspeak.data.webrtc

import android.content.Context
import android.util.Log
import com.belajar.naraspeak.data.model.DataModel
import com.belajar.naraspeak.data.model.DataModelType
import com.google.gson.Gson
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.Observer
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack

class WebRtcClient(
    private val context: Context,
    private val username: String,
    private val observer: Observer
) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var peerConnection: PeerConnection

    private val iceServer = ArrayList<PeerConnection.IceServer>()
    private lateinit var videoCapture: CameraVideoCapturer
    private val gson: Gson = Gson()

    private val mediaConstraints = MediaConstraints()

    private lateinit var localVideoSource: VideoSource
    private lateinit var localAudioSource: AudioSource


    private lateinit var videoTrack: VideoTrack
    private lateinit var audioTrack: AudioTrack
    private lateinit var localStream: MediaStream

    var peerListener: PeerListener? = null


    init {
        initPeerConnectionFactory()
        peerConnectionFactory = createPeerConnectionFactory()
        peerConnection = createPeerConnection(observer)

        iceServer.add(
            PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
                .setUsername("83eebabf8b4cce9d5dbcb649")
                .setPassword("2D7JvfkOQtBdYW3R")
                .createIceServer()
        )

        localVideoSource = peerConnectionFactory.createVideoSource(false)
        localAudioSource = peerConnectionFactory.createAudioSource(object : MediaConstraints() {

        })

        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

    }

    private fun initPeerConnectionFactory() {
        val option = PeerConnectionFactory.InitializationOptions.builder(context)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/").setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(option)

    }

    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        val option = PeerConnectionFactory.Options()
        option.disableEncryption = false
        option.disableNetworkMonitor = false
        val peerConnectionFact = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))
            .setOptions(option)
            .createPeerConnectionFactory()
        return peerConnectionFact
    }


    private fun createPeerConnection(observer: Observer): PeerConnection {
        return peerConnectionFactory.createPeerConnection(iceServer, observer) as PeerConnection
    }


    private fun initSurfaceRenderer(surfaceRenderer: SurfaceViewRenderer) {
        surfaceRenderer.setEnableHardwareScaler(true)
        surfaceRenderer.setMirror(true)
        surfaceRenderer.init(eglBaseContext, null)
    }


    fun initLocalViewRenderer(surfaceRenderer: SurfaceViewRenderer) {
        initSurfaceRenderer(surfaceRenderer)
        startLocalVideoStreaming(surfaceRenderer)
    }

    fun initRemoteViewRenderer(surfaceRenderer: SurfaceViewRenderer) {
        initSurfaceRenderer(surfaceRenderer)
    }


    private fun startLocalVideoStreaming(surfaceRenderer: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, eglBaseContext)

        videoCapture = getVideoCapturer()
        videoCapture.initialize(surfaceTextureHelper, context, localVideoSource.capturerObserver)
        videoCapture.startCapture(640, 480, 30)

        videoTrack =
            peerConnectionFactory.createVideoTrack("${LOCAL_TRACK_ID}_video", localVideoSource)

        videoTrack.addSink(surfaceRenderer)

        audioTrack =
            peerConnectionFactory.createAudioTrack("${LOCAL_TRACK_ID}_audio", localAudioSource)

        audioTrack.enabled()

        localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(videoTrack)
        localStream.addTrack(audioTrack)

        peerConnection.addStream(localStream)

    }

    private fun getVideoCapturer(): CameraVideoCapturer {
        val cameraEnumerator = Camera2Enumerator(context)

        for (i in cameraEnumerator.deviceNames) {
            if (cameraEnumerator.isFrontFacing(i)) {
                return cameraEnumerator.createCapturer(i, null)
            }
        }
        throw IllegalStateException("No front facing camera found")
    }

    fun call(target: String) {
        try {
            peerConnection.createOffer(object : WebRtcSdp() {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    super.onCreateSuccess(sessionDescription)
                    peerConnection.setLocalDescription(
                        object : WebRtcSdp() {
                            override fun onSetSuccess() {
                                super.onSetSuccess()
                                peerListener?.onTransferDataToOtherPeer(
                                    DataModel(
                                        sender = username,
                                        target = target,
                                        data = sessionDescription.description,
                                        dataModelType = DataModelType.Offer

                                    )
                                )

                            }

                        }, sessionDescription
                    )
                }
            }, mediaConstraints)
        } catch (e: Exception) {
            Log.e(TAG, "call: ${e.message}")
        }
    }

    fun answer(target: String) {
        try {
            peerConnection.createAnswer(object : WebRtcSdp() {
                override fun onCreateSuccess(sessionDescription: SessionDescription) {
                    super.onCreateSuccess(sessionDescription)
                    peerConnection.setLocalDescription(
                        object : WebRtcSdp() {
                            override fun onSetSuccess() {
                                super.onSetSuccess()
                                peerListener?.onTransferDataToOtherPeer(
                                    DataModel(
                                        sender = username,
                                        target = target,
                                        data = sessionDescription.description,
                                        dataModelType = DataModelType.Answer

                                    )
                                )

                            }

                        }, sessionDescription
                    )
                }
            }, mediaConstraints)
        } catch (e: Exception) {
            Log.e(TAG, "call: ${e.message}")
        }
    }


    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        peerConnection.setRemoteDescription(object : WebRtcSdp() {

        }, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate) {
        peerConnection.addIceCandidate(iceCandidate)
    }

    fun sendIceCandidate(iceCandidate: IceCandidate, target: String) {
        addIceCandidate(iceCandidate)
        peerListener?.onTransferDataToOtherPeer(
            DataModel(
                sender = username,
                target = target,
                data = gson.toJson(iceCandidate),
                dataModelType = DataModelType.IceCandidate,
            )
        )

    }


    fun switchCamera() {
        videoCapture.switchCamera(null)
    }

    fun muteMicrophone(isEnabled: Boolean) {
        audioTrack.setEnabled(isEnabled)
    }

    fun disableVideo(isEnabled: Boolean) {
        videoTrack.setEnabled(isEnabled)
    }

    fun disconnect() {
        videoTrack.dispose()
        videoCapture.dispose()
        videoCapture.stopCapture()
        peerConnection.close()

    }

    interface PeerListener {
        fun onTransferDataToOtherPeer(model: DataModel)
    }


    companion object {
        private const val TAG = "WebRtcClient"
        private val eglBaseContext: EglBase.Context = EglBase.create().eglBaseContext
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_stream"
    }

}