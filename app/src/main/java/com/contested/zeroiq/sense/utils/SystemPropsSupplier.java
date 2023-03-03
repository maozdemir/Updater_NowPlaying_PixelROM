package com.contested.zeroiq.sense.utils;

import java.text.ParseException;

public class SystemPropsSupplier {
    public static String DEVICE_BRAND =
            SystemPropsGetter.getSystemProperty("ro.product.system.manufacturer");
    public static String DEVICE_CODE =
            SystemPropsGetter.getSystemProperty("ro.product.vendor.device");
    public static String DEVICE_MODEL =
            SystemPropsGetter.getSystemProperty("ro.product.system.model");
    public static String DEVICE_BUILD_ID =
            SystemPropsGetter.getSystemProperty("ro.build.id");
    private static String buildDate =
            SystemPropsGetter.getSystemProperty("ro.build.date.utc");
    private static String securityPatch =
            SystemPropsGetter.getSystemProperty("ro.build.version.security_patch");
    public static long DEVICE_SECURITY_PATCH;

    static {
        try {
            DEVICE_SECURITY_PATCH = DateTimeThinger.SecurityPatchEpoch(securityPatch);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static long DEVICE_BUILD_DATE =
            Long.parseLong(buildDate) * 1000;
    public static int ANDROID_VERSION =
            Integer.parseInt(SystemPropsGetter.getSystemProperty("ro.build.version.release"));
    public static Devices DEVICE_BERYLLIUM = new Devices(0,"beryllium", "Pocophone F1");
    public static Devices DEVICE_DIPPER = new Devices(1,"dipper", "Mi 8");
    public static Devices DEVICE_POLARIS = new Devices(2,"polaris", "Mi Mix 2");
    public static Devices[] DEVICES = new Devices[]{
            new Devices(0,"beryllium", "Pocophone F1"),
            new Devices(1,"dipper", "Mi 8"),
            new Devices(2,"polaris", "Mi Mix 2")
    };
}
