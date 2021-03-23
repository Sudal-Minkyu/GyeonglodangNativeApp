package com.tuya.smart.android.demo.config;

import android.os.Bundle;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.activity.BaseActivity;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;

public class AddDeviceTypeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device_type);
        initToolbar();
        initView();
        setTitle(getString(R.string.ty_add_device_sort));
        setDisplayHomeAsUpEnabled();
    }

    private void initView() {
        findViewById(R.id.grcode_btn).setOnClickListener(v -> startQrCodeDevConfig());
    }

    private void startQrCodeDevConfig() {
        ActivityUtils.gotoActivity(this, QrCodeConfigActivity.class, ActivityUtils.ANIMATE_FORWARD, false);
    }

}
