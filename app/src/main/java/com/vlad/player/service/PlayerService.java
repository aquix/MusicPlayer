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
import android.util.Log;

import com.vlad.player.R;
import com.vlad.player.data.models.Song;
import com.vlad.player.ui.songs.SongsActivity;
import com.vlad.player.utils.Constants;
import com.vlad.player.utils.RecentSongsService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;


public class PlayerService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    public interface PlayerCallback {
        void onNewState(float seekPosition, boolean isPlaying, @Nullable Song song);

        void onNextSong(boolean isPlaying, Song song);

        void onSeekPositionChange(int seekPosition);
    }

    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    private static final int NOTIFICATION_ID = 100;

    private final IBinder binder = new LocalBinder();
    private LinkedList<PlayerCallback> callbacks = new LinkedList<>();
    private MediaPlayer mediaPlayer;

    private int currentSongIndex = -1;
    private ArrayList<Song> songs;
    private long artistId;
    private boolean wasPrepared = false;

    private Thread positionNotifierThread;

    public PlayerService() {
        this.initializePlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void addPlayerListener(PlayerCallback callback) {
        this.callbacks.add(callback);
        this.notifyNewState(callback);
    }

    public void removePlayerListener(PlayerCallback callback) {
        this.callbacks.remove(callback);
    }

    private void notifyNewState(PlayerCallback callback) {
        Log.d("dsfjsadf", "get state");
        float seekPos = 1;
        boolean isPlaying = false;
        if (this.mediaPlayer != null) {
            if (this.wasPrepared) {
                seekPos = (float) this.mediaPlayer.getDuration() / this.mediaPlayer.getCurrentPosition();
            }
            isPlaying = this.mediaPlayer.isPlaying();
        }

        Song currentSong = null;
        if (isPlaying) {
            currentSong = this.songs.get(this.currentSongIndex);
        }

        callback.onNewState(seekPos, isPlaying, currentSong);
    }

    private void notifyNextSong(Song song) {
        boolean isPlaying = false;
        if (this.mediaPlayer != null) {
            isPlaying = this.mediaPlayer.isPlaying();
        }

        for (PlayerCallback callback : this.callbacks) {
            callback.onNextSong(isPlaying, song);
        }
    }

    private void initializePlayer() {
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.mediaPlayer.setOnPreparedListener(this);
        this.mediaPlayer.setOnCompletionListener(this);

        this.positionNotifierThread = new Thread(new Runnable() {
            @Override
            public void run() {
            while (true) {
                try {
                    Thread.sleep(2000);
                    for (PlayerCallback callback : PlayerService.this.callbacks) {
                        callback.onSeekPositionChange(
                                PlayerService.this.mediaPlayer.getCurrentPosition());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            }
        });
    }

    public void setCurrentDataSource() {
        String songPath = this.songs.get(this.currentSongIndex).getPath();
        try {
            this.mediaPlayer.reset();
            this.mediaPlayer.setDataSource(songPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play(int songIndex, ArrayList<Song> songs) {
        long albumId = songs.get(songIndex).getArtistId();
        boolean isPause = !this.mediaPlayer.isPlaying();
        boolean isSongNotChanged = albumId == this.artistId && songIndex == this.currentSongIndex;

        if (isPause && isSongNotChanged) {
            this.mediaPlayer.start();
        } else {
            this.currentSongIndex = songIndex;
            this.songs = songs;
            this.artistId = albumId;

            this.setCurrentDataSource();
            this.mediaPlayer.prepareAsync();
        }
    }

    public void pause() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.pause();
        }

        this.positionNotifierThread.interrupt();
    }

    public void stop() {
        if (this.mediaPlayer != null) {
            this.mediaPlayer.stop();
        }

        this.positionNotifierThread.interrupt();

        this.stopForeground(true);
        this.stopSelf();
    }

    public void seekTo(int songPosition) {
        this.mediaPlayer.seekTo(songPosition);
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
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentText(this.songs.get(this.currentSongIndex).getName())
                .setContentIntent(PendingIntent.getActivity(this, 0, songIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        this.positionNotifierThread.interrupt();
        if (this.songs == null) {
            return;
        }
        this.currentSongIndex = (this.currentSongIndex + 1) % this.songs.size();
        this.setCurrentDataSource();
        this.mediaPlayer.prepareAsync();

        Song song = this.songs.get(this.currentSongIndex);
        this.notifyNextSong(song);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer.start();
        this.wasPrepared = true;
        RecentSongsService.addToRecent(this.songs.get(this.currentSongIndex));
        this.startForeground(NOTIFICATION_ID, this.getNotification());
        Notification n = this.getNotification();
        for (PlayerCallback callback : this.callbacks) {
            this.notifyNewState(callback);
        }
        if (!this.positionNotifierThread.isAlive()) {
            this.positionNotifierThread.start();
        }
        Log.d("thread", "started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.release();
    }
}
