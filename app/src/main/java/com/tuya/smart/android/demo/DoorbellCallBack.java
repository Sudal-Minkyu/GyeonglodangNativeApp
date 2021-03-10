package com.tuya.smart.android.demo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.AlarmManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.tuya.smart.android.camera.api.bean.CameraPushDataBean;
import com.tuya.smart.android.demo.base.activity.BaseActivity;
import com.tuya.smart.android.demo.base.utils.LoginHelper;
import com.tuya.smart.sdk.api.ITuyaGetBeanCallback;

import org.json.JSONObject;

import java.util.Date;

public class DoorbellCallBack extends Activity implements ITuyaGetBeanCallback<CameraPushDataBean>{

    private static final String TAG = "DoorbellCallBack";

    private Context mcontext;

    public DoorbellCallBack(Context context) {
        mcontext = context;
    }

    public void WakeUpMoveFullScreen() {
        Log.e(TAG, "KMK 백그라운드 화면 호출");
        AlarmManager manager = (AlarmManager)mcontext.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(mcontext.getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mcontext.getApplicationContext(), 0, intent, 0);
        Date t = new Date();
        t.setTime(java.lang.System.currentTimeMillis() + 1000);
        if(manager!=null){
            Log.e(TAG, "KMK manager != 널!");
            AlarmManagerCompat.setAlarmClock(manager, t.getTime(), pendingIntent, pendingIntent);
        }else{
            Log.e(TAG, "KMK manager = 널!");
        }
    }

    @Override
    public void onResult(CameraPushDataBean bean) {
        Log.e(TAG, "KMK 도어벨 클릭");
//            Log.e(TAG, "timestamp=" + bean.getTimestamp());
        Log.e(TAG, "KMK 장비아이디값 = " + bean.getDevId());
//            Log.e(TAG, "msgid=" + bean.getEdata());
//            Log.e(TAG, "etype=" + bean.getEtype());

        boolean isBackground = Foreground.isBackground();

        if (isBackground) {
            Log.e(TAG, "KMK 현재 백그라운드 상태입니다.");
            WakeUpMoveFullScreen();

        } else {
            Log.e(TAG, "KMK 현재 도어벨화면 상태입니다.");

            // 새 버전 fcm 전송
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();

                // Log and toast
                // String msg = getString(R.string.msg_token_fmt, token);
                Log.e(TAG, "KMK FCM토큰 : " + token);
                try {
                    JSONObject data = new JSONObject();
                    JSONObject auth = new JSONObject();

                    data.put("title", "초인종이 울렸습니다~");
                    data.put("message", "확인해주세요~");

                    auth.put("to", token);
                    auth.put("priority", "high");
                    auth.put("direct_book_ok", true);
                    auth.put("data", data);

                    Log.e(TAG, "KMK 초인종 FCM호출");
                    new LoginHelper.HttpUtil().execute(auth.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }
    }
}
