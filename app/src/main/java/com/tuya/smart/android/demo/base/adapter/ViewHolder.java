package com.tuya.smart.android.demo.base.adapter;

import android.view.View;

public abstract class ViewHolder<T> {

    protected View contentView;

    public ViewHolder(View contentView) {
        this.contentView = contentView;
    }

    public abstract void initData(T data);

}
