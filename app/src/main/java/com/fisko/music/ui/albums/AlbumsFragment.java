package com.fisko.music.ui.albums;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.fisko.music.R;
import com.fisko.music.data.Album;
import com.fisko.music.data.Song;
import com.fisko.music.data.source.MusicDataSource;
import com.fisko.music.data.source.MusicRepository;
import com.fisko.music.data.source.local.MusicLocalDataSource;
import com.fisko.music.service.PlayerService;
import com.fisko.music.ui.view.HeaderGridView;
import com.fisko.music.utils.MusicUtils;

import java.util.List;

public class AlbumsFragment extends Fragment implements
        MusicRepository.AlbumsRepositoryObserver,
        PlayerService.PlayerCallback {

    private static final int PORTRAIT_COLUMNS_COUNT = 2;
    private static final int LANDSCAPE_COLUMNS_COUNT = 3;

    private View mHeader;

    private AlbumsListAdapter mAdapter;
    private AlbumsRecentAdapter mRecentSongsAdapter;
    private MusicRepository mRepository;
    private Handler mMainHandler;

    private PlayerService mService;
    private boolean mBound = false;

    private List<Album> mAlbums;

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
        mHeader = inflater.inflate(R.layout.albums_list_footer, container, false);


        HeaderGridView albumsGrid = (HeaderGridView) view.findViewById(R.id.albums_grid);
        setColumnsCount(albumsGrid);
        mAdapter = new AlbumsListAdapter(getActivity());
        addRecentSongsHeader(albumsGrid);
        albumsGrid.setAdapter(mAdapter);
        loadAlbums();

        mMainHandler = new Handler(getContext().getMainLooper());

        return view;
    }

    private void addRecentSongsHeader(HeaderGridView albumsGrid) {
        List<Song> recentSongs = MusicUtils.getRecent();
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView songsList = (RecyclerView) mHeader.findViewById(R.id.recent_songs);
        songsList.setLayoutManager(layoutManager);

        mRecentSongsAdapter = new AlbumsRecentAdapter(recentSongs, getContext());
        songsList.setAdapter(mRecentSongsAdapter);
        if(recentSongs.isEmpty()) {
            mHeader.setVisibility(View.GONE);
        }
        albumsGrid.addHeaderView(mHeader);
    }

    private void loadAlbums() {
        mAlbums = mRepository.getAlbums();
        mAdapter.replaceData(mAlbums);
    }

    private void setColumnsCount(HeaderGridView albumsGrid) {
        int orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            albumsGrid.setNumColumns(PORTRAIT_COLUMNS_COUNT);
        } else {
            albumsGrid.setNumColumns(LANDSCAPE_COLUMNS_COUNT);
        }
    }

    @Override
    public void onAlbumsChanged() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                loadAlbums();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), PlayerService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (!MusicUtils.getRecent().isEmpty()) {
            mHeader.setVisibility(View.VISIBLE);
            mRecentSongsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            mService.removePlayerListener(this);
            getActivity().unbindService(mConnection);
            mBound = false;
        }
        mRepository.removeContentObserver(this);
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
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                int position = info.position;
                Album album = mAlbums.get(position);
                mAdapter.removeAlbum(album);
                mRepository.deleteAlbum(album);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void setActiveAlbum(boolean isPlaying, Song song) {
        if (isPlaying) {
            mAdapter.setPlayingAlbum(song.getAlbumId());
        } else {
            mAdapter.setPlayingAlbum(null);
        }
    }

    @Override
    public void onGetState(float seekPosition, boolean isPlaying, @Nullable Song song) {
        setActiveAlbum(isPlaying, song);
    }

    @Override
    public void onStateChanged(boolean isPlaying, Song song) {
        setActiveAlbum(isPlaying, song);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mService.addPlayerListener(AlbumsFragment.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

}
