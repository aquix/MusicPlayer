package com.vlad.player.ui.artists;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vlad.player.R;
import com.vlad.player.data.models.Artist;
import com.vlad.player.ui.songs.SongsActivity;
import com.vlad.player.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

class AlbumsListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<Artist> artists;
    private Activity activity;

    private long playingArtistId;

    AlbumsListAdapter(Activity activity) {
        this.layoutInflater = LayoutInflater.from(activity);
        this.artists = new ArrayList<>();
        this.activity = activity;
    }

    void replaceData(List<Artist> artists) {
        this.artists = new ArrayList<>(artists);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.artists.size();
    }

    @Override
    public Artist getItem(int position) {
        return this.artists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {
        TextView artistName;
        ImageView playingIndicator;
        ImageView image;
    }

    public void setPlayingArtist(long artistId) {
        this.playingArtistId = artistId;
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Artist artist = this.getItem(position);
        ViewHolder holder;

        if(view == null || view.getTag() == null) {
            view = this.layoutInflater.inflate(R.layout.albums_list_item, parent, false);
            holder = new ViewHolder();

            holder.artistName = (TextView) view.findViewById(R.id.album_artist);
            holder.playingIndicator = (ImageView) view.findViewById(R.id.album_playing_indicator);
            holder.image = (ImageView) view.findViewById(R.id.album_cover);

            this.fillHolder(holder, artist);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            this.fillHolder(holder, artist);
        }

        view.setClickable(true);
        view.setLongClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumsListAdapter.this.openArtist(artist);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlbumsListAdapter.this.activity.registerForContextMenu(v);
                AlbumsListAdapter.this.activity.openContextMenu(v);
                return true;
            }
        });

        return view;
    }

    void removeArtist(Artist artist) {
        this.artists.remove(artist);
        this.notifyDataSetChanged();
    }

    private void openArtist(Artist artist) {
        Intent intent = new Intent(this.activity, SongsActivity.class);
        intent.putExtra(Constants.ALBUM_BUNDLE.ALBUM, artist);
        this.activity.startActivity(intent);
    }

    private void fillHolder(ViewHolder holder, Artist artist) {
        holder.artistName.setText(artist.getName());
        if(artist.getId() == this.playingArtistId) {
            Drawable drawable = ContextCompat.getDrawable(this.activity, R.drawable.playing_indicator);
            holder.playingIndicator.setImageDrawable(drawable);
        } else {
            holder.playingIndicator.setImageResource(android.R.color.transparent);
        }
        String coverUrl = artist.getImagePath();
        Picasso.with(this.activity).load(coverUrl).into(holder.image);
    }


}