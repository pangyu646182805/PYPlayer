package com.neuroandroid.pyplayer.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.base.SelectAdapter;
import com.neuroandroid.pyplayer.bean.ISelect;
import com.neuroandroid.pyplayer.utils.ShowUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.LoadingLayout;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by NeuroAndroid on 2017/5/10.
 */

public abstract class BaseRecyclerViewFragment<ADAPTER extends SelectAdapter, LM extends RecyclerView.LayoutManager> extends BaseMusicServiceFragment {
    @Nullable
    @BindView(R.id.loading_layout)
    LoadingLayout mLoadingLayout;

    @Nullable
    @BindView(R.id.rv_music)
    RecyclerView mRvMusic;

    private ADAPTER mAdapter;
    private LM mLayoutManager;

    protected Context mContext;
    protected View mRootView;
    private Unbinder mUnbinder;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initLayoutManager();
        initAdapter();
        setUpRecyclerView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (useEventBus())
            EventBus.getDefault().register(this);
        initData();
        initListener();
    }

    private void initLayoutManager() {
        mLayoutManager = createLayoutManager();
    }

    private void initAdapter() {
        mAdapter = createAdapter();
        mAdapter.setSelectedMode(ISelect.MULTIPLE_MODE);
        mAdapter.updateSelectMode(false);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void setUpRecyclerView() {
        mRvMusic.setLayoutManager(new LinearLayoutManager(mContext));
        mRvMusic.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mContext)
                .sizeResId(R.dimen.y2).colorResId(R.color.split).build());

        RecyclerView.ItemAnimator animator = mRvMusic.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mRvMusic.getItemAnimator().setChangeDuration(333);
        mRvMusic.getItemAnimator().setMoveDuration(333);
        if (mRvMusic instanceof FastScrollRecyclerView) {
            // 设置scroll_bar颜色
            // ViewUtil.setUpFastScrollRecyclerViewColor(getActivity(), ((FastScrollRecyclerView) recyclerView), ThemeStore.accentColor(getActivity()));
        }
        mRvMusic.setLayoutManager(mLayoutManager);
        mRvMusic.setAdapter(mAdapter);
    }

    /**
     * 重新设置布局管理器
     */
    protected void invalidateLayoutManager() {
        initLayoutManager();
        mRvMusic.setLayoutManager(mLayoutManager);
    }

    /**
     * 重新设置适配器
     */
    protected void invalidateAdapter() {
        initAdapter();
        checkIsEmpty();
        mRvMusic.setAdapter(mAdapter);
    }

    private void checkIsEmpty() {
        if (mLoadingLayout != null) {
            if (mAdapter == null || mAdapter.getItemCount() == 0) {
                ShowUtils.showToast((mAdapter == null) + " : " + mAdapter.getItemCount());
                showError(() -> {
                });
            }
        }
    }

    @LayoutRes
    protected int attachLayoutRes() {
        return R.layout.fragment_base_recyclerview;
    }

    @StringRes
    protected int getEmptyMessage() {
        return R.string.empty;
    }

    /**
     * 是否使用EventBus
     */
    protected boolean useEventBus() {
        return false;
    }

    /**
     * 初始化视图控件
     */
    protected abstract void initView();

    protected abstract LM createLayoutManager();

    @NonNull
    protected abstract ADAPTER createAdapter();

    protected void initData() {
    }

    protected void initListener() {
    }

    protected ADAPTER getAdapter() {
        return mAdapter;
    }

    protected LM getLayoutManager() {
        return mLayoutManager;
    }

    protected RecyclerView getRecyclerView() {
        return mRvMusic;
    }

    public void showLoading() {
        if (mLoadingLayout != null) {
            mLoadingLayout.setStatus(LoadingLayout.STATUS_LOADING);
        }
    }

    public void showTip(String tip) {
    }

    public void hideLoading() {
        if (mLoadingLayout != null) {
            mLoadingLayout.hide();
        }
    }

    public void showError(LoadingLayout.OnRetryListener onRetryListener) {
        showError(onRetryListener, UIUtils.getString(getEmptyMessage()));
    }

    public void showError(LoadingLayout.OnRetryListener onRetryListener, String status) {
        if (mLoadingLayout != null) {
            mLoadingLayout.setStatus(LoadingLayout.STATUS_NO_NET, status);
            mLoadingLayout.setOnRetryListener(onRetryListener);
        }
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
        this.mUnbinder = null;
        this.mContext = null;
        this.mRootView = null;
    }
}
