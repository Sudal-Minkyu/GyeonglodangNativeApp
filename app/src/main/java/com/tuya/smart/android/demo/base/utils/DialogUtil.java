package com.tuya.smart.android.demo.base.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.tuya.smart.android.demo.R;

public class DialogUtil {

    public static void simpleSmartDialog(Context context, CharSequence msg,
                                         final DialogInterface.OnClickListener listener) {
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClick(dialog, which);
            }
        };
        AlertDialog.Builder dialog = UIFactory.buildSmartAlertDialog(context);
        dialog.setPositiveButton(R.string.ty_confirm, onClickListener);
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    public static void simpleConfirmDialog(Context context, String title, CharSequence msg,
                                           final DialogInterface.OnClickListener listener) {
        DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onClick(dialog, which);
            }
        };
        AlertDialog.Builder dialog = UIFactory.buildAlertDialog(context);
        dialog.setNegativeButton(R.string.ty_cancel, onClickListener);
        dialog.setPositiveButton(R.string.ty_confirm, onClickListener);
        dialog.setTitle(title);
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.create().show();
    }

    public static void simpleInputDialog(Context context, String title, CharSequence text,
                                         boolean isHint,
                                         final SimpleInputDialogInterface listener) {
        AlertDialog.Builder dialog = UIFactory.buildAlertDialog(context);
        final EditText inputEditText = (EditText) LayoutInflater.from(context).inflate(
                R.layout.ty_dialog_simple_input, null);
        DialogInterface.OnClickListener onClickListener = (dialog1, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (listener != null) {
                        listener.onPositive(dialog1, inputEditText.getEditableText().toString());
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    if (listener != null) {
                        listener.onNegative(dialog1);
                    }
                    break;
                default:
                    break;
            }
        };
        dialog.setNegativeButton(R.string.ty_cancel, onClickListener);
        dialog.setPositiveButton(R.string.ty_confirm, onClickListener);
        dialog.setTitle(title);
        if (!TextUtils.isEmpty(text)) {
            if (isHint) {
                inputEditText.setHint(text);
            } else {
                inputEditText.setText(text);
            }
        }
        dialog.setView(inputEditText);
        inputEditText.requestFocus();
        dialog.setCancelable(false);
        dialog.create().show();
    }

    public interface SimpleInputDialogInterface {
        void onPositive(DialogInterface dialog, String inputText);
        void onNegative(DialogInterface dialog);
    }

}
