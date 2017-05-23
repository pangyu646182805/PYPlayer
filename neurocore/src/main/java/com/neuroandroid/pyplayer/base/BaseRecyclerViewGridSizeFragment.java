package com.neuroandroid.pyplayer.base;

import android.support.v7.widget.RecyclerView;

import com.neuroandroid.pyplayer.adapter.base.SelectAdapter;

/**
 * Created by NeuroAndroid on 2017/5/10.
 */

public abstract class BaseRecyclerViewGridSizeFragment<ADAPTER extends SelectAdapter, LM extends RecyclerView.LayoutManager> extends BaseRecyclerViewFragment<ADAPTER, LM> {
    /**
     * 网格大小
     */
    private int mGridSize;

    private boolean mUsePaletteInitialized;
    /**
     * 是否使用画板变色
     */
    private boolean mUsePalette;

    public final int getGridSize() {
        if (mGridSize == 0) {
            mGridSize = loadGridSize();
        }
        return mGridSize;
    }

    public final boolean usePalette() {
        if (!mUsePaletteInitialized) {
            mUsePalette = loadUsePalette();
            mUsePaletteInitialized = true;
        }
        return mUsePalette;
    }

    /**
     * 设置和保存网格大小
     *
     * @param gridSize 网格大小
     */
    public void setAndSaveGridSize(final int gridSize) {
        int oldItemType = getItemType();
        mGridSize = gridSize;
        saveGridSize(gridSize);
        // only recreate the adapter and layout manager if the layout currentLayoutRes has changed
        if (oldItemType != getItemType()) {
            invalidateLayoutManager();
            invalidateAdapter();
            invalidateListener();
        } else {
            setGridSize(gridSize);
        }
    }

    /**
     * 是否使用画板变色功能
     */
    public void setAndSaveUsePalette(final boolean usePalette) {
        mUsePalette = usePalette;
        saveUsePalette(usePalette);
        setUsePalette(usePalette);
    }

    @Override
    protected void initView() {

    }

    public boolean canUsePalette() {
        return getItemType() == SelectAdapter.TYPE_GRID;
    }

    protected int getItemType() {
        if (getGridSize() > getMaxGridSizeForList()) {
            return SelectAdapter.TYPE_GRID;
        }
        return SelectAdapter.TYPE_LIST;
    }

    /**
     * 清除选择
     */
    public void clearSelected() {
        getAdapter().clearSelected();
        getAdapter().notifyDataSetChanged();
        getAdapter().updateSelectMode(false);
    }

    /**
     * 重新设置监听器
     */
    protected void invalidateListener() {
    }

    protected int getMaxGridSizeForList() {
        return 1;
    }

    protected abstract int loadGridSize();

    protected abstract boolean loadUsePalette();

    protected abstract void saveGridSize(int gridColumns);

    protected abstract void setGridSize(int gridSize);

    protected abstract void saveUsePalette(boolean usePalette);

    protected abstract void setUsePalette(boolean usePalette);
}
