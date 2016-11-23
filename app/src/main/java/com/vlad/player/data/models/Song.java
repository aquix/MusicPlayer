package com.vlad.player.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;


public class Song implements Parcelable {
    private long id;
    private String name;
    private String path;
    private String imagePath;
    private int duration;
    private long artistId;

    public Song(String name, String path, String imagePath, int duration, long artistId) {
        this(0, name, path, imagePath, duration, artistId);
    }

    public Song(long id, String name, String path, String imagePath, int duration, long artistId) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.imagePath = imagePath;
        this.duration = duration;
        this.artistId = artistId;
    }

    private Song(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        this.path = parcel.readString();
        this.imagePath = parcel.readString();
        this.duration = parcel.readInt();
        this.artistId = parcel.readLong();
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(this.id);
        parcel.writeString(this.name);
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
                Objects.equal(this.name, song.getName()) &&
                Objects.equal(this.path, song.getPath()) &&
                Objects.equal(this.imagePath, song.getImagePath()) &&
                Objects.equal(this.duration, song.getDuration()) &&
                Objects.equal(this.artistId, song.getArtistId());
    }

}
