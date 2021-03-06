package com.fisko.music.ui.songs;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fisko.music.R;
import com.fisko.music.data.Album;
import com.fisko.music.data.Song;
import com.fisko.music.utils.Constants;

public class SongsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.songs_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Album album =  null;
        Song openedSong = null;
        if (bundle != null) {
            album = bundle.getParcelable(Constants.ALBUM_BUNDLE.ALBUM);
            openedSong = bundle.getParcelable(Constants.SONG_BUNDLE.OPENED_SONG);
        }
        if (savedInstanceState != null) {
            album = savedInstanceState.getParcelable(Constants.ALBUM_BUNDLE.ALBUM);
            openedSong = savedInstanceState.getParcelable(Constants.SONG_BUNDLE.OPENED_SONG);
        }

        SongsFragment songsFragment =
                (SongsFragment) getSupportFragmentManager().findFragmentByTag(Constants.SONGS_FRAGMENT_TAG);
        if (songsFragment == null) {
            songsFragment = SongsFragment.newInstance(album, openedSong);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, songsFragment, Constants.SONGS_FRAGMENT_TAG);
            transaction.commit();
        }
    }
}
