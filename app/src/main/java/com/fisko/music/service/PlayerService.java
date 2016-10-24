package com.fisko.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.fisko.music.ui.songs.SongsActivity;
import com.fisko.music.utils.Constants;
import com.fisko.music.utils.MathUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class PlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    public interface PlayerCallback {
        void onGetState(float seekPosition, boolean isPlaying, @Nullable Song song);
        void onStateChanged(boolean isPlaying, Song song);
    }

//    private static final int UPDATE_DELAY = 200;
    private static final int NOTIFICATION_ID = 100;
    private static final int INDEX_NOT_INIT = -1;

    private final IBinder mBinder = new LocalBinder();
    private LinkedList<PlayerCallback> mCallbacks = new LinkedList<>();
    private MediaPlayer mMediaPlayer;

    private int mSongIndex = INDEX_NOT_INIT;
    private ArrayList<Song> mSongs;
    private String mAlbumId;

    private boolean wasPrepared = false;


    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public PlayerService() {
        initializePlayer();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void addPlayerListener(PlayerCallback callback) {
        mCallbacks.add(callback);
        notifyGetState(callback);
    }

    public void removePlayerListener(PlayerCallback callback) {
        mCallbacks.remove(callback);
    }

    private void notifyGetState(PlayerCallback callback) {
        float seekPos = 0;
        boolean isPlaying = false;
        if (mMediaPlayer != null) {
            if (wasPrepared) {
                seekPos = (float) mMediaPlayer.getDuration() / mMediaPlayer.getCurrentPosition();
            }
            isPlaying = mMediaPlayer.isPlaying();
        }

        if (isPlaying) {
            Song song = mSongs.get(mSongIndex);
            callback.onGetState(seekPos, true, song);
        } else {
            callback.onGetState(seekPos, false, null);
        }
    }

    private void notifyNextSong(Song song) {
        boolean isPlaying = false;
        if (mMediaPlayer != null) {
            isPlaying = mMediaPlayer.isPlaying();
        }
        for(PlayerCallback callback: mCallbacks) {
            callback.onStateChanged(isPlaying, song);
        }
    }


    private void initializePlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);

//        mTimer = new CountDownTimer(Long.MAX_VALUE, UPDATE_DELAY) {
//            public void onTick(long millisUntilFinished) {
//                notifyCallbacks();
//            }
//
//            public void onFinish() {}
//        }.start();
    }

    public void setCurDataSource() {
        String songPath = mSongs.get(mSongIndex).getPath();
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(songPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play(int songIndex, ArrayList<Song> songs) {
        String albumId = songs.get(songIndex).getAlbumId();
        boolean isPause = !mMediaPlayer.isPlaying();
        boolean isSongNotChanged = albumId.equals(mAlbumId) && songIndex == mSongIndex;

        if (isPause && isSongNotChanged) {
            mMediaPlayer.start();
        } else {
            mSongIndex = songIndex;
            mSongs = songs;
            mAlbumId = albumId;

            setCurDataSource();
            mMediaPlayer.prepareAsync();
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopForeground(true);
    }

    private Notification getNotification() {
        Song song = mSongs.get(mSongIndex);
        Intent songIntent = new Intent(this, SongsActivity.class);
        songIntent.putExtra(Constants.SONG_BUNDLE.OPENED_SONG, song);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.current_playing))
                .setSmallIcon(R.drawable.playing_indicator)
                .setContentText(mSongs.get(mSongIndex).getName())
                .setContentIntent(PendingIntent.getActivity(this, 0, songIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(mSongs == null) {
            return;
        }
        mSongIndex = MathUtils.getPositiveModule(mSongIndex + 1, mSongs.size());
        setCurDataSource();
        mMediaPlayer.prepareAsync();

        Song song = mSongs.get(mSongIndex);
        notifyNextSong(song);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
        wasPrepared = true;
        startForeground(NOTIFICATION_ID, getNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

}
