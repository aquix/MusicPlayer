package com.fisko.music.ui.songs;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fisko.music.R;
import com.fisko.music.ui.albums.AlbumsListAdapter;

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
            String albumId = savedInstanceState.getString(AlbumsListAdapter.ALBUM_ID);
            songsFragment = SongsFragment.newInstance(albumId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, songsFragment);
            transaction.commit();
        }
    }
}
