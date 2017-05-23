package com.neuroandroid.pyplayer.utils;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.bean.ThemeColorBean;
import com.neuroandroid.pyplayer.config.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeuroAndroid on 2017/5/18.
 */

public class ThemeUtils {
    public static int[] THEME_COLOR_ARRAY = {R.color.theme_0, R.color.theme_1, R.color.theme_2,
            R.color.theme_3, R.color.theme_4, R.color.theme_5,
            R.color.theme_6, R.color.theme_7, R.color.theme_8,
            R.color.theme_9, R.color.theme_10, R.color.theme_11,
            R.color.theme_12, R.color.theme_13, R.color.theme_14,
            R.color.theme_15, R.color.theme_16, R.color.theme_17, R.color.white};

    /**
     * 获取app主题颜色
     * 默认是colorPrimary
     */
    @ColorRes
    public static int getThemeColor(Context context) {
        return SPUtils.getInt(context, Constant.APP_THEME_COLOR, R.color.theme_4);
    }

    public static int getCheckedThemeColorPosition(Context context) {
        int color = getThemeColor(context);
        // 默认为4
        int position = 4;
        for (int i = 0; i < THEME_COLOR_ARRAY.length; i++) {
            if (THEME_COLOR_ARRAY[i] == color) {
                position = i;
                break;
            }
        }
        return position;
    }

    /**
     * 保存app主题颜色
     */
    public static void saveThemeColor(Context context, @ColorRes int themeColor) {
        SPUtils.putInt(context, Constant.APP_THEME_COLOR, themeColor);
    }

    /**
     * 是否使用暗色状态栏
     */
    public static boolean isDarkStatusBar(Context context) {
        return SPUtils.getBoolean(context, Constant.DARK_STATUS_BAR, false);
    }

    public static void saveDarkStatusBar(Context context, boolean darkStatusBar) {
        SPUtils.putBoolean(context, Constant.DARK_STATUS_BAR, darkStatusBar);
    }

    /**
     * 是否是夜间模式
     */
    public static boolean isDarkTheme(Context context) {
        return SPUtils.getBoolean(context, Constant.DARK_THEME, false);
    }

    public static void saveDarkTheme(Context context, boolean darkTheme) {
        SPUtils.putBoolean(context, Constant.DARK_THEME, darkTheme);
    }

    public static List<ThemeColorBean> generateThemeColorDataList(Context context) {
        List<ThemeColorBean> dataList = new ArrayList<>();
        ThemeColorBean themeColorBean;
        int checkedThemeColorPosition = getCheckedThemeColorPosition(context);
        for (int i = 0; i < THEME_COLOR_ARRAY.length; i++) {
            themeColorBean = new ThemeColorBean();
            themeColorBean.setSelected(i == checkedThemeColorPosition);
            if (THEME_COLOR_ARRAY[i] == R.color.theme_10 || THEME_COLOR_ARRAY[i] == R.color.theme_11
                    || THEME_COLOR_ARRAY[i] == R.color.theme_12 || THEME_COLOR_ARRAY[i] == R.color.white) {
                themeColorBean.setDarkStatusBar(true);
            }
            themeColorBean.setThemeColor(THEME_COLOR_ARRAY[i]);
            dataList.add(themeColorBean);
        }
        return dataList;
    }

    public static void setBackgroundColor(View target, @ColorInt int themeColor) {
        if (target != null)
            target.setBackgroundColor(themeColor);
    }

    public static void setImageResource(ImageView target, @ColorRes int themeColor) {
        if (target != null)
            target.setImageResource(themeColor);
    }

    public static void setTextColor(TextView tv, @ColorInt int themeColor) {
        if (tv != null)
            tv.setTextColor(themeColor);
    }
}
