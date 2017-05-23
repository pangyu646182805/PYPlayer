package com.neuroandroid.pyplayer.bean;

/**
 * Created by NeuroAndroid on 2017/5/19.
 */

public class ThemeColorBean implements ISelect {
    private int themeColor;
    private boolean selected;
    private boolean darkStatusBar;  // 暗色状态栏

    public boolean isDarkStatusBar() {
        return darkStatusBar;
    }

    public void setDarkStatusBar(boolean darkStatusBar) {
        this.darkStatusBar = darkStatusBar;
    }

    public int getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(int themeColor) {
        this.themeColor = themeColor;
    }

    @Override
    public String getText() {
        return null;
    }

    @Override
    public void setText(String text) {

    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
