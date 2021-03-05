package com.tuya.smart.android.demo;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.tuya.smart.android.demo.base.activity.BackgroundSplashActivity;
import com.tuya.smart.android.demo.camera.BackgroundCameraPanelActivity;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

import java.util.Date;

import androidx.core.app.AlarmManagerCompat;

import static android.content.Context.ALARM_SERVICE;

public class BootReceiver extends BroadcastReceiver {

    private final static String TAG = BootReceiver.class.getSimpleName();


    public boolean IsBootAction(String action) {
        return action == "android.intent.action.BOOT_COMPLETED" ||
                action == "android.intent.action.ACTION_BOOT_COMPLETED" ||
                action == "android.intent.action.QUICKBOOT_POWERON" ||
                action == "com.htc.intent.action.QUICKBOOT_POWERON";
    }

    // BroadcastReceiver를 상속하여 처리 해줍니다.
    @Override
    public void onReceive(final Context context, Intent intent) {
        // 전달 받은 Broadcast의 값을 가져오기
        // androidmanifest.xml에 정의한 인텐트 필터를 받아 올 수 있습니다.
        String action = intent.getAction();
        // 전달된 값이 '부팅완료' 인 경우에만 동작 하도록 조건문을 설정 해줍니다.
        Log.d(TAG, "action = " + action);

        if (IsBootAction(action)) {
            // 부팅 이후 처리해야 코드 작성
            // Ex.서비스 호출, 특정 액티비티 호출등등
            Log.d(TAG, "action = " + action);

            new Handler().postDelayed(new Runnable() {
                // 3초 후에 실행
                @Override
                public void run() {
                    Log.d(TAG, "TuyaHomeSdk.getUserInstance().isLogin()" + TuyaHomeSdk.getUserInstance().isLogin());
                    if (TuyaHomeSdk.getUserInstance().isLogin()) {
                        // BackgroundService
                        Intent serviceLauncher = new Intent(context, BackgroundSplashActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, serviceLauncher, 0);

                        Date t = new Date();
                        t.setTime(java.lang.System.currentTimeMillis() + 1 * 1000);
                        AlarmManagerCompat.setAlarmClock((AlarmManager) context.getSystemService(ALARM_SERVICE), t.getTime(), pendingIntent, pendingIntent);
                    }
                }
            }, 300);
        }
    }

    public static boolean isServiceRunning(Context context, Class serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i(TAG, "ServiceRunning? = " + true);
                return true;
            }
        }
        Log.i(TAG, "ServiceRunning? = " + false);
        return false;
    }
}

