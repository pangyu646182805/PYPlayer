package com.neuroandroid.pyplayer.ui.activity;

import android.content.Intent;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.base.BaseSlidingPanelActivity;
import com.neuroandroid.pyplayer.ui.fragment.FoldersFragment;
import com.neuroandroid.pyplayer.ui.fragment.LibraryMusicFragment;
import com.neuroandroid.pyplayer.utils.NavigationViewUtil;
import com.neuroandroid.pyplayer.utils.ShowUtils;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.utils.ThemeUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import didikee.com.permissionshelper.PermissionsHelper;
import didikee.com.permissionshelper.permission.DangerousPermissions;

public class MainActivity extends BaseSlidingPanelActivity {
    /**
     * 媒体库
     */
    private static final int LIBRARY_MUSIC = 0;
    /**
     * 文件夹
     */
    private static final int LIBRARY_FOLDERS = 1;
    /**
     * 需要动态申请的权限
     */
    private static final String[] PERMISSIONS = new String[]{
            DangerousPermissions.STORAGE,
    };

    @BindView(R.id.fl_drawer_container)
    FrameLayout mFlDrawerContainer;
    @BindView(R.id.nav_layout)
    NavigationView mNavLayout;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Nullable
    MainActivityFragmentCallbacks mCurrentFragment;
    /**
     * 动态申请权限帮助类
     */
    private PermissionsHelper mPermissionsHelper;

    /**
     * 加载主页面
     */
    @Override
    protected View attachLayout() {
        View contentView = getLayoutInflater().inflate(R.layout.activity_main, null);
        ViewGroup drawerContent = ButterKnife.findById(contentView, R.id.fl_drawer_container);
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main_content));
        return contentView;
    }

    @Override
    protected void initView() {
        // 需要，父类有代码执行
        super.initView();
        setThemeColor(UIUtils.getColor(ThemeUtils.getThemeColor(this)), ThemeUtils.isDarkStatusBar(this));
        setTaskDescriptionColor(UIUtils.getColor(ThemeUtils.getThemeColor(this)));
    }

    @Override
    protected void initData() {
        mPermissionsHelper = new PermissionsHelper(this, PERMISSIONS);
        checkPermission();
    }

    /**
     * 初始化监听器
     */
    @Override
    protected void initListener() {
        super.initListener();
        // 侧滑菜单选择项监听
        mNavLayout.setNavigationItemSelectedListener(item -> {
            mDrawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.nav_library:
                    UIUtils.getHandler().postDelayed(() -> setChooser(LIBRARY_MUSIC), 150);
                    break;
                case R.id.nav_folders:
                    UIUtils.getHandler().postDelayed(() -> setChooser(LIBRARY_FOLDERS), 150);
                    break;
                case R.id.support_development:
                    ShowUtils.showToast("development");
                    break;
                case R.id.nav_settings:
                    UIUtils.getHandler().postDelayed(() -> {
                        mIntent.setClass(this, SettingActivity.class);
                        UIUtils.toLayout(mIntent);
                    }, 150);
                    break;
                case R.id.nav_about:
                    ShowUtils.showToast("about");
                    break;
            }
            return true;
        });
    }

    /**
     * 跳转到Fragment
     * {@link MainActivity#LIBRARY_MUSIC}
     * {@link MainActivity#LIBRARY_FOLDERS}
     */
    private void setChooser(int key) {
        switch (key) {
            case LIBRARY_MUSIC:
                mNavLayout.setCheckedItem(R.id.nav_library);
                setCurrentFragment(LibraryMusicFragment.newInstance());
                break;
            case LIBRARY_FOLDERS:
                mNavLayout.setCheckedItem(R.id.nav_folders);
                setCurrentFragment(FoldersFragment.newInstance());
                break;
        }
    }

    /**
     * 设置当前Fragment
     */
    private void setCurrentFragment(@SuppressWarnings("NullableProblems") Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, null).commit();
        mCurrentFragment = (MainActivityFragmentCallbacks) fragment;
    }

    /**
     * 打开侧滑菜单
     */
    public void openDrawer() {
        if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    /**
     * 检查有没有权限
     * 没有则去动态申请权限
     */
    private void checkPermission() {
        if (mPermissionsHelper.checkAllPermissions(PERMISSIONS)) {
            setChooser(LIBRARY_MUSIC);
            mPermissionsHelper.onDestroy();
        } else {
            // 申请权限
            mPermissionsHelper.startRequestNeedPermissions();
        }
        mPermissionsHelper.setonAllNeedPermissionsGrantedListener(new PermissionsHelper.onAllNeedPermissionsGrantedListener() {
            @Override
            public void onAllNeedPermissionsGranted() {
                ShowUtils.showToast("权限申请成功");
                setChooser(LIBRARY_MUSIC);
            }

            @Override
            public void onPermissionsDenied() {
                ShowUtils.showToast("权限申请失败");
                finish();
            }
        });
    }

    /**
     * 处理返回事件
     * {@link BaseSlidingPanelActivity#onBackPressed()}
     */
    @Override
    public boolean handleBackPress() {
        if (mDrawerLayout.isDrawerOpen(mNavLayout)) {
            mDrawerLayout.closeDrawers();
            return true;
        }
        return super.handleBackPress() || (mCurrentFragment != null && mCurrentFragment.handleBackPress());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPermissionsHelper.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 处理fragment返回事件
     */
    public interface MainActivityFragmentCallbacks {
        boolean handleBackPress();
    }

    /**
     * 设置主题颜色
     */
    public void setThemeColor(@ColorInt int themeColor, boolean darkStatusBar) {
        NavigationViewUtil.setItemIconColors(mNavLayout, UIUtils.getColor(R.color.colorGray333), themeColor);
        NavigationViewUtil.setItemTextColors(mNavLayout, UIUtils.getColor(R.color.colorGray333), themeColor);
        if (darkStatusBar) {
            SystemUtils.myStatusBar(this);
        } else {
            SystemUtils.setTranslateStatusBar(this);
        }
    }
}
