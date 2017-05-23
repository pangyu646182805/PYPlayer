package com.neuroandroid.pyplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.base.BaseRvAdapter;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.glide.PYPlayerColoredTarget;
import com.neuroandroid.pyplayer.glide.SongGlideRequest;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by NeuroAndroid on 2017/5/17.
 */

public class AlbumCoverAdapter extends BaseRvAdapter<Song, AlbumCoverAdapter.Holder> {
    /**
     * 存储封面调色板颜色
     */
    private int[] mAlbumCoverColors;

    public int[] getAlbumCoverColors() {
        return mAlbumCoverColors;
    }

    public AlbumCoverAdapter(Context context, List<Song> dataList) {
        super(context, dataList);
    }

    @Override
    public Holder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_album_cover, parent, false));
    }

    @Override
    public void onBindItemViewHolder(Holder holder, int position) {
        holder.onBind(mDataList.get(position));
    }

    @Override
    public void replaceAll(List<Song> dataList) {
        super.replaceAll(dataList);
        mAlbumCoverColors = new int[dataList.size()];
    }

    public class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_img)
        ImageView mIvImg;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(Song song) {
            loadAlbumCover(song);
        }

        private void loadAlbumCover(Song song) {
            SongGlideRequest.Builder.from(Glide.with(mContext), song)
                    .generatePalette(mContext).build()
                    .into(new PYPlayerColoredTarget(mIvImg) {
                        @Override
                        public void onColorReady(int color) {
                            int position = getAdapterPosition();
                            if (position >= 0 && mAlbumCoverColors != null && mAlbumCoverColors.length > position) {
                                if (mAlbumCoverColors[position] == 0) {
                                    // 如果没有存储颜色值则存储
                                    mAlbumCoverColors[position] = color;
                                }
                            }
                        }
                    });
        }
    }
}
