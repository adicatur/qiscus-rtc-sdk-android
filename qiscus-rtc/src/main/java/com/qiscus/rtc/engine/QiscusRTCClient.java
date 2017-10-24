package com.qiscus.rtc.engine;

import android.content.Context;

import com.qiscus.rtc.engine.hub.HubListener;
import com.qiscus.rtc.engine.hub.HubSignal;
import com.qiscus.rtc.engine.hub.WSSignal;
import com.qiscus.rtc.engine.peer.PCClient;
import com.qiscus.rtc.engine.peer.PCFactory;
import com.qiscus.rtc.engine.util.LooperExecutor;
import com.qiscus.rtc.engine.util.QiscusRTCListener;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

/**
 * Created by fitra on 2/10/17.
 */

public class QiscusRTCClient implements HubSignal.SignalEvents, PCClient.PeerConnectionEvents {
    private static final String TAG = QiscusRTCClient.class.getSimpleName();

    private final Context context;

    private EglBase rootEglBase;
    private RendererCommon.ScalingType scalingType;
    private HubSignal hubSignal;
    private HubListener hubListener;
    private QiscusRTCListener rtcListener;
    private QiscusRTCViewRenderer localRender;
    private QiscusRTCViewLayout localRenderLayout;
    private QiscusRTCViewRenderer remoteRender;
    private QiscusRTCViewLayout remoteRenderLayout;
    private PCFactory pcFactory;
    private PCClient pcClient;
    private String clientId;
    private String roomId;
    private boolean initiator;
    private boolean videoEnabled;

    public QiscusRTCClient(Context context, QiscusRTCViewRenderer localRender, QiscusRTCViewRenderer remoteRender, QiscusRTCViewLayout localRenderLayout, QiscusRTCViewLayout remoteRenderLayout, HubListener hubListener, QiscusRTCListener rtcListener) {
        this.context = context;
        this.localRender = localRender;
        this.localRenderLayout = localRenderLayout;
        this.remoteRender = remoteRender;
        this.remoteRenderLayout = remoteRenderLayout;
        this.rtcListener = rtcListener;
        this.hubListener = hubListener;

        pcFactory = new PCFactory(context);
        rootEglBase = EglBase.create();

        this.localRender.init(rootEglBase.getEglBaseContext(), null);
        this.remoteRender.init(rootEglBase.getEglBaseContext(), null);
        this.localRender.setZOrderMediaOverlay(true);

        scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
    }

    public void start(String clientId, String roomId, boolean initiator, boolean videoEnabled, String target) {
        this.clientId = clientId;
        this.roomId = roomId;
        this.initiator = initiator;
        this.videoEnabled = videoEnabled;

        HubSignal.SignalParameters parameters = new HubSignal.SignalParameters(clientId, roomId, initiator, videoEnabled, target);
        hubSignal = new WSSignal(QiscusRTCClient.this, parameters, new LooperExecutor());
        hubSignal.connect();
        pcClient = PCClient.getInstance();
        pcClient.init(videoEnabled, pcFactory, context, QiscusRTCClient.this);
        pcClient.createPeerConnection(rootEglBase.getEglBaseContext(), localRender, remoteRender);
    }

    public void acceptCall() {
        hubSignal.acceptCall();
    }

    public void rejectCall() {
        hubSignal.rejectCall();
    }

    public void end() {
        if (pcClient != null) {
            pcClient.close();
            pcClient = null;
        }
        if (localRender != null) {
            localRender.release();
        }
        if (remoteRender != null) {
            remoteRender.release();
        }
        if (pcFactory != null) {
            if (pcClient == null) {
                pcFactory.dispose();
                pcFactory = null;
            }
        }
        if (hubSignal != null) {
            hubSignal.close();
            hubSignal = null;
        }
    }

    public void setVideoEnabled(boolean on) {
        if (pcClient != null) {
            pcClient.setVideoEnabled(on);
        }
    }

    public void switchCamera() {
        if (pcClient != null) {
            pcClient.switchCamera();
        }
    }

    public void setAudioEnabled(boolean on) {
        if (pcClient != null) {
            pcClient.setAudioEnabled(on);
        }
    }

    public void endCall() {
        hubSignal.endCall();
    }

    @Override
    public void onLoggedinToRoom() {
        hubSignal.ping();
    }

    @Override
    public void onPNReceived() {
        hubListener.onPNReceived();
    }

    @Override
    public void onCallAccepted() {
        hubListener.onCallAccepted();
        pcClient.createOffer();
    }

    @Override
    public void onCallRejected() {
        hubListener.onCallRejected();
    }

    @Override
    public void onCallCanceled() {
        hubListener.onCallCanceled();
    }

    @Override
    public void onSDPOffer(SessionDescription sdp) {
        rtcListener.onConnectingState(1);
        pcClient.setRemoteDescription(sdp);
        pcClient.createAnswer();
    }

    @Override
    public void onSDPAnswer(SessionDescription sdp) {
        rtcListener.onConnectingState(2);
        pcClient.setRemoteDescription(sdp);
    }

    @Override
    public void onICECandidate(IceCandidate candidate) {
        rtcListener.onConnectingState(2);
        pcClient.setRemoteCandidate(candidate);
    }

    @Override
    public void onClose() {
        rtcListener.onPeerDown();
    }

    @Override
    public void onError(String description) {
        rtcListener.onPeerError();
    }

    @Override
    public void onOfferLocalDescription(SessionDescription sdp) {
        rtcListener.onConnectingState(1);
        hubSignal.sendOffer(sdp);
    }


    @Override
    public void onAnswerLocalDescription(SessionDescription sdp) {
        rtcListener.onConnectingState(2);
        hubSignal.sendAnswer(sdp);
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        rtcListener.onConnectingState(3);
        hubSignal.trickleCandidate(candidate);
    }

    @Override
    public void onIceCompleted() {
        //
    }

    @Override
    public void onIceConnected() {
        rtcListener.onUpdateVideoView(true, scalingType);
    }

    @Override
    public void onIceDisconnected() {
        rtcListener.onPeerDown();
    }

    @Override
    public void onRemoveStream() {
        rtcListener.onPeerDown();
    }

    @Override
    public void onPeerConnectionClosed() {
        //listener.onPeerDown();
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        //
    }

    @Override
    public void onPeerConnectionError(String description) {
        rtcListener.onPeerError();
    }
}
