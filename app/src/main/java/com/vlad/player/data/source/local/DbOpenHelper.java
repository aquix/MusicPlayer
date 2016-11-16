package com.vlad.player.data.source.local;/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.vlad.player.data.source.local.DbConstants.AlbumEntity;
import com.vlad.player.data.source.local.DbConstants.SongEntity;

class DbOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 16;

    private static final String DATABASE_NAME = "player.db";
    

    private static final String SQL_CREATE_TABLE_ALBUMS =
            "CREATE TABLE " + AlbumEntity.TABLE_NAME + " (" +
                    AlbumEntity._ID + " INTEGER" + " PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    AlbumEntity.COLUMN_NAME_ENTRY_ID + " TEXT, " +
                    AlbumEntity.COLUMN_NAME_ALBUM_NAME + " TEXT, " +
                    AlbumEntity.COLUMN_NAME_ALBUM_ARTIST + " TEXT, " +
                    AlbumEntity.COLUMN_NAME_ALBUM_IMAGE + " TEXT, " +
                    AlbumEntity.COLUMN_NAME_ALBUM_PATH + " TEXT" +
            " );";
    private static final String SQL_CREATE_TABLE_SONGS =
            "CREATE TABLE " + SongEntity.TABLE_NAME + " (" +
                    SongEntity._ID + " INTEGER" + " PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    SongEntity.COLUMN_NAME_ENTRY_ID + " TEXT, " +
                    SongEntity.COLUMN_NAME_ALBUM_ID + " TEXT, " +
                    SongEntity.COLUMN_NAME_SONG_NAME + " TEXT, " +
                    SongEntity.COLUMN_NAME_SONG_IMAGE + " TEXT, " +
                    SongEntity.COLUMN_NAME_SONG_DURATION + " INTEGER, " +
                    SongEntity.COLUMN_NAME_SONG_PATH + " TEXT" +
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
