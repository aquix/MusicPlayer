package com.vlad.player.utils;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.vlad.player.R;
import com.vlad.player.data.Song;
import com.vlad.player.ui.song.SongFragment;

import java.util.ArrayList;
import java.util.List;

public final class UiUtils {
    public static void openSongPlayer(Song song, List<Song> songs, FragmentActivity activity) {
        SongFragment songFragment = (SongFragment) activity.getSupportFragmentManager()
                .findFragmentByTag(Constants.SONG_FRAGMENT_TAG);
        if (songFragment == null) {
            songFragment = SongFragment.newInstance(songs.indexOf(song), (ArrayList<Song>) songs);
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(Constants.SONG_FRAGMENT_TAG)
                    .replace(R.id.content_frame, songFragment);
            transaction.commit();
        }
    }

    public static Toolbar setUpToolbar(final boolean isFinishActivity, String title, final AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        toolbar.setTitle(title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFinishActivity) {
                    activity.finish();
                } else {
                    activity.getSupportFragmentManager().popBackStack();
                }
            }
        });
        return toolbar;
    }

}
