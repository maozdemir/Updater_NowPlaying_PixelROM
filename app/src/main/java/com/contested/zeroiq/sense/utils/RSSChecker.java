package com.contested.zeroiq.sense.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.ParseException;

import static android.content.ContentValues.TAG;

public class RSSChecker {
    public static ROMUpdates checkThePage(Context c) throws Exception {
        String rssToRead = "https://sourceforge.net/projects/pixel3rom/rss?path=/";
        SharedPreferences sharedPref = c.getSharedPreferences("updateChecker", Context.MODE_PRIVATE);
        HandleXML handleXML;
        handleXML = new HandleXML(rssToRead);
        ROMUpdates romUpdates = null;

        long buildDate = 0;
        String dlLink = null;
        handleXML.fetchXML();

        int i = 0;
        while(handleXML.isParsingComplete()) {
            dlLink = handleXML.getLink();
            //Log.d(TAG, i+" - parsed - "+handleXML.getLink());
            if (dlLink.contains(SystemPropsSupplier.DEVICE_CODE)) {
                try {
                    buildDate = DateTimeThinger.SourceforgeEpoch(dlLink);
                    sharedPref.edit().putLong("lastCheck",System.currentTimeMillis()).apply();
                    romUpdates = new ROMUpdates(buildDate,dlLink,true);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return romUpdates;
    }
}
