package com.neuroandroid.pyplayer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.listener.Playback;
import com.neuroandroid.pyplayer.listener.PlayingNotification;
import com.neuroandroid.pyplayer.provider.MusicPlaybackQueueStore;
import com.neuroandroid.pyplayer.service.notification.PlayingNotificationImpl;
import com.neuroandroid.pyplayer.service.notification.PlayingNotificationImpl24;
import com.neuroandroid.pyplayer.utils.L;
import com.neuroandroid.pyplayer.utils.MusicUtil;
import com.neuroandroid.pyplayer.utils.ShowUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by NeuroAndroid on 2017/5/16.
 */

public class PYMusicService extends Service implements Playback.PlaybackCallback {
    public static final String ACTION_REPEAT_MODE_CHANGED = "action_repeat_mode_changed";
    public static final String ACTION_PLAY_STATE_CHANGED = "action_play_state_changed";
    public static final String ACTION_META_CHANGED = "action_meta_changed";

    public static final String ACTION_NOTIFICATION_TOGGLE_PAUSE = "action_media_toggle_pause";
    public static final String ACTION_NOTIFICATION_REWIND = "action_media_rewind";
    public static final String ACTION_NOTIFICATION_SKIP = "action_media_skip";
    public static final String ACTION_NOTIFICATION_QUIT = "action_media_quit";

    public static final int MESSAGE_PLAY_SONG = 999;
    // 专门处理无缝播放
    public static final int MESSAGE_TRACK_WENT_TO_NEXT = 1000;
    // 当前歌曲播放完成的处理
    public static final int MESSAGE_TRACK_ENDED = 1001;
    public static final int MESSAGE_FOCUS_CHANG = 1002;
    public static final int MESSAGE_DUCK = 1003;
    public static final int MESSAGE_UNDUCK = 1004;
    public static final int MESSAGE_RESTORE_QUEUES = 1005;

    public static final String SP_PLAY_MODE = "sp_play_mode";
    public static final String SP_CURRENT_POSITION = "sp_current_position";
    public static final String SP_CURRENT_PROGRESS = "sp_current_progress";

    public static final int MODE_ORDER = 0;  // 顺序播放
    public static final int MODE_SINGLE_REPEAT = 1;  // 单曲循环
    public static final int MODE_ALL_REPEAT = 2;  // 循环播放
    private int mCurrentPlayMode = MODE_ORDER;

    /**
     * 播放器控制类
     */
    private Playback mPlayback;

    /**
     * 通知栏
     */
    private PlayingNotification mPlayingNotification;

    /**
     * 播放队列
     */
    private ArrayList<Song> mPlayingQueue = new ArrayList<>();

    /**
     * 当前的索引
     */
    private int mCurrentPosition;

    private HandlerThread mPlayerHandlerThread;

    /**
     * 播放器控制处理类
     */
    private PlaybackHandler mPlaybackHandler;

    /**
     * Android音频管理器
     */
    private AudioManager mAudioManager;

    /**
     * 处理音频焦点
     * 例如当短信来时，自动停止播放音乐
     */
    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = focusChange ->
            mPlaybackHandler.obtainMessage(MESSAGE_FOCUS_CHANG, focusChange, 0).sendToTarget();

    /**
     * 是否Audio输出通道改变触发某些事件
     */
    private boolean mBecomingNoisyReceiverRegistered;

    /**
     * 是否是瞬间失去音频焦点情况下的暂停
     */
    private boolean mPausedByTransientLossOfFocus;

    /**
     * Audio输出通道改变的Action
     */
    private IntentFilter mBecomingNoisyReceiverIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private final BroadcastReceiver mBecomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                // Audio输出通道改变暂停音乐播放
                pause();
            }
        }
    };

    /**
     * service是否绑定
     */
    private boolean isServiceBound;

    private boolean mQueuesRestored;

    /**
     * 有没有处理ACTION_META_CHANGED
     */
    private boolean mNotHandledMetaChangedForCurrentTrack;

    private MusicBinder mMusicBinder = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayerHandlerThread = new HandlerThread("PlaybackHandler");
        mPlayerHandlerThread.start();
        mPlaybackHandler = new PlaybackHandler(this, mPlayerHandlerThread.getLooper());

        mPlayback = new MusicPlayer(this);
        mPlayback.setCallback(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mPlayingNotification = new PlayingNotificationImpl24();
        } else {
            mPlayingNotification = new PlayingNotificationImpl();
        }
        mPlayingNotification.init(this);

        restoreState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!UIUtils.isEmpty(action)) {
                restoreQueuesAndPosition();
                switch (action) {
                    case ACTION_NOTIFICATION_TOGGLE_PAUSE:
                        if (isPlaying()) {
                            pause();
                        } else {
                            play();
                        }
                        break;
                    case ACTION_NOTIFICATION_REWIND:
                        playPreviousSong();
                        break;
                    case ACTION_NOTIFICATION_SKIP:
                        playNextSong();
                        break;
                    case ACTION_NOTIFICATION_QUIT:
                        return quit();
                }
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isServiceBound = true;
        return mMusicBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        isServiceBound = true;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isServiceBound = false;
        if (!isPlaying()) {
            stopSelf();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBecomingNoisyReceiverRegistered) {
            unregisterReceiver(mBecomingNoisyReceiver);
            mBecomingNoisyReceiverRegistered = false;
        }
        quit();
        releaseResources();
    }

    @Override
    public void onTrackWentToNext() {
        mPlaybackHandler.sendEmptyMessage(MESSAGE_TRACK_WENT_TO_NEXT);
    }

    @Override
    public void onTrackEnded() {
        mPlaybackHandler.sendEmptyMessage(MESSAGE_TRACK_ENDED);
    }

    public class MusicBinder extends Binder {
        public PYMusicService getService() {
            return PYMusicService.this;
        }
    }

    /**
     * 回复状态
     */
    private void restoreState() {
        mCurrentPlayMode = PreferenceManager.getDefaultSharedPreferences(this).getInt(SP_PLAY_MODE, 0);
        handleAndSendChangeInternal(ACTION_REPEAT_MODE_CHANGED);

        mPlaybackHandler.removeMessages(MESSAGE_RESTORE_QUEUES);
        mPlaybackHandler.sendEmptyMessage(MESSAGE_RESTORE_QUEUES);
    }

    /**
     * 根据position播放歌曲
     */
    public void playSongAt(final int position) {
        mPlaybackHandler.removeMessages(MESSAGE_PLAY_SONG);
        mPlaybackHandler.obtainMessage(MESSAGE_PLAY_SONG, position, 0).sendToTarget();
    }

    /**
     * 根据position播放歌曲(实现方法)
     */
    private void playSongAtImpl(final int position) {
        if (openCurrent(position)) {
            play();
        } else {
            ShowUtils.showToast(getResources().getString(R.string.unplayable_file));
        }
    }

    /**
     * 获取歌曲的总时长
     */
    public int getSongDurationMillis() {
        return mPlayback.duration();
    }

    /**
     * 播放
     */
    public void play() {
        synchronized (this) {
            if (requestFocus()) {
                if (!mPlayback.isPlaying()) {
                    if (!mPlayback.isInitialized()) {
                        playSongAt(getCurrentPosition());
                    } else {
                        mPlayback.start();
                        if (!mBecomingNoisyReceiverRegistered) {
                            registerReceiver(mBecomingNoisyReceiver, mBecomingNoisyReceiverIntentFilter);
                            mBecomingNoisyReceiverRegistered = true;
                        }
                        if (mNotHandledMetaChangedForCurrentTrack) {
                            handleChangeInternal(ACTION_META_CHANGED);
                            mNotHandledMetaChangedForCurrentTrack = false;
                        }
                        notifyChange(ACTION_PLAY_STATE_CHANGED);
                    }
                }
            } else {
                ShowUtils.showToast(getResources().getString(R.string.audio_focus_denied));
            }
        }
    }

    /**
     * 请求音频焦点
     */
    private boolean requestFocus() {
        return (getAudioManager().requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
    }

    private AudioManager getAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        return mAudioManager;
    }

    /**
     * 打开当前歌曲
     * 打开异常返回false
     */
    public boolean openCurrent(final int position) {
        synchronized (this) {
            try {
                this.mCurrentPosition = position;
                notifyChange(ACTION_META_CHANGED);
                mNotHandledMetaChangedForCurrentTrack = false;
                return mPlayback.setDataSource(getTrackUri(getCurrentSong()));
            } catch (Exception e) {
                L.e("error : " + e.getMessage());
                return false;
            }
        }
    }

    /**
     * 获取当前歌曲信息
     */
    public Song getCurrentSong() {
        return getSongAt(getCurrentPosition());
    }

    /**
     * 获取当前位置
     */
    public int getCurrentPosition() {
        return this.mCurrentPosition;
    }

    /**
     * 获取上一首的position
     */
    public int getPreviousPosition() {
        int newPosition = getCurrentPosition() - 1;
        switch (getCurrentPlayMode()) {
            case MODE_ALL_REPEAT:
                if (newPosition < 0) {
                    newPosition = getPlayingQueue().size() - 1;
                }
                break;
            case MODE_SINGLE_REPEAT:
                newPosition = getCurrentPosition();
                break;
            default:
            case MODE_ORDER:
                if (newPosition < 0) {
                    newPosition = 0;
                }
                break;
        }
        return newPosition;
    }

    /**
     * 获取下一首的position
     */
    public int getNextPosition() {
        int position = getCurrentPosition() + 1;
        switch (getCurrentPlayMode()) {
            case MODE_ALL_REPEAT:
                if (isLastTrack()) {
                    position = 0;
                }
                break;
            case MODE_SINGLE_REPEAT:
                position -= 1;
                break;
            default:
            case MODE_ORDER:
                if (isLastTrack()) {
                    position -= 1;
                }
                break;
        }
        return position;
    }

    /**
     * 移动进度条
     */
    public int seekTo(int millis) {
        synchronized (this) {
            try {
                return mPlayback.seek(millis);
            } catch (Exception e) {
                return -1;
            }
        }
    }

    /**
     * 周期的设置播放模式
     */
    public void cyclePlayMode() {
        switch (getCurrentPlayMode()) {
            case MODE_ORDER:
                setPlayMode(MODE_ALL_REPEAT);
                break;
            case MODE_ALL_REPEAT:
                setPlayMode(MODE_SINGLE_REPEAT);
                break;
            default:
            case MODE_SINGLE_REPEAT:
                setPlayMode(MODE_ORDER);
                break;
        }
    }

    /**
     * 设置播放模式
     */
    public void setPlayMode(final int playMode) {
        switch (playMode) {
            case MODE_ORDER:
            case MODE_ALL_REPEAT:
            case MODE_SINGLE_REPEAT:
                this.mCurrentPlayMode = playMode;
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putInt(SP_PLAY_MODE, playMode)
                        .apply();
                handleAndSendChangeInternal(ACTION_REPEAT_MODE_CHANGED);
                break;
        }
    }

    /**
     * 播放上一首歌曲
     */
    public void playPreviousSong() {
        playSongAt(getPreviousPosition());
    }

    /**
     * 播放下一首歌曲
     */
    public void playNextSong() {
        playSongAt(getNextPosition());
    }

    /**
     * 通知改变
     */
    private void notifyChange(@NonNull final String what) {
        handleAndSendChangeInternal(what);
    }

    /**
     * 处理状态改变事件
     */
    public void handleAndSendChangeInternal(@NonNull final String what) {
        handleChangeInternal(what);
        sendBroadcast(new Intent(what));
    }

    private void handleChangeInternal(@NonNull final String what) {
        switch (what) {
            case ACTION_PLAY_STATE_CHANGED:
                updateNotification();
                if (!isPlaying() && getSongProgressMillis() > 0) {
                    // 当前没有在播放且进度大于0则保存进度
                    savePositionInTrack();
                }
                break;
            case ACTION_META_CHANGED:
                updateNotification();
                savePosition();
                savePositionInTrack();
                break;
        }
    }

    /**
     * 获取播放队列
     */
    public ArrayList<Song> getPlayingQueue() {
        return mPlayingQueue;
    }

    /**
     * 获取当前播放模式
     */
    public int getCurrentPlayMode() {
        return mCurrentPlayMode;
    }

    /**
     * 获取position位置的歌曲信息
     */
    public Song getSongAt(int position) {
        if (position >= 0 && position < getPlayingQueue().size()) {
            return getPlayingQueue().get(position);
        } else {
            return Song.EMPTY_SONG;
        }
    }

    /**
     * 是否是最后一首歌曲
     */
    private boolean isLastTrack() {
        return getCurrentPosition() == getPlayingQueue().size() - 1;
    }

    /**
     * 获取歌曲播放的Uri
     */
    private static String getTrackUri(@NonNull Song song) {
        return MusicUtil.getSongFileUri(song.id).toString();
    }

    public boolean isPlaying() {
        return mPlayback != null && mPlayback.isPlaying();
    }

    private void updateNotification() {
        if (getCurrentSong().id != -1) {
            mPlayingNotification.update();
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            notifyChange(ACTION_PLAY_STATE_CHANGED);
        }
    }

    /**
     * 退出
     */
    private int quit() {
        pause();
        mPlayingNotification.stop();

        if (isServiceBound) {
            return START_STICKY;
        } else {
            getAudioManager().abandonAudioFocus(mAudioFocusListener);
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    /**
     * 保存当前歌曲索引
     */
    private void savePosition() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SP_CURRENT_POSITION, getCurrentPosition()).apply();
    }

    /**
     * 保存当前进度条位置
     */
    private void savePositionInTrack() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(SP_CURRENT_PROGRESS, getSongProgressMillis()).apply();
    }

    /**
     * 获取当前进度条位置
     */
    public int getSongProgressMillis() {
        return mPlayback.position();
    }

    /**
     * 释放资源
     */
    private void releaseResources() {
        mPlaybackHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= 18) {
            mPlayerHandlerThread.quitSafely();
        } else {
            mPlayerHandlerThread.quit();
        }
        mPlayback.release();
        mPlayback = null;
    }

    /**
     * 重新获取播放队列以及保存的一些参数
     */
    private synchronized void restoreQueuesAndPosition() {
        if (!mQueuesRestored && mPlayingQueue.isEmpty()) {
            ArrayList<Song> restoreQueues = MusicPlaybackQueueStore.getInstance(this).getSavedPlayingQueue();
            int restoredPosition = PreferenceManager.getDefaultSharedPreferences(this).getInt(SP_CURRENT_POSITION, -1);
            int restoredProgress = PreferenceManager.getDefaultSharedPreferences(this).getInt(SP_CURRENT_PROGRESS, -1);

            if (restoreQueues.size() > 0) {
                this.mPlayingQueue = restoreQueues;
                openCurrent(restoredPosition);
                if (restoredProgress > 0) seekTo(restoredProgress);

                mNotHandledMetaChangedForCurrentTrack = true;
                sendBroadcast(new Intent(ACTION_META_CHANGED));
            }
        }
        mQueuesRestored = true;
    }

    private static final class PlaybackHandler extends Handler {
        @NonNull
        private final WeakReference<PYMusicService> mService;
        private float mCurrentDuckVolume = 1.0f;

        public PlaybackHandler(final PYMusicService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PYMusicService pyMusicService = mService.get();
            if (pyMusicService == null) return;

            switch (msg.what) {
                case MESSAGE_DUCK:
                    mCurrentDuckVolume -= .05f;
                    if (mCurrentDuckVolume > .2f) {
                        sendEmptyMessageDelayed(MESSAGE_DUCK, 10);
                    } else {
                        mCurrentDuckVolume = .2f;
                    }
                    pyMusicService.mPlayback.setVolume(mCurrentDuckVolume);
                    break;
                case MESSAGE_UNDUCK:
                    mCurrentDuckVolume += .03f;
                    if (mCurrentDuckVolume < 1f) {
                        sendEmptyMessageDelayed(MESSAGE_UNDUCK, 10);
                    } else {
                        mCurrentDuckVolume = 1f;
                    }
                    break;
                case MESSAGE_TRACK_WENT_TO_NEXT:
                    // 处理无缝播放
                    break;
                case MESSAGE_TRACK_ENDED:
                    if (pyMusicService.getCurrentPlayMode() == MODE_ORDER && pyMusicService.isLastTrack()) {
                        pyMusicService.notifyChange(ACTION_PLAY_STATE_CHANGED);
                        pyMusicService.pause();
                        pyMusicService.seekTo(0);
                    } else {
                        pyMusicService.playNextSong();
                    }
                    break;
                case MESSAGE_PLAY_SONG:
                    pyMusicService.playSongAtImpl(msg.arg1);
                    break;
                case MESSAGE_RESTORE_QUEUES:
                    pyMusicService.restoreQueuesAndPosition();
                    break;
                case MESSAGE_FOCUS_CHANG:
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                            // 已经获取音频焦点
                            if (!pyMusicService.isPlaying() && pyMusicService.mPausedByTransientLossOfFocus) {
                                pyMusicService.play();
                                pyMusicService.mPausedByTransientLossOfFocus = false;
                            }
                            removeMessages(MESSAGE_DUCK);
                            sendEmptyMessage(MESSAGE_UNDUCK);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS:
                            // 已经失去音频焦点
                            pyMusicService.pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            // 暂时失去音频焦点
                            boolean wasPlaying = pyMusicService.isPlaying();
                            pyMusicService.pause();
                            pyMusicService.mPausedByTransientLossOfFocus = wasPlaying;
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            // 这说明你已经临时失去了音频焦点
                            // 但允许你安静的播放音频（低音量），而不是完全的终止音频播放
                            removeMessages(MESSAGE_UNDUCK);
                            sendEmptyMessage(MESSAGE_DUCK);
                            break;
                    }
                    break;
            }
        }
    }
}
