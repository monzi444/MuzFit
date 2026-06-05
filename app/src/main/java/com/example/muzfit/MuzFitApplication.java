package com.example.muzfit;

import android.app.Application;

import com.example.muzfit.utils.ThemeHelper;

public class MuzFitApplication extends Application {

    @Override
    public void onCreate() {
        ThemeHelper.applySavedTheme(this);
        super.onCreate();
    }
}
