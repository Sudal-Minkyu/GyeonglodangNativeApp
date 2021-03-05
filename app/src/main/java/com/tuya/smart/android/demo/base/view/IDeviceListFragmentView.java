package com.tuya.smart.android.demo.base.view;

import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

/**
 * Created by letian on 16/7/18.
 */
public interface IDeviceListFragmentView {
    // 빈 목록 프롬프트를 표시할지 여부를 결정하는 데 사용 된 모든 장치의 현재 수를 반환합니다.
    void updateDeviceData(List<DeviceBean> myDevices);

    void loadStart();

    void loadFinish();

    void showNetWorkTipView(int tipRes);

    void hideNetWorkTipView();

    void showBackgroundView();

    void hideBackgroundView();

    void gotoCreateHome();
}
