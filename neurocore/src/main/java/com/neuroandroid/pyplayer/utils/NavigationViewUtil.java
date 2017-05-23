package com.neuroandroid.pyplayer.utils;

import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;

/**
 * Created by NeuroAndroid on 2017/5/22.
 */

public class NavigationViewUtil {
    public static void setItemIconColors(@NonNull NavigationView navigationView, @ColorInt int normalColor, @ColorInt int selectedColor) {
        ColorStateList iconSl = new ColorStateList(new int[][]{{-16842912}, {16842912}}, new int[]{normalColor, selectedColor});
        navigationView.setItemIconTintList(iconSl);
    }

    public static void setItemTextColors(@NonNull NavigationView navigationView, @ColorInt int normalColor, @ColorInt int selectedColor) {
        ColorStateList textSl = new ColorStateList(new int[][]{{-16842912}, {16842912}}, new int[]{normalColor, selectedColor});
        navigationView.setItemTextColor(textSl);
    }

    private NavigationViewUtil() {
    }
}
