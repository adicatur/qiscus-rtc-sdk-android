package com.qiscus.rtc.data.config;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;

import com.qiscus.rtc.R;
import com.qiscus.rtc.data.model.QiscusRTCCall;

/**
 * Created by rahardyan on 07/06/17.
 * <p>
 * class for setting call configuration
 */

public class CallConfig {
    //ui ux configuration
    private int waitingSound = R.raw.phone_waiting;
    private int ringingSound = R.raw.phone_ring;
    private int backgroundDrawable = R.drawable.bg_call;
    private int backgroundColor;
    private int backgroundImage;
    private boolean isOngoingNotificationEnable = true;
    private int onGoingSmallIcon = R.drawable.ic_accept_voice_call;
    private int onGoingLargeIcon = R.drawable.ic_accept_voice_call;
    private int endCallButton = R.drawable.bt_end_call;
    private int acceptVoiceCallButton = R.drawable.bt_voice_call;
    private int acceptVideoCallButton = R.drawable.bt_video_call;
    private int speakerActiveIcon = R.drawable.ic_speaker;
    private int speakerInactiveIcon = R.drawable.ic_no_speaker;
    private int micActiveIcon = R.drawable.ic_mic;
    private int micInactiveIcon = R.drawable.ic_no_mic;
    private int videoActiveIcon = R.drawable.ic_video;
    private int videoInactiveIcon = R.drawable.ic_no_video;
    private int frontCameraIcon = R.drawable.ic_switch_camera_front;
    private int rearCameraIcon = R.drawable.ic_switch_camera_back;


    //ui call interaction listerner
    private CallButtonListener.OnEndCallClickListener onEndCallClickListener;
    private CallButtonListener.OnCancelCallClickListener onCancelCallClickListener;
    private CallButtonListener.OnRejectCallClickListener onRejectCallClickListener;
    private CallButtonListener.OnAcceptCallClickListener onAcceptCallClickListener;
    private CallPanelListener.OnSpeakerClickListener onSpeakerClickListener;
    private CallPanelListener.OnMicClickListener onMicClickListener;
    private CallPanelListener.OnVideoClickListener onVideoClickListener;
    private CallPanelListener.OnCameraClickListener onCameraClickListener;

    //call state listener
    private CallStateListener.OnCallDisconenctedListener onCallDisconenctedListener;
    private CallStateListener.OnCallConnectedListener onCallConnectedListener;

    /**
     * set speaker active icon
     *
     * @param speakerActiveIcon - drwawable res id
     */
    public CallConfig setSpeakerActiveIcon(@DrawableRes int speakerActiveIcon) {
        this.speakerActiveIcon = speakerActiveIcon;
        return this;
    }

    /**
     * get speaker active icon drawable id
     *
     * @return - drawable res id
     */
    public int getSpeakerActiveIcon() {
        return speakerActiveIcon;
    }

    /**
     * set speaker inactive icon
     *
     * @param speakerInactiveIcon - drwawable res id
     */
    public CallConfig setSpeakerInactiveIcon(@DrawableRes int speakerInactiveIcon) {
        this.speakerInactiveIcon = speakerInactiveIcon;
        return this;
    }

    /**
     * get speaker inactive icon drawable id
     *
     * @return - drawable res id
     */
    public int getSpeakerInactiveIcon() {
        return speakerInactiveIcon;
    }

    /**
     * set mic active icon
     *
     * @param micActiveIcon - drwawable res id
     */
    public CallConfig setMicActiveIcon(@DrawableRes int micActiveIcon) {
        this.micActiveIcon = micActiveIcon;
        return this;
    }

    /**
     * get mic active icon drawable id
     *
     * @return - drawable res id
     */
    public int getMicActiveIcon() {
        return micActiveIcon;
    }

    /**
     * set mic inactive icon
     *
     * @param micInactiveIcon - drwawable res id
     */
    public CallConfig setMicInactiveIcon(@DrawableRes int micInactiveIcon) {
        this.micInactiveIcon = micInactiveIcon;
        return this;
    }

    /**
     * get mic inactive icon drawable id
     *
     * @return - drawable res id
     */
    public int getMicInactiveIcon() {
        return micInactiveIcon;
    }

    /**
     * set video active icon
     *
     * @param videoActiveIcon - drwawable res id
     */
    public CallConfig setVideoActiveIcon(@DrawableRes int videoActiveIcon) {
        this.videoActiveIcon = videoActiveIcon;
        return this;
    }

    /**
     * get video active icon drawable id
     *
     * @return - drawable res id
     */
    public int getVideoActiveIcon() {
        return videoActiveIcon;
    }

    /**
     * set video inactive icon
     *
     * @param videoInactiveIcon - drwawable res id
     */
    public CallConfig setVideoInactiveIcon(@DrawableRes int videoInactiveIcon) {
        this.videoInactiveIcon = videoInactiveIcon;
        return this;
    }

    /**
     * get video inactive icon drawable id
     *
     * @return - drawable res id
     */
    public int getVideoInactiveIcon() {
        return videoInactiveIcon;
    }

    /**
     * set front camera icon
     *
     * @param frontCameraIcon - drwawable res id
     */
    public CallConfig setFrontCameraIcon(@DrawableRes int frontCameraIcon) {
        this.frontCameraIcon = frontCameraIcon;
        return this;
    }

    /**
     * get front camera icon drawable id
     *
     * @return - drawable res id
     */
    public int getFrontCameraIcon() {
        return frontCameraIcon;
    }

    /**
     * set rear camera icon
     *
     * @param rearCameraIcon - drwawable res id
     */
    public CallConfig setRearCameraIcon(@DrawableRes int rearCameraIcon) {
        this.rearCameraIcon = rearCameraIcon;
        return this;
    }

    /**
     * get rear camera icon drawable id
     *
     * @return - drawable res id
     */
    public int getRearCameraIcon() {
        return rearCameraIcon;
    }

    /**
     * set waiting sound
     *
     * @param waitingSound - sound raw id
     */
    public void setWaitingSound(@RawRes int waitingSound) {
        this.waitingSound = waitingSound;
    }

    /**
     * get wating sound
     *
     * @return - waiting sound raw id
     */
    @RawRes
    public int getWaitingSound() {
        return waitingSound;
    }

    /**
     * set ringing sound
     *
     * @param ringingSound - ringing sound raw id
     */
    public CallConfig setRingingSound(@RawRes int ringingSound) {
        this.ringingSound = ringingSound;
        return this;
    }

    /**
     * get ringing sound
     *
     * @return - ringing sound raw id
     */
    @RawRes
    public int getRingingSound() {
        return ringingSound;
    }

    /**
     * set background drawble
     *
     * @param backgroundDrawable - call background drawable id
     */
    public CallConfig setBackgroundDrawble(@DrawableRes int backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
        return this;
    }

    /**
     * get background drawable
     *
     * @return - background drawable id
     */
    @DrawableRes
    public int getBackgroundDrawable() {
        return backgroundDrawable;
    }

    /**
     * set background color
     *
     * @param backgroundColor - call background color id
     */
    public CallConfig setBackgroundColor(@ColorRes int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * get background color
     *
     * @return - background color id
     */
    @ColorRes
    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * set background image
     *
     * @param backgroundImage - call background image id
     */
    public void setBackgroundImage(@DrawableRes int backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    /**
     * get background image
     *
     * @return - background image id
     */
    @DrawableRes
    public int getBackgroundImage() {
        return backgroundImage;
    }

    /**
     * set ongoing call notification enable or disable
     *
     * @param isEnabled - true ongoing call notification will be created, false ongoing call notification will be removed
     */
    public CallConfig setOngoingNotificationEnable(boolean isEnabled) {
        this.isOngoingNotificationEnable = isEnabled;
        return this;
    }

    /**
     * get ongoing notification availablity
     *
     * @return - true ongoing call notification will be created, false ongoing call notification will be removed
     */
    public boolean isOngoingNotificationEnable() {
        return isOngoingNotificationEnable;
    }

    /**
     * set small ongoig notif icon
     *
     * @param smallOngoingIcon - icon drawable id
     */
    public void setSmallOngoingNotifIcon(@DrawableRes int smallOngoingIcon) {
        this.onGoingSmallIcon = smallOngoingIcon;
    }

    /**
     * get small ongoing notif icon
     *
     * @return - icon drawable id
     */
    public int getSmallOngoingNotifIcon() {
        return onGoingSmallIcon;
    }

    /**
     * set large ongoing notif icon
     *
     * @param largeOngoingIcon - icon drawable id
     */
    public CallConfig setLargeOngoingNotifIcon(@DrawableRes int largeOngoingIcon) {
        this.onGoingLargeIcon = largeOngoingIcon;
        return this;
    }

    /**
     * get large ongoing notif icon
     *
     * @return - icon drawable id
     */
    public int getLargeOngoingNotifIcon() {
        return onGoingLargeIcon;
    }

    /**
     * set end call button drawable id
     * - create drawable item with selector icon
     *
     * @param endCallButton - drawable res id
     */
    public CallConfig setEndCallButton(@DrawableRes int endCallButton) {
        this.endCallButton = endCallButton;
        return this;
    }

    /**
     * get end call button drawable id
     *
     * @return - end call button drawable id
     */
    @DrawableRes
    public int getEndCallButton() {
        return endCallButton;
    }

    /**
     * set accept voice call button
     * - create drawable item with selector icon
     *
     * @param voiceCallButton - drawable res id
     */
    public CallConfig setAcceptVoiceCallButton(@DrawableRes int voiceCallButton) {
        this.acceptVoiceCallButton = voiceCallButton;
        return this;
    }

    /**
     * get accept voice call button
     *
     * @return - accept voice call button drawable id
     */
    @DrawableRes
    public int getAcceptVoiceCallButton() {
        return acceptVoiceCallButton;
    }

    /**
     * set accept video call button
     * - create drawable item with selector icon
     *
     * @param videoCallButton - drawable res id
     */
    public CallConfig setAcceptVideoCallButton(@DrawableRes int videoCallButton) {
        this.acceptVideoCallButton = videoCallButton;
        return this;
    }

    /**
     * get accept video call button
     *
     * @return - accept video call button drawable id
     */
    @DrawableRes
    public int getAcceptVideoCallButton() {
        return acceptVideoCallButton;
    }

    /**
     * listen on end call click event
     *
     * @param onEndCallListener - listener
     */
    public CallConfig setOnEndCallListener(@NonNull CallButtonListener.OnEndCallClickListener onEndCallListener) {
        this.onEndCallClickListener = onEndCallListener;
        return this;
    }

    /**
     * get end call listener
     *
     * @return - listener
     */
    @Nullable
    public CallButtonListener.OnEndCallClickListener getOnEndCallClickListener() {
        return onEndCallClickListener;
    }

    /**
     * listen on cancel call click event
     *
     * @param onCancelCallClickListener - listener
     */
    public CallConfig setOnCancelCallClickListener(@NonNull CallButtonListener.OnCancelCallClickListener onCancelCallClickListener) {
        this.onCancelCallClickListener = onCancelCallClickListener;
        return this;
    }

    /**
     * get cancel call listener
     *
     * @return - listener
     */
    @Nullable
    public CallButtonListener.OnCancelCallClickListener getOnCancelCallClickListener() {
        return onCancelCallClickListener;
    }

    /**
     * listen on reject call click event
     *
     * @param onRejectCallClickListener - listener
     */
    public CallConfig setOnRejectCallClickListener(@NonNull CallButtonListener.OnRejectCallClickListener onRejectCallClickListener) {
        this.onRejectCallClickListener = onRejectCallClickListener;
        return this;
    }

    /**
     * get on reject call listener
     *
     * @return - listener
     */
    @Nullable
    public CallButtonListener.OnRejectCallClickListener getOnRejectCallClickListener() {
        return onRejectCallClickListener;
    }

    /**
     * listen on accept call click event
     *
     * @param onAcceptCallClickListener - listener
     */
    public CallConfig setOnAcceptCallClickListener(@NonNull CallButtonListener.OnAcceptCallClickListener onAcceptCallClickListener) {
        this.onAcceptCallClickListener = onAcceptCallClickListener;
        return this;
    }

    /**
     * get on accept call listener
     *
     * @return - listener
     */
    @Nullable
    public CallButtonListener.OnAcceptCallClickListener getOnAcceptCallClickListener() {
        return onAcceptCallClickListener;
    }

    /**
     * set on call connected event
     *
     * @param onCallConnectedListener - listener
     */
    public CallConfig setOnCallConnectedListener(@NonNull CallStateListener.OnCallConnectedListener onCallConnectedListener) {
        this.onCallConnectedListener = onCallConnectedListener;
        return this;
    }

    /**
     * get on call connected listener
     *
     * @return - listener
     */
    @Nullable
    public CallStateListener.OnCallConnectedListener getOnCallConnectedListener() {
        return onCallConnectedListener;
    }

    /**
     * set on call disconnected event
     *
     * @param onCallDisconenctedListener - listener
     */
    public CallConfig setOnCallDisconenctedListener(@NonNull CallStateListener.OnCallDisconenctedListener onCallDisconenctedListener) {
        this.onCallDisconenctedListener = onCallDisconenctedListener;
        return this;
    }

    /**
     * get on call disconnected listener
     *
     * @return - listener
     */
    @Nullable
    public CallStateListener.OnCallDisconenctedListener getOnCallDisconenctedListener() {
        return onCallDisconenctedListener;
    }

    /**
     * set on speaker click event
     *
     * @param onSpeakerClickListener - listener
     */
    public CallConfig setOnSpeakerClickListener(@NonNull CallPanelListener.OnSpeakerClickListener onSpeakerClickListener) {
        this.onSpeakerClickListener = onSpeakerClickListener;
        return this;
    }

    /**
     * get on speaker click listener
     *
     * @return - listener
     */
    @Nullable
    public CallPanelListener.OnSpeakerClickListener getOnSpeakerClickListener() {
        return onSpeakerClickListener;
    }

    /**
     * set on mic click event
     *
     * @param onMicClickListener - listener
     */
    public CallConfig setOnMicClickListener(@NonNull CallPanelListener.OnMicClickListener onMicClickListener) {
        this.onMicClickListener = onMicClickListener;
        return this;
    }

    /**
     * get on mic click listener
     *
     * @return - listener
     */
    @Nullable
    public CallPanelListener.OnMicClickListener getOnMicClickListener() {
        return onMicClickListener;
    }

    /**
     * set on video click event
     *
     * @param onVideoClickListener - listener
     */
    public CallConfig setOnVideoClickListener(@NonNull CallPanelListener.OnVideoClickListener onVideoClickListener) {
        this.onVideoClickListener = onVideoClickListener;
        return this;
    }

    /**
     * get on video click listener
     *
     * @return - listener
     */
    @NonNull
    public CallPanelListener.OnVideoClickListener getOnVideoClickListener() {
        return onVideoClickListener;
    }

    /**
     * set on camera click event
     *
     * @param onCameraClickListener - listener
     */
    public CallConfig setOnCameraClickListener(@NonNull CallPanelListener.OnCameraClickListener onCameraClickListener) {
        this.onCameraClickListener = onCameraClickListener;
        return this;
    }

    /**
     * get on camera click listener
     *
     * @return - listener
     */
    @Nullable
    public CallPanelListener.OnCameraClickListener getOnCameraClickListener() {
        return onCameraClickListener;
    }

    /**
     * interface call panel
     */
    public interface CallPanelListener {
        interface OnSpeakerClickListener {
            void onClick(boolean isSpeakerOn);
        }

        interface OnMicClickListener {
            void onClick(boolean isMicOn);
        }

        interface OnVideoClickListener {
            void onClick(boolean isVideoOn);
        }

        interface OnCameraClickListener {
            void onClick(boolean isFrontCamera);
        }
    }

    /**
     * interface ui call interaction
     */
    public interface CallButtonListener {
        interface OnEndCallClickListener {
            void onClick(QiscusRTCCall callData, long callDurationInMillis);
        }

        interface OnCancelCallClickListener {
            void onClick(QiscusRTCCall callData);
        }

        interface OnRejectCallClickListener {
            void onClick(QiscusRTCCall callData);
        }

        interface OnAcceptCallClickListener {
            void onClick(QiscusRTCCall callData);
        }
    }

    /**
     * interface call state
     */
    public interface CallStateListener {
        interface OnCallConnectedListener {
            void onConnect();
        }

        interface OnCallDisconenctedListener {
            void onDisconnect(long callDurationInMillis);
        }
    }
}

