package com.tuya.smart.android.demo.base.presenter;

import android.app.Activity;

import androidx.fragment.app.Fragment;

import com.tuya.smart.android.demo.base.fragment.DeviceListFragment;
import com.tuya.smart.android.demo.base.fragment.PersonalCenterFragment;
import com.tuya.smart.android.demo.base.utils.CollectionUtils;
import com.tuya.smart.android.demo.base.view.IHomeView;
import com.tuya.smart.android.demo.family.model.FamilyIndexModel;
import com.tuya.smart.android.demo.family.model.IFamilyIndexModel;
import com.tuya.smart.android.mvp.presenter.BasePresenter;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;

import java.util.ArrayList;
import java.util.List;

//import com.tuya.smart.android.demo.base.activity.SplashActivity;

public class HomePresenter extends BasePresenter {

    public static final String TAG = "HomeKitPresenter";

    private IHomeView mHomeView;
    protected Activity mActivity;

    public static final int TAB_MY_DEVICE = 0;
    public static final int TAB_PERSONAL_CENTER = 1;

    protected int mCurrentTab = -1;

    private IFamilyIndexModel mFamilyIndexModel;

    public HomePresenter(IHomeView homeView, Activity ctx) {
        mHomeView = homeView;
        mActivity = ctx;
        mFamilyIndexModel = new FamilyIndexModel(ctx);
    }

    public void checkFamilyCount() {
        mFamilyIndexModel.queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (null == mHomeView) {
                    return;
                }

                if (CollectionUtils.isEmpty(list)) {
//                    Constant.finishActivity();
                    List<String> checkRoomList = new ArrayList<>();
                    String name = "경로당";
                    TuyaHomeSdk.getHomeManagerInstance().createHome(name, 1,2,name,checkRoomList,new ITuyaHomeResultCallback() {
                        @Override
                        public void onError(String errorCode, String errorMsg) {
                            // do something
                        }
                        @Override
                        public void onSuccess(HomeBean bean) {
//                            // do something
//                            Intent intent = new Intent(mActivity, DoorbellActivity.class);
//                            mActivity.startActivity(intent);
                        }
                    });
//                    mHomeView.goToFamilyEmptyActivity();
                }
            }

            @Override
            public void onError(String s, String s1) {

            }
        });
    }

    public void showPersonalCenterPage() {
        showTab(TAB_PERSONAL_CENTER);
    }

    public void showMyDevicePage() {
        showTab(TAB_MY_DEVICE);
    }

    public void showTab(int tab) {
        if (tab == mCurrentTab) {
            return;
        }
        mHomeView.offItem(mCurrentTab);

        mHomeView.onItem(tab);

        mCurrentTab = tab;
    }

    public int getFragmentCount() {
        return 2;
    }

    public Fragment getFragment(int type) {
        if (type == TAB_MY_DEVICE) {
            return DeviceListFragment.newInstance();
        } else if (type == TAB_PERSONAL_CENTER) {
            return PersonalCenterFragment.newInstance();
        }
        return null;
    }

    public int getCurrentTab() {
        return mCurrentTab;
    }

    public void setCurrentTab(int tab) {
        mCurrentTab = tab;
    }
}
