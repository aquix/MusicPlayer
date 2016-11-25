package com.vlad.player.data.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.vlad.player.data.models.Artist;
import com.vlad.player.data.models.Song;
import com.vlad.player.data.services.DbConstants.ArtistEntity;
import com.vlad.player.data.services.DbConstants.SongEntity;
import com.vlad.player.data.viewmodels.SongFullInfo;

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

    @Override
    public List<Song> getSongsForArtist(long artistId, boolean sortByName) {
        SQLiteDatabase db = this.dbOpenHelper.getReadableDatabase();
        List<Song> songs = new ArrayList<>();
        String[] columnNames =  {
                SongEntity.ID,
                SongEntity.NAME,
                SongEntity.PATH,
                SongEntity.DURATION,
                SongEntity.IMAGE_PATH,
        };

        String selection = SongEntity.ARTIST_ID + " = ?";
        String[] selectionArgs = { String.valueOf(artistId) };

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
                long songId = c
                        .getLong(c.getColumnIndexOrThrow(SongEntity.ID));
                String songName = c
                        .getString(c.getColumnIndexOrThrow(SongEntity.NAME));
                String songPath =
                        c.getString(c.getColumnIndexOrThrow(SongEntity.PATH));
                int songDuration =
                        c.getInt(c.getColumnIndexOrThrow(SongEntity.DURATION));
                String songImagePath =
                        c.getString(c.getColumnIndexOrThrow(SongEntity.IMAGE_PATH));
                Song song = new Song(songId, songName, songPath, songImagePath, songDuration, artistId);
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
    public List<Artist> getAllArtists() {
        SQLiteDatabase db = this.dbOpenHelper.getReadableDatabase();
        List<Artist> artists = new ArrayList<>();
        String[] columnNames =  {
                ArtistEntity.ID,
                ArtistEntity.NAME,
                ArtistEntity.IMAGE_PATH,
        };

        Cursor c = db.query(
                ArtistEntity.TABLE_NAME, columnNames, null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                long albumId = c
                        .getLong(c.getColumnIndexOrThrow(ArtistEntity.ID));
                String albumName =
                        c.getString(c.getColumnIndexOrThrow(ArtistEntity.NAME));
                String albumImagePath =
                        c.getString(c.getColumnIndexOrThrow(ArtistEntity.IMAGE_PATH));
                Artist artist = new Artist(albumId, albumName, albumImagePath);
                artists.add(artist);
            }
        }
        if (c != null) {
            c.close();
        }
        db.close();

        return artists;
    }

    @Override
    public long addSongsForArtist(@NonNull List<Song> songs, Artist artist) {
        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();

        Artist existingArtist = this.getArtistByName(db, artist.getName());
        long artistId = 0;
        if (existingArtist == null) {
            artistId = this.addArtist(db, artist);
        }

        for(Song song: songs) {
            if(this.isEntityExist(db, SongEntity.TABLE_NAME, SongEntity.PATH, song.getPath())) {
                continue;
            }

            ContentValues values = new ContentValues();
            values.put(SongEntity.ARTIST_ID, artistId);
            values.put(SongEntity.NAME, song.getName());
            values.put(SongEntity.PATH, song.getPath());
            values.put(SongEntity.DURATION, song.getDuration());
            values.put(SongEntity.IMAGE_PATH, song.getImagePath());

            db.insert(SongEntity.TABLE_NAME, null, values);
        }
        db.close();
        return artistId;
    }

    @Override
    public void deleteArtist(@NonNull Artist artist) {
        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();

        String selection = ArtistEntity.ID + " = ?";
        String[] selectionArgs = { String.valueOf(artist.getId()) };
        db.delete(ArtistEntity.TABLE_NAME, selection, selectionArgs);
        this.deleteSongsForArtist(db, artist);

        db.close();
    }

    @Override
    public void deleteSong(@NonNull Song song) {
        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();
        String selection = SongEntity.ID + " = ?";
        String[] selectionArgs = { String.valueOf(song.getId()) };
        db.delete(SongEntity.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    @Override
    public ArrayList<SongFullInfo> getAllSongs() {
        SQLiteDatabase db = this.dbOpenHelper.getReadableDatabase();

        String table = ArtistEntity.TABLE_NAME + " as Artists inner join "+ SongEntity.TABLE_NAME +" as Songs " +
                "on Artists."+ ArtistEntity.ID +" = Songs." + SongEntity.ARTIST_ID;
        String[] columnNames = {
                "Songs."+ SongEntity.ID +" as Id",
                "Artists."+ ArtistEntity.NAME +" as Artist",
                "Songs."+ SongEntity.NAME +" as Title",
                "Songs."+ SongEntity.DURATION +" as Duration",
                "Songs."+ SongEntity.IMAGE_PATH +" as ImagePath"
        };
        Cursor c = db.query(table, columnNames, null, null, null, null, null);

        ArrayList<SongFullInfo> result = new ArrayList<>();

        while (c.moveToNext()) {
            long id =
                    c.getLong(c.getColumnIndex("Id"));
            String artist =
                    c.getString(c.getColumnIndexOrThrow("Artist"));
            String title =
                    c.getString(c.getColumnIndexOrThrow("Title"));
            int duration =
                    c.getInt(c.getColumnIndexOrThrow("Duration"));
            String imagePath =
                    c.getString(c.getColumnIndexOrThrow("ImagePath"));
            result.add(new SongFullInfo(id, artist, title, imagePath, duration));
        }

        c.close();
        db.close();

        return result;
    }

    @Override
    public void clearDb() {
        String clearArtistsQuery = "delete from " + ArtistEntity.TABLE_NAME;
        String clearSongsQuery = "delete from " + SongEntity.TABLE_NAME;

        SQLiteDatabase db = this.dbOpenHelper.getWritableDatabase();
        db.rawQuery(clearArtistsQuery, null);
        db.rawQuery(clearSongsQuery, null);
        db.close();
    }

    private Artist getArtistByName(SQLiteDatabase db, String name) {
        Artist artist = null;
        String[] columnNames =  {
                ArtistEntity.ID,
                ArtistEntity.NAME,
                ArtistEntity.IMAGE_PATH,
        };

        Cursor c = db.query(
                ArtistEntity.TABLE_NAME, columnNames,
                ArtistEntity.NAME + " LIKE ?", new String[] { name },
                null, null, null);

        if  (c != null && c.moveToNext()) {
            long albumId = c
                    .getLong(c.getColumnIndexOrThrow(ArtistEntity.ID));
            String albumName =
                    c.getString(c.getColumnIndexOrThrow(ArtistEntity.NAME));
            String albumImagePath =
                    c.getString(c.getColumnIndexOrThrow(ArtistEntity.IMAGE_PATH));
            artist = new Artist(albumId, albumName, albumImagePath);
        }

        if (c != null) {
            c.close();
        }

        return artist;
    }

    private long addArtist(SQLiteDatabase db, @NonNull Artist artist) {
        Artist existingArtist = this.getArtistByName(db, artist.getName());
        if (existingArtist != null) {
            return artist.getId();
        }

        ContentValues values = new ContentValues();
        values.put(ArtistEntity.NAME, artist.getName());
        values.put(ArtistEntity.IMAGE_PATH, artist.getImagePath());

        return db.insert(ArtistEntity.TABLE_NAME, null, values);
    }

    private void deleteSongsForArtist(SQLiteDatabase db, @NonNull Artist artist) {
        String selection = SongEntity.ARTIST_ID + " LIKE ?";
        String[] selectionArgs = { String.valueOf(artist.getId()) };
        db.delete(SongEntity.TABLE_NAME, selection, selectionArgs);
    }

    private boolean isEntityExist(SQLiteDatabase db, String tableName, String fieldName, String entryId) {
        Cursor c = null;
        try {
            String query = "select count(*) from " + tableName + " where " + fieldName + " = ?";
            c = db.rawQuery(query, new String[]{ entryId });
            return c.moveToFirst() && c.getInt(0) != 0;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
