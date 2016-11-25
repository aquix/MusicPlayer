package com.vlad.player.data.services;

import android.content.Context;
import android.support.annotation.NonNull;

import com.vlad.player.data.models.Artist;
import com.vlad.player.data.models.Song;
import com.vlad.player.data.viewmodels.SongFullInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class DbObservableContext implements IDbContext {
    public interface AlbumsRepositoryObserver {
        void onArtistsChanged();
    }

    private static DbObservableContext instance = null;

    private final IDbContext db;

    private List<AlbumsRepositoryObserver> observers = new ArrayList<>();

    private Map<Long, Artist> cachedAlbums;
    private Map<Long, Song> cachedSongs;

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
    public void deleteArtist(@NonNull Artist artist) {
        this.db.deleteArtist(artist);

        if (this.cachedAlbums != null) {
            if (this.cachedAlbums.containsKey(artist.getId())) {
                this.cachedAlbums.remove(artist.getId());
            }
        }

        if (this.cachedSongs != null) {
            for(Song song: this.cachedSongs.values()) {
                if (artist.getId() == song.getArtistId()) {
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

    public Artist getArtist(long albumId) {
        if(this.cachedAlbums == null) {
            this.getAllArtists();
        }
        return this.cachedAlbums.get(albumId);
    }

    @NonNull
    @Override
    public List<Artist> getAllArtists() {
        if (!this.isAlbumsCacheDirty) {
            return this.getCachedAlbums();
        } else {
            List<Artist> artists = this.db.getAllArtists();

            this.cachedAlbums = new LinkedHashMap<>();
            for(Artist artist : artists) {
                this.cachedAlbums.put(artist.getId(), artist);
            }
            this.isAlbumsCacheDirty = false;

            return artists;
        }
    }

    @Override
    public long addSongsForArtist(@NonNull List<Song> songs, Artist artist) {
        if (this.cachedAlbums == null) {
            this.cachedAlbums = new LinkedHashMap<>();
        }
        long newArtistId = this.db.addSongsForArtist(songs, artist);
        artist.updateId(newArtistId);
        this.cachedAlbums.put(newArtistId, artist);
        this.notifyAlbumsChanged();
        return newArtistId;
    }

    @Override
    public List<Song> getSongsForArtist(long artistId, boolean sortByName) {
        if (!this.isSongsCacheDirty) {
            return this.getCachedSongs(artistId, sortByName);
        } else {
            List<Artist> artists = this.db.getAllArtists();
            this.cachedSongs = new LinkedHashMap<>();
            for(Artist nextArtist : artists) {
                for(Song nextSong: this.db.getSongsForArtist(nextArtist.getId(), sortByName)) {
                    this.cachedSongs.put(nextSong.getId(), nextSong);
                }
            }

            this.isSongsCacheDirty = false;
            return this.getCachedSongs(artistId, sortByName);
        }
    }

    @Override
    public ArrayList<SongFullInfo> getAllSongs() {
        return this.db.getAllSongs();
    }

    @Override
    public void clearDb() {
        this.db.clearDb();
        if (this.cachedAlbums != null) {
            this.cachedAlbums.clear();
        }
        if (this.cachedSongs != null) {
            this.cachedSongs.clear();
        }

        this.notifyAlbumsChanged();
    }

    private void notifyAlbumsChanged() {
        for(AlbumsRepositoryObserver observer: this.observers) {
            observer.onArtistsChanged();
        }
    }

    private List<Artist> getCachedAlbums() {
        return this.cachedAlbums == null ? new ArrayList<Artist>() : new ArrayList<>(this.cachedAlbums.values());
    }

    private List<Song> getCachedSongs(long artistId, boolean sortByName) {
        Comparator<Song> comparator;
        if (sortByName) {
            comparator = new Comparator<Song>() {
                @Override
                public int compare(Song song1, Song song2) {
                    return song1.getTitle().compareTo(song2.getTitle());
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
            if (song.getArtistId() == artistId) {
                songs.add(song);
            }
        }

        Collections.sort(songs, comparator);
        return songs;
    }
}
