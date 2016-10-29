package com.fisko.music.ui.songs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import com.fisko.music.R;
import com.fisko.music.data.Album;
import com.fisko.music.data.Song;
import com.fisko.music.data.source.MusicDataSource;
import com.fisko.music.data.source.MusicRepository;
import com.fisko.music.data.source.local.MusicLocalDataSource;
import com.fisko.music.service.PlayerService;
import com.fisko.music.utils.Constants;
import com.fisko.music.utils.UIUtils;

import java.io.File;
import java.util.List;

public class SongsFragment extends Fragment implements PlayerService.PlayerCallback {

    private Album mAlbum;

    private SongsListAdapter mAdapter;
    private MusicRepository mRepository;
    private List<Song> mSongs;
    private boolean isSortBySize = true;


    private PlayerService mService;
    private boolean mBound = false;

    public static SongsFragment newInstance(Album album, Song openedSong) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.ALBUM_BUNDLE.ALBUM, album);
        args.putParcelable(Constants.SONG_BUNDLE.OPENED_SONG, openedSong);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Song openedSong = null;
        if (getArguments() != null) {
            mAlbum = getArguments().getParcelable(Constants.ALBUM_BUNDLE.ALBUM);
            openedSong = getArguments().getParcelable(Constants.SONG_BUNDLE.OPENED_SONG);
        }

        MusicDataSource localDataSource = MusicLocalDataSource.getInstance(getContext());
        mRepository = MusicRepository.getInstance(localDataSource);
        if(mAlbum == null && openedSong != null) {
            mAlbum = mRepository.getAlbum(openedSong.getAlbumId());
        }

        if(openedSong != null) {
            List<Song> songs = mRepository.getSongs(mAlbum.getId(), isSortBySize);
            UIUtils.openSongPlayer(openedSong, songs, getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view  = inflater.inflate(R.layout.songs_fragment, container, false);
        UIUtils.setUpToolbar(true, mAlbum.getName() ,(AppCompatActivity) getActivity());

        ListView albumsList = (ListView) view.findViewById(R.id.songs_list);
        mAdapter = new SongsListAdapter(mAlbum, getActivity());
        albumsList.setAdapter(mAdapter);
        loadAlbums();

        registerForContextMenu(albumsList);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.ALBUM_BUNDLE.ALBUM, mAlbum);
    }

    private void loadAlbums() {
        mSongs = mRepository.getSongs(mAlbum.getId(), isSortBySize);
        mAdapter.replaceData(mSongs);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.songs_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.songs_menu_sort:
                isSortBySize = !isSortBySize;
                loadAlbums();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), PlayerService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            mService.removePlayerListener(this);
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_from_list_item:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                int position = info.position;
                Song song = mSongs.get(position);
                removeSong(song);
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void removeSong(Song song) {
        mAdapter.deleteSong(song);
        mRepository.deleteSong(song);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File file = new File(song.getPath());
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(getContext(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setPlayingSong(boolean isPlaying, Song song) {
        if (isPlaying) {
            int songIndex = mSongs.indexOf(song);
            mAdapter.setPlayingSong(songIndex);
        } else {
            mAdapter.setPlayingSong(SongsListAdapter.INDEX_NOT_INIT);
        }
    }

    @Override
    public void onGetState(float seekPosition, boolean isPlaying, @Nullable Song song) {
        setPlayingSong(isPlaying, song);
    }

    @Override
    public void onStateChanged(boolean isPlaying, Song song) {
        setPlayingSong(isPlaying, song);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mService.addPlayerListener(SongsFragment.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}
