package com.tuya.smart.android.demo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tuya.smart.android.base.utils.PreferencesUtil;
import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
import com.tuya.smart.android.demo.base.activity.BaseActivity;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.base.utils.CollectionUtils;
import com.tuya.smart.android.demo.camera.CameraPanelActivity;
import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
import com.tuya.smart.android.demo.family.activity.IFamilyAddView;
import com.tuya.smart.android.demo.family.presenter.FamilyAddPresenter;
import com.tuya.smart.android.demo.login.activity.LoginActivity;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuyasmart.camera.devicecontrol.utils.CRC32;
import com.tuyasmart.camera.devicecontrol.utils.IntToButeArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by letian on 16/7/15.
 */
public class MainActivity extends BaseActivity implements IFamilyAddView {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FamilyAddPresenter mPresenter;

    DoorBellLogin doorBellLogin;

    private void initPresenter() {
        mPresenter = new FamilyAddPresenter(this);
//        doorBellLogin = new DoorBellLogin();
    }

    private void initService() {
        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
            boolean isServiceRunningCheck = ActivityUtils.isServiceRunningCheck(MainActivity.this);
            Log.d(TAG, "isServiceRunningCheck = " + isServiceRunningCheck);
            if (isServiceRunningCheck) {
                return;
            }
            Intent serviceLauncher = new Intent(MainActivity.this, BackgroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceLauncher);
            } else {
                startService(serviceLauncher);
            }
        }, 3000);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPresenter();
//        initData();
        initService();
        new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
            if (TuyaHomeSdk.getUserInstance().isLogin()) {
                Log.e(TAG, "KMK 앱실행 완료");
//                    LoginHelper.afterLogin(); //CIS삭제
//                    DoorBellLogin doorBellLogin = new DoorBellLogin();
//                    doorBellLogin.afterLogout();
//                doorBellLogin.afterLogin(this);
                deviceLoad();
            } else {
                ActivityUtils.gotoActivity(MainActivity.this, LoginActivity.class, ActivityUtils.ANIMATE_FORWARD, true);
            }
        }, 1000); //3초 뒤에 Runner객체 실행하도록 함
    }

    public void deviceLoad() {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homeBeans) {
                if (CollectionUtils.isEmpty(homeBeans)) {
                    List<String> checkRoomList = new ArrayList<>();
                    String name = "경로당";
                    mPresenter.addFamily(name, checkRoomList);
                } else {
                    final long homeId = homeBeans.get(0).getHomeId();
                    Constant.HOME_ID = homeId;
                    PreferencesUtil.set("homeId", Constant.HOME_ID);
                    TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                        @Override
                        public void onSuccess(HomeBean bean) {
                            Intent intent = new Intent(MainActivity.this, CameraPanelActivity.class);
                            if (bean.getDeviceList().size() == 0) {
                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, "devId");
                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY, "localId");
                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, "p2pId");
                                intent.putExtra(CommonDeviceDebugPresenter.CALL, "false");
                                intent.putExtra(CommonDeviceDebugPresenter.DEVICE, "false");
                                intent.putExtra(CommonDeviceDebugPresenter.DEVICESTART, "false");
                            } else {
//                                ITuyaCameraDevice mDeviceControl;
                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, bean.getDeviceList().get(0).getDevId());
                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY, bean.getDeviceList().get(0).getLocalKey());
                                Map<String, Object> map = bean.getDeviceList().get(0).getSkills();
                                int p2pType = -1;
                                if (map == null || map.size() == 0) {
                                    p2pType = -1;
                                } else {
                                    p2pType = (Integer) (map.get("p2pType"));
                                }
                                intent.putExtra(CommonDeviceDebugPresenter.DEVICE, "true");
                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, p2pType);
                                intent.putExtra(CommonDeviceDebugPresenter.CALL, "false");
                                intent.putExtra(CommonDeviceDebugPresenter.DEVICESTART, "true");
//                                mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(bean.getDeviceList().get(0).getDevId());
                                Log.e(TAG, "KMK 앱 처음 실행 카메라를 켭니다.");
                                int crcsum = CRC32.getChecksum(bean.getDeviceList().get(0).getLocalKey().getBytes());
                                Log.e(TAG, "KMK CAMERA_ON crcsum : "+crcsum);
                                String topicId = "m/w/" + bean.getDeviceList().get(0).getDevId();
                                Log.e(TAG, "KMK CAMERA_ON topicId : "+topicId);
                                byte[] bytes = IntToButeArray.intToByteArray(crcsum);
                                Log.e(TAG, "KMK CAMERA_ON bytes : "+bytes);
                                ITuyaHomeCamera homeCamera = TuyaHomeSdk.getCameraInstance();
                                Log.e(TAG, "KMK CAMERA_ON homeCamera : "+homeCamera);
                                homeCamera.publishWirelessWake(topicId, bytes);
//                                mDeviceControl.wirelessWake(bean.getDeviceList().get(0).getLocalKey(), bean.getDeviceList().get(0).getDevId());
                            }
                            startActivity(intent);
                        }

                        @Override
                        public void onError(String errorCode, String errorMsg) {
                            Log.e(TAG, "KMK 메인액티비티 조회에러2 -> deviceLoad();함수실행");
                            deviceLoad();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorCode, String error) {
                Log.e(TAG, "KMK 메인액티비티 조회에러1 -> deviceLoad();함수실행");
                deviceLoad();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public boolean needLogin() {
        return false;
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void doSaveSuccess() {
        showToast(R.string.save_success);
        deviceLoad();
    }

    @Override
    public void doSaveFailed() {
        showToast(R.string.save_failed);
        deviceLoad();
    }

}
