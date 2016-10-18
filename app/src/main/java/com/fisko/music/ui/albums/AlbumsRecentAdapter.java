package com.fisko.music.ui.albums;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.squareup.picasso.Picasso;

import java.util.List;


class AlbumsRecentAdapter extends RecyclerView.Adapter<AlbumsRecentAdapter.ViewHolder> {

    private List<Song> mSongs;
    private Context mContext;

    AlbumsRecentAdapter(List<Song> songs, Context context) {
        mSongs = songs;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.albums_list_footer_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Song songs = mSongs.get(i);
        viewHolder.songName.setText(songs.getName());
        String coverUrl = mSongs.get(i).getImagePath();
        Picasso.with(mContext).load(coverUrl).into(viewHolder.cover);
    }

    @Override
    public int getItemCount() {
        return mSongs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView cover;
        private TextView songName;

        ViewHolder(View itemView) {
            super(itemView);
            cover = (ImageView) itemView.findViewById(R.id.album_cover);
            songName = (TextView) itemView.findViewById(R.id.album_name);

        }
    }
}
