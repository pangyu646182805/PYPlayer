package com.neuroandroid.pyplayer.ui.fragment;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.base.BaseFragment;
import com.neuroandroid.pyplayer.ui.activity.MainActivity;

/**
 * Created by NeuroAndroid on 2017/5/8.
 */

public class FoldersFragment extends BaseFragment implements MainActivity.MainActivityFragmentCallbacks {
    public static FoldersFragment newInstance() {
        return new FoldersFragment();
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.fragment_folders;
    }

    @Override
    protected void initView() {

    }

    @Override
    public boolean handleBackPress() {
        return false;
    }
}
