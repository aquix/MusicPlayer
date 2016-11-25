package com.vlad.player.data.services;


import android.provider.BaseColumns;

public final class DbConstants {
    private DbConstants() {}

    static abstract class ArtistEntity implements BaseColumns {
        static final String TABLE_NAME = "artists";

        static final String ID = "_id";
        static final String NAME = "name";
        static final String IMAGE_PATH = "image_path";
    }

    static abstract class SongEntity implements BaseColumns {
        static final String TABLE_NAME = "songs";

        static final String ID = "_id";
        static final String ARTIST_ID = "artist_id";
        static final String TITLE = "title";
        static final String ALBUM = "album";
        static final String PATH = "path";
        static final String DURATION = "duration";
        static final String IMAGE_PATH = "image";
    }
}
