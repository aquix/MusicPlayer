package com.vlad.player.ui.songs;

import android.app.ListActivity;
import android.os.Bundle;

import com.vlad.player.R;
import com.vlad.player.data.services.DbObservableContext;
import com.vlad.player.data.viewmodels.SongFullInfo;

import java.util.List;

public class AllSongsActivity extends ListActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.all_songs_activity);

        DbObservableContext dbService = DbObservableContext.getInstance(this);

        List<SongFullInfo> allSongs = dbService.getAllSongs();
        AllSongsListAdapter adapter = new AllSongsListAdapter(allSongs, this);

        this.setListAdapter(adapter);
    }
}
