package com.tuya.smart.android.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tuya.smart.home.sdk.TuyaHomeSdk;

public class ScreenReceiver extends BroadcastReceiver {

    private final static String TAG = ScreenReceiver.class.getSimpleName();

    // BroadcastReceiver를 상속하여 처리 해줍니다.
    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO Auto-generated method stub
        // 전달 받은 Broadcast의 값을 가져오기
        String action = intent.getAction();
        // 전달된 값이 '부팅완료' 인 경우에만 동작 하도록 조건문을 설정 해줍니다.
        Log.d(TAG, "action = " + action);
        if (action.equals("android.intent.action.SCREEN_OFF")) {
            TuyaHomeSdk.enableBackgroundConnect();
            Log.d(TAG, "tuya sdk background connect ");
        }
    }
}

