package com.vlad.player.ui.albums;

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
import com.vlad.player.data.Album;
import com.vlad.player.ui.songs.SongsActivity;
import com.vlad.player.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

class AlbumsListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<Album> albums;
    private Activity activity;

    private String playingAlbumId;

    AlbumsListAdapter(Activity activity) {
        layoutInflater = LayoutInflater.from(activity);
        albums = new ArrayList<>();
        this.activity = activity;
    }

    void replaceData(List<Album> albums) {
        this.albums = new ArrayList<>(albums);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Album getItem(int position) {
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {
        TextView albumName;
        TextView albumArtist;
        ImageView playingIndicator;
        ImageView albumCover;
    }

    void setPlayingAlbum(String albumId) {
        playingAlbumId = albumId;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Album album = getItem(position);
        ViewHolder holder;

        if(view == null || view.getTag() == null) {
            view = layoutInflater.inflate(R.layout.albums_list_item, parent, false);
            holder = new ViewHolder();

            holder.albumName = (TextView) view.findViewById(R.id.album_name);
            holder.albumArtist = (TextView) view.findViewById(R.id.album_artist);
            holder.playingIndicator = (ImageView) view.findViewById(R.id.album_playing_indicator);
            holder.albumCover = (ImageView) view.findViewById(R.id.album_cover);

            fillHolder(holder, album);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            fillHolder(holder, album);
        }

        view.setClickable(true);
        view.setLongClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlbum(album);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                activity.registerForContextMenu(v);
                activity.openContextMenu(v);
                return true;
            }
        });

        return view;
    }

    void removeAlbum(Album album) {
        albums.remove(album);
        notifyDataSetChanged();
    }

    private void openAlbum(Album album) {
        Intent intent = new Intent(activity, SongsActivity.class);
        intent.putExtra(Constants.ALBUM_BUNDLE.ALBUM, album);
        activity.startActivity(intent);
    }

    private void fillHolder(ViewHolder holder, Album album) {
        holder.albumName.setText(album.getName());
        holder.albumArtist.setText(album.getArtist());
        if(album.getId().equals(playingAlbumId)) {
            Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.playing_indicator);
            holder.playingIndicator.setImageDrawable(drawable);
        } else {
            holder.playingIndicator.setImageResource(android.R.color.transparent);
        }
        String coverUrl = album.getImagePath();
        Picasso.with(activity).load(coverUrl).into(holder.albumCover);
    }


}