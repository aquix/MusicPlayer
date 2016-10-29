package com.fisko.music.data.source;

import android.support.annotation.NonNull;

import com.fisko.music.data.Album;
import com.fisko.music.data.Song;

import java.util.ArrayList;
import java.util.List;

public interface MusicDataSource {

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
