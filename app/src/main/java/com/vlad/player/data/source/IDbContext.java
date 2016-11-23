package com.vlad.player.data.source;

import android.support.annotation.NonNull;

import com.vlad.player.data.models.Artist;
import com.vlad.player.data.models.Song;
import com.vlad.player.data.viewmodels.SongFullInfo;

import java.util.ArrayList;
import java.util.List;

public interface IDbContext {

    @NonNull
    List<Artist> getAllArtists();

    long addSongsForArtist(@NonNull List<Song> songs, Artist artist);

    List<Song> getSongsForArtist(long artistId, boolean sortByName);

    void deleteArtist(@NonNull Artist artist);

    void deleteSong(@NonNull Song song);

    ArrayList<SongFullInfo> getAllSongs();
}
