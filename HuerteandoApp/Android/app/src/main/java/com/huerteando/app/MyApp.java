package com.huerteando.app;

import android.app.Application;

import com.huerteando.app.utils.SessionManager;

public class MyApp extends Application {
    private static MyApp instance;
    private static SessionManager session;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        session = new SessionManager(getApplicationContext());
    }

    public static MyApp getInstance() {
        return instance;
    }

    public static SessionManager getSession() {
        return session;
    }
}