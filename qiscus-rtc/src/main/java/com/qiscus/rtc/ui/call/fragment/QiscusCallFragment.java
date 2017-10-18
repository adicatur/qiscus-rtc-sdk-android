package com.qiscus.rtc.ui.call.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qiscus.rtc.QiscusRTC;
import com.qiscus.rtc.R;
import com.qiscus.rtc.data.model.QiscusRTCCall;
import com.qiscus.rtc.engine.QiscusRTCClient;
import com.qiscus.rtc.engine.QiscusRTCRendererCommon;
import com.qiscus.rtc.engine.QiscusRTCViewLayout;
import com.qiscus.rtc.engine.QiscusRTCViewRenderer;
import com.qiscus.rtc.engine.hub.HubListener;
import com.qiscus.rtc.engine.util.QiscusRTCListener;
import com.qiscus.rtc.util.RingManager;

import static com.qiscus.rtc.data.config.Constants.CALL_DATA;

/**
 * Created by rahardyan on 08/06/17.
 */

public class QiscusCallFragment extends Fragment implements HubListener, QiscusRTCListener {
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 48;
    private static final int LOCAL_Y_CONNECTED_BOTTOM = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;

    // call data
    private QiscusRTCCall callData;
    private QiscusRTCClient rtcClient;
    private QiscusRTCViewLayout localRenderLayout;
    private QiscusRTCViewLayout remoteRenderLayout;
    private QiscusRTCViewRenderer localRender;
    private QiscusRTCViewRenderer remoteRender;

    private CallStateListener callStateListener;

    public static QiscusCallFragment newInstance(String roomId, String targetUserId) {
        Bundle args = new Bundle();
        QiscusRTCCall callData = new QiscusRTCCall();
        callData.setRoomId(roomId);
        callData.setCallType(QiscusRTC.Call.getInstance().getCallType());
        callData.setCallerDisplayName(QiscusRTC.Call.getInstance().getUsername());
        callData.setCalleeUsername(targetUserId);
        args.putParcelable(CALL_DATA, callData);

        QiscusCallFragment fragment = new QiscusCallFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callStateListener = (CallStateListener) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_call, container, false);
        initView(view);
        callData = getArguments().getParcelable(CALL_DATA);
        String id = callData.getCallAs() == QiscusRTC.CallAs.CALLER ? callData.getCallerUsername() : callData.getCalleeUsername();
        String target = callData.getCallAs() == QiscusRTC.CallAs.CALLER ? callData.getCalleeUsername() : callData.getCallerUsername();
        rtcClient.start(id, callData.getRoomId(), callData.getCallAs() == QiscusRTC.CallAs.CALLER, callData.getCallType() == QiscusRTC.CallType.VIDEO, target);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private void initView(View view) {
        localRender = (QiscusRTCViewRenderer) view.findViewById(R.id.local_video_view);
        remoteRender = (QiscusRTCViewRenderer) view.findViewById(R.id.remote_video_view);
        localRenderLayout = (QiscusRTCViewLayout) view.findViewById(R.id.local_video_layout);
        remoteRenderLayout = (QiscusRTCViewLayout) view.findViewById(R.id.remote_video_layout);
        rtcClient = new QiscusRTCClient(getContext(), localRender, remoteRender, localRenderLayout, remoteRenderLayout, this, this);
    }

    public void onSpeakerToggle(boolean speakerOn) {
        if (QiscusRTC.Call.getCallConfig().getOnSpeakerClickListener() != null) {
            QiscusRTC.Call.getCallConfig().getOnSpeakerClickListener().onClick(speakerOn);
        }
        RingManager.getInstance(getContext()).setSpeakerPhoneOn(speakerOn);
    }

    public void onMicToggle(boolean micOn) {
        if (QiscusRTC.Call.getCallConfig().getOnMicClickListener() != null) {
            QiscusRTC.Call.getCallConfig().getOnMicClickListener().onClick(micOn);
        }
        rtcClient.setAudioEnabled(micOn);
    }

    public void onVideoToggle(boolean videoOn) {
        if (QiscusRTC.Call.getCallConfig().getOnVideoClickListener() != null) {
            QiscusRTC.Call.getCallConfig().getOnVideoClickListener().onClick(videoOn);
        }
        rtcClient.setVideoEnabled(videoOn);
    }

    public void onCameraSwitch(boolean frontCamera) {
        if (QiscusRTC.Call.getCallConfig().getOnCameraClickListener() != null) {
            QiscusRTC.Call.getCallConfig().getOnCameraClickListener().onClick(frontCamera);
        }
        rtcClient.switchCamera();
        try {
            localRender.setMirror(frontCamera);
            localRender.requestLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        callStateListener.onCallDisconnected();
        if (rtcClient != null) {
            try {
                rtcClient.end();
                rtcClient = null;
                localRender = null;
                remoteRender = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!getActivity().isFinishing() && getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onPNReceived() {
        //
    }

    @Override
    public void onCallAccepted() {
        //
    }

    @Override
    public void onCallRejected() {
        //
    }

    @Override
    public void onCallCanceled() {
        //
    }

    @Override
    public void onCallEnded() {
        //
    }

    @Override
    public void onUpdateVideoView(final Boolean iceConnected, final QiscusRTCRendererCommon.ScalingType scalingType) {
        callStateListener.onCallConnected();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (QiscusRTC.Call.getCallConfig().getOnCallConnectedListener() != null) {
                    QiscusRTC.Call.getCallConfig().getOnCallConnectedListener().onConnect();
                }
                remoteRenderLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
                if (remoteRender != null) {
                    remoteRender.setScalingType(scalingType);
                }
                remoteRender.setMirror(false);

                if (iceConnected) {
                    localRenderLayout.setPosition(
                            LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED, LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED);
                    localRender.setScalingType(QiscusRTCRendererCommon.ScalingType.SCALE_ASPECT_FIT);
                } else {
                    localRenderLayout.setPosition(
                            LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING);
                    localRender.setScalingType(scalingType);
                }
                localRender.setMirror(false);

                localRender.requestLayout();
                remoteRender.requestLayout();

            }
        });
    }

    @Override
    public void onPeerDown() {
        disconnect();
    }

    @Override
    public void onPeerError() {

    }

    public interface CallStateListener {
        void onCallConnected();
        void onCallDisconnected();
    }
}


