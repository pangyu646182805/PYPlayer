package com.neuroandroid.pyplayer.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.module.GlideModule;
import com.neuroandroid.pyplayer.glide.artistimage.ArtistImage;
import com.neuroandroid.pyplayer.glide.artistimage.ArtistImageLoader;
import com.neuroandroid.pyplayer.glide.audiocover.AudioFileCover;
import com.neuroandroid.pyplayer.glide.audiocover.AudioFileCoverLoader;

import java.io.InputStream;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PYPlayerGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        glide.register(AudioFileCover.class, InputStream.class, new AudioFileCoverLoader.Factory());
        glide.register(ArtistImage.class, InputStream.class, new ArtistImageLoader.Factory(context));
    }
}
