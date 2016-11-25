package com.vlad.player.ui.artists;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vlad.player.R;
import com.vlad.player.data.models.Song;
import com.vlad.player.ui.songs.SongsActivity;
import com.vlad.player.utils.Constants;
import com.squareup.picasso.Picasso;

import java.util.List;


class RecentArtistsAdapter extends RecyclerView.Adapter<RecentArtistsAdapter.ViewHolder> {

    private List<Song> songs;
    private Context context;

    RecentArtistsAdapter(List<Song> songs, Context context) {
        this.songs = songs;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.recent_artists_list_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final Song song = this.songs.get(i);
        String coverUrl = this.songs.get(i).getImagePath();

        viewHolder.songName.setText(song.getTitle());
        Picasso.with(this.context)
                .load(coverUrl)
                .error(R.drawable.artist_image_default)
                .into(viewHolder.cover);

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent songIntent = new Intent(RecentArtistsAdapter.this.context, SongsActivity.class);
                songIntent.putExtra(Constants.SONG_BUNDLE.OPENED_SONG, song);
                RecentArtistsAdapter.this.context.startActivity(songIntent);
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
