package com.neuroandroid.pyplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.ArtistsAdapter;
import com.neuroandroid.pyplayer.base.BaseRecyclerViewGridSizeFragment;
import com.neuroandroid.pyplayer.bean.Artist;
import com.neuroandroid.pyplayer.bean.ISelect;
import com.neuroandroid.pyplayer.config.Constant;
import com.neuroandroid.pyplayer.event.SelectedEvent;
import com.neuroandroid.pyplayer.listener.LoaderIds;
import com.neuroandroid.pyplayer.loader.ArtistLoader;
import com.neuroandroid.pyplayer.misc.WrappedAsyncTaskLoader;
import com.neuroandroid.pyplayer.utils.NavigationUtils;
import com.neuroandroid.pyplayer.utils.SPUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeuroAndroid on 2017/5/8.
 */

public class ArtistsFragment extends BaseRecyclerViewGridSizeFragment<ArtistsAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Artist>> {
    private static final int LOADER_ID = LoaderIds.ARTISTS_FRAGMENT;

    /**
     * 被选中的艺术家
     */
    private List<Artist> mSelectedArtists = new ArrayList<>();

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showLoading();
        // 异步加载Artists列表
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
    protected ArtistsAdapter createAdapter() {
        boolean usePalette = loadUsePalette();
        List<Artist> dataList = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataList();
        return new ArtistsAdapter(mContext, dataList, getItemType(), usePalette);
    }

    /**
     * @return 返回列表为空时显示的信息
     */
    @Override
    protected int getEmptyMessage() {
        return R.string.no_artists;
    }

    /**
     * 当媒体库改变的时候重新异步加载Artists列表
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
        return SPUtils.getInt(mContext, Constant.ARTIST_GRID_SIZE, 1);
    }

    /**
     * @return 返回是否使用画板(保存在本地, 默认使用)
     */
    @Override
    protected boolean loadUsePalette() {
        return SPUtils.getBoolean(mContext, Constant.ARTIST_COLORED_FOOTERS, true);
    }

    /**
     * 保存网格大小到本地
     *
     * @param gridColumns 网格大小
     */
    @Override
    protected void saveGridSize(int gridColumns) {
        SPUtils.putInt(mContext, Constant.ARTIST_GRID_SIZE, gridColumns);
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
        SPUtils.putBoolean(mContext, Constant.ARTIST_COLORED_FOOTERS, usePalette);
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
    public Loader<ArrayList<Artist>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncArtistLoader(mContext);
    }

    /**
     * 异步加载Artists列表结束
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<Artist>> loader, ArrayList<Artist> artists) {
        hideLoading();
        getAdapter().replaceAll(artists);
        getAdapter().setItemLongClickListener((view, position, artist) -> {
            if (!getAdapter().isSelectMode()) {
                getAdapter().updateSelectMode(true, position);
                // getAdapter().longTouchSelectModeEnable(true);
            }
        });
        getAdapter().setItemSelectedListener(new ISelect.OnItemSelectedListener<Artist>() {
            @Override
            public void onItemSelected(View view, int position, boolean isSelected, Artist artist) {
                if (isSelected) {
                    mSelectedArtists.add(artist);
                } else {
                    mSelectedArtists.remove(artist);
                }
                EventBus.getDefault().post(new SelectedEvent<Artist>().setSelectedBeans(mSelectedArtists));
            }

            @Override
            public void onNothingSelected() {
                // 一个条目都没有被选择则取消多选模式
                getAdapter().updateSelectMode(false);
            }
        });
        getAdapter().setItemClickListener((view, position, artist) -> {
            Pair[] artistPairs = new Pair[]{
                    Pair.create(view.findViewById(R.id.iv_img),
                            UIUtils.getString(R.string.transition_album_artist)
                    )};
            NavigationUtils.goToArtist(getActivity(), artist.getId(), artistPairs);
        });
    }

    @Override
    public void clearSelected() {
        super.clearSelected();
        // 清除被选中的集合
        if (mSelectedArtists.size() > 0) mSelectedArtists.clear();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Artist>> loader) {
        getAdapter().replaceAll(new ArrayList<>());
    }

    /**
     * 异步的Artists列表加载器
     */
    private static class AsyncArtistLoader extends WrappedAsyncTaskLoader<ArrayList<Artist>> {
        public AsyncArtistLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Artist> loadInBackground() {
            return ArtistLoader.getAllArtists(getContext());
        }
    }
}
