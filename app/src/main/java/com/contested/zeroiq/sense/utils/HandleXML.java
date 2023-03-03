package com.contested.zeroiq.sense.utils;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static android.content.ContentValues.TAG;

public class HandleXML {
    private String title = "title";
    private String link = "link";
    private String description = "description";
    private String urlString = null;
    private XmlPullParserFactory xmlFactoryObject;
    private volatile boolean parsingComplete = true;
    private volatile boolean noErrors = true;
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public HandleXML(String url){
        this.urlString = url;
    }
    public String getTitle(){
        return title;
    }
    public String getLink(){
        return link;
    }
    public String getDescription(){
        return description;
    }
    public void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;
        String text=null;
        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        switch (name) {
                            case "title":
                                title = text;
                                break;
                            case "link":
                                link = text;
                                break;
                            case "description":
                                description = text;
                                break;
                            default:
                                break;
                        }
                        break;
                }
                event = myParser.next();
            }
            setParsingComplete(false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchXML() throws Exception {
        Thread thread = new Thread();
        Runnable fetchXMLRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    //InetAddress.getByName(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(3000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    Log.d(TAG, "run: timeouts: " + conn.getConnectTimeout() + conn.getReadTimeout());

                    // Starts the query
                    conn.connect();
                    Log.d(TAG, "run:responsecode" + conn.getResponseCode());
                    InputStream stream = conn.getInputStream();

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);
                    noErrors = true;

                    parseXMLAndStoreIt(myparser);
                    stream.close();
                } catch (Exception e)
                {
                    noErrors = false;
                    e.printStackTrace();
                }
            }
        };
        Thread fetchXMLThread = new Thread(fetchXMLRunnable);
        fetchXMLThread.start();
        //executor.execute(fetchXMLThread);
    }

    public boolean isParsingComplete() {
        return parsingComplete;
    }

    public void setParsingComplete(boolean parsingComplete) {
        this.parsingComplete = parsingComplete;
    }
}