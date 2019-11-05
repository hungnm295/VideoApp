package com.example.videoapp.model.sql;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

public class App extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        context = this;
    }
    public static Context getContext(){
        return context;
    }
}
