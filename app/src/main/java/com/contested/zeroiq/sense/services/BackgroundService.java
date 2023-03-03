    package com.contested.zeroiq.sense.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.acrcloud.rec.ACRCloudClient;
import com.acrcloud.rec.ACRCloudConfig;
import com.acrcloud.rec.ACRCloudResult;
import com.acrcloud.rec.IACRCloudListener;
import com.acrcloud.rec.utils.ACRCloudLogger;

import com.contested.zeroiq.sense.R;
import com.contested.zeroiq.sense.utils.DBConstructor;
import com.contested.zeroiq.sense.utils.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.text.TextUtils.isEmpty;


public class BackgroundService extends Service implements  IACRCloudListener {
    private ACRCloudConfig mConfig = null;
    public String  TAG = "pixelromnowplaying";
    private ACRCloudClient mClient = null;
    public String curr = "a";
    Notification.Builder notification = null;
    ScheduledExecutorService scheduledExecutorService;
    private long looperTimer = 60;
    private long lastRecognize = 0;


    public int onStartCommand(Intent intent, int flags, int startId) {
        notification = new Notification.Builder(getBaseContext(), "BACKGROUND_SERVICE")
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_audiotrack_light);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("Pixel ROM Log", "onStartCommand");
        NotificationChannel notificationChannel = new NotificationChannel("BACKGROUND_SERVICE", "Foreground Service", NotificationManager.IMPORTANCE_MIN);
        notificationChannel.setShowBadge(false);
        if (notificationManager != null)
            notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1, notification.build());
        startForeground(1, notification.build());
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                startListening();
                Log.i("Pixel ROM Log", "Looper "+looperTimer);
            }
        }, 0, looperTimer, TimeUnit.SECONDS);

        looperTimer = 60;
        return Service.START_STICKY;
    }

    public void onCreate() {
        super.onCreate();
        Log.d("Pixel ROM Log", "onCreate");
    }

    private void startListening() {
        long minus = (lastRecognize - System.currentTimeMillis());
        if((lastRecognize - System.currentTimeMillis()) < looperTimer) {
            this.mConfig = new ACRCloudConfig();
            this.mConfig.acrcloudListener = this;
            this.mConfig.context = this;
            Log.d("Pixel ROM Log", "startListening");
            String[] accessKeyPool = {""};
            String[] accessSecPool = {""};
            Random rand = new Random();
            int randPick = rand.nextInt(3);
            this.mConfig.accessKey = accessKeyPool[randPick];
            this.mConfig.accessSecret = accessSecPool[randPick];
            this.mConfig.host = "identify-global.acrcloud.com";
            this.mConfig.hostAuto = "";
            this.mConfig.accessKeyAuto = "";
            this.mConfig.accessSecretAuto = "";
            this.mConfig.recorderType = ACRCloudConfig.RecorderType.DEFAULT;
            this.mConfig.recorderConfig.rate = 8000;
            this.mConfig.recorderConfig.channels = 1;
            this.mConfig.recorderConfig.periods = 150000;
            this.mConfig.recorderConfig.reservedRecordBufferMS = 0;
            this.mConfig.createFingerprintMode = ACRCloudConfig.CreateFingerprintMode.DEFAULT;
            this.mConfig.recorderConfig.isVolumeCallback = false;
            this.mConfig.recorderConfig.initMaxRetryNum = 1;
            this.mConfig.autoRecognizeIntervalMS = 150000;
            this.mConfig.protocol = ACRCloudConfig.NetworkProtocol.HTTPS;
            this.mClient = new ACRCloudClient();
            ACRCloudLogger.setLog(false);
            mClient.initWithConfig(this.mConfig);
            Log.d("Pixel ROM Log", "startRecon");
            final ACRCloudClient finalMClient = this.mClient;
            finalMClient.startRecognize();
        } else {
            looperTimer = looperTimer - lastRecognize + 1;
        }
        lastRecognize = minus;
    }

    public void onDestroy() {
        super.onDestroy();
        this.mClient.release();
        Log.d("Pixel ROM Log", "Background Service was destroyed.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onResult(ACRCloudResult results) {
        String result = results.getResult();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName() + "_preferences", 0);
        NotificationChannel notificationChannel = new NotificationChannel("DETECTED_SONG", "Song notificatinator", NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setShowBadge(false);
        if(notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        } else {
            Log.e(TAG, "onResult: notificationManager returned null.");
        }

        String tres = "\n";
        String artist = null;
        String track = null;
        String acrid = null;
        int currentTime;
        int fullTime;
        int calculatedTimeMS;
        Log.d("Pixel ROM Log", "released");

        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if(j2 == 0){
                JSONObject metadata = j.getJSONObject("metadata");
                //
                if (metadata.has("music")) {
                    JSONArray musics = metadata.getJSONArray("music");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        track = tt.getString("title");
                        acrid = tt.getString("acrid");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        artist = art.getString("name");
                        tres = tres + (i+1) + ".  Title: " + track + "    Artist: " + artist + "\n";
                        currentTime = tt.getInt("play_offset_ms");
                        fullTime = tt.getInt("duration_ms");
                        calculatedTimeMS = fullTime-currentTime;
                        looperTimer = (TimeUnit.MILLISECONDS.toSeconds(calculatedTimeMS)/2) + 5;
                    }
                }

                tres = tres + "\n\n" + result;
            }else{
                looperTimer = 60;
                tres = result;
            }
        } catch (JSONException e) {
            tres = result;
            e.printStackTrace();
        }
        Log.d("RRR",tres);
        if (!isEmpty(artist) && !isEmpty((track))) {
            final int cooldown = 150000;
            final String cast = unwrapString(track + " by " + artist);
            if (sharedPreferences.getBoolean("ambient_keyguard", true)) {
                Context  applicationContext = getApplicationContext();

                String pName = "com.android.systemui";
                PendingIntent pendingIntent ;

                Intent intentGoogleSearch = new Intent("com.google.android.googlequicksearchbox.MUSIC_SEARCH")
                    .putExtra("android.soundsearch.extra.RECOGNIZED_ARTIST",artist)
                    .putExtra("android.soundsearch.extra.RECOGNIZED_TITLE",track);

                pendingIntent = PendingIntent.getActivity(this, 1, intentGoogleSearch, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent intent = new Intent("com.google.android.ambientindication.action.AMBIENT_INDICATION_SHOW")
                        .putExtra("com.google.android.ambientindication.extra.VERSION", 1)
                        .putExtra("com.google.android.ambientindication.extra.TEXT", cast)
                        .putExtra("com.google.android.ambientindication.extra.OPEN_INTENT",pendingIntent)
                        .putExtra("com.google.android.ambientindication.extra.TTL_MILLIS", cooldown);
                intent.setPackage(pName);

                applicationContext.sendBroadcast(intent, "com.google.android.ambientindication.permission.AMBIENT_INDICATION");
            }
            if (sharedPreferences.getBoolean("ambient_notification", true)) {
                Notification.Builder notif = new Notification.Builder(getBaseContext(), "DETECTED_SONG").setSmallIcon(R.drawable.ic_audiotrack_light).setContentTitle(track).setContentText(artist).setPriority(Notification.PRIORITY_MIN);
                notificationManager.notify(2,notif.build());
                //notificationManager.notify(1,notification.setContentTitle(cast).build());
            }
            curr = cast;

            // TODO: Proper database implementation.
            DBHelper dbHelper = new DBHelper(getApplicationContext());
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String sortOrder = DBConstructor.FeedEntry.COLUMN_NAME_ARTIST + " DESC";

            Cursor cursor = db.rawQuery("SELECT * FROM "+DBConstructor.FeedEntry.TABLE_NAME+" ORDER BY "+DBConstructor.FeedEntry.COLUMN_NAME_DATE+" DESC LIMIT 1;", null);
            List itemIds = new ArrayList<>();
            String lastArtist = null;
            String lastTrack = null;
            String lastacrid = null;
            if(cursor.moveToFirst()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(DBConstructor.FeedEntry._ID));
                lastacrid = cursor.getString(3);
                Log.d("lastacr", "aa"+cursor.getString(3)+"aa");
                itemIds.add(itemId);
                Log.d("curacr", "aa" + acrid+"aa");
            }
            cursor.close();

            if(isEmpty(lastacrid)) lastacrid = "asd";
            if(!lastacrid.equals(acrid) || isEmpty(lastacrid)) {
                ContentValues values = new ContentValues();
                values.put(DBConstructor.FeedEntry.COLUMN_NAME_ARTIST, artist);
                values.put(DBConstructor.FeedEntry.COLUMN_NAME_TRACK, track);
                values.put(DBConstructor.FeedEntry.COLUMN_NAME_DATE, System.currentTimeMillis());
                values.put(DBConstructor.FeedEntry.COLUMN_NAME_ACRID, acrid);
                Log.d("h","h");

                db.insert(DBConstructor.FeedEntry.TABLE_NAME, null, values);
                Log.d("EntryStatus", "Entry was added.");
            }
            else {
                Log.d("EntryStatus", "Entry was NOT added.");
            }
            db.close();
        }
    }

    @Override
    public void onVolumeChanged(double v) {
        Log.d("Pixel ROM Log", "Volume Changed.");

    }

    private static String unwrapString(String string) {
        return string.replace("%A", "\"").replace("%B", "'").replace("%C", "(").replace("%D", ")").replace("%E", "…");
    }


}
