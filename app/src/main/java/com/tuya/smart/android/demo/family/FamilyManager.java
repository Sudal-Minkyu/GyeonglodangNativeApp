package com.tuya.smart.android.demo.family;

import android.util.Log;

import androidx.annotation.NonNull;

import com.tuya.smart.android.demo.base.utils.CollectionUtils;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;

import java.util.List;

public class FamilyManager {

    public static final String TAG = FamilyManager.class.getSimpleName();

    private static volatile FamilyManager instance;

    private HomeBean currentHomeBean;

    private final FamilySpHelper mFamilySpHelper;


    private FamilyManager() {
        mFamilySpHelper = new FamilySpHelper();
    }

    public static FamilyManager getInstance() {
        if (null == instance) {
            synchronized (FamilyManager.class) {
                if (null == instance) {
                    instance = new FamilyManager();
                }
            }
        }
        return instance;
    }

    public void setCurrentHome(HomeBean homeBean) {
        Log.e(TAG, "KMK setCurrentHome - homeBean : "+homeBean);
        if (null == homeBean) {
            return;
        }

        Log.e(TAG, "KMK setCurrentHome - homeBean : "+homeBean);
        if (null != currentHomeBean) {
            long currentHomeId = currentHomeBean.getHomeId();
            long targetHomeId = homeBean.getHomeId();
            Log.e(TAG, "KMK setCurrentHome - currentHomeBean : "+currentHomeBean);
            Log.i(TAG, "KMK setCurrentHome: currentHomeId=" + currentHomeId + " targetHomeId=" + targetHomeId);
        }
        // 메모리 업데이트
        currentHomeBean = homeBean;
        System.out.println("currentHomeBean : "+currentHomeBean);
        mFamilySpHelper.putCurrentHome(currentHomeBean);
    }


    public HomeBean getCurrentHome() {
        if (null == currentHomeBean) {
            Log.e(TAG, "KMK currentHomeBean : 널입니다.");
        }else{
            Log.e(TAG, "KMK currentHomeBean : 널이 아닙니다.");
        }
        return currentHomeBean;
    }


    public long getCurrentHomeId() {
        HomeBean currentHome = getCurrentHome();
//        Log.e(TAG, "KMK currentHome : "+currentHome);
        if (null == currentHome) {
            Log.e(TAG, "KMK currentHome은 널입니다. 토큰이 비어있음.");
            return -1;
        }
        return currentHome.getHomeId();
    }


    public void getHomeList(@NonNull final ITuyaGetHomeListCallback callback) {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (!CollectionUtils.isEmpty(list) && null == getCurrentHome()) {
                    setCurrentHome(list.get(0));
                }
                callback.onSuccess(list);
            }

            @Override
            public void onError(String s, String s1) {
                callback.onError(s, s1);
            }
        });
    }


    public void createHome(String homeName,
                           List<String> roomList,
                           @NonNull final ITuyaHomeResultCallback callback) {
        TuyaHomeSdk.getHomeManagerInstance().createHome(homeName,
                0, 0, "", roomList, new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean homeBean) {
                        setCurrentHome(homeBean);
                        callback.onSuccess(homeBean);
                    }

                    @Override
                    public void onError(String s, String s1) {
                        callback.onError(s, s1);
                    }
                });
    }




}
