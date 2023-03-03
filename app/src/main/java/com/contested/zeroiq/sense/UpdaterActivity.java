package com.contested.zeroiq.sense;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import com.contested.zeroiq.sense.services.DownloaderService;
import com.contested.zeroiq.sense.services.UpdateCheckWorker;
import com.contested.zeroiq.sense.utils.Devices;
import com.contested.zeroiq.sense.utils.ROMUpdates;
import com.contested.zeroiq.sense.utils.RSSChecker;
import com.contested.zeroiq.sense.utils.SystemPropsSupplier;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.os.Handler;
import android.os.RecoverySystem;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class UpdaterActivity extends AppCompatActivity {

    static int UPDATER_ERROR_NOT_VERIFIED = 1;
    static int UPDATER_ERROR_FAIILED_DOWNLOAD = 2;
    static int UPDATER_SUCCESS_INSTALL = 99;

    private Handler mHandler;

    private View view;
    RSSChecker rssChecker;
    ROMUpdates romUpdates;
    Devices currentDevice = SystemPropsSupplier.DEVICE_DIPPER;

    String dlLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_updater);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }
        actionBar.hide();

        Intent updaterIntent = getIntent();
        int code = updaterIntent.getIntExtra("code",0);
        if(code == 0) {
            Devices[] devices = SystemPropsSupplier.DEVICES;
            currentDevice = SystemPropsSupplier.DEVICE_DIPPER;
            for (int i = 0; i < devices.length; i++) {
                if (devices[i].getCodename().contains(SystemPropsSupplier.DEVICE_CODE)) {
                    currentDevice = devices[i];
                }
            }
            doTheCheck();

            final TextView textView = (TextView) findViewById(R.id.textView2);
            final TextView textView1 = (TextView) findViewById(R.id.textView3);
            final SharedPreferences sharedPref = this.getSharedPreferences("updateChecker", Context.MODE_PRIVATE);
            final Button installTheUpdateBT = (Button) findViewById(R.id.installTheUpdateBT);
            final Button checkTheUpdatesBT = (Button) findViewById(R.id.checkForUpdatesBT);
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
            final LinearLayout buttonLayout = (LinearLayout)findViewById(R.id.buttonLayout);
            final Devices finalCurrentDevice = currentDevice;
            //if (isNetworkConnected()) {
                //updateMessageHandler();
        }

    }

    public void doTheCheck() {

        final TextView textView = (TextView) findViewById(R.id.textView2);
        final TextView textView1 = (TextView) findViewById(R.id.textView3);
        final SharedPreferences sharedPref = this.getSharedPreferences("updateChecker", Context.MODE_PRIVATE);
        final Button installTheUpdateBT = (Button) findViewById(R.id.installTheUpdateBT);
        final Button checkTheUpdatesBT = (Button) findViewById(R.id.checkForUpdatesBT);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final LinearLayout buttonLayout = (LinearLayout)findViewById(R.id.buttonLayout);
        final Devices finalCurrentDevice = currentDevice;

        progressBar.setVisibility(View.VISIBLE);
        textView1.setVisibility(View.GONE);
        buttonLayout.setVisibility(View.GONE);

        WorkRequest updateCheckWorkRequest = new OneTimeWorkRequest.Builder(UpdateCheckWorker.class).build();
        UUID updateCheckWorkRequestId = updateCheckWorkRequest.getId();
        WorkManager workManager = WorkManager.getInstance(UpdaterActivity.this);
        workManager.enqueue(updateCheckWorkRequest);
        workManager.getWorkInfoByIdLiveData(updateCheckWorkRequestId).observeForever(new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                Log.d(TAG, "onChanged: observe" +workInfo.getState().toString());
                String middleTextToShow;
                switch(workInfo.getState()) {
                    case RUNNING:
                        textView.setText("Checking for updates");
                        break;
                    case SUCCEEDED:
                        Data workData = workInfo.getOutputData();
                        boolean isUpToDate = workData.getBoolean("isUpToDate",true);
                        progressBar.setVisibility(View.GONE);
                        textView1.setVisibility(View.VISIBLE);
                        buttonLayout.setVisibility(View.VISIBLE);
                        if (isUpToDate) {
                            sharedPref.edit().putBoolean("isDownloaded",false).apply();
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");
                            String secPatchDate = sdf.format(new Date(SystemPropsSupplier.DEVICE_SECURITY_PATCH));
                            long lastCheck = sharedPref.getLong("lastCheck", 0);
                            String lastCheckInString = DateUtils.getRelativeTimeSpanString(getApplicationContext(), lastCheck, true).toString();
                            textView.setText("Your system is up to date");
                            installTheUpdateBT.setVisibility(View.GONE);
                            checkTheUpdatesBT.setVisibility(View.VISIBLE);
                            checkTheUpdatesBT.setText("Check for update");
                            middleTextToShow = "Android version: " + SystemPropsSupplier.ANDROID_VERSION + "\n" +
                                    "Security patch level: " + secPatchDate + "\n" +
                                    "\n\n" +
                                    "Last successful check for update " + lastCheckInString;
                            textView1.setText(middleTextToShow);
                            checkTheUpdatesBT.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    doTheCheck();
                                }
                            });

                        } else {
                            textView.setText("System update available");
                            if(isMyServiceRunning(DownloaderService.class)) installTheUpdateBT.setEnabled(false);
                            if(sharedPref.getBoolean("isDownloaded",false)) {
                                installTheUpdateBT.setText("Install the update");
                                installTheUpdateBT.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        installTheUpdateBT.setEnabled(false);
                                    try {
                                        RecoverySystem.installPackage(getApplicationContext(),new File("/data/tmp/update.zip"));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                        Log.d(TAG, "onClick: "+new File("/data/tmp/update.zip").canRead());
                                    }
                                });
                                installTheUpdateBT.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        if(!new File("/data/tmp/update.zip").delete()) {
                                            /*Runnable dialog = new Runnable() {
                                                @Override
                                                public void run() {
                                                    new AlertDialog.Builder(getApplicationContext())
                                                        .setTitle("There was an error with this update")
                                                        .setMessage("Could not delete the downloaded file.")
                                                        .setPositiveButton(android.R.string.ok, null)
                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                        .show();
                                                }
                                            };
                                            runOnUiThread(dialog);*/
                                            sharedPref.edit().putBoolean("isDownloaded",false).apply();
                                            Toast.makeText(getApplicationContext(),"Update file removed successfully.",Toast.LENGTH_LONG).show();
                                        } else {
                                            sharedPref.edit().putBoolean("isDownloaded",false).apply();
                                            Toast.makeText(getApplicationContext(),"Update file removed successfully.",Toast.LENGTH_LONG).show();
                                        }
                                        return false;
                                    }
                                });
                            } else {
                                dlLink = workData.getString("dlLink");
                                installTheUpdateBT.setText("Download the update");
                                installTheUpdateBT.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Uri uri = Uri.parse(dlLink);
                                        Intent downloadServiceIntent = new Intent(getApplicationContext(), DownloaderService.class);
                                        downloadServiceIntent.putExtra("dlLink", dlLink);
                                        startForegroundService(downloadServiceIntent);
                                    }
                                });
                            }
                            installTheUpdateBT.setVisibility(View.VISIBLE);
                            checkTheUpdatesBT.setVisibility(View.GONE);
                            middleTextToShow = "This update fixes critical bugs and improves the performance and " +
                                    "stability of your " + finalCurrentDevice.getName() + ". If you download updates over the cellular network" +
                                    " or while roaming, additional charges may apply.";
                            textView1.setText(middleTextToShow);
                        }
                        break;
                    case FAILED:
                        textView.setText("ERROR");
                        progressBar.setVisibility(View.GONE);
                        textView1.setVisibility(View.VISIBLE);
                        buttonLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(),"There was an error during checking updates", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_updater, menu);
        return true;
    }

    public void UpdaterSettingsLauncher (MenuItem view) {
        Intent intentE = new Intent(UpdaterActivity.this, UpdaterSettingsActivity.class);
        startActivity(intentE);
    }

    public static boolean isPrivilegedApp(ApplicationInfo ai) {
        try {
            Method method = ApplicationInfo.class.getDeclaredMethod("isPrivilegedApp");
            return (Boolean)method.invoke(ai);
        } catch(Exception e) {
            Log.d(TAG, "exception", e);
            return false;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        assert cm != null;
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
