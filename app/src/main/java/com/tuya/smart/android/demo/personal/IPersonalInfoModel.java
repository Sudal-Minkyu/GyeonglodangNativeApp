package com.tuya.smart.android.demo.personal;


import com.tuya.smart.android.mvp.model.IModel;

public interface IPersonalInfoModel extends IModel {
    String getNickName();

    void reNickName(String nickName);

    void logout();
}
