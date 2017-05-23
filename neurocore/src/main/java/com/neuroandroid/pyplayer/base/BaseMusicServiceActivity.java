package com.neuroandroid.pyplayer.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;

import com.neuroandroid.pyplayer.config.Constant;
import com.neuroandroid.pyplayer.listener.MusicServiceEventListener;
import com.neuroandroid.pyplayer.service.PYMusicService;
import com.neuroandroid.pyplayer.service.PYPlayerHelper;
import com.neuroandroid.pyplayer.utils.SPUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeuroAndroid on 2017/5/10.
 */

public class BaseMusicServiceActivity extends BaseActivity implements MusicServiceEventListener {
    private final List<MusicServiceEventListener> mMusicServiceEventListeners = new ArrayList<>();
    private PYPlayerHelper.ServiceToken mServiceToken;
    private boolean mReceiverRegistered;
    private MusicStateReceiver mMusicStateReceiver;

    @Override
    protected View attachLayout() {
        return null;
    }

    @Override
    protected void initView() {
        boolean firstIntoApp = SPUtils.getBoolean(this, Constant.FIRST_INTO_APP, true);
        if (!firstIntoApp) {
            bindToMusicService();
        }
    }

    public void bindToMusicService() {
        mServiceToken = PYPlayerHelper.bindToService(this, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                BaseMusicServiceActivity.this.onServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                BaseMusicServiceActivity.this.onServiceDisconnected();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PYPlayerHelper.unbindFromService(mServiceToken);
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        if (mReceiverRegistered) {
            if (mMusicStateReceiver != null) {
                unregisterReceiver(mMusicStateReceiver);
                mMusicStateReceiver = null;
                mReceiverRegistered = false;
            }
        }
    }

    public void addMusicServiceEventListener(final MusicServiceEventListener listener) {
        if (listener != null) {
            mMusicServiceEventListeners.add(listener);
        }
    }

    public void removeMusicServiceEventListener(final MusicServiceEventListener listener) {
        if (listener != null) {
            mMusicServiceEventListeners.remove(listener);
        }
    }

    @Override
    public void onServiceConnected() {
        if (!mReceiverRegistered) {
            mMusicStateReceiver = new MusicStateReceiver(this);
            final IntentFilter filter = new IntentFilter();
            filter.addAction(PYMusicService.ACTION_PLAY_STATE_CHANGED);
            filter.addAction(PYMusicService.ACTION_META_CHANGED);
            filter.addAction(PYMusicService.ACTION_REPEAT_MODE_CHANGED);
            registerReceiver(mMusicStateReceiver, filter);
            mReceiverRegistered = true;
        }
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            if (listener != null) listener.onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected() {
        unregisterReceiver();
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            if (listener != null) listener.onServiceDisconnected();
        }
    }

    @Override
    public void onQueueChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            if (listener != null) listener.onQueueChanged();
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            if (listener != null) listener.onPlayingMetaChanged();
        }
    }

    @Override
    public void onPlayStateChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            if (listener != null) listener.onPlayStateChanged();
        }
    }

    @Override
    public void onRepeatModeChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            if (listener != null) listener.onRepeatModeChanged();
        }
    }

    @Override
    public void onShuffleModeChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            if (listener != null) listener.onShuffleModeChanged();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        for (MusicServiceEventListener listener : mMusicServiceEventListeners) {
            if (listener != null) listener.onMediaStoreChanged();
        }
    }

    private static final class MusicStateReceiver extends BroadcastReceiver {
        private final WeakReference<BaseMusicServiceActivity> mReference;

        public MusicStateReceiver(final BaseMusicServiceActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();
                BaseMusicServiceActivity activity = mReference.get();
                if (!UIUtils.isEmpty(action) && activity != null) {
                    switch (action) {
                        case PYMusicService.ACTION_REPEAT_MODE_CHANGED:
                            activity.onRepeatModeChanged();
                            break;
                        case PYMusicService.ACTION_PLAY_STATE_CHANGED:
                            activity.onPlayStateChanged();
                            break;
                        case PYMusicService.ACTION_META_CHANGED:
                            activity.onPlayingMetaChanged();
                            break;
                    }
                }
            }
        }
    }
}
