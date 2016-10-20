package com.fisko.music.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.fisko.music.data.Album;
import com.fisko.music.data.Song;
import com.fisko.music.data.source.MusicDataSource;
import com.fisko.music.data.source.local.TablesPersistenceContract.AlbumEntry;
import com.fisko.music.data.source.local.TablesPersistenceContract.SongEntry;

import java.util.ArrayList;
import java.util.List;


public class MusicLocalDataSource implements MusicDataSource {

    private static MusicLocalDataSource INSTANCE;

    private TablesDbHelper mDbHelper;

    // Prevent direct instantiation.
    private MusicLocalDataSource(@NonNull Context context) {
        mDbHelper = new TablesDbHelper(context);
//        mDb = mDbHelper.getWritableDatabase();
    }

    public static MusicLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MusicLocalDataSource(context);
        }
        return INSTANCE;
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

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if(isEntryExist(db, AlbumEntry.TABLE_NAME, AlbumEntry.COLUMN_NAME_ALBUM_PATH, album.getPath())) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AlbumEntry.COLUMN_NAME_ENTRY_ID, album.getId());
        values.put(AlbumEntry.COLUMN_NAME_ALBUM_NAME, album.getName());
        values.put(AlbumEntry.COLUMN_NAME_ALBUM_ARTIST, album.getArtist());
        values.put(AlbumEntry.COLUMN_NAME_ALBUM_PATH, album.getPath());
        values.put(AlbumEntry.COLUMN_NAME_ALBUM_IMAGE, album.getImagePath());

        db.insert(AlbumEntry.TABLE_NAME, null, values);
        db.close();

        return true;
    }

    @Override
    public void saveSongs(@NonNull List<Song> songs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        for(Song song: songs) {
            if(isEntryExist(db, SongEntry.TABLE_NAME, SongEntry.COLUMN_NAME_SONG_PATH, song.getPath())) {
                return;
            }

            ContentValues values = new ContentValues();
            values.put(SongEntry.COLUMN_NAME_ENTRY_ID, song.getId());
            values.put(SongEntry.COLUMN_NAME_ALBUM_ID, song.getAlbumId());
            values.put(SongEntry.COLUMN_NAME_SONG_NAME, song.getName());
            values.put(SongEntry.COLUMN_NAME_SONG_PATH, song.getPath());
            values.put(SongEntry.COLUMN_NAME_SONG_DURATION, song.getDuration());
            values.put(SongEntry.COLUMN_NAME_SONG_IMAGE, song.getImagePath());

            db.insert(SongEntry.TABLE_NAME, null, values);
        }
        db.close();
    }

    @Override
    public void deleteAlbum(@NonNull Album album) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = AlbumEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {album.getId()};
        db.delete(AlbumEntry.TABLE_NAME, selection, selectionArgs);
        db.close();

        removeAlbumSongs(album);
    }

    @Override
    public void deleteSong(@NonNull Song song) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = SongEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {song.getId()};
        db.delete(SongEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    private void removeAlbumSongs(@NonNull Album album) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String selection = SongEntry.COLUMN_NAME_ALBUM_ID + " LIKE ?";
        String[] selectionArgs = {album.getId()};
        db.delete(SongEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    @NonNull
    @Override
    public List<Album> getAlbums() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Album> tasks = new ArrayList<>();
        String[] projection =  {
                AlbumEntry.COLUMN_NAME_ENTRY_ID,
                AlbumEntry.COLUMN_NAME_ALBUM_NAME,
                AlbumEntry.COLUMN_NAME_ALBUM_ARTIST,
                AlbumEntry.COLUMN_NAME_ALBUM_PATH,
                AlbumEntry.COLUMN_NAME_ALBUM_IMAGE,
        };

        Cursor c = db.query(
                AlbumEntry.TABLE_NAME, projection, null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String albumId = c
                        .getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ENTRY_ID));
                String albumName =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ALBUM_NAME));
                String albumArtist =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ALBUM_ARTIST));
                String albumPath =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ALBUM_PATH));
                String albumImagePath =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ALBUM_IMAGE));
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

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull String albumId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<Song> songs = new ArrayList<>();
        String[] projection =  {
                SongEntry.COLUMN_NAME_ENTRY_ID,
                SongEntry.COLUMN_NAME_SONG_NAME,
                SongEntry.COLUMN_NAME_SONG_PATH,
                SongEntry.COLUMN_NAME_SONG_DURATION,
                SongEntry.COLUMN_NAME_SONG_IMAGE,
        };

        String selection = SongEntry.COLUMN_NAME_ALBUM_ID + " LIKE ?";
        String[] selectionArgs = { albumId };

        Cursor c = db.query(
                SongEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String songId = c
                        .getString(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_ENTRY_ID));
                String songName = c
                        .getString(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_SONG_NAME));
                String songPath =
                        c.getString(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_SONG_PATH));
                int songDuration =
                        c.getInt(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_SONG_DURATION));
                String songImagePath =
                        c.getString(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_SONG_IMAGE));
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
