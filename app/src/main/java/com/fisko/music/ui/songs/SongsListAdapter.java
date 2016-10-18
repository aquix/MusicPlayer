package com.fisko.music.ui.songs;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.fisko.music.utils.UIUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

class SongsListAdapter extends BaseAdapter {

    private FragmentActivity mActivity;
    private LayoutInflater mInflater;
    private ArrayList<Song> mSongs;
    private int mPlayingSongIndex;


    SongsListAdapter(Activity activity) {
        mInflater = LayoutInflater.from(activity);
        mSongs = new ArrayList<>();
        mActivity = (FragmentActivity) activity;
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
        ImageView playingIndicator;
        ImageView albumCover;
    }

    void setPlayingSong(int playingSongIndex) {
        mPlayingSongIndex = playingSongIndex;
    }

    void deleteSong(Song song) {
        mSongs.remove(song);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Song song = getItem(position);
        ViewHolder holder;

        if(view == null || view.getTag() == null) {
            view = mInflater.inflate(R.layout.songs_list_item, parent, false);
            holder = new ViewHolder();

            holder.songName = (TextView) view.findViewById(R.id.album_name);
            holder.playingIndicator = (ImageView) view.findViewById(R.id.song_playing_indicator);
            holder.albumCover = (ImageView) view.findViewById(R.id.song_image);

            fillHolder(holder, song, position);
            view.setTag(holder);
            mActivity.registerForContextMenu(view);
        } else {
            holder = (ViewHolder) view.getTag();
            fillHolder(holder, song, position);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.openSongPlayer(song, mSongs, mActivity);
            }
        });

        return view;
    }

    private void fillHolder(ViewHolder holder, Song song, int songIndex) {
        holder.songName.setText(song.getName());
        if(songIndex == mPlayingSongIndex) {
            Drawable drawable = ContextCompat.getDrawable(mActivity, R.drawable.playing_indicator);
            holder.playingIndicator.setImageDrawable(drawable);
        } else {
            holder.playingIndicator.setImageResource(android.R.color.transparent);
        }
        String songImageUrl = song.getImagePath();
        Picasso.with(mActivity).load(songImageUrl).into(holder.albumCover);
    }


}