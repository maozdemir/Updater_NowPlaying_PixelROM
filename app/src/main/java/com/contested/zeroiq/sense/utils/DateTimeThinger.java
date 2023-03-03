package com.contested.zeroiq.sense.utils;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;

import java.text.ParseException;
import java.util.Date;

public class DateTimeThinger {
    private static Calendar KernelCalendar(String dateTimeInfoInFormat) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(KernelEpoch(dateTimeInfoInFormat));
        return calendar;
    }
    private static Calendar CustCalendar(long dateTimeInfoInFormat) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(dateTimeInfoInFormat);
        return calendar;
    }
    public static String KernelDateTimeThingyStringer(String dateTimeInfoInFormat) throws ParseException {
        Calendar calendar = KernelCalendar(dateTimeInfoInFormat);
        String k_dayOfInstalled = ((calendar.get(Calendar.DATE) < 10) ? "0"+calendar.get(Calendar.DATE) : calendar.get(Calendar.DATE)+"");
        String k_monthOfInstalled = (((calendar.get(Calendar.MONTH)+1) < 10) ? "0"+(calendar.get(Calendar.MONTH)+1) : (calendar.get(Calendar.MONTH)+1)+"");
        String kernelUpdate = k_dayOfInstalled+"-"+k_monthOfInstalled+"-"+calendar.get(Calendar.YEAR);
        return kernelUpdate;
    }
    public static String DateTimeThingyStringer(long dateTimeInfoInFormat) throws ParseException {
        Calendar calendar = CustCalendar(dateTimeInfoInFormat);
        String k_dayOfInstalled = ((calendar.get(Calendar.DATE) < 10) ? "0"+calendar.get(Calendar.DATE) : calendar.get(Calendar.DATE)+"");
        String k_monthOfInstalled = (((calendar.get(Calendar.MONTH)+1) < 10) ? "0"+(calendar.get(Calendar.MONTH)+1) : (calendar.get(Calendar.MONTH)+1)+"");
        String kernelUpdate = k_dayOfInstalled+"-"+k_monthOfInstalled+"-"+calendar.get(Calendar.YEAR);
        return kernelUpdate;
    }
    public static long KernelEpoch (String dateTimeInfoInFormat) throws ParseException {
        dateTimeInfoInFormat = dateTimeInfoInFormat.split("PREEMPT ")[1];
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        Date date = df.parse(dateTimeInfoInFormat);
        long epoch = date.getTime();
        return epoch;
    }
    public static long KernelGithubEpoch(String dateTimeInfoInFormat) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = df.parse(dateTimeInfoInFormat);
        long epoch = date.getTime();
        return epoch;
    }
    public static long SecurityPatchEpoch(String dateTimeInfoInFormat) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df.parse(dateTimeInfoInFormat);
        long epoch = date.getTime();
        return epoch;
    }
    public static long SourceforgeEpoch (String url) throws ParseException {
        url = url.split("REL.")[1];
        url = url.split(".zip")[0];
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd"); //20200912
        Date date = df.parse(url);
        long epoch = date.getTime();
        return epoch;
    }
}
