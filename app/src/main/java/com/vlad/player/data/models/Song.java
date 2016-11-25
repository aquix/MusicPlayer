package com.vlad.player.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;


public class Song implements Parcelable {
    private long id;
    private String title;
    private String path;
    private String album;
    private String imagePath;
    private int duration;
    private long artistId;

    public Song(String title, String album, String path, String imagePath, int duration, long artistId) {
        this(0, title, album, path, imagePath, duration, artistId);
    }

    public Song(long id, String title, String album, String path, String imagePath, int duration, long artistId) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.path = path;
        this.imagePath = imagePath;
        this.duration = duration;
        this.artistId = artistId;
    }

    private Song(Parcel parcel) {
        this.id = parcel.readLong();
        this.title = parcel.readString();
        this.album = parcel.readString();
        this.path = parcel.readString();
        this.imagePath = parcel.readString();
        this.duration = parcel.readInt();
        this.artistId = parcel.readLong();
    }

    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPath() {
        return this.path;
    }

    public long getArtistId() {
        return this.artistId;
    }

    public int getDuration() {
        return this.duration;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public String getAlbum() {
        return this.album;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(this.id);
        parcel.writeString(this.title);
        parcel.writeString(this.album);
        parcel.writeString(this.path);
        parcel.writeString(this.imagePath);
        parcel.writeInt(this.duration);
        parcel.writeLong(this.artistId);
    }

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != Song.class) {
            return false;
        }
        final Song song = (Song) obj;
        return Objects.equal(this.id, song.getId()) &&
                Objects.equal(this.title, song.getTitle()) &&
                Objects.equal(this.path, song.getPath()) &&
                Objects.equal(this.imagePath, song.getImagePath()) &&
                Objects.equal(this.duration, song.getDuration()) &&
                Objects.equal(this.artistId, song.getArtistId());
    }
}
