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

        Log.d("mytag","connectToGoogleFit() called");

        listener.askForPermissions();

    }
}