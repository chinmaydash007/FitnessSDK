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
import com.example.fitness.googlefit.WebAppInterface;
import com.getvisitapp.google_fit.GenericListener;

public class MainActivity extends CordovaActivity implements GoogleFitStatusListener, GenericListener {
    GoogleFitUtil googleFitUtil;
    WebAppInterface webAppInterface;
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

        webAppInterface = new WebAppInterface(this);
        webView.addJavascriptInterface(webAppInterface, "Android");

        googleFitUtil = new GoogleFitUtil(this, this);
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
        Log.d("mytag", "onGrandFitPermission() called");
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
