package com.vlad.player.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.vlad.player.data.Album;
import com.vlad.player.data.Song;
import com.vlad.player.data.source.IDbContext;
import com.vlad.player.data.source.local.DbConstants.AlbumEntity;
import com.vlad.player.data.source.local.DbConstants.SongEntity;

import java.util.ArrayList;
import java.util.List;


public class DbContext implements IDbContext {
    private static final String LOG_TAG = "DB log";
    private static DbContext instance;

    private DbOpenHelper dbOpenHelper;

    private DbContext(@NonNull Context context) {
        dbOpenHelper = new DbOpenHelper(context);
    }

    public static DbContext getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new DbContext(context);
        }
        return instance;
    }

    private boolean isEntryExist(SQLiteDatabase db, String tableName, String fieldName, String entryId) {
        Cursor c = null;
        try {
            String query = "select count(*) from " + tableName + " where " + fieldName + " = ?";
            c = db.rawQuery(query, new String[]{entryId});
            return c.moveToFirst() && c.getInt(0) != 0;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public boolean saveAlbum(@NonNull Album album, @NonNull List<Song> songs) {
        saveSongs(songs);

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        if(isEntryExist(db, AlbumEntity.TABLE_NAME, AlbumEntity.COLUMN_NAME_ALBUM_PATH, album.getPath())) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AlbumEntity.COLUMN_NAME_ENTRY_ID, album.getId());
        values.put(AlbumEntity.COLUMN_NAME_ALBUM_NAME, album.getName());
        values.put(AlbumEntity.COLUMN_NAME_ALBUM_ARTIST, album.getArtist());
        values.put(AlbumEntity.COLUMN_NAME_ALBUM_PATH, album.getPath());
        values.put(AlbumEntity.COLUMN_NAME_ALBUM_IMAGE, album.getImagePath());

        db.insert(AlbumEntity.TABLE_NAME, null, values);
        db.close();

        return true;
    }

    @Override
    public void saveSongs(@NonNull List<Song> songs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        for(Song song: songs) {
            if(isEntryExist(db, SongEntity.TABLE_NAME, SongEntity.COLUMN_NAME_SONG_PATH, song.getPath())) {
                return;
            }

            ContentValues values = new ContentValues();
            values.put(SongEntity.COLUMN_NAME_ENTRY_ID, song.getId());
            values.put(SongEntity.COLUMN_NAME_ALBUM_ID, song.getAlbumId());
            values.put(SongEntity.COLUMN_NAME_SONG_NAME, song.getName());
            values.put(SongEntity.COLUMN_NAME_SONG_PATH, song.getPath());
            values.put(SongEntity.COLUMN_NAME_SONG_DURATION, song.getDuration());
            values.put(SongEntity.COLUMN_NAME_SONG_IMAGE, song.getImagePath());

            db.insert(SongEntity.TABLE_NAME, null, values);
        }
        db.close();
    }

    @Override
    public void deleteAlbum(@NonNull Album album) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();

        String selection = AlbumEntity.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {album.getId()};
        db.delete(AlbumEntity.TABLE_NAME, selection, selectionArgs);
        db.close();

        removeAlbumSongs(album);
    }

    @Override
    public void deleteSong(@NonNull Song song) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        String selection = SongEntity.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {song.getId()};
        db.delete(SongEntity.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    private void removeAlbumSongs(@NonNull Album album) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        String selection = SongEntity.COLUMN_NAME_ALBUM_ID + " LIKE ?";
        String[] selectionArgs = {album.getId()};
        db.delete(SongEntity.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    @NonNull
    @Override
    public List<Album> getAlbums() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        List<Album> tasks = new ArrayList<>();
        String[] projection =  {
                AlbumEntity.COLUMN_NAME_ENTRY_ID,
                AlbumEntity.COLUMN_NAME_ALBUM_NAME,
                AlbumEntity.COLUMN_NAME_ALBUM_ARTIST,
                AlbumEntity.COLUMN_NAME_ALBUM_PATH,
                AlbumEntity.COLUMN_NAME_ALBUM_IMAGE,
        };

        Cursor c = db.query(
                AlbumEntity.TABLE_NAME, projection, null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String albumId = c
                        .getString(c.getColumnIndexOrThrow(AlbumEntity.COLUMN_NAME_ENTRY_ID));
                String albumName =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntity.COLUMN_NAME_ALBUM_NAME));
                String albumArtist =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntity.COLUMN_NAME_ALBUM_ARTIST));
                String albumPath =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntity.COLUMN_NAME_ALBUM_PATH));
                String albumImagePath =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntity.COLUMN_NAME_ALBUM_IMAGE));
                Album album = new Album(albumId, albumName, albumArtist, albumPath, albumImagePath);
                tasks.add(album);
            }
        }
        if (c != null) {
            c.close();
        }
        db.close();

        return tasks;

    }

    @Override
    public ArrayList<Integer> printAllSongs() {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();

        String table = AlbumEntity.TABLE_NAME + " as Album inner join "+ SongEntity.TABLE_NAME +" as Song " +
                "on Album."+ AlbumEntity.COLUMN_NAME_ENTRY_ID +" = Song." + SongEntity.COLUMN_NAME_ALBUM_ID;
        String columns[] = {
                "Album."+ AlbumEntity.COLUMN_NAME_ALBUM_NAME +" as Album",
                "Album."+ AlbumEntity.COLUMN_NAME_ALBUM_ARTIST +" as Artist",
                "Song."+ SongEntity.COLUMN_NAME_SONG_NAME +" as Name",
                "Song."+ SongEntity.COLUMN_NAME_SONG_DURATION +" as Duration",
        };
        Cursor c = db.query(table, columns, null, null, null, null, null);
        ArrayList<Integer> durations = logCursor(c);

        if (c != null) {
            c.close();
        }
        db.close();

        return durations;
    }

    private ArrayList<Integer> logCursor(Cursor c) {
        ArrayList<Integer> durations = new ArrayList<>();
        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : c.getColumnNames()) {
                        str = str.concat(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
                        durations.add(c.getInt(c.getColumnIndex("Duration")));
                    }
                    Log.d(LOG_TAG, str);
                } while (c.moveToNext());
            }
        } else {
            Log.d(LOG_TAG, "Cursor is null");
        }
        return durations;
    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull String albumId, boolean sortByName) {
        SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
        List<Song> songs = new ArrayList<>();
        String[] projection =  {
                SongEntity.COLUMN_NAME_ENTRY_ID,
                SongEntity.COLUMN_NAME_SONG_NAME,
                SongEntity.COLUMN_NAME_SONG_PATH,
                SongEntity.COLUMN_NAME_SONG_DURATION,
                SongEntity.COLUMN_NAME_SONG_IMAGE,
        };

        String selection = SongEntity.COLUMN_NAME_ALBUM_ID + " LIKE ?";
        String[] selectionArgs = { albumId };

        String sortBy;
        if (sortByName) {
            sortBy = SongEntity.COLUMN_NAME_SONG_NAME;
        } else {
            sortBy = SongEntity.COLUMN_NAME_SONG_DURATION;
        }

        Cursor c = db.query(
                SongEntity.TABLE_NAME, projection, selection, selectionArgs, null, null, sortBy);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String songId = c
                        .getString(c.getColumnIndexOrThrow(SongEntity.COLUMN_NAME_ENTRY_ID));
                String songName = c
                        .getString(c.getColumnIndexOrThrow(SongEntity.COLUMN_NAME_SONG_NAME));
                String songPath =
                        c.getString(c.getColumnIndexOrThrow(SongEntity.COLUMN_NAME_SONG_PATH));
                int songDuration =
                        c.getInt(c.getColumnIndexOrThrow(SongEntity.COLUMN_NAME_SONG_DURATION));
                String songImagePath =
                        c.getString(c.getColumnIndexOrThrow(SongEntity.COLUMN_NAME_SONG_IMAGE));
                Song song = new Song(songId, songName, songPath, songImagePath, songDuration, albumId);
                songs.add(song);
            }
        }
        if (c != null) {
            c.close();
        }
        db.close();

        return songs;
    }
}
