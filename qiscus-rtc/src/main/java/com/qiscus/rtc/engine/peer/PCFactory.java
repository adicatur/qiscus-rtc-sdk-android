package com.qiscus.rtc.engine.peer;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.EglBase;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioManager;

/**
 * Created by fitra on 2/10/17.
 */

public class PCFactory {
    private static final String TAG = PCFactory.class.getSimpleName();

    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnectionFactory.Options options;

    private Boolean isError;

    public PCFactory(Context context) {
        peerConnectionFactory = null;
        options = null;
        isError = false;
        create(context);
    }

    private void create(Context context) {
        PeerConnectionFactory.initializeFieldTrials("WebRTC-MediaCodecVideoEncoder-AutomaticResize/Enabled/");
        WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);

        if (!PeerConnectionFactory.initializeAndroidGlobals(context, true, true, true)) {
            Log.e(TAG, "Failed to initializeAndroidGlobals");
            isError = true;
        }

        peerConnectionFactory = new PeerConnectionFactory(options);

        Log.d(TAG, "Peer connection factory created");
    }

    public void setVideoHwAcceleration(EglBase.Context renderEGLContext) {
        peerConnectionFactory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
    }

    public PeerConnection createPeerConnection(PeerConnection.RTCConfiguration rtcConfig, MediaConstraints pcConstraints, PeerConnection.Observer pcObserver) {
        return peerConnectionFactory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }

    public MediaStream createLocalMediaStream(String label) {
        return peerConnectionFactory.createLocalMediaStream(label);
    }

    public AudioSource createAudioSource(MediaConstraints audioConstraints) {
        return peerConnectionFactory.createAudioSource(audioConstraints);
    }

    public AudioTrack createAudioTrack(String label, AudioSource audioSource) {
        return peerConnectionFactory.createAudioTrack(label, audioSource);
    }

    public VideoSource createVideoSource(VideoCapturerAndroid capturer, MediaConstraints videoConstraints) {
        return peerConnectionFactory.createVideoSource(capturer, videoConstraints);
    }

    public VideoTrack createVideoTrack(String label, VideoSource videoSource) {
        return peerConnectionFactory.createVideoTrack(label, videoSource);
    }

    public void dispose() {
        peerConnectionFactory.dispose();
        options = null;
    }
}

