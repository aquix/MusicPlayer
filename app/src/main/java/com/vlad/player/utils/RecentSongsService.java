package com.vlad.player.utils;

import com.vlad.player.data.Song;

import java.util.LinkedList;
import java.util.List;

public class RecentSongsService {
    private static final int MAX_RECENT_SONGS_SIZE = 10;
    private static final LinkedList<Song> mRecentSongs = new LinkedList<>();

    public static void addToRecent(Song song) {
        if(mRecentSongs.contains(song)) {
            mRecentSongs.remove(song);
        }
        mRecentSongs.addFirst(song);

        if(mRecentSongs.size() > MAX_RECENT_SONGS_SIZE) {
            mRecentSongs.removeLast();
        }
    }

    public static List<Song> getRecent() {
        return mRecentSongs;
    }
}
