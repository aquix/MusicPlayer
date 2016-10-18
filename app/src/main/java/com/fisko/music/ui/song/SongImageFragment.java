package com.fisko.music.ui.song;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.squareup.picasso.Picasso;


public class SongImageFragment extends Fragment {

    static final String SONG_INSTANCE = "SONG_INSTANCE";

    private Song mSong;

    public SongImageFragment () {}

    public static SongImageFragment newInstance(Song song) {
        SongImageFragment fragment = new SongImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(SONG_INSTANCE, song);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSong = getArguments().getParcelable(SONG_INSTANCE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.song_pager_item, container, false);

        ImageView image = (ImageView) view.findViewById(R.id.song_image);
        String imageUrl = mSong.getImagePath();
        Picasso.with(getContext()).load(imageUrl).into(image);

        return view;
    }
}
