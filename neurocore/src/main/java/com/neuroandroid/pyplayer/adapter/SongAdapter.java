package com.neuroandroid.pyplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.base.SelectAdapter;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.glide.PYPlayerColoredTarget;
import com.neuroandroid.pyplayer.glide.SongGlideRequest;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.utils.MusicUtil;
import com.neuroandroid.pyplayer.widget.IconImageView;
import com.neuroandroid.pyplayer.widget.NoPaddingTextView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by NeuroAndroid on 2017/5/9.
 */

public class SongAdapter extends SelectAdapter<Song, SongAdapter.Holder> implements FastScrollRecyclerView.SectionedAdapter {
    public SongAdapter(Context context, List<Song> dataList, int itemType, boolean usePalette) {
        super(context, dataList);
        mUsePalette = usePalette;
        mCurrentType = itemType;
    }

    @Override
    public Holder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        Holder holder = null;
        switch (viewType) {
            case TYPE_LIST:
                holder = new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_list, parent, false));
                break;
            case TYPE_GRID:
                holder = new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_grid, parent, false));
                break;
        }
        return holder;
    }

    @Override
    public void onBindItemViewHolder(Holder holder, int position) {
        holder.onBind(mDataList.get(position));
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return MusicUtil.getSectionName(mDataList.get(position).title);
    }

    public class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_img)
        ImageView mIvImg;
        @BindView(R.id.tv_title)
        NoPaddingTextView mTvTitle;
        @BindView(R.id.tv_sub_title)
        NoPaddingTextView mTvSubTitle;
        @Nullable
        @BindView(R.id.iv_menu)
        IconImageView mIvMenu;
        @Nullable
        @BindView(R.id.ll_palette)
        LinearLayout mLlPalette;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(Song song) {
            itemView.setActivated(song.isSelected());

            mTvTitle.setText(song.title);
            mTvSubTitle.setText(song.artistName);

            loadAlbumCover(song);

            if (mIvMenu != null) {
                mIvMenu.setOnClickListener(view -> {
                    PopupMenu popupMenu = new PopupMenu(mContext, view);
                    popupMenu.inflate(R.menu.menu_item_song);
                    popupMenu.setOnMenuItemClickListener(null);
                    popupMenu.show();
                });
            }
        }

        private void loadAlbumCover(Song song) {
            SongGlideRequest.Builder.from(Glide.with(mContext), song)
                    .generatePalette(mContext).build()
                    .into(new PYPlayerColoredTarget(mIvImg) {
                        @Override
                        public void onLoadCleared(Drawable placeholder) {
                            super.onLoadCleared(placeholder);
                            setPaletteColor(getDefaultFooterColor());
                        }

                        @Override
                        public void onColorReady(int color) {
                            if (mUsePalette) {
                                setPaletteColor(color);
                            } else {
                                setPaletteColor(getDefaultFooterColor());
                            }
                        }
                    });
        }

        private void setPaletteColor(int color) {
            if (mLlPalette != null) {
                mLlPalette.setBackgroundColor(color);
                if (mTvTitle != null) {
                    mTvTitle.setTextColor(ColorUtils.getPrimaryTextColor(mContext, ColorUtils.isColorLight(color)));
                }
                if (mTvSubTitle != null) {
                    mTvSubTitle.setTextColor(ColorUtils.getSecondaryTextColor(mContext, ColorUtils.isColorLight(color)));
                }
            }
        }
    }
}
