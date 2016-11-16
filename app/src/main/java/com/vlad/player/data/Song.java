package com.vlad.player.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import com.google.common.base.Objects;


public class Song implements Parcelable {
    private String id;
    private String name;
    private String path;
    private String imagePath;
    private int duration;
    private String albumId;

    public Song(String name, String path, String imagePath, int duration, String albumId) {
        this(UUID.randomUUID().toString(), name, path, imagePath, duration, albumId);
    }

    public Song(String id, String name, String path, String imagePath, int duration, String albumId) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.imagePath = imagePath;
        this.duration = duration;
        this.albumId = albumId;
    }

    private Song(Parcel parcel) {
        this.id = parcel.readString();
        this.name = parcel.readString();
        this.path = parcel.readString();
        this.imagePath = parcel.readString();
        this.duration = parcel.readInt();
        this.albumId = parcel.readString();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public String getAlbumId() {
        return this.albumId;
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
        parcel.writeString(this.id);
        parcel.writeString(this.name);
        parcel.writeString(this.path);
        parcel.writeString(this.imagePath);
        parcel.writeInt(this.duration);
        parcel.writeString(this.albumId);
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
                Objects.equal(this.albumId, song.getAlbumId());
    }

}
