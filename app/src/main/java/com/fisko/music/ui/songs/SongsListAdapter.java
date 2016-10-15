package com.fisko.music.ui.songs;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fisko.music.R;
import com.fisko.music.data.Song;

import java.util.ArrayList;
import java.util.List;

class SongsListAdapter extends BaseAdapter {

    static final String CURRENT_SONG_INDEX = "CURRENT_SONG_INDEX";
    static final String SONGS_LIST = "SONGS_LIST";
    static final String ALBUM_ID = "ALBUM_ID";

    private AppCompatActivity mActivity;
    private LayoutInflater mInflater;
    private ArrayList<Song> mSongs;
    private String mAlbumId;


    SongsListAdapter(Activity activity, String albumId) {
        mInflater = LayoutInflater.from(activity);
        mSongs = new ArrayList<>();
        mActivity = (AppCompatActivity) activity;
        mAlbumId = albumId;
    }

    void replaceData(List<Song> songs) {
        mSongs = new ArrayList<>(songs);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSongs.size();
    }

    @Override
    public Song getItem(int position) {
        return mSongs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {
        TextView songName;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Song song = getItem(position);
        ViewHolder holder;

        if(view == null || view.getTag() == null) {
            view = mInflater.inflate(R.layout.songs_list_item, parent, false);
            holder = new ViewHolder();

            holder.songName = (TextView) view.findViewById(R.id.album_name);

            fillHolder(holder, song);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            fillHolder(holder, song);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSongPlayer(song);
            }
        });

        return view;
    }

    private void openSongPlayer(Song song) {
        Fragment fragment = SongFragment.newInstance(mSongs.indexOf(song), mSongs, mAlbumId);
        FragmentManager manager = mActivity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.content_frame, fragment);
        transaction.commit();
    }

    private void fillHolder(ViewHolder holder, Song song) {
        holder.songName.setText(song.getName());
    }


}