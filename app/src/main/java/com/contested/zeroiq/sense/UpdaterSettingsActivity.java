package com.contested.zeroiq.sense;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.contested.zeroiq.sense.services.BackgroundService;

public class UpdaterSettingsActivity extends AppCompatActivity {


    private UpdaterFragment updaterFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setTheme(android.R.style.Theme_DeviceDefault_DayNight);
        //getSupportActionBar().hide();
        setContentView(R.layout.settings_activity);
        updaterFragment = new UpdaterFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, updaterFragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Preference historyMenu = updaterFragment.findPreference("ambient_history");
        //Preference ambientDisabler = updaterFragment.findPreference("ambient_disabler");
        CharSequence entrySeq4checkFreq[] = {"Daily", "Weekly", "Monthly"};
        CharSequence entrySeq4kernelSub[] = {"Release channel", "Nightly channel"};
        String aaa = "aaa";
        final ListPreference checkFrequency = updaterFragment.findPreference("checkFrequency");
        final ListPreference kernelSubscription = updaterFragment.findPreference("kernelSubscription");
        checkFrequency.setEntries(entrySeq4checkFreq);
        checkFrequency.setSummary(
                PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString(checkFrequency.getKey(),"Monthly")
        );
        checkFrequency.setDialogTitle("Checking frequency");
        checkFrequency.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                checkFrequency.setSummary(newValue.toString());
                return true;
            }
        });
        kernelSubscription.setEntries(entrySeq4kernelSub);
        kernelSubscription.setEntryValues(entrySeq4kernelSub);
        kernelSubscription.setSummary(
                PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getString(kernelSubscription.getKey(),"Release channel")
        );
        kernelSubscription.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                kernelSubscription.setSummary(newValue.toString());
                return true;
            }
        });
    }

    public static class UpdaterFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.prefs_updater, rootKey);
        }
    }
}
