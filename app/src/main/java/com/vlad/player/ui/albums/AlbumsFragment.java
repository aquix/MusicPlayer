package com.vlad.player.ui.albums;

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
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.vlad.player.R;
import com.vlad.player.data.Album;
import com.vlad.player.data.Song;
import com.vlad.player.data.source.DbObservableContext;
import com.vlad.player.service.PlayerService;
import com.vlad.player.ui.view.HeaderGridView;
import com.vlad.player.utils.RecentSongsService;

import java.util.ArrayList;
import java.util.List;

public class AlbumsFragment extends Fragment implements
        DbObservableContext.AlbumsRepositoryObserver,
        PlayerService.PlayerCallback {

    static {
        // System.loadLibrary("native-lib");
    }

    private static final int PORTRAIT_COLUMNS_COUNT = 2;
    private static final int LANDSCAPE_COLUMNS_COUNT = 3;

    private static final String TAG_LIFECYCLE = "LIFECYCLE";

    private View header;

    private AlbumsListAdapter albumsListAdapter;
    private AlbumsRecentAdapter albumsRecentAdapter;
    private DbObservableContext musicRepository;
    private Handler mainHandler;

    private PlayerService playerService;
    private boolean isServiceBound = false;

    private List<Album> albums;

    public AlbumsFragment() {}

    public static AlbumsFragment newInstance() {
        return new AlbumsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.musicRepository = DbObservableContext.getInstance(this.getContext());
        this.musicRepository.addContentObserver(this);
        ArrayList<Integer> durations = this.musicRepository.printAllSongs();
        this.checkJNI(durations);
        Log.d(TAG_LIFECYCLE, "onCreate");
    }

    private void checkJNI(ArrayList<Integer> durations) {
        long result = this.sum(durations);
        Log.d("JNI all songs duration", Long.toString(result));

        long startTime = System.currentTimeMillis();
        long sum = 0;
        for(int j = 0; j < 500; ++j) {
            for (Integer value : durations) {
                sum += value;
            }
        }
        Log.d("Java all songs duration", Long.toString(sum));
        Log.d("Java running time", "" + (System.currentTimeMillis() - startTime));

    }

    public long sum(ArrayList<Integer> list) {
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.albums_fragment, container, false);
        this.header = inflater.inflate(R.layout.albums_list_footer, container, false);


        HeaderGridView albumsGrid = (HeaderGridView) view.findViewById(R.id.albums_grid);
        this.setColumnsCount(albumsGrid);
        this.albumsListAdapter = new AlbumsListAdapter(this.getActivity());
        this.addRecentSongsHeader(albumsGrid);
        albumsGrid.setAdapter(this.albumsListAdapter);
        this.loadAlbums();

        this.registerForContextMenu(albumsGrid);

        this.mainHandler = new Handler(this.getContext().getMainLooper());

        Log.d(TAG_LIFECYCLE, "onCreateView");

        return view;
    }

    private void addRecentSongsHeader(HeaderGridView albumsGrid) {
        List<Song> recentSongs = RecentSongsService.getRecent();
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView songsList = (RecyclerView) this.header.findViewById(R.id.recent_songs);
        songsList.setLayoutManager(layoutManager);

        this.albumsRecentAdapter = new AlbumsRecentAdapter(recentSongs, this.getContext());
        songsList.setAdapter(this.albumsRecentAdapter);
        if(recentSongs.isEmpty()) {
            this.header.setVisibility(View.GONE);
        }
        albumsGrid.addHeaderView(this.header);
    }

    private void loadAlbums() {
        this.albums = this.musicRepository.getAlbums();
        this.albumsListAdapter.replaceData(this.albums);
    }

    private void setColumnsCount(HeaderGridView albumsGrid) {
        int orientation = this.getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            albumsGrid.setNumColumns(PORTRAIT_COLUMNS_COUNT);
        } else {
            albumsGrid.setNumColumns(LANDSCAPE_COLUMNS_COUNT);
        }
    }

    @Override
    public void onAlbumsChanged() {
        this.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                AlbumsFragment.this.loadAlbums();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this.getActivity(), PlayerService.class);
        this.getActivity().startService(intent);
        this.getActivity().bindService(intent, this.mConnection, Context.BIND_AUTO_CREATE);

        if (!RecentSongsService.getRecent().isEmpty()) {
            this.header.setVisibility(View.VISIBLE);
            this.albumsRecentAdapter.notifyDataSetChanged();
        }
        Log.d(TAG_LIFECYCLE, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.isServiceBound) {
            this.playerService.removePlayerListener(this);
            this.getActivity().unbindService(this.mConnection);
            this.isServiceBound = false;
        }
        this.musicRepository.removeContentObserver(this);
        Log.d(TAG_LIFECYCLE, "onStop");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_from_list_item:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                int position = info.position - 2;
                Album album = this.albums.get(position);
                this.albumsListAdapter.removeAlbum(album);
                this.musicRepository.deleteAlbum(album);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void setActiveAlbum(boolean isPlaying, Song song) {
        if (isPlaying) {
            this.albumsListAdapter.setPlayingAlbum(song.getAlbumId());
        } else {
            this.albumsListAdapter.setPlayingAlbum(null);
        }
    }

    @Override
    public void onNewState(float seekPosition, boolean isPlaying, @Nullable Song song) {
        this.setActiveAlbum(isPlaying, song);
    }

    @Override
    public void onNextSong(boolean isPlaying, Song song) {
        this.setActiveAlbum(isPlaying, song);
    }

    @Override
    public void onSeekPositionChange(int seekPosition) { }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            AlbumsFragment.this.playerService = binder.getService();
            AlbumsFragment.this.playerService.addPlayerListener(AlbumsFragment.this);
            AlbumsFragment.this.isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            AlbumsFragment.this.isServiceBound = false;
        }
    };

    @Override
    public LoaderManager getLoaderManager() {
        return super.getLoaderManager();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG_LIFECYCLE, "onAttach");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG_LIFECYCLE, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG_LIFECYCLE, "onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG_LIFECYCLE, "onDestroy");
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG_LIFECYCLE, "onDetach");
    }
}
