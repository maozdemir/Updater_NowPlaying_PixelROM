package com.contested.zeroiq.sense.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSResolverUtil implements Runnable {
    private String domain;
    private InetAddress inetAddr;

    public DNSResolverUtil(String domain) {
        this.domain = domain;
    }

    public void run() {
        try {
            InetAddress addr = InetAddress.getByName(domain);
            set(addr);
        } catch (UnknownHostException e) {

        }
    }

    public synchronized void set(InetAddress inetAddr) {
        this.inetAddr = inetAddr;
    }
    public synchronized InetAddress get() {
        return inetAddr;
    }
}