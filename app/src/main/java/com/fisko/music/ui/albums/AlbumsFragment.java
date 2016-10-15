package com.fisko.music.ui.albums;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.fisko.music.R;
import com.fisko.music.data.Album;
import com.fisko.music.data.source.MusicDataSource;
import com.fisko.music.data.source.MusicRepository;
import com.fisko.music.data.source.local.MusicLocalDataSource;

import java.util.List;

public class AlbumsFragment extends Fragment implements MusicRepository.AlbumsRepositoryObserver {

    private ListView mAlbumsList;
    private AlbumsListAdapter mAdapter;
    private MusicRepository mRepository;

    public AlbumsFragment() {}

    public static AlbumsFragment newInstance() {
        return new AlbumsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicDataSource localDataSource = MusicLocalDataSource.getInstance(getContext());
        mRepository = MusicRepository.getInstance(localDataSource);
        mRepository.addContentObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.albums_fragment, container, false);

        mAlbumsList = (ListView) view.findViewById(R.id.albums_list);
        mAdapter = new AlbumsListAdapter(getActivity());
        mAlbumsList.setAdapter(mAdapter);
        loadAlbums();

        return view;
    }

    private void loadAlbums() {
        List<Album> albums = mRepository.getAlbums();
        mAdapter.replaceData(albums);
    }

    @Override
    public void onAlbumsChanged() {
        loadAlbums();
    }

    @Override
    public void onStop() {
        super.onStop();
        mRepository.removeContentObserver(this);
    }
}
