package com.neuroandroid.pyplayer.listener;

import android.view.View;

/**
 * Created by NeuroAndroid on 2017/2/14.
 */

public interface OnItemClickListener<T> {
    void onItemClick(View view, int position, T t);
}
