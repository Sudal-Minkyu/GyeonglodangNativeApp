package com.tuya.smart.android.demo;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.camera.BackgroundCameraPanelActivity;
import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "background AlarmReceiver");
        try {
            intent = new Intent(context, FullscreenActivity.class);
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, ActivityUtils.GetString(context, "deviceId"));
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT);
            pi.send();

        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }


}

