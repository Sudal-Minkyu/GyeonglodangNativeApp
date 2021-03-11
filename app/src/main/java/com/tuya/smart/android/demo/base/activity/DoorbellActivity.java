package com.tuya.smart.android.demo.base.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.tuya.smart.android.base.utils.PreferencesUtil;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.TuyaUtil;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.fragment.DeviceListFragment;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.base.utils.DialogUtil;
import com.tuya.smart.android.demo.camera.CameraPanelActivity;
import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
import com.tuya.smart.android.demo.login.activity.LoginActivity;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHomeStatusListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;

import java.util.List;
import java.util.Map;


/**
 * Created by letian on 16/7/19.
 */
public class DoorbellActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (TuyaHomeSdk.getUserInstance().isLogin()) {
                    deviceLoad();
                } else {
                    ActivityUtils.gotoActivity(DoorbellActivity.this, LoginActivity.class, ActivityUtils.ANIMATE_FORWARD, true);
                }
            }
        }, 1000); //3초 뒤에 Runner객체 실행하도록 함

    }


    public void deviceLoad() {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homeBeans) {
                final long homeId = homeBeans.get(0).getHomeId();
                Constant.HOME_ID = homeId;
                PreferencesUtil.set("homeId", Constant.HOME_ID);
                TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean bean) {
                        Intent intent = new Intent(DoorbellActivity.this, CameraPanelActivity.class);
                        if (bean.getDeviceList().size()==0) {
                            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID,"devId");
                            intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY,"localId");
                            intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE,"p2pId");
                            intent.putExtra(CommonDeviceDebugPresenter.CALL,"false");
                            intent.putExtra(CommonDeviceDebugPresenter.DEVICE,"false");
                        }else{
                            ITuyaCameraDevice mDeviceControl;
                            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, bean.getDeviceList().get(0).getDevId());
                            intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY, bean.getDeviceList().get(0).getLocalKey());
                            Map<String, Object> map = bean.getDeviceList().get(0).getSkills();
                            int p2pType = -1;
                            if (map == null || map.size() == 0) {
                                p2pType = -1;
                            } else {
                                p2pType = (Integer) (map.get("p2pType"));
                            }
                            intent.putExtra(CommonDeviceDebugPresenter.DEVICE,"true");
                            intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, p2pType);
                            intent.putExtra(CommonDeviceDebugPresenter.CALL,"true");
                            mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(bean.getDeviceList().get(0).getDevId());
                            mDeviceControl.wirelessWake(bean.getDeviceList().get(0).getLocalKey(),bean.getDeviceList().get(0).getDevId());
                        }
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {

                    }
                });
            }
            @Override
            public void onError(String errorCode, String error) {
                TuyaHomeSdk.newHomeInstance(Constant.HOME_ID).getHomeLocalCache(new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean bean) {

                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {

                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}
