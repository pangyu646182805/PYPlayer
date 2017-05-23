package com.neuroandroid.pyplayer.service.notification;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.glide.SongGlideRequest;
import com.neuroandroid.pyplayer.glide.palette.BitmapPaletteWrapper;
import com.neuroandroid.pyplayer.listener.PlayingNotification;
import com.neuroandroid.pyplayer.service.PYMusicService;
import com.neuroandroid.pyplayer.ui.activity.MainActivity;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.utils.PYPlayerColorUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;

public class PlayingNotificationImpl implements PlayingNotification {
    private PYMusicService mMusicService;

    private Target<BitmapPaletteWrapper> target;

    private boolean stopped;

    @Override
    public synchronized void init(PYMusicService service) {
        this.mMusicService = service;
    }

    @Override
    public synchronized void update() {
        stopped = false;

        final Song song = mMusicService.getCurrentSong();

        final boolean isPlaying = mMusicService.isPlaying();

        final RemoteViews notificationLayout = new RemoteViews(mMusicService.getPackageName(), R.layout.notification);
        final RemoteViews notificationLayoutBig = new RemoteViews(mMusicService.getPackageName(), R.layout.notification_big);

        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName)) {
            notificationLayout.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            notificationLayout.setViewVisibility(R.id.media_titles, View.VISIBLE);
            notificationLayout.setTextViewText(R.id.title, song.title);
            notificationLayout.setTextViewText(R.id.text, song.artistName);
        }

        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName) && TextUtils.isEmpty(song.albumName)) {
            notificationLayoutBig.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            notificationLayoutBig.setViewVisibility(R.id.media_titles, View.VISIBLE);
            notificationLayoutBig.setTextViewText(R.id.title, song.title);
            notificationLayoutBig.setTextViewText(R.id.text, song.artistName);
            notificationLayoutBig.setTextViewText(R.id.text2, song.albumName);
        }

        linkButtons(notificationLayout, notificationLayoutBig);

        Intent action = new Intent(mMusicService, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(mMusicService, 0, action, 0);

        final Notification notification = new NotificationCompat.Builder(mMusicService)
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentIntent(openAppPendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(notificationLayout)
                .setCustomBigContentView(notificationLayoutBig)
                .build();

        final int bigNotificationImageSize = mMusicService.getResources().getDimensionPixelSize(R.dimen.y256);
        UIUtils.getHandler().post(() -> {
            if (target != null) {
                Glide.clear(target);
            }
            target = SongGlideRequest.Builder.from(Glide.with(mMusicService), song)
                    .generatePalette(mMusicService).build()
                    .into(new SimpleTarget<BitmapPaletteWrapper>(bigNotificationImageSize, bigNotificationImageSize) {
                        @Override
                        public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                            update(resource.getBitmap(), PYPlayerColorUtils.getColor(resource.getPalette(), Color.TRANSPARENT));
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            super.onLoadFailed(e, errorDrawable);
                            update(null, Color.TRANSPARENT);
                        }

                        private void update(@Nullable Bitmap bitmap, int bgColor) {
                            if (bitmap != null) {
                                notificationLayout.setImageViewBitmap(R.id.image, bitmap);
                                notificationLayoutBig.setImageViewBitmap(R.id.image, bitmap);
                            } else {
                                notificationLayout.setImageViewResource(R.id.image, R.mipmap.default_album_art);
                                notificationLayoutBig.setImageViewResource(R.id.image, R.mipmap.default_album_art);
                            }

                            // if (!PreferenceUtil.getInstance(mMusicService).coloredNotification()) {
                            if (false) {
                                bgColor = Color.TRANSPARENT;
                            }
                            setBackgroundColor(bgColor);
                            setNotificationContent(bgColor == Color.TRANSPARENT ? Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP : ColorUtils.isColorLight(bgColor));

                            if (stopped)
                                return; // notification has been stopped before loading was finished
                            mMusicService.startForeground(NOTIFICATION_ID, notification);
                        }

                        private void setBackgroundColor(int color) {
                            notificationLayout.setInt(R.id.root, "setBackgroundColor", color);
                            notificationLayoutBig.setInt(R.id.root, "setBackgroundColor", color);
                        }

                        private void setNotificationContent(boolean dark) {
                            int primary = ColorUtils.getPrimaryTextColor(mMusicService, dark);
                            int secondary = ColorUtils.getSecondaryTextColor(mMusicService, dark);

                            Bitmap prev = createBitmap(UIUtils.getTintedVectorDrawable(mMusicService, R.drawable.ic_skip_previous_white, primary), 1.5f);
                            Bitmap next = createBitmap(UIUtils.getTintedVectorDrawable(mMusicService, R.drawable.ic_skip_next_white, primary), 1.5f);
                            Bitmap playPause = createBitmap(UIUtils.getTintedVectorDrawable(mMusicService, isPlaying ? R.drawable.ic_pause_white : R.drawable.ic_play_arrow_white, primary), 1.5f);
                            Bitmap close = createBitmap(UIUtils.getTintedVectorDrawable(mMusicService, R.drawable.ic_close_white, secondary), 1f);

                            notificationLayout.setTextColor(R.id.title, primary);
                            notificationLayout.setTextColor(R.id.text, secondary);
                            notificationLayout.setImageViewBitmap(R.id.action_prev, prev);
                            notificationLayout.setImageViewBitmap(R.id.action_next, next);
                            notificationLayout.setImageViewBitmap(R.id.action_play_pause, playPause);

                            notificationLayoutBig.setTextColor(R.id.title, primary);
                            notificationLayoutBig.setTextColor(R.id.text, secondary);
                            notificationLayoutBig.setTextColor(R.id.text2, secondary);
                            notificationLayoutBig.setImageViewBitmap(R.id.action_prev, prev);
                            notificationLayoutBig.setImageViewBitmap(R.id.action_next, next);
                            notificationLayoutBig.setImageViewBitmap(R.id.action_play_pause, playPause);
                            notificationLayoutBig.setImageViewBitmap(R.id.action_quit, close);
                        }
                    });
        });
    }

    @Override
    public synchronized void stop() {
        stopped = true;
        mMusicService.stopForeground(true);
    }

    private void linkButtons(final RemoteViews notificationLayout, final RemoteViews notificationLayoutBig) {
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(mMusicService, PYMusicService.class);

        // Previous track
        pendingIntent = buildPendingIntent(mMusicService, PYMusicService.ACTION_NOTIFICATION_REWIND, serviceName);
        notificationLayout.setOnClickPendingIntent(R.id.action_prev, pendingIntent);
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_prev, pendingIntent);

        // Play and pause
        pendingIntent = buildPendingIntent(mMusicService, PYMusicService.ACTION_NOTIFICATION_TOGGLE_PAUSE, serviceName);
        notificationLayout.setOnClickPendingIntent(R.id.action_play_pause, pendingIntent);
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_play_pause, pendingIntent);

        // Next track
        pendingIntent = buildPendingIntent(mMusicService, PYMusicService.ACTION_NOTIFICATION_SKIP, serviceName);
        notificationLayout.setOnClickPendingIntent(R.id.action_next, pendingIntent);
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_next, pendingIntent);

        // Quit
        pendingIntent = buildPendingIntent(mMusicService, PYMusicService.ACTION_NOTIFICATION_QUIT, serviceName);
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_quit, pendingIntent);
    }

    private PendingIntent buildPendingIntent(Context context, final String action, final ComponentName serviceName) {
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(context, 0, intent, 0);
    }

    private static Bitmap createBitmap(Drawable drawable, float sizeMultiplier) {
        Bitmap bitmap = Bitmap.createBitmap((int) (drawable.getIntrinsicWidth() * sizeMultiplier), (int) (drawable.getIntrinsicHeight() * sizeMultiplier), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight());
        drawable.draw(c);
        return bitmap;
    }
}
