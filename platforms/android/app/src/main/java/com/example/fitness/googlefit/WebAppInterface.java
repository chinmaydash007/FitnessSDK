package com.example.fitness.googlefit;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class WebAppInterface {
    GoogleFitStatusListener listener;

    public WebAppInterface(GoogleFitStatusListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void connectToGoogleFit() {

        Log.d("mytag", "connectToGoogleFit() called");

        listener.askForPermissions();

    }

    @JavascriptInterface
    public void getDataToGenerateGraph(String type, String frequency, long timestamp) {
        Log.d("mytag", "getDataToGenerateGraph() called. type:" + type + " frequency: " + frequency + " timestamp:" + timestamp);
        listener.requestActivityData(type, frequency, timestamp);
    }
}