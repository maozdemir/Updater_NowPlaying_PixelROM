package com.contested.zeroiq.sense.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.contested.zeroiq.sense.R;
import com.contested.zeroiq.sense.UpdaterActivity;
import com.contested.zeroiq.sense.utils.ROMUpdates;
import com.contested.zeroiq.sense.utils.RSSChecker;
import com.contested.zeroiq.sense.utils.SystemPropsSupplier;

import static android.content.ContentValues.TAG;

public class UpdateCheckWorker extends Worker {
    public UpdateCheckWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //if(!isNetworkAvailable()) Result.failure();
        ROMUpdates romUpdates= null;
        try {
            romUpdates = RSSChecker.checkThePage(getApplicationContext());
        } catch (Exception e) {
            Log.d(TAG, "doWork: There was an error in UpdateCheckWorker.java");
            e.printStackTrace();
        }
        if(SystemPropsSupplier.DEVICE_BUILD_DATE >= romUpdates.getBuildDate()) {
            //TODO: change == to >= to avoid failures on testers
            Data data = new Data.Builder()
                    .putLong("buildDate", romUpdates.getBuildDate())
                    .putBoolean("isUpToDate",true)
                    .build();
            return Result.success(data);
        } else {
            Intent intent = new Intent(getApplicationContext(), UpdaterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            Notification.Builder notification;
            notification = new Notification.Builder(
                    getApplicationContext(),
                    "BACKGROUND_SERVICE_UPDATER")
                    .setContentTitle("New system update available.")
                    .setContentText("Click here to see more info")
                    //.setPriority(Notification.PRIORITY_HIGH)
                    .setOngoing(true).setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_audiotrack_light);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Log.d("Pixel ROM Log", "onStartCommand");
            NotificationChannel notificationChannel = new NotificationChannel(
                    "BACKGROUND_SERVICE_UPDATER",
                    "Foreground Service for Updater",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(false);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(notificationChannel);
            notificationManager.notify(1, notification.build());
            Data data = new Data.Builder()
                    .putLong("buildDate", romUpdates.getBuildDate())
                    .putBoolean("isUpToDate",false)
                    .putString("dlLink",romUpdates.getDownloadLink())
                    .build();
            return Result.success(data);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
