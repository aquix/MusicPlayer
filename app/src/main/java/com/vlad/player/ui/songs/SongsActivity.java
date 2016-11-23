package com.vlad.player.ui.songs;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.vlad.player.R;
import com.vlad.player.data.models.Artist;
import com.vlad.player.data.models.Song;
import com.vlad.player.utils.Constants;

public class SongsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getIntent().getExtras();
        this.setContentView(R.layout.songs_activity);
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        Artist artist =  null;
        Song openedSong = null;
        if (bundle != null) {
            artist = bundle.getParcelable(Constants.ALBUM_BUNDLE.ALBUM);
            openedSong = bundle.getParcelable(Constants.SONG_BUNDLE.OPENED_SONG);
        }

        if (savedInstanceState != null) {
            artist = savedInstanceState.getParcelable(Constants.ALBUM_BUNDLE.ALBUM);
            openedSong = savedInstanceState.getParcelable(Constants.SONG_BUNDLE.OPENED_SONG);
        }

        SongsFragment songsFragment =
                (SongsFragment) this.getSupportFragmentManager().findFragmentByTag(Constants.SONGS_FRAGMENT_TAG);
        if (songsFragment == null) {
            songsFragment = SongsFragment.newInstance(artist, openedSong);
            FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, songsFragment, Constants.SONGS_FRAGMENT_TAG);
            transaction.commit();
        }
    }
}
