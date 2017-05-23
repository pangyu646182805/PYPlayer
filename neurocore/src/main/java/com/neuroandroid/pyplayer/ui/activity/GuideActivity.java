package com.neuroandroid.pyplayer.ui.activity;

import android.animation.ArgbEvaluator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.base.BaseActivity;
import com.neuroandroid.pyplayer.config.Constant;
import com.neuroandroid.pyplayer.utils.SPUtils;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.NoPaddingTextView;

import butterknife.BindView;

/**
 * Created by Administrator on 2017/5/7.
 */

public class GuideActivity extends BaseActivity {
    @BindView(R.id.vp_content)
    ViewPager mVpContent;
    @BindView(R.id.ll_root)
    LinearLayout mLlRoot;
    private ArgbEvaluator mArgbEvaluator;
    private int[] mGuideColors = {R.color.md_blue_grey_100, R.color.md_deep_purple_500, R.color.md_indigo_500};
    private int[] mGuideIcons = {R.mipmap.icon_web, R.mipmap.tutorial_queue_swipe_up, R.mipmap.tutorial_rearrange_queue};
    private String[] mGuideTitles = {"MusicPlayer", "播放队列", "播放队列"};
    private String[] mGuideDescs = {"欢迎使用PYPlayer，这是一款精致且简洁的Android音乐播放器",
            "上滑正在播放界面内的卡片即可展开播放队列", "通过拖动歌曲名前面的序列号来调整播放队列的顺序"};

    @Override
    protected View attachLayout() {
        return getLayoutInflater().inflate(R.layout.activity_guide, null);
    }

    @Override
    protected void initView() {
        SystemUtils.myStatusBar(GuideActivity.this);
        mArgbEvaluator = new ArgbEvaluator();
        mVpContent.setAdapter(new GuideAdapter());
    }

    @Override
    protected void initListener() {
        mVpContent.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                int color = (int) mArgbEvaluator.evaluate(positionOffset, UIUtils.getColor(mGuideColors[position % mGuideColors.length]),
                        UIUtils.getColor(mGuideColors[(position + 1) % mGuideColors.length]));
                mLlRoot.setBackgroundColor(color);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    SystemUtils.myStatusBar(GuideActivity.this);
                } else {
                    SystemUtils.setTranslateStatusBar(GuideActivity.this);
                }
            }
        });
    }

    private class GuideAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return mGuideColors.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemLayout = LayoutInflater.from(GuideActivity.this).inflate(R.layout.pager_guide, container, false);
            ImageView ivGuide = (ImageView) itemLayout.findViewById(R.id.iv_guide);
            NoPaddingTextView tvTitle = (NoPaddingTextView) itemLayout.findViewById(R.id.tv_title);
            NoPaddingTextView tvDescription = (NoPaddingTextView) itemLayout.findViewById(R.id.tv_description);
            Button btnStarted = (Button) itemLayout.findViewById(R.id.btn_started);
            NoPaddingTextView tvPosition = (NoPaddingTextView) itemLayout.findViewById(R.id.tv_position);
            ivGuide.setImageResource(mGuideIcons[position]);
            tvTitle.setText(mGuideTitles[position]);
            tvDescription.setText(mGuideDescs[position]);
            tvPosition.setText((position + 1) + "/" + mGuideIcons.length);
            if (position == getCount() - 1) {
                btnStarted.setVisibility(View.VISIBLE);
                btnStarted.setOnClickListener(v -> {
                    SPUtils.putBoolean(GuideActivity.this, Constant.IS_USER_GUIDE_SHOWED, true);
                    mIntent.setClass(GuideActivity.this, MainActivity.class);
                    UIUtils.toLayout(mIntent);
                    finish();
                });
            } else {
                btnStarted.setVisibility(View.INVISIBLE);
                btnStarted.setOnClickListener(null);
            }
            container.addView(itemLayout);
            return itemLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
