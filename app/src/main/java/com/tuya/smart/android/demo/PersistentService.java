package com.tuya.smart.android.demo;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class PersistentService extends Service {
    private String TAG = "PersistentService";

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        unregisterRestartAlram(); //이미 등록된 알람이 있으면 제거

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        registerRestartAlram(); // 서비스가 죽을때 알람을 등록

        super.onDestroy();
    }

    // support persistent of Service
    public void registerRestartAlram() {
        Log.d(TAG, "registerRestartAlarm");

        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction(RestartService. ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 10*1000; // 10초 후에 알람이벤트 발생
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 10*1000, sender);
    }

    public void unregisterRestartAlram() {
        Log.d(TAG, "unregisterRestartAlarm");
        Intent intent = new Intent(PersistentService.this, RestartService.class);
        intent.setAction(RestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(PersistentService.this, 0, intent, 0);

        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

    /*
     * 액티비티와 통신하기 위한 부분 START
     * ********************************************************
     */
    public class LocalBinder extends Binder {
        PersistentService getService() {
            return PersistentService.this;
        }
    }

}
