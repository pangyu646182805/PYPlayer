package com.neuroandroid.pyplayer.helper;

import android.os.Handler;
import android.os.Message;

import com.neuroandroid.pyplayer.service.PYPlayerHelper;

/**
 * Created by NeuroAndroid on 2017/5/18.
 */

public class MusicProgressUpdateHelper extends Handler {
    private static final int MESSAGE_UPDATE_PROGRESS_VIEWS = 11;
    private static final int MIN_INTERVAL = 20;
    private static final int UPDATE_INTERVAL_PLAYING = 1000;
    private static final int UPDATE_INTERVAL_PAUSED = 500;

    private ProgressUpdateCallBack mUpdateCallBack;

    public MusicProgressUpdateHelper(ProgressUpdateCallBack updateCallBack) {
        this.mUpdateCallBack = updateCallBack;
    }

    public void start() {
        queueNextRefresh(1);
    }

    public void stop() {
        removeMessages(MESSAGE_UPDATE_PROGRESS_VIEWS);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == MESSAGE_UPDATE_PROGRESS_VIEWS) {
            queueNextRefresh(refreshProgressViews());
        }
    }

    private int refreshProgressViews() {
        final int progressMillis = PYPlayerHelper.getSongProgressMillis();
        final int totalMillis = PYPlayerHelper.getSongDurationMillis();
        mUpdateCallBack.onUpdateProgressViews(progressMillis, totalMillis);
        if (!PYPlayerHelper.isPlaying()) {
            return UPDATE_INTERVAL_PAUSED;
        }
        final int remainingMillis = UPDATE_INTERVAL_PLAYING - progressMillis % UPDATE_INTERVAL_PLAYING;
        return Math.max(MIN_INTERVAL, remainingMillis);
    }

    private void queueNextRefresh(final long delay) {
        final Message message = obtainMessage(MESSAGE_UPDATE_PROGRESS_VIEWS);
        removeMessages(MESSAGE_UPDATE_PROGRESS_VIEWS);
        sendMessageDelayed(message, delay);
    }

    public interface ProgressUpdateCallBack {
        void onUpdateProgressViews(int progress, int total);
    }
}
