package com.qiscus.rtc.sample;

import android.app.Application;

import com.qiscus.rtc.QiscusRTC;

/**
 * Created by fitra on 04/10/17.
 */

public class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        QiscusRTC.init(this, "sample-application-C2", "KpPiqKGpoN");
    }
}
