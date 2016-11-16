package com.vlad.player.data.source.local;


import android.provider.BaseColumns;

public final class DbConstants {

    private DbConstants() {}

    static abstract class AlbumEntity implements BaseColumns {
        static final String TABLE_NAME = "albums";

        static final String ID = "id";
        static final String NAME = "name";
        static final String ARTIST = "artist";
        static final String PATH = "path";
        static final String ALBUM_ART_PATH = "album_art_path";
    }

    static abstract class SongEntity implements BaseColumns {
        static final String TABLE_NAME = "songs";

        static final String ID = "id";
        static final String ALBUM_ID = "album_id";
        static final String NAME = "name";
        static final String PATH = "path";
        static final String DURATION = "duration";
        static final String IMAGE_PATH = "image";
    }
}
