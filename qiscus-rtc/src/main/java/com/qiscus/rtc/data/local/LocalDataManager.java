package com.qiscus.rtc.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qiscus.rtc.QiscusRTC;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajapulau on 9/15/17.
 */

public class LocalDataManager {
    private static LocalDataManager INSTANCE = new LocalDataManager();

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private LocalDataManager() {
        sharedPreferences = QiscusRTC.getAppInstance().getSharedPreferences("qiscusRtc.cfg", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static LocalDataManager getInstance() {
        return INSTANCE;
    }

    public List<String> getCallSessions() {
        String json = sharedPreferences.getString("call_sessions", "");
        List<String> callSessions = gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
        if (callSessions == null) {
            callSessions = new ArrayList<>();
        }
        return callSessions;
    }

    public void addCallSession(String callRoomId) {
        List<String> callSessions = getCallSessions();
        callSessions.add(callRoomId);
        int size = callSessions.size();
        if (size > 10) {
            int start = size - 10;
            callSessions = callSessions.subList(start, size);
        }
        sharedPreferences.edit().putString("call_sessions", gson.toJson(callSessions)).apply();
    }

    public boolean isContainCallSession(String callRoomId) {
        return getCallSessions().contains(callRoomId);
    }
}

