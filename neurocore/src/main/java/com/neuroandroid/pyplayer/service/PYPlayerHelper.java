package com.neuroandroid.pyplayer.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.neuroandroid.pyplayer.bean.Song;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by NeuroAndroid on 2017/5/16.
 */

public class PYPlayerHelper {
    @NonNull
    private static PYMusicService mMusicService;

    private static final Map<Context, ServiceBinder> mConnectionMap = new WeakHashMap<>();

    /**
     * 绑定服务
     *
     * @param context 上下文
     * @param callback ServiceConnection回调
     */
    public static ServiceToken bindToService(@NonNull final Context context,
                                             final ServiceConnection callback) {
        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }

        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, PYMusicService.class));

        final ServiceBinder binder = new ServiceBinder(callback);

        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, PYMusicService.class), binder, Context.BIND_AUTO_CREATE)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    /**
     * 解除绑定
     */
    public static void unbindFromService(@Nullable final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mMusicService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;

        public ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PYMusicService.MusicBinder binder = (PYMusicService.MusicBinder) iBinder;
            mMusicService = binder.getService();
            if (mCallback != null) mCallback.onServiceConnected(componentName, iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (mCallback != null) mCallback.onServiceDisconnected(componentName);
            mMusicService = null;
        }
    }

    /**
     * 播放指定位置的歌曲
     */
    public static void playSongAt(final int position) {
        if (mMusicService != null) {
            mMusicService.playSongAt(position);
        }
    }

    /**
     * 暂停歌曲
     */
    public static void pause() {
        if (mMusicService != null) {
            mMusicService.pause();
        }
    }

    /**
     * 播放前一首歌曲
     */
    public static void playPreviousSong() {
        if (mMusicService != null) {
            mMusicService.playPreviousSong();
        }
    }

    /**
     * 播放下一首歌曲
     */
    public static void playNextSong() {
        if (mMusicService != null) {
            mMusicService.playNextSong();
        }
    }

    /**
     * 歌曲是否正在播放
     */
    public static boolean isPlaying() {
        return mMusicService != null && mMusicService.isPlaying();
    }

    /**
     * 回复播放
     */
    public static void resumePlaying() {
        if (mMusicService != null) {
            mMusicService.play();
        }
    }

    /**
     * 返回当前播放的歌曲信息
     */
    public static Song getCurrentSong() {
        if (mMusicService != null) {
            return mMusicService.getCurrentSong();
        }
        return Song.EMPTY_SONG;
    }

    /**
     * 返回当前歌曲的索引
     */
    public static int getCurrentPosition() {
        if (mMusicService != null) {
            return mMusicService.getCurrentPosition();
        }
        return -1;
    }

    /**
     * 获取正在播放的队列
     */
    public static ArrayList<Song> getPlayingQueue() {
        if (mMusicService != null) {
            return mMusicService.getPlayingQueue();
        }
        return new ArrayList<>();
    }

    /**
     * 获取歌曲当前的播放进度
     */
    public static int getSongProgressMillis() {
        if (mMusicService != null) {
            return mMusicService.getSongProgressMillis();
        }
        return -1;
    }

    /**
     * 获取歌曲总时长
     */
    public static int getSongDurationMillis() {
        if (mMusicService != null) {
            return mMusicService.getSongDurationMillis();
        }
        return -1;
    }

    /**
     * 拖动进度
     */
    public static int seekTo(int millis) {
        if (mMusicService != null) {
            return mMusicService.seekTo(millis);
        }
        return -1;
    }

    /**
     * 获取播放模式
     */
    public static int getPlayMode() {
        if (mMusicService != null) {
            return mMusicService.getCurrentPlayMode();
        }
        return PYMusicService.MODE_ORDER;
    }

    /**
     * 周期性地改变播放模式
     */
    public static boolean cyclePlayMode() {
        if (mMusicService != null) {
            mMusicService.cyclePlayMode();
            return true;
        }
        return false;
    }
}
