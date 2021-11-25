package com.example.fitness.googlefit;

import com.getvisitapp.google_fit.pojo.SleepCard;

public class SleepStepsData {
    public SleepCard sleepCard;
    public int steps;

    public SleepStepsData(SleepCard sleepCard, int steps) {
        this.sleepCard = sleepCard;
        this.steps = steps;
    }
}
