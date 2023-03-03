package com.contested.zeroiq.sense.utils;

public class SystemPropertiesProxy {
    private SystemPropertiesProxy() {
    }

    public static String get(String key) throws IllegalArgumentException {
        String str = "";
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            return (String) SystemProperties.getMethod("get", String.class).invoke(SystemProperties, new Object[]{key});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return "";
        }
    }

    public static String get(String key, String def) throws IllegalArgumentException {
        String str = def;
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            return (String) SystemProperties.getMethod("get", String.class, String.class).invoke(SystemProperties, new Object[]{key, def});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return def;
        }
    }

    public static Integer getInt(String key, int def) throws IllegalArgumentException {
        Integer valueOf = Integer.valueOf(def);
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            return (Integer) SystemProperties.getMethod("getInt", String.class, Integer.TYPE).invoke(SystemProperties, new Object[]{key, Integer.valueOf(def)});
        } catch (IllegalArgumentException IAE) {
            throw IAE;
        } catch (Exception e) {
            return Integer.valueOf(def);
        }
    }

    public static Long getLong(String key, long def) throws IllegalArgumentException {
        Long valueOf = Long.valueOf(def);
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            return (Long) SystemProperties.getMethod("getLong", String.class, Long.TYPE).invoke(SystemProperties, new Object[]{key, Long.valueOf(def)});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return Long.valueOf(def);
        }
    }

    public static Boolean getBoolean(String key, boolean def) throws IllegalArgumentException {
        Boolean valueOf = Boolean.valueOf(def);
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            return (Boolean) SystemProperties.getMethod("getBoolean", String.class, Boolean.TYPE).invoke(SystemProperties, new Object[]{key, Boolean.valueOf(def)});
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            return Boolean.valueOf(def);
        }
    }

    public static void set(String key, String val) throws IllegalArgumentException {
        try {
            Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
            SystemProperties.getMethod("set", String.class, String.class).invoke(SystemProperties, key, val);
        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
        }
    }
}
