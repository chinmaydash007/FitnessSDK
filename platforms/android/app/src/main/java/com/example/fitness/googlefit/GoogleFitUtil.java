package com.example.fitness.googlefit;

import android.app.Activity;
import android.util.Log;

import com.getvisitapp.google_fit.GenericListener;
import com.getvisitapp.google_fit.GoogleFitConnector;
import com.getvisitapp.google_fit.StepsCounter;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GoogleFitUtil {
    String default_web_client_id = "713515041527-opnka9a94tob87pt74ad565b58lupong.apps.googleusercontent.com";
    String TAG = "mytag";


    private Activity context;
    GoogleFitStatusListener listener;
    private Subscriber<SleepStepsData> sleepStepsDataSubscriber;

    public GoogleFitUtil(Activity context, GoogleFitStatusListener listener) {
        this.context = context;
        this.listener = listener;
    }

    private StepsCounter stepsCounter;
    private GoogleFitConnector googleFitConnector;

    public StepsCounter getStepsCounter() {
        return stepsCounter;
    }

    public GoogleFitConnector getGoogleFitConnector() {
        return googleFitConnector;
    }

    public void init() {
        stepsCounter = StepsCounter.getInstance(context);
        googleFitConnector = new GoogleFitConnector(context, default_web_client_id, new GoogleFitConnector.GoogleConnectorFitListener() {
            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete() called");
            }

            @Override
            public void onError() {
                Log.d(TAG, "onError() called");
            }

            @Override
            public void onServerAuthCodeFound(String s) {
                Log.d(TAG, "error Occured: " + s);
            }
        });

        sleepStepsDataSubscriber = new Subscriber<SleepStepsData>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(SleepStepsData sleepStepsData) {
                Log.d("mytag", "steps:" + sleepStepsData.steps + " , sleep=" + sleepStepsData.sleepCard);
                listener.loadWebUrl("https://web.getvisitapp.xyz/home?fitnessPermission=true&steps=" + sleepStepsData.steps + "&sleep=" + TimeUnit.SECONDS.toMinutes(sleepStepsData.sleepCard.getSleepSeconds()));
            }
        };

    }

    public void askForGoogleFitPermission() {
        Log.d("mytag", "askForPermission() called");
        stepsCounter.run(default_web_client_id, new GenericListener() {
            @Override
            public void onJobDone(String s) {
                Log.d(TAG, "onJobDone() called Email Id: " + s);

                listener.onFitnessPermissionGranted();
            }


        });

    }

    public void fetchDataFromFit() {
        Observable.zip(googleFitConnector.getTotalStepsForToday(),
                googleFitConnector.getSleepForToday(),
                (integers, sleepCard) -> {
                    SleepStepsData sleepStepsData;
                    if (!integers.isEmpty()) {
                        sleepStepsData = new SleepStepsData(sleepCard, integers.get(0));
                    } else {
                        sleepStepsData = new SleepStepsData(sleepCard, 0);
                    }
                    return sleepStepsData;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sleepStepsDataSubscriber);
    }


}
