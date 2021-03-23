package com.tuya.smart.android.demo.device;

public interface ICommonDeviceDebugView {

    void deviceRemoved();

    void deviceOnlineStatusChanged(boolean online);

    void onNetworkStatusChanged(boolean status);

    void devInfoUpdate();

}
