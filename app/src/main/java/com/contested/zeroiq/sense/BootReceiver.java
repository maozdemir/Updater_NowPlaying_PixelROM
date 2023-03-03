package com.contested.zeroiq.sense;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.contested.zeroiq.sense.services.BackgroundService;
import com.contested.zeroiq.sense.services.UpdateCheckWorker;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean ambientStatus = sharedPreferences.getBoolean("ambient_disabler",true);
        WorkManager wm = WorkManager.getInstance(context);
        wm.cancelAllWork();
        PeriodicWorkRequest updateCheckWorkRequest = new PeriodicWorkRequest.Builder(UpdateCheckWorker.class, 1, TimeUnit.DAYS).build();
        wm.enqueueUniquePeriodicWork("updaterWorker", ExistingPeriodicWorkPolicy.REPLACE, updateCheckWorkRequest);
        if(ambientStatus) {
            Intent intent2 = new Intent(context, BackgroundService.class);
            context.startForegroundService(intent2);
        }
    }
}