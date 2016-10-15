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

    private SQLiteDatabase mDb;

    // Prevent direct instantiation.
    private MusicLocalDataSource(@NonNull Context context) {
        TablesDbHelper mDbHelper = new TablesDbHelper(context);
        mDb = mDbHelper.getWritableDatabase();
    }

    public static MusicLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MusicLocalDataSource(context);
        }
        return INSTANCE;
    }

    @Override
    public void saveAlbum(@NonNull Album album, @NonNull List<Song> songs) {
        saveSongs(songs);

        try {
            ContentValues values = new ContentValues();
            values.put(AlbumEntry.COLUMN_NAME_ENTRY_ID, album.getId());
            values.put(AlbumEntry.COLUMN_NAME_ALBUM_NAME, album.getName());
            values.put(AlbumEntry.COLUMN_NAME_ALBUM_PATH, album.getPath());
            values.put(AlbumEntry.COLUMN_NAME_ALBUM_IMAGE, album.getImagePath());

            mDb.insert(AlbumEntry.TABLE_NAME, null, values);
        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
    }

    @Override
    public void saveSongs(@NonNull List<Song> songs) {
        for(Song song: songs) {
            try {
                ContentValues values = new ContentValues();
                values.put(SongEntry.COLUMN_NAME_ENTRY_ID, song.getId());
                values.put(SongEntry.COLUMN_NAME_ALBUM_ID, song.getAlbumId());
                values.put(SongEntry.COLUMN_NAME_SONG_NAME, song.getName());
                values.put(SongEntry.COLUMN_NAME_SONG_PATH, song.getPath());
                values.put(SongEntry.COLUMN_NAME_SONG_IMAGE, song.getImagePath());

                mDb.insert(SongEntry.TABLE_NAME, null, values);
            } catch (IllegalStateException e) {
                // Send to analytics, log etc
            }
        }
    }

    @Override
    public void deleteAlbum(@NonNull Album album) {
        String selection = AlbumEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {album.getId()};
        mDb.delete(AlbumEntry.TABLE_NAME, selection, selectionArgs);

        removeSongs(album);
    }

    private void removeSongs(@NonNull Album album) {
        String selection = SongEntry.COLUMN_NAME_ALBUM_ID + " LIKE ?";
        String[] selectionArgs = {album.getId()};
        mDb.delete(SongEntry.TABLE_NAME, selection, selectionArgs);
    }

    @NonNull
    @Override
    public List<Album> getAlbums() {
        List<Album> tasks = new ArrayList<>();
        try {
            String[] projection =  {
                    AlbumEntry.COLUMN_NAME_ENTRY_ID,
                    AlbumEntry.COLUMN_NAME_ALBUM_NAME,
                    AlbumEntry.COLUMN_NAME_ALBUM_PATH,
                    AlbumEntry.COLUMN_NAME_ALBUM_IMAGE,
            };

            Cursor c = mDb.query(
                    AlbumEntry.TABLE_NAME, projection, null, null, null, null, null);

            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    String albumId = c
                            .getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ENTRY_ID));
                    String albumName = c
                            .getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ALBUM_NAME));
                    String albumPath =
                            c.getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ALBUM_PATH));
                    String albumImagePath =
                            c.getString(c.getColumnIndexOrThrow(AlbumEntry.COLUMN_NAME_ALBUM_IMAGE));
                    Album album = new Album(albumId, albumName, albumPath, albumImagePath);
                    tasks.add(album);
                }
            }
            if (c != null) {
                c.close();
            }

        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
        return tasks;

    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull String albumId) {
        List<Song> songs = new ArrayList<>();
        try {
            String[] projection =  {
                    SongEntry.COLUMN_NAME_ENTRY_ID,
                    SongEntry.COLUMN_NAME_SONG_NAME,
                    SongEntry.COLUMN_NAME_SONG_PATH,
                    SongEntry.COLUMN_NAME_SONG_IMAGE,
            };

            String selection = SongEntry.COLUMN_NAME_ALBUM_ID + " LIKE ?";
            String[] selectionArgs = { albumId };

            Cursor c = mDb.query(
                    SongEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    String songId = c
                            .getString(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_ENTRY_ID));
                    String songName = c
                            .getString(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_SONG_NAME));
                    String songPath =
                            c.getString(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_SONG_PATH));
                    String songImagePath =
                            c.getString(c.getColumnIndexOrThrow(SongEntry.COLUMN_NAME_SONG_IMAGE));
                    Song song = new Song(songId, songName, songPath, albumId, songImagePath);
                    songs.add(song);
                }
            }
            if (c != null) {
                c.close();
            }

        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
        return songs;
    }
}
