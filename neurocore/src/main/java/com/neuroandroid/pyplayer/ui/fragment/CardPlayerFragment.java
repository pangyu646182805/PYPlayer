package com.neuroandroid.pyplayer.ui.fragment;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.PlayingQueuesAdapter;
import com.neuroandroid.pyplayer.base.BasePlayerFragment;
import com.neuroandroid.pyplayer.base.BaseSlidingPanelActivity;
import com.neuroandroid.pyplayer.service.PYPlayerHelper;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.IconImageView;
import com.neuroandroid.pyplayer.widget.NoPaddingTextView;
import com.neuroandroid.pyplayer.widget.TitleBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by NeuroAndroid on 2017/5/11.
 */

public class CardPlayerFragment extends BasePlayerFragment implements PlayerAlbumCoverFragment.PlayerAlbumCoverCallBack, SlidingUpPanelLayout.PanelSlideListener {
    @BindView(R.id.player_shadow_title_bar)
    TitleBar mPlayerShadowTitleBar;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingLayout;
    @BindView(R.id.rv_playing_queues)
    RecyclerView mRvPlayingQueues;
    @BindView(R.id.cv_playing_queues)
    CardView mCvPlayingQueues;
    @BindView(R.id.color_background)
    View mBackgroundView;
    @BindView(R.id.tv_player_queue_sub_header)
    NoPaddingTextView mTvSubHeader;
    @BindView(R.id.rl_player_content)
    RelativeLayout mRlPlayerContent;
    @BindView(R.id.iv_img)
    ImageView mIvImg;
    @BindView(R.id.tv_title)
    NoPaddingTextView mTvTitle;
    @BindView(R.id.tv_sub_title)
    NoPaddingTextView mTvSubTitle;
    @BindView(R.id.iv_menu)
    IconImageView mIvMenu;
    private Unbinder mUnbinder;
    private int mLastPaletteColor = -1;

    private CardPlayerPlaybackControlsFragment mPlaybackControlsFragment;
    private PlayerAlbumCoverFragment mAlbumCoverFragment;
    private PlayingQueuesAdapter mPlayingQueuesAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_player, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpSubFragments();
        setUpRecyclerView();
        setUpSlidingLayout(view);
    }

    private void initView() {
        mIvImg.setScaleType(ImageView.ScaleType.CENTER);
        mIvImg.setColorFilter(UIUtils.getColor(R.color.colorGray333), PorterDuff.Mode.SRC_IN);
        mIvImg.setImageResource(R.drawable.ic_volume_up_white);
        CardView.LayoutParams params = (CardView.LayoutParams) mCvPlayingQueues.getLayoutParams();
        params.topMargin = SystemUtils.getStatusHeight(getActivity());
        mPlayerShadowTitleBar.setImmersive(getBaseActivity().mImmersive);
        mPlayerShadowTitleBar.addLeftAction(new TitleBar.ImageAction(R.drawable.ic_close_white) {
            @Override
            public void performAction(View view) {
                getActivity().onBackPressed();
            }
        });
        mPlayerShadowTitleBar.addRightAction(new TitleBar.ImageAction(R.mipmap.ic_more_vert_white_24dp) {
            @Override
            public void performAction(View view) {

            }
        });
    }

    /**
     * 初始化CardPlayerPlaybackControlsFragment和PlayerAlbumCoverFragment
     */
    private void setUpSubFragments() {
        mPlaybackControlsFragment = (CardPlayerPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        mAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);
        mAlbumCoverFragment.setAlbumCoverCallBack(this);
    }

    /**
     * 初始化播放列表
     */
    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRvPlayingQueues.setLayoutManager(layoutManager);
        mPlayingQueuesAdapter = new PlayingQueuesAdapter(getContext(), new ArrayList<>());
        mRvPlayingQueues.setAdapter(mPlayingQueuesAdapter);
        mRvPlayingQueues.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getContext())
                .sizeResId(R.dimen.y2).colorResId(R.color.split).build());
    }

    /**
     * 设置滑动面板
     */
    private void setUpSlidingLayout(View rootView) {
        mSlidingLayout.addPanelSlideListener(this);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int panelHeight = SystemUtils.getScreenHeight(getActivity()) - mRlPlayerContent.getHeight() + SystemUtils.getStatusHeight(getBaseActivity());
                // 设置滑动面板高度
                mSlidingLayout.setPanelHeight(panelHeight);
                // 拦截播放列表所在界面的滑动事件
                // 效果就是滑动二级菜单的时候不会对一级菜单的滑动造成影响
                ((BaseSlidingPanelActivity) getActivity()).setAntiDragView(mSlidingLayout.findViewById(R.id.fl_player_panel));
            }
        });
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        // 当音乐服务连接上的回调
        mPlayingQueuesAdapter.replaceAll(PYPlayerHelper.getPlayingQueue());
        updateCurrentSong();
    }

    @Override
    public void onPlayingMetaChanged() {
        updateCurrentSong();
    }

    @Override
    public int getPaletteColor() {
        return mLastPaletteColor == -1 ? UIUtils.getColor(R.color.colorPrimary) : mLastPaletteColor;
    }

    @Override
    public void onShow() {
        mPlaybackControlsFragment.show();
    }

    @Override
    public void onHide() {
        mPlaybackControlsFragment.hide();
        onBackPressed();
    }

    private void updateCurrentSong() {
        mTvTitle.setText(PYPlayerHelper.getCurrentSong().title);
        mTvSubTitle.setText(PYPlayerHelper.getCurrentSong().artistName);
    }

    /**
     * 处理Fragment返回事件
     */
    @Override
    public boolean onBackPressed() {
        boolean wasExpanded = false;
        if (mSlidingLayout != null) {
            wasExpanded = mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        return wasExpanded;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSlidingLayout != null) mSlidingLayout.removePanelSlideListener(null);
        mUnbinder.unbind();
    }

    /**
     * 当音乐封面调色板颜色改变的回调
     */
    @Override
    public void onColorChanged(int color) {
        animateColorChange(color);
        mPlaybackControlsFragment.setPlayerButtonColor(ColorUtils.isColorLight(color));
        getPaletteColorCallback().onPaletteColorChanged();
    }

    /**
     * 改变背景颜色(是否需要圆形展开收起动画)
     */
    private void animateColorChange(final int newColor) {
        mLastPaletteColor = newColor;
        mBackgroundView.setBackgroundColor(newColor);
    }

    /**
     * 当面板滑动时的回调
     * 动态改变CardView的CardElevation(阴影)
     */
    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float density = getResources().getDisplayMetrics().density;
            mCvPlayingQueues.setCardElevation((6 * slideOffset + 2) * density);
            mPlaybackControlsFragment.mFabPlayPause.setElevation((2 * Math.max(0, (1 - (slideOffset * 16))) + 2) * density);
        }
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        if (newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
            // this fixes a bug where the panel would get stuck for some reason
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }
}
