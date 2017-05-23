package com.neuroandroid.pyplayer.widget.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.base.SelectAdapter;
import com.neuroandroid.pyplayer.annotation.SelectMode;
import com.neuroandroid.pyplayer.bean.ISelect;
import com.neuroandroid.pyplayer.widget.dialog.base.BaseDialog;

import butterknife.BindView;

/**
 * Created by NeuroAndroid on 2017/3/10.
 */

public class ListDialog<ADAPTER extends SelectAdapter, DATA extends ISelect> extends BaseDialog<ListDialog<ADAPTER, DATA>> {
    @BindView(R.id.rv)
    RecyclerView mRv;
    private ADAPTER mSelectAdapter;

    public ListDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.layout_list_dialog;
    }

    @Override
    protected void initView() {
        mRv.setLayoutManager(new LinearLayoutManager(mContext));
        /*mRv.addItemDecoration(new HorizontalDividerItemDecoration.Builder(mContext)
                .colorResId(R.color.split).sizeResId(R.dimen.y2).build());*/

        RecyclerView.ItemAnimator animator = mRv.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mRv.getItemAnimator().setChangeDuration(333);
        mRv.getItemAnimator().setMoveDuration(333);

        /**
         * 单选模式
         * 默认没有标题栏和底部栏
         * 如果需要显示则调用
         * @see BaseDialog#showTitle()
         * @see BaseDialog#showButton()
         */
        setNoTitle();
        setNoButton();
    }

    /**
     * 自定义适配器
     */
    public ListDialog setAdapter(RecyclerView.Adapter adapter) {
        mRv.setAdapter(adapter);
        return this;
    }

    public ListDialog setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mRv.setLayoutManager(layoutManager);
        return this;
    }

    /**
     * 设置选择模式(单选或者多选)
     * 默认单选
     *
     * @param mode {@link SelectMode}
     */
    public ListDialog setSelectMode(@SelectMode int mode) {
        if (mSelectAdapter == null) {
            throw new IllegalArgumentException("selectAdapter is null");
        } else {
            mSelectAdapter.setSelectedMode(mode);
        }
        return this;
    }

    /**
     * 设置(单选或者多选)模式的适配器
     * item高度自定义
     *
     */
    public ListDialog setSelectAdapter(ADAPTER selectAdapter,
                                       ISelect.OnItemSelectedListener<DATA> itemSelectedListener) {
        if (selectAdapter == null) {
            throw new IllegalArgumentException("selectAdapter is null");
        }
        mSelectAdapter = selectAdapter;
        if (itemSelectedListener != null) {
            mSelectAdapter.setItemSelectedListener(itemSelectedListener);
        }
        mRv.setAdapter(mSelectAdapter);
        return this;
    }

    /**
     * 根据数据源的isSelect勾选
     */
    public ListDialog setAdapterCheckedPos() {
        if (mSelectAdapter != null) {
            mSelectAdapter.setCheckedPos();
        }
        return this;
    }

    /**
     * 根据传入的position勾选
     */
    public ListDialog setAdapterCheckedPos(int checkedPos) {
        if (mSelectAdapter != null) {
            mSelectAdapter.setCheckedPos(checkedPos);
        }
        return this;
    }
}
