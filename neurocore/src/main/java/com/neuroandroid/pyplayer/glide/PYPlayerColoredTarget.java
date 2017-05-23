package com.neuroandroid.pyplayer.glide;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.glide.palette.BitmapPaletteTarget;
import com.neuroandroid.pyplayer.glide.palette.BitmapPaletteWrapper;
import com.neuroandroid.pyplayer.utils.PYPlayerColorUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;

public abstract class PYPlayerColoredTarget extends BitmapPaletteTarget {
    public PYPlayerColoredTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadFailed(Exception e, Drawable errorDrawable) {
        super.onLoadFailed(e, errorDrawable);
        onColorReady(getDefaultFooterColor());
    }

    @Override
    public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
        super.onResourceReady(resource, glideAnimation);
        onColorReady(PYPlayerColorUtils.getColor(resource.getPalette(), getDefaultFooterColor()));
    }

    protected int getDefaultFooterColor() {
        return UIUtils.getColor(R.color.colorTabItem);
    }

    public abstract void onColorReady(int color);
}
