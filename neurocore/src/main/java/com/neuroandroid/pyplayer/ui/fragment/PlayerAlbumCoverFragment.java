package com.neuroandroid.pyplayer.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.AlbumCoverAdapter;
import com.neuroandroid.pyplayer.base.BaseMusicServiceFragment;
import com.neuroandroid.pyplayer.service.PYPlayerHelper;
import com.neuroandroid.pyplayer.widget.recyclerviewpager.RecyclerViewPager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by NeuroAndroid on 2017/5/12.
 */

public class PlayerAlbumCoverFragment extends BaseMusicServiceFragment implements RecyclerViewPager.OnPageChangedListener {
    @BindView(R.id.rv_album_cover)
    RecyclerViewPager mRvAlbumCover;
    Unbinder unbinder;

    private PlayerAlbumCoverCallBack mAlbumCoverCallBack;
    private int mCurrentPosition;
    private AlbumCoverAdapter mAlbumCoverAdapter;

    public void setAlbumCoverCallBack(PlayerAlbumCoverCallBack albumCoverCallBack) {
        mAlbumCoverCallBack = albumCoverCallBack;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_album_cover, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRvAlbumCover.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAlbumCoverAdapter = new AlbumCoverAdapter(getContext(), new ArrayList<>());
        mRvAlbumCover.setAdapter(mAlbumCoverAdapter);
        mRvAlbumCover.addOnPageChangedListener(this);
    }

    @Override
    public void onServiceConnected() {
        updatePlayingQueues();
    }

    private void updatePlayingQueues() {
        mAlbumCoverAdapter.replaceAll(PYPlayerHelper.getPlayingQueue());
        mRvAlbumCover.smoothScrollToPosition(PYPlayerHelper.getCurrentPosition());
    }

    @Override
    public void onPlayingMetaChanged() {
        if (PYPlayerHelper.getCurrentPosition() == mRvAlbumCover.getCurrentPosition()) {
            OnPageChanged(-1, mRvAlbumCover.getCurrentPosition());
        } else {
            mRvAlbumCover.smoothScrollToPosition(PYPlayerHelper.getCurrentPosition());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRvAlbumCover.removeOnPageChangedListener(this);
        unbinder.unbind();
    }

    private void notifyColorChanged(int color) {
        if (mAlbumCoverCallBack != null) mAlbumCoverCallBack.onColorChanged(color);
    }

    @Override
    public void OnPageChanged(int oldPosition, int newPosition) {
        mCurrentPosition = newPosition;
        int color = mAlbumCoverAdapter.getAlbumCoverColors()[newPosition];
        notifyColorChanged(color);
        if (newPosition != PYPlayerHelper.getCurrentPosition()) {
            PYPlayerHelper.playSongAt(newPosition);
        }
    }

    public interface PlayerAlbumCoverCallBack {
        void onColorChanged(int color);
    }
}
