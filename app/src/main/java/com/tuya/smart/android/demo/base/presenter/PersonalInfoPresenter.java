package com.tuya.smart.android.demo.base.presenter;

import android.app.Activity;
import android.os.Message;
import android.text.TextUtils;

import com.tuya.smart.android.demo.base.utils.ProgressUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.login.activity.AccountConfirmActivity;
import com.tuya.smart.android.demo.personal.IPersonalInfoView;
import com.tuya.smart.android.demo.personal.PersonalInfoModel;
import com.tuya.smart.android.mvp.bean.Result;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

public class PersonalInfoPresenter extends BasePresenter {

    private final Activity mActivity;
    private PersonalInfoModel mPersonalInfoModel;
    private IPersonalInfoView mView;

    public PersonalInfoPresenter(Activity activity, IPersonalInfoView view) {
        super(activity);
        mActivity = activity;
        mView = view;
        mPersonalInfoModel = new PersonalInfoModel(mActivity, mHandler);
    }

    public void reNickName(String titleName) {
        mPersonalInfoModel.reNickName(titleName);
    }

    public String getNickName() {
        return mPersonalInfoModel.getNickName();
    }

    private void saveNickName(String nickName) {
        ProgressUtil.hideLoading();
        mView.reNickName(nickName);
    }

    public void logout() {
        mPersonalInfoModel.logout();
        mView.onLogout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPersonalInfoModel.onDestroy();
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case PersonalInfoModel.RENAME_NICKNAME_ERROR:
                ToastUtil.showToast(mActivity, ((Result) msg.obj).error);
                ProgressUtil.hideLoading();
                break;
            case PersonalInfoModel.RENAME_NICKNAME_SUCCESS:
                saveNickName((String) ((Result) msg.obj).getObj());
                break;
        }

        return super.handleMessage(msg);
    }

    public void resetPassword() {
        User user = TuyaHomeSdk.getUserInstance().getUser();
        int accountType;
        String strAccount;
        if (!TextUtils.isEmpty(user.getEmail())) {
            accountType = AccountConfirmActivity.PLATFORM_EMAIL;
            strAccount = user.getEmail();
            AccountConfirmActivity.gotoAccountConfirmActivityForResult(mActivity, strAccount, user.getPhoneCode(), AccountConfirmActivity.MODE_CHANGE_PASSWORD, accountType, 0);
        }
    }
}
