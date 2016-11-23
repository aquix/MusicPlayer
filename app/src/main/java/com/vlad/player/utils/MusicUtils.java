package com.vlad.player.utils;

import android.accounts.NetworkErrorException;
import android.media.MediaMetadataRetriever;

import org.json.JSONException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public final class MusicUtils {
    private static final String DEFAULT_COVER =
            "https://yt3.ggpht.com/0v8T0CTAv8VPxA5lJtz-tqJe-tR-3VQc0ONhD6Az2RWjNRnwh5QQzPYz5I7wbYljU_tQjZ2ok2W59_v_=s900-nd-c-c0xffffffff-rj-k-no";

    public static class SongInfo {
        public String artist;
        public String album;
        public String title;
        public int duration;
    }

    public static String getCover(SongInfo songInfo) {
        String coverUrl;
        try {
             coverUrl = LastFmService.getSongCover(songInfo);
        } catch (JSONException | NetworkErrorException e) {
            coverUrl = DEFAULT_COVER;
        }

        return coverUrl;
    }

    public static String getArtistImage(String artistName) {
        String imageUrl;
        try {
            imageUrl = LastFmService.getArtistImage(artistName);
        } catch (JSONException | NetworkErrorException e) {
            imageUrl = DEFAULT_COVER;
        }

        return imageUrl;
    }

    private static SongInfo getEmptySongInfo() {
        SongInfo emptyInfo = new SongInfo();
        emptyInfo.artist = "Unknown";
        emptyInfo.album = "Unknown";
        emptyInfo.title = "Unknown";
        emptyInfo.duration = 0;
        return emptyInfo;
    }

    public static SongInfo extractSongInfo(String songPath) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(songPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return getEmptySongInfo();
        }
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        try {
            metaRetriever.setDataSource(inputStream.getFD());
        } catch (Exception e) {
            e.printStackTrace();
            return getEmptySongInfo();
        }
        SongInfo songInfo = new SongInfo();
        songInfo.artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        songInfo.album = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        songInfo.title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        songInfo.duration = Integer.parseInt(duration);
        return songInfo;
    }

}
