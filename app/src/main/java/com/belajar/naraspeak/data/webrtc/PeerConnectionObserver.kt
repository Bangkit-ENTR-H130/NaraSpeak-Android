package com.belajar.naraspeak.data.webrtc

import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver

abstract class PeerConnectionObserver: PeerConnection.Observer {
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {


    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) {
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
    }

    override fun onIceCandidate(iceCandidate: IceCandidate?) {
    }

    override fun onIceCandidatesRemoved(listIceCandidate: Array<out IceCandidate>?) {
    }

    override fun onAddStream(mediaStream: MediaStream?) {
    }

    override fun onRemoveStream(p0: MediaStream?) {
    }

    override fun onDataChannel(p0: DataChannel?) {
    }

    override fun onRenegotiationNeeded() {
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
    }
}