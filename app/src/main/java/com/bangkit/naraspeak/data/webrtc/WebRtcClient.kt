package com.bangkit.naraspeak.data.webrtc

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionConfig
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.bangkit.naraspeak.BuildConfig
import com.bangkit.naraspeak.data.model.DataModel
import com.bangkit.naraspeak.data.model.DataModelType
import com.bangkit.naraspeak.helper.createTemptFile
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okio.IOException
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
import org.webrtc.audio.JavaAudioDeviceModule.AudioSamples
import java.net.URISyntaxException

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
    private var mediaRecorder: MediaRecorder? = null

    private lateinit var socket: Socket


    init {
        initPeerConnectionFactory()
        peerConnectionFactory = createPeerConnectionFactory()

        iceServer.add(
            PeerConnection.IceServer.builder("turn:a.relay.metered.ca:443?transport=tcp")
                .setUsername("83eebabf8b4cce9d5dbcb649")
                .setPassword("2D7JvfkOQtBdYW3R")
                .createIceServer()
        )

        peerConnection = createPeerConnection(observer)


        localVideoSource = peerConnectionFactory.createVideoSource(false)
        localAudioSource = peerConnectionFactory.createAudioSource(mediaConstraints)

        mediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        try {
            socket = IO.socket(BuildConfig.URL_STT)
            Log.d(TAG, "${socket.isActive}")
        } catch (e: URISyntaxException) {
            Log.e(TAG, "socket STT: ${e.message}, ${e.reason}")
        }

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
        return peerConnectionFactory.createPeerConnection(iceServer, observer) ?: peerConnection
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
                dataModelType = DataModelType.IceCandidate
            )
        )

    }

    fun socketConnect() {
        socket.connect()
    }

    fun socketDisconnect() {
        socket.disconnect()
    }

    fun sendMessage(event: String, message: String) {
        socket.emit(event, message)
    }

    fun on(event: String, listener: Emitter.Listener) {
        socket.on(event, listener)
    }

    fun off(event: String, listener: Emitter.Listener) {
        socket.off(event, listener)
    }



    fun startRecordAudio(context: Context) {
        val fileLocation = createTemptFile(context)
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

    fun stopRecordAudio() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null

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