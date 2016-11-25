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
import com.vlad.player.data.models.Song;
import com.vlad.player.data.viewmodels.SongFullInfo;
import com.vlad.player.utils.UiUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class AllSongsListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater layoutInflater;
    private ArrayList<SongFullInfo> songs;

    AllSongsListAdapter(List<SongFullInfo> songs, Activity activity) {
        this.activity = activity;
        this.layoutInflater = LayoutInflater.from(activity);
        this.songs = new ArrayList<>(songs);
    }

    @Override
    public int getCount() {
        return this.songs.size();
    }

    @Override
    public SongFullInfo getItem(int position) {
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
        ImageView image;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final SongFullInfo song = this.getItem(position);
        ViewHolder holder;

        if(view == null || view.getTag() == null) {
            view = this.layoutInflater.inflate(R.layout.all_songs_list_item, parent, false);
            holder = new ViewHolder();

            holder.songName = (TextView) view.findViewById(R.id.allsongs_title);
            holder.songArtist = (TextView) view.findViewById(R.id.allsongs_artist);
            holder.songDuration = (TextView) view.findViewById(R.id.allsongs_duration);
            holder.image = (ImageView) view.findViewById(R.id.allsongs_image);

            this.fillHolder(holder, song, position);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            this.fillHolder(holder, song, position);
        }

        return view;
    }

    private String formatDuration(int durationMs) {
        int seconds = durationMs / 1000;
        int minutes = seconds / 60;
        seconds %= 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void fillHolder(ViewHolder holder, SongFullInfo song, int songIndex) {
        holder.songName.setText(song.Title);
        holder.songArtist.setText(song.Artist);
        holder.songDuration.setText(this.formatDuration(song.Duration));

        String songImageUrl = song.ImagePath;
        Picasso.with(this.activity)
                .load(songImageUrl)
                .error(R.drawable.album_art_default)
                .into(holder.image);
    }
}