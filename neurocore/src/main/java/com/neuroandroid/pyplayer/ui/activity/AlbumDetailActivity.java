package com.neuroandroid.pyplayer.ui.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.adapter.PlayingQueuesAdapter;
import com.neuroandroid.pyplayer.base.BaseActivity;
import com.neuroandroid.pyplayer.bean.Album;
import com.neuroandroid.pyplayer.glide.PYPlayerColoredTarget;
import com.neuroandroid.pyplayer.glide.SongGlideRequest;
import com.neuroandroid.pyplayer.glide.palette.BitmapPaletteWrapper;
import com.neuroandroid.pyplayer.listener.LoaderIds;
import com.neuroandroid.pyplayer.loader.AlbumLoader;
import com.neuroandroid.pyplayer.misc.WrappedAsyncTaskLoader;
import com.neuroandroid.pyplayer.utils.ColorUtils;
import com.neuroandroid.pyplayer.utils.SystemUtils;
import com.neuroandroid.pyplayer.utils.ThemeUtils;
import com.neuroandroid.pyplayer.utils.UIUtils;
import com.neuroandroid.pyplayer.widget.TitleBar;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;

/**
 * Created by NeuroAndroid on 2017/5/22.
 */

public class AlbumDetailActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Album> {
    public static final String EXTRA_ALBUM_ID = "extra_album_id";
    private static final int LOADER_ID = LoaderIds.ALBUM_DETAIL_ACTIVITY;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.rv_album)
    RecyclerView mRvAlbum;
    @BindView(R.id.iv_album)
    ImageView mIvAlbum;
    @BindView(R.id.shadow_title_bar)
    TitleBar mShadowTitleBar;

    private Album mAlbum;
    private PlayingQueuesAdapter mQueuesAdapter;

    @Override
    protected View attachLayout() {
        return LayoutInflater.from(this).inflate(R.layout.activity_album_artist_detail, null);
    }

    @Override
    protected void initView() {
        mShadowTitleBar.setImmersive(mImmersive);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRvAlbum.setLayoutManager(new LinearLayoutManager(this));
        mRvAlbum.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this)
                .sizeResId(R.dimen.y2).colorResId(R.color.split).build());
        mQueuesAdapter = new PlayingQueuesAdapter(this, new ArrayList<>());
        mRvAlbum.setAdapter(mQueuesAdapter);
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
    public Loader<Album> onCreateLoader(int id, Bundle args) {
        return new AsyncAlbumLoader(this, args.getInt(EXTRA_ALBUM_ID));
    }

    @Override
    public void onLoadFinished(Loader<Album> loader, Album data) {
        supportStartPostponedEnterTransition();
        setAlbum(data);
    }

    private void setAlbum(Album album) {
        this.mAlbum = album;
        mCollapsingToolbarLayout.setTitle(album.getTitle());
        loadAlbumCover();
        mQueuesAdapter.replaceAll(album.songs);
    }

    @Override
    public void onLoaderReset(Loader<Album> loader) {

    }

    private void loadAlbumCover() {
        SongGlideRequest.Builder.from(Glide.with(this), getAlbum().safeGetFirstSong())
                .generatePalette(this).build()
                .dontAnimate()
                .listener(new RequestListener<Object, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<BitmapPaletteWrapper> target, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, Object model, Target<BitmapPaletteWrapper> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        supportStartPostponedEnterTransition();
                        return false;
                    }
                })
                .into(new PYPlayerColoredTarget(mIvAlbum) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        setColors(UIUtils.getColor(ThemeUtils.getThemeColor(AlbumDetailActivity.this)));
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

    private Album getAlbum() {
        if (mAlbum == null) mAlbum = new Album();
        return mAlbum;
    }

    private static class AsyncAlbumLoader extends WrappedAsyncTaskLoader<Album> {
        private final int albumId;

        public AsyncAlbumLoader(Context context, int albumId) {
            super(context);
            this.albumId = albumId;
        }

        @Override
        public Album loadInBackground() {
            return AlbumLoader.getAlbum(getContext(), albumId);
        }
    }
}
