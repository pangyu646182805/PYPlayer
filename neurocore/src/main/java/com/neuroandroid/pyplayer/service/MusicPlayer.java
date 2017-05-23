package com.neuroandroid.pyplayer.service;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.listener.Playback;
import com.neuroandroid.pyplayer.utils.ShowUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;

/**
 * Created by NeuroAndroid on 2017/5/15.
 */

public class MusicPlayer implements Playback, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private Context mContext;
    private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
    private MediaPlayer mNextMediaPlayer;
    @Nullable
    private PlaybackCallback mPlaybackCallback;

    private boolean mIsInitialized = false;

    public MusicPlayer(Context context) {
        this.mContext = context;
        mCurrentMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
    }

    @Override
    public boolean setDataSource(@NonNull String path) {
        mIsInitialized = false;
        mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
        if (mIsInitialized) {
            setNextDataSource(null);
        }
        return mIsInitialized;
    }

    private boolean setDataSourceImpl(@NonNull final MediaPlayer player, @NonNull final String path) {
        if (mContext == null) {
            return false;
        }
        try {
            player.reset();
            player.setOnPreparedListener(null);
            if (path.startsWith("content://")) {
                player.setDataSource(mContext, Uri.parse(path));
            } else {
                player.setDataSource(path);
            }
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.prepare();
        } catch (Exception e) {
            return false;
        }
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.getPackageName());
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
        mContext.sendBroadcast(intent);
        return true;
    }

    @Override
    public void setNextDataSource(@Nullable String path) {

    }

    @Override
    public void setCallback(PlaybackCallback callback) {
        this.mPlaybackCallback = callback;
    }

    @Override
    public boolean isInitialized() {
        return mIsInitialized;
    }

    @Override
    public boolean start() {
        try {
            mCurrentMediaPlayer.start();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public void stop() {
        mCurrentMediaPlayer.reset();
        mIsInitialized = false;
    }

    @Override
    public void release() {
        stop();
        mCurrentMediaPlayer.release();
    }

    @Override
    public boolean pause() {
        try {
            mCurrentMediaPlayer.pause();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public boolean isPlaying() {
        return mIsInitialized && mCurrentMediaPlayer.isPlaying();
    }

    @Override
    public int duration() {
        if (!mIsInitialized) {
            return -1;
        }
        try {
            return mCurrentMediaPlayer.getDuration();
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    @Override
    public int position() {
        if (!mIsInitialized) {
            return -1;
        }
        try {
            return mCurrentMediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    @Override
    public int seek(int whereto) {
        try {
            mCurrentMediaPlayer.seekTo(whereto);
            return whereto;
        } catch (IllegalStateException e) {
            return -1;
        }
    }

    @Override
    public boolean setVolume(float vol) {
        try {
            mCurrentMediaPlayer.setVolume(vol, vol);
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    @Override
    public boolean setAudioSessionId(int sessionId) {
        try {
            mCurrentMediaPlayer.setAudioSessionId(sessionId);
            return true;
        } catch (@NonNull IllegalArgumentException | IllegalStateException e) {
            return false;
        }
    }

    @Override
    public int getAudioSessionId() {
        return mCurrentMediaPlayer.getAudioSessionId();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mediaPlayer == mCurrentMediaPlayer && mNextMediaPlayer != null) {
            mIsInitialized = false;
            mCurrentMediaPlayer.release();
            mCurrentMediaPlayer = mNextMediaPlayer;
            mIsInitialized = true;
            mNextMediaPlayer = null;
            if (mPlaybackCallback != null)
                mPlaybackCallback.onTrackWentToNext();
        } else {
            if (mPlaybackCallback != null)
                mPlaybackCallback.onTrackEnded();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        mIsInitialized = false;
        mCurrentMediaPlayer.release();
        mCurrentMediaPlayer = new MediaPlayer();
        mCurrentMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        if (mContext != null) {
            ShowUtils.showToast(UIUtils.getString(R.string.unplayable_file));
        }
        return false;
    }
}
