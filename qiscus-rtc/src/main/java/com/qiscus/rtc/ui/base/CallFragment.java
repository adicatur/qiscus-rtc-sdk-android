package com.qiscus.rtc.ui.base;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qiscus.rtc.QiscusRTC;
import com.qiscus.rtc.R;
import com.qiscus.rtc.data.model.QiscusRTCCall;
import com.qiscus.rtc.ui.call.fragment.VideoCallFragment;
import com.qiscus.rtc.ui.call.fragment.VoiceCallFragment;
import com.qiscus.rtc.ui.widget.RoundedImageView;
import com.squareup.picasso.Picasso;

import static com.qiscus.rtc.data.config.Constants.CALL_DATA;

/**
 * Created by rahardyan on 06/06/17.
 */

public abstract class CallFragment extends Fragment {
    protected boolean frontCamera = true;
    protected boolean isVideoOn = true;
    protected OnCallListener onCallListener;
    private QiscusRTCCall callData;

    @Nullable
    private RelativeLayout headerContainer;
    private LinearLayout panelBtnContainer;
    private TextView tvCallerName;
    private ImageView btnEndCall, btnMic, btnSpeaker, backgroundImage;
    private View background;
    private RoundedImageView calleeAvatar;
    private Chronometer callDuration;
    private boolean speakerOn;
    private boolean micOn = true;
    private boolean isPanelHidden;
    private long callDurationMillis;

    public static CallFragment newInstance(QiscusRTCCall callData) {
        Bundle args = new Bundle();
        args.putParcelable(CALL_DATA, callData);
        CallFragment fragment = new VideoCallFragment();

        if (callData.getCallType() == QiscusRTC.CallType.VOICE) {
            fragment = new VoiceCallFragment();
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArgumentData();
        onCallListener = (OnCallListener) getActivity();
    }

    private void parseArgumentData() {
        callData = getArguments().getParcelable(CALL_DATA);
        speakerOn = callData.getCallType() == QiscusRTC.CallType.VIDEO;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        initView(view);
        onParentViewCreated(view);

        return view;
    }

    protected abstract void onParentViewCreated(View view);

    private void initView(View view) {
        btnEndCall = (ImageView) view.findViewById(R.id.button_end_call);
        btnSpeaker = (ImageView) view.findViewById(R.id.button_speaker);
        btnMic = (ImageView) view.findViewById(R.id.button_mic);
        callDuration = (Chronometer) view.findViewById(R.id.call_duration);
        tvCallerName = (TextView) view.findViewById(R.id.caller_name);
        background = view.findViewById(R.id.background);
        backgroundImage = (ImageView) view.findViewById(R.id.image_background);
        panelBtnContainer = (LinearLayout) view.findViewById(R.id.panel_btn_container);
        calleeAvatar = (RoundedImageView) view.findViewById(R.id.caller_avatar);

        if (callData.getCallType() == QiscusRTC.CallType.VIDEO) {
            headerContainer = (RelativeLayout) view.findViewById(R.id.header_container);
        }

        setConfigToView();
        startTime();

        if (callData.getCallType() == QiscusRTC.CallType.VOICE) {
            speakerOn = false;
        }

        tvCallerName.setText(callData.getCalleeDisplayName());
        if (callData.getCallAs() == QiscusRTC.CallAs.CALLER) {
            String displayCalleeAvatar = (callData.getCalleeAvatar() == null ||
                    callData.getCalleeAvatar().isEmpty() ||
                    callData.getCalleeAvatar().equals("null")) ? "Anonymous" : callData.getCalleeAvatar();
            tvCallerName.setText(callData.getCalleeDisplayName());
            Picasso.with(getContext()).load(displayCalleeAvatar).placeholder(R.drawable.profile_account).into(calleeAvatar);
        } else {
            String displayCallerAvatar = (callData.getCalleeAvatar() == null || callData.getCallerAvatar().isEmpty()) ? "Anonymous" : callData.getCallerAvatar();
            tvCallerName.setText(callData.getCallerDisplayName());
            Picasso.with(getContext()).load(displayCallerAvatar).placeholder(R.drawable.profile_account).into(calleeAvatar);
        }

        final int speakerActiveIcon = QiscusRTC.Call.getCallConfig().getSpeakerActiveIcon();
        final int speakerInactiveIcon = QiscusRTC.Call.getCallConfig().getSpeakerInactiveIcon();
        btnSpeaker.setImageResource(callData.getCallType() == QiscusRTC.CallType.VIDEO ? speakerActiveIcon : speakerInactiveIcon);
        btnSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakerOn = !speakerOn;
                btnSpeaker.setImageResource(speakerOn ? speakerActiveIcon : speakerInactiveIcon);
                onCallListener.onSpeakerToggle(speakerOn);
            }
        });

        final int micActiveIcon = QiscusRTC.Call.getCallConfig().getMicActiveIcon();
        final int micInactiveIcon = QiscusRTC.Call.getCallConfig().getMicInactiveIcon();
        btnMic.setImageResource(micActiveIcon);
        btnMic.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                micOn = !micOn;
                btnMic.setImageResource(micOn ? micActiveIcon : micInactiveIcon);
                onCallListener.onMicToggle(micOn);
            }
        });

        final int btnEndCallDrawable = QiscusRTC.Call.getCallConfig().getEndCallButton();
        btnEndCall.setImageResource(btnEndCallDrawable);
        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
                onCallListener.onEndCall(callDurationMillis);
            }
        });
    }

    public void hidePanelButton() {
        if (isPanelHidden) {
            slideUpView(panelBtnContainer);
        } else {
            slideDownView(panelBtnContainer);
        }
        isPanelHidden = !isPanelHidden;
    }

    public void startTime() {
        callDuration.setBase(SystemClock.elapsedRealtime());
        callDuration.start();
    }

    public long getCallDurationMillis() {
        return callDurationMillis;
    }

    private void slideDownView(final ViewGroup viewGroup) {
        Animation slideDownAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fly_out_down);
        slideDownAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewGroup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        viewGroup.startAnimation(slideDownAnim);
    }

    private void slideUpView(final ViewGroup viewGroup) {
        Animation slideUpAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fly_in_up);
        slideUpAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                viewGroup.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        viewGroup.startAnimation(slideUpAnim);
    }

    private void stopTimer() {
        callDurationMillis = SystemClock.elapsedRealtime() - callDuration.getBase();
        callDuration.stop();
    }

    private void setConfigToView() {
        if (QiscusRTC.Call.getCallConfig().getBackgroundColor() == 0) {
            background.setBackground(getResources().getDrawable(QiscusRTC.Call.getCallConfig().getBackgroundDrawable()));
        } else {
            background.setBackgroundColor(getResources().getColor(QiscusRTC.Call.getCallConfig().getBackgroundColor()));
        }
    }

    public boolean isFrontCamera() {
        return frontCamera;
    }

    protected abstract int getLayout();

    public interface OnCallListener {
        void onSpeakerToggle(boolean speakerOn);

        void onMicToggle(boolean micOn);

        void onVideoToggle(boolean videoOn);

        void onCameraSwitch(boolean frontCamera);

        void onEndCall(long callDurationMillis);
    }
}
