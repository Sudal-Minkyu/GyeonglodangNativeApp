package com.tuya.smart.android.demo.login.presenter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.widget.Toast;

import com.tuya.smart.android.base.utils.PreferencesUtil;
import com.tuya.smart.android.demo.MainActivity;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.utils.DialogUtil;
import com.tuya.smart.android.demo.camera.CameraPanelActivity;
import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
import com.tuya.smart.android.demo.family.presenter.FamilyAddPresenter;
import com.tuya.smart.android.demo.login.activity.AccountConfirmActivity;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.base.utils.LoginHelper;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.android.demo.login.IAccountConfirmView;
import com.tuya.smart.android.demo.login.activity.LoginActivity;
import com.tuya.smart.android.mvp.bean.Result;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.api.IRegisterCallback;
import com.tuya.smart.android.user.api.IResetPasswordCallback;
import com.tuya.smart.android.user.api.IValidateCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHomeStatusListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tuya.smart.android.demo.login.activity.AccountConfirmActivity.MODE_REGISTER;

/**
 * Created by lee on 16/6/3.
 */
public class AccountConfirmPresenter extends BasePresenter {
    private static final String TAG = "AccountConfirmPresenter";

    public static final int MSG_SEND_VALIDATE_CODE_SUCCESS = 12;
    public static final int MSG_SEND_VALIDATE_CODE_ERROR = 13;
    public static final int MSG_RESET_PASSWORD_SUCC = 14;
    public static final int MSG_RESET_PASSWORD_FAIL = 15;
    public static final int MSG_REGISTER_SUCC = 16;
    public static final int MSG_REGISTER_FAIL = 17;
    public static final int MSG_LOGIN_FAIL = 18;

    private static final int GET_VALIDATE_CODE_PERIOD = 60 * 1000;

    private IAccountConfirmView mView;


    private String mCountryCode;
    private String mPhoneNum;
    private String mEmail;

    protected boolean mSend;
    private CountDownTimer mCountDownTimer;
    private Activity mContext;

    IResultCallback iResultCallback = new IResultCallback() {
        @Override
        public void onError(String s, String s1) {
            getValidateCodeFail(s, s1);
        }

        @Override
        public void onSuccess() {
            mHandler.sendEmptyMessage(MSG_SEND_VALIDATE_CODE_SUCCESS);
        }
    };
    private IValidateCallback mIValidateCallback = new IValidateCallback() {
        @Override
        public void onSuccess() {
            mHandler.sendEmptyMessage(MSG_SEND_VALIDATE_CODE_SUCCESS);
        }

        @Override
        public void onError(String s, String s1) {
            getValidateCodeFail(s, s1);
        }
    };

    private IResetPasswordCallback mIResetPasswordCallback = new IResetPasswordCallback() {
        @Override
        public void onSuccess() {
            mHandler.sendEmptyMessage(MSG_RESET_PASSWORD_SUCC);
        }

        @Override
        public void onError(String errorCode, String errorMsg) {
            Message msg = MessageUtil.getCallFailMessage(MSG_RESET_PASSWORD_FAIL, errorCode, errorMsg);
            mHandler.sendMessage(msg);
        }
    };

    private IRegisterCallback mIRegisterCallback = new IRegisterCallback() {
        @Override
        public void onSuccess(User user) {
            mHandler.sendEmptyMessage(MSG_REGISTER_SUCC);
        }

        @Override
        public void onError(String errorCode, String errorMsg) {
            Message msg = MessageUtil.getCallFailMessage(MSG_REGISTER_FAIL, errorCode, errorMsg);
            mHandler.sendMessage(msg);
        }
    };

    private ILoginCallback mILoginCallback = new ILoginCallback() {
        @Override
        public void onSuccess(User user) {
            loginSuccess();
        }

        public void onError(String errorCode, String errorMsg) {
            Message msg = MessageUtil.getCallFailMessage(MSG_LOGIN_FAIL, errorCode, errorMsg);
            mHandler.sendMessage(msg);
        }
    };


    public AccountConfirmPresenter(Activity activity, IAccountConfirmView validateCodeView) {
        super(activity);
        mView = validateCodeView;
        mContext = activity;
        initData(activity);
        if (mView.getMode() == AccountConfirmActivity.MODE_FORGET_PASSWORD
                || (mView.getMode() == MODE_REGISTER && mView.getPlatform() == AccountConfirmActivity.PLATFORM_PHONE)) {
            getValidateCode();
        }
    }


    private void initData(Activity activity) {
        mCountryCode = activity.getIntent().getStringExtra(AccountConfirmActivity.EXTRA_COUNTRY_CODE);
        if (TextUtils.isEmpty(mCountryCode)) {
            // 정상적인 상황에서는 어느 입구에 있든 국가 코드를 가져와야합니다.
            // 여기서 기본 구성은 예측할 수 없습니다.
            mCountryCode = "86";
        }
        String account = activity.getIntent().getStringExtra(AccountConfirmActivity.EXTRA_ACCOUNT);
        String showAccount;
        if (mView.getPlatform() == AccountConfirmActivity.PLATFORM_PHONE) {
            mPhoneNum = account;
            showAccount = mCountryCode + "-" + mPhoneNum;
        } else {
            mEmail = account;
            showAccount = mEmail;
        }

        String tip;

        if (mView.getMode() == AccountConfirmActivity.MODE_CHANGE_PASSWORD) {
            tip = getTip(R.string.ty_current_bind_phone_tip, showAccount);
        } else {
            if (mView.getPlatform() == AccountConfirmActivity.PLATFORM_PHONE) {
                tip = getTip(R.string.code_has_send_to_phone, showAccount);
            } else {
                tip = getTip(R.string.code_has_send_to_email, showAccount);
            }
        }

        mView.setValidateTip(Html.fromHtml(tip));
    }

    private String getTip(int tipResId, String showAccount) {
        return "<font color=\"#626262\">" + mContext.getString(tipResId) + "</font>"
                + "<br><font color=\"#ff0000\">" + showAccount + "</font>";
    }

    public void getValidateCode() {
        mSend = true;
        buildCountDown();
        if (mView == null) {
            return;
        }

        switch (mView.getPlatform()) {
            case AccountConfirmActivity.PLATFORM_EMAIL:
                if (mView.getMode() == MODE_REGISTER){
                    TuyaHomeSdk.getUserInstance().getRegisterEmailValidateCode(mCountryCode,mEmail,iResultCallback);
                }else {
                    TuyaHomeSdk.getUserInstance().getEmailValidateCode(mCountryCode, mEmail, mIValidateCallback);

                }
                break;

            case AccountConfirmActivity.PLATFORM_PHONE:
                TuyaHomeSdk.getUserInstance().getValidateCode(mCountryCode, mPhoneNum, mIValidateCallback);
                break;
        }

    }



    public void confirm() {
        // 암호 규칙 판단 추가 (개발자 배경과 일치, 6-20 자, 문자 / 숫자 / 기호 허용)
        if (mView.getPassword().length() < 6 || mView.getPassword().length() > 20) {
            // ToastUtil.shortToast(mContext, "비밀번호 길이는 6-20자 사이여야합니다.");
            DialogUtil.simpleSmartDialog(mContext, mContext.getString(R.string.ty_enter_keyword_tip), null);
            return;
        }

        switch (mView.getMode()) {
            case AccountConfirmActivity.MODE_CHANGE_PASSWORD:
                resetPassword();
                break;
            case AccountConfirmActivity.MODE_FORGET_PASSWORD:
                resetPassword();
                break;
            case MODE_REGISTER:
                register();
                break;
        }
    }

    private void register() {
        TuyaHomeSdk.getUserInstance().registerAccountWithEmail(mCountryCode, mEmail, mView.getPassword(), mView.getValidateCode(),new IRegisterCallback(){
            @Override
            public void onSuccess (User user) {
                Toast.makeText (mContext, "회원가입 성공, 로그인 해주세요.", Toast.LENGTH_SHORT).show ();
                LoginHelper.reLogin(mContext, false);
            }
            @Override
            public void onError (String code, String error) {
                Toast.makeText (mContext, "회원가입 실패, 다시 시도해주세요." + error, Toast.LENGTH_SHORT).show ();
                return;
            }
        });
//        switch (mView.getPlatform()) {
//            case AccountConfirmActivity.PLATFORM_EMAIL:
//                System.out.println("이메일 가입완료");
//                TuyaHomeSdk.getUserInstance().registerAccountWithEmail(mCountryCode, mEmail, mView.getPassword(), mView.getValidateCode(),mIRegisterCallback);
//                break;
//            case AccountConfirmActivity.PLATFORM_PHONE:
//                TuyaHomeSdk.getUserInstance().registerAccountWithPhone(mCountryCode, mPhoneNum, mView.getPassword(), mView.getValidateCode(), mIRegisterCallback);
//                break;
//        }
    }

    private void resetPassword() {
        switch (mView.getPlatform()) {
            case AccountConfirmActivity.PLATFORM_EMAIL:
                TuyaHomeSdk.getUserInstance().resetEmailPassword(mCountryCode, mEmail, mView.getValidateCode(), mView.getPassword(), mIResetPasswordCallback);
                break;

            case AccountConfirmActivity.PLATFORM_PHONE:
                TuyaHomeSdk.getUserInstance().resetPhonePassword(mCountryCode, mPhoneNum, mView.getValidateCode(), mView.getPassword(), mIResetPasswordCallback);
                break;
        }
    }

    private void loginWithPhoneCode() {
        TuyaHomeSdk.getUserInstance().loginWithPhone(mCountryCode, mPhoneNum, mView.getValidateCode(), mILoginCallback);
    }

    private void loginWithPassword() {
        if (mView.getPlatform() == AccountConfirmActivity.PLATFORM_PHONE) {
            if (mView.getMode() == AccountConfirmActivity.MODE_FORGET_PASSWORD) {
                TuyaHomeSdk.getUserInstance().loginWithPhonePassword(mCountryCode, mPhoneNum, mView.getPassword(), mILoginCallback);
            } else {
                TuyaHomeSdk.getUserInstance().loginWithPhonePassword(mCountryCode, mPhoneNum, mView.getPassword(), mILoginCallback);
            }

        } else {
            TuyaHomeSdk.getUserInstance().loginWithEmail(mCountryCode, mEmail, mView.getPassword(), mILoginCallback);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SEND_VALIDATE_CODE_SUCCESS:
                mView.modelResult(msg.what, null);
                break;

            case MSG_SEND_VALIDATE_CODE_ERROR:
            case MSG_RESET_PASSWORD_FAIL:
            case MSG_LOGIN_FAIL:
                mView.modelResult(msg.what, (Result) msg.obj);
                break;

            case MSG_REGISTER_FAIL:
                Result result = (Result) msg.obj;
                if ("IS_EXISTS".equals(result.getErrorCode())) {
                    if (AccountConfirmActivity.PLATFORM_PHONE == mView.getPlatform()) {
                        DialogUtil.simpleConfirmDialog(mContext, mContext.getString(R.string.user_exists), mContext.getString(R.string.direct_login),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            loginWithPhoneCode();
                                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                                            resetPassword();
                                        }
                                    }
                                });
                    } else {
                        mView.modelResult(msg.what, (Result) msg.obj);
                    }
                } else {
                    mView.modelResult(msg.what, (Result) msg.obj);
                }
                break;

            case MSG_RESET_PASSWORD_SUCC:
                if (mView.getMode() == AccountConfirmActivity.MODE_CHANGE_PASSWORD) {
                    DialogUtil.simpleSmartDialog(mContext, R.string.modify_password_success, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoginHelper.reLogin(mContext, false);
                        }
                    });

                } else {
                    // 找回/更换 密码成功直接进入app
                    loginWithPassword();
                }

                break;

            case MSG_REGISTER_SUCC:
                loginSuccess();
                break;
        }
        return super.handleMessage(msg);
    }

    private void loginSuccess() {
        Constant.finishActivity();
        //LoginHelper.afterLogin(); //CIS삭제
        ActivityUtils.gotoMainActivity(mContext);
    }


    protected void getValidateCodeFail(String errorCode, String errorMsg) {
        Message msg = MessageUtil.getCallFailMessage(MSG_SEND_VALIDATE_CODE_ERROR, errorCode, errorMsg);
        mHandler.sendMessage(msg);
        mSend = false;
    }

    public boolean isSended() {
        return mSend;
    }

    private void buildCountDown() {
        mCountDownTimer = new Countdown(GET_VALIDATE_CODE_PERIOD, 1000);
        mCountDownTimer.start();
        mView.disableGetValidateCode();
    }

    private class Countdown extends CountDownTimer {

        public Countdown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mView.setCountdown((int) (millisUntilFinished / 1000));
        }

        @Override
        public void onFinish() {
            mView.enableGetValidateCode();
            mSend = false;
            mView.checkValidateCode();
        }
    }

}
