package com.neuroandroid.pyplayer.ui.fragment;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.SongAdapter;
import com.neuroandroid.pyplayer.base.BaseRecyclerViewGridSizeFragment;
import com.neuroandroid.pyplayer.base.BaseSlidingPanelActivity;
import com.neuroandroid.pyplayer.bean.ISelect;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.config.Constant;
import com.neuroandroid.pyplayer.event.SelectedEvent;
import com.neuroandroid.pyplayer.listener.LoaderIds;
import com.neuroandroid.pyplayer.loader.SongLoader;
import com.neuroandroid.pyplayer.misc.WrappedAsyncTaskLoader;
import com.neuroandroid.pyplayer.provider.MusicPlaybackQueueStore;
import com.neuroandroid.pyplayer.service.PYPlayerHelper;
import com.neuroandroid.pyplayer.utils.L;
import com.neuroandroid.pyplayer.utils.SPUtils;
import com.neuroandroid.pyplayer.utils.SetUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeuroAndroid on 2017/5/8.
 */
public class SongsFragment extends BaseRecyclerViewGridSizeFragment<SongAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Song>> {
    private static final int LOADER_ID = LoaderIds.SONGS_FRAGMENT;

    /**
     * 被选中的歌曲
     */
    private List<Song> mSelectedSongs = new ArrayList<>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showLoading();
        // 异步加载Songs列表
        getParentFragment().getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * 创建RecyclerView布局管理器
     */
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(mContext, getGridSize());
    }

    /**
     * 创建RecyclerView适配器
     */
    @NonNull
    @Override
    protected SongAdapter createAdapter() {
        boolean usePalette = loadUsePalette();
        List<Song> dataList = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataList();
        return new SongAdapter(mContext, dataList, getItemType(), usePalette);
    }

    /**
     * @return 返回列表为空时显示的信息
     */
    @Override
    protected int getEmptyMessage() {
        return R.string.no_songs;
    }

    /**
     * 当媒体库改变的时候重新异步加载Songs列表
     */
    @Override
    public void onMediaStoreChanged() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    /**
     * @return 返回网格布局的大小(保存在本地)
     */
    @Override
    protected int loadGridSize() {
        return SPUtils.getInt(mContext, Constant.SONG_GRID_SIZE, 1);
    }

    /**
     * @return 返回是否使用画板(保存在本地, 默认使用)
     */
    @Override
    protected boolean loadUsePalette() {
        return SPUtils.getBoolean(mContext, Constant.SONG_COLORED_FOOTERS, true);
    }

    /**
     * 保存网格大小到本地
     *
     * @param gridColumns 网格大小
     */
    @Override
    protected void saveGridSize(int gridColumns) {
        SPUtils.putInt(mContext, Constant.SONG_GRID_SIZE, gridColumns);
    }

    /**
     * 设置网格大小
     *
     * @param gridSize 网格大小
     */
    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyItemRangeChanged(0, getAdapter().getItemCount());
    }

    /**
     * 保存usePalette到本地
     *
     * @param usePalette 是否使用画板
     */
    @Override
    protected void saveUsePalette(boolean usePalette) {
        SPUtils.putBoolean(mContext, Constant.SONG_COLORED_FOOTERS, usePalette);
    }

    /**
     * 设置是否使用画板
     *
     * @param usePalette 是否使用画板
     */
    @Override
    protected void setUsePalette(boolean usePalette) {
        getAdapter().usePalette(usePalette);
    }

    @Override
    public Loader<ArrayList<Song>> onCreateLoader(int id, Bundle args) {
        return new AsyncSongLoader(mContext);
    }

    /**
     * 异步加载Songs列表结束
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<Song>> loader, ArrayList<Song> data) {
        hideLoading();
        getAdapter().replaceAll(data);
        invalidateListener();

        L.d("" + data.containsAll(MusicPlaybackQueueStore.getInstance(mContext).getSavedPlayingQueue()));
        ArrayList<Song> savedPlayingQueue = MusicPlaybackQueueStore.getInstance(mContext).getSavedPlayingQueue();
        if (!SetUtils.equals(savedPlayingQueue, data)) {
            L.e("有歌曲被添加或者删除，去保存歌曲");
            new AsyncSongSaver().execute(data);
        } else {
            L.e("不需要保存歌曲到数据库");
        }
    }

    @Override
    protected void invalidateListener() {
        getAdapter().setItemLongClickListener((view, position, song) -> {
            if (!getAdapter().isSelectMode()) {
                getAdapter().updateSelectMode(true, position);
            }
        });
        getAdapter().setItemSelectedListener(new ISelect.OnItemSelectedListener<Song>() {
            @Override
            public void onItemSelected(View view, int position, boolean isSelected, Song song) {
                if (isSelected) {
                    mSelectedSongs.add(song);
                } else {
                    mSelectedSongs.remove(song);
                }
                EventBus.getDefault().post(new SelectedEvent<Song>().setSelectedBeans(mSelectedSongs));
            }

            @Override
            public void onNothingSelected() {
                // 一个条目都没有被选择则取消多选模式
                getAdapter().updateSelectMode(false);
            }
        });
        getAdapter().setItemClickListener((view, position, song) -> PYPlayerHelper.playSongAt(position));
    }

    @Override
    public void clearSelected() {
        super.clearSelected();
        // 清除被选中的集合
        if (mSelectedSongs.size() > 0) mSelectedSongs.clear();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Song>> loader) {
        getAdapter().replaceAll(new ArrayList<>());
    }

    /**
     * 异步的Songs列表加载器
     */
    private static class AsyncSongLoader extends WrappedAsyncTaskLoader<ArrayList<Song>> {
        public AsyncSongLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Song> loadInBackground() {
            return SongLoader.getAllSongs(getContext());
        }
    }

    /**
     * 异步任务
     * 保存歌曲信息
     */
    private class AsyncSongSaver extends AsyncTask<ArrayList<Song>, Void, Void> {
        @Override
        protected Void doInBackground(ArrayList<Song>... arrayLists) {
            ArrayList<Song> songs = arrayLists[0];
            MusicPlaybackQueueStore.getInstance(mContext).saveQueues(songs);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            boolean firstIntoApp = SPUtils.getBoolean(getContext(), Constant.FIRST_INTO_APP, true);
            if (firstIntoApp) {
                // 如果是第一次进入app
                if ((getActivity() instanceof BaseSlidingPanelActivity)) {
                    BaseSlidingPanelActivity slidingPanelActivity = (BaseSlidingPanelActivity) getActivity();
                    slidingPanelActivity.bindToMusicService();
                    SPUtils.putBoolean(getContext(), Constant.FIRST_INTO_APP, false);
                }
            }
        }
    }
}
