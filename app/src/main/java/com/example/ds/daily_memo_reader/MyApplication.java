package com.example.ds.daily_memo_reader;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;


public class MyApplication extends Application {
    public static final String TAG = MyApplication.class
            .getSimpleName();

    public Tracker mTracker;

    public void startTracking(){
        if(mTracker==null){
            GoogleAnalytics ga = GoogleAnalytics.getInstance(this);
            mTracker = ga.newTracker(R.xml.app_tracker);
            ga.enableAutoActivityReports(this);
        }
    }

    public Tracker getTracker(){
        startTracking();
        return mTracker;
    }
}