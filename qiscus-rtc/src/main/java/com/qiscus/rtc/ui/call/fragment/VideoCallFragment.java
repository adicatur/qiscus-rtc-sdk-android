package com.qiscus.rtc.ui.call.fragment;

import android.view.View;
import android.widget.ImageView;

import com.qiscus.rtc.QiscusRTC;
import com.qiscus.rtc.R;
import com.qiscus.rtc.ui.base.CallFragment;

/**
 * Created by rahardyan on 06/06/17.
 */

public class VideoCallFragment extends CallFragment {
    private ImageView btnVideo;
    private ImageView btnSwithCamera;

    @Override
    protected int getLayout() {
        return R.layout.fragment_video_call;
    }


    @Override
    protected void onParentViewCreated(View view) {
        initView(view);
    }

    private void initView(View view) {
        btnSwithCamera = (ImageView) view.findViewById(R.id.button_switch_camera);
        btnVideo = (ImageView) view.findViewById(R.id.button_video);

        final int cameraFrontIcon = QiscusRTC.Call.getCallConfig().getFrontCameraIcon();
        final int camreaRearIcon = QiscusRTC.Call.getCallConfig().getRearCameraIcon();
        btnSwithCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frontCamera = !frontCamera;
                btnSwithCamera.setImageResource(frontCamera ? camreaRearIcon : cameraFrontIcon);
                onCallListener.onCameraSwitch(frontCamera);
            }
        });

        final int videoActiveIcon = QiscusRTC.Call.getCallConfig().getVideoActiveIcon();
        final int videoInactiveIcon = QiscusRTC.Call.getCallConfig().getVideoInactiveIcon();
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideoOn = !isVideoOn;
                btnVideo.setImageResource(isVideoOn ? videoActiveIcon : videoInactiveIcon);
                btnSwithCamera.setVisibility(isVideoOn ? View.VISIBLE : View.GONE);
                onCallListener.onVideoToggle(isVideoOn);
            }
        });
    }
}

