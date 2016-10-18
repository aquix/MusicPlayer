package com.fisko.music.utils;


import android.net.Uri;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class NetworkUtils {

    private static final String LAST_FM_API_KEY = "";

    public static JSONObject loadURL(String path) {
        InputStream is = null;
        try {
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            is = conn.getInputStream();

            InputStreamReader reader = null;
            String response = null;
            try {
                reader = new InputStreamReader(is, Charsets.UTF_8);
                response = CharStreams.toString(reader);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return new JSONObject(response);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static Uri.Builder getUrlBase(MusicUtils.SongInfo songInfo) {
        return new Uri.Builder()
                .scheme("http")
                .authority("ws.audioscrobbler.com")
                .path("2.0")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("artist", songInfo.artist)
                .appendQueryParameter("album", songInfo.album)
                .appendQueryParameter("album", songInfo.album)
                .appendQueryParameter("api_key", LAST_FM_API_KEY);
    }

    public static String genSongInfoUrl(MusicUtils.SongInfo songInfo) {
        Uri uri = getUrlBase(songInfo)
            .appendQueryParameter("method", "track.getinfo")
            .build();
        return uri.toString();
    }

    public static String genAlbomInfoUrl(MusicUtils.SongInfo songInfo) {
        Uri uri = getUrlBase(songInfo)
                .appendQueryParameter("method", "albom.getinfo")
                .build();
        return uri.toString();
    }
}
