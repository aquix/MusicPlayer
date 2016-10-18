package com.fisko.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.fisko.music.ui.songs.SongsActivity;
import com.fisko.music.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class PlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    public interface PlayerCallback {
        void OnSongInfoChanged(float seekPos, int curSong, String albumId, boolean isPlaying);
    }

    private static final int UPDATE_DELAY = 100;
    private static final int NOTIFICATION_ID = 100;

    private final IBinder mBinder = new LocalBinder();
    private LinkedList<PlayerCallback> mCallbacks = new LinkedList<>();
    private CountDownTimer mTimer;
    private MediaPlayer mMediaPlayer;

    private int mSongIndex;
    private ArrayList<Song> mSongs;
    private String mAlbumId;


    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
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
        notifyCallbacks();
    }

    public void removePlayerListener(PlayerCallback callback) {
        mCallbacks.remove(callback);
        notifyCallbacks();
    }

    private void notifyCallbacks() {
        float seekPos = (float) mMediaPlayer.getDuration() / mMediaPlayer.getCurrentPosition();
        boolean isPlaying = mMediaPlayer.isPlaying();
        for(PlayerCallback callback: mCallbacks) {
            callback.OnSongInfoChanged(seekPos, mSongIndex, mAlbumId, isPlaying);
        }
    }

    private void initializePlayer() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        mTimer = new CountDownTimer(Long.MAX_VALUE, UPDATE_DELAY) {
            public void onTick(long millisUntilFinished) {
                notifyCallbacks();
            }

            public void onFinish() {}
        }.start();
    }

    public void setCurDataSource() {
        String songPath = mSongs.get(mSongIndex).getPath();
        try {
            mMediaPlayer.setDataSource(songPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play(int songIndex, ArrayList<Song> songs) {
        String albumId = songs.get(songIndex).getAlbumId();
        boolean isPause = mMediaPlayer != null && !mMediaPlayer.isPlaying();
        boolean isSongNotChanged = albumId.equals(mAlbumId) && songIndex == mSongIndex;

        if (isPause && isSongNotChanged) {
            mMediaPlayer.start();
        } else {
            mSongIndex = songIndex;
            mSongs = songs;
            mAlbumId = albumId;

            initializePlayer();
            setCurDataSource();
        }

        mMediaPlayer.prepareAsync();
    }

    public void pause() {
        mMediaPlayer.pause();
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
        if (mTimer != null) {
            mTimer.cancel();
        }
        stopForeground(true);
    }

    private Notification getNotification() {
        Song song = mSongs.get(mSongIndex);
        Intent songIntent = new Intent(this, SongsActivity.class);
        songIntent.putExtra(Constants.SONG_BUNDLE.OPENED_SONG, song);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.current_playing))
            .setContentText(mSongs.get(mSongIndex).getName())
            .setContentIntent(PendingIntent.getActivity(this, 0, songIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        return builder.build();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mSongIndex = (mSongIndex + 1) % mSongs.size();
        setCurDataSource();
        notifyCallbacks();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
        notifyCallbacks();
        startForeground(NOTIFICATION_ID, getNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

}
