package com.fisko.music.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.fisko.music.R;
import com.fisko.music.data.Song;
import com.fisko.music.ui.song.SongFragment;

import java.util.ArrayList;
import java.util.List;

public final class UIUtils {

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

}
