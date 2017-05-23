package com.neuroandroid.pyplayer.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.base.BaseRvAdapter;
import com.neuroandroid.pyplayer.bean.Album;
import com.neuroandroid.pyplayer.bean.Song;
import com.neuroandroid.pyplayer.glide.PYPlayerColoredTarget;
import com.neuroandroid.pyplayer.glide.SongGlideRequest;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.widget.IconImageView;
import com.neuroandroid.pyplayer.widget.NoPaddingTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by NeuroAndroid on 2017/5/23.
 */

public class ArtistDetailAdapter extends BaseRvAdapter<Song, RecyclerView.ViewHolder> {
    private static final int TYPE_HORIZONTAL_ALBUM = 0;
    private static final int TYPE_ARTIST_SONG = 1;
    private ArrayList<Album> mAlbumDataList;

    public void setAlbumDataList(ArrayList<Album> albumDataList) {
        mAlbumDataList = albumDataList;
    }

    public ArtistDetailAdapter(Context context, List<Song> dataList) {
        super(context, dataList);
    }

    @Override
    public RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return viewType == TYPE_HORIZONTAL_ALBUM ?
                new HorizontalAlbumHolder(LayoutInflater.from(mContext).inflate(R.layout.item_horizontal_album, parent, false))
                : new ArtistSongHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list, parent, false));
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_HORIZONTAL_ALBUM) {
            HorizontalAlbumHolder horizontalAlbumHolder = (HorizontalAlbumHolder) holder;
            horizontalAlbumHolder.onBind();
        } else {
            ArtistSongHolder artistSongHolder = (ArtistSongHolder) holder;
            artistSongHolder.onBind(mDataList.get(position - 1));
        }
    }

    public class HorizontalAlbumHolder extends RecyclerView.ViewHolder {
        private RecyclerView mRvHorizontalAlbum;

        public HorizontalAlbumHolder(View itemView) {
            super(itemView);
            mRvHorizontalAlbum = ButterKnife.findById(itemView, R.id.rv_horizontal_album);
            mRvHorizontalAlbum.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        }

        public void onBind() {
            if (mAlbumDataList != null) {
                mRvHorizontalAlbum.setAdapter(new HorizontalAlbumAdapter(mContext, mAlbumDataList));
            }
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_HORIZONTAL_ALBUM;
        return TYPE_ARTIST_SONG;
    }

    public class ArtistSongHolder extends RecyclerView.ViewHolder {
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

        public ArtistSongHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(Song song) {
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
                            setPaletteColor(getDefaultFooterColor());
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

    public class HorizontalAlbumAdapter extends BaseRvAdapter<Album, HorizontalAlbumAdapter.Holder> {
        public HorizontalAlbumAdapter(Context context, List<Album> dataList) {
            super(context, dataList);
        }

        @Override
        public Holder onCreateItemViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(mContext).inflate(R.layout.item_album, parent, false));
        }

        @Override
        public void onBindItemViewHolder(Holder holder, int position) {
            holder.onBind(mDataList.get(position));
        }

        public class Holder extends RecyclerView.ViewHolder {
            @BindView(R.id.iv_img)
            ImageView mIvImg;
            @BindView(R.id.tv_title)
            NoPaddingTextView mTvTitle;
            @BindView(R.id.tv_sub_title)
            NoPaddingTextView mTvSubTitle;
            @BindView(R.id.ll_palette)
            LinearLayout mLlPalette;

            public Holder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void onBind(Album album) {
                mTvTitle.setText(album.getTitle());
                mTvSubTitle.setText(album.getArtistName());

                loadAlbumCover(album);
            }

            protected void loadAlbumCover(Album album) {
                if (mIvImg == null) return;

                SongGlideRequest.Builder.from(Glide.with(mContext), album.safeGetFirstSong())
                        .generatePalette(mContext).build()
                        .into(new PYPlayerColoredTarget(mIvImg) {
                            @Override
                            public void onLoadCleared(Drawable placeholder) {
                                super.onLoadCleared(placeholder);
                                setPaletteColor(getDefaultFooterColor());
                            }

                            @Override
                            public void onColorReady(int color) {
                                setPaletteColor(getDefaultFooterColor());
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
}
