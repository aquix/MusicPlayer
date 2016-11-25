package com.vlad.player.ui.artists;

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
import com.vlad.player.data.models.Artist;
import com.vlad.player.data.models.Song;
import com.vlad.player.data.services.DbObservableContext;
import com.vlad.player.service.PlayerService;
import com.vlad.player.ui.view.HeaderGridView;
import com.vlad.player.utils.RecentSongsService;

import java.util.List;

public class ArtistsFragment extends Fragment implements
        DbObservableContext.AlbumsRepositoryObserver,
        PlayerService.PlayerCallback {

    static {
        System.loadLibrary("native-lib");
    }

    private static final int PORTRAIT_COLUMNS_COUNT = 2;
    private static final int LANDSCAPE_COLUMNS_COUNT = 3;

    private static final String TAG_LIFECYCLE = "LIFECYCLE";

    private View header;

    private ArtistsListAdapter artistsListAdapter;
    private RecentArtistsAdapter recentArtistsAdapter;
    private DbObservableContext dbService;
    private Handler mainHandler;

    private PlayerService playerService;
    private boolean isServiceBound = false;

    private List<Artist> artists;

    public ArtistsFragment() {}

    public static ArtistsFragment newInstance() {
        return new ArtistsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dbService = DbObservableContext.getInstance(this.getContext());
        this.dbService.addContentObserver(this);
        Log.d(TAG_LIFECYCLE, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artists_fragment, container, false);
        this.header = inflater.inflate(R.layout.artists_list_header, container, false);


        HeaderGridView albumsGrid = (HeaderGridView) view.findViewById(R.id.albums_grid);
        this.setColumnsCount(albumsGrid);
        this.artistsListAdapter = new ArtistsListAdapter(this.getActivity());
        this.addRecentSongsHeader(albumsGrid);
        albumsGrid.setAdapter(this.artistsListAdapter);
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

        this.recentArtistsAdapter = new RecentArtistsAdapter(recentSongs, this.getContext());
        songsList.setAdapter(this.recentArtistsAdapter);
        if(recentSongs.isEmpty()) {
            this.header.setVisibility(View.GONE);
        }
        albumsGrid.addHeaderView(this.header);
    }

    private void loadAlbums() {
        this.artists = this.dbService.getAllArtists();
        this.artistsListAdapter.replaceData(this.artists);
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
    public void onArtistsChanged() {
        this.mainHandler.post(new Runnable() {
            @Override
            public void run() {
                ArtistsFragment.this.loadAlbums();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this.getActivity(), PlayerService.class);
        this.getActivity().startService(intent);
        this.getActivity().bindService(intent, this.serviceConnection, Context.BIND_AUTO_CREATE);

        if (!RecentSongsService.getRecent().isEmpty()) {
            this.header.setVisibility(View.VISIBLE);
            this.recentArtistsAdapter.notifyDataSetChanged();
        }
        Log.d(TAG_LIFECYCLE, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.isServiceBound) {
            this.playerService.removePlayerListener(this);
            this.getActivity().unbindService(this.serviceConnection);
            this.isServiceBound = false;
        }
        this.dbService.removeContentObserver(this);
        Log.d(TAG_LIFECYCLE, "onStop");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.artists_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_from_list_item:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                int position = info.position - 2;
                Artist artist = this.artists.get(position);
                this.artistsListAdapter.removeArtist(artist);
                this.dbService.deleteArtist(artist);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void setActiveArtist(boolean isPlaying, Song song) {
        if (isPlaying) {
            this.artistsListAdapter.setPlayingArtist(song.getArtistId());
        } else {
            this.artistsListAdapter.setPlayingArtist(-1);
        }
    }

    @Override
    public void onNewState(float seekPosition, boolean isPlaying, @Nullable Song song) {
        this.setActiveArtist(isPlaying, song);
    }

    @Override
    public void onNextSong(boolean isPlaying, Song song) {
        this.setActiveArtist(isPlaying, song);
    }

    @Override
    public void onSeekPositionChange(int seekPosition) { }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            ArtistsFragment.this.playerService = binder.getService();
            ArtistsFragment.this.playerService.addPlayerListener(ArtistsFragment.this);
            ArtistsFragment.this.isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            ArtistsFragment.this.isServiceBound = false;
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
