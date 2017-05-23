package com.neuroandroid.pyplayer.event;

/**
 * Created by NeuroAndroid on 2017/5/19.
 */

public class ThemeEvent extends BaseEvent {
    private int themeColor;
    private boolean darkStatusBar;
    private boolean darkTheme;

    public ThemeEvent() {
        setEventFlag(EVENT_THEME_COLOR);
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public ThemeEvent setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
        return this;
    }

    public boolean isDarkStatusBar() {
        return darkStatusBar;
    }

    public ThemeEvent setDarkStatusBar(boolean darkStatusBar) {
        this.darkStatusBar = darkStatusBar;
        return this;
    }

    public int getThemeColor() {
        return themeColor;
    }

    public ThemeEvent setThemeColor(int themeColor) {
        this.themeColor = themeColor;
        return this;
    }
}
