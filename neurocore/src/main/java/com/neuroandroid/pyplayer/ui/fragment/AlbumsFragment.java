package com.neuroandroid.pyplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.AlbumAdapter;
import com.neuroandroid.pyplayer.base.BaseRecyclerViewGridSizeFragment;
import com.neuroandroid.pyplayer.bean.Album;
import com.neuroandroid.pyplayer.bean.ISelect;
import com.neuroandroid.pyplayer.config.Constant;
import com.neuroandroid.pyplayer.event.SelectedEvent;
import com.neuroandroid.pyplayer.listener.LoaderIds;
import com.neuroandroid.pyplayer.loader.AlbumLoader;
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

public class AlbumsFragment extends BaseRecyclerViewGridSizeFragment<AlbumAdapter, GridLayoutManager> implements LoaderManager.LoaderCallbacks<ArrayList<Album>> {
    private static final int LOADER_ID = LoaderIds.ALBUMS_FRAGMENT;

    /**
     * 被选中的专辑
     */
    private List<Album> mSelectedAlbums = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showLoading();
        // 异步加载Albums列表
        getLoaderManager().initLoader(LOADER_ID, null, this);
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
    protected AlbumAdapter createAdapter() {
        boolean usePalette = loadUsePalette();
        List<Album> dataList = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataList();
        return new AlbumAdapter(mContext, dataList, getItemType(), usePalette);
    }

    /**
     * @return 返回列表为空时显示的信息
     */
    @Override
    protected int getEmptyMessage() {
        return R.string.no_albums;
    }

    /**
     * 当媒体库改变的时候重新异步加载Albums列表
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
        return SPUtils.getInt(mContext, Constant.ALBUM_GRID_SIZE, 1);
    }

    /**
     * @return 返回是否使用画板(保存在本地, 默认使用)
     */
    @Override
    protected boolean loadUsePalette() {
        return SPUtils.getBoolean(mContext, Constant.ALBUM_COLORED_FOOTERS, true);
    }

    /**
     * 保存网格大小到本地
     *
     * @param gridColumns 网格大小
     */
    @Override
    protected void saveGridSize(int gridColumns) {
        SPUtils.putInt(mContext, Constant.ALBUM_GRID_SIZE, gridColumns);
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
        SPUtils.putBoolean(mContext, Constant.ALBUM_COLORED_FOOTERS, usePalette);
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
    public Loader<ArrayList<Album>> onCreateLoader(int i, Bundle bundle) {
        return new AsyncAlbumLoader(mContext);
    }

    /**
     * 异步加载Albums列表结束
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<Album>> loader, ArrayList<Album> alba) {
        hideLoading();
        getAdapter().replaceAll(alba);
        invalidateListener();
    }

    @Override
    protected void invalidateListener() {
        getAdapter().setItemLongClickListener((view, position, album) -> {
            if (!getAdapter().isSelectMode()) {
                getAdapter().updateSelectMode(true, position);
            }
        });
        getAdapter().setItemSelectedListener(new ISelect.OnItemSelectedListener<Album>() {
            @Override
            public void onItemSelected(View view, int position, boolean isSelected, Album album) {
                if (isSelected) {
                    mSelectedAlbums.add(album);
                } else {
                    mSelectedAlbums.remove(album);
                }
                EventBus.getDefault().post(new SelectedEvent<Album>().setSelectedBeans(mSelectedAlbums));
            }

            @Override
            public void onNothingSelected() {
                // 一个条目都没有被选择则取消多选模式
                getAdapter().updateSelectMode(false);
            }
        });
        getAdapter().setItemClickListener((view, position, album) -> {
            Pair[] albumPairs = new Pair[]{
                    Pair.create(view.findViewById(R.id.iv_img),
                            UIUtils.getString(R.string.transition_album_artist)
                    )};
            NavigationUtils.goToAlbum(getActivity(), album.getId(), albumPairs);
        });
    }

    @Override
    public void clearSelected() {
        super.clearSelected();
        // 清除被选中的集合
        if (mSelectedAlbums.size() > 0) mSelectedAlbums.clear();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Album>> loader) {
        getAdapter().replaceAll(new ArrayList<>());
    }

    /**
     * 异步的Albums列表加载器
     */
    private static class AsyncAlbumLoader extends WrappedAsyncTaskLoader<ArrayList<Album>> {
        public AsyncAlbumLoader(Context context) {
            super(context);
        }

        @Override
        public ArrayList<Album> loadInBackground() {
            return AlbumLoader.getAllAlbums(getContext());
        }
    }
}
