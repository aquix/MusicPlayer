package com.fisko.music.ui.song;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.fisko.music.service.PlayerService;
import com.fisko.music.utils.Constants;
import com.fisko.music.utils.MathUtils;
import com.fisko.music.utils.UIUtils;

import java.util.ArrayList;

public class SongFragment extends Fragment implements PlayerService.PlayerCallback {

    private ViewPager mSongPager;
    private ImageView mPlayButton;


    private PlayerService mService;
    private boolean mBound = false;

    private int mSongIndex;
    private ArrayList<Song> mSongs;
    private boolean isPlaying;

    private Toolbar mToolbar;

    public SongFragment() {}

    public static SongFragment newInstance(int curSong, ArrayList<Song> songs) {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.SONG_BUNDLE.CURRENT_SONG_INDEX, curSong);
        args.putParcelableArrayList(Constants.SONG_BUNDLE.SONGS_LIST, songs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSongIndex = getArguments().getInt(Constants.SONG_BUNDLE.CURRENT_SONG_INDEX);
            mSongs = getArguments().getParcelableArrayList(Constants.SONG_BUNDLE.SONGS_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.song_fragment, container, false);
        mToolbar = UIUtils.setUpToolbar(false, null, (AppCompatActivity) getActivity());

        mSongPager = (ViewPager) view.findViewById(R.id.song_pager);
        PagerAdapter pagerAdapter = new SongPagerAdapter(mSongs, getFragmentManager());
        mSongPager.setAdapter(pagerAdapter);

        View swipePanelControl = view.findViewById(R.id.swipe_control_panel);
        addSwipeGestureControl(swipePanelControl);

        View prevButton = view.findViewById(R.id.prev_song_button);
        View nextButton = view.findViewById(R.id.next_song_button);
        mPlayButton = (ImageView) view.findViewById(R.id.player_control);

        setSongData();

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAnotherSong(-1);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAnotherSong(1);
            }
        });
        mPlayButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        playAnotherSong(0);
                        break;
                }
                return true;
            }
        });

        mSongPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position != mSongIndex) {
                    playAnotherSong(position - mSongIndex);
                }
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.SONG_BUNDLE.CURRENT_SONG_INDEX, mSongIndex);
        outState.putParcelable(Constants.SONG_BUNDLE.OPENED_SONG, mSongs.get(mSongIndex));
    }

    private void setSongData() {
        if (mSongPager.getCurrentItem() != mSongIndex) {
            mSongPager.setCurrentItem(mSongIndex, true);
        }
        String songName = mSongs.get(mSongIndex).getName();
        if (mToolbar != null) {
            mToolbar.setTitle(songName);
        }

        int playImageResource;
        if(isPlaying) {
            playImageResource = android.R.drawable.ic_media_pause;
        } else {
            playImageResource = android.R.drawable.ic_media_play;
        }
        mPlayButton.setImageResource(playImageResource);
    }

    public void playAnotherSong(int offset) {
        if (offset == 0) {
            if(isPlaying) {
                mService.pause();
            } else {
                mService.play(mSongIndex, mSongs);
            }
            isPlaying = !isPlaying;
        } else {
            mSongIndex = MathUtils.getPositiveModule(mSongIndex + offset, mSongs.size());
            if(isPlaying) {
                mService.play(mSongIndex, mSongs);
            } else {
                mService.pause();
            }
        }

        setSongData();
    }

    private void playSong() {
        mService.play(mSongIndex, mSongs);
        setSongData();
    }

    public void addSwipeGestureControl(View panel) {
        final int swipeMinLen = (int) getResources().getDimension(R.dimen.sipe_min_len);
        panel.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0 ;
            float endX = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        endX = event.getX();
                        if(Math.abs(endX - startX) > swipeMinLen) {
                            if (startX > endX) {
                                playAnotherSong(-1);
                            } else {
                                playAnotherSong(1);
                            }
                        }
                        break;
                }
                return true;
            }
        });
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
    public void onGetState(float seekPosition, boolean isPlaying, @Nullable Song song) {
        Song curSong = mSongs.get(mSongIndex);
        this.isPlaying = isPlaying;

        if (curSong.equals(song)) {

        } else {
            if (isPlaying ) {
                playSong();
            }
        }

        setSongData();
    }

    @Override
    public void onStateChanged(boolean isPlaying, Song song) {
        mSongIndex = mSongs.indexOf(song);
        this.isPlaying = isPlaying;
        setSongData();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
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

}