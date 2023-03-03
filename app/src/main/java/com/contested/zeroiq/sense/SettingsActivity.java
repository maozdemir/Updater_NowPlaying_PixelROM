package com.contested.zeroiq.sense;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.contested.zeroiq.sense.services.BackgroundService;

public class SettingsActivity extends AppCompatActivity  {

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(android.R.style.Theme_DeviceDefault_DayNight);
        getSupportActionBar().hide();
        setContentView(R.layout.settings_activity);
        settingsFragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(getApplicationContext(),"Back button clicked", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Preference historyMenu = settingsFragment.findPreference("ambient_history");
        Preference ambientDisabler = settingsFragment.findPreference("ambient_disabler");
        ambientDisabler.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.equals(true)){
                    Log.i("ambient_disabler","enables");
                    startForegroundService(new Intent(SettingsActivity.this, BackgroundService.class));
                    return true;
                }
                else{
                    Log.i("ambient_disabler","ddisables");
                    stopService(new Intent(SettingsActivity.this, BackgroundService.class));
                    return true;
                }
            }
        });
        historyMenu.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent intentE = new Intent(SettingsActivity.this, HistoryActivity.class);
                startActivity(intentE);
                return false;
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.prefs, rootKey);
        }
    }
}