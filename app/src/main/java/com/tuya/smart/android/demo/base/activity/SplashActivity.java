//package com.tuya.smart.android.demo.base.activity;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.ApplicationInfo;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.text.TextUtils;
//
//import com.tuya.smart.android.base.utils.PreferencesUtil;
//import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
//import com.tuya.smart.android.common.utils.L;
//import com.tuya.smart.android.common.utils.TuyaUtil;
//import com.tuya.smart.android.demo.MainActivity;
//import com.tuya.smart.android.demo.R;
//import com.tuya.smart.android.demo.TuyaSmartApp;
//import com.tuya.smart.android.demo.base.app.Constant;
//import com.tuya.smart.android.demo.base.fragment.DeviceListFragment;
//import com.tuya.smart.android.demo.base.presenter.DeviceListFragmentPresenter;
//import com.tuya.smart.android.demo.base.presenter.HomePresenter;
//import com.tuya.smart.android.demo.base.utils.CollectionUtils;
//import com.tuya.smart.android.demo.base.utils.DialogUtil;
//import com.tuya.smart.android.demo.base.utils.ToastUtil;
//import com.tuya.smart.android.demo.base.view.IHomeView;
//import com.tuya.smart.android.demo.camera.CameraPanelActivity;
//import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
//import com.tuya.smart.android.demo.family.activity.FamilyAddActivity;
//import com.tuya.smart.android.demo.family.activity.IFamilyAddView;
//import com.tuya.smart.android.demo.family.presenter.FamilyAddPresenter;
//import com.tuya.smart.android.demo.login.activity.LoginActivity;
//import com.tuya.smart.android.demo.base.utils.ActivityUtils;
//import com.tuya.smart.android.demo.base.utils.LoginHelper;
//import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
//import com.tuya.smart.home.sdk.TuyaHomeSdk;
//import com.tuya.smart.home.sdk.api.ITuyaHomeStatusListener;
//import com.tuya.smart.home.sdk.bean.HomeBean;
//import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
//import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
//import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
//import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.UUID;
//
//public class SplashActivity extends BaseActivity implements IFamilyAddView {
//
//    private FamilyAddPresenter mPresenter;
//    private void initPresenter() {
//        mPresenter = new FamilyAddPresenter(this);
//        }
//    private static ITuyaHomeCamera homeCamera;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main2);
//        initPresenter();
////        L.d("splash", "tuyaTime: " + TuyaUtil.formatDate(System.currentTimeMillis(), "yyyy-mm-dd hh:mm:ss"));
//
////        Intent intent = new Intent(SplashActivity.this, FamilyAddActivity.class);
////        startActivity(intent);
//
//        if (isInitAppkey()) {
//            if (TuyaHomeSdk.getUserInstance().isLogin()) {
//                //LoginHelper.afterLogin(); //CIS삭제
//                deviceLoad();
////                ActivityUtils.gotoHomeActivity(this);
//            } else {
//                ActivityUtils.gotoActivity(this, LoginActivity.class, ActivityUtils.ANIMATE_FORWARD, true);
//            }
//        } else {
//            showTipDialog();
//        }
//    }
//
//    public void deviceLoad() {
//        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
//            @Override
//            public void onSuccess(List<HomeBean> homeBeans) {
//                if (CollectionUtils.isEmpty(homeBeans)) {
//                    List<String> checkRoomList = new ArrayList<>();
//                    String name = "경로당";
//                    mPresenter.addFamily(name,checkRoomList);
//                }else{
//                    final long homeId = homeBeans.get(0).getHomeId();
//                    Constant.HOME_ID = homeId;
//                    PreferencesUtil.set("homeId", Constant.HOME_ID);
//                    TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
//                        @Override
//                        public void onSuccess(HomeBean bean) {
//                            Intent intent = new Intent(SplashActivity.this, CameraPanelActivity.class);
//                            if (bean.getDeviceList().size()==0) {
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID,"devId");
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY,"localId");
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE,"p2pId");
//                                intent.putExtra(CommonDeviceDebugPresenter.CALL,"false");
//                                intent.putExtra(CommonDeviceDebugPresenter.DEVICE,"false");
//                            }else{
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
//                                intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, p2pType);
//                                intent.putExtra(CommonDeviceDebugPresenter.CALL,"false");
//                                intent.putExtra(CommonDeviceDebugPresenter.DEVICE,"true");
//                                mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(bean.getDeviceList().get(0).getDevId());
//                                mDeviceControl.wirelessWake(bean.getDeviceList().get(0).getLocalKey(),bean.getDeviceList().get(0).getDevId());
//                            }
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
//    private boolean isInitAppkey() {
//        String appkey = getInfo("TUYA_SMART_APPKEY", this);
//        String appSecret = getInfo("TUYA_SMART_SECRET", this);
//        if (TextUtils.equals("null", appkey) || TextUtils.equals("null", appSecret)) return false;
//        return !TextUtils.isEmpty(appkey) && !TextUtils.isEmpty(appSecret);
//    }
//
//    public static String getInfo(String infoName, Context context) {
//        ApplicationInfo e;
//        try {
//            e = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
//            return e.metaData.getString(infoName);
//        } catch (PackageManager.NameNotFoundException e1) {
//            e1.printStackTrace();
//        }
//        return "";
//    }
//
//    private void showTipDialog() {
//        DialogUtil.simpleConfirmDialog(this, "appkey or appsecret is empty. \nPlease check your configuration", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                finish();
//            }
//        });
//    }
//
//    @Override
//    public Context getContext() {
//        return null;
//    }
//
//    @Override
//    public void doSaveSuccess() {
//        showToast(R.string.save_success);
//        deviceLoad();
//    }
//
//    @Override
//    public void doSaveFailed() {
//        showToast(R.string.save_failed);
//        deviceLoad();
//    }
//}
