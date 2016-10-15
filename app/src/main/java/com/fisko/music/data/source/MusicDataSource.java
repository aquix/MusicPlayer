package com.fisko.music.data.source;

import android.support.annotation.NonNull;

import com.fisko.music.data.Album;
import com.fisko.music.data.Song;

import java.util.List;

public interface MusicDataSource {

    void saveAlbum(@NonNull Album album, @NonNull List<Song> songs);

    void saveSongs(@NonNull List<Song> songs);

    void deleteAlbum(@NonNull Album album);

    @NonNull
    List<Album> getAlbums();

    @NonNull
    List<Song> getSongs(@NonNull String albumId);

}
