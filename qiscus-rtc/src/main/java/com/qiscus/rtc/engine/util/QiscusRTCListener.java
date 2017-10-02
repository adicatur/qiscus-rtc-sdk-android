package com.qiscus.rtc.engine.util;

import org.webrtc.RendererCommon;

/**
 * Created by fitra on 2/10/17.
 */

public interface QiscusRTCListener {
    public void onUpdateVideoView(Boolean iceConnected, RendererCommon.ScalingType scalingType);
    public void onPeerDown();
    public void onPeerError();
}
