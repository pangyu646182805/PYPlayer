package com.neuroandroid.pyplayer.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.listener.MusicServiceEventListener;
import com.neuroandroid.pyplayer.ui.activity.MainActivity;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.LoadingLayout;
import com.neuroandroid.pyplayer.widget.TitleBar;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by NeuroAndroid on 2017/3/8.
 */
public abstract class BaseFragment<T extends IBasePresenter> extends Fragment implements IBaseView<T>, MusicServiceEventListener {
    @Nullable
    @BindView(R.id.loading_layout)
    LoadingLayout mLoadingLayout;

    @Nullable
    @BindView(R.id.title_bar)
    TitleBar mTitleBar;

    protected T mPresenter;
    protected Activity mActivity;
    protected Context mContext;
    protected View mRootView;
    private Unbinder mUnbinder;
    private boolean mIsMulti = false;

    /**
     * 获得全局的，防止使用getActivity()为空
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(attachLayoutRes(), null);
            mUnbinder = ButterKnife.bind(this, mRootView);
            initView();
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (useEventBus())
            EventBus.getDefault().register(this);
        if (getUserVisibleHint() && mRootView != null && !mIsMulti) {
        }
        initData();
        initListener();
    }

    /*@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser && isVisible() && mRootView != null && !mIsMulti) {
            mIsMulti = true;
            initData();
            initListener();
        } else {
            super.setUserVisibleHint(isVisibleToUser);
        }
    }*/

    @Override
    public void showLoading() {
        if (mLoadingLayout != null) {
            mLoadingLayout.setStatus(LoadingLayout.STATUS_LOADING);
        }
    }

    @Override
    public void showTip(String tip) {
    }

    @Override
    public void hideLoading() {
        if (mLoadingLayout != null) {
            mLoadingLayout.hide();
        }
    }

    @Override
    public void showError(LoadingLayout.OnRetryListener onRetryListener) {
        if (mLoadingLayout != null) {
            mLoadingLayout.setStatus(LoadingLayout.STATUS_NO_NET);
            mLoadingLayout.setOnRetryListener(onRetryListener);
        }
    }

    protected void initTitleBar(CharSequence title) {
        initTitleBar(title, true);
    }

    protected void initTitleBar(CharSequence title, boolean immersive) {
        if (mTitleBar != null) {
            if (immersive) {
                if (mActivity instanceof BaseActivity) {
                    BaseActivity baseActivity = (BaseActivity) mActivity;
                    mTitleBar.setImmersive(baseActivity.mImmersive);
                }
            }
            mTitleBar.setTextColor(Color.WHITE);
            mTitleBar.setTitle(title);
        }
    }

    protected void initLeftAction(TitleBar.Action action) {
        if (mTitleBar != null) {
            mTitleBar.addLeftAction(action);
        }
    }

    protected void initRightAction(TitleBar.Action action) {
        if (mTitleBar != null) {
            mTitleBar.addRightAction(action);
        }
    }

    protected void setBackgroundColor(int color) {
        if (getTitleBar() != null) {
            getTitleBar().setBackgroundColor(color);
        }
    }

    protected MainActivity getMainActivity() {
        if (mActivity != null && mActivity instanceof MainActivity) {
            return (MainActivity) mActivity;
        }
        return null;
    }

    protected TitleBar getTitleBar() {
        return mTitleBar;
    }

    @Override
    public void setPresenter(T presenter) {
        mPresenter = presenter;
    }

    /**
     * 是否使用EventBus
     */
    protected boolean useEventBus() {
        return false;
    }

    /**
     * 绑定布局文件
     */
    protected abstract int attachLayoutRes();

    /**
     * 初始化视图控件
     */
    protected abstract void initView();

    protected void initData() {
    }

    protected void initListener() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mUnbinder != Unbinder.EMPTY) mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放资源
        if (useEventBus()) EventBus.getDefault().unregister(this);
        if (mPresenter != null) mPresenter.onDestroy();
        this.mPresenter = null;
        this.mUnbinder = null;
        this.mActivity = null;
        this.mContext = null;
        this.mRootView = null;
    }

    /**
     * 设置状态栏的颜色
     *
     * @param statusBar 状态栏的颜色
     */
    protected void setStatusBar(View statusBar) {
        if (mActivity instanceof BaseActivity) {
            BaseActivity baseActivity = (BaseActivity) mActivity;
            if (baseActivity.mImmersive) {
                statusBar.getLayoutParams().height = SystemUtils.getStatusHeight(mActivity);
                statusBar.setBackgroundColor(UIUtils.getColor(R.color.colorPrimary));
            }
        }
    }

    @Override
    public void onPlayingMetaChanged() {

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
