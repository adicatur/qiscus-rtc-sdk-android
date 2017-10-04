package com.qiscus.rtc.sample.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by fitra on 04/10/17.
 */

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            Log.i("Sample PN", "Connected");
            context.startService(WebsocketService.startIntent(context.getApplicationContext()));
        } else if (networkInfo != null){
            NetworkInfo.DetailedState state = networkInfo.getDetailedState();
            Log.i("Sample PN", "State: " + state.name());
        } else {
            Log.i("Sample PN", "Disconnected");
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, WebsocketService.pingIntent(context), PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null){
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
            context.startService(WebsocketService.closeIntent(context));
        }
    }
}
