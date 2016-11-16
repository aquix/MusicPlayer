package com.vlad.player.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;

import java.util.UUID;

public class Album implements Parcelable {
    private String id;
    private String name;
    private String artist;
    private String path;
    private String imagePath;

    public Album(String name, String artist, String path, String imagePath) {
        this(UUID.randomUUID().toString(), name, artist, path, imagePath);
    }

    public Album(String id, String name, String artist, String path, String imagePath) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.imagePath = imagePath;
    }

    private Album(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        artist = parcel.readString();
        path = parcel.readString();
        imagePath = parcel.readString();
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
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
        parcel.writeString(artist);
        parcel.writeString(path);
        parcel.writeString(imagePath);
    }

    public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != Album.class) {
            return false;
        }
        final Album album = (Album) obj;
        return Objects.equal(id, album.getId()) &&
                Objects.equal(name, album.getName()) &&
                Objects.equal(artist, album.getArtist()) &&
                Objects.equal(path, album.getPath()) &&
                Objects.equal(imagePath, album.getImagePath());
    }

}
