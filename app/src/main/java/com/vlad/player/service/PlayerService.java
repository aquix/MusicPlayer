package com.vlad.player.service;

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

import com.vlad.player.R;
import com.vlad.player.data.Song;
import com.vlad.player.ui.songs.SongsActivity;
import com.vlad.player.utils.Constants;
import com.vlad.player.utils.MathUtils;
import com.vlad.player.utils.MusicUtils;

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

    private final IBinder binder = new LocalBinder();
    private LinkedList<PlayerCallback> callbacks = new LinkedList<>();
    private MediaPlayer mediaPlayer;

    private int currentSongIndex = INDEX_NOT_INIT;
    private ArrayList<Song> songs;
    private String albumId;

    private boolean wasPrepared = false;


    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    public PlayerService() {
        this.initializePlayer();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void addPlayerListener(PlayerCallback callback) {
        this.callbacks.add(callback);
        this.notifyGetState(callback);
    }

    public void removePlayerListener(PlayerCallback callback) {
        this.callbacks.remove(callback);
    }

    private void notifyGetState(PlayerCallback callback) {
        float seekPos = 0;
        boolean isPlaying = false;
        if (this.mediaPlayer != null) {
            if (this.wasPrepared) {
                seekPos = (float) this.mediaPlayer.getDuration() / this.mediaPlayer.getCurrentPosition();
            }
            isPlaying = this.mediaPlayer.isPlaying();
        }

        if (isPlaying) {
            Song song = this.songs.get(this.currentSongIndex);
            callback.onGetState(seekPos, true, song);
        } else {
            callback.onGetState(seekPos, false, null);
        }
    }

    private void notifyNextSong(Song song) {
        boolean isPlaying = false;
        if (this.mediaPlayer != null) {
            isPlaying = this.mediaPlayer.isPlaying();
        }
        for(PlayerCallback callback: this.callbacks) {
            callback.onStateChanged(isPlaying, song);
        }
    }


    private void initializePlayer() {
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnCompletionListener(this);

//        mTimer = new CountDownTimer(Long.MAX_VALUE, UPDATE_DELAY) {
//            public void onTick(long millisUntilFinished) {
//                notifyCallbacks();
//            }
//
//            public void onFinish() {}
//        }.start();
    }

    public void setCurDataSource() {
        String songPath = this.songs.get(this.currentSongIndex).getPath();
        try {
            this.mediaPlayer.reset();
            this.mediaPlayer.setDataSource(songPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play(int songIndex, ArrayList<Song> songs) {
        String albumId = songs.get(songIndex).getAlbumId();
        boolean isPause = !this.mediaPlayer.isPlaying();
        boolean isSongNotChanged = albumId.equals(this.albumId) && songIndex == this.currentSongIndex;

        if (isPause && isSongNotChanged) {
            this.mediaPlayer.start();
        } else {
            this.currentSongIndex = songIndex;
            this.songs = songs;
            this.albumId = albumId;

            this.setCurDataSource();
            this.mediaPlayer.prepareAsync();
        }
    }

    public void pause() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.pause();
        }
    }

    public void release() {
        if (this.mediaPlayer != null) {
            try {
                this.mediaPlayer.release();
                this.mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.stopForeground(true);
    }

    private Notification getNotification() {
        Song song = this.songs.get(this.currentSongIndex);
        Intent songIntent = new Intent(this, SongsActivity.class);
        songIntent.putExtra(Constants.SONG_BUNDLE.OPENED_SONG, song);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(this.getString(R.string.current_playing))
                .setSmallIcon(R.drawable.playing_indicator)
                .setContentText(this.songs.get(this.currentSongIndex).getName())
                .setContentIntent(PendingIntent.getActivity(this, 0, songIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(this.songs == null) {
            return;
        }
        this.currentSongIndex = MathUtils.getPositiveModule(this.currentSongIndex + 1, this.songs.size());
        this.setCurDataSource();
        this.mediaPlayer.prepareAsync();

        Song song = this.songs.get(this.currentSongIndex);
        this.notifyNextSong(song);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer.start();
        this.wasPrepared = true;
        MusicUtils.addToRecent(this.songs.get(this.currentSongIndex));
        this.startForeground(NOTIFICATION_ID, this.getNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.release();
    }

}
