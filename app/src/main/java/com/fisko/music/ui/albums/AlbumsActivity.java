package com.fisko.music.ui.albums;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fisko.music.R;
import com.fisko.music.service.SearchService;

public class AlbumsActivity extends AppCompatActivity {

    private SearchService mService;
    private boolean mServiceBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            SearchService.LocalBinder binder = (SearchService.LocalBinder) service;
            mService = binder.getService();
            mServiceBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        AlbumsFragment albumsFragment =
                (AlbumsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (albumsFragment == null) {
            albumsFragment = AlbumsFragment.newInstance();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.content_frame, albumsFragment);
            transaction.commit();
        }
    }

    private void searchMusic() {
        Intent intent = new Intent(this, SearchService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        if(mServiceBound) {
            unbindService(mConnection);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.albums_search_menu:
                searchMusic();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.albums_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
