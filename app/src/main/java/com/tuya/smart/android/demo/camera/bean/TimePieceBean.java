package com.tuya.smart.android.demo.camera.bean;

import androidx.annotation.NonNull;

public class TimePieceBean implements Comparable<TimePieceBean> {
    private final int startTime;
    private final int endTime;
    private final int playTime;

    public TimePieceBean(int startTime, int endTime, int playTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.playTime = playTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    @Override
    public int compareTo(@NonNull TimePieceBean o) {
        if(this.endTime >= o.getEndTime()){
            return 1;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "TimePieceBean{" +
                "starttime='" + startTime + '\'' +
                "playTime='" + playTime + '\'' +
                ", endtime='" + endTime + '\'' +
                '}';
    }
}
