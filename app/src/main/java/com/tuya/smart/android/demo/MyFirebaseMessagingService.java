package com.tuya.smart.android.demo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.AlarmManagerCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tuya.smart.android.demo.camera.bean.AlarmMessage;
import com.tuya.smart.android.demo.personal.IPersonalInfoView;
import com.tuya.smart.android.demo.personal.PersonalInfoModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final static String TAG = "FCM_MESSAGE";

//    private boolean isLock;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        Log.d(TAG, "message size: " + remoteMessage.getData());

        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        boolean isBackground = Foreground.isBackground();

        if (remoteMessage.getData().size() > 0) {
            sendNotification(remoteMessage);
        }
//        else{
//            WakeUpMoveFullScreen("여기가 진짜야");
//        }

    }

    NotificationChannel mNotificationChannel;
    private void DisableNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mNotificationChannel != null) {
                mNotificationChannel.setVibrationPattern(new long[]{0});
                mNotificationChannel.enableVibration(true);
            }
        }
    }

    public void WakeUpMoveFullScreen(String alarm) {
        Log.e(TAG, "KMK WakeUpMoveFullScreen");
        Log.e(TAG, "KMK WakeUpMoveFullScreen 알람 : "+alarm);
        Log.e(TAG, "KMK WakeUpMoveFullScreen ALARM_SERVICE : "+ALARM_SERVICE);
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        Date t = new Date();
        t.setTime(java.lang.System.currentTimeMillis() + 1000);
        if(manager!=null){
            Log.e(TAG, "KMK manager != null!");
            AlarmManagerCompat.setAlarmClock(manager, t.getTime(), pendingIntent, pendingIntent);
        }else{
            Log.e(TAG, "KMK manager = null!");
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        Log.e(TAG, "KMK title : " + title);
        Log.e(TAG, "KMK message : " + message);

        if (remoteMessage.getNotification() != null) {
            String title1 = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd",java.util.Locale.getDefault());
            Date date = new Date(System.currentTimeMillis());
            AlarmMessage alarmMessage = new AlarmMessage(title1, body, simpleDateFormat.format(date));
            alarmMessage.save();
        }

//        Context context = getApplicationContext();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final String CHANNEL_ID = "ChannerID";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_NAME = "ChannerName";
            final String CHANNEL_DESCRIPTION = "ChannerDescription";
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            // add in API level 26
            mNotificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mNotificationChannel.setDescription(CHANNEL_DESCRIPTION);
            mNotificationChannel.enableLights(true);

            mNotificationChannel.enableVibration(true);
            mNotificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            mNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(mNotificationChannel);
        }

//        if (!isLock && isBackground) {
//            isLock = true;
//        if (isBackground) {
//            WakeUpMoveFullScreen();
//
//            HandlerThread handlerThread = new HandlerThread("HandlerThreadName");
//            handlerThread.start();
//            Handler mHandler = new Handler(handlerThread.getLooper());
//            mHandler.postDelayed(new Runnable() {
//                public void run() {
//                    DisableNotification();
//                    isLock = false;
//                }
//            }, 1000);
//        }
//        else {
            Intent notifyIntent = new Intent(getApplicationContext(), MainActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Set the Activity to start in a new, empty task
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Create the PendingIntent
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                    this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            if (remoteMessage.getNotification() != null) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logoimg)) //BitMap 이미지 요구
                        .setSmallIcon(R.drawable.logoimg) //필수 (안해주면 에러)
                        .setContentTitle(remoteMessage.getNotification().getTitle()) //타이틀 TEXT
                        .setContentText(remoteMessage.getNotification().getBody()) //서브 타이틀 TEXT
                        .setContentIntent(notifyPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setWhen(System.currentTimeMillis());

                Notification notification = builder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
                startForeground(0, notification);

                notificationManager.notify(0, notification);
                // 3초 후에 실행
                new Handler(Looper.getMainLooper()).postDelayed(this::DisableNotification, 5000);

            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logoimg)) //BitMap 이미지 요구
                        .setSmallIcon(R.drawable.logoimg) //필수 (안해주면 에러)
                        .setContentTitle(title) //타이틀 TEXT
                        .setContentText(message) //서브 타이틀 TEXT
                        .setContentIntent(notifyPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setWhen(System.currentTimeMillis());

                Notification notification = builder.build();
                notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
                startForeground(0, notification);

                notificationManager.notify(0, notification);
                // 3초 후에 실행
                new Handler(Looper.getMainLooper()).postDelayed(this::DisableNotification, 5000);
            }
//        }

    }

    @Override
    public void onNewToken(@NonNull String s) {
        Log.e(TAG, "KMK FCM 토큰새로고침 Token : " + s);
        super.onNewToken(s);
    }
}
