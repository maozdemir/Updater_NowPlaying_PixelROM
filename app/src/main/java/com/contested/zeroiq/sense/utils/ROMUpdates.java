package com.contested.zeroiq.sense.utils;

public class ROMUpdates {
    //private Devices device;
    private long buildDate;
    private String downloadLink;
    private boolean isSuccessful;

    public ROMUpdates(/*Devices dev,*/ long b, String d, boolean s) {
        //device = dev;
        isSuccessful = s;
        buildDate = b;
        downloadLink = d;
    }
    //Devices getDevice() { return device;}
    public long getBuildDate() { return buildDate;}
    public String getDownloadLink() { return  downloadLink;}
    public boolean isSuccessful() { return  isSuccessful;}
    public void setSuccessful() { isSuccessful = true;}
}
