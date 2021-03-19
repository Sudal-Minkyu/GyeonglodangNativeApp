package com.tuya.smart.android.demo.base.app;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Constant {

    public static  long HOME_ID = 1099001;

    private static final ArrayList<WeakReference<Activity>> activityStack = new ArrayList<>();

    public static final String TAG = "tuya";

    public static void finishActivity() {
        for (WeakReference<Activity> activity : activityStack) {
            if (activity != null && activity.get() != null) activity.get().finish();
        }
        activityStack.clear();
    }

    public static void attachActivity(Activity activity) {
        WeakReference<Activity> act = new WeakReference<>(activity);
        if (!activityStack.contains(act)) activityStack.add(act);
    }
}
