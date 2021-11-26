package com.example.fitness.googlefit;

public interface GoogleFitStatusListener {
    void askForPermissions();
    void onFitnessPermissionGranted();
    void loadWebUrl(String urlString);
    void setGraphData(String type, String frequency, int timestamp);
}