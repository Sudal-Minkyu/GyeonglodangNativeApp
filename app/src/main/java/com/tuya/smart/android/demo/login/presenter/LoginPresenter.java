package com.tuya.smart.android.demo.login.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.tuya.smart.android.common.utils.ValidatorUtil;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.base.utils.CountryUtils;
import com.tuya.smart.android.demo.base.utils.LoginHelper;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.android.demo.login.ILoginView;
import com.tuya.smart.android.demo.login.activity.CountryListActivity;
import com.tuya.smart.android.mvp.bean.Result;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

public class LoginPresenter extends BasePresenter {
    protected Activity mContext;
    protected ILoginView mView;

    private String mCountryName;
    private String mCountryCode;

    public static final int MSG_LOGIN_SUCCESS = 15;
    public static final int MSG_LOGIN_FAILURE = 16;

    public LoginPresenter(Context context, ILoginView view) {
        super();
        mContext = (Activity) context;
        mView = view;
        initCountryInfo();
    }

    private void initCountryInfo() {
        mCountryName = CountryUtils.getCountryTitle("KR");
        mCountryCode = CountryUtils.getCountryNum("KR");
        mView.setCountry(mCountryName, mCountryCode);
    }

    // 选择国家/地区信息
    public void selectCountry() {
        mContext.startActivityForResult(new Intent(mContext, CountryListActivity.class), 0x01);
    }

    // 登录
    public void login(String userName, String password) {

        if (!ValidatorUtil.isEmail(userName)) {
            TuyaHomeSdk.getUserInstance().loginWithPhonePassword(mCountryCode, userName, password, mLoginCallback);
        } else {
            TuyaHomeSdk.getUserInstance().loginWithEmail(mCountryCode, userName, password, mLoginCallback);
        }
    }

    private final ILoginCallback mLoginCallback = new ILoginCallback() {
        @Override
        public void onSuccess(User user) {
            mHandler.sendEmptyMessage(MSG_LOGIN_SUCCESS);
        }

        @Override
        public void onError(String s, String s1) {
            Message msg = MessageUtil.getCallFailMessage(MSG_LOGIN_FAILURE, s, s1);
            mHandler.sendMessage(msg);
        }
    };

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case MSG_LOGIN_SUCCESS:
                // 로그인 성공
                mView.modelResult(msg.what, null);
                LoginHelper.afterLogin(mContext);
                ActivityUtils.gotoMainActivity(mContext); // 스플레쉬액티비티 실행 -> 최초로그인후 나오는화면
                break;
            case MSG_LOGIN_FAILURE:
                // 로그인 실패
                mView.modelResult(msg.what, (Result) msg.obj);
                break;
            default:
                break;
        }

        return super.handleMessage(msg);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x01) {
            if (resultCode == Activity.RESULT_OK) {
                mCountryName = data.getStringExtra(CountryListActivity.COUNTRY_NAME);
                mCountryCode = data.getStringExtra(CountryListActivity.PHONE_CODE);
                mView.setCountry(mCountryName, mCountryCode);
            }
        }
    }
}
