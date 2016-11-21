package com.vlad.player.utils;


import android.accounts.NetworkErrorException;
import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public final class LastFmService {
    private static final String LAST_FM_API_KEY = "7edc9ed8ea208544f00c800ab9ebd963";

    public static String getSongCover(MusicUtils.SongInfo songInfo)
            throws JSONException, NetworkErrorException {
        Uri uri = buildUrl(songInfo)
                .appendQueryParameter("method", "track.getinfo")
                .build();
        JSONObject response = NetworkService.get(uri.toString());

        if (response == null) {
            throw new NetworkErrorException();
        }

        return response
                .getJSONObject("track")
                .getJSONObject("album")
                .getJSONArray("image")
                .getJSONObject(2)
                .getString("#text");
    }

    public static String genAlbomInfoUrl(MusicUtils.SongInfo songInfo) {
        Uri uri = buildUrl(songInfo)
                .appendQueryParameter("method", "albom.getinfo")
                .build();
        return uri.toString();
    }

    private static Uri.Builder buildUrl(MusicUtils.SongInfo songInfo) {
        return new Uri.Builder()
                .scheme("http")
                .authority("ws.audioscrobbler.com")
                .path("2.0")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("artist", songInfo.artist)
                .appendQueryParameter("track", songInfo.title)
                .appendQueryParameter("api_key", LAST_FM_API_KEY);
    }
}
