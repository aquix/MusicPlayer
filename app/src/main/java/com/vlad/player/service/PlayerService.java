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
        initializePlayer();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void addPlayerListener(PlayerCallback callback) {
        callbacks.add(callback);
        notifyGetState(callback);
    }

    public void removePlayerListener(PlayerCallback callback) {
        callbacks.remove(callback);
    }

    private void notifyGetState(PlayerCallback callback) {
        float seekPos = 0;
        boolean isPlaying = false;
        if (mediaPlayer != null) {
            if (wasPrepared) {
                seekPos = (float) mediaPlayer.getDuration() / mediaPlayer.getCurrentPosition();
            }
            isPlaying = mediaPlayer.isPlaying();
        }

        if (isPlaying) {
            Song song = songs.get(currentSongIndex);
            callback.onGetState(seekPos, true, song);
        } else {
            callback.onGetState(seekPos, false, null);
        }
    }

    private void notifyNextSong(Song song) {
        boolean isPlaying = false;
        if (mediaPlayer != null) {
            isPlaying = mediaPlayer.isPlaying();
        }
        for(PlayerCallback callback: callbacks) {
            callback.onStateChanged(isPlaying, song);
        }
    }


    private void initializePlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);

//        mTimer = new CountDownTimer(Long.MAX_VALUE, UPDATE_DELAY) {
//            public void onTick(long millisUntilFinished) {
//                notifyCallbacks();
//            }
//
//            public void onFinish() {}
//        }.start();
    }

    public void setCurDataSource() {
        String songPath = songs.get(currentSongIndex).getPath();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(songPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void play(int songIndex, ArrayList<Song> songs) {
        String albumId = songs.get(songIndex).getAlbumId();
        boolean isPause = !mediaPlayer.isPlaying();
        boolean isSongNotChanged = albumId.equals(this.albumId) && songIndex == currentSongIndex;

        if (isPause && isSongNotChanged) {
            mediaPlayer.start();
        } else {
            currentSongIndex = songIndex;
            this.songs = songs;
            this.albumId = albumId;

            setCurDataSource();
            mediaPlayer.prepareAsync();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopForeground(true);
    }

    private Notification getNotification() {
        Song song = songs.get(currentSongIndex);
        Intent songIntent = new Intent(this, SongsActivity.class);
        songIntent.putExtra(Constants.SONG_BUNDLE.OPENED_SONG, song);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getString(R.string.current_playing))
                .setSmallIcon(R.drawable.playing_indicator)
                .setContentText(songs.get(currentSongIndex).getName())
                .setContentIntent(PendingIntent.getActivity(this, 0, songIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if(songs == null) {
            return;
        }
        currentSongIndex = MathUtils.getPositiveModule(currentSongIndex + 1, songs.size());
        setCurDataSource();
        this.mediaPlayer.prepareAsync();

        Song song = songs.get(currentSongIndex);
        notifyNextSong(song);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.mediaPlayer.start();
        wasPrepared = true;
        MusicUtils.addToRecent(songs.get(currentSongIndex));
        startForeground(NOTIFICATION_ID, getNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

}
