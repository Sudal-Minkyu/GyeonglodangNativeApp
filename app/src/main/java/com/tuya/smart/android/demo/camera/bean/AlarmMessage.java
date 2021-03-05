package com.tuya.smart.android.demo.camera.bean;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class AlarmMessage extends SugarRecord {
    @Unique
    private Long id;
    private String title;
    private String text;
    private String date;

    public AlarmMessage(String title, String text, String date) {
        this.title = title;
        this.text = text;
        this.date = date;
    }

    public AlarmMessage() {

    }

    public Long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
