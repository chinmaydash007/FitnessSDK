package com.example.fitness;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.getvisitapp.google_fit.GenericListener;
import com.getvisitapp.google_fit.GoogleFitConnector;
import com.getvisitapp.google_fit.StepsCounter;
import com.getvisitapp.google_fit.pojo.ActivitySession;
import com.getvisitapp.google_fit.pojo.HealthDataGraphValues;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity2 extends AppCompatActivity {
    private String TAG = "mytag";

    private StepsCounter stepsCounter;
    private GoogleFitConnector googleFitConnector;
    private Subscriber<HealthDataGraphValues> healthDataGraphValuesSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        healthDataGraphValuesSubscriber = new Subscriber<HealthDataGraphValues>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted: healthDataGraphValuesSubscriber");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(HealthDataGraphValues healthDataGraphValues) {
                Log.d(TAG, "healthDataValues :" + healthDataGraphValues);
                Log.d(TAG, "totalActivityTime: " + healthDataGraphValues.getTotalActivityTimeInMinutes());
                Log.d(TAG, "activityType: " + healthDataGraphValues.getActivityType());
                Log.d(TAG, "values: " + healthDataGraphValues.getValues());



            }
        };

        String default_web_client_id = "713515041527-opnka9a94tob87pt74ad565b58lupong.apps.googleusercontent.com";
        googleFitConnector = new GoogleFitConnector(this, default_web_client_id, new GoogleFitConnector.GoogleConnectorFitListener() {
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

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepsCounter = StepsCounter.getInstance(MainActivity2.this);

                stepsCounter.run(
                        default_web_client_id, new GenericListener() {
                            @Override
                            public void onJobDone(String s) {
                                Log.d(TAG, "Job Done: "+s);

                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(new Date());
                                calendar.setFirstDayOfWeek(2);
                                calendar.set(2021, 10, 20, 10, 0, 0);
                                long startOfDay = calendar.getTimeInMillis();

                                calendar.set(2021, 10, 25, 10, 0, 0);
                                long endOfDay = calendar.getTimeInMillis();


                                googleFitConnector.getWeeklySteps(startOfDay, endOfDay)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(healthDataGraphValuesSubscriber);


                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (googleFitConnector != null) {
            googleFitConnector.onActivityResult(requestCode, resultCode, data);
        }
        if (stepsCounter != null) {
            stepsCounter.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}