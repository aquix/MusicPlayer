package com.vlad.player.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;

public class Artist implements Parcelable {
    private long id;
    private String name;
    private String imagePath;

    public Artist(String name, String imagePath) {
        this(0, name, imagePath);
    }

    public Artist(long id, String name, String imagePath) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
    }

    private Artist(Parcel parcel) {
        this.id = parcel.readLong();
        this.name = parcel.readString();
        this.imagePath = parcel.readString();
    }


    public long getId() {
        return this.id;
    }

    public void updateId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
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
        parcel.writeString(this.imagePath);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != Artist.class) {
            return false;
        }
        final Artist artist = (Artist) obj;
        return Objects.equal(this.id, artist.getId()) &&
                Objects.equal(this.name, artist.getName()) &&
                Objects.equal(this.imagePath, artist.getImagePath());
    }
}
