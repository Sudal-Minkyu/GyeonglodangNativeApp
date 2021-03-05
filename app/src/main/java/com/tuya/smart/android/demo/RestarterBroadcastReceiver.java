package com.tuya.smart.android.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;



public class RestarterBroadcastReceiver extends BroadcastReceiver {

    private final static String TAG = RestarterBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "RestarterBroadcastReceiver.onReceive");
            // BackgroundService
            Intent serviceLauncher = new Intent(context, BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceLauncher);
            } else {
                context.startService(serviceLauncher);
            }
    }
}