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

import com.getvisitapp.google_fit.StepsCounter;
import com.getvisitapp.google_fit.data.GoogleFitStatusListener;
import com.getvisitapp.google_fit.data.GoogleFitUtil;

import org.apache.cordova.CordovaActivity;


public class MainActivity extends CordovaActivity implements GoogleFitStatusListener {
    String TAG = "mytag";
    GoogleFitUtil googleFitUtil;
    WebView webView;
    ActivityResultLauncher<String> fitnessPermissionResultLauncher;


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

        googleFitUtil = new GoogleFitUtil(this, this,"713515041527-opnka9a94tob87pt74ad565b58lupong.apps.googleusercontent.com","https://web.getvisitapp.xyz/");
        webView.addJavascriptInterface(googleFitUtil.getWebAppInterface(), "Android");
        googleFitUtil.init();


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
        Log.d(TAG, "onFitnessPermissionGranted() called");
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
    public void requestActivityData(String type, String frequency, long timestamp) {
        Log.d(TAG, "requestActivityData() called.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type != null && frequency != null) {
                    googleFitUtil.getActivityData(type, frequency, timestamp);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("mytag", "onActivityResult called. requestCode: " + requestCode + " resultCode: " + resultCode);

        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (requestCode == 4097 || requestCode == 1900) {
                googleFitUtil.onActivityResult(requestCode, resultCode, intent);
            }
        }
    }


    @Override
    public void loadGraphDataUrl(String url) {
        webView.evaluateJavascript(
                url,
                null
        );
    }
}
