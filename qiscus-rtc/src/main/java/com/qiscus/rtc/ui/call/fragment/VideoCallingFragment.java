package com.qiscus.rtc.ui.call.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.qiscus.rtc.R;
import com.qiscus.rtc.ui.base.CallingFragment;

/**
 * Created by rahardyan on 06/06/17.
 */

public class VideoCallingFragment extends CallingFragment {
    @Override
    protected int getLayout() {
        return R.layout.fragment_calling_video;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}

