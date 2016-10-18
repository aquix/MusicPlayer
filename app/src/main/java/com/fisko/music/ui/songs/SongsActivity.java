package com.fisko.music.ui.songs;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.fisko.music.utils.Constants;

public class SongsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        SongsFragment songsFragment =
                (SongsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (songsFragment == null) {
            String albumId = savedInstanceState.getString(Constants.ALBUM_BUNDLE.ALBUM_ID);
            Song openedSong = savedInstanceState.getParcelable(Constants.SONG_BUNDLE.OPENED_SONG);
            songsFragment = SongsFragment.newInstance(albumId, openedSong);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, songsFragment);
            transaction.commit();
        }
    }
}
