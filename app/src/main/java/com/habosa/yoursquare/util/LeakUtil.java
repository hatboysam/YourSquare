package com.habosa.yoursquare.util;

import android.app.Application;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

public class LeakUtil {

    private static final String TAG = "LeakUtil";
    private static RefWatcher sWatcher;

    public static RefWatcher install(Application application) {
        if (sWatcher == null) {
            sWatcher = LeakCanary.install(application);
        }

        return sWatcher;
    }

    public static RefWatcher getRefWatcher() {
        if (sWatcher != null) {
            return sWatcher;
        } else {
            Log.w(TAG, "Warning: LeakUtil has not been initialized. Returning DISABLED.");
            return RefWatcher.DISABLED;
        }
    }

}
