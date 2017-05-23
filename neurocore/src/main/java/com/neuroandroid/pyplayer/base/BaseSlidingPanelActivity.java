package com.neuroandroid.pyplayer.base;

import android.annotation.SuppressLint;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.service.PYPlayerHelper;
import com.neuroandroid.pyplayer.ui.fragment.CardPlayerFragment;
import com.neuroandroid.pyplayer.ui.fragment.MiniPlayerFragment;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by NeuroAndroid on 2017/5/11.
 */

public class BaseSlidingPanelActivity extends BaseMusicServiceActivity implements SlidingUpPanelLayout.PanelSlideListener, CardPlayerFragment.PaletteColorCallback {
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingUpPanelLayout;

    private BasePlayerFragment mPlayerFragment;
    private MiniPlayerFragment mMiniPlayerFragment;
    /**
     * 最近任务列表的颜色
     */
    private int mTaskColor;

    @Override
    protected void initView() {
        super.initView();
        mPlayerFragment = new CardPlayerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_player_fragment_container, mPlayerFragment).commit();
        getSupportFragmentManager().executePendingTransactions();
        mMiniPlayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.mini_player_fragment);

        mSlidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSlidingUpPanelLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    onPanelSlide(mSlidingUpPanelLayout, 1);
                    onPanelExpanded(mSlidingUpPanelLayout);
                } else if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    onPanelCollapsed(mSlidingUpPanelLayout);
                } else {
                    mPlayerFragment.onHide();
                }
            }
        });
    }

    /**
     * 设置迷你播放器进度条颜色
     */
    public void setUpMiNiPlayerProgressColor(int themeColor) {
        if (mMiniPlayerFragment != null) mMiniPlayerFragment.setUpProgressColor(themeColor);
    }

    @Override
    protected void initListener() {
        mSlidingUpPanelLayout.addPanelSlideListener(this);
        mMiniPlayerFragment.getView().setOnClickListener(view -> expandPanel());
    }

    public void setAntiDragView(View antiDragView) {
        mSlidingUpPanelLayout.setAntiDragView(antiDragView);
    }

    public void setTouchEnabled(boolean touchEnabled) {
        mSlidingUpPanelLayout.setClickable(touchEnabled);
        mSlidingUpPanelLayout.setEnabled(touchEnabled);
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        hideBottomBar(PYPlayerHelper.getPlayingQueue().isEmpty());
    }

    /**
     * 获取滑动面板的状态
     * {@link SlidingUpPanelLayout.PanelState}
     */
    public SlidingUpPanelLayout.PanelState getPanelState() {
        return mSlidingUpPanelLayout == null ? null : mSlidingUpPanelLayout.getPanelState();
    }

    protected View wrapSlidingMusicPanel(@LayoutRes int resId) {
        @SuppressLint("InflateParams")
        View slidingMusicPanelLayout = getLayoutInflater().inflate(R.layout.sliding_music_panel_layout, null);
        ViewGroup contentContainer = ButterKnife.findById(slidingMusicPanelLayout, R.id.fl_content);
        getLayoutInflater().inflate(resId, contentContainer);
        return slidingMusicPanelLayout;
    }

    @Override
    public void onPanelSlide(View panel, @FloatRange(from = 0, to = 1) float slideOffset) {
        setMiniPlayerAlphaProgress(slideOffset);
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case COLLAPSED:
                onPanelCollapsed(panel);
                break;
            case EXPANDED:
                onPanelExpanded(panel);
                break;
            case ANCHORED:
                // this fixes a bug where the panel would get stuck for some reason
                collapsePanel();
                break;
        }
    }

    private void setMiniPlayerAlphaProgress(@FloatRange(from = 0, to = 1) float progress) {
        if (mMiniPlayerFragment.getView() == null) return;
        float alpha = 1 - progress;
        mMiniPlayerFragment.getView().setAlpha(alpha);
        // necessary to make the views below clickable
        mMiniPlayerFragment.getView().setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
    }

    /**
     * 当面板展开时的回调
     */
    public void onPanelExpanded(View panel) {
        int playerFragmentColor = mPlayerFragment.getPaletteColor();
        super.setTaskDescriptionColor(playerFragmentColor);

        mPlayerFragment.setMenuVisibility(true);
        mPlayerFragment.setUserVisibleHint(true);
        mPlayerFragment.onShow();
    }

    /**
     * 当面板收缩时的回调
     */
    public void onPanelCollapsed(View panel) {
        super.setTaskDescriptionColor(mTaskColor);

        mPlayerFragment.setMenuVisibility(false);
        mPlayerFragment.setUserVisibleHint(false);
        mPlayerFragment.onHide();
    }

    /**
     * 展开面板
     */
    public void expandPanel() {
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    /**
     * 收缩面板
     */
    public void collapsePanel() {
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void hideBottomBar(final boolean hide) {
        if (hide) {
            mSlidingUpPanelLayout.setPanelHeight(0);
            collapsePanel();
        } else {
            mSlidingUpPanelLayout.setPanelHeight((int) UIUtils.getDimen(R.dimen.y112));
        }
    }

    @Override
    public void onBackPressed() {
        if (!handleBackPress())
            super.onBackPressed();
    }

    /**
     * 处理返回事件
     */
    public boolean handleBackPress() {
        if (mSlidingUpPanelLayout.getPanelHeight() != 0 && mPlayerFragment.onBackPressed())
            return true;
        if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            collapsePanel();
            return true;
        }
        return false;
    }

    @Override
    public void onPaletteColorChanged() {
        if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            int paletteColor = mPlayerFragment.getPaletteColor();
            super.setTaskDescriptionColor(paletteColor);
        }
    }

    @Override
    public void setTaskDescriptionColor(@ColorInt int color) {
        this.mTaskColor = color;
        if (getPanelState() == null || getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.setTaskDescriptionColor(color);
        }
    }
}
