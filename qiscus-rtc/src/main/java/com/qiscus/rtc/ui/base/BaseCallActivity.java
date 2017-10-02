package com.qiscus.rtc.ui.base;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qiscus.rtc.ui.call.fragment.QiscusCallFragment;

/**
 * Created by rahardyan on 08/06/17.
 */

public abstract class BaseCallActivity extends BaseActivity implements QiscusCallFragment.CallStateListener {
    private static final String TAG = BaseCallActivity.class.getSimpleName();
    private QiscusCallFragment qiscusCallFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public int getLayout() {
        return getActivityLayout();
    }

    protected abstract int getActivityLayout();

    protected abstract int getFragmentContainerId();

    protected void createCallFragment(String roomId, String targetUserId) {
        qiscusCallFragment = QiscusCallFragment.newInstance(roomId, targetUserId);
        getSupportFragmentManager().beginTransaction().replace(getFragmentContainerId(), qiscusCallFragment).commit();
    }

    /**
     * end call session
     */
    protected void endCall() {
        if (qiscusCallFragment != null) {
            qiscusCallFragment.disconnect();
        }
    }

    /**
     * set speaker or earspeaker
     *
     * @param speakerOn - true speaker on, false earspeaker on
     */
    protected void setSpeaker(boolean speakerOn) {
        qiscusCallFragment.onSpeakerToggle(speakerOn);
    }

    /**
     * mute and unmute
     *
     * @param micOn - true = unmute, false = mute
     */
    protected void setMic(boolean micOn) {
        qiscusCallFragment.onMicToggle(micOn);
    }

    /**
     * set video enable or disable
     *
     * @param videoOn - true = video on, false = video of
     */
    protected void setVideo(boolean videoOn) {
        qiscusCallFragment.onVideoToggle(videoOn);
    }

    /**
     * set front camera or rear camera
     * @param frontCamera
     */
    protected void onCameraSwitch(boolean frontCamera) {
        qiscusCallFragment.onCameraSwitch(frontCamera);
    }


    @Override
    public void onCallConnected() {
        Log.d(TAG, "onCallConnected: call connected");
    }

    @Override
    public void onCallDisconnected() {
        Log.d(TAG, "onCallDisconnected: call disconnected");
    }
}

