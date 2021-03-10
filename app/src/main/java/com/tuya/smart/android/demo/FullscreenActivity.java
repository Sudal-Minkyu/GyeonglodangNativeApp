package com.tuya.smart.android.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.tuya.drawee.view.DecryptImageView;
import com.tuya.smart.android.demo.base.activity.DoorbellActivity;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.utils.DateUtils;
import com.tuya.smart.android.demo.utils.TimeZoneUtils;
import com.tuya.smart.android.network.Business;
import com.tuya.smart.android.network.http.BusinessResponse;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageBean;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageClassifyBean;
import com.tuya.smart.ipc.messagecenter.business.CameraMessageBusiness;

import org.eclipse.paho.client.mqttv3.util.Strings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_DEVID;
import static com.tuya.smart.android.demo.utils.Constants.ALARM_DETECTION_DATE_MONTH_FAILED;
import static com.tuya.smart.android.demo.utils.Constants.ALARM_DETECTION_DATE_MONTH_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.MOTION_CLASSIFY_FAILED;
import static com.tuya.smart.android.demo.utils.Constants.MOTION_CLASSIFY_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.MSG_GET_ALARM_DETECTION;

// 전체화면 알람
public class FullscreenActivity extends Activity {

    private static final String TAG = FullscreenActivity.class.getSimpleName();
    private DecryptImageView mSnapshot;

    private void ShowPicture(CameraMessageBean cameraMessageBean) {
        String attachPics = cameraMessageBean.getAttachPics();
        mSnapshot.setVisibility(View.VISIBLE);
        if (!Strings.isEmpty(attachPics)) {
            if (attachPics.contains("@")) {
                int index = attachPics.lastIndexOf("@");
                try {
                    String decryption = attachPics.substring(index + 1);
                    String imageUrl = attachPics.substring(0, index);
                    mSnapshot.setImageURI(imageUrl, decryption.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Uri uri = null;
                try {
                    uri = Uri.parse(attachPics);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DraweeController controller = Fresco.newDraweeControllerBuilder().setUri(uri).build();
                mSnapshot.setController(controller);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //진동울리기

        initVibrator();
        initMedia();
    }

    /**
     * 미디어플레이어 초기화
     */
    private void initMedia() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.doorbell);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    @Override
    public void onUserLeaveHint() {
        mediaPlayer.stop();
        hasVibrator = false;
    }

    /**
     * 진동 초기화
     */
    private void initVibrator() {
        ShowVibrator(false);
        ShowVibrator(true);
    }

    /**
     * 진동 활성화 여부
     *
     * @param isShowing 활성화 여부
     */
    private void ShowVibrator(boolean isShowing) {
        if (isShowing) {
            EnableVibrator();
        } else {
            DisableVibrator();
        }
    }

    private boolean hasVibrator; // 진동여부

    /**
     * 진동 활성화
     */
    private void EnableVibrator() {
        if (!hasVibrator) {
            hasVibrator = true;
            ((TuyaSmartApp) TuyaSmartApp.getAppContext()).vibrator.vibrate(new long[]{100, 1000, 100, 500, 100, 500, 100, 1000}, 0); // 무한진동오게하기
        }
    }

    /**
     * 진동 비 활성화
     */
    private void DisableVibrator() {
        if (((TuyaSmartApp) TuyaSmartApp.getAppContext()) != null)
            ((TuyaSmartApp) TuyaSmartApp.getAppContext()).vibrator.cancel();
    }

    @Override
    protected void onPause() {
        super.onPause();
        DisableVibrator();
    }

    private MediaPlayer mediaPlayer;
    private BroadcastReceiver scrOffReceiver;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_fullscreen);

        mSnapshot = (DecryptImageView) findViewById(R.id.call_Icon);
        devId = getIntent().getStringExtra(INTENT_DEVID);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Notify();

        scrOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    System.out.println("인텐트 : " + intent.getAction());
                    System.out.println("전원버튼 클릭 (꺼짐)");
                    mediaPlayer.stop();
                    hasVibrator = false;
                }
            }
        };

        // TODO: IntentFilter에 Action 등록
        IntentFilter scrFilter = new IntentFilter();
        scrFilter.addAction(Intent.ACTION_SCREEN_OFF);
        scrFilter.addAction(Intent.ACTION_SCREEN_ON);
        scrFilter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
        registerReceiver(scrOffReceiver, scrFilter);

        Button cancelBtn = findViewById(R.id.fullscreen_cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DisableVibrator();

                mediaPlayer.stop();
                mediaPlayer.reset();

                Constant.finishActivity();
                finish();
            }
        });

        // 문 열기 버튼에 해당 액션 연결 필요
        Button openBtn = findViewById(R.id.fullscreen_open_btn);
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.finishActivity();
                finish();
                DisableVibrator();

                mediaPlayer.stop();
                mediaPlayer.reset();
                Intent intent = new Intent(FullscreenActivity.this, DoorbellActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
    }

    @Override
    public void onBackPressed() {
    }


    /**
     * huangdaju
     * 2019-11-19
     **/

    private String devId;
    private List<CameraMessageBean> mWaitingDeleteCameraMessageList;
    protected List<CameraMessageBean> mCameraMessageList;
    private CameraMessageBusiness messageBusiness;
    private CameraMessageClassifyBean selectClassify;
    private int day, year, month;
    private int offset = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALARM_DETECTION_DATE_MONTH_FAILED:
                    break;
                case ALARM_DETECTION_DATE_MONTH_SUCCESS:
                    handlAlarmDetectionDateSuccess(msg);
                    break;
                case MSG_GET_ALARM_DETECTION:
                    handleAlarmDetection();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    private void handleAlarmDetection() {
        Log.d(TAG, "handleAlarmDetection size " + mCameraMessageList.size());
        if (mCameraMessageList.size() > 0) {
            ShowPicture(mCameraMessageList.get(0));
        }
    }

    private void handlAlarmDetectionDateSuccess(Message msg) {
        if (null != messageBusiness) {
            long time = DateUtils.getCurrentTime(year, month, day);
            long startTime = DateUtils.getTodayStart(time);
            long endTime = DateUtils.getTodayEnd(time) - 1L;
            offset = 0;
            com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
            object.put("msgSrcId", devId);
            object.put("startTime", startTime);
            object.put("endTime", endTime);
            object.put("msgType", 4);
            object.put("limit", 30);
            object.put("keepOrig", true);
            object.put("offset", offset);
            if (null != selectClassify) {
                object.put("msgCodes", selectClassify.getMsgCode());
            }
            messageBusiness.getAlarmDetectionMessageList(object.toJSONString(), new Business.ResultListener<com.alibaba.fastjson.JSONObject>() {
                @Override
                public void onFailure(BusinessResponse businessResponse, com.alibaba.fastjson.JSONObject jsonObject, String s) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                }

                @Override
                public void onSuccess(BusinessResponse businessResponse, com.alibaba.fastjson.JSONObject jsonObject, String s) {
                    List<CameraMessageBean> msgList;
                    try {
                        msgList = JSONArray.parseArray(jsonObject.getString("datas"), CameraMessageBean.class);
                    } catch (Exception e) {
                        msgList = null;
                    }
                    if (msgList != null) {
                        offset = msgList.size();
                        mCameraMessageList = msgList;


                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_SUCCESS));
                    } else {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                    }
                }
            });
        }
    }


    private void Notify() {
        mWaitingDeleteCameraMessageList = new ArrayList<>();
        mCameraMessageList = new ArrayList<>();
        messageBusiness = new CameraMessageBusiness();
        queryCameraMessageClassify(devId);
        queryAlarmDetectionByMonth();


    }

    public void queryCameraMessageClassify(String devId) {
        if (messageBusiness != null) {
            messageBusiness.queryAlarmDetectionClassify(devId, new Business.ResultListener<ArrayList<CameraMessageClassifyBean>>() {
                @Override
                public void onFailure(BusinessResponse businessResponse, ArrayList<CameraMessageClassifyBean> cameraMessageClassifyBeans, String s) {
                    mHandler.sendEmptyMessage(MOTION_CLASSIFY_FAILED);
                }

                @Override
                public void onSuccess(BusinessResponse businessResponse, ArrayList<CameraMessageClassifyBean> cameraMessageClassifyBeans, String s) {
                    selectClassify = cameraMessageClassifyBeans.get(0);
                    mHandler.sendEmptyMessage(MOTION_CLASSIFY_SUCCESS);
                }
            });
        }
    }


    private void queryAlarmDetectionByMonth() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        String inputStr = simpleDateFormat.format(date);
        if (TextUtils.isEmpty(inputStr)) {
            ToastUtil.shortToast(this, "input query date");
            return;
        }
        String[] substring = inputStr.split("/");
        year = Integer.parseInt(substring[0]);
        month = Integer.parseInt(substring[1]);
        day = Integer.parseInt(substring[2]);
        com.alibaba.fastjson.JSONObject object = new JSONObject();
        object.put("msgSrcId", devId);
        object.put("timeZone", TimeZoneUtils.getTimezoneGCMById(TimeZone.getDefault().getID()));

        String monthDate = year + "-" + month;
        object.put("month", monthDate);


        messageBusiness.queryAlarmDetectionDaysByMonth(object.toJSONString(),
                new Business.ResultListener<JSONArray>() {
                    @Override
                    public void onFailure(BusinessResponse businessResponse, JSONArray objects, String s) {
                        mHandler.sendEmptyMessage(ALARM_DETECTION_DATE_MONTH_FAILED);
                        Log.d(TAG, "error : " + s);

                    }

                    @Override
                    public void onSuccess(BusinessResponse businessResponse, JSONArray objects, String s) {
                        Log.d(TAG, "success : " + s);

                        mHandler.sendEmptyMessage(ALARM_DETECTION_DATE_MONTH_SUCCESS);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        hasVibrator = false; // 진동상태 해제

        if(scrOffReceiver!=null) {
            unregisterReceiver(scrOffReceiver);
        }

        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (null != messageBusiness) {
            messageBusiness.onDestroy();
        }
    }
}