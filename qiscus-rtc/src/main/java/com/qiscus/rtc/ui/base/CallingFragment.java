package com.qiscus.rtc.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.rtc.QiscusRTC;
import com.qiscus.rtc.R;
import com.qiscus.rtc.data.model.QiscusRTCCall;
import com.qiscus.rtc.ui.call.fragment.VideoCallingFragment;
import com.qiscus.rtc.ui.call.fragment.VoiceCallingFragment;
import com.qiscus.rtc.ui.widget.RoundedImageView;
import com.qiscus.rtc.util.RingManager;
import com.squareup.picasso.Picasso;

import static com.qiscus.rtc.data.config.Constants.CALL_DATA;

/**
 * Created by rahardyan on 06/06/17.
 */

public abstract class CallingFragment extends Fragment {
    private ImageView btnEndCall, btnAcceptCall, backgroundImage;
    private View background;
    private OnCallingListener onCallingListener;
    private RingManager ringManager;
    private QiscusRTCCall callData;
    private RoundedImageView calleeAvatar;
    private TextView tvCallerName, tvCallState;

    public static CallingFragment newInstance(QiscusRTCCall callData) {
        Bundle args = new Bundle();
        args.putParcelable(CALL_DATA, callData);
        CallingFragment fragment = new VideoCallingFragment();

        if (callData.getCallType() == QiscusRTC.CallType.VOICE) {
            fragment = new VoiceCallingFragment();
        }

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseBundleData();
        onCallingListener = (OnCallingListener) getActivity();
        ringManager = RingManager.getInstance(getContext());
        ringManager.play(callData.getCallType(), callData.getCallAs());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        initView(view);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ringManager.stop();
    }

    private void parseBundleData() {
        callData = getArguments().getParcelable(CALL_DATA);
    }

    private void initView(View view) {
        btnAcceptCall = (ImageView) view.findViewById(R.id.button_accept_call);
        btnEndCall = (ImageView) view.findViewById(R.id.button_end_call);
        tvCallerName = (TextView) view.findViewById(R.id.caller_name);
        tvCallState = (TextView) view.findViewById(R.id.call_state);
        background = view.findViewById(R.id.background);
        backgroundImage = (ImageView) view.findViewById(R.id.image_background);
        calleeAvatar = (RoundedImageView) view.findViewById(R.id.caller_avatar);

        setConfigToView();

        if (callData.getCallAs() == QiscusRTC.CallAs.CALLER) {
            String displayCalleeAvatar = (callData.getCalleeAvatar() == null || callData.getCalleeAvatar().isEmpty() || callData.getCalleeAvatar().equals("null")) ? "Anonymous" : callData.getCalleeAvatar();
            tvCallerName.setText(callData.getCalleeDisplayName());
            Picasso.with(getContext()).load(displayCalleeAvatar).placeholder(R.drawable.profile_account).into(calleeAvatar);
            btnAcceptCall.setVisibility(View.GONE);
            tvCallState.setText(R.string.calling_state);
        } else {
            String displayCallerAvatar = (callData.getCalleeAvatar() == null || callData.getCallerAvatar().isEmpty()) ? "Anonymous" : callData.getCallerAvatar();
            tvCallerName.setText(callData.getCallerDisplayName());
            Picasso.with(getContext()).load(displayCallerAvatar).placeholder(R.drawable.profile_account).into(calleeAvatar);
            btnAcceptCall.setVisibility(View.VISIBLE);
            tvCallState.setText(R.string.incoming_state);
        }

        btnAcceptCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QiscusRTC.Call.getCallConfig().getOnAcceptCallClickListener() != null) {
                    QiscusRTC.Call.getCallConfig().getOnAcceptCallClickListener().onClick(callData);
                }
                onCallingListener.onCallingAccepted();
                ringManager.stop();
            }
        });

        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callData.getCallAs() == QiscusRTC.CallAs.CALLER) {
                    if (QiscusRTC.Call.getCallConfig().getOnCancelCallClickListener() != null) {
                        QiscusRTC.Call.getCallConfig().getOnCancelCallClickListener().onClick(callData);
                    }
                    onCallingListener.onCallingCanceled();
                } else {
                    if (QiscusRTC.Call.getCallConfig().getOnRejectCallClickListener() != null) {
                        QiscusRTC.Call.getCallConfig().getOnRejectCallClickListener().onClick(callData);
                    }
                    onCallingListener.onCallingRejected();
                }
                ringManager.stop();
            }
        });
    }

    public void setCalleeAvatarAndDisplayName(final String displayName, final String avatarUrl) {
        tvCallerName.setText(displayName);
        Picasso.with(getContext()).load(avatarUrl).into(calleeAvatar);
    }

    public void setTvCallState(final String callState) {
        tvCallState.setText(callState);
        ringManager.stop();
        ringManager.play(QiscusRTC.CallType.VOICE, QiscusRTC.CallAs.CALLEE);
    }

    private void setConfigToView() {
        //configure accept call button
        int btnAcceptDrawable = QiscusRTC.Call.getCallConfig().getAcceptVoiceCallButton();
        if (callData.getCallType() == QiscusRTC.CallType.VIDEO) {
            btnAcceptDrawable = QiscusRTC.Call.getCallConfig().getAcceptVideoCallButton();
        }

        //configure end call button
        btnAcceptCall.setImageResource(btnAcceptDrawable);
        int btnEndCallDrawable = QiscusRTC.Call.getCallConfig().getEndCallButton();
        btnEndCall.setImageResource(btnEndCallDrawable);

        //configure background cal;
        if (QiscusRTC.Call.getCallConfig().getBackgroundColor() == 0) {
            background.setBackground(getResources().getDrawable(QiscusRTC.Call.getCallConfig().getBackgroundDrawable()));
        } else {
            background.setBackgroundColor(getResources().getColor(QiscusRTC.Call.getCallConfig().getBackgroundColor()));
        }
    }

    public interface OnCallingListener {
        void onCallingAccepted();

        void onCallingRejected();

        void onCallingCanceled();
    }

    protected abstract int getLayout();
}

