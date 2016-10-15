package com.fisko.music.ui.songs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.fisko.music.service.PlayerService;

import java.util.ArrayList;

public class SongFragment extends Fragment implements PlayerService.PlayerCallback {

    public ViewPager mSongPager;
    public View mPrevButton;
    public View mPlayButton;
    public View mNextButton;

    PlayerService mService;
    boolean mBound = false;

    private int mSongIndex;
    private ArrayList<Song> mSongs;
    private String mAlbumId;

    public SongFragment() {}

    public static SongFragment newInstance(int curSong, ArrayList<Song> songs, String albumId) {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putInt(SongsListAdapter.CURRENT_SONG_INDEX, curSong);
        args.putParcelableArrayList(SongsListAdapter.SONGS_LIST, songs);
        args.putString(SongsListAdapter.ALBUM_ID, albumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSongIndex = getArguments().getInt(SongsListAdapter.CURRENT_SONG_INDEX);
            mSongs = getArguments().getParcelableArrayList(SongsListAdapter.SONGS_LIST);
            mAlbumId = getArguments().getString(SongsListAdapter.ALBUM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.song_fragment, container, false);

        mSongPager = (ViewPager) view.findViewById(R.id.song_pager);

        mPrevButton = view.findViewById(R.id.prev_song_button);
        mPlayButton = view.findViewById(R.id.player_control);
        mNextButton = view.findViewById(R.id.next_song_button);

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveSong(-1);
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveSong(1);
            }
        });

        return view;
    }

    public void moveSong(int offset) {
        mSongIndex = (mSongIndex + offset) % mSongs.size();
        mSongPager.setCurrentItem(mSongIndex, true);
        mService.play(mSongIndex, mSongs, mAlbumId);
        mService.addPlayerListener(this);

        String songName = mSongs.get(mSongIndex).getName();
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(songName);
        }
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

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mService.addPlayerListener(SongFragment.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void OnSongInfoChanged(float seekPos, int curSong, int albumId, boolean isPlaying) {

    }
}
