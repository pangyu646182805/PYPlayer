package com.neuroandroid.pyplayer.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.neuroandroid.pyplayer.R;
import com.neuroandroid.pyplayer.ui.fragment.AlbumsFragment;
import com.neuroandroid.pyplayer.ui.fragment.ArtistsFragment;
import com.neuroandroid.pyplayer.ui.fragment.SongsFragment;
import com.neuroandroid.pyplayer.utils.UIUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NeuroAndroid on 2017/5/8.
 */

public class MusicLibraryPagerAdapter extends FragmentPagerAdapter {
    private final SparseArray<WeakReference<Fragment>> mFragmentArray = new SparseArray<>();
    private final List<Holder> mHolderList = new ArrayList<>();
    private String[] mTitles;
    private Context mContext;

    public MusicLibraryPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        mTitles = new String[]{UIUtils.getString(R.string.songs),
                UIUtils.getString(R.string.albums),
                UIUtils.getString(R.string.artists)};
        final MusicFragments[] fragments = MusicFragments.values();
        for (final MusicLibraryPagerAdapter.MusicFragments fragment : fragments) {
            add(fragment.getFragmentClass(), null);
        }
    }

    @SuppressWarnings("synthetic-access")
    public void add(@NonNull final Class<? extends Fragment> className, final Bundle params) {
        final Holder mHolder = new Holder();
        mHolder.mClassName = className.getName();
        mHolder.mParams = params;

        final int mPosition = mHolderList.size();
        mHolderList.add(mPosition, mHolder);
        notifyDataSetChanged();
    }

    public Fragment getFragment(final int position) {
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null && mWeakFragment.get() != null) {
            return mWeakFragment.get();
        }
        return getItem(position);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final Fragment mFragment = (Fragment) super.instantiateItem(container, position);
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
        mFragmentArray.put(position, new WeakReference<>(mFragment));
        return mFragment;
    }

    @Override
    public Fragment getItem(int position) {
        final Holder mCurrentHolder = mHolderList.get(position);
        return Fragment.instantiate(mContext,
                mCurrentHolder.mClassName, mCurrentHolder.mParams);
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        super.destroyItem(container, position, object);
        final WeakReference<Fragment> mWeakFragment = mFragmentArray.get(position);
        if (mWeakFragment != null) {
            mWeakFragment.clear();
        }
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    public enum MusicFragments {
        SONG(SongsFragment.class),
        ALBUM(AlbumsFragment.class),
        ARTIST(ArtistsFragment.class);

        private final Class<? extends Fragment> mFragmentClass;

        MusicFragments(final Class<? extends Fragment> fragmentClass) {
            mFragmentClass = fragmentClass;
        }

        public Class<? extends Fragment> getFragmentClass() {
            return mFragmentClass;
        }
    }

    private final static class Holder {
        String mClassName;
        Bundle mParams;
    }
}
