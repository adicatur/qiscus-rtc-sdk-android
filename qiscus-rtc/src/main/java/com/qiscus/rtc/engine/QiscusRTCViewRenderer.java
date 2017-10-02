package com.qiscus.rtc.engine;

import android.content.Context;
import android.util.AttributeSet;

import org.webrtc.SurfaceViewRenderer;

/**
 * Created by fitra on 2/10/17.
 */

public class QiscusRTCViewRenderer extends SurfaceViewRenderer {
    public QiscusRTCViewRenderer(Context context) {
        super(context);
    }

    public QiscusRTCViewRenderer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
