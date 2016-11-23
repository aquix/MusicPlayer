package com.vlad.player.data.viewmodels;

public class SongFullInfo {
    public long Id;
    public String Artist;
    public String Title;
    public String ImagePath;
    public int Duration;

    public SongFullInfo(long id, String artist, String title, String imagePath, int duration) {
        this.Id = id;
        this.Artist = artist;
        this.Title = title;
        this.ImagePath = imagePath;
        this.Duration = duration;
    }
}
