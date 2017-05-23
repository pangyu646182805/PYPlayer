package com.neuroandroid.pyplayer.service.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.glide.SongGlideRequest;
import com.neuroandroid.pyplayer.glide.palette.BitmapPaletteWrapper;
import com.neuroandroid.pyplayer.listener.PlayingNotification;
import com.neuroandroid.pyplayer.service.PYMusicService;
import com.neuroandroid.pyplayer.ui.activity.MainActivity;
import com.neuroandroid.pyplayer.utils.UIUtils;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class PlayingNotificationImpl24 implements PlayingNotification {
    private static final int NOTIFY_MODE_FOREGROUND = 1;
    private static final int NOTIFY_MODE_BACKGROUND = 0;

    private PYMusicService mMusicService;

    private NotificationManager mNotificationManager;

    private int notifyMode = NOTIFY_MODE_BACKGROUND;

    private boolean stopped;

    @Override
    public synchronized void init(PYMusicService service) {
        this.mMusicService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public synchronized void update() {
        stopped = false;

        final Song song = mMusicService.getCurrentSong();

        final String albumName = song.albumName;
        final String artistName = song.artistName;
        final boolean isPlaying = mMusicService.isPlaying();
        final String text = TextUtils.isEmpty(albumName)
                ? artistName : artistName + " - " + albumName;

        final int playButtonResId = isPlaying
                ? R.drawable.ic_pause_white : R.drawable.ic_play_arrow_white;

        Intent action = new Intent(mMusicService, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent clickIntent = PendingIntent.getActivity(mMusicService, 0, action, 0);

        final ComponentName serviceName = new ComponentName(mMusicService, PYMusicService.class);
        Intent intent = new Intent(PYMusicService.ACTION_NOTIFICATION_QUIT);
        intent.setComponent(serviceName);
        final PendingIntent deleteIntent = PendingIntent.getService(mMusicService, 0, intent, 0);

        final int bigNotificationImageSize = mMusicService.getResources().getDimensionPixelSize(R.dimen.y256);
        UIUtils.getHandler().post(() -> SongGlideRequest.Builder.from(Glide.with(mMusicService), song)
                .generatePalette(mMusicService).build()
                .into(new SimpleTarget<BitmapPaletteWrapper>(bigNotificationImageSize, bigNotificationImageSize) {
                    @Override
                    public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                        Palette palette = resource.getPalette();
                        update(resource.getBitmap(), palette.getVibrantColor(palette.getMutedColor(Color.TRANSPARENT)));
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        update(null, Color.TRANSPARENT);
                    }

                    void update(Bitmap bitmap, int color) {
                        if (bitmap == null)
                            bitmap = BitmapFactory.decodeResource(mMusicService.getResources(), R.mipmap.default_album_art);
                        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(playButtonResId,
                                mMusicService.getString(R.string.action_play_pause),
                                retrievePlaybackAction(PYMusicService.ACTION_NOTIFICATION_TOGGLE_PAUSE));
                        NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white,
                                mMusicService.getString(R.string.action_previous),
                                retrievePlaybackAction(PYMusicService.ACTION_NOTIFICATION_REWIND));
                        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white,
                                mMusicService.getString(R.string.action_next),
                                retrievePlaybackAction(PYMusicService.ACTION_NOTIFICATION_SKIP));
                        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(mMusicService)
                                .setSmallIcon(R.mipmap.ic_notification)
                                .setLargeIcon(bitmap)
                                .setContentIntent(clickIntent)
                                .setDeleteIntent(deleteIntent)
                                .setContentTitle(song.title)
                                .setContentText(text)
                                .setOngoing(isPlaying)
                                .setShowWhen(false)
                                .addAction(previousAction)
                                .addAction(playPauseAction)
                                .addAction(nextAction);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder.setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                            // if (PreferenceUtil.getInstance(service).coloredNotification())
                            if (true)
                                builder.setColor(color);
                        }

                        if (stopped)
                            return; // notification has been stopped before loading was finished
                        updateNotifyModeAndPostNotification(builder.build());
                    }
                }));
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(mMusicService, PYMusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);

        return PendingIntent.getService(mMusicService, 0, intent, 0);
    }

    private void updateNotifyModeAndPostNotification(Notification notification) {
        int newNotifyMode;
        // if (mMusicService.isPlaying()) {
        if (true) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        }

        if (notifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            mMusicService.stopForeground(false);
        }

        if (newNotifyMode == NOTIFY_MODE_FOREGROUND) {
            mMusicService.startForeground(NOTIFICATION_ID, notification);
        } else if (newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }

        notifyMode = newNotifyMode;
    }

    @Override
    public synchronized void stop() {
        stopped = true;
        mMusicService.stopForeground(true);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }
}
