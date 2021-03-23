package com.tuya.smart.android.demo.device.common;

import android.app.Activity;
import android.content.Context;

import com.tuya.smart.android.demo.device.ICommonDeviceDebugView;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;

public class CommonDeviceDebugPresenter extends BasePresenter implements IDevListener {

    private final Context mContext;
    private final ICommonDeviceDebugView mView;
    public static final String INTENT_DEVID = "intent_devId";
    public static final String INTENT_LOCALKEY = "intent_localkey";
    public static final String INTENT_P2P_TYPE = "intent_p2p_type";
    public static final String CALL = "callin";
    public static final String DEVICE = "device";
    public static final String DEVICESTART = "devicestart";
    private String mDevId;
    private DeviceBean mDevBean;
    private ITuyaDevice mTuyaDevice;

    public CommonDeviceDebugPresenter(Context context, ICommonDeviceDebugView view) {
        mContext = context;
        mView = view;
        initData();
        initListener();
    }

    private void initListener() {
        mTuyaDevice = TuyaHomeSdk.newDeviceInstance(mDevId);
        mTuyaDevice.registerDevListener(this);
    }

    private void initData() {
        mDevId = ((Activity) mContext).getIntent().getStringExtra(INTENT_DEVID);
        mDevBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDevId);
        if (mDevBean == null) {
            ((Activity) mContext).finish();
        }
    }

    public String getTitle() {
        return mDevBean == null ? "" : mDevBean.getName();
    }

    @Override
    public void onDpUpdate(String devId, String dpStr) {

    }

    @Override
    public void onRemoved(String devId) {
        mView.deviceRemoved();
    }

    @Override
    public void onStatusChanged(String devId, boolean online) {
        mView.deviceOnlineStatusChanged(online);
    }

    @Override
    public void onNetworkStatusChanged(String devId, boolean status) {
        mView.onNetworkStatusChanged(status);
    }

    @Override
    public void onDevInfoUpdate(String devId) {
        mView.devInfoUpdate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTuyaDevice != null) mTuyaDevice.onDestroy();

    }
}
