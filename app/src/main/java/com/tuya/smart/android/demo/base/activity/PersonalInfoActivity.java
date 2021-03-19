package com.tuya.smart.android.demo.base.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.presenter.PersonalInfoPresenter;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.base.utils.DialogUtil;
import com.tuya.smart.android.demo.base.utils.LoginHelper;
import com.tuya.smart.android.demo.base.utils.ProgressUtil;
import com.tuya.smart.android.demo.personal.IPersonalInfoView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PersonalInfoActivity extends BaseActivity implements IPersonalInfoView {
    private static final String TAG = PersonalInfoActivity.class.getSimpleName();
    private PersonalInfoPresenter mPersonalInfoPresenter;

    @BindView(R.id.tv_renickname)
    public TextView mNickName;

    @BindView(R.id.tv_topic)
    public TextView mTopic;

    @BindView(R.id.tv_login_id)
    public TextView mUserId;


    private Unbinder mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);
        mBind = ButterKnife.bind(this);
        initToolbar();
        initTitle();
        initPresenter();
        initData();
    }

    private void initTitle() {
        setTitle(getString(R.string.personal_center));
        setDisplayHomeAsUpEnabled();
    }

    private void initPresenter() {
        mPersonalInfoPresenter = new PersonalInfoPresenter(this, this);
    }

    private void initData() {
        mNickName.setText(mPersonalInfoPresenter.getNickName());

        mUserId.setText(ActivityUtils.GetString(getBaseContext(), "user_id"));
        String topicKey = ActivityUtils.GetString(getBaseContext(), "topic_key");
        if (topicKey == null || topicKey.trim().equals("")) {
            ActivityUtils.SetString(getBaseContext(), "topic_key", "jb");
            mTopic.setText("jb");
        } else {
            mTopic.setText(topicKey);

        }
    }

    @OnClick(R.id.rl_renickname)
    public void onClickRenickname() {
        DialogUtil.simpleInputDialog(this, getString(R.string.reNickName), mNickName.getText().toString(), false, new DialogUtil.SimpleInputDialogInterface() {
            @Override
            public void onPositive(DialogInterface dialog, String inputText) {
                mPersonalInfoPresenter.reNickName(inputText);
            }

            @Override
            public void onNegative(DialogInterface dialog) {

            }
        });
    }


    @OnClick(R.id.rl_topic)
    public void onClickReTopic() {
        DialogUtil.simpleInputDialog(this, getString(R.string.reTopic), mTopic.getText().toString(), false, new DialogUtil.SimpleInputDialogInterface() {
            @Override
            public void onPositive(DialogInterface dialog, String inputText) {
                if (inputText == null || inputText.trim().equals("")) {
                    DialogUtil.simpleSmartDialog(PersonalInfoActivity.this, "값을 입력해주세요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                        }
                    });
                } else {
                    mTopic.setText(inputText);
                    ActivityUtils.SetString(getBaseContext(), "topic_key", inputText);
                }

            }

            @Override
            public void onNegative(DialogInterface dialog) {

            }
        });
    }

    @OnClick(R.id.btn_passwordchange)
    public void onClickResetPassword() {
        mPersonalInfoPresenter.resetPassword();
    }

    @OnClick(R.id.btn_logout)
    public void logout() {
        DialogUtil.simpleConfirmDialog(PersonalInfoActivity.this, "로그아웃", "로그아웃 하시겠습니까?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (DialogInterface.BUTTON_POSITIVE == i) {
                    ProgressUtil.showLoading(PersonalInfoActivity.this, R.string.ty_logout_loading);
                    mPersonalInfoPresenter.logout();
                }
                dialogInterface.dismiss();
            }
        });
    }

    @Override
    public void reNickName(String nickName) {
        mNickName.setText(nickName);
    }

    @Override
    public void onLogout() {
        LoginHelper.reLogin(this, false);
        ProgressUtil.hideLoading();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBind.unbind();
        mPersonalInfoPresenter.onDestroy();
    }

}
