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
import com.fisko.music.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        if (!isSearchActive()) {
            MusicSearcher searcher = new MusicSearcher();
            new Thread(searcher).start();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public boolean isSearchActive() {
        return isSearchActive;
    }


    private class MusicSearcher implements Runnable {

        ExecutorService mExecutor;

        MusicSearcher() {
            mExecutor = Executors.newSingleThreadExecutor();
        }

        private class SongImageSearcher implements Callable<String> {
            private MusicUtils.SongInfo mSongInfo;

            SongImageSearcher(MusicUtils.SongInfo songInfo) {
                mSongInfo = songInfo;
            }

            @Override
            public String call() throws Exception {
                String requestUrl = NetworkUtils.genAlbomInfoUrl(mSongInfo);
                JSONObject json = NetworkUtils.loadURL(requestUrl);
                assert json != null;
                return json.getJSONObject("album").getString("medium");
            }
        }

        @Override
        public void run() {
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File searchStart = new File(sdPath);
            if (!searchStart.exists() && !searchStart.isDirectory()) {
                return;
            }

            final HashMap<String, List<String>> music = new HashMap<>();
            File[] ignored = searchStart.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    if (name.endsWith("mp3")) {
                        String dirPath = dir.getAbsolutePath();
                        if (!music.containsKey(dirPath)) {
                            music.put(dirPath, new LinkedList<String>());
                        }
                        List<String> songNames = music.get(dirPath);
                        songNames.add(name);
                    }
                    return false;
                }
            });

            for(String albumPath: music.keySet()) {
                ArrayList<MusicUtils.SongInfo> songsInfo = new ArrayList<>();
                List<String> songsFileName = music.get(albumPath);
                for(String songFileName: songsFileName) {
                    String songPath = albumPath + songFileName;
                    MusicUtils.SongInfo songInfo = MusicUtils.extractSongInfo(songPath);
                    songsInfo.add(songInfo);
                }

                String firstSongFileName = albumPath + songsFileName.get(0);
                MusicUtils.SongInfo firstSongInfo = MusicUtils.extractSongInfo(firstSongFileName);
                Callable<String> callable = new SongImageSearcher(firstSongInfo);
                Future<String> future = mExecutor.submit(callable);

                String albumImageUrl = null;
                try {
                    albumImageUrl = future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

                String albumName = firstSongInfo.album;
                Album album = new Album(albumName, albumPath, albumImageUrl);
                ArrayList<Song> songs = new ArrayList<>();
                for(int i = 0; i < songsInfo.size(); ++i) {
                    MusicUtils.SongInfo songInfo = songsInfo.get(i);
                    String songName = songInfo.title;
                    String songPath = albumPath + songsFileName.get(i);
                    Song song = new Song(songName, songPath, albumImageUrl, album.getId());
                    songs.add(song);
                }
                mRepository.saveAlbum(album, songs);
            }

            isSearchActive = false;
            stopSelf();
        }
    }

}
