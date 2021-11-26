package com.example.fitness.googlefit;

import com.getvisitapp.google_fit.pojo.HealthDataGraphValues;

import java.util.ArrayList;

public interface GoogleFitStatusListener {
    void askForPermissions();
    void onFitnessPermissionGranted();
    void loadWebUrl(String urlString);
    void loadActivityData(String type, String frequency, long timestamp);
    void updateGraph(String type, String frequency, ArrayList<Integer> values, int averageTime, HealthDataGraphValues graphValues);
}
