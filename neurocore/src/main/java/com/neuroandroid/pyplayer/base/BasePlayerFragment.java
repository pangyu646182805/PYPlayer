package com.neuroandroid.pyplayer.base;

import android.content.Context;

import com.neuroandroid.pyplayer.listener.PaletteColorHolder;

/**
 * Created by NeuroAndroid on 2017/5/11.
 */

public abstract class BasePlayerFragment extends BaseMusicServiceFragment implements PaletteColorHolder {
    private PaletteColorCallback mPaletteColorCallback;

    public PaletteColorCallback getPaletteColorCallback() {
        return mPaletteColorCallback;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mPaletteColorCallback = (PaletteColorCallback) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.getClass().getSimpleName() + " must implement " + PaletteColorCallback.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mPaletteColorCallback = null;
    }

    public abstract void onShow();

    public abstract void onHide();

    public abstract boolean onBackPressed();

    public interface PaletteColorCallback {
        void onPaletteColorChanged();
    }

    protected BaseActivity getBaseActivity() {
        if (getActivity() instanceof BaseActivity) {
            return (BaseActivity) getActivity();
        }
        return null;
    }
}
