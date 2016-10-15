package com.fisko.music.utils;

import android.media.MediaMetadataRetriever;
import android.support.v7.app.NotificationCompat;

public class MusicUtils {

    static public class SongInfo {
        String artist;
        public String album;
        public String title;
    }


    public static SongInfo extractSongInfo(String songPath) {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(songPath);
        SongInfo songInfo = new SongInfo();
        songInfo.artist =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        songInfo.album =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        songInfo.title =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        return songInfo;
    }

}
