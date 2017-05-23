package com.neuroandroid.pyplayer.ui.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.ArtistDetailAdapter;
import com.neuroandroid.pyplayer.base.BaseActivity;
import com.neuroandroid.pyplayer.bean.Artist;
import com.neuroandroid.pyplayer.glide.PYPlayerColoredTarget;
import com.neuroandroid.pyplayer.glide.artistimage.ArtistImage;
import com.neuroandroid.pyplayer.glide.palette.BitmapPaletteTranscoder;
import com.neuroandroid.pyplayer.glide.palette.BitmapPaletteWrapper;
import com.neuroandroid.pyplayer.listener.LoaderIds;
import com.neuroandroid.pyplayer.loader.ArtistLoader;
import com.neuroandroid.pyplayer.misc.WrappedAsyncTaskLoader;
import com.neuroandroid.pyplayer.utils.ArtistSignatureUtil;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.widget.TitleBar;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by NeuroAndroid on 2017/5/23.
 */

public class ArtistDetailActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Artist> {
    public static final String EXTRA_ARTIST_ID = "extra_artist_id";
    private static final int LOADER_ID = LoaderIds.ARTIST_DETAIL_ACTIVITY;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.rv_album)
    RecyclerView mRv;
    @BindView(R.id.iv_album)
    ImageView mIvArtist;
    @BindView(R.id.shadow_title_bar)
    TitleBar mShadowTitleBar;

    private Artist mArtist;
    private ArtistDetailAdapter mArtistDetailAdapter;

    @Override
    protected View attachLayout() {
        return LayoutInflater.from(this).inflate(R.layout.activity_album_artist_detail, null);
    }

    @Override
    protected void initView() {
        mShadowTitleBar.setImmersive(mImmersive);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .sizeResId(R.dimen.y2).colorResId(R.color.split).build());
        mArtistDetailAdapter = new ArtistDetailAdapter(this, new ArrayList<>());
        mRv.setAdapter(mArtistDetailAdapter);
    }

    @Override
    protected void initData() {
        getSupportLoaderManager().initLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Artist> onCreateLoader(int id, Bundle args) {
        return new AsyncArtistDataLoader(this, args.getInt(EXTRA_ARTIST_ID));
    }

    @Override
    public void onLoadFinished(Loader<Artist> loader, Artist data) {
        supportPostponeEnterTransition();
        setArtist(data);
    }

    private void setArtist(Artist artist) {
        this.mArtist = artist;
        loadArtistImage(false);
        mCollapsingToolbarLayout.setTitle(artist.getName());
        mArtistDetailAdapter.setAlbumDataList(artist.albums);
        mArtistDetailAdapter.replaceAll(artist.getSongs());
    }

    private Artist getArtist() {
        if (mArtist == null) mArtist = new Artist();
        return mArtist;
    }

    private void loadArtistImage(final boolean forceDownload) {
        if (forceDownload) {
            ArtistSignatureUtil.getInstance(this).updateArtistSignature(getArtist().getName());
        }
        Glide.with(this)
                .load(new ArtistImage(getArtist().getName(), forceDownload))
                .asBitmap()
                .transcode(new BitmapPaletteTranscoder(this), BitmapPaletteWrapper.class)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.mipmap.default_artist_image)
                .signature(ArtistSignatureUtil.getInstance(this).getArtistSignature(getArtist().getName()))
                .dontAnimate()
                .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .listener(new RequestListener<ArtistImage, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(@Nullable Exception e, ArtistImage model, Target<BitmapPaletteWrapper> target, boolean isFirstResource) {
                        if (forceDownload) {
                            Toast.makeText(ArtistDetailActivity.this, e != null ? e.getClass().getSimpleName() : "Error", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, ArtistImage model, Target<BitmapPaletteWrapper> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (forceDownload) {
                            Toast.makeText(ArtistDetailActivity.this, getString(R.string.updated_artist_image), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                })
                .into(new PYPlayerColoredTarget(mIvArtist) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }

    private void setColors(int color) {
        mCollapsingToolbarLayout.setStatusBarScrimColor(color);
        mCollapsingToolbarLayout.setContentScrimColor(color);
        boolean dark = ColorUtils.isColorLight(color);
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(ColorUtils.getPrimaryTextColor(this, dark));
        mCollapsingToolbarLayout.setExpandedTitleTextColor(ColorStateList.valueOf(ColorUtils.getPrimaryTextColor(this, dark)));
        if (dark) {
            SystemUtils.myStatusBar(this);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black);
        } else {
            SystemUtils.setTranslateStatusBar(this);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        }

        setTaskDescriptionColor(color);
    }

    @Override
    public void onLoaderReset(Loader<Artist> loader) {

    }

    private static class AsyncArtistDataLoader extends WrappedAsyncTaskLoader<Artist> {
        private final int artistId;

        public AsyncArtistDataLoader(Context context, int artistId) {
            super(context);
            this.artistId = artistId;
        }

        @Override
        public Artist loadInBackground() {
            return ArtistLoader.getArtist(getContext(), artistId);
        }
    }
}
