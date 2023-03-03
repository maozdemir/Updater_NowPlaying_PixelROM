package com.contested.zeroiq.sense.services;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.os.RecoverySystem;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.contested.zeroiq.sense.R;
import com.contested.zeroiq.sense.UpdaterActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;

import static android.content.ContentValues.TAG;

public class DownloaderService extends Service {

    static int UPDATER_ERROR_NOT_VERIFIED = 1;
    static int UPDATER_ERROR_FAIILED_DOWNLOAD = 2;
    static int UPDATER_SUCCESS_INSTALL = 99;
    Notification.Builder notification = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        final SharedPreferences sharedPref = this.getSharedPreferences("updateChecker", Context.MODE_PRIVATE);
        notification = new Notification.Builder(getBaseContext(), "UPDATE_DOWNLOADER_SERVICE")
                .setContentTitle("Downloading")
                .setContentText("Downloading")
                .setProgress(0,0,true)
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_audiotrack_light);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("Pixel ROM Log", "onStartCommand");
        NotificationChannel notificationChannel = new NotificationChannel("UPDATE_DOWNLOADER_SERVICE", "Updater Download Service", NotificationManager.IMPORTANCE_MIN);
        notificationChannel.setShowBadge(false);
        if (notificationManager != null)
            notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(3, notification.build());
        startForeground(3, notification.build());
        final File file = new File("/data/tmp/update.zip");
        final Intent statusIntent = new Intent(this, UpdaterActivity.class);
        if(!sharedPref.getBoolean("isDownloaded",false)) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String urlToDownload = (String) intent.getExtras().get("dlLink");
                                Log.d(TAG, "run: "+urlToDownload);
                                URL url = new URL(urlToDownload);
                                URLConnection connection = url.openConnection();
                                connection.connect();
                                int fileLength = connection.getContentLength();
                                InputStream input = new BufferedInputStream(url.openStream());
                                OutputStream output = new FileOutputStream(file);

                                byte data[] = new byte[1024];
                                long total = 0;
                                int count;
                                while ((count = input.read(data)) != -1) {
                                    total += count;

                                    notification
                                            .setProgress(fileLength, (int) total, false)
                                            .setContentText(((total * 100) / fileLength) + "%");
                                    notificationManager.notify(3, notification.build());
                                    output.write(data, 0, count);
                                    Log.d(TAG, "run: "+total+"/"+fileLength);
                                }

                                notificationManager.notify(3, notification.build());
                                output.flush();
                                output.close();
                                input.close();
                                notification
                                        .setProgress(0, 0, false)
                                        .setContentText("System Update download finished. Tap to install.");
                                sharedPref.edit().putBoolean("isDownloaded", true).apply();
                                statusIntent.putExtra("code", UPDATER_SUCCESS_INSTALL);
                                statusIntent.putExtra("download_location", file.getAbsoluteFile().toString());

                                //Intent intent = new Intent(getApplicationContext(), UpdaterActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(
                                        getApplicationContext(),
                                        0,
                                        intent,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                                notification
                                        .setContentIntent(pendingIntent);
                                notificationManager.notify(3, notification.build());

                            } catch (final Exception e) {
                                statusIntent.putExtra("code", UPDATER_ERROR_FAIILED_DOWNLOAD);
                                statusIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                statusIntent.putExtra("download_location", file.getAbsoluteFile());
                                sharedPref.edit().putBoolean("isDownloaded", false).apply();
                                startActivity(statusIntent);
                                e.printStackTrace();
                            }
                        }
                    }
            ).start();
        } else {
            notification
                    .setProgress(0, 0, false)
                    .setContentText("System Update download finished. Tap to install.");
            //sharedPref.edit().putBoolean("isDownloaded", true).apply();
            notificationManager.notify(1, notification.build());
        }

        return START_STICKY;
    }
}
