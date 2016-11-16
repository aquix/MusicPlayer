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
        this.id = parcel.readString();
        this.name = parcel.readString();
        this.artist = parcel.readString();
        this.path = parcel.readString();
        this.imagePath = parcel.readString();
    }


    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getPath() {
        return this.path;
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
        parcel.writeString(this.artist);
        parcel.writeString(this.path);
        parcel.writeString(this.imagePath);
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
        return Objects.equal(this.id, album.getId()) &&
                Objects.equal(this.name, album.getName()) &&
                Objects.equal(this.artist, album.getArtist()) &&
                Objects.equal(this.path, album.getPath()) &&
                Objects.equal(this.imagePath, album.getImagePath());
    }

}
