package com.tuya.smart.android.demo.base.utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.tuya.smart.android.demo.R;

public class UIFactory {

    public static AlertDialog.Builder buildAlertDialog(Context context) {
        return new AlertDialog.Builder(context, R.style.Dialog_Alert);
    }

    public static AlertDialog.Builder buildSmartAlertDialog(Context context) {
        return new AlertDialog.Builder(context, R.style.Dialog_Alert_NoTitle);
    }

}
