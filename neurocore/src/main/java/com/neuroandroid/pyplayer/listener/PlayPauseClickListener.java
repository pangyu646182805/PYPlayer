package com.neuroandroid.pyplayer.listener;

import android.view.View;

import com.neuroandroid.pyplayer.service.PYPlayerHelper;

/**
 * Created by NeuroAndroid on 2017/5/18.
 */

public class PlayPauseClickListener implements View.OnClickListener {
    @Override
    public void onClick(View view) {
        if (PYPlayerHelper.isPlaying()) {
            PYPlayerHelper.pause();
        } else {
            PYPlayerHelper.resumePlaying();
        }
    }
}
