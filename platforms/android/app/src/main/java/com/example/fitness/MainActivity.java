/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.example.fitness;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import org.apache.cordova.*;

import com.example.fitness.googlefit.GoogleFitStatusListener;
import com.example.fitness.googlefit.GoogleFitUtil;
import com.example.fitness.googlefit.GraphValueUtil;
import com.example.fitness.googlefit.WebAppInterface;
import com.getvisitapp.google_fit.GenericListener;
import com.getvisitapp.google_fit.pojo.HealthDataGraphValues;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends CordovaActivity implements GoogleFitStatusListener, GenericListener {
    GoogleFitUtil googleFitUtil;
    WebAppInterface webAppInterface;
    WebView webView;
    ActivityResultLauncher<String> fitnessPermissionResultLauncher;
    GraphValueUtil graphValueUtil;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // enable Cordova apps to be started in the background
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }

        // Set by <content src="index.html" /> in config.xml
        loadUrl(launchUrl);

        webView = (WebView) appView.getEngine().getView();
        webView.getSettings().setJavaScriptEnabled(true);

        webAppInterface = new WebAppInterface(this);
        webView.addJavascriptInterface(webAppInterface, "Android");

        googleFitUtil = new GoogleFitUtil(this, this);
        googleFitUtil.init();

        graphValueUtil = new GraphValueUtil(googleFitUtil.getGoogleFitConnector(), this);


        if (googleFitUtil.getStepsCounter().hasAccess()) {
            googleFitUtil.fetchDataFromFit();
        }

        fitnessPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean granted) {
                if (granted) {
                    googleFitUtil.askForGoogleFitPermission();
                }
            }
        });
    }

    @Override
    public void askForPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fitnessPermissionResultLauncher.launch(
                    android.Manifest.permission.ACTIVITY_RECOGNITION);
        } else {
            googleFitUtil.askForGoogleFitPermission();
        }
    }

    @Override
    public void onFitnessPermissionGranted() {
        Log.d("mytag", "onFitnessPermissionGranted() called");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                googleFitUtil.fetchDataFromFit();
            }
        });

    }

    @Override
    public void loadWebUrl(String urlString) {
        loadUrl(urlString);
    }

    @Override
    public void loadActivityData(String type, String frequency, long timestamp) {
        Log.d("mytag", "loadActivityData() called.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type != null && frequency != null) {
                    graphValueUtil.getActivityData(type, frequency, timestamp);
                }
            }
        });

    }

    @Override
    public void updateGraph(String type, String frequency, ArrayList<Integer> values, int averageTime, HealthDataGraphValues graphValues) {
        if (type != null && frequency != null) {
            if (type.equals("sleep")) {
                switch (frequency) {
                    case "day": {
                        String sleepDuration = graphValues.getSleepCard().getFormattedSleep();

                        String value = "window.updateSleepForAndroid('" + sleepDuration + "','" + graphValues.getSleepCard().getStartSleepTimeFormatted(graphValues.getSleepCard().getStartSleepTime()) + "','" + graphValues.getSleepCard().getStartSleepTimeFormatted(graphValues.getSleepCard().getEndSleepTime()) + "')";

                        Log.d(TAG, "run: getSleep minutes daily: " + value);
                        webView.evaluateJavascript(
                                value,
                                null
                        );
                        break;
                    }
                    case "week": {
                        String value = "DetailedGraph.updateSleepData(JSON.stringify(" + graphValues.getSleepDataForWeeklyGraphInJson() + "));"
                                + "$('.sleep-duration').text('" + graphValues.getAverageSleep() + "')";
                        Log.d(TAG, "run: getSleep minutes daily: " + value);
                        webView.evaluateJavascript(
                                value,
                                null
                        );
                        break;
                    }
                }
            } else {
                switch (frequency) {
                    case "day": {
                        String value = "DetailedGraph.updateData([1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24]," + values + ", '" + type + "', 'day','" + averageTime + "')";
                        LOG.d("mytag", "valueString: " + value);
                        webView.evaluateJavascript(
                                value,
                                null
                        );
                        break;
                    }
                    case "week": {
                        String value = "DetailedGraph.updateData([1,2,3,4,5,6,7]," + values + ",'" + type + "', 'week','" + averageTime + "')";
                        LOG.d("mytag", "valueString: " + value);
                        webView.evaluateJavascript(
                                value,
                                null
                        );
                        break;
                    }
                    case "month": {
                        String value = "DetailedGraph.updateData([1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31]," + values + ",'" + type + "', 'month','" + averageTime + "')";
                        LOG.d("mytag", "valueString: " + value);
                        webView.evaluateJavascript(
                                value,
                                null
                        );
                        break;
                    }
                }
                Log.d("mytag", "updateGraph() called. " + type + " frequency: " + frequency + " values:" + values);

            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (googleFitUtil.getGoogleFitConnector() != null) {
            googleFitUtil.getGoogleFitConnector().onActivityResult(requestCode, resultCode, intent);
        }

        if (googleFitUtil.getStepsCounter() != null) {
            googleFitUtil.getStepsCounter().onActivityResult(requestCode, resultCode, intent, this);
        }


        super.onActivityResult(requestCode, resultCode, intent);

    }

    @Override
    public void onJobDone(String s) {
        Log.d("mytag", "onJobDone() called email: " + s);
        onFitnessPermissionGranted();
    }
}
