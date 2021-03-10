package com.tuya.smart.android.demo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
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

