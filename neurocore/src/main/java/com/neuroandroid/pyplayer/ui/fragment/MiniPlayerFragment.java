package com.neuroandroid.pyplayer.ui.fragment;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.base.BaseMusicServiceFragment;
import com.neuroandroid.pyplayer.base.BaseSlidingPanelActivity;
import com.neuroandroid.pyplayer.helper.MusicProgressUpdateHelper;
import com.neuroandroid.pyplayer.listener.PlayPauseClickListener;
import com.neuroandroid.pyplayer.service.PYPlayerHelper;
import com.neuroandroid.pyplayer.utils.ThemeUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.IconImageView;
import com.neuroandroid.pyplayer.widget.PlayPauseDrawable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by NeuroAndroid on 2017/5/8.
 */

public class MiniPlayerFragment extends BaseMusicServiceFragment implements MusicProgressUpdateHelper.ProgressUpdateCallBack {
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.iv_play_pause)
    IconImageView mIvPlayPause;
    @BindView(R.id.progress_bar)
    MaterialProgressBar mProgressBar;
    private Unbinder mUnbinder;

    private MusicProgressUpdateHelper mProgressUpdateHelper;
    private PlayPauseDrawable mPlayPauseDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressUpdateHelper = new MusicProgressUpdateHelper(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mIvPlayPause.setImageDrawable(new PlayPauseDrawable(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpMiniPlayer();
        setUpProgressColor(ThemeUtils.getThemeColor(getContext()));
    }

    public void setUpProgressColor(int themeColor) {
        if (themeColor == R.color.white) {
            themeColor = UIUtils.getColor(R.color.black);
        } else {
            themeColor = UIUtils.getColor(themeColor);
        }
        mProgressBar.setProgressTintList(ColorStateList.valueOf(themeColor));
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

    private void setUpMiniPlayer() {
        mPlayPauseDrawable = new PlayPauseDrawable(getContext());
        mPlayPauseDrawable.setColorFilter(UIUtils.getColor(R.color.colorGray333), PorterDuff.Mode.SRC_IN);
        mIvPlayPause.setImageDrawable(mPlayPauseDrawable);
        mIvPlayPause.setOnClickListener(new PlayPauseClickListener() {
            @Override
            public void onClick(View view) {
                if (!UIUtils.getString(R.string.app_name).equals(mTvTitle.getText().toString())) {
                    super.onClick(view);
                }
            }
        });
    }

    @Override
    public void onServiceConnected() {
        updateSongTitle();
        updatePlayPauseState(false);
    }

    @Override
    public void onPlayingMetaChanged() {
        updateSongTitle();
    }

    @Override
    public void onPlayStateChanged() {
        updatePlayPauseState(true);
    }

    @Override
    public void onUpdateProgressViews(int progress, int total) {
        mProgressBar.setMax(total);
        mProgressBar.setProgress(progress);
    }

    /**
     * 更新播放暂停按钮
     *
     * @param anim 是否执行动画
     */
    public void updatePlayPauseState(boolean anim) {
        if (PYPlayerHelper.isPlaying()) {
            mPlayPauseDrawable.setPause(anim);
        } else {
            mPlayPauseDrawable.setPlay(anim);
        }
    }

    private void updateSongTitle() {
        String title = PYPlayerHelper.getCurrentSong().title;
        if (UIUtils.isEmpty(title)) {
            title = UIUtils.getString(R.string.app_name);
            if ((getActivity() instanceof BaseSlidingPanelActivity)) {
                BaseSlidingPanelActivity slidingPanelActivity = (BaseSlidingPanelActivity) getActivity();
                slidingPanelActivity.setTouchEnabled(false);
            }
        } else {
            if ((getActivity() instanceof BaseSlidingPanelActivity)) {
                BaseSlidingPanelActivity slidingPanelActivity = (BaseSlidingPanelActivity) getActivity();
                slidingPanelActivity.setTouchEnabled(true);
            }
        }
        mTvTitle.setText(title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
