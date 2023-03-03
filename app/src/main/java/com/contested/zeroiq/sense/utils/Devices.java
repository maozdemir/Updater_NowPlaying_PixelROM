package com.contested.zeroiq.sense.utils;

public class Devices {
    private int id;
    private String codename;
    private String name;

    Devices(int i, String c, String n) {
        i = id;
        codename = c;
        name = n;
    }
    public String getCodename() {
        return codename;
    }
    public String getName() {
        return name;
    }
    public int getId() {
        return id;
    }
}
