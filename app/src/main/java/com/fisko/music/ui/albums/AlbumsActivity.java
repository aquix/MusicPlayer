package com.fisko.music.ui.albums;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.fisko.music.R;
import com.fisko.music.service.SearchService;

import java.util.ArrayList;
import java.util.Arrays;

public class AlbumsActivity extends AppCompatActivity {

    private static String[] PERMISSIONS;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            PERMISSIONS = new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
        }
    }


    private SearchService mService;
    private boolean mServiceBound;
    private static final int REQUEST_READ_STORAGE = 100;

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
        if (mServiceBound) {
            mService.startMusicSearch();
        } else {
            Intent intent = new Intent(this, SearchService.class);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
        }
    }

    private void requestPermAndSearchMusic() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_READ_STORAGE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mServiceBound) {
            unbindService(mConnection);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.albums_search_menu:
                requestPermAndSearchMusic();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    searchMusic();
                } else {
                    Toast.makeText(this, R.string.read_storage_not_granted, Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.albums_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            SearchService.LocalBinder binder = (SearchService.LocalBinder) service;
            mService = binder.getService();
            mServiceBound = true;
            mService.startMusicSearch();
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mServiceBound = false;
        }
    };


}
