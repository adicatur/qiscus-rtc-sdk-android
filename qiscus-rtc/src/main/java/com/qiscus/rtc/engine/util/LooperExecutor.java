package com.qiscus.rtc.engine.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Executor;

/**
 * Created by fitra on 2/10/17.
 */

public class LooperExecutor extends Thread implements Executor {
    private static final String TAG = LooperExecutor.class.getSimpleName();

    private final Object looperStartedEvent = new Object();

    private Handler handler = null;
    private Boolean running = false;
    private long threadId;

    @Override
    public void run() {
        Looper.prepare();
        synchronized (looperStartedEvent) {
            Log.d(TAG, "Looper thread started.");
            handler = new Handler();
            threadId = Thread.currentThread().getId();
            looperStartedEvent.notify();
        }
        Looper.loop();
    }

    @Override
    public synchronized void execute(final Runnable runnable) {
        if (!running) {
            Log.w(TAG, "Running looper executor without calling requestStart()");
            return;
        }
        if (Thread.currentThread().getId() == threadId) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    public synchronized void requestStart() {
        if (running) {
            return;
        }
        running = true;
        handler = null;
        start();
        synchronized (looperStartedEvent) {
            while (handler == null) {
                try {
                    looperStartedEvent.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Can not start looper thread");
                    running = false;
                }
            }
        }
    }

    public synchronized void requestStop() {
        if (!running) {
            return;
        }
        running = false;
        handler.post(new Runnable() {
            @Override
            public void run() {
                Looper.myLooper().quit();
                Log.d(TAG, "Looper thread finished.");
            }
        });
    }
}

