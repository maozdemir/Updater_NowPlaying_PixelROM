package com.contested.zeroiq.sense;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.contested.zeroiq.sense.utils.DBConstructor;
import com.contested.zeroiq.sense.utils.DBHelper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class HistoryActivity extends AppCompatActivity {
    static ArrayList<String> track = new ArrayList<String>();
    static ArrayList<String> artist = new ArrayList<String>();
    static ArrayList<String> date = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        this.setTheme(android.R.style.Theme_DeviceDefault_DayNight);
        //getSupportActionBar().hide();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new HistoryFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBConstructor.FeedEntry.TABLE_NAME + " ORDER BY date DESC", null);

        if(cursor.moveToFirst()) {
            do {
                track.add(cursor.getString(cursor.getColumnIndex("track")));
                artist.add(cursor.getString(cursor.getColumnIndex("artist")));
                date.add(cursor.getString(cursor.getColumnIndex("date")));
            }while(cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    public static class HistoryFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            //setPreferencesFromResource(R.xml.prefs, rootKey);
            int array_size = track.size();
            Log.d("Pixel ROM Log.Size of tracks", array_size+"");
            Context context = getActivity();
            PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
            preferenceScreen.removeAll();
            PreferenceCategory preferenceCategory = new PreferenceCategory(context);
            preferenceScreen.addPreference(preferenceCategory);
            int i;
            if(array_size >= 1) {
                for (i = 0; i < array_size; i++) {
                    long milliSeconds = Long.parseLong(date.get(i));
                    Timestamp timestamp = new Timestamp(Long.parseLong(date.get(i)));
                    String text;
                    Calendar calendar = Calendar.getInstance();
                    Calendar newCalendar = Calendar.getInstance();

                    calendar.setTimeInMillis(milliSeconds);
                    String minutes = calendar.get(Calendar.MINUTE)+"";
                    if(minutes.trim().length() == 1) minutes = "0"+calendar.get(Calendar.MINUTE);
                    int seconds = calendar.get(Calendar.SECOND);
                    String hours = calendar.get(Calendar.HOUR_OF_DAY)+"";
                    if(hours.trim().length() == 1) hours = "0"+calendar.get(Calendar.HOUR_OF_DAY);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    //Log.d("MONTH",Calendar.MONTH+"");
                    /*if (day == newCalendar.get(Calendar.DAY_OF_MONTH) && month == (newCalendar.get(Calendar.MONTH) + 1)) {
                        text = "Today, " + hours + ":" + minutes;
                    } else if (calendar.get(Calendar.DAY_OF_MONTH) == (newCalendar.get(Calendar.DAY_OF_MONTH) - 1) && calendar.get(Calendar.MONTH) == (newCalendar.get(Calendar.MONTH))) {
                        text = "Yesterday, " + hours + ":" + minutes;
                    } else {
                        text = month+"/"+day+"/"+year+" - "+hours+":"+minutes;
                    }*/
                    text = DateUtils.getRelativeTimeSpanString(milliSeconds).toString();
                    Preference preference = new Preference(context);
                    preference.setTitle(track.get(i));
                    preference.setSummary(artist.get(i) + " | " + text);
                    preferenceCategory.addPreference(preference);
                }
                setPreferenceScreen(preferenceScreen);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        track.clear();
        date.clear();
        artist.clear();
    }


}