package com.tuya.smart.android.demo.base.utils;

import android.os.Message;

import com.tuya.smart.android.mvp.bean.Result;

public class MessageUtil {

    public static Message getCallFailMessage(int msgWhat, String errorCode, String errorMsg){
        Message msg = new Message();
        msg.what = msgWhat;
        Result result = new Result();
        result.error = errorMsg;
        result.errorCode = errorCode;
        msg.obj = result;
        return msg;
    }

    public static Message getMessage(int msgWhat, Object msgObj){
        Message msg = new Message();
        msg.what = msgWhat;
        msg.obj = msgObj;
        return msg;
    }

    public static Message getMessage(int msgWhat,int arg1,Object msgObj){
        Message msg = getMessage(msgWhat,msgObj);
        msg.arg1 = arg1;
        return msg;
    }

}
