package com.vlad.player.data.source;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import com.vlad.player.data.Album;
import com.vlad.player.data.Song;
import com.vlad.player.data.source.DbConstants.AlbumEntity;
import com.vlad.player.data.source.DbConstants.SongEntity;

import java.util.ArrayList;
import java.util.List;


public class DbContext implements IDbContext {
    private static final String LOG_TAG = "DB log";
    private static DbContext instance;

    private DbOpenHelper dbOpenHelper;

    private DbContext(@NonNull Context context) {
        this.dbOpenHelper = new DbOpenHelper(context);
    }

    public static DbContext getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new DbContext(context);
        }
        return instance;
    }

    private boolean isEntityExist(SQLiteDatabase db, String tableName, String fieldName, String entryId) {
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

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull String albumId, boolean sortByName) {
        SQLiteDatabase db = this.dbOpenHelper.getReadableDatabase();
        List<Song> songs = new ArrayList<>();
        String[] columnNames =  {
                SongEntity.ID,
                SongEntity.NAME,
                SongEntity.PATH,
                SongEntity.DURATION,
                SongEntity.IMAGE_PATH,
        };

        String selection = SongEntity.ALBUM_ID + " LIKE ?";
        String[] selectionArgs = { albumId };

        String sortBy;
        if (sortByName) {
            sortBy = SongEntity.NAME;
        } else {
            sortBy = SongEntity.DURATION;
        }

        Cursor c = db.query(
                SongEntity.TABLE_NAME, columnNames, selection, selectionArgs, null, null, sortBy);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String songId = c
                        .getString(c.getColumnIndexOrThrow(SongEntity.ID));
                String songName = c
                        .getString(c.getColumnIndexOrThrow(SongEntity.NAME));
                String songPath =
                        c.getString(c.getColumnIndexOrThrow(SongEntity.PATH));
                int songDuration =
                        c.getInt(c.getColumnIndexOrThrow(SongEntity.DURATION));
                String songImagePath =
                        c.getString(c.getColumnIndexOrThrow(SongEntity.IMAGE_PATH));
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

    @NonNull
    @Override
    public List<Album> getAlbums() {
        SQLiteDatabase db = this.dbOpenHelper.getReadableDatabase();
        List<Album> albums = new ArrayList<>();
        String[] columnNames =  {
                AlbumEntity.ID,
                AlbumEntity.NAME,
                AlbumEntity.ARTIST,
                AlbumEntity.PATH,
                AlbumEntity.ALBUM_ART_PATH,
        };

        Cursor c = db.query(
                AlbumEntity.TABLE_NAME, columnNames, null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String albumId = c
                        .getString(c.getColumnIndexOrThrow(AlbumEntity.ID));
                String albumName =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntity.NAME));
                String albumArtist =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntity.ARTIST));
                String albumPath =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntity.PATH));
                String albumImagePath =
                        c.getString(c.getColumnIndexOrThrow(AlbumEntity.ALBUM_ART_PATH));
                Album album = new Album(albumId, albumName, albumArtist, albumPath, albumImagePath);
                albums.add(album);
            }
        }
        if (c != null) {
            c.close();
        }
        db.close();

        return albums;

    }

    @Override
    public boolean addAlbum(@NonNull Album album, @NonNull List<Song> songs) {
        this.addSongs(songs);

        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();
        if(this.isEntityExist(db, AlbumEntity.TABLE_NAME, AlbumEntity.PATH, album.getPath())) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(AlbumEntity.ID, album.getId());
        values.put(AlbumEntity.NAME, album.getName());
        values.put(AlbumEntity.ARTIST, album.getArtist());
        values.put(AlbumEntity.PATH, album.getPath());
        values.put(AlbumEntity.ALBUM_ART_PATH, album.getImagePath());

        db.insert(AlbumEntity.TABLE_NAME, null, values);
        db.close();

        return true;
    }

    @Override
    public void addSongs(@NonNull List<Song> songs) {
        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();
        for(Song song: songs) {
            if(this.isEntityExist(db, SongEntity.TABLE_NAME, SongEntity.PATH, song.getPath())) {
                return;
            }

            ContentValues values = new ContentValues();
            values.put(SongEntity.ID, song.getId());
            values.put(SongEntity.ALBUM_ID, song.getAlbumId());
            values.put(SongEntity.NAME, song.getName());
            values.put(SongEntity.PATH, song.getPath());
            values.put(SongEntity.DURATION, song.getDuration());
            values.put(SongEntity.IMAGE_PATH, song.getImagePath());

            db.insert(SongEntity.TABLE_NAME, null, values);
        }
        db.close();
    }

    @Override
    public void deleteAlbum(@NonNull Album album) {
        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();

        String selection = AlbumEntity.ID + " LIKE ?";
        String[] selectionArgs = {album.getId()};
        db.delete(AlbumEntity.TABLE_NAME, selection, selectionArgs);
        db.close();

        this.removeAlbumSongs(album);
    }

    @Override
    public void deleteSong(@NonNull Song song) {
        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();
        String selection = SongEntity.ID + " LIKE ?";
        String[] selectionArgs = {song.getId()};
        db.delete(SongEntity.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    private void removeAlbumSongs(@NonNull Album album) {
        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();
        String selection = SongEntity.ALBUM_ID + " LIKE ?";
        String[] selectionArgs = {album.getId()};
        db.delete(SongEntity.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    @Override
    public ArrayList<Integer> printAllSongs() {
        SQLiteDatabase db = this.dbOpenHelper.getReadableDatabase();

        String table = AlbumEntity.TABLE_NAME + " as Album inner join "+ SongEntity.TABLE_NAME +" as Song " +
                "on Album."+ AlbumEntity.ID +" = Song." + SongEntity.ALBUM_ID;
        String[] columnNames = {
                "Album."+ AlbumEntity.NAME +" as Album",
                "Album."+ AlbumEntity.ARTIST +" as Artist",
                "Song."+ SongEntity.NAME +" as Name",
                "Song."+ SongEntity.DURATION +" as Duration",
        };
        Cursor c = db.query(table, columnNames, null, null, null, null, null);
        ArrayList<Integer> durations = this.logCursor(c);

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


}
