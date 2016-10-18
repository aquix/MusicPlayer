package com.fisko.music.ui.song;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fisko.music.data.Song;

import java.util.List;

class SongPagerAdapter extends FragmentPagerAdapter {

    private List<Song> mSongs;

    SongPagerAdapter(List<Song> songs, FragmentManager fm) {
        super(fm);
        mSongs = songs;
    }

    @Override
    public Fragment getItem(int position) {
        return SongImageFragment.newInstance(mSongs.get(position));
    }

    @Override
    public int getCount() {
        return mSongs.size();
    }
}
