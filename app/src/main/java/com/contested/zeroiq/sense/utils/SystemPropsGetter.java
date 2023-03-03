package com.contested.zeroiq.sense.utils;

import android.annotation.SuppressLint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressLint("PrivateApi")
public class SystemPropsGetter {


    public static String GetKernelVersion() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class vintfRuntimeInfoInterface = Class.forName("android.os.VintfRuntimeInfo");
        Method hid = vintfRuntimeInfoInterface.getMethod("getOsVersion");
        return (String) hid.invoke(null);
    }
    public static String GetKernelDist() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class vintfRuntimeInfoInterface = Class.forName("android.os.VintfRuntimeInfo");
        Method hid = vintfRuntimeInfoInterface.getMethod("getOsRelease");
        return (String) hid.invoke(null);
    }
    public static String getSystemProperty(String key) {
        String value = null;
        try {
            value = (String) Class.forName("android.os.SystemProperties")
                    .getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
