package com.vlad.player.data.source;

import android.support.annotation.NonNull;

import com.vlad.player.data.Album;
import com.vlad.player.data.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class MusicRepository implements IDbContext {

    private static MusicRepository instance = null;

    private final IDbContext db;

    private List<AlbumsRepositoryObserver> observers = new ArrayList<>();

    private Map<String, Album> cachedAlbums;
    private Map<String, Song> cachedSongs;

    private boolean isAlbumsCacheDirty = true;
    private boolean isSongsCacheDirty = true;

    public static MusicRepository getInstance(IDbContext musicLocalDataSource) {
        if (instance == null) {
            instance = new MusicRepository(musicLocalDataSource);
        }
        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }

    private MusicRepository(@NonNull IDbContext tasksLocalDataSource) {
        db = checkNotNull(tasksLocalDataSource);
    }

    public void addContentObserver(AlbumsRepositoryObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeContentObserver(AlbumsRepositoryObserver observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    private List<Album> getCachedAlbums() {
        return cachedAlbums == null ? new ArrayList<Album>() : new ArrayList<>(cachedAlbums.values());
    }

    private List<Song> getCachedSongs(@NonNull String albumId, boolean sortByName) {
        Comparator<Song> comparator;
        if (sortByName) {
            comparator = new Comparator<Song>() {
                @Override
                public int compare(Song song1, Song song2) {
                    return song1.getName().compareTo(song2.getName());
                }
            };
        } else {
            comparator = new Comparator<Song>() {
                @Override
                public int compare(Song song1, Song song2) {
                    return song1.getDuration() - song2.getDuration();
                }
            };
        }

        if (cachedSongs == null) {
            return new ArrayList<>();
        }

        ArrayList<Song> songs = new ArrayList<>();
        for(Song song: cachedSongs.values()) {
            if (song.getAlbumId().equals(albumId)) {
                songs.add(song);
            }
        }

        Collections.sort(songs, comparator);
        return songs;
    }

    @Override
    public boolean saveAlbum(@NonNull Album album, @NonNull List<Song> songs) {
        saveSongs(songs);
        if (cachedAlbums == null) {
            cachedAlbums = new LinkedHashMap<>();
        }
        cachedAlbums.put(album.getId(), album);

        boolean successState = db.saveAlbum(album, songs);
        if (successState) {
            notifyAlbumsChanged();
        }
        return successState;
    }

    @Override
    public void saveSongs(@NonNull List<Song> songs) {
        db.saveSongs(songs);

        if (cachedSongs == null) {
            cachedSongs = new LinkedHashMap<>();
        }
        for(Song song: songs) {
            cachedSongs.put(song.getId(), song);
        }
    }

    @Override
    public void deleteAlbum(@NonNull Album album) {
        db.deleteAlbum(album);

        if (cachedAlbums != null) {
            if (cachedAlbums.containsKey(album.getId())) {
                cachedAlbums.remove(album.getId());
            }
        }

        if (cachedSongs != null) {
            for(Song song: cachedSongs.values()) {
                if (album.getId().equals(song.getAlbumId())) {
                    cachedSongs.remove(song.getId());
                }
            }
        }

        notifyAlbumsChanged();
    }

    @Override
    public void deleteSong(@NonNull Song song) {
        db.deleteSong(song);

        if (cachedSongs != null) {
            if (cachedSongs.containsKey(song.getId())) {
                cachedSongs.remove(song.getId());
            }
        }
    }
    public Album getAlbum(String albumId) {
        if(cachedAlbums == null) {
            getAlbums();
        }
        return cachedAlbums.get(albumId);
    }

    @NonNull
    @Override
    public List<Album> getAlbums() {
        if (!isAlbumsCacheDirty) {
            return getCachedAlbums();
        } else {
            List<Album> albums = db.getAlbums();

            cachedAlbums = new LinkedHashMap<>();
            for(Album album: albums) {
                cachedAlbums.put(album.getId(), album);
            }
            isAlbumsCacheDirty = false;

            return albums;
        }
    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull String albumId, boolean sortByName) {
        if (!isSongsCacheDirty) {
            return getCachedSongs(albumId, sortByName);
        } else {
            List<Album> albums = db.getAlbums();
            cachedSongs = new LinkedHashMap<>();
            for(Album nextAlbum: albums) {
                for(Song nextSong: db.getSongs(nextAlbum.getId(), sortByName)) {
                    cachedSongs.put(nextSong.getId(), nextSong);
                }
            }

            isSongsCacheDirty = false;
            return getCachedSongs(albumId, sortByName);
        }
    }

    @Override
    public ArrayList<Integer>  printAllSongs() {
        return db.printAllSongs();
    }

    private void notifyAlbumsChanged() {
        for(AlbumsRepositoryObserver observer: observers) {
            observer.onAlbumsChanged();
        }
    }

    public interface AlbumsRepositoryObserver {
        void onAlbumsChanged();
    }
}
