package com.vlad.player.ui.songs;

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

import com.vlad.player.R;
import com.vlad.player.data.models.Artist;
import com.vlad.player.data.models.Song;
import com.vlad.player.data.services.DbObservableContext;
import com.vlad.player.service.PlayerService;
import com.vlad.player.utils.Constants;
import com.vlad.player.utils.UiUtils;

import java.io.File;
import java.util.List;

public class SongsFragment extends Fragment implements PlayerService.PlayerCallback {
    private Artist artist;

    private SongsListAdapter adapter;
    private DbObservableContext dbService;
    private List<Song> songs;
    private boolean isSortBySize = true;

    private PlayerService playerService;
    private boolean isServiceBound = false;

    public static SongsFragment newInstance(Artist artist, Song openedSong) {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.ALBUM_BUNDLE.ALBUM, artist);
        args.putParcelable(Constants.SONG_BUNDLE.OPENED_SONG, openedSong);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Song openedSong = null;
        if (this.getArguments() != null) {
            this.artist = this.getArguments().getParcelable(Constants.ALBUM_BUNDLE.ALBUM);
            openedSong = this.getArguments().getParcelable(Constants.SONG_BUNDLE.OPENED_SONG);
        }

        this.dbService = DbObservableContext.getInstance(this.getContext());
        if(this.artist == null && openedSong != null) {
            this.artist = this.dbService.getArtist(openedSong.getArtistId());
        }

        if(openedSong != null) {
            List<Song> songs = this.dbService.getSongsForArtist(this.artist.getId(), this.isSortBySize);
            UiUtils.openSongPlayer(openedSong, songs, this.getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.setHasOptionsMenu(true);
        View view  = inflater.inflate(R.layout.songs_fragment, container, false);
        UiUtils.setUpToolbar(true, this.artist.getName() ,(AppCompatActivity) this.getActivity());

        ListView albumsList = (ListView) view.findViewById(R.id.songs_list);
        this.adapter = new SongsListAdapter(this.artist, this.getActivity());
        albumsList.setAdapter(this.adapter);
        this.loadAlbums();

        this.registerForContextMenu(albumsList);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.ALBUM_BUNDLE.ALBUM, this.artist);
    }

    private void loadAlbums() {
        this.songs = this.dbService.getSongsForArtist(this.artist.getId(), this.isSortBySize);
        this.adapter.replaceData(this.songs);
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
                this.isSortBySize = !this.isSortBySize;
                this.loadAlbums();
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
        Intent intent = new Intent(this.getActivity(), PlayerService.class);
        this.getActivity().bindService(intent, this.mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.isServiceBound) {
            this.playerService.removePlayerListener(this);
            this.getActivity().unbindService(this.mConnection);
            this.isServiceBound = false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.songs_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        Song song = this.songs.get(position);

        if (song == null) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.remove_song_from_disk:
                File file = new File(song.getPath());
                file.delete();
                this.removeSong(song);
                break;
            case R.id.remove_song_from_list:
                this.removeSong(song);
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void removeSong(Song song) {
        this.adapter.deleteSong(song);
        this.dbService.deleteSong(song);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File file = new File(song.getPath());
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(this.getContext(), R.string.delete_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setPlayingSong(boolean isPlaying, Song song) {
        if (isPlaying) {
            int songIndex = this.songs.indexOf(song);
            this.adapter.setPlayingSong(songIndex);
        } else {
            this.adapter.setPlayingSong(-1);
        }
    }

    @Override
    public void onNewState(float seekPosition, boolean isPlaying, @Nullable Song song) {
        this.setPlayingSong(isPlaying, song);
    }

    @Override
    public void onNextSong(boolean isPlaying, Song song) {
        this.setPlayingSong(isPlaying, song);
    }

    @Override
    public void onSeekPositionChange(int seekPosition) { }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            SongsFragment.this.playerService = binder.getService();
            SongsFragment.this.playerService.addPlayerListener(SongsFragment.this);
            SongsFragment.this.isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            SongsFragment.this.isServiceBound = false;
        }
    };

}
