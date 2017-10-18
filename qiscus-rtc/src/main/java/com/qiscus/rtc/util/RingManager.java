package com.qiscus.rtc.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import com.qiscus.rtc.QiscusRTC;

/**
 * Created by rahardyan on 11/02/17.
 */

public class RingManager {
    private static RingManager ringManager;
    private AudioManager audioManager;
    private MediaPlayer phoneRingPlayer;
    private Context context;
    private Vibrator v;

    public static RingManager getInstance(Context context) {
        if (ringManager == null) {
            ringManager = new RingManager(context);
        }

        return ringManager;
    }

    private RingManager(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }


    public synchronized void play(QiscusRTC.CallType callType, QiscusRTC.CallAs callAs) {
        phoneRingPlayer = MediaPlayer.create(context, QiscusRTC.Call.getCallConfig().getRingingSound());
        if (callAs == QiscusRTC.CallAs.CALLER) {
            phoneRingPlayer = MediaPlayer.create(context, QiscusRTC.Call.getCallConfig().getWaitingSound());
        }

        phoneRingPlayer.setLooping(true);

        if (phoneRingPlayer != null && !phoneRingPlayer.isPlaying()) {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(callType == QiscusRTC.CallType.VIDEO);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, 0);
            switch (audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    try {
                        vibrate();
                        phoneRingPlayer.start();
                    } catch (Throwable t) {
                        Log.e("RingtoneManager", "Failed to start playing ring tone");
                    }

                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    //vibrate();
            }
        }
    }

    public synchronized void playRinging(QiscusRTC.CallType callType) {
        stop();
        phoneRingPlayer = MediaPlayer.create(context, QiscusRTC.Call.getCallConfig().getRingingSound());

        phoneRingPlayer.setLooping(true);

        if (phoneRingPlayer != null && !phoneRingPlayer.isPlaying()) {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(callType == QiscusRTC.CallType.VIDEO);
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, 0);
            switch (audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_NORMAL:
                    try {
                        phoneRingPlayer.start();
                    } catch (Throwable t) {
                        Log.e("RingtoneManager", "Failed to start playing ring tone");
                    }

                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    vibrate();
            }
        }
    }

    public void setSpeakerPhoneOn(boolean speakerOn) {
        audioManager.setSpeakerphoneOn(speakerOn);
    }

    public synchronized void stop() {
        if (phoneRingPlayer != null) {
            if (phoneRingPlayer.isPlaying()) {
                phoneRingPlayer.stop();
            }
            phoneRingPlayer.release();
            phoneRingPlayer = null;
        }
        stopVibrate();
    }

    private void vibrate() {
        long[] pattern = {500, 300, 500};
        if (v != null) {
            v.vibrate(pattern, 0);
        }
    }

    private void stopVibrate() {
        if (v != null) {
            v.cancel();
        }
    }

    private void calculateVolume() {
        int ringVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        int maxRingVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        int maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int calculatedVolume = ringVolume * maxMusicVolume / maxRingVolume;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, calculatedVolume, AudioManager.FLAG_PLAY_SOUND);
    }
}

