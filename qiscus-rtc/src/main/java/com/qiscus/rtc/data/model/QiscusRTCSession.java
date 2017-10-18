package com.qiscus.rtc.data.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by fitra on 2/10/17.
 */

public class QiscusRTCSession {
    private static final String PREF_NAME = "QiscusRTCSession";
    private static final String KEY_ISREGISTERED = "isRegietered";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    int PRIVATE_MODE = 0;

    public QiscusRTCSession(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void register(QiscusRTCAccount user) {
        editor.putBoolean(KEY_ISREGISTERED, true);
        editor.putString("username", user.getUsername());
        editor.putString("displayname", user.getDisplayName());
        editor.putString("avatarurl", user.getAvatarUrl());
        editor.apply();
    }

    public boolean isRegistered() {
        return sharedPreferences.getBoolean(KEY_ISREGISTERED, false);
    }

    public String getName() {
        return sharedPreferences.getString("username", "");
    }

    public String getDisplayName() {
        return sharedPreferences.getString("displayname", "");
    }

    public String getAvatarUrl() {
        return sharedPreferences.getString("avatarurl", "");
    }

    public void logout() {
        editor.clear().apply();
    }
}

