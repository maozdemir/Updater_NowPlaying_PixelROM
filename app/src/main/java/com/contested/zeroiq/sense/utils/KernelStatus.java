package com.contested.zeroiq.sense.utils;

public class KernelStatus {
    private boolean isUpToDate;
    private String updateURL;
    private long updateDate;
    private long currentDate;

    /*public KernelStatus(boolean u, String url, long ud, long cd) {
        isUpToDate = u;
        updateURL = url;
        updateDate = ud;
        currentDate = cd;
    }*/

    public boolean isUpToDate() {
        return isUpToDate;
    }

    public long getCurrentDate() {
        return currentDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public String getUpdateURL() {
        return updateURL;
    }

    public void setCurrentDate(long currentDate) {
        this.currentDate = currentDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public void setUpdateURL(String updateURL) {
        this.updateURL = updateURL;
    }

    public void setUpToDate(boolean upToDate) {
        isUpToDate = upToDate;
    }
}
