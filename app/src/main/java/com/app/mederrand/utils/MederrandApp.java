package com.app.mederrand.utils;

import android.app.Application;

import androidx.multidex.MultiDex;

public class MederrandApp extends Application {
    private static boolean sIsChatActivityOpen = false;

    public static boolean isChatActivityOpen() {
        return sIsChatActivityOpen;
    }

    public static void setChatActivityOpen(boolean isChatActivityOpen) {
        MederrandApp.sIsChatActivityOpen = isChatActivityOpen;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);
    }
}
