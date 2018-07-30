package com.python.cat.testgradle;

import android.app.Application;

import com.apkfuns.logutils.LogUtils;
import com.facebook.stetho.Stetho;

public class App extends Application {

    static {
        LogUtils.getLogConfig().configShowBorders(false);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
