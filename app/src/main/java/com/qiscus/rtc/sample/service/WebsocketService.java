package com.qiscus.rtc.sample.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fitraditya.androidwebsocket.WebsocketClient;

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
    private WebsocketClient websocketClient;
    private ServiceListener serviceListener;
    private Handler handler;
    private HashSet<String> list = new HashSet<String>();
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
            websocketClient = new WebsocketClient(URI.create("wss://rtc.qiscus.com/pn"), this, null, clientlock);
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
        WebsocketService getService() {
            return WebsocketService.this;
        }
    }

    public synchronized void setListener(ServiceListener listener){
        serviceListener = listener;
    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onMessage(String s) {

    }

    @Override
    public void onMessage(byte[] bytes) {

    }

    @Override
    public void onDisconnect(int i, String s) {

    }

    @Override
    public void onError(Exception e) {

    }

    public interface ServiceListener{
        public void newResponse(String response);
    }
}
