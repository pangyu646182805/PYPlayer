package com.neuroandroid.pyplayer.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.neuroandroid.pyplayer.listener.MusicServiceEventListener;

/**
 * Created by NeuroAndroid on 2017/5/10.
 */

public class BaseMusicServiceFragment extends Fragment implements MusicServiceEventListener {
    private BaseMusicServiceActivity mMusicServiceActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMusicServiceActivity = (BaseMusicServiceActivity) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must be an instance of " + BaseMusicServiceActivity.class.getSimpleName());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMusicServiceActivity.addMusicServiceEventListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMusicServiceActivity.removeMusicServiceEventListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMusicServiceActivity = null;
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {

    }

    @Override
    public void onPlayStateChanged() {

    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {

    }
}
