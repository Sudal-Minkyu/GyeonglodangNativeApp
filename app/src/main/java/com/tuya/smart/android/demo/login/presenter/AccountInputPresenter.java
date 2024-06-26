package com.tuya.smart.android.demo.login.presenter;

import android.app.Activity;
import android.content.Intent;

import com.tuya.smart.android.demo.base.utils.CountryUtils;
import com.tuya.smart.android.demo.login.IAccountInputView;
import com.tuya.smart.android.demo.login.activity.AccountConfirmActivity;
import com.tuya.smart.android.demo.login.activity.AccountInputActivity;
import com.tuya.smart.android.demo.login.activity.CountryListActivity;
import com.tuya.smart.android.mvp.presenter.BasePresenter;

public class AccountInputPresenter extends BasePresenter {
    private static final int REQUEST_COUNTRY_CODE = 998;
    private static final int REQUEST_CONFIRM = 999;
    private final IAccountInputView mView;

    private final Activity mContext;

    private String mCountryName;
    private String mCountryCode;


    public AccountInputPresenter(Activity context, IAccountInputView iAccountInputView) {
        mContext = context;
        mView = iAccountInputView;

        initCountryInfo();
    }

    private void initCountryInfo() {
        mCountryName = CountryUtils.getCountryTitle("KR");
        mCountryCode = CountryUtils.getCountryNum("KR");
        mView.setCountryInfo(mCountryName, mCountryCode);
    }

    public void selectCountry() {
        mContext.startActivityForResult(new Intent(mContext, CountryListActivity.class), REQUEST_COUNTRY_CODE);
    }

    public void gotoNext(int accountType) {
        int mode = AccountConfirmActivity.MODE_REGISTER;
        if (AccountInputActivity.MODE_PASSWORD_FOUND == mView.getMode()) {
            mode = AccountConfirmActivity.MODE_FORGET_PASSWORD;
        }

        AccountConfirmActivity.gotoAccountConfirmActivityForResult(mContext, mView.getAccount(), mCountryCode, mode, accountType, REQUEST_CONFIRM);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_COUNTRY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    mCountryName = data.getStringExtra(CountryListActivity.COUNTRY_NAME);
                    mCountryCode = data.getStringExtra(CountryListActivity.PHONE_CODE);
                    mView.setCountryInfo(mCountryName, mCountryCode);
                }
                break;

            case REQUEST_CONFIRM:
                if (resultCode == Activity.RESULT_OK) {
                    mContext.finish();
                }
                break;
        }
    }
}
