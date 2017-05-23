package com.neuroandroid.pyplayer.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.loader.SongLoader;

import java.util.ArrayList;

/**
 * Created by NeuroAndroid on 2017/5/15.
 */

public class MusicPlaybackQueueStore extends SQLiteOpenHelper {
    @Nullable
    private static MusicPlaybackQueueStore sInstance = null;
    public static final String DATABASE_NAME = "music_playback_state.db";
    public static final String PLAYING_QUEUE_TABLE_NAME = "playing_queue";
    private static final int VERSION = 3;

    public MusicPlaybackQueueStore(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase, PLAYING_QUEUE_TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PLAYING_QUEUE_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + PLAYING_QUEUE_TABLE_NAME);
        onCreate(db);
    }

    @NonNull
    public static synchronized MusicPlaybackQueueStore getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new MusicPlaybackQueueStore(context.getApplicationContext());
        }
        return sInstance;
    }

    private void createTable(@NonNull final SQLiteDatabase db, final String tableName) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(tableName);
        builder.append("(");

        builder.append(BaseColumns._ID);
        builder.append(" INT NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.TITLE);
        builder.append(" STRING NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.TRACK);
        builder.append(" INT NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.YEAR);
        builder.append(" INT NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.DURATION);
        builder.append(" LONG NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.DATA);
        builder.append(" STRING NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.DATE_MODIFIED);
        builder.append(" LONG NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.ALBUM_ID);
        builder.append(" INT NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.ALBUM);
        builder.append(" STRING NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.ARTIST_ID);
        builder.append(" INT NOT NULL,");

        builder.append(MediaStore.Audio.AudioColumns.ARTIST);
        builder.append(" STRING NOT NULL);");

        db.execSQL(builder.toString());
    }

    public synchronized void saveQueues(@NonNull final ArrayList<Song> playingQueue) {
        saveQueue(PLAYING_QUEUE_TABLE_NAME, playingQueue);
    }

    private synchronized void saveQueue(final String tableName, @NonNull final ArrayList<Song> queue) {
        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        try {
            database.delete(tableName, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        final int NUM_PROCESS = 20;
        int position = 0;
        while (position < queue.size()) {
            database.beginTransaction();
            try {
                for (int i = position; i < queue.size() && i < position + NUM_PROCESS; i++) {
                    Song song = queue.get(i);
                    ContentValues values = new ContentValues(4);

                    values.put(BaseColumns._ID, song.id);
                    values.put(MediaStore.Audio.AudioColumns.TITLE, song.title);
                    values.put(MediaStore.Audio.AudioColumns.TRACK, song.trackNumber);
                    values.put(MediaStore.Audio.AudioColumns.YEAR, song.year);
                    values.put(MediaStore.Audio.AudioColumns.DURATION, song.duration);
                    values.put(MediaStore.Audio.AudioColumns.DATA, song.data);
                    values.put(MediaStore.Audio.AudioColumns.DATE_MODIFIED, song.dateModified);
                    values.put(MediaStore.Audio.AudioColumns.ALBUM_ID, song.albumId);
                    values.put(MediaStore.Audio.AudioColumns.ALBUM, song.albumName);
                    values.put(MediaStore.Audio.AudioColumns.ARTIST_ID, song.artistId);
                    values.put(MediaStore.Audio.AudioColumns.ARTIST, song.artistName);

                    database.insert(tableName, null, values);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
                position += NUM_PROCESS;
            }
        }
    }

    @NonNull
    public ArrayList<Song> getSavedPlayingQueue() {
        return getQueue(PLAYING_QUEUE_TABLE_NAME);
    }

    @NonNull
    private ArrayList<Song> getQueue(@NonNull final String tableName) {
        Cursor cursor = getReadableDatabase().query(tableName, null,
                null, null, null, null, null);
        return SongLoader.getSongs(cursor);
    }
}
