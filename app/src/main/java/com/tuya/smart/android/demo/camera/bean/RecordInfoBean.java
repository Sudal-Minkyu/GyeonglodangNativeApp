package com.tuya.smart.android.demo.camera.bean;

import java.util.List;

public class RecordInfoBean {
    private int count;

    private final List<TimePieceBean> items;

    public RecordInfoBean(List<TimePieceBean> items) {
        this.items = items;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<TimePieceBean> getItems() {
        return items;
    }

}
