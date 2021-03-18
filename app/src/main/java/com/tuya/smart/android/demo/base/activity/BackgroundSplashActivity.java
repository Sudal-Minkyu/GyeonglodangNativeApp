//package com.tuya.smart.android.demo.base.activity;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.content.res.Resources;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//
//import com.tuya.smart.android.base.utils.PreferencesUtil;
//import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
//import com.tuya.smart.android.demo.BackgroundService;
//import com.tuya.smart.android.demo.R;
//import com.tuya.smart.android.demo.base.app.Constant;
//import com.tuya.smart.android.demo.base.utils.CollectionUtils;
//import com.tuya.smart.android.demo.base.utils.LoginHelper;
//import com.tuya.smart.android.demo.camera.BackgroundCameraPanelActivity;
//import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
//import com.tuya.smart.android.demo.family.activity.IFamilyAddView;
//import com.tuya.smart.android.demo.family.presenter.FamilyAddPresenter;
//import com.tuya.smart.home.sdk.TuyaHomeSdk;
//import com.tuya.smart.home.sdk.bean.HomeBean;
//import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
//import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
//import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
//import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
///**
// * Created by letian on 16/7/15.
// */
//public class BackgroundSplashActivity extends Activity implements IFamilyAddView {
//
//    private FamilyAddPresenter mPresenter;
//
//    private void initPresenter() {
//        mPresenter = new FamilyAddPresenter(this);
//    }
//
//    private static ITuyaHomeCamera homeCamera;
//
//
//    private void initService() {
//        new Handler().postDelayed(new Runnable() {
//            // 3초 후에 실행
//            @Override
//            public void run() {
//                // BackgroundService
//                    Intent serviceLauncher = new Intent(BackgroundSplashActivity.this, BackgroundService.class);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        startForegroundService(serviceLauncher);
//                    } else {
//                        startService(serviceLauncher);
//                    }
//            }
//        }, 3000);
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.transparent_activity);
//
//
//        initPresenter();
//
//        initService();
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (TuyaHomeSdk.getUserInstance().isLogin()) {
//                    //LoginHelper.afterLogin(); //CIS삭제
//                    deviceLoad();
//                }
//            }
//        }, 1000); //3초 뒤에 Runner객체 실행하도록 함
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_BOOT_COMPLETE) {
//            finish();
//        }
//    }
//
//
//    int REQUEST_BOOT_COMPLETE = 2100; // 부트 완료 여부
//
//
//    public void deviceLoad() {
//        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
//            @Override
//            public void onSuccess(List<HomeBean> homeBeans) {
//                if (CollectionUtils.isEmpty(homeBeans)) {
//                    List<String> checkRoomList = new ArrayList<>();
//                    String name = "경로당";
//                    mPresenter.addFamily(name, checkRoomList);
//                } else {
//                    final long homeId = homeBeans.get(0).getHomeId();
//                    Constant.HOME_ID = homeId;
//                    PreferencesUtil.set("homeId", Constant.HOME_ID);
//                    TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
//                        @Override
//                        public void onSuccess(HomeBean bean) {
//                            Intent intent = new Intent(BackgroundSplashActivity.this, BackgroundCameraPanelActivity.class);
//                            if (bean.getDeviceList().size() == 0) {
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, "devId");
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY, "localId");
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, "p2pId");
//                                intent.putExtra(CommonDeviceDebugPresenter.CALL, "false");
//                                intent.putExtra(CommonDeviceDebugPresenter.DEVICE, "false");
//                            } else {
//                                ITuyaCameraDevice mDeviceControl;
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, bean.getDeviceList().get(0).getDevId());
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY, bean.getDeviceList().get(0).getLocalKey());
//                                Map<String, Object> map = bean.getDeviceList().get(0).getSkills();
//                                int p2pType = -1;
//                                if (map == null || map.size() == 0) {
//                                    p2pType = -1;
//                                } else {
//                                    p2pType = (Integer) (map.get("p2pType"));
//                                }
//                                intent.putExtra(CommonDeviceDebugPresenter.DEVICE, "true");
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, p2pType);
//                                intent.putExtra(CommonDeviceDebugPresenter.CALL, "false");
//                                mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(bean.getDeviceList().get(0).getDevId());
//                                mDeviceControl.wirelessWake(bean.getDeviceList().get(0).getLocalKey(), bean.getDeviceList().get(0).getDevId());
//                            }
////                            startActivityForResult(intent, REQUEST_BOOT_COMPLETE);
//                            startActivity(intent);
//                        }
//
//                        @Override
//                        public void onError(String errorCode, String errorMsg) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onError(String errorCode, String error) {
//                TuyaHomeSdk.newHomeInstance(Constant.HOME_ID).getHomeLocalCache(new ITuyaHomeResultCallback() {
//                    @Override
//                    public void onSuccess(HomeBean bean) {
//
//                    }
//
//                    @Override
//                    public void onError(String errorCode, String errorMsg) {
//
//                    }
//                });
//            }
//        });
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        finish();
//    }
//
//
//    @Override
//    public Resources getResources() {
//        Resources res = super.getResources();
//        Configuration config = new Configuration();
//        config.setToDefaults();
//        res.updateConfiguration(config, res.getDisplayMetrics());
//        return res;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public Context getContext() {
//        return null;
//    }
//
//    @Override
//    public void doSaveSuccess() {
//
//    }
//
//    @Override
//    public void doSaveFailed() {
//
//    }
//
//
//}
