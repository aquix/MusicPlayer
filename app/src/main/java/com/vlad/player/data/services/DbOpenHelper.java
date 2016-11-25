package com.vlad.player.data.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.vlad.player.data.services.DbConstants.ArtistEntity;
import com.vlad.player.data.services.DbConstants.SongEntity;


class DbOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 17;

    private static final String DATABASE_NAME = "player.db";

    private static final String SQL_CREATE_TABLE_ALBUMS =
            "CREATE TABLE " + ArtistEntity.TABLE_NAME + " (" +
                    ArtistEntity._ID + " INTEGER" + " PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    ArtistEntity.NAME + " TEXT, " +
                    ArtistEntity.IMAGE_PATH + " TEXT" +
            " );";
    private static final String SQL_CREATE_TABLE_SONGS =
            "CREATE TABLE " + SongEntity.TABLE_NAME + " (" +
                    SongEntity._ID + " INTEGER" + " PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    SongEntity.ARTIST_ID + " TEXT, " +
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
        db.execSQL("DROP TABLE IF EXISTS " + ArtistEntity.TABLE_NAME);
        db.execSQL(SQL_CREATE_TABLE_ALBUMS);
        db.execSQL(SQL_CREATE_TABLE_SONGS);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
