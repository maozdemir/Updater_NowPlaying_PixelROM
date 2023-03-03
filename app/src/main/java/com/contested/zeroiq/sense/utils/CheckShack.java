package com.contested.zeroiq.sense.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Scanner;

public class CheckShack extends AsyncTask {
    // you may separate this or combined to caller class.
    public interface AsyncResponse {
        void processFinish(KernelStatus output);
    }

    public AsyncResponse delegate = null;

    public CheckShack(AsyncResponse delegate){
        this.delegate = delegate;
    }

    public static KernelStatus KernelChecker() throws IOException, ParseException {
        KernelStatus kernelStatus = new KernelStatus();
        GitHubClient gitHubClient = new GitHubClient();
        GitHubRequest gitHubRequest = new GitHubRequest();
        gitHubRequest.setUri("/repos/RaphielGang/spins_kernel_xiaomi_sdm845/releases/latest");
        InputStream inputStream = gitHubClient.getStream(gitHubRequest);
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        //String result = TestString.string;
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(result);
        JsonArray jsonArray = jsonObject.getAsJsonArray("assets");
        int jsonArraySize = jsonArray.size();
        String[][] stringArrayForDownloads = new String[jsonArraySize][2];
        for(int i = 0; i < jsonArraySize; i++) {
            JsonObject jsonObjectForDownloads = jsonArray.get(i).getAsJsonObject();
            String browserDownloadUrl = (String) jsonObjectForDownloads.get("browser_download_url")
                    .getAsString();
            String createdAt = (String) jsonObjectForDownloads.get("created_at")
                    .getAsString();
            long epoch = DateTimeThinger.KernelGithubEpoch(createdAt);
            stringArrayForDownloads[i][0] = browserDownloadUrl;
            stringArrayForDownloads[i][1] = String.valueOf(epoch);
        }
        Devices currentDevice = null;
        switch (SystemPropsSupplier.DEVICE_CODE) {
            case "dipper":
                currentDevice = SystemPropsSupplier.DEVICE_DIPPER;
                break;
            case "polaris":
                currentDevice = SystemPropsSupplier.DEVICE_POLARIS;
                break;
            case "beryllium":
                currentDevice = SystemPropsSupplier.DEVICE_BERYLLIUM;
                break;
        }
        String kernelDate = null;
        try {
            kernelDate = SystemPropsGetter.GetKernelVersion();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        long kernelEpoch = DateTimeThinger.KernelEpoch(kernelDate);
        assert currentDevice != null;
        Log.d("contested.KernelCheckDoerSupplier",currentDevice.getCodename());
        int currentDeviceID = currentDevice.getId();
        String updateURL = stringArrayForDownloads[currentDeviceID][0];
        long updateTime = Long.parseLong(stringArrayForDownloads[currentDeviceID][1]);
        kernelStatus.setCurrentDate(kernelEpoch);
        kernelStatus.setUpdateDate(updateTime);
        kernelStatus.setUpdateURL(updateURL);
        if(updateTime > kernelEpoch) {
            Log.d("contested.updateTime",updateTime+"");
            Log.d("contested.KernelComparator", "NOT UP TO DATE!");
            Log.d("contested.updateTime",kernelEpoch+"");
            kernelStatus.setUpToDate(false);
        } else {
            Log.d("contested.updateTime",kernelEpoch+"");
            Log.d("contested.updateTime",updateTime+"");
            Log.d("contested.KernelComparator", "UP TO DATE!");
            kernelStatus.setUpToDate(true);
        }
        return kernelStatus;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            return KernelChecker();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        KernelStatus kernelStatus = (KernelStatus) o;
        delegate.processFinish(kernelStatus);
        Log.d("contested.OnPostExecute","ON FIRE!");
    }
}
