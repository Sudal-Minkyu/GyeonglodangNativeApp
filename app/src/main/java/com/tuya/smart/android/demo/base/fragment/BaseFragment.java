package com.tuya.smart.android.demo.base.fragment;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.tuya.smart.android.demo.R;

public abstract class BaseFragment extends Fragment {

    protected Toolbar mToolBar;

    protected void initToolbar(View contentView) {
        if (mToolBar == null) {
            mToolBar = contentView.findViewById(R.id.toolbar_top_view);
            if (mToolBar != null) {
                TypedArray a = getActivity().obtainStyledAttributes(new int[]{
                        R.attr.status_font_color});
                int titleColor = a.getInt(0, Color.WHITE);
                mToolBar.setTitleTextColor(titleColor);
            }
        }
    }

    protected void setTitle(String title) {
        if (mToolBar != null) {
            mToolBar.setTitle(title);
        }
    }

    protected void setMenu(int resId, Toolbar.OnMenuItemClickListener listener) {
        if (mToolBar != null) {
            mToolBar.inflateMenu(resId);
            mToolBar.setOnMenuItemClickListener(listener);
        }
    }

    protected void setDisplayHomeAsUpEnabled() {
        if (mToolBar != null) {
            mToolBar.setNavigationIcon(R.drawable.tysmart_back);
            mToolBar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        }
    }

    protected void setDisplayHomeAsUpEnabled(final View.OnClickListener listener) {
        if (mToolBar != null) {
            mToolBar.setNavigationIcon(R.drawable.tysmart_back);
            mToolBar.setNavigationOnClickListener(listener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mToolBar = null;
    }
}
