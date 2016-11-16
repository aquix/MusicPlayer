package com.vlad.player.data.source;

import android.content.Context;
import android.support.annotation.NonNull;

import com.vlad.player.data.Album;
import com.vlad.player.data.Song;
import com.vlad.player.data.source.local.DbContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class DbObservableContext implements IDbContext {
    public interface AlbumsRepositoryObserver {
        void onAlbumsChanged();
    }

    private static DbObservableContext instance = null;

    private final IDbContext db;

    private List<AlbumsRepositoryObserver> observers = new ArrayList<>();

    private Map<String, Album> cachedAlbums;
    private Map<String, Song> cachedSongs;

    private boolean isAlbumsCacheDirty = true;
    private boolean isSongsCacheDirty = true;

    public static DbObservableContext getInstance(Context context) {
        if (instance == null) {
            instance = new DbObservableContext(context);
        }
        return instance;
    }

    private DbObservableContext(@NonNull Context context) {
        this.db = checkNotNull(DbContext.getInstance(context));
    }


    public void addContentObserver(AlbumsRepositoryObserver observer) {
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    public void removeContentObserver(AlbumsRepositoryObserver observer) {
        if (this.observers.contains(observer)) {
            this.observers.remove(observer);
        }
    }

    @Override
    public boolean addAlbum(@NonNull Album album, @NonNull List<Song> songs) {
        this.addSongs(songs);
        if (this.cachedAlbums == null) {
            this.cachedAlbums = new LinkedHashMap<>();
        }
        this.cachedAlbums.put(album.getId(), album);

        boolean successState = this.db.addAlbum(album, songs);
        if (successState) {
            this.notifyAlbumsChanged();
        }
        return successState;
    }

    @Override
    public void addSongs(@NonNull List<Song> songs) {
        this.db.addSongs(songs);

        if (this.cachedSongs == null) {
            this.cachedSongs = new LinkedHashMap<>();
        }
        for(Song song: songs) {
            this.cachedSongs.put(song.getId(), song);
        }
    }

    @Override
    public void deleteAlbum(@NonNull Album album) {
        this.db.deleteAlbum(album);

        if (this.cachedAlbums != null) {
            if (this.cachedAlbums.containsKey(album.getId())) {
                this.cachedAlbums.remove(album.getId());
            }
        }

        if (this.cachedSongs != null) {
            for(Song song: this.cachedSongs.values()) {
                if (album.getId().equals(song.getAlbumId())) {
                    this.cachedSongs.remove(song.getId());
                }
            }
        }

        this.notifyAlbumsChanged();
    }

    @Override
    public void deleteSong(@NonNull Song song) {
        this.db.deleteSong(song);

        if (this.cachedSongs != null) {
            if (this.cachedSongs.containsKey(song.getId())) {
                this.cachedSongs.remove(song.getId());
            }
        }
    }
    public Album getAlbum(String albumId) {
        if(this.cachedAlbums == null) {
            this.getAlbums();
        }
        return this.cachedAlbums.get(albumId);
    }

    @NonNull
    @Override
    public List<Album> getAlbums() {
        if (!this.isAlbumsCacheDirty) {
            return this.getCachedAlbums();
        } else {
            List<Album> albums = this.db.getAlbums();

            this.cachedAlbums = new LinkedHashMap<>();
            for(Album album: albums) {
                this.cachedAlbums.put(album.getId(), album);
            }
            this.isAlbumsCacheDirty = false;

            return albums;
        }
    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull String albumId, boolean sortByName) {
        if (!this.isSongsCacheDirty) {
            return this.getCachedSongs(albumId, sortByName);
        } else {
            List<Album> albums = this.db.getAlbums();
            this.cachedSongs = new LinkedHashMap<>();
            for(Album nextAlbum: albums) {
                for(Song nextSong: this.db.getSongs(nextAlbum.getId(), sortByName)) {
                    this.cachedSongs.put(nextSong.getId(), nextSong);
                }
            }

            this.isSongsCacheDirty = false;
            return this.getCachedSongs(albumId, sortByName);
        }
    }

    @Override
    public ArrayList<Integer>  printAllSongs() {
        return this.db.printAllSongs();
    }

    private void notifyAlbumsChanged() {
        for(AlbumsRepositoryObserver observer: this.observers) {
            observer.onAlbumsChanged();
        }
    }

    private List<Album> getCachedAlbums() {
        return this.cachedAlbums == null ? new ArrayList<Album>() : new ArrayList<>(this.cachedAlbums.values());
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

        if (this.cachedSongs == null) {
            return new ArrayList<>();
        }

        ArrayList<Song> songs = new ArrayList<>();
        for(Song song: this.cachedSongs.values()) {
            if (song.getAlbumId().equals(albumId)) {
                songs.add(song);
            }
        }

        Collections.sort(songs, comparator);
        return songs;
    }
}
