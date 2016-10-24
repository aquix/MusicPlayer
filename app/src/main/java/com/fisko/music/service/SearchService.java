package com.fisko.music.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.fisko.music.data.Album;
import com.fisko.music.data.Song;
import com.fisko.music.data.source.MusicDataSource;
import com.fisko.music.data.source.MusicRepository;
import com.fisko.music.data.source.local.MusicLocalDataSource;
import com.fisko.music.utils.MusicUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SearchService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private final MusicRepository mRepository;
    private boolean isSearchActive = false;

    public SearchService() {
        MusicDataSource localDataSource = MusicLocalDataSource.getInstance(this);
        mRepository = MusicRepository.getInstance(localDataSource);
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
        return mBinder;
    }

    public void startMusicSearch() {
        if (!isSearchActive()) {
            MusicSearcher searcher = new MusicSearcher();
            new Thread(searcher).start();
        }
    }


    public boolean isSearchActive() {
        return isSearchActive;
    }


    private class MusicSearcher implements Runnable {

//        ExecutorService mExecutor;
//
//        MusicSearcher() {
//            mExecutor = Executors.newSingleThreadExecutor();
//        }
//
//        private class SongImageSearcher implements Callable<String> {
//            private MusicUtils.SongInfo mSongInfo;
//
//            SongImageSearcher(MusicUtils.SongInfo songInfo) {
//                mSongInfo = songInfo;
//            }
//
//            @Override
//            public String call() throws Exception {
//                String requestUrl = NetworkUtils.genAlbomInfoUrl(mSongInfo);
//                JSONObject json = NetworkUtils.loadURL(requestUrl);
//                assert json != null;
//                return json.getJSONObject("album").getString("medium");
//            }
//        }

        private void iterateFiles(File[] files, HashMap<String, List<String>> music) {
            for (File file : files) {
                if (file.isDirectory()) {
                    iterateFiles(file.listFiles(), music);
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

        @Override
        public void run() {
            File musicFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
            String musicFolderPath = musicFolder.getAbsolutePath();
            File searchStart = new File(musicFolderPath);
            if (!searchStart.exists() && !searchStart.isDirectory()) {
                return;
            }

            final HashMap<String, List<String>> music = new HashMap<>();
            iterateFiles(searchStart.listFiles(), music);

            for(String albumPath: music.keySet()) {
                ArrayList<MusicUtils.SongInfo> songsInfo = new ArrayList<>();
                List<String> songsFileName = music.get(albumPath);
                for(String songFileName: songsFileName) {
                    String songPath = albumPath + '/' + songFileName;
                    MusicUtils.SongInfo songInfo = MusicUtils.extractSongInfo(songPath);
                    songsInfo.add(songInfo);
                }

                MusicUtils.SongInfo firstSongInfo = songsInfo.get(0);

//                Callable<String> callable = new SongImageSearcher(firstSongInfo);
//                Future<String> future = mExecutor.submit(callable);
//
//                String albumImageUrl = null;
//                try {
//                    albumImageUrl = future.get();
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }

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
                mRepository.saveAlbum(album, songs);
            }

            isSearchActive = false;
            stopSelf();
        }
    }

}
