package com.vlad.player.ui.albums;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vlad.player.R;
import com.vlad.player.data.Song;
import com.vlad.player.ui.songs.SongsActivity;
import com.vlad.player.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;


class AlbumsRecentAdapter extends RecyclerView.Adapter<AlbumsRecentAdapter.ViewHolder> {

    private List<Song> songs;
    private Context context;

    AlbumsRecentAdapter(List<Song> songs, Context context) {
        this.songs = songs;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.albums_list_footer_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final Song song = this.songs.get(i);
        String coverUrl = this.songs.get(i).getImagePath();

        viewHolder.songName.setText(song.getName());
        Picasso.with(this.context).load(coverUrl).into(viewHolder.cover);
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent songIntent = new Intent(AlbumsRecentAdapter.this.context, SongsActivity.class);
                songIntent.putExtra(Constants.SONG_BUNDLE.OPENED_SONG, song);
                AlbumsRecentAdapter.this.context.startActivity(songIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageView cover;
        private TextView songName;

        ViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.cover = (ImageView) itemView.findViewById(R.id.album_cover);
            this.songName = (TextView) itemView.findViewById(R.id.album_name);

        }
    }
}
