package com.tuya.smart.android.demo.base.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tuya.smart.android.common.utils.NetworkUtil;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.presenter.DeviceListFragmentPresenter;
import com.tuya.smart.android.demo.base.utils.AnimationUtil;
import com.tuya.smart.android.demo.base.view.IDeviceListFragmentView;
import com.tuya.smart.android.demo.device.CommonDeviceAdapter;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

public class DeviceListFragment extends BaseFragment implements IDeviceListFragmentView {

    private volatile static DeviceListFragment mDeviceListFragment;
    private View mContentView;
    private DeviceListFragmentPresenter deviceListFragmentPresenter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CommonDeviceAdapter mCommonDeviceAdapter;
    private ListView mDevListView;
    private TextView mNetWorkTip;
    private View mRlView;
    private View mAddDevView;

    public static Fragment newInstance() {
        if (mDeviceListFragment == null) {
            synchronized (DeviceListFragment.class) {
                if (mDeviceListFragment == null) {
                    mDeviceListFragment = new DeviceListFragment();
                }
            }
        }
        return mDeviceListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_device_list, container, false);
        initToolbar(mContentView);
        initView();
        initAdapter();
        initSwipeRefreshLayout();
        return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPresenter();
        deviceListFragmentPresenter.getDataFromServer();
    }

    public void initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary_button_bg_color),
                getResources().getColor(R.color.primary_button_bg_color),
                getResources().getColor(R.color.primary_button_bg_color),
                getResources().getColor(R.color.primary_button_bg_color));
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (NetworkUtil.isNetworkAvailable(getContext())) {
                deviceListFragmentPresenter.getDataFromServer();
            } else {
                loadFinish();
            }
        });
    }

    public void initAdapter() {
        mCommonDeviceAdapter = new CommonDeviceAdapter(getActivity());
        mDevListView.setAdapter(mCommonDeviceAdapter);
        mDevListView.setOnItemLongClickListener((parent, view, position, id) -> deviceListFragmentPresenter.onDeviceLongClick((DeviceBean) parent.getAdapter().getItem(position)));
        mDevListView.setOnItemClickListener((parent, view, position, id) -> deviceListFragmentPresenter.onDeviceClick((DeviceBean) parent.getAdapter().getItem(position)));
    }

    @Override
    public void updateDeviceData(List<DeviceBean> myDevices) {
        Log.e("myDevicesSize : ", String.valueOf(myDevices.size()));
        // 도어벨화면이동
        if (myDevices.size()==0) {
            deviceListFragmentPresenter.gotoDeviceCommonActivity(null);
        }else{
            deviceListFragmentPresenter.gotoDeviceCommonActivity(myDevices.get(0));
        }

        if (mCommonDeviceAdapter != null) {
            mCommonDeviceAdapter.setData(myDevices);
        }
    }

    @Override
    public void loadStart() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    protected void initView() {
        mSwipeRefreshLayout = mContentView.findViewById(R.id.swipe_container);
        mNetWorkTip = mContentView.findViewById(R.id.network_tip);
        mDevListView = mContentView.findViewById(R.id.lv_device_list);
        mRlView = mContentView.findViewById(R.id.rl_list);
        mAddDevView = mContentView.findViewById(R.id.tv_empty_func);
        mAddDevView.setOnClickListener(view -> deviceListFragmentPresenter.addDemoDevice());
    }

    protected void initPresenter() {
        deviceListFragmentPresenter = new DeviceListFragmentPresenter(this, this);
    }

    @Override
    public void loadFinish() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showNetWorkTipView(int tipRes) {
        mNetWorkTip.setText(tipRes);
        if (mNetWorkTip.getVisibility() != View.VISIBLE) {
            AnimationUtil.translateView(mRlView, 0, 0, -mNetWorkTip.getHeight(), 0, 300, false, null);
            mNetWorkTip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideNetWorkTipView() {
        if (mNetWorkTip.getVisibility() != View.GONE) {
            AnimationUtil.translateView(mRlView, 0, 0, mNetWorkTip.getHeight(), 0, 300, false, null);
            mNetWorkTip.setVisibility(View.GONE);
        }
    }

    @Override
    public void gotoCreateHome() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        deviceListFragmentPresenter.onDestroy();
    }

}
