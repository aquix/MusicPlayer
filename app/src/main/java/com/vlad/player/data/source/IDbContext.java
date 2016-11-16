package com.vlad.player.data.source;

import android.support.annotation.NonNull;

import com.vlad.player.data.Album;
import com.vlad.player.data.Song;

import java.util.ArrayList;
import java.util.List;

public interface IDbContext {

    boolean saveAlbum(@NonNull Album album, @NonNull List<Song> songs);

    void saveSongs(@NonNull List<Song> songs);

    void deleteAlbum(@NonNull Album album);

    void deleteSong(@NonNull Song song);

    @NonNull
    List<Album> getAlbums();

    @NonNull
    List<Song> getSongs(@NonNull String albumId, boolean sortByName);

    ArrayList<Integer> printAllSongs();

}
