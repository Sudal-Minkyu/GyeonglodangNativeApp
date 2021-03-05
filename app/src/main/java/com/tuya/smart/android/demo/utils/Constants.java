package com.tuya.smart.android.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * huangdaju
 * 2018/12/17
 **/

public class Constants {

    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;
    public static final int EXTERNAL_AUDIO_REQ_CODE = 11;

    public static final int ARG1_OPERATE_SUCCESS = 0;
    public static final int ARG1_OPERATE_FAIL = 1;
    public static final int DEVICENOT = 2;

    public static final int MSG_CONNECT = 2033;
    public static final int MSG_CREATE_DEVICE = 2099;
    public static final int MSG_GET_CLARITY = 2054;

    public static final int MSG_TALK_BACK_FAIL = 2021;
    public static final int MSG_TALK_BACK_BEGIN = 2022;
    public static final int MSG_TALK_BACK_OVER = 2023;
    public static final int MSG_DATA_DATE = 2035;

    public static final int MSG_MUTE = 2024;
    public static final int MSG_SCREENSHOT = 2017;

    public static final int MSG_VIDEO_RECORD_FAIL = 2018;
    public static final int MSG_VIDEO_RECORD_BEGIN = 2019;
    public static final int MSG_VIDEO_RECORD_OVER = 2020;
    public static final int MSG_CAMERA_REFRESH = 20210;


    public static final int MSG_DATA_DATE_BY_DAY_SUCC = 2045;
    public static final int MSG_DATA_DATE_BY_DAY_FAIL = 2046;

    public static final int ALARM_DETECTION_DATE_MONTH_FAILED = 2047;
    public static final int ALARM_DETECTION_DATE_MONTH_SUCCESS = 2048;
    public static final int MSG_GET_ALARM_DETECTION = 2049;
    public static final int MOTION_CLASSIFY_FAILED = 2050;
    public static final int MOTION_CLASSIFY_SUCCESS = 2051;
    public static final int MSG_DELETE_ALARM_DETECTION = 2052;

    public synchronized static boolean hasStoragePermission() {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "a.log";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                boolean iscreate = file.createNewFile();
                if (iscreate) {
                    file.delete();
                    return true;
                } else {
                    return false;
                }
            } else {
                file.delete();
                return false;
            }
        } catch (Exception e) {
            return false;
        }

    }
    public synchronized static boolean requestPermission(Context context, String permission, int requestCode, String tip) {
        //현재 활동이 권한을 얻었는지 확인
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

            // 사용자가 앱 권한 요청을 거부 한 경우 여기에서 사용자에게 설명해야합니다.
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    permission)) {
                Toast.makeText(context, tip, Toast.LENGTH_SHORT).show();
            } else {
                // 권한 요청하기
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{permission},
                        requestCode);
                return false;
            }

            return false;
        } else {
            return true;
        }
    }

    public static boolean hasRecordPermission() {
        int minBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        int bufferSizeInBytes = 640;
        byte[] audioData = new byte[bufferSizeInBytes];
        int readSize = 0;
        AudioRecord audioRecord = null;
        try {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 8000,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
            // 녹음 시작
            audioRecord.startRecording();
        } catch (Exception e) {
            // 가능한 상황 1
            if (audioRecord != null) {
                audioRecord.release();
                audioRecord = null;
            }
            return false;
        }
        // 녹화 중인지 확인하고 6.0 미만이면이 상태로 돌아갑니다.
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            // 가능한 상황 2
            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            return false;
        } else {// 녹음
            readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
            // 녹화 결과를 얻을 수 있는지 확인
            if (readSize <= 0) {
                // 가능한 상황 3
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                Log.e("녹화권한 : ", "녹화 데이터가없고 녹화 권한이 없습니다.");
                return false;
            } else {
                // 권한이 있고 정상적으로 기록을 시작하고 데이터가 있습니다.
                if (audioRecord != null) {
                    audioRecord.stop();
                    audioRecord.release();
                    audioRecord = null;
                }
                return true;
            }
        }
    }
}
