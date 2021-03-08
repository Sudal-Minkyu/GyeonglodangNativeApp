package com.tuya.smart.android.demo.config;

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
import com.tuya.smart.android.base.utils.PreferencesUtil;
import com.tuya.smart.android.common.utils.WiFiUtil;
import com.tuya.smart.android.demo.MainActivity;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.family.FamilyManager;
import com.tuya.smart.android.demo.utils.QRCodeUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.builder.TuyaCameraActivatorBuilder;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaCameraDevActivator;
import com.tuya.smart.sdk.api.ITuyaSmartCameraActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

public class QrCodeConfigActivity extends AppCompatActivity implements ITuyaSmartCameraActivatorListener {

    private static final String TAG = QrCodeConfigActivity.class.getSimpleName();
    private String wifiSSId = "";
    private String token = "";
    private ImageView mIvQr;
    private LinearLayout mLlInputWifi;
    private EditText mEtInputWifiSSid;
    private EditText mEtInputWifiPwd;

    private ITuyaCameraDevActivator mTuyaActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_config);
        mLlInputWifi = findViewById(R.id.ll_input_wifi);
        mEtInputWifiSSid = findViewById(R.id.et_wifi_ssid);
        mEtInputWifiSSid.setEnabled(false);
        mEtInputWifiPwd = findViewById(R.id.et_wifi_pwd);
        // 커서위치 고정 테스트
        mEtInputWifiPwd.setSelection(mEtInputWifiPwd.length());
        Button mBtnSave = findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(v -> createQrcode());
        mIvQr = findViewById(R.id.iv_qrcode);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WifiManager wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifimanager.getConnectionInfo();
            wifiSSId = wifiInfo.getSSID();
            wifiSSId = wifiSSId.replaceAll("\"", "");
        } else {
            wifiSSId = WiFiUtil.getCurrentSSID(this);
        }

        Log.d(TAG, "KMK wifiSSId : "+wifiSSId);
        Log.e(TAG, "KMK QR코드생성 시작");
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(FamilyManager.getInstance().getCurrentHomeId(), new ITuyaActivatorGetToken() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "KMK QR코드생성 성공");
                token = s;
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.e(TAG, "KMK QR코드생성 실패");
                Log.e(TAG, "KMK 현재토큰 token : "+token);
                TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
                    @Override
                    public void onSuccess(List<HomeBean> list) {
                        final long homeId = list.get(0).getHomeId();
                        Constant.HOME_ID = homeId;
                        PreferencesUtil.set("homeId", Constant.HOME_ID);
                        Log.e(TAG, "KMK 홈리스트조회 성공");
                        Log.e(TAG, "KMK 토큰 리플레쉬 시작");
                        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                            @Override
                            public void onSuccess(HomeBean homeBean) {
                                Log.e(TAG, "KMK 홈리스트 인스턴스초기화 성공");
                                TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId, new ITuyaActivatorGetToken() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Log.e(TAG, "KMK 토큰 리플레쉬 성공");
                                        Log.e(TAG, "KMK 새로 생성된토큰 token : "+s);
                                        token = s;
                                    }

                                    @Override
                                    public void onFailure(String s, String s1) {
                                        Log.e(TAG, "KMK 토큰 리플레쉬 실패");
                                    }
                                });
                            }

                            @Override
                            public void onError(String errorCode, String errorMsg) {
                                Log.e(TAG, "KMK 홈리스트 인스턴스초기화 실패");
                            }
                        });
                    }
                    @Override
                    public void onError(String s, String s1) {
                        Log.e(TAG, "KMK 홈리스트조회 실패");
                    }
                });
            }
        });
        if (!wifiSSId.equals("<unknown ssid>")) {
            mEtInputWifiSSid.setText(wifiSSId);
        }
    }

    private void createQrcode() {
        Log.e(TAG, "KMK 받은 토큰 token = "+token);
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "토큰이 비어 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String wifiPwd = mEtInputWifiPwd.getText().toString();

        Log.e(TAG, "KMK token : " + token);
        Log.e(TAG, "KMK wifiPwd : " + wifiPwd);
        Log.e(TAG, "KMK wifiSSId : " + wifiSSId);

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
            QrCodeConfigActivity.this.runOnUiThread(() -> {
                mIvQr.setImageBitmap(bitmap);
                mIvQr.setVisibility(View.VISIBLE);
                mLlInputWifi.setVisibility(View.GONE);
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
