package com.neuroandroid.pyplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.base.BaseRvAdapter;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.widget.IconImageView;
import com.neuroandroid.pyplayer.widget.NoPaddingTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by NeuroAndroid on 2017/5/17.
 */

public class PlayingQueuesAdapter extends BaseRvAdapter<Song, PlayingQueuesAdapter.Holder> {
    public PlayingQueuesAdapter(Context context, List<Song> dataList) {
        super(context, dataList);
    }

    @Override
    public Holder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_playing_queues, parent, false));
    }

    @Override
    public void onBindItemViewHolder(Holder holder, int position) {
        holder.onBind(mDataList.get(position));
    }

    public class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_position)
        NoPaddingTextView mTvPosition;
        @BindView(R.id.tv_title)
        NoPaddingTextView mTvTitle;
        @BindView(R.id.tv_sub_title)
        NoPaddingTextView mTvSubTitle;
        @BindView(R.id.iv_menu)
        IconImageView mIvMenu;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(Song song) {
            mTvPosition.setText(String.valueOf(getAdapterPosition() + 1));
            mTvTitle.setText(song.title);
            mTvSubTitle.setText(song.artistName);
        }
    }
}
