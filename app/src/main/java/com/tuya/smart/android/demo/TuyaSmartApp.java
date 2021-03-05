package com.tuya.smart.android.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import com.orm.SugarContext;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.login.activity.LoginActivity;
import com.tuya.smart.android.demo.utils.FrescoManager;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.INeedLoginListener;

import androidx.multidex.MultiDexApplication;


public class TuyaSmartApp extends MultiDexApplication {

    private static final String TAG = "TuyaSmartApp";
    private Intent mBackgroundServiceIntent;
    private BackgroundService mBackgroundService;
    private String deviceId;
    public Vibrator vibrator;

    public void SetDeviceId(String state) {
        this.deviceId = deviceId;
    }

    public String GetDeviceId() {
        return deviceId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        SugarContext.init(getApplicationContext());
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        L.d(TAG, "onCreate " + getProcessName(this));
        L.setSendLogOn(true);
        TuyaHomeSdk.init(this);
        TuyaHomeSdk.setOnNeedLoginListener(new INeedLoginListener() {
            @Override
            public void onNeedLogin(Context context) {
                Intent intent = new Intent(context, LoginActivity.class);
                if (!(context instanceof Activity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(intent);
            }
        });

        FrescoManager.initFresco(this);
        TuyaSdk.setDebugMode(true);

        Foreground.init(this);
    }


    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

    private static Context context;

    public static Context getAppContext() {
        return context;
    }

}
