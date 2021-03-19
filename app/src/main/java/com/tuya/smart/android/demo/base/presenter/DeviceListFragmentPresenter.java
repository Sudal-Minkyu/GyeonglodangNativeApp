package com.tuya.smart.android.demo.base.presenter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;

import com.tuya.smart.android.base.event.NetWorkStatusEvent;
import com.tuya.smart.android.base.event.NetWorkStatusEventModel;
import com.tuya.smart.android.base.utils.PreferencesUtil;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.activity.BrowserActivity;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.fragment.DeviceListFragment;
import com.tuya.smart.android.demo.base.utils.DialogUtil;
import com.tuya.smart.android.demo.base.utils.ProgressUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.base.view.IDeviceListFragmentView;
import com.tuya.smart.android.demo.camera.CameraPanelActivity;
import com.tuya.smart.android.demo.config.CommonConfig;
import com.tuya.smart.android.demo.device.SwitchActivity;
import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHomeStatusListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.TuyaSdk;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;

import java.util.List;
import java.util.Map;

public class DeviceListFragmentPresenter extends BasePresenter implements NetWorkStatusEvent {

    private static final String TAG = "DeviceListFragmentPresenter";
    private static final int WHAT_JUMP_GROUP_PAGE = 10212;
    protected IDeviceListFragmentView mView;

    public DeviceListFragmentPresenter(DeviceListFragment fragment, IDeviceListFragmentView view) {
        mActivity = fragment.getActivity();
        mView = view;
        TuyaSdk.getEventBus().register(this);
        Constant.HOME_ID = PreferencesUtil.getLong("homeId", Constant.HOME_ID);
    }

    public void getData() {
        mView.loadStart();
        getDataFromServer();
    }

    private void showDevIsNotOnlineTip(final DeviceBean deviceBean) {
        final boolean isShared = deviceBean.isShare;
        DialogUtil.customDialog(mActivity, mActivity.getString(R.string.title_device_offline), mActivity.getString(R.string.content_device_offline),
                mActivity.getString(isShared ? R.string.ty_offline_delete_share : R.string.cancel_connect),
                mActivity.getString(R.string.right_button_device_offline), mActivity.getString(R.string.left_button_device_offline), (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            if (isShared) {
//                                    //공유 삭제로 이동
//                                    Intent intent = new Intent(mActivity, SharedActivity.class);
//                                    intent.putExtra(SharedActivity.CURRENT_TAB, SharedActivity.TAB_RECEIVED);
//                                    mActivity.startActivity(intent);
                            } else {
                                DialogUtil.simpleConfirmDialog(mActivity, mActivity.getString(R.string.device_confirm_remove), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            unBindDevice(deviceBean);
                                        }
                                    }
                                });
                            }
                            break;
                        case DialogInterface.BUTTON_NEUTRAL:
//                              //재설정 지침
                            Intent intent = new Intent(mActivity, BrowserActivity.class);
                            intent.putExtra(BrowserActivity.EXTRA_LOGIN, false);
                            intent.putExtra(BrowserActivity.EXTRA_REFRESH, true);
                            intent.putExtra(BrowserActivity.EXTRA_TOOLBAR, true);
                            intent.putExtra(BrowserActivity.EXTRA_TITLE, mActivity.getString(R.string.left_button_device_offline));
                            intent.putExtra(BrowserActivity.EXTRA_URI, CommonConfig.RESET_URL);
                            mActivity.startActivity(intent);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }).show();

    }


    public void onDeviceClick(DeviceBean deviceBean) {
        if (!deviceBean.getIsOnline()) {
            showDevIsNotOnlineTip(deviceBean);
            return;
        }
        // 장비클릭시 카메라깨우기
        mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(deviceBean.getDevId());
        mDeviceControl.wirelessWake(deviceBean.getLocalKey(),deviceBean.getDevId());
        onItemClick(deviceBean);
    }


    protected void onItemClick(DeviceBean devBean) {
        if (devBean == null) {
            ToastUtil.showToast(mActivity, R.string.no_device_found);
            return;
        }
        if (devBean.getProductId().equals("4eAeY1i5sUPJ8m8d")) {
            Intent intent = new Intent(mActivity, SwitchActivity.class);
            intent.putExtra(SwitchActivity.INTENT_DEVID, devBean.getDevId());
            mActivity.startActivity(intent);
        } else {
            gotoDeviceCommonActivity(devBean);
        }
    }

    private ITuyaCameraDevice mDeviceControl;
    protected Activity mActivity;
    public void gotoDeviceCommonActivity(DeviceBean devBean) {
        Intent intent = new Intent(mActivity, CameraPanelActivity.class);
        if(devBean == null){
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID,"devId");
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY,"localId");
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE,"p2pId");
        }else{
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, devBean.getDevId());
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY, devBean.getLocalKey());
            Map<String, Object> map = devBean.getSkills();
            int p2pType = -1;
            if (map == null || map.size() == 0) {
                p2pType = -1;
            } else {
                p2pType = (Integer) (map.get("p2pType"));
            }
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, p2pType);
            mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devBean.getDevId());
            mDeviceControl.wirelessWake(devBean.getLocalKey(),devBean.getDevId());
        }
        mActivity.startActivity(intent);
    }

    public void getDataFromServer() {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homeBeans) {
                if (homeBeans.size() == 0) {
                    mView.gotoCreateHome();
                    return;
                }
                final long homeId = homeBeans.get(0).getHomeId();
                Constant.HOME_ID = homeId;
                PreferencesUtil.set("homeId", Constant.HOME_ID);
                TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean bean) {
                        updateDeviceData(bean.getDeviceList());
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {

                    }
                });
                TuyaHomeSdk.newHomeInstance(homeId).registerHomeStatusListener(new ITuyaHomeStatusListener() {
                    @Override
                    public void onDeviceAdded(String devId) {

                    }

                    @Override
                    public void onDeviceRemoved(String devId) {

                    }

                    @Override
                    public void onGroupAdded(long groupId) {

                    }

                    @Override
                    public void onGroupRemoved(long groupId) {

                    }

                    @Override
                    public void onMeshAdded(String meshId) {
                        L.d(TAG, "onMeshAdded: " + meshId);
                    }


                });

            }

            @Override
            public void onError(String errorCode, String error) {
                TuyaHomeSdk.newHomeInstance(Constant.HOME_ID).getHomeLocalCache(new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean bean) {
                        L.d(TAG, com.alibaba.fastjson.JSONObject.toJSONString(bean));
                        updateDeviceData(bean.getDeviceList());
                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {

                    }
                });
            }
        });
    }

    public boolean onDeviceLongClick(final DeviceBean deviceBean) {
        if (deviceBean.getIsShare()) {
            return false;
        }
        DialogUtil.simpleConfirmDialog(mActivity, mActivity.getString(R.string.device_confirm_remove), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    unBindDevice(deviceBean);
                }
            }
        });
        return true;
    }

    /**
     * 移除网关
     */
    private void unBindDevice(final DeviceBean deviceBean) {
        ProgressUtil.showLoading(mActivity, R.string.loading);
        TuyaHomeSdk.newDeviceInstance(deviceBean.getDevId()).removeDevice(new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                ProgressUtil.hideLoading();
                ToastUtil.showToast(mActivity, s1);
            }

            @Override
            public void onSuccess() {
                ProgressUtil.hideLoading();
            }
        });

    }

    private void updateDeviceData(List<DeviceBean> list) {
        mView.updateDeviceData(list);
        mView.loadFinish();
    }

    @Override
    public void onEvent(NetWorkStatusEventModel eventModel) {
        netStatusCheck(eventModel.isAvailable());
    }

    public void netStatusCheck(boolean isNetOk) {
        networkTip(isNetOk, R.string.ty_no_net_info);
    }

    private void networkTip(boolean networkok, int tipRes) {
        if (networkok) {
            mView.hideNetWorkTipView();
        } else {
            mView.showNetWorkTipView(tipRes);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TuyaSdk.getEventBus().unregister(this);
    }

    public void addDemoDevice() {
        ProgressUtil.showLoading(mActivity, null);
        TuyaHomeSdk.getRequestInstance().requestWithApiName("s.m.dev.sdk.demo.list", "1.0", null, new IRequestCallback() {
            @Override
            public void onSuccess(Object result) {
                ProgressUtil.hideLoading();
                getDataFromServer();
            }

            @Override
            public void onFailure(String errorCode, String errorMsg) {
                ProgressUtil.hideLoading();
                ToastUtil.showToast(mActivity, errorMsg);
            }
        });
    }
}
