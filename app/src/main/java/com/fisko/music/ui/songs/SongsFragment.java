package com.fisko.music.ui.songs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.fisko.music.data.source.MusicDataSource;
import com.fisko.music.data.source.MusicRepository;
import com.fisko.music.data.source.local.MusicLocalDataSource;
import com.fisko.music.ui.albums.AlbumsListAdapter;

import java.util.List;

public class SongsFragment extends Fragment {

    private String mAlbumId;

    public ListView mAlbumsList;
    public SongsListAdapter mAdapter;
    public MusicRepository mRepository;

    public SongsFragment() {}

    public static SongsFragment newInstance(String albumId) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putString(AlbumsListAdapter.ALBUM_ID, albumId);
        fragment.setArguments(args);
        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlbumId = getArguments().getString(AlbumsListAdapter.ALBUM_ID);
        }
        MusicDataSource localDataSource = MusicLocalDataSource.getInstance(getContext());
        mRepository = MusicRepository.getInstance(localDataSource);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.song_fragment, container, false);

        mAlbumsList = (ListView) view.findViewById(R.id.songs_list);
        mAdapter = new SongsListAdapter(getActivity(), mAlbumId);
        mAlbumsList.setAdapter(mAdapter);
        loadAlbums();

        return view;
    }

    private void loadAlbums() {
        List<Song> songs = mRepository.getSongs(mAlbumId);
        mAdapter.replaceData(songs);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
