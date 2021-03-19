package com.tuya.smart.android.demo.base.utils;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.device.bean.BitmapSchemaBean;
import com.tuya.smart.android.device.bean.EnumSchemaBean;
import com.tuya.smart.android.device.bean.StringSchemaBean;
import com.tuya.smart.android.device.bean.ValueSchemaBean;

public class SchemaMapper {

    public static EnumSchemaBean toEnumSchema(String data) {
        return JSONObject.parseObject(data, EnumSchemaBean.class);
    }


    public static StringSchemaBean toStringSchema(String data) {
        return JSONObject.parseObject(data, StringSchemaBean.class);
    }


    public static ValueSchemaBean toValueSchema(String data) {
        return JSONObject.parseObject(data, ValueSchemaBean.class);
    }

    public static BitmapSchemaBean toBitmapSchema(String data) {
        return JSONObject.parseObject(data, BitmapSchemaBean.class);
    }

}
