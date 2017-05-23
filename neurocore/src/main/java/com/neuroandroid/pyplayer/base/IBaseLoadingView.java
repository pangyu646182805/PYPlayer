package com.neuroandroid.pyplayer.base;

/**
 * Created by NeuroAndroid on 2017/3/9.
 */

public abstract class IBaseLoadingView<T extends IBaseView> implements IBaseView<T> {
    abstract void loadData();

    abstract void loadMoreData();
}
