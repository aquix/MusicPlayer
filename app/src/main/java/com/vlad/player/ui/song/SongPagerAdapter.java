package com.vlad.player.ui.song;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vlad.player.data.Song;

import java.util.List;

class SongPagerAdapter extends FragmentPagerAdapter {

    private List<Song> songs;

    SongPagerAdapter(List<Song> songs, FragmentManager fm) {
        super(fm);
        this.songs = songs;
    }

    @Override
    public Fragment getItem(int position) {
        return SongImageFragment.newInstance(songs.get(position));
    }

    @Override
    public int getCount() {
        return songs.size();
    }
}
