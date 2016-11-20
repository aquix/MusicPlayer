package com.vlad.player.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.vlad.player.data.Album;
import com.vlad.player.data.Song;
import com.vlad.player.data.source.DbObservableContext;
import com.vlad.player.utils.MusicUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

            final HashMap<String, List<String>> music = new HashMap<>();
            this.iterateFiles(searchStart.listFiles(), music);

            for(String albumPath: music.keySet()) {
                ArrayList<MusicUtils.SongInfo> songsInfo = new ArrayList<>();
                List<String> songsFileName = music.get(albumPath);
                for(String songFileName: songsFileName) {
                    String songPath = albumPath + '/' + songFileName;
                    MusicUtils.SongInfo songInfo = MusicUtils.extractSongInfo(songPath);
                    songsInfo.add(songInfo);
                }

                MusicUtils.SongInfo firstSongInfo = songsInfo.get(0);

                String albumName = firstSongInfo.album;
                String albumArtist = firstSongInfo.artist;
                Album album = new Album(albumName, albumArtist, albumPath, MusicUtils.getNextCover());
                ArrayList<Song> songs = new ArrayList<>();
                for(int i = 0; i < songsInfo.size(); ++i) {
                    MusicUtils.SongInfo songInfo = songsInfo.get(i);
                    String songName = songInfo.title;
                    String songPath = albumPath + '/' + songsFileName.get(i);
                    String albumCover = MusicUtils.getNextCover();
                    Song song = new Song(songName, songPath, albumCover, songInfo.duration, album.getId());
                    songs.add(song);
                }
                SearchService.this.repository.addAlbum(album, songs);
            }

            SearchService.this.isSearchActive = false;
            SearchService.this.stopSelf();
        }

        private void iterateFiles(File[] files, HashMap<String, List<String>> music) {
            for (File file : files) {
                if (file.isDirectory()) {
                    this.iterateFiles(file.listFiles(), music);
                } else {
                    String name = file.getName();
                    if (name.endsWith(".mp3")) {
                        String dirPath = file.getParent();
                        if (!music.containsKey(dirPath)) {
                            music.put(dirPath, new LinkedList<String>());
                        }
                        List<String> songNames = music.get(dirPath);
                        songNames.add(name);
                    }
                }
            }
        }
    }
}
