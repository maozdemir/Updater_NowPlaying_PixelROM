package com.contested.zeroiq.sense;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;

import com.contested.zeroiq.sense.services.BackgroundService;
import com.contested.zeroiq.sense.services.DownloaderService;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean ambientStatus = sharedPreferences.getBoolean("ambient_disabler",true);
        if(ambientStatus) {
            Intent intent = new Intent(MainActivity.this, BackgroundService.class);
            startForegroundService(intent);
        }
        Intent intentE = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intentE);
        finish();
    }
}