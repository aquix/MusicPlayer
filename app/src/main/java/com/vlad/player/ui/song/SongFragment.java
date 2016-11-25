package com.vlad.player.ui.song;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.os.Handler;

import com.vlad.player.R;
import com.vlad.player.data.models.Song;
import com.vlad.player.service.PlayerService;
import com.vlad.player.utils.Constants;
import com.vlad.player.utils.UiUtils;

import java.util.ArrayList;

public class SongFragment extends Fragment implements PlayerService.PlayerCallback {
    private ViewPager songPager;
    private ImageView playButton;
    private SeekBar seekBar;

    private PlayerService playerService;
    private boolean isServiceBound = false;

    private int songIndex;
    private ArrayList<Song> songs;
    private boolean isPlaying;

    private Toolbar toolbar;

    public SongFragment() { }

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
        if (this.getArguments() != null) {
            this.songIndex = this.getArguments().getInt(Constants.SONG_BUNDLE.CURRENT_SONG_INDEX);
            this.songs = this.getArguments().getParcelableArrayList(Constants.SONG_BUNDLE.SONGS_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.song_fragment, container, false);
        this.toolbar = UiUtils.setUpToolbar(false, null, (AppCompatActivity) this.getActivity());

        this.songPager = (ViewPager) view.findViewById(R.id.song_pager);
        PagerAdapter pagerAdapter = new SongPagerAdapter(this.songs, this.getFragmentManager());
        this.songPager.setAdapter(pagerAdapter);

        View swipePanelControl = view.findViewById(R.id.swipe_control_panel);
        this.addSwipeGestureControl(swipePanelControl);

        View prevButton = view.findViewById(R.id.prev_song_button);
        View nextButton = view.findViewById(R.id.next_song_button);
        this.playButton = (ImageView) view.findViewById(R.id.player_control);

        this.seekBar = (SeekBar) view.findViewById(R.id.song_seek_bar);


        this.updateSongData();

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongFragment.this.playAnotherSong(-1);
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongFragment.this.playAnotherSong(1);
            }
        });
        this.playButton.setOnTouchListener(new View.OnTouchListener() {
            private int LONG_PRESS_TIME = 500;
            private boolean wasLongPress = false;

            final Handler handler = new Handler();
            Runnable longPressed = new Runnable() {
                public void run() {
                    wasLongPress = true;
                    SongFragment.this.stopSong();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        this.handler.postDelayed(this.longPressed, this.LONG_PRESS_TIME);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        this.handler.removeCallbacks(this.longPressed);
                        break;
                    case MotionEvent.ACTION_UP:
                        this.handler.removeCallbacks(this.longPressed);
                        if (!this.wasLongPress) {
                            SongFragment.this.togglePlayPause();
                        }
                        this.wasLongPress = false;
                        break;
                }
                return true;
            }
        });

        this.songPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position != SongFragment.this.songIndex) {
                    SongFragment.this.playAnotherSong(position - SongFragment.this.songIndex);
                }
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.SONG_BUNDLE.CURRENT_SONG_INDEX, this.songIndex);
        outState.putParcelable(Constants.SONG_BUNDLE.OPENED_SONG, this.songs.get(this.songIndex));
    }

    private void updateSongData() {
        if (this.songPager.getCurrentItem() != this.songIndex) {
            this.songPager.setCurrentItem(this.songIndex, true);
        }
        String songName = this.songs.get(this.songIndex).getTitle();
        if (this.toolbar != null) {
            this.toolbar.setTitle(songName);
        }

        this.updatePlayPauseButton();
        this.seekBar.setMax(this.songs.get(this.songIndex).getDuration());
        this.seekBar.setProgress(0);
    }

    public void playAnotherSong(int offset) {
        this.songIndex = (this.songIndex + this.songs.size() + this.songs.size() + offset) % this.songs.size();
        if(this.isPlaying) {
            this.playerService.play(this.songIndex, this.songs);
        } else {
            this.playerService.pause();
        }

        this.updateSongData();
    }

    public void togglePlayPause() {
        if(this.isPlaying) {
            this.playerService.pause();
        } else {
            this.playerService.play(this.songIndex, this.songs);
        }
        this.isPlaying = !this.isPlaying;
        this.updatePlayPauseButton();
    }

    public void stopSong() {
        this.playerService.stop();

        this.isPlaying = false;
        this.updatePlayPauseButton();
    }

    private void playSong() {
        this.playerService.play(this.songIndex, this.songs);
        this.updateSongData();
    }

    public void addSwipeGestureControl(View panel) {
        final int swipeMinLen = (int) this.getResources().getDimension(R.dimen.sipe_min_len);
        panel.setOnTouchListener(new View.OnTouchListener() {
            float startX = 0 ;
            float endX = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        this.startX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        this.endX = event.getX();
                        if(Math.abs(this.endX - this.startX) > swipeMinLen) {
                            if (this.startX > this.endX) {
                                SongFragment.this.playAnotherSong(-1);
                            } else {
                                SongFragment.this.playAnotherSong(1);
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
        Intent intent = new Intent(this.getActivity(), PlayerService.class);
        this.getActivity().bindService(intent, this.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.isServiceBound) {
            this.playerService.removePlayerListener(this);
            this.getActivity().unbindService(this.serviceConnection);
            this.isServiceBound = false;
        }
    }

    @Override
    public void onNewState(float seekPosition, boolean isPlaying, @Nullable Song song) {
        Song currentSong = this.songs.get(this.songIndex);
        this.isPlaying = isPlaying;

        if (!currentSong.equals(song)) {
            if (isPlaying) {
                this.playSong();
            }
        }

        this.seekBar.setMax(this.songs.get(this.songIndex).getDuration());
        this.updateSongData();
    }

    @Override
    public void onNextSong(boolean isPlaying, Song song) {
        this.songIndex = this.songs.indexOf(song);
        this.isPlaying = isPlaying;
        this.updateSongData();
    }

    @Override
    public void onSeekPositionChange(int seekPosition) {
        this.seekBar.setProgress(seekPosition);
    }

    private void updatePlayPauseButton() {
        int playImageResource;
        if(this.isPlaying) {
            playImageResource = android.R.drawable.ic_media_pause;
        } else {
            playImageResource = android.R.drawable.ic_media_play;
        }
        this.playButton.setImageResource(playImageResource);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            SongFragment.this.playerService = binder.getService();
            SongFragment.this.playerService.addPlayerListener(SongFragment.this);


            SongFragment.this.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) { }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    Log.d("pr", "changed");
                    if(SongFragment.this.playerService != null){
                        SongFragment.this.playerService.seekTo(seekBar.getProgress());
                    }
                }
            });

            SongFragment.this.isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            SongFragment.this.isServiceBound = false;
        }
    };

}