package com.tuya.smart.android.demo.login.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tuya.smart.android.common.utils.ValidatorUtil;
import com.tuya.smart.android.demo.MainActivity;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.activity.BaseActivity;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.base.utils.ProgressUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.login.ILoginView;
import com.tuya.smart.android.demo.login.presenter.LoginPresenter;
import com.tuya.smart.android.demo.utils.Constants;
import com.tuya.smart.android.mvp.bean.Result;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.tuya.smart.android.demo.login.activity.AccountInputActivity.EXTRA_ACCOUNT_INPUT_MODE;
import static com.tuya.smart.android.demo.login.activity.AccountInputActivity.MODE_PASSWORD_FOUND;

/**
 * Created by letian on 16/7/15.
 */
public class LoginActivity extends BaseActivity implements ILoginView, TextWatcher {

    @BindView(R.id.login_submit)
    public Button mLoginSubmit;

    @BindView(R.id.country_name)
    public TextView mCountryName;

    @BindView(R.id.password)
    public EditText mPassword;

    @BindView(R.id.password_switch)
    public ImageButton mPasswordSwitch;
    @BindView(R.id.user_name)
    public TextView mUserName;
    private Unbinder mBind;

    private LoginPresenter mLoginPresenter;

    private boolean passwordOn;
    private Button regBtn;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_DENIED) {
                        // 하나라도 거부한다면 앱실행 불가 메세지 띄움
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                finish();
                                            }
                                        }
                                ).setCancelable(false).show();
                        return;
                    }
                }
                Toast.makeText(this, "권한이 허용 되었습니다!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkVerify();
        checkWhiteListRegist();


        mBind = ButterKnife.bind(this);
        initToolbar();
        initView();
        initTitle();
        disableLogin();

        regBtn = findViewById(R.id.action_login_reg_onclick);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountInputActivity.gotoAccountInputActivity(LoginActivity.this, AccountInputActivity.MODE_REGISTER, 0);
            }
        });

        mLoginPresenter = new LoginPresenter(this, this);
    }


    private void initTitle() {
        setTitle("스마트경로당");
    }

    protected void setDisplayHomeAsUpEnabled() {
        setDisplayHomeAsUpEnabled(R.drawable.tysmart_back_white, null);

    }

    private void initView() {
        passwordOn = false;
        mUserName.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        mPasswordSwitch.setImageResource(R.drawable.ty_password_off);
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }


    // 계정 모니터링 입력
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String userName = mUserName.getText().toString();
        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            disableLogin();
        } else {
            if (ValidatorUtil.isEmail(userName)) {
                // 사서함
                enableLogin();
            } else {
                // 휴대전화 번호
                try {
                    Long.valueOf(userName);
                    enableLogin();
                } catch (Exception e) {
                    disableLogin();
                }
            }
        }
    }

    @Override
    public boolean onPanelKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                exitBy2Click();
                return true;
        }
        return false;
    }

    @OnClick(R.id.option_validate_code)
    public void onClickLoginWithPhoneCode() {
        startActivity(new Intent(LoginActivity.this, LoginWithPhoneActivity.class));
    }

    @OnClick(R.id.option_forget_password)
    public void onClickRetrievePassword() {
        Intent intent = new Intent(LoginActivity.this, AccountInputActivity.class);
        intent.putExtra(EXTRA_ACCOUNT_INPUT_MODE, MODE_PASSWORD_FOUND);
        startActivity(intent);
    }

    @OnClick(R.id.country_name)
    public void onClickSelectCountry() {
        mLoginPresenter.selectCountry();
    }

    @OnClick(R.id.password_switch)
    public void onClickPasswordSwitch() {
        passwordOn = !passwordOn;

        // 디스플레이 아이콘 전환
        if (passwordOn) {
            mPasswordSwitch.setImageResource(R.drawable.ty_password_on);
            mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            mPasswordSwitch.setImageResource(R.drawable.ty_password_off);
            mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        // 커서 위치 업데이트
        if (mPassword.getText().length() > 0) {
            mPassword.setSelection(mPassword.getText().length());
        }
    }

    @OnClick(R.id.login_submit)
    public void onClickLogin() {
        // 로그인
        if (mLoginSubmit.isEnabled()) {
            String userName = mUserName.getText().toString();
            if (!ValidatorUtil.isEmail(userName)) {
                ToastUtil.shortToast(LoginActivity.this, getString(R.string.ty_phone_num_error));
                return;
            }
            hideIMM();
            disableLogin();
            ProgressUtil.showLoading(LoginActivity.this, R.string.logining);
            ActivityUtils.SetString(getBaseContext(), "user_id", userName);
            mLoginPresenter.login(userName, mPassword.getText().toString());
        }
    }

    @Override
    public void setCountry(String name, String code) {
        mCountryName.setText(String.format("%s +%s", name, code));
    }

    @Override
    public void modelResult(int what, Result result) {
        switch (what) {
            case LoginPresenter.MSG_LOGIN_SUCCESS:
                ProgressUtil.hideLoading();
                break;
            case LoginPresenter.MSG_LOGIN_FAILURE:
                ProgressUtil.hideLoading();
                ToastUtil.shortToast(this, result.error);
                enableLogin();
                break;
            default:
                break;
        }
    }

    // 로그인 버튼 상태
    public void enableLogin() {
        if (!mLoginSubmit.isEnabled()) mLoginSubmit.setEnabled(true);
    }

    public void disableLogin() {
        if (mLoginSubmit.isEnabled()) mLoginSubmit.setEnabled(false);
    }

    @Override
    public boolean needLogin() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLoginPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBind.unbind();
        mLoginPresenter.onDestroy();
    }
}
