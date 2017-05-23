package com.neuroandroid.pyplayer.ui.activity;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.ThemeColorAdapter;
import com.neuroandroid.pyplayer.base.BaseActivity;
import com.neuroandroid.pyplayer.bean.ISelect;
import com.neuroandroid.pyplayer.bean.ThemeColorBean;
import com.neuroandroid.pyplayer.event.ThemeEvent;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.utils.ThemeUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.NoPaddingTextView;
import com.neuroandroid.pyplayer.widget.TitleBar;
import com.neuroandroid.pyplayer.widget.dialog.ListDialog;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by NeuroAndroid on 2017/5/19.
 */

public class SettingActivity extends BaseActivity {
    @BindView(R.id.tv_color_title)
    NoPaddingTextView mTvColorTitle;
    @BindView(R.id.iv_theme_color)
    CircleImageView mIvThemeColor;
    @BindView(R.id.ll_theme_color)
    LinearLayout mLlThemeColor;
    @BindView(R.id.ll_dark_theme)
    LinearLayout mLlDarkTheme;
    @BindView(R.id.sw)
    SwitchCompat mSw;

    private ThemeColorBean mSelectedThemeColorBean;
    private TitleBar.ImageAction mBackAction;

    @Override
    protected View attachLayout() {
        return LayoutInflater.from(this).inflate(R.layout.activity_setting, null);
    }

    @Override
    protected void initView() {
        initTitleBar("设置");
        getImageAction(R.drawable.ic_arrow_back);
        initLeftAction(mBackAction);
        setThemeColor(ThemeUtils.getThemeColor(this), ThemeUtils.isDarkStatusBar(this));
    }

    private ColorStateList generateColorStateList(int[][] states, @ColorInt int color1, @ColorInt int color2) {
        return new ColorStateList(states, new int[]{color1, color2});
    }

    private void getImageAction(@DrawableRes int res) {
        mBackAction = new TitleBar.ImageAction(res) {
            @Override
            public void performAction(View view) {
                finish();
            }
        };
    }

    @Override
    protected void initData() {
        mSelectedThemeColorBean = new ThemeColorBean();
        mSelectedThemeColorBean.setThemeColor(ThemeUtils.getThemeColor(this));
    }

    @Override
    protected void initListener() {
        mLlThemeColor.setOnClickListener(view -> {
            ListDialog<ThemeColorAdapter, ThemeColorBean> listDialog = new ListDialog<>(this)
                    .setLayoutManager(new GridLayoutManager(this, 5))
                    .setSelectAdapter(new ThemeColorAdapter(this, ThemeUtils.generateThemeColorDataList(SettingActivity.this)),
                            new ISelect.OnItemSelectedListener<ThemeColorBean>() {
                                @Override
                                public void onItemSelected(View view, int position, boolean isSelected, ThemeColorBean themeColorBean) {
                                    if (isSelected) mSelectedThemeColorBean = themeColorBean;
                                }

                                @Override
                                public void onNothingSelected() {

                                }
                            });
            listDialog.setCustomTitle("主色调");
            listDialog.showTitle();
            listDialog.showButton();
            listDialog.setAdapterCheckedPos(ThemeUtils.getCheckedThemeColorPosition(SettingActivity.this));
            listDialog.setConfirmClickListener((dialog, v) -> {
                // getTitleBar().setBackgroundColor(UIUtils.getColor(mSelectedThemeColorBean.getThemeColor()));
                ThemeUtils.saveThemeColor(SettingActivity.this, mSelectedThemeColorBean.getThemeColor());
                ThemeUtils.saveDarkStatusBar(SettingActivity.this, mSelectedThemeColorBean.isDarkStatusBar());
                setThemeColor(mSelectedThemeColorBean.getThemeColor(), mSelectedThemeColorBean.isDarkStatusBar());
                EventBus.getDefault().post(new ThemeEvent().setThemeColor(mSelectedThemeColorBean.getThemeColor())
                        .setDarkStatusBar(mSelectedThemeColorBean.isDarkStatusBar()));
                dialog.dismiss();
            });
            listDialog.show();
        });
    }

    private void setThemeColor(int themeColor, boolean darkStatusBar) {
        setTaskDescriptionColor(UIUtils.getColor(themeColor));
        ThemeUtils.setBackgroundColor(getTitleBar(), UIUtils.getColor(themeColor));
        ThemeUtils.setImageResource(mIvThemeColor, themeColor);
        if (themeColor == R.color.white) {
            ThemeUtils.setTextColor(mTvColorTitle, UIUtils.getColor(R.color.black));
        } else {
            ThemeUtils.setTextColor(mTvColorTitle, UIUtils.getColor(themeColor));
        }
        if (darkStatusBar) {
            SystemUtils.myStatusBar(this);
            getTitleBar().removeLeftAction(mBackAction);
            getImageAction(R.drawable.ic_arrow_back_black);
            initLeftAction(mBackAction);
            getTitleBar().setTextColor(Color.BLACK);
        } else {
            SystemUtils.setTranslateStatusBar(this);
            getTitleBar().removeLeftAction(mBackAction);
            getImageAction(R.drawable.ic_arrow_back);
            initLeftAction(mBackAction);
            getTitleBar().setTextColor(Color.WHITE);
        }
        // 设置Switch
        int stateChecked = android.R.attr.state_checked;
        ColorStateList thumbTintList = generateColorStateList(new int[][]{{stateChecked}, {-stateChecked}},
                themeColor != R.color.white ? UIUtils.getColor(ThemeUtils.getThemeColor(this)) : Color.BLACK,
                UIUtils.getColor(R.color.backgroundPanel));
        mSw.setThumbTintList(thumbTintList);
        ColorStateList trackTintList = generateColorStateList(new int[][]{{stateChecked}, {-stateChecked}},
                themeColor != R.color.white ? ColorUtils.adjustAlpha(UIUtils.getColor(ThemeUtils.getThemeColor(this)), 0.5f) : ColorUtils.adjustAlpha(Color.BLACK, 0.5f),
                UIUtils.getColor(R.color.grayccc));
        mSw.setTrackTintList(trackTintList);
    }
}
