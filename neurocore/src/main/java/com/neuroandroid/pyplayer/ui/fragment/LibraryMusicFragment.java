package com.neuroandroid.pyplayer.ui.fragment;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.PopupMenu;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.MusicLibraryPagerAdapter;
import com.neuroandroid.pyplayer.base.BaseFragment;
import com.neuroandroid.pyplayer.base.BaseRecyclerViewGridSizeFragment;
import com.neuroandroid.pyplayer.bean.Album;
import com.neuroandroid.pyplayer.bean.Artist;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.event.BaseEvent;
import com.neuroandroid.pyplayer.event.SelectedEvent;
import com.neuroandroid.pyplayer.event.ThemeEvent;
import com.neuroandroid.pyplayer.ui.activity.MainActivity;
import com.neuroandroid.pyplayer.utils.ShowUtils;
import com.neuroandroid.pyplayer.utils.ThemeUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.TitleBar;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

/**
 * Created by NeuroAndroid on 2017/5/8.
 */

public class LibraryMusicFragment extends BaseFragment implements MainActivity.MainActivityFragmentCallbacks {
    @BindView(R.id.status_bar)
    View mStatusBar;
    @BindView(R.id.tabs)
    TabLayout mTabs;
    @BindView(R.id.appbar)
    AppBarLayout mAppbar;
    @BindView(R.id.vp_content)
    ViewPager mVpContent;
    private MusicLibraryPagerAdapter mMusicLibraryPagerAdapter;
    private TitleBar.ImageAction mMenuAction;
    private TitleBar.ImageAction mCloseAction;
    private boolean mSelectedMenuOpen;
    /**
     * 记录上一个页面的position
     */
    private int mPrePosition;
    private TitleBar.ImageAction mSpinnerAction;
    private TitleBar.ImageAction mDeleteAction;
    private TitleBar.ImageAction mRedoAction;
    private TitleBar.ImageAction mAddAction;
    private TitleBar.ImageAction mPlayListAction;
    private PopupMenu mPopupMenu;

    public static LibraryMusicFragment newInstance() {
        return new LibraryMusicFragment();
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.fragment_library_music;
    }

    @Override
    protected void initView() {
        initTitleBar(UIUtils.getString(R.string.app_name), false);
        initImageAction(ThemeUtils.isDarkStatusBar(mContext));
        initLeftAction(mMenuAction);
        initRightAction(mSpinnerAction);
        setStatusBar(mStatusBar);
        setUpViewPager();
        setThemeColor(ThemeUtils.getThemeColor(mContext), ThemeUtils.isDarkStatusBar(mContext));
    }

    private void initImageAction(boolean darkStatusBar) {
        mMenuAction = new TitleBar.ImageAction(darkStatusBar ? R.drawable.ic_menu_black : R.drawable.ic_menu_white) {
            @Override
            public void performAction(View view) {
                getMainActivity().openDrawer();
            }
        };
        mCloseAction = new TitleBar.ImageAction(darkStatusBar ? R.drawable.ic_close_black : R.drawable.ic_close_white) {
            @Override
            public void performAction(View view) {
                if (mSelectedMenuOpen) {
                    mSelectedMenuOpen = false;
                    closeSelectedMenu();
                    clearSelected();
                }
            }
        };
        mSpinnerAction = new TitleBar.ImageAction(darkStatusBar ? R.drawable.ic_more_vert_black : R.drawable.ic_more_vert_white) {
            @Override
            public void performAction(View view) {
                mPopupMenu = new PopupMenu(mContext, view);
                mPopupMenu.inflate(R.menu.menu_main);
                mPopupMenu.setOnMenuItemClickListener(menuItem -> {
                    BaseRecyclerViewGridSizeFragment recyclerViewGridSizeFragment = (BaseRecyclerViewGridSizeFragment) getCurrentFragment();
                    if (menuItem.getItemId() == R.id.action_colored_footers) {
                        menuItem.setChecked(!menuItem.isChecked());
                        recyclerViewGridSizeFragment.setAndSaveUsePalette(menuItem.isChecked());
                        return true;
                    }
                    if (handleGridSizeMenuItem(recyclerViewGridSizeFragment, menuItem)) {
                        return true;
                    }
                    switch (menuItem.getItemId()) {
                        case R.id.action_sleep_timer:
                            ShowUtils.showToast("SET_SLEEP_TIMER");
                            return true;
                        case R.id.action_equalizer:
                            ShowUtils.showToast("均衡器");
                            return true;
                        case R.id.action_shuffle_all:
                            ShowUtils.showToast("随机播放所有歌曲");
                            return true;
                    }
                    return false;
                });
                Menu menu = mPopupMenu.getMenu();
                BaseRecyclerViewGridSizeFragment recyclerViewGridSizeFragment = (BaseRecyclerViewGridSizeFragment) getCurrentFragment();
                MenuItem gridSizeItem = menu.findItem(R.id.action_grid_size);
                setUpGridSize(recyclerViewGridSizeFragment, gridSizeItem.getSubMenu());
                menu.findItem(R.id.action_colored_footers).setChecked(recyclerViewGridSizeFragment.usePalette());
                menu.findItem(R.id.action_colored_footers).setEnabled(recyclerViewGridSizeFragment.canUsePalette());
                mPopupMenu.show();
            }
        };
        mDeleteAction = new TitleBar.ImageAction(darkStatusBar ? R.drawable.ic_delete_black : R.drawable.ic_delete_white) {
            @Override
            public void performAction(View view) {
                ShowUtils.showToast("delete");
            }
        };
        mRedoAction = new TitleBar.ImageAction(darkStatusBar ? R.drawable.ic_redo_black : R.drawable.ic_redo_white) {
            @Override
            public void performAction(View view) {
                ShowUtils.showToast("mRedoAction");
            }
        };
        mAddAction = new TitleBar.ImageAction(darkStatusBar ? R.drawable.ic_library_add_black : R.drawable.ic_library_add_white_24dp) {
            @Override
            public void performAction(View view) {
                ShowUtils.showToast("mAddAction");
            }
        };
        mPlayListAction = new TitleBar.ImageAction(darkStatusBar ? R.drawable.ic_playlist_add_black : R.drawable.ic_playlist_add_white) {
            @Override
            public void performAction(View view) {
                ShowUtils.showToast("mPlayListAction");
            }
        };
    }

    private boolean handleGridSizeMenuItem(@NonNull BaseRecyclerViewGridSizeFragment recyclerViewGridSizeFragment,
                                           @NonNull MenuItem menuItem) {
        int gridSize = 0;
        switch (menuItem.getItemId()) {
            case R.id.action_grid_size_1:
                gridSize = 1;
                break;
            case R.id.action_grid_size_2:
                gridSize = 2;
                break;
            case R.id.action_grid_size_3:
                gridSize = 3;
                break;
            case R.id.action_grid_size_4:
                gridSize = 4;
                break;
        }
        if (gridSize > 0) {
            menuItem.setChecked(true);
            recyclerViewGridSizeFragment.setAndSaveGridSize(gridSize);
            mPopupMenu.getMenu().findItem(R.id.action_colored_footers).setEnabled(recyclerViewGridSizeFragment.canUsePalette());
            return true;
        }
        return false;
    }

    /**
     * 设置SubMenu网格大小
     */
    private void setUpGridSize(BaseRecyclerViewGridSizeFragment recyclerViewGridSizeFragment, SubMenu subMenu) {
        switch (recyclerViewGridSizeFragment.getGridSize()) {
            case 1:
                subMenu.findItem(R.id.action_grid_size_1).setChecked(true);
                break;
            case 2:
                subMenu.findItem(R.id.action_grid_size_2).setChecked(true);
                break;
            case 3:
                subMenu.findItem(R.id.action_grid_size_3).setChecked(true);
                break;
            case 4:
                subMenu.findItem(R.id.action_grid_size_4).setChecked(true);
                break;
        }
    }

    /**
     * ViewPager的相关设置
     */
    private void setUpViewPager() {
        mMusicLibraryPagerAdapter = new MusicLibraryPagerAdapter(getChildFragmentManager(), mContext);
        mVpContent.setAdapter(mMusicLibraryPagerAdapter);
        mVpContent.setOffscreenPageLimit(mMusicLibraryPagerAdapter.getCount() - 1);
        mTabs.setupWithViewPager(mVpContent);
    }

    @Override
    protected void initListener() {
        // ViewPager滑动监听
        mVpContent.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // 如果选择菜单已经打开
                if (mSelectedMenuOpen) {
                    mSelectedMenuOpen = false;
                    // 关闭选择菜单
                    closeSelectedMenu();
                    // 清理上一个页面的选择项
                    clearSelected(mPrePosition);
                }
            }
        });
    }

    /**
     * 返回当前Fragment
     */
    public Fragment getCurrentFragment() {
        return getFragment(mVpContent.getCurrentItem());
    }

    /**
     * 根据position返回Fragment
     */
    public Fragment getFragment(int position) {
        return mMusicLibraryPagerAdapter.getFragment(position);
    }

    /**
     * 是否使用EventBus
     * 本类中EventBus主要用于接受多选模式下的消息
     * {@link LibraryMusicFragment#onEvent(BaseEvent)}
     * {@link LibraryMusicFragment#handleSelectedEvent(int, String)}
     */
    @Override
    protected boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BaseEvent baseEvent) {
        if (baseEvent != null) {
            if (baseEvent.getEventFlag() == BaseEvent.EVENT_SELECTED_MODE) {
                SelectedEvent selectedEvent = (SelectedEvent) baseEvent;
                int size;
                switch (mVpContent.getCurrentItem()) {
                    case 0:
                        List<Song> songs = selectedEvent.getSelectedBeans();
                        size = songs.size();
                        handleSelectedEvent(size, size <= 0 ? null : songs.get(0).title);
                        break;
                    case 1:
                        List<Album> albums = selectedEvent.getSelectedBeans();
                        size = albums.size();
                        handleSelectedEvent(size, size <= 0 ? null : albums.get(0).getTitle());
                        break;
                    case 2:
                        List<Artist> artists = selectedEvent.getSelectedBeans();
                        size = artists.size();
                        handleSelectedEvent(size, size <= 0 ? null : artists.get(0).getName());
                        break;
                }
            } else if (baseEvent.getEventFlag() == BaseEvent.EVENT_THEME_COLOR) {
                ThemeEvent themeEvent = (ThemeEvent) baseEvent;
                setThemeColor(themeEvent.getThemeColor(), themeEvent.isDarkStatusBar());
                if (getMainActivity() != null) {
                    getMainActivity().setThemeColor(themeEvent.getThemeColor(), themeEvent.isDarkStatusBar());
                    getMainActivity().setUpMiNiPlayerProgressColor(themeEvent.getThemeColor());
                    getMainActivity().setTaskDescriptionColor(UIUtils.getColor(themeEvent.getThemeColor()));
                }
            }
        }
    }

    /**
     * 处理多选事件
     */
    private void handleSelectedEvent(int selectedSize, String selectedSizeIsOneTitle) {
        if (selectedSize <= 0) {
            if (mSelectedMenuOpen) {
                mSelectedMenuOpen = false;
                closeSelectedMenu();
            }
            getTitleBar().setTitle(UIUtils.getString(R.string.app_name));
        } else if (selectedSize == 1) {
            if (!mSelectedMenuOpen) {
                mSelectedMenuOpen = true;
                mPrePosition = mVpContent.getCurrentItem();
                openSelectedMenu();
            }
            getTitleBar().setTitle(selectedSizeIsOneTitle);
        } else {
            getTitleBar().setTitle(UIUtils.getString(R.string.x_selected, selectedSize));
        }
    }

    /**
     * 打开选择的菜单
     */
    private void openSelectedMenu() {
        getTitleBar().removeLeftAction(mMenuAction);
        getTitleBar().removeRightAction(mSpinnerAction);
        initLeftAction(mCloseAction);
        initRightAction(mRedoAction);
        initRightAction(mAddAction);
        initRightAction(mPlayListAction);
        initRightAction(mDeleteAction);
    }

    /**
     * 关闭选择的菜单
     */
    private void closeSelectedMenu() {
        getTitleBar().removeLeftAction(mCloseAction);
        getTitleBar().removeRightAction(mRedoAction);
        getTitleBar().removeRightAction(mAddAction);
        getTitleBar().removeRightAction(mPlayListAction);
        getTitleBar().removeRightAction(mDeleteAction);
        initLeftAction(mMenuAction);
        initRightAction(mSpinnerAction);
    }

    /**
     * 清除当前Fragment的选择项
     */
    private void clearSelected() {
        clearSelected(mVpContent.getCurrentItem());
    }

    /**
     * 清除指定Fragment的选择项
     */
    private void clearSelected(int position) {
        getTitleBar().setTitle(UIUtils.getString(R.string.app_name));
        Fragment currentFragment = getFragment(position);
        if (currentFragment instanceof SongsFragment) {
            SongsFragment songsFragment = (SongsFragment) currentFragment;
            songsFragment.clearSelected();
        } else if (currentFragment instanceof AlbumsFragment) {
            AlbumsFragment albumsFragment = (AlbumsFragment) currentFragment;
            albumsFragment.clearSelected();
        } else {
            ArtistsFragment artistsFragment = (ArtistsFragment) currentFragment;
            artistsFragment.clearSelected();
        }
    }

    /**
     * 处理返回事件
     */
    @Override
    public boolean handleBackPress() {
        if (mSelectedMenuOpen) {
            mSelectedMenuOpen = false;
            closeSelectedMenu();
            clearSelected();
            return true;
        }
        return false;
    }

    /**
     * 设置主题颜色
     */
    private void setThemeColor(int themeColor, boolean darkStatusBar) {
        themeColor = UIUtils.getColor(themeColor);
        ThemeUtils.setBackgroundColor(mStatusBar, themeColor);
        ThemeUtils.setBackgroundColor(mAppbar, themeColor);
        ThemeUtils.setBackgroundColor(getTitleBar(), themeColor);
        if (darkStatusBar) {
            getTitleBar().setTextColor(Color.BLACK);
            mTabs.setTabTextColors(Color.parseColor("#aa000000"), Color.parseColor("#000000"));
            mTabs.setSelectedTabIndicatorColor(Color.BLACK);
        } else {
            getTitleBar().setTextColor(Color.WHITE);
            mTabs.setTabTextColors(Color.parseColor("#aaffffff"), Color.parseColor("#ffffff"));
            mTabs.setSelectedTabIndicatorColor(Color.WHITE);
        }
        getTitleBar().removeLeftAction(mMenuAction);
        getTitleBar().removeRightAction(mSpinnerAction);
        initImageAction(darkStatusBar);
        initLeftAction(mMenuAction);
        initRightAction(mSpinnerAction);
    }
}
