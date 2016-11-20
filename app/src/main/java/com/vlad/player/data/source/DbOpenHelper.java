package com.vlad.player.data.source;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.vlad.player.data.source.DbConstants.AlbumEntity;
import com.vlad.player.data.source.DbConstants.SongEntity;


class DbOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 16;

    private static final String DATABASE_NAME = "player.db";

    private static final String SQL_CREATE_TABLE_ALBUMS =
            "CREATE TABLE " + AlbumEntity.TABLE_NAME + " (" +
                    AlbumEntity._ID + " INTEGER" + " PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    AlbumEntity.ID + " TEXT, " +
                    AlbumEntity.NAME + " TEXT, " +
                    AlbumEntity.ARTIST + " TEXT, " +
                    AlbumEntity.ALBUM_ART_PATH + " TEXT, " +
                    AlbumEntity.PATH + " TEXT" +
            " );";
    private static final String SQL_CREATE_TABLE_SONGS =
            "CREATE TABLE " + SongEntity.TABLE_NAME + " (" +
                    SongEntity._ID + " INTEGER" + " PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    SongEntity.ID + " TEXT, " +
                    SongEntity.ALBUM_ID + " TEXT, " +
                    SongEntity.NAME + " TEXT, " +
                    SongEntity.IMAGE_PATH + " TEXT, " +
                    SongEntity.DURATION + " INTEGER, " +
                    SongEntity.PATH + " TEXT" +
            " );";

    DbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_ALBUMS);
        db.execSQL(SQL_CREATE_TABLE_SONGS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SongEntity.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AlbumEntity.TABLE_NAME);
        db.execSQL(SQL_CREATE_TABLE_ALBUMS);
        db.execSQL(SQL_CREATE_TABLE_SONGS);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
