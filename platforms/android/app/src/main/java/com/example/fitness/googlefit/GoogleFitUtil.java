package com.example.fitness.googlefit;

import android.app.Activity;
import android.util.Log;

import com.getvisitapp.google_fit.GenericListener;
import com.getvisitapp.google_fit.GoogleFitConnector;
import com.getvisitapp.google_fit.StepsCounter;
import com.getvisitapp.google_fit.pojo.HealthDataGraphValues;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private Subscriber<HealthDataGraphValues> healthDataGraphValuesSubscriber;

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

    public void getActivityData(String type, String frequency, long timeStamp) {

        SimpleDateFormat readableFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

        addHealthDataSubscriber(type, frequency);


        Calendar cal = Calendar.getInstance();


        cal.setTimeInMillis(timeStamp);


        cal.setFirstDayOfWeek(Calendar.MONDAY);


        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();


        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long endOfDay = cal.getTimeInMillis();


        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        long startOfWeek = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        long endOfWeek = cal.getTimeInMillis();


        // get start of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        long startOfMonth = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, 1);
        long endOfMonth = cal.getTimeInMillis();

//        Log.d("mytag", "getDataFromGoogleFitStore: start of week: " + readableFormat.format(startOfWeek));
//        Log.d("mytag", "getDataFromGoogleFitStore: end of week: " + readableFormat.format(endOfWeek));

        if (type != null && frequency != null) {

            if (type.equals("steps")) {
                switch (frequency) {
                    case "day": {
                        googleFitConnector.getDailySteps(startOfDay, endOfDay)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                    case "week": {
                        googleFitConnector.getWeeklySteps(startOfWeek, endOfWeek)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                    case "month": {
                        googleFitConnector.getWeeklySteps(startOfMonth, endOfMonth)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                }
            } else if (type.equals("distance")) {
                switch (frequency) {
                    case "day": {
                        googleFitConnector.getDailyDistance(startOfDay, endOfDay)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                    case "week": {
                        googleFitConnector.getWeeklyDistance(startOfWeek, endOfWeek)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                    case "month": {
                        googleFitConnector.getWeeklyDistance(startOfMonth, endOfMonth)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }

                }
            } else if (type.equals("calories")) {
                switch (frequency) {
                    case "day": {
                        googleFitConnector.getDailyCalories(startOfDay, endOfDay)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                    case "week": {
                        googleFitConnector.getWeeklyCalories(startOfWeek, endOfWeek)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                    case "month": {
                        googleFitConnector.getWeeklyCalories(startOfMonth, endOfMonth)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }

                }
            } else if (type.equals("sleep")) {
                switch (frequency) {
                    case "day": {
                        googleFitConnector.getSleepForTheDay(startOfDay)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                    case "week": {
                        googleFitConnector.getSleepForWeek(startOfWeek, 7)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(healthDataGraphValuesSubscriber);
                        break;
                    }
                }
            }
        }


    }

    private void addHealthDataSubscriber(String type, String frequency) {
        if (healthDataGraphValuesSubscriber != null && !healthDataGraphValuesSubscriber.isUnsubscribed()) {
            healthDataGraphValuesSubscriber.unsubscribe();
        }
        healthDataGraphValuesSubscriber = new Subscriber<HealthDataGraphValues>() {
            @Override
            public void onCompleted() {
                Log.d("mytag", "onCompleted: healthDataGraphValuesSubscriber");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(HealthDataGraphValues graphValues) {

                listener.updateGraph(type, frequency, graphValues.getValues(), graphValues.getTotalActivityTimeInMinutes(), graphValues);
            }
        };
    }


}
