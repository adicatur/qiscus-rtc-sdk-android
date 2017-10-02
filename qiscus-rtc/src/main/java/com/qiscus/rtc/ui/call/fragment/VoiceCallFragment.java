package com.qiscus.rtc.ui.call.fragment;

import android.view.View;

import com.qiscus.rtc.R;
import com.qiscus.rtc.ui.base.CallFragment;

/**
 * Created by rahardyan on 06/06/17.
 */

public class VoiceCallFragment extends CallFragment {
    @Override
    protected int getLayout() {
        return R.layout.fragment_voice_call;
    }

    @Override
    protected void onParentViewCreated(View view) {
        //
    }
}

