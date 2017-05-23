package com.neuroandroid.pyplayer.ui.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.base.BaseMusicServiceFragment;
import com.neuroandroid.pyplayer.helper.MusicProgressUpdateHelper;
import com.neuroandroid.pyplayer.listener.PlayPauseClickListener;
import com.neuroandroid.pyplayer.listener.SimpleOnSeekBarChangeListener;
import com.neuroandroid.pyplayer.service.PYMusicService;
import com.neuroandroid.pyplayer.service.PYPlayerHelper;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.utils.MusicUtil;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.PlayPauseDrawable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by NeuroAndroid on 2017/5/12.
 */

public class CardPlayerPlaybackControlsFragment extends BaseMusicServiceFragment implements MusicProgressUpdateHelper.ProgressUpdateCallBack {
    @BindView(R.id.fab_play_pause)
    FloatingActionButton mFabPlayPause;
    @BindView(R.id.tv_current_progress)
    TextView mTvCurrentProgress;
    @BindView(R.id.tv_total_time)
    TextView mTvTotalTime;
    @BindView(R.id.seek_bar_player_progress)
    SeekBar mSeekBarPlayerProgress;
    @BindView(R.id.btn_pre)
    ImageButton mBtnPre;
    @BindView(R.id.btn_next)
    ImageButton mBtnNext;
    @BindView(R.id.btn_repeat)
    ImageButton mBtnRepeat;
    @BindView(R.id.btn_shuffle)
    ImageButton mBtnShuffle;
    private Unbinder mUnbinder;
    private PlayPauseDrawable mPlayPauseDrawable;

    private int mLastPlaybackControlsColor;
    private int mLastDisablePlaybackControlsColor;

    private MusicProgressUpdateHelper mProgressUpdateHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressUpdateHelper = new MusicProgressUpdateHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_player_playback_controls, null);
        mUnbinder = ButterKnife.bind(this, view);
        initView();
        initListener();
        return view;
    }

    private void initView() {
        mPlayPauseDrawable = new PlayPauseDrawable(getContext());
        mPlayPauseDrawable.setColorFilter(UIUtils.getColor(R.color.colorGray333), PorterDuff.Mode.SRC_IN);
        mFabPlayPause.setImageDrawable(mPlayPauseDrawable);
        mFabPlayPause.post(() -> {
            if (mFabPlayPause != null) {
                mFabPlayPause.setPivotX(mFabPlayPause.getWidth() / 2);
                mFabPlayPause.setPivotY(mFabPlayPause.getHeight() / 2);
            }
        });
        updatePreAndNextButtonColor();
    }

    private void initListener() {
        mFabPlayPause.setOnClickListener(new PlayPauseClickListener());
        mBtnPre.setOnClickListener(view -> PYPlayerHelper.playPreviousSong());
        mBtnNext.setOnClickListener(view -> PYPlayerHelper.playNextSong());
        mBtnRepeat.setOnClickListener(view -> PYPlayerHelper.cyclePlayMode());
        mSeekBarPlayerProgress.setOnSeekBarChangeListener(new SimpleOnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    PYPlayerHelper.seekTo(progress);
                    onUpdateProgressViews(PYPlayerHelper.getSongProgressMillis(), PYPlayerHelper.getSongDurationMillis());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgressUpdateHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mProgressUpdateHelper.stop();
    }

    @Override
    public void onServiceConnected() {
        updatePlayPauseState(false);
        updatePlayMode();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseState(true);
    }

    @Override
    public void onRepeatModeChanged() {
        updatePlayMode();
    }

    /**
     * 更新播放器按钮的状态
     */
    public void setPlayerButtonColor(boolean dark) {
        mLastPlaybackControlsColor = ColorUtils.getPrimaryTextColor(getContext(), dark);
        mLastDisablePlaybackControlsColor = ColorUtils.getSecondaryTextColor(getContext(), dark);

        updatePlayMode();
        updatePreAndNextButtonColor();
    }

    /**
     * 更新播放暂停按钮
     * @param anim 是否执行动画
     */
    public void updatePlayPauseState(boolean anim) {
        if (PYPlayerHelper.isPlaying()) {
            mPlayPauseDrawable.setPause(anim);
        } else {
            mPlayPauseDrawable.setPlay(anim);
        }
    }

    /**
     * 更新播放模式按钮
     */
    private void updatePlayMode() {
        switch (PYPlayerHelper.getPlayMode()) {
            case PYMusicService.MODE_ALL_REPEAT:
                mBtnRepeat.setImageResource(R.drawable.ic_repeat_white);
                mBtnRepeat.setColorFilter(mLastDisablePlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case PYMusicService.MODE_ORDER:
                mBtnRepeat.setImageResource(R.drawable.ic_repeat_white);
                mBtnRepeat.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
            case PYMusicService.MODE_SINGLE_REPEAT:
                mBtnRepeat.setImageResource(R.drawable.ic_repeat_one_white);
                mBtnRepeat.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
                break;
        }
        mBtnShuffle.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    /**
     * 更新上一首/下一首按钮
     */
    private void updatePreAndNextButtonColor() {
        mBtnPre.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
        mBtnNext.setColorFilter(mLastPlaybackControlsColor, PorterDuff.Mode.SRC_IN);
    }

    /**
     * MiniPlayerFragment展开时
     * fab按钮执行动画
     */
    public void show() {
        mFabPlayPause.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(360f)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * MiniPlayerFragment收起时
     * fab按钮恢复状态
     */
    public void hide() {
        if (mFabPlayPause != null) {
            mFabPlayPause.setScaleX(0f);
            mFabPlayPause.setScaleY(0f);
            mFabPlayPause.setRotation(0f);
        }
    }

    /**
     * 更新进度条和文本
     */
    @Override
    public void onUpdateProgressViews(int progress, int total) {
        mSeekBarPlayerProgress.setMax(total);
        mSeekBarPlayerProgress.setProgress(progress);
        mTvTotalTime.setText(MusicUtil.getReadableDurationString(total));
        mTvCurrentProgress.setText(MusicUtil.getReadableDurationString(progress));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
