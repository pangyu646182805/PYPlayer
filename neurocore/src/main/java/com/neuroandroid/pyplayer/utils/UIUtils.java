package com.neuroandroid.pyplayer.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.neuroandroid.pyplayer.base.BaseApplication;

/**
 * Created by NeuroAndroid on 2017/2/8.
 */

public class UIUtils {
    public static Context getContext() {
        return BaseApplication.getContext();
    }

    public static Resources getResources() {
        return getContext().getResources();
    }

    /**
     * 得到String.xml中的字符串
     */
    public static String getString(int resId) {
        return getResources().getString(resId);
    }

    public static void toLayout(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    /**
     * 得到String.xml中的字符串,带占位符
     */
    public static String getString(int id, Object... formatArgs) {
        return getResources().getString(id, formatArgs);
    }

    public static float getDimen(int dimen) {
        return getResources().getDimension(dimen);
    }

    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    /**
     * 得到String.xml中的字符串数组
     */
    public static String[] getStringArr(int resId) {
        return getResources().getStringArray(resId);
    }

    /**
     * 得到colors.xml中的颜色
     */
    public static int getColor(int colorId) {
        return getResources().getColor(colorId);
    }

    /**
     * 得到应用程序的包名
     */
    public static String getPackageName() {
        return getContext().getPackageName();
    }

    public static Handler getHandler() {
        return BaseApplication.getHandler();
    }

    public static String getEditTextStr(EditText et) {
        return et.getText().toString();
    }

    public static String getTextViewStr(TextView tv) {
        return tv.getText().toString();
    }

    /**
     * @param tv
     * @param str  要设置的文本
     * @param emptyStr  要设置的文本为空时需要设置的文本
     */
    public static void setText(TextView tv, String str, String emptyStr) {
        tv.setText(isEmpty(str) ? emptyStr : str);
    }

    public static void setImage(ImageView iv, int resId) {
        iv.setImageResource(resId);
    }

    public static Drawable getTintedVectorDrawable(@NonNull Context context, @DrawableRes int id, @ColorInt int color) {
        return createTintedDrawable(getVectorDrawable(context.getResources(), id, context.getTheme()), color);
    }

    private static Drawable getVectorDrawable(@NonNull Resources res, @DrawableRes int resId, @Nullable Resources.Theme theme) {
        if (Build.VERSION.SDK_INT >= 21) {
            return res.getDrawable(resId, theme);
        }
        return VectorDrawableCompat.create(res, resId, theme);
    }

    @CheckResult
    @Nullable
    private static Drawable createTintedDrawable(@Nullable Drawable drawable, @ColorInt int color) {
        if(drawable == null) {
            return null;
        } else {
            drawable = DrawableCompat.wrap(drawable.mutate());
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            DrawableCompat.setTint(drawable, color);
            return drawable;
        }
    }
}
