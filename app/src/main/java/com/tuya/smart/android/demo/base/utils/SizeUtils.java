package com.tuya.smart.android.demo.base.utils;

import com.tuya.smart.android.demo.TuyaSmartApp;

public class SizeUtils {

    private SizeUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static int dp2px(float dpValue) {
        float scale = TuyaSmartApp.getAppContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getWidth() {
        return TuyaSmartApp.getAppContext().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getHeight() {
        return TuyaSmartApp.getAppContext().getResources().getDisplayMetrics().heightPixels;
    }

}
