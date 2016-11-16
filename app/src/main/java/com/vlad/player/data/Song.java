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
        id = parcel.readString();
        name = parcel.readString();
        path = parcel.readString();
        imagePath = parcel.readString();
        duration = parcel.readInt();
        albumId = parcel.readString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getAlbumId() {
        return albumId;
    }

    public int getDuration() {
        return duration;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(path);
        parcel.writeString(imagePath);
        parcel.writeInt(duration);
        parcel.writeString(albumId);
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
        return Objects.equal(id, song.getId()) &&
                Objects.equal(name, song.getName()) &&
                Objects.equal(path, song.getPath()) &&
                Objects.equal(imagePath, song.getImagePath()) &&
                Objects.equal(duration, song.getDuration()) &&
                Objects.equal(albumId, song.getAlbumId());
    }

}
