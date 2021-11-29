package com.example.fitness.googlefit;

import com.getvisitapp.google_fit.pojo.HealthDataGraphValues;

import java.util.ArrayList;

public interface GoogleFitStatusListener {
    void askForPermissions();
    void onFitnessPermissionGranted();
    void loadWebUrl(String urlString);
    void requestActivityData(String type, String frequency, long timestamp);
    void loadGraphDataUrl(String url);
}
