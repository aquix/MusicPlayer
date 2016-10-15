package com.fisko.music.ui.albums;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fisko.music.R;
import com.fisko.music.data.Album;
import com.fisko.music.ui.songs.SongsActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AlbumsListAdapter extends BaseAdapter {

    public static final String ALBUM_ID = "SONGS_LIST";

    private LayoutInflater mInflater;
    private ArrayList<Album> mAlbums;
    private Activity mActivity;


    AlbumsListAdapter(Activity activity) {
        mInflater = LayoutInflater.from(activity);
        mAlbums = new ArrayList<>();
        mActivity = activity;
    }

    void replaceData(List<Album> albums) {
        mAlbums = new ArrayList<>(albums);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAlbums.size();
    }

    @Override
    public Album getItem(int position) {
        return mAlbums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {
        TextView albumName;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final Album album = getItem(position);
        ViewHolder holder;

        if(view == null || view.getTag() == null) {
            view = mInflater.inflate(R.layout.albums_list_item, parent, false);
            holder = new ViewHolder();

            holder.albumName = (TextView) view.findViewById(R.id.album_name);

            fillHolder(holder, album);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
            fillHolder(holder, album);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlbum(album);
            }
        });

        return view;
    }

    private void openAlbum(Album album) {
        Intent intent = new Intent(mActivity, SongsActivity.class);
        intent.putExtra(ALBUM_ID, album.getId());
        mActivity.startActivity(intent);
    }

    private void fillHolder(ViewHolder holder, Album album) {
        holder.albumName.setText(album.getName());
    }


}