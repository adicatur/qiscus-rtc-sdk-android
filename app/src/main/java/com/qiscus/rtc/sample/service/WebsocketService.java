package com.qiscus.rtc.sample.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fitraditya.androidwebsocket.WebsocketClient;
import com.qiscus.rtc.QiscusRTC;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashSet;

/**
 * Created by fitra on 04/10/17.
 */

public class WebsocketService extends Service implements WebsocketClient.WebsocketListener {
    public static final String ACTION_PING = "PN.ACTION_PING";
    public static final String ACTION_CONNECT = "PN.ACTION_CONNECT";
    public static final String ACTION_DISCONNECT = "PN.ACTION_DISCONNECT";

    private final IBinder iBinder = new Binder();
    private static WebsocketClient websocketClient;
    private ServiceListener serviceListener;
    private Handler handler;
    private boolean disconnect = false;

    public static Intent startIntent(Context context){
        Intent i = new Intent(context, WebsocketService.class);
        i.setAction(ACTION_CONNECT);
        return i;
    }

    public static Intent pingIntent(Context context){
        Intent i = new Intent(context, WebsocketService.class);
        i.setAction(ACTION_PING);
        return i;
    }

    public static Intent closeIntent(Context context){
        Intent i = new Intent(context, WebsocketService.class);
        i.setAction(ACTION_DISCONNECT);
        return i;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Nullable
    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        Log.d("Sample PN", "Creating service " + this.toString());
    }

    @Nullable
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Sample PN", "Destroying Service " + this.toString());
        if (websocketClient != null && websocketClient.isConnected()) {
            websocketClient.disconnect();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerManager.WakeLock wakelock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sample PN Service");
        wakelock.acquire();

        Log.i("Sample PN", "Websocket service start command");
        if (intent != null) {
            Log.i("Sample PN", "Intent: " + intent.toUri(0));
        }

        disconnect = false;
        if (websocketClient == null) {
            PowerManager.WakeLock clientlock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EECS780");
            websocketClient = new WebsocketClient(URI.create("wss://rtc.qiscus.com/signal/pn"), this, null, clientlock);
        }

        if (!websocketClient.isConnected()) {
            websocketClient.connect();
        }

        if (intent != null) {
            if (ACTION_PING.equals(intent.getAction())) {
                if (websocketClient.isConnected()) {
                    websocketClient.send("{\"action\":\"ping\"}");
                }
            } else if (ACTION_DISCONNECT.equals(intent.getAction())){
                disconnect = true;
                if (websocketClient.isConnected()) {
                    websocketClient.disconnect();
                }
            }
        }

        if (intent == null || !intent.getAction().equals(ACTION_DISCONNECT)) {
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, WebsocketService.pingIntent(this), PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent == null){
                pendingIntent = PendingIntent.getService(this, 0, WebsocketService.pingIntent(this), PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
            }
        }

        wakelock.release();
        return START_STICKY;
    }

    public class Binder extends android.os.Binder{
        public WebsocketService getService() {
            return WebsocketService.this;
        }
    }

    public synchronized void setListener(ServiceListener listener){
        serviceListener = listener;
    }

    public synchronized boolean isConnected() {
        return websocketClient != null && websocketClient.isConnected();
    }

    @Override
    public void onConnect() {
        Log.d("Sample PN", "Connected to websocket server");

        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        try {
            data.put("username", QiscusRTC.getUser());
            data.put("force", true);
            jsonObject.put("request", "register");
            jsonObject.put("data", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //if (websocketClient.isConnected()) {
            websocketClient.send(jsonObject.toString());
        //}
    }

    @Override
    public void onMessage(String s) {
        PowerManager.WakeLock wakelock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EECS780 Service");
        wakelock.acquire();

        Log.d("Sample PN", "Received message: " + s);

        try {
            JSONObject jsonObject = new JSONObject(s);

            if (jsonObject.has("event")) {
                String event = jsonObject.getString("event");
                String data = jsonObject.getString("data");

                JSONObject dataObject = new JSONObject(data);

                if (event.equals("call_syn")) {
                    String roomId = dataObject.getString("roomId");
                    boolean video = dataObject.getBoolean("video");
                    String callerName = dataObject.getString("callerName");
                    String callerAvatar = dataObject.getString("callerAvatar");

                    QiscusRTC.buildCallWith(roomId)
                            .setCallAs(QiscusRTC.CallAs.CALLEE)
                            .setCallType(video == true ? QiscusRTC.CallType.VIDEO : QiscusRTC.CallType.VOICE)
                            .setCalleeUsername(QiscusRTC.getUser())
                            .setCallerUsername(callerName)
                            .setCallerDisplayName(callerName)
                            .setCallerDisplayAvatar(callerAvatar)
                            .show(this);

                    /*
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //
                        }
                    }, 3000);
                    */
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        wakelock.release();
    }

    @Override
    public void onMessage(byte[] bytes) {
        //
    }

    @Override
    public void onDisconnect(int i, String s) {
        Log.d("Sample PN", String.format("Disconnected from websocket service. Code: %d, reason: %s", i, s));
        if (!disconnect) {
            startService(startIntent(this));
        } else{
            stopSelf();
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e("Sample PN", "Error occured: ", e);
        startService(startIntent(this));
    }

    public interface ServiceListener{
        public void newResponse(String response);
    }

    public static void initCall(String roomId, QiscusRTC.CallType callType, String targetUser, String callerName, String callerAvatar) {
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        try {
            data.put("roomId", roomId);
            data.put("video", callType == QiscusRTC.CallType.VIDEO);
            data.put("callerName", callerName);
            data.put("callerAvatar", callerAvatar);
            jsonObject.put("request", "call_syn");
            jsonObject.put("recipient", targetUser);
            jsonObject.put("data", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (websocketClient.isConnected()) {
            websocketClient.send(jsonObject.toString());
        }
    }
}
