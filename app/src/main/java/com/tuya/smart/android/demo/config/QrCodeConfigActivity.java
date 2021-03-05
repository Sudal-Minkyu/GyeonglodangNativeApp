package com.tuya.smart.android.demo.config;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.WriterException;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.WiFiUtil;
import com.tuya.smart.android.demo.MainActivity;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.activity.SplashActivity;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.camera.CameraPanelActivity;
import com.tuya.smart.android.demo.family.FamilyManager;
import com.tuya.smart.android.demo.utils.QRCodeUtil;
import com.tuya.smart.android.user.api.IGetQRCodeTokenCallback;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.TuyaCameraActivatorBuilder;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaCameraDevActivator;
import com.tuya.smart.sdk.api.ITuyaSmartCameraActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;

public class QrCodeConfigActivity extends AppCompatActivity implements ITuyaSmartCameraActivatorListener {

    private static final String TAG = QrCodeConfigActivity.class.getSimpleName();
    private String wifiSSId = "";
    private String token = "";
    private String wifiPwd = "Wi-Fi 비밀번호입력";
    private ImageView mIvQr;
    private LinearLayout mLlInputWifi;
    private EditText mEtInputWifiSSid;
    private EditText mEtInputWifiPwd;
    private Button mBtnSave;

    private ITuyaCameraDevActivator mTuyaActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_config);
        mLlInputWifi = findViewById(R.id.ll_input_wifi);
        mEtInputWifiSSid = findViewById(R.id.et_wifi_ssid);
        mEtInputWifiPwd = findViewById(R.id.et_wifi_pwd);
        // 커서위치 고정 테스트
        EditText edit = findViewById(R.id.et_wifi_pwd);
        edit.setSelection(edit.length());
        mBtnSave = findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createQrcode();
            }
        });
        mIvQr = findViewById(R.id.iv_qrcode);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WifiManager wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifimanager.getConnectionInfo();
            wifiSSId = wifiInfo.getSSID();
            wifiSSId = wifiSSId.replaceAll("\\\"", "");

        } else {
            wifiSSId = WiFiUtil.getCurrentSSID(this);
        }

        Log.d(TAG, "wifiSSId" + wifiSSId);


        TuyaHomeSdk.getActivatorInstance().getActivatorToken(FamilyManager.getInstance().getCurrentHomeId(), new ITuyaActivatorGetToken() {
            @Override
            public void onSuccess(String s) {
                token = s;
            }

            @Override
            public void onFailure(String s, String s1) {
                System.out.println("QR 코드구성 활동 : " + s);
                System.out.println("QR 코드구성 활동2 : " + s1);
            }
        });
        if (!wifiSSId.equals("<unknown ssid>")) {
            mEtInputWifiSSid.setText(wifiSSId);
        }
    }

    private void createQrcode() {
        System.out.println("받은 토큰 : " + token);
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "토큰이 비어 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        wifiPwd = mEtInputWifiPwd.getText().toString();

        Log.e("tuya", "token : " + token);
        Log.e("tuya", "wifiPwd : " + wifiPwd);
        Log.e("tuya", "wifiSSId : " + wifiSSId);

        TuyaCameraActivatorBuilder builder = new TuyaCameraActivatorBuilder()
                .setToken(token).setPassword(wifiPwd).setSsid(wifiSSId).setListener(this);
        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newCameraDevActivator(builder);
        mTuyaActivator.createQRCode();
        mTuyaActivator.start();
    }

    @Override
    public void onQRCodeSuccess(String s) {
        final Bitmap bitmap;
        try {
            bitmap = QRCodeUtil.createQRCode(s, 300);
            QrCodeConfigActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIvQr.setImageBitmap(bitmap);
                    mIvQr.setVisibility(View.VISIBLE);
                    mLlInputWifi.setVisibility(View.GONE);
                }
            });
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(String s, String s1) {

    }

    @Override
    public void onActiveSuccess(DeviceBean deviceBean) {
        ToastUtil.showToast(QrCodeConfigActivity.this, "도어벨 등록성공");
        finish();
        Intent intent = new Intent(QrCodeConfigActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTuyaActivator) {
            mTuyaActivator.stop();
            mTuyaActivator.onDestroy();
        }
    }
}
