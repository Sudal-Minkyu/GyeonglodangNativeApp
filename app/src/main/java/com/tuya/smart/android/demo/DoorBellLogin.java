package com.tuya.smart.android.demo;

import android.content.Context;
import android.util.Log;

import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

public class DoorBellLogin {

    private static final String TAG = "DoorbellCallBack";

    void afterLogin(Context context) {
        Log.e(TAG, "KMK afterLogout 호출");
        ITuyaHomeCamera homeCamera = TuyaHomeSdk.getCameraInstance();
        if (homeCamera != null) {
            homeCamera.registerCameraPushListener(new DoorbellCallBack(context));
        }
    }

}
