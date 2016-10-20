package com.fisko.music.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import com.google.common.base.Objects;


public class Song implements Parcelable {
    private String mId;
    private String mName;
    private String mPath;
    private String mImagePath;
    private int mDuration;
    private String mAlbumId;

    public Song(String name, String path, String imagePath, int duration, String albumId) {
        this(UUID.randomUUID().toString(), name, path, imagePath, duration, albumId);
    }

    public Song(String id, String name, String path, String imagePath, int duration, String albumId) {
        mId = id;
        mName = name;
        mPath = path;
        mImagePath = imagePath;
        mDuration = duration;
        mAlbumId = albumId;
    }

    private Song(Parcel parcel) {
        mId = parcel.readString();
        mName = parcel.readString();
        mPath = parcel.readString();
        mImagePath = parcel.readString();
        mDuration = parcel.readInt();
        mAlbumId = parcel.readString();
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getPath() {
        return mPath;
    }

    public String getAlbumId() {
        return  mAlbumId;
    }

    public int getDuration() {
        return  mDuration;
    }

    public String getImagePath() {
        return  mImagePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mId);
        parcel.writeString(mName);
        parcel.writeString(mPath);
        parcel.writeString(mImagePath);
        parcel.writeInt(mDuration);
        parcel.writeString(mAlbumId);
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
        return Objects.equal(mId, song.getId()) &&
                Objects.equal(mName, song.getName()) &&
                Objects.equal(mPath, song.getPath()) &&
                Objects.equal(mImagePath, song.getImagePath()) &&
                Objects.equal(mDuration, song.getDuration()) &&
                Objects.equal(mAlbumId, song.getAlbumId());
    }

}
