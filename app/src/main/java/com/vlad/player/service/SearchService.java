package com.vlad.player.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.vlad.player.data.models.Artist;
import com.vlad.player.data.models.Song;
import com.vlad.player.data.source.DbObservableContext;
import com.vlad.player.utils.MusicUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchService extends Service {
    private final IBinder binder = new LocalBinder();
    private final DbObservableContext repository;
    private boolean isSearchActive = false;

    public SearchService() {
        this.repository = DbObservableContext.getInstance(this);
    }

    public class LocalBinder extends Binder {
        public SearchService getService() {
            return SearchService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public void startMusicSearch() {
        if (!this.isSearchActive) {
            MusicSearcherThread searcher = new MusicSearcherThread();
            new Thread(searcher).start();
        }
    }

    private class MusicSearcherThread implements Runnable {
        @Override
        public void run() {
            File musicFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            String musicFolderPath = musicFolder.getAbsolutePath();
            File searchStart = new File(musicFolderPath);
            if (!searchStart.exists() && !searchStart.isDirectory()) {
                return;
            }

            ArrayList<File> allSongFiles = new ArrayList<>();
            this.iterateFiles(searchStart.listFiles(), allSongFiles);
            HashMap<String, List<Song>> groupedSongs = new HashMap<>();
            for (File songFile : allSongFiles) {
                MusicUtils.SongInfo songInfo = MusicUtils.extractSongInfo(songFile.getAbsolutePath());
                String albumCover = MusicUtils.getCover(songInfo);
                String songTitle = songInfo.title;
                if (songTitle == null || songTitle.isEmpty()) {
                    int nameLength = songFile.getName().length();
                    // exclude .mp3 extension
                    songTitle = songFile.getName().substring(nameLength - 4);
                }

                Song song = new Song(songTitle, songFile.getAbsolutePath(), albumCover, songInfo.duration, 0);
                if (!groupedSongs.containsKey(songInfo.artist)) {
                    groupedSongs.put(songInfo.artist, new ArrayList<Song>());
                }

                groupedSongs.get(songInfo.artist).add(song);
            }

            for (String artistName : groupedSongs.keySet()) {
                String artistImage = MusicUtils.getArtistImage(artistName);
                Artist artist = new Artist(artistName, artistImage);
                SearchService.this.repository.addSongsForArtist(groupedSongs.get(artistName), artist);
            }

            SearchService.this.isSearchActive = false;
            SearchService.this.stopSelf();
        }

        private void iterateFiles(File[] files, ArrayList<File> allFiles) {
            for (File file : files) {
                if (file.isDirectory()) {
                    this.iterateFiles(file.listFiles(), allFiles);
                } else {
                    if (file.getName().endsWith(".mp3")) {
                        allFiles.add(file);
                    }
                }
            }
        }
    }
}
