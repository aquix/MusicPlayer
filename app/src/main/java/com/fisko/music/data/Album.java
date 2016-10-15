package com.fisko.music.data;

import java.util.UUID;

public class Album {
    private String mId;
    private String mName;
    private String mPath;
    private String mImagePath;

    public Album(String name, String path, String imagePath) {
        this(UUID.randomUUID().toString(), name, path, imagePath);
    }

    public Album(String id, String name, String path, String imagePath) {
        mId = id;
        mName = name;
        mPath = path;
        mImagePath = imagePath;
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
    public String getImagePath() {
        return mImagePath;
    }
}
