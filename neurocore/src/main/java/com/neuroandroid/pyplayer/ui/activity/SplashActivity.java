package com.neuroandroid.pyplayer.ui.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.config.Constant;
import com.neuroandroid.pyplayer.utils.SPUtils;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.utils.ThemeUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.NoPaddingTextView;

/**
 * Created by Administrator on 2017/5/7.
 */

public class SplashActivity extends AppCompatActivity {
    private LinearLayout mLlSplash;
    private NoPaddingTextView mTvSplash;
    private ImageView mIvSplash;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mLlSplash = (LinearLayout) findViewById(R.id.ll_splash);
        mTvSplash = (NoPaddingTextView) findViewById(R.id.tv_splash);
        mIvSplash = (ImageView) findViewById(R.id.iv_splash);
        SystemUtils.myStatusBar(this);
        startAnim();

        int themeColor = UIUtils.getColor(ThemeUtils.getThemeColor(this));
        mIvSplash.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
        mTvSplash.setTextColor(themeColor);
    }

    private void startAnim() {
        AlphaAnimation aa = new AlphaAnimation(0.0f, 1);
        aa.setDuration(2000);
        aa.setFillAfter(true);
        aa.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                jumpNextPage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLlSplash.startAnimation(aa);
    }

    private void jumpNextPage() {
        boolean guide = SPUtils.getBoolean(this, Constant.IS_USER_GUIDE_SHOWED, false);
        if (guide) {
            UIUtils.toLayout(new Intent(this, MainActivity.class));
            finish();
        } else {
            // 显示引导页面
            UIUtils.toLayout(new Intent(this, GuideActivity.class));
            finish();
        }
    }
}
