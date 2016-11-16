package com.vlad.player.ui.songs;

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

import com.vlad.player.R;
import com.vlad.player.data.Album;
import com.vlad.player.data.Song;
import com.vlad.player.utils.UIUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class SongsListAdapter extends BaseAdapter {

    static final int INDEX_NOT_INIT = -1;

    private FragmentActivity activity;
    private LayoutInflater layoutInflater;
    private ArrayList<Song> songs;
    private Album album;
    private int playingSongIndex;


    SongsListAdapter(Album album, Activity activity) {
        this.activity = (FragmentActivity) activity;
        this.layoutInflater = LayoutInflater.from(activity);
        this.album = album;
        this.songs = new ArrayList<>();
        this.playingSongIndex = INDEX_NOT_INIT;
    }

    void replaceData(List<Song> songs) {
        this.songs = new ArrayList<>(songs);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.songs.size();
    }

    @Override
    public Song getItem(int position) {
        return this.songs.get(position);
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
        this.playingSongIndex = playingSongIndex;
        this.notifyDataSetChanged();
    }

    void deleteSong(Song song) {
        this.songs.remove(song);
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Song song = this.getItem(position);
        ViewHolder holder;

        if(view == null || view.getTag() == null) {
            view = this.layoutInflater.inflate(R.layout.songs_list_item, parent, false);
            holder = new ViewHolder();

            holder.songName = (TextView) view.findViewById(R.id.song_name);
            holder.songArtist = (TextView) view.findViewById(R.id.song_artist);
            holder.songDuration = (TextView) view.findViewById(R.id.song_duration);
            holder.playingIndicator = (ImageView) view.findViewById(R.id.song_playing_indicator);
            holder.albumCover = (ImageView) view.findViewById(R.id.song_image);

            this.fillHolder(holder, song, position);
            view.setTag(holder);
            this.activity.registerForContextMenu(view);
        } else {
            holder = (ViewHolder) view.getTag();
            this.fillHolder(holder, song, position);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtils.openSongPlayer(song, SongsListAdapter.this.songs, SongsListAdapter.this.activity);
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
        holder.songArtist.setText(this.album.getArtist());
        holder.songDuration.setText(this.formatDuration(song.getDuration()));
        if(songIndex == this.playingSongIndex) {
            Drawable drawable = ContextCompat.getDrawable(this.activity, R.drawable.playing_indicator);
            holder.playingIndicator.setImageDrawable(drawable);
        } else {
            holder.playingIndicator.setImageResource(android.R.color.transparent);
        }
        String songImageUrl = song.getImagePath();
        Picasso.with(this.activity).load(songImageUrl).into(holder.albumCover);
    }


}