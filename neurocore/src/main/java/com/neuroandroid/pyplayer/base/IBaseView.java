package com.neuroandroid.pyplayer.base;

import com.neuroandroid.pyplayer.widget.LoadingLayout;

/**
 * Created by NeuroAndroid on 2017/3/8.
 */

public interface IBaseView<T> {
    /**
     * 设置presenter
     * @param presenter
     */
    void setPresenter(T presenter);

    /**
     * 显示加载动画
     */
    void showLoading();

    /**
     * 隐藏加载
     */
    void hideLoading();

    /**
     * 显示网络错误
     */
    void showError(LoadingLayout.OnRetryListener onRetryListener);

    void showTip(String tip);
}
