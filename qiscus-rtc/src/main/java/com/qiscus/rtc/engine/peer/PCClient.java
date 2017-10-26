package com.qiscus.rtc.engine.peer;

import android.content.Context;
import android.util.Log;

import com.qiscus.rtc.engine.util.LooperExecutor;

import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fitra on 2/10/17.
 */

public class PCClient {
    public static final String TAG = PCClient.class.getSimpleName();

    private static final PCClient instance = new PCClient();

    private final PCClient.PCObserver pcObserver = new PCClient.PCObserver();
    private final PCClient.SDPObserver sdpObserver = new PCClient.SDPObserver();
    private final LooperExecutor executor;

    private Context context;
    private PeerConnection peerConnection;
    private PCClient.PeerConnectionEvents events;
    private MediaConstraints pcConstraints;
    private MediaConstraints videoConstraints;
    private MediaConstraints audioConstraints;
    private MediaConstraints sdpMediaConstraints;
    private MediaStream mediaStream;
    private SessionDescription localSdp;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    private AudioTrack audioTrack;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private VideoCapturerAndroid videoCapturer;
    private VideoSource videoSource;
    private PCFactory pcFactory;
    private Boolean isError;
    private Boolean videoEnabled;
    private Boolean videoSourceStopped;
    private int numberOfCameras;
    private static String qualityVideo = "VP9";

    private PCClient() {
        executor = new LooperExecutor();
        executor.requestStart();
    }

    public static PCClient getInstance() {
        return instance;
    }

    public void init(Boolean videoEnabled, final PCFactory pcFactory, final Context context, final PCClient.PeerConnectionEvents events) {
        this.context = context;
        this.events = events;
        this.pcFactory = pcFactory;
        this.videoEnabled = videoEnabled;
        isError = false;
        localSdp = null;
        localVideoTrack = null;
        videoCapturer = null;
        videoSourceStopped = false;
    }

    public static String saveQualityVideo(String quality) {
        qualityVideo = quality;
        return qualityVideo;
    }

    public void createPeerConnection(final EglBase.Context renderEGLContext, final VideoRenderer.Callbacks localRender, final VideoRenderer.Callbacks remoteRender) {
        this.localRender = localRender;
        this.remoteRender = remoteRender;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                createMediaConstraintsInternal();
                createPeerConnectionInternal(renderEGLContext);
            }
        });
    }

    private void createMediaConstraintsInternal() {
        pcConstraints = new MediaConstraints();
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        numberOfCameras = CameraEnumerationAndroid.getDeviceCount();

        if (videoEnabled) {
            if (numberOfCameras == 0) {
                Log.w(TAG, "No camera on device, switch to audio only call");
                videoEnabled = false;
            }
        }

        if (videoEnabled) {
            int minVideoWidth = 1280;
            int maxVideoWidth = 1280;
            int minVideoHeight = 720;
            int maxVideoHeight = 720;
            int minVideoFps = 0;
            int maxVideoFps = 30;

            videoConstraints = new MediaConstraints();

            if (minVideoWidth > 0 && minVideoHeight > 0) {
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minWidth", Integer.toString(minVideoWidth)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxWidth", Integer.toString(maxVideoWidth)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minHeight", Integer.toString(minVideoHeight)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxHeight", Integer.toString(maxVideoHeight)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(minVideoFps)));
                videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(maxVideoFps)));
            }
        }

        audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression" , "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("levelControl" , "true"));

        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));

        if (videoEnabled) {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        } else {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        }
    }

    private void createPeerConnectionInternal(EglBase.Context renderEGLContext) {
        if (pcFactory == null || isError) {
            Log.e(TAG, "Peer connection factory is not created");
            return;
        }

        Log.d(TAG, "Create peer connection using constraints: " + pcConstraints.toString());

        if (videoConstraints != null) {
            Log.d(TAG, "Video constraints: " + videoConstraints.toString());
        }

        if (videoEnabled) {
            pcFactory.setVideoHwAcceleration(renderEGLContext);
        }

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(new PeerConnection.IceServer("stun:139.59.110.14:3478"));
        iceServers.add(new PeerConnection.IceServer("turn:139.59.110.14:3478", "sangkil", "qiscuslova"));

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        peerConnection = pcFactory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);

        mediaStream = pcFactory.createLocalMediaStream("ARDAMS");

        if (videoEnabled) {
            String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(0);
            String frontCameraDeviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();

            if (numberOfCameras > 1 && frontCameraDeviceName != null) {
                cameraDeviceName = frontCameraDeviceName;
            }

            Log.d(TAG, "Opening camera: " + cameraDeviceName);

            videoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null, true);

            if (videoCapturer == null) {
                Log.e(TAG, "Failed to open camera");
                return;
            }

            mediaStream.addTrack(createVideoTrack(videoCapturer));
        }

        audioTrack = pcFactory.createAudioTrack("ARDAMSa0", pcFactory.createAudioSource(audioConstraints));
        mediaStream.addTrack(audioTrack);

        peerConnection.addStream(mediaStream);

        Log.d(TAG, "Peer connection created");
    }

    public void createOffer() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection != null && !isError) {
                    Log.d(TAG, "Create offer");
                    peerConnection.createOffer(sdpObserver, sdpMediaConstraints);
                }
            }
        });
    }

    public void createAnswer() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection != null && !isError) {
                    Log.d(TAG, "Create answer");
                    peerConnection.createAnswer(sdpObserver, sdpMediaConstraints);
                }
            }
        });
    }

    public void setRemoteDescription(final SessionDescription sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection == null || isError) {
                    return;
                }

                String sdpDescription = sdp.description;

                if (videoEnabled) {
                    sdpDescription = preferCodec(sdpDescription, qualityVideo, false);
                    sdpDescription = setBandwith(sdpDescription, 50, 250);
                }

                /*
                if (videoEnabled) {
                    sdpDescription = setStartBitrate("VP8", sdpDescription, 720);
                    sdpDescription = setStartBitrate("VP9", sdpDescription, 720);
                    sdpDescription = setStartBitrate("H264", sdpDescription, 720);
                    sdpDescription = setAudioStartBitrate("opus", sdpDescription, 32);
                }
                */

                sdpDescription = preferCodec(sdpDescription, "opus", true);

                SessionDescription sdpRemote = new SessionDescription(sdp.type, sdpDescription);
                peerConnection.setRemoteDescription(sdpObserver, sdpRemote);
            }
        });
    }

    public void setRemoteCandidate(final IceCandidate candidate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection == null || isError) {
                    return;
                }

                peerConnection.addIceCandidate(candidate);
            }
        });
    }

    private VideoTrack createVideoTrack(VideoCapturerAndroid capturer) {
        videoSource = pcFactory.createVideoSource(capturer, videoConstraints);
        localVideoTrack = pcFactory.createVideoTrack("ARDAMSv0", videoSource);
        localVideoTrack.setEnabled(true);
        localVideoTrack.addRenderer(new VideoRenderer(localRender));
        return localVideoTrack;
    }

    public void setVideoEnabled(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (localVideoTrack != null) {
                    localVideoTrack.setEnabled(enable);
                }
            }
        });
    }

    public void switchCamera() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                switchCameraInternal();
            }
        });
    }

    public void setAudioEnabled(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (audioTrack != null) {
                    audioTrack.setEnabled(enable);
                }
            }
        });
    }

    private void switchCameraInternal() {
        if (!videoEnabled || numberOfCameras < 2 || isError || videoCapturer == null) {
            Log.e(TAG, "Failed to switch camera, nmber of cameras: " + numberOfCameras);
            return;
        }

        Log.d(TAG, "Switch camera");
        videoCapturer.switchCamera(null);
    }

    public void restartVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (videoSource != null && videoSourceStopped) {
                    Log.d(TAG, "Restart video source");
                    videoSource.restart();
                    videoSourceStopped = false;
                }
            }
        });
    }

    public void stopVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (videoSource != null && !videoSourceStopped) {
                    Log.d(TAG, "Stop video source.");
                    videoSource.stop();
                    videoSourceStopped = true;
                }
            }
        });
    }

    public void close() {
        closeInternal();
    }

    private void closeInternal() {
        Log.d(TAG, "Closing peer connection");

        if (peerConnection != null) {
            peerConnection.dispose();
            peerConnection = null;
        }

        Log.d(TAG, "Stopping capture.");

        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch(InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }

        Log.d(TAG, "Closing video source");

        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }

        events.onPeerConnectionClosed();
    }

    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        int mLineIndex = -1;
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        String codecRtpMap = null;
        String mediaDescription = "m=video ";

        if (isAudio) {
            mediaDescription = "m=audio ";
        }

        String[] lines = sdpDescription.split("\r\n");
        Pattern codecPattern = Pattern.compile(regex);

        for (int i = 0; (i < lines.length) && (mLineIndex == -1 || codecRtpMap == null); i++) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i;
                continue;
            }

            Matcher codecMatcher = codecPattern.matcher(lines[i]);

            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
            }
        }

        if (mLineIndex == -1) {
            Log.w(TAG, "No " + mediaDescription + " line, so can't prefer " + codec);
            return sdpDescription;
        }

        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec);
            return sdpDescription;
        }

        Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + ", prefer at " + lines[mLineIndex]);
        String[] origMLineParts = lines[mLineIndex].split(" ");

        if (origMLineParts.length > 3) {
            int origPartIndex = 0;
            StringBuilder newMLine = new StringBuilder();
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(codecRtpMap);

            for (; origPartIndex < origMLineParts.length; origPartIndex++) {
                if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
                    newMLine.append(" ").append(origMLineParts[origPartIndex]);
                }
            }

            lines[mLineIndex] = newMLine.toString();
            Log.d(TAG, "Change media description: " + lines[mLineIndex]);
        } else {
            Log.e(TAG, "Wrong SDP media description format: " + lines[mLineIndex]);
        }

        StringBuilder newSdpDescription = new StringBuilder();

        for (String line : lines) {
            newSdpDescription.append(line).append("\r\n");
        }

        return newSdpDescription.toString();
    }

    private static String setStartBitrate(String codec, String sdpDescription, int bitrateKbps) {
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        String codecRtpMap = null;
        String[] lines = sdpDescription.split("\r\n");
        Pattern codecPattern = Pattern.compile(regex);

        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);

            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }

        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec + " codec");
            return sdpDescription;
        }

        Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]);
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);

        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);

            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " +  codec + " " + lines[i]);
                lines[i] += "; " + "x-google-start-bitrate" + "=" + bitrateKbps;
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");

            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet = "a=fmtp:" + codecRtpMap + " " + "x-google-start-bitrate" + "=" + bitrateKbps;
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }
        }

        return newSdpDescription.toString();
    }

    private static String setAudioStartBitrate(String codec, String sdpDescription, int bitrateKbps) {
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        String codecRtpMap = null;
        String[] lines = sdpDescription.split("\r\n");
        Pattern codecPattern = Pattern.compile(regex);

        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);

            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }

        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec + " codec");
            return sdpDescription;
        }

        Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]);
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);

        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);

            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " +  codec + " " + lines[i]);
                lines[i] += "; " + "maxaveragebitrate" + "=" + (bitrateKbps * 1000);
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();

        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");

            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet = "a=fmtp:" + codecRtpMap + " " + "maxaveragebitrate" + "=" + (bitrateKbps * 1000);
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }
        }

        return newSdpDescription.toString();
    }

    private static String setBandwith(String sdpDescription, int audioBw, int videoBw) {
        String regex1 = "^a=mid:audio+[\r]?$";
        String regex2 = "^a=mid:video+[\r]?$";
        String[] lines = sdpDescription.split("\r\n");
        Pattern codecPattern1 = Pattern.compile(regex1);
        Pattern codecPattern2 = Pattern.compile(regex2);

        StringBuilder newSdpDescription = new StringBuilder();

        // Set audio bandwith
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher1 = codecPattern1.matcher(lines[i]);

            if (codecMatcher1.matches()) {
                Log.d(TAG, "Found mid:audio at " + lines[i]);
                lines[i] += "\r\n" + "b=AS:" + audioBw;
                break;
            }
        }

        // Set video bandwith
        for (int j = 0; j < lines.length; j++) {
            Matcher codecMatcher2 = codecPattern2.matcher(lines[j]);

            if (codecMatcher2.matches()) {
                Log.d(TAG, "Found mid:video at " + lines[j]);
                lines[j] += "\r\n" + "b=AS:" + videoBw;
                break;
            }
        }

        for (int k = 0; k < lines.length; k++) {
            newSdpDescription.append(lines[k]).append("\r\n");
        }

        Log.d(TAG, newSdpDescription.toString());
        return newSdpDescription.toString();
    }

    public static interface PeerConnectionEvents {
        public void onOfferLocalDescription(final SessionDescription sdp);
        public void onAnswerLocalDescription(final SessionDescription sdp);
        public void onIceState(String state);
        public void onIceCandidate(final IceCandidate candidate);
        public void onIceConnected();
        public void onIceDisconnected();
        public void onIceCompleted();
        public void onRemoveStream();
        public void onPeerConnectionClosed();
        public void onPeerConnectionError(final String description);
        public void onPeerConnectionStatsReady(final StatsReport[] reports);
    }

    private class PCObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    events.onIceCandidate(candidate);
                }
            });
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            //
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            Log.d(TAG, "Signaling state: " + newState);
        }

        @Override
        public void onIceConnectionChange(final PeerConnection.IceConnectionState newState) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IceConnection state: " + newState);

                    if (newState == PeerConnection.IceConnectionState.NEW ||
                            newState == PeerConnection.IceConnectionState.CONNECTED ||
                            newState == PeerConnection.IceConnectionState.FAILED ) {
                        events.onIceState(newState.name());
                    }

                    if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                        events.onIceConnected();
                    } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                        events.onIceDisconnected();
                    } else if (newState == PeerConnection.IceConnectionState.COMPLETED) {
                        events.onIceCompleted();
                    }else if (newState == PeerConnection.IceConnectionState.FAILED) {
                        Log.e(TAG, "IceConnection failed");
                    }
                }
            });
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Log.d(TAG, "IceGathering state: " + newState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Log.d(TAG, "IceConnection receiving changed to: " + receiving);
        }

        @Override
        public void onAddStream(final MediaStream stream){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection == null || isError) {
                        return;
                    }

                    if (stream.audioTracks.size() > 1 || stream.videoTracks.size() > 1) {
                        Log.e(TAG, "Weird-looking stream: " + stream);
                        return;
                    }

                    if (stream.videoTracks.size() == 1) {
                        remoteVideoTrack = stream.videoTracks.get(0);
                        remoteVideoTrack.setEnabled(true);
                        remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
                    }
                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream stream){
            Log.w(TAG, "Stream removed.");
            events.onRemoveStream();
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
            //
        }

        @Override
        public void onRenegotiationNeeded() {
            //
        }
    }

    private class SDPObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription origSdp) {
            if (localSdp != null) {
                Log.e(TAG, "Multiple SDP created");
                return;
            }

            String sdpDescription = origSdp.description;

            if (videoEnabled) {
                sdpDescription = preferCodec(sdpDescription, qualityVideo, false);
            }

            sdpDescription = preferCodec(sdpDescription, "opus", true);

            final SessionDescription sdp = new SessionDescription(origSdp.type, sdpDescription);
            localSdp = sdp;

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection != null && !isError) {
                        Log.d(TAG, "Set local SDP from " + sdp.type);
                        peerConnection.setLocalDescription(sdpObserver, sdp);
                    }
                }
            });
        }

        @Override
        public void onSetSuccess() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection == null || isError) {
                        return;
                    }

                    if (peerConnection.getRemoteDescription() == null) {
                        Log.d(TAG, "Local SDP set succesfully");
                        events.onOfferLocalDescription(localSdp);
                    } else if (peerConnection.getLocalDescription() != null) {
                        Log.d(TAG, "Local SDP set succesfully");
                        events.onAnswerLocalDescription(localSdp);
                    } else {
                        Log.d(TAG, "Remote SDP set succesfully");
                    }
                }
            });
        }

        @Override
        public void onCreateFailure(final String error) {
            Log.e(TAG, "Create SDP error: " + error);
        }

        @Override
        public void onSetFailure(final String error) {
            Log.e(TAG, "Set SDP error: " + error);
        }
    }
}
