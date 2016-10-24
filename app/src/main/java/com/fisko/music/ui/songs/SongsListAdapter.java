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
import com.fisko.music.data.Album;
import com.fisko.music.data.Song;
import com.fisko.music.utils.UIUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class SongsListAdapter extends BaseAdapter {

    static final int INDEX_NOT_INIT = -1;

    private FragmentActivity mActivity;
    private LayoutInflater mInflater;
    private ArrayList<Song> mSongs;
    private Album mAlbum;
    private int mPlayingSongIndex;


    SongsListAdapter(Album album, Activity activity) {
        mActivity = (FragmentActivity) activity;
        mInflater = LayoutInflater.from(activity);
        mAlbum = album;
        mSongs = new ArrayList<>();
        mPlayingSongIndex = INDEX_NOT_INIT;
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
        TextView songArtist;
        TextView songDuration;
        ImageView playingIndicator;
        ImageView albumCover;
    }

    void setPlayingSong(int playingSongIndex) {
        mPlayingSongIndex = playingSongIndex;
        notifyDataSetChanged();
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

            holder.songName = (TextView) view.findViewById(R.id.song_name);
            holder.songArtist = (TextView) view.findViewById(R.id.song_artist);
            holder.songDuration = (TextView) view.findViewById(R.id.song_duration);
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

    private String formatDuration(int durationMs) {
        int seconds = durationMs / 1000;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void fillHolder(ViewHolder holder, Song song, int songIndex) {
        holder.songName.setText(song.getName());
        holder.songArtist.setText(mAlbum.getArtist());
        holder.songDuration.setText(formatDuration(song.getDuration()));
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