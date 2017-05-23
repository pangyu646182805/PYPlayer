package com.neuroandroid.pyplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.base.SelectAdapter;
import com.neuroandroid.pyplayer.bean.ThemeColorBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by NeuroAndroid on 2017/5/19.
 */

public class ThemeColorAdapter extends SelectAdapter<ThemeColorBean, ThemeColorAdapter.Holder> {
    public ThemeColorAdapter(Context context, List<ThemeColorBean> dataList) {
        super(context, dataList);
        longTouchSelectModeEnable(false);
    }

    @Override
    public Holder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_theme_color, parent, false));
    }

    @Override
    public void onBindItemViewHolder(Holder holder, int position) {
        holder.onBind(mDataList.get(position));
    }

    public class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_theme_color)
        CircleImageView mIvThemeColor;
        @BindView(R.id.iv_checked)
        ImageView mIvChecked;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(ThemeColorBean themeColorBean) {
            mIvChecked.setVisibility(themeColorBean.isSelected() ? View.VISIBLE : View.GONE);
            if (themeColorBean.getThemeColor() == R.color.white) {
                mIvChecked.setImageResource(R.drawable.ic_done_green);
            } else {
                mIvChecked.setImageResource(R.drawable.ic_done_white);
            }
            mIvThemeColor.setImageResource(themeColorBean.getThemeColor());
        }
    }
}
