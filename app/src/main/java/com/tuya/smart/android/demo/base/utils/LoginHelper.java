package com.tuya.smart.android.demo.base.utils;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
import com.tuya.smart.android.camera.api.bean.CameraPushDataBean;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.login.activity.LoginActivity;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaGetBeanCallback;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginHelper extends Activity {

    private static final String TAG = "LoginHelper";
    private static ITuyaHomeCamera homeCamera;
    private static String mToken;
    private static URL obj;

    static {
        try {
            obj = new URL("https://fcm.googleapis.com/fcm/send");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static ITuyaGetBeanCallback<CameraPushDataBean> mTuyaGetBeanCallback = new ITuyaGetBeanCallback<CameraPushDataBean>() {
        @Override
        public void onResult(CameraPushDataBean o) {
//            L.e(TAG, "Doorbell Info");
//            L.e(TAG, "timestamp=" + o.getTimestamp());
//            L.e(TAG, "devid=" + o.getDevId());
//            L.e(TAG, "msgid=" + o.getEdata());
//            L.e(TAG, "etype=" + o.getEtype());

            // 새 버전 fcm 전송
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();

                    // Log and toast
                    // String msg = getString(R.string.msg_token_fmt, token);
                    Log.e("Firebase Token : ", "CIS "  + token);

                    try {
                        JSONObject data = new JSONObject();
                        JSONObject auth = new JSONObject();

                        data.put("title", "초인종이 울렸습니다~");
                        data.put("message", "확인해주세요~");

                        auth.put("to", token);
                        auth.put("priority", "high");
                        auth.put("direct_book_ok", true);
                        auth.put("data", data);

                        Log.e("Firebase Token", "CIS 초인종 FCM호출 - LoginHelper.java onComplete");
                        new HttpUtil().execute(auth.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public static class HttpUtil extends AsyncTask<String, Void, Void> {
        @Override
        public Void doInBackground(String... params) {
            try {
//                URL obj = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "key=AAAAsIcKiJo:APA91bEpVRhvZt8PlOHMNcu1tNqMKH8eogyd4TN0eOuAxJvLRm8ZbWhXe8BTjlkydK7pyughHlljYX94IagDmnGihidK2poV0y_mBhEsxWdMSjnyZwTTazwCPOjOwUCWXKyy9eumsHUk");
                byte[] outputInBytes = params[0].getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputInBytes);
                os.close();

                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void afterLogin() {
        L.e(TAG, "afterLogin");
        homeCamera = TuyaHomeSdk.getCameraInstance();
        if (homeCamera != null) {
            L.e(TAG, "CIS - 로그인 후 도어벨 리스너 서비스 시작 ");
            homeCamera.registerCameraPushListener(mTuyaGetBeanCallback);
        }
    }

    private static void afterLogout() {
        L.e(TAG, "afterLogout");
        if (homeCamera != null) {
            homeCamera.unRegisterCameraPushListener(mTuyaGetBeanCallback);
        }
        homeCamera = null;
    }

    public static void reLogin(Context context) {
        reLogin(context, true);
    }

    public static void reLogin(Context context, boolean tip) {
        onLogout(context);
        if (tip) {
            ToastUtil.shortToast(context, R.string.login_session_expired);
        }
        try {
            ActivityUtils.gotoActivity((Activity) context, LoginActivity.class, ActivityUtils.ANIMATE_FORWARD, true);
        } catch (java.lang.ClassCastException e) {
        }
    }

    private static void onLogout(Context context) {
        afterLogout();
        exit(context);
    }

    public static void exit(Context context) {
        Constant.finishActivity();
        TuyaHomeSdk.onDestroy();
    }

    public static void exit2() {
        Constant.finishActivity();
    }

}
