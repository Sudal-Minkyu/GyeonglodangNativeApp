package com.tuya.smart.android.demo.camera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.github.florent37.tutoshowcase.TutoShowcase;
import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.MainActivity;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.activity.BaseActivity;
import com.tuya.smart.android.demo.base.activity.ImageViewActivity;
import com.tuya.smart.android.demo.base.activity.PersonalInfoActivity;
import com.tuya.smart.android.demo.base.presenter.PersonalInfoPresenter;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.android.demo.base.utils.ProgressUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.camera.awsiot.MyTopic;
import com.tuya.smart.android.demo.config.AddDeviceTypeActivity;
import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
import com.tuya.smart.android.demo.personal.IPersonalInfoView;
import com.tuya.smart.android.demo.utils.Constants;
import com.tuya.smart.camera.camerasdk.typlayer.callback.AbsP2pCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnRenderDirectionCallback;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
import com.tuya.smart.camera.middleware.p2p.TuyaSmartCameraP2PFactory;
import com.tuya.smart.camera.middleware.widget.AbsVideoViewCallback;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.camera.utils.AudioUtils;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;
import com.tuyasmart.camera.devicecontrol.api.ITuyaCameraDeviceControlCallback;
import com.tuyasmart.camera.devicecontrol.bean.DpPIRSwitch;
import com.tuyasmart.camera.devicecontrol.bean.DpPTZControl;
import com.tuyasmart.camera.devicecontrol.bean.DpPTZStop;
import com.tuyasmart.camera.devicecontrol.bean.DpWirelessBatterylock;
import com.tuyasmart.camera.devicecontrol.bean.DpWirelessElectricity;
import com.tuyasmart.camera.devicecontrol.bean.DpWirelessLowpower;
import com.tuyasmart.camera.devicecontrol.bean.DpWirelessPowermode;
import com.tuyasmart.camera.devicecontrol.model.DpNotifyModel;
import com.tuyasmart.camera.devicecontrol.model.PTZDirection;
import com.tuyasmart.camera.devicecontrol.utils.CRC32;
import com.tuyasmart.camera.devicecontrol.utils.IntToButeArray;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.CALL;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.DEVICE;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.DEVICESTART;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_DEVID;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_LOCALKEY;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_P2P_TYPE;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.DEVICENOT;
import static com.tuya.smart.android.demo.utils.Constants.MSG_CAMERA_REFRESH;
import static com.tuya.smart.android.demo.utils.Constants.MSG_CONNECT;
import static com.tuya.smart.android.demo.utils.Constants.MSG_GET_CLARITY;
import static com.tuya.smart.android.demo.utils.Constants.MSG_MUTE;
import static com.tuya.smart.android.demo.utils.Constants.MSG_SCREENSHOT;
import static com.tuya.smart.android.demo.utils.Constants.MSG_TALK_BACK_BEGIN;
import static com.tuya.smart.android.demo.utils.Constants.MSG_TALK_BACK_OVER;
import static com.tuya.smart.android.demo.utils.Constants.MSG_VIDEO_RECORD_BEGIN;
import static com.tuya.smart.android.demo.utils.Constants.MSG_VIDEO_RECORD_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.MSG_VIDEO_RECORD_OVER;

public class CameraPanelActivity extends BaseActivity implements View.OnClickListener, IPersonalInfoView {

    private static final String TAG = "CameraPanelActivity";

    private PersonalInfoPresenter mPersonalInfoPresenter;
    @BindView(R.id.camera_video_view)
    public TuyaCameraView mVideoView;
    @BindView(R.id.camera_mute)
    public ImageView muteImg;
    @BindView(R.id.speak_Txt)
    public ImageView speakTxt;
    @BindView(R.id.camera_quality)
    public TextView qualityTv;
    @BindView(R.id.record_Txt)
    public TextView recordTxt;
    @BindView(R.id.photo_Txt)
    public TextView photoTxt;
    @BindView(R.id.doorTxt)
    public TextView doorTxt;
    @BindView(R.id.Open_Door)
    public View OpenDoorTxt;

    @BindView(R.id.toolbar_view)
    public Toolbar mToolBar;
    @BindView(R.id.doorImage)
    public ImageView doorImage;
    @BindView(R.id.Battery_Txt)
    public TextView BatteryTxt;

    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private boolean isSpeaking = false;
    private boolean isRecording = false;
    private boolean isPlay = false;
    private int previewMute = ICameraP2P.MUTE;
    private int videoClarity = ICameraP2P.HD;

    private int p2pType;

    private String device;
    private String callin;
    private String devicestart;
    private String devId;
    private String localKey;
    private ITuyaCameraDevice mDeviceControl;
    private ITuyaSmartCameraP2P mCameraP2P;

    private AWSIotMqttClient client;

    private Unbinder mBind;

    @Override
    public boolean onPanelKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                exitByClick(mCameraP2P);
                return true;
        }
        return false;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CAMERA_REFRESH:
                    onRefresh();
                    break;
                case MSG_CONNECT:
                    handleConnect(msg);
                    break;
                case MSG_GET_CLARITY:
                    handleClarity(msg);
                    break;
                case MSG_MUTE:
                    handleMute(msg);
                    break;
                case MSG_SCREENSHOT:
                    handlesnapshot(msg);
                    break;
                case MSG_VIDEO_RECORD_BEGIN:
                    ToastUtil.shortToast(CameraPanelActivity.this, "동영상촬영 시작");
                    break;
                case MSG_VIDEO_RECORD_FAIL:
                    ToastUtil.shortToast(CameraPanelActivity.this, "동영상촬영 실패");
                    break;
                case MSG_VIDEO_RECORD_OVER:
                    handleVideoRecordOver(msg);
                    break;
                case MSG_TALK_BACK_BEGIN:
                    handleStartTalk(msg);
                    break;
                case MSG_TALK_BACK_OVER:
                    handleStopTalk(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleStopTalk(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, "말하기를 멈춥니다.");
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "말하기 실패");
        }
    }

    private void handleStartTalk(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, "말하기를 합니다.");
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "말하기 실패");
        }
    }

    private void handleVideoRecordOver(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, "동영상촬영 종료.\n갤러리에서 확인해주세요.");
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "동영상촬영 종료실패");
        }
    }

    private void handlesnapshot(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, "사진을 촬영했습니다.\n갤러리에서 확인해주세요.");
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "촬영 실패");
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            boolean isMute = previewMute == ICameraP2P.MUTE;
            muteImg.setSelected(isMute);
            if (isMute) {
                ToastUtil.shortToast(CameraPanelActivity.this, "음소거 모드로 변경하였습니다.");
            } else {
                ToastUtil.shortToast(CameraPanelActivity.this, "음소거 모드가 해제되었습니다.");
            }
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "operation fail");
        }
    }

    private void handleClarity(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            qualityTv.setText(videoClarity == ICameraP2P.HD ? "고화질" : "일반");
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, "다시 시도해주세요");
        }
    }

    private void handleConnect(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            Log.e(TAG, "KMK 카메라연결성공");
            ProgressUtil.hideLoading();
            isPlay = true;
            preview();
        } else if (msg.arg1 == DEVICENOT) {
            Log.e(TAG, "KMK 카메라존재하지않음");
            ToastUtil.shortToast(CameraPanelActivity.this, "등록된 도어벨이 없습니다.\n도어벨을 등록해주세요.");
            ProgressUtil.hideLoading();
        } else {
            Log.e(TAG, "KMK 카메라연결실패 다시 새로고침합니다.");
            ProgressUtil.hideLoading();
//            ToastUtil.shortToast(CameraPanelActivity.this, "카메라 연결상태가 좋지않습니다.\n잠시만 기다려주세요.");
            ToastUtil.shortToast(CameraPanelActivity.this, "카메라 연결상태가 좋지않습니다.\n잠시후 상단의 새로고침 버튼을 눌러주세요.");
//            onRefresh();
        }
    }

    private static final int THREAD_ID = 10000;

    private void initPresenter() {
        mPersonalInfoPresenter = new PersonalInfoPresenter(this, this);
        nickName = mPersonalInfoPresenter.getNickName();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_panel);
        checkVerify();
//        checkWhiteListRegist();
        mBind = ButterKnife.bind(this);
        initPresenter();
        initView();
        initMenu();
        initData();
        initListener();
        BatteryTxt.performClick();

        TrafficStats.setThreadStatsTag(THREAD_ID);
        int value = DoorOpen();
        if (value == 1) {
            openDoorNowData();
        }

        if (mDeviceControl != null && mDeviceControl.isSupportCameraDps(DpPTZControl.ID)) {

            mVideoView.setOnRenderDirectionCallback(new OnRenderDirectionCallback() {

                @Override
                public void onLeft() {
                    mDeviceControl.publishCameraDps(DpPTZControl.ID, PTZDirection.LEFT.getDpValue());
                }

                @Override
                public void onRight() {
                    mDeviceControl.publishCameraDps(DpPTZControl.ID, PTZDirection.RIGHT.getDpValue());
                }

                @Override
                public void onUp() {
                    mDeviceControl.publishCameraDps(DpPTZControl.ID, PTZDirection.UP.getDpValue());
                }

                @Override
                public void onDown() {
                    mDeviceControl.publishCameraDps(DpPTZControl.ID, PTZDirection.DOWN.getDpValue());
                }

                @Override
                public void onCancel() {
                    mDeviceControl.publishCameraDps(DpPTZStop.ID, true);
                }
            });
        }

        device = getIntent().getStringExtra(DEVICE);
        callin = getIntent().getStringExtra(CALL);
        devicestart = getIntent().getStringExtra(DEVICESTART);

        devId = getIntent().getStringExtra(INTENT_DEVID);
        localKey = getIntent().getStringExtra(INTENT_LOCALKEY);
        mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devId);

        ToastUtil.shortToast(CameraPanelActivity.this, "장방 경로당 앱이 실행되었습니다.");

    }

    @Override
    public void onUserLeaveHint() {
        if (null != mCameraP2P) {
            Log.e(TAG, "KMK 홈버튼 클릭 devicestart : " + devicestart);
            devicestart = "false";
            mCameraP2P.removeOnP2PCameraListener();
            mCameraP2P.destroyP2P();
        }

        if (!Boolean.parseBoolean(callin)) {
            speakStatue(false);
            muteImg.setSelected(true);
            isSpeaking = false;
            previewMute = 1;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
            mCameraP2P.destroyP2P();
        }

        mBind.unbind();
        ProgressUtil.hideLoading();
    }

    protected void initToolbar() {
        if (mToolBar != null) {
            TypedArray a = obtainStyledAttributes(new int[]{
                    R.attr.status_font_color});
            int titleColor = a.getInt(0, Color.WHITE);
            mToolBar.setTitleTextColor(titleColor);
        }
    }

    protected void setMenu(int resId, Toolbar.OnMenuItemClickListener listener) {
        if (mToolBar != null) {
            mToolBar.inflateMenu(resId);
            mToolBar.setOnMenuItemClickListener(listener);
        }
    }

    final int GET_GALLERY_IMAGE = 200;

    private void initMenu() {
        devId = getIntent().getStringExtra(INTENT_DEVID);

        ActivityUtils.SetString(getBaseContext(), "deviceId", devId);
        localKey = getIntent().getStringExtra(INTENT_LOCALKEY);
        p2pType = getIntent().getIntExtra(INTENT_P2P_TYPE, -1);
        if (devId.equals("devId")) {
            mToolBar.setTitle("장방경로당 (도어벨을 등록해주세요.)");
            setMenu(R.menu.toolbar_top_smart_camera, new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.profile: // 프로필
                            intent = new Intent(CameraPanelActivity.this, PersonalInfoActivity.class);
                            // onUserLeaveHint함수 호출하지않기.
                            // intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                            startActivity(intent);
                            break;
                        case R.id.device_add: // 장비등록 : 장비가존재하지않을때 나옴
                            intent = new Intent(CameraPanelActivity.this, AddDeviceTypeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                            startActivity(intent);
                            break;
                        case R.id.gallery: // 갤러리
                            intent = new Intent(CameraPanelActivity.this, ImageViewActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.history: // 알림센터림
                            intent = new Intent(CameraPanelActivity.this, AlarmDetectionActivity.class);
                            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, devId);

                            startActivity(intent);
                            break;
                        case R.id.tutorial: // 튜토리얼
                            Log.e("튜토리얼 클릭", "");
                            tutorialStep = 0;
                            GetTutorial();
                            break;
                    }
                    return false;
                }
            });
        } else {
            String userId = ActivityUtils.GetString(getBaseContext(), "user_id");


            if (nickName != null && !nickName.trim().equals("")) {
                mToolBar.setTitle(nickName);
            } else if (userId != null && !userId.trim().equals("")) {
                mToolBar.setTitle(userId);
            } else {
                mToolBar.setTitle("장방경로당");
            }
            setMenu(R.menu.toolbar_top_smart_camera2, new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent;
                    switch (item.getItemId()) {
                        case R.id.profile:  //  프로필
                            intent = new Intent(CameraPanelActivity.this, PersonalInfoActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                            startActivity(intent);
                            break;
                        case R.id.action_unconnect:  // 장비제거 : 장비가존재할때 나옴
                            //  한스탭밟고 제거하기
                            AlertDialog.Builder builder = new AlertDialog.Builder(CameraPanelActivity.this);
                            builder.setTitle("도어벨제거");
                            builder.setMessage("등록된 도어벨을 제거 하시겠습니까?");
                            builder.setPositiveButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    //  취소시 처리 로직
                                    Toast.makeText(CameraPanelActivity.this, "도어벨 제거를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            builder.setNegativeButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // 확인시 처리 로직
                                    TuyaHomeSdk.newDeviceInstance(devId).removeDevice(new IResultCallback() {
                                        @Override
                                        public void onError(String code, String error) {
                                            ToastUtil.shortToast(getApplicationContext(), "도어벨 제거를 실패하였습니다. 인터넷연결 확인 후 다시 시도해주시길 바랍니다.");
                                        }

                                        @Override
                                        public void onSuccess() {
                                            ToastUtil.showToast(getApplicationContext(), "도어벨을 제거하였습니다.");
                                            Intent intent = new Intent(CameraPanelActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                    finish();
                                }
                            });
                            builder.create().show();
                            break;
                        case R.id.gallery: // 갤러리
                            intent = new Intent(CameraPanelActivity.this, ImageViewActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.history: // 알림센터
                            intent = new Intent(CameraPanelActivity.this, AlarmDetectionActivity.class);
                            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, devId);
                            startActivity(intent);
                            break;
                        case R.id.tutorial: // 튜토리얼
                            // 튜토리얼클릭
                            tutorialStep = 0;
                            GetTutorial();
                            break;
                        case R.id.main_refresh: // 새로고침버튼
                            isPlay = false;
                            onRefresh();
                            break;
                    }
                    return false;
                }
            });
        }
    }

    private int tutorialStep;
    private final int[] tutorialLayoutArray = {R.layout.tutorial_01, R.layout.tutorial_02, R.layout.tutorial_03, R.layout.tutorial_05, R.layout.tutorial_06, R.layout.tutorial_07, R.layout.tutorial_08};
    private final int[] tutorialTargetArray = {R.id.photo_Txt, R.id.record_Txt, R.id.main_refresh, R.id.Battery_Txt, R.id.camera_mute, R.id.speak_Txt, R.id.Open_Door};
    TutoShowcase showcase;

    public void GetTutorial() {

        if (devId.equals("devId") && (tutorialStep == 2 || tutorialStep == 3)) {
            tutorialStep = 4;
        }

        showcase = TutoShowcase.from(CameraPanelActivity.this)
                .setContentView(tutorialLayoutArray[tutorialStep])
                .on(tutorialTargetArray[tutorialStep])
                .addCircle()
                .withBorder()
                .onClick(new View.OnClickListener() {
                    public void onClick(View view) {

                    }
                })
                .onClickContentView(R.id.touchView, view -> {
                    tutorialStep++;
                    showcase.dismiss();
                    if (tutorialStep < tutorialLayoutArray.length)
                        GetTutorial();

                });
        showcase.show();
    }

    private void cameraOn() {
        if(!Boolean.parseBoolean(devicestart)){
            Log.e(TAG, "KMK 카메라를 다시 실행합니다. devicestart : "+devicestart);
//            Log.e(TAG, "KMK 카메라를 켭니다. 장비아이디 devId = "+devId);
//            Log.e(TAG, "KMK 카메라를 켭니다. 로컬키 localKey = "+localKey);
//            mDeviceControl.wirelessWake(localKey, devId);

            Log.e(TAG, "KMK 다시실행함 장비아이디 : "+devId);
            Log.e(TAG, "KMK 다시실행함 앱 처음 실행 카메라를 켭니다.");
            int crcsum = CRC32.getChecksum(localKey.getBytes());
            Log.e(TAG, "KMK 다시실행함 CAMERA_ON crcsum : "+crcsum);
            String topicId = "m/w/" + devId;
            Log.e(TAG, "KMK 다시실행함 CAMERA_ON topicId : "+topicId);
            byte[] bytes = IntToButeArray.intToByteArray(crcsum);
            Log.e(TAG, "KMK 다시실행함 CAMERA_ON bytes : "+bytes);
            ITuyaHomeCamera homeCamera = TuyaHomeSdk.getCameraInstance();
            Log.e(TAG, "KMK 다시실행함 CAMERA_ON homeCamera : "+homeCamera);
            homeCamera.publishWirelessWake(topicId, bytes);

            devicestart = "true";
        }else{
            Log.e(TAG, "KMK 카메라는 이미 실행했습니다. devicestart : "+devicestart);
        }
    }

    private void initView() {
        int width = getDisplayWidth(this);
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        //  기기별 높이해상도 : 갤럭시S7,갤럭시노트9 -> 450
        //  기기별 높이해상도 : 갤럭시S6,LG폰 -> 600
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height + 600); // 카메라 해상도?
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_view);
        findViewById(R.id.camera_video_view_Rl).setLayoutParams(layoutParams);

        photoTxt.setSelected(true);
        muteImg.setSelected(true);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    int getDisplayWidth(Context context) {
        int width;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT > 12) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        } else {
            width = display.getWidth();  // Deprecated
        }
        return width;
    }

    private void initData() {
        devId = getIntent().getStringExtra(INTENT_DEVID);
        p2pType = getIntent().getIntExtra(INTENT_P2P_TYPE, -1);
        mCameraP2P = TuyaSmartCameraP2PFactory.createCameraP2P(p2pType, devId);

        mVideoView.setViewCallback(new AbsVideoViewCallback() {
            @Override
            public void onCreated(Object o) {
                super.onCreated(o);
                if (null != mCameraP2P) {
                    mCameraP2P.generateCameraView(o);
                }
            }
        });
        mVideoView.createVideoView(p2pType);

        mCameraP2P.registerP2PCameraListener(new AbsP2pCameraListener() {
            @Override
            public void onSessionStatusChanged(Object o, int i, int i1) {
                super.onSessionStatusChanged(o, i, i1);
            }
        });

        if (null == mCameraP2P) {
            showNotSupportToast();
        } else {
            mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devId);
        }
    }

    private void showNotSupportToast() {
        ToastUtil.shortToast(CameraPanelActivity.this, "device is not support!");
    }

    private void preview() {
        mCameraP2P.startPreview(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                Log.e(TAG, "KMK 프리뷰함수 실행 isPlay=true");
                isPlay = true;
                devicestart = "false";
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                Log.e(TAG, "KMK 프리뷰함수 실행 isPlay=false");
                isPlay = false;
                devicestart = "false";
            }
        });
    }

    private void initListener() {
        if (mCameraP2P == null) {
            return;
        }

        muteImg.setOnClickListener(this);
        qualityTv.setOnClickListener(this);
        speakTxt.setOnClickListener(this);
        recordTxt.setOnClickListener(this);
        photoTxt.setOnClickListener(this);

        OpenDoorTxt.setOnClickListener(this);
        BatteryTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_mute:
                muteClick();
                break;
            case R.id.camera_quality:
                setVideoClarity();
                break;
            case R.id.speak_Txt:
                speakClick();
                break;
            case R.id.record_Txt:
                recordClick();
                break;
            case R.id.photo_Txt:
                snapShotClick();
                break;
            // 문열림버튼누를시 사용되는 이벤트입니다.
            case R.id.Open_Door:
                if (!isDoorOpened) {
                    openDoorActive();
                    openDoorActiveData();
                }
                break;
            case R.id.Battery_Txt:
                mDeviceControl.registorTuyaCameraDeviceControlCallback(DpWirelessElectricity.ID, new ITuyaCameraDeviceControlCallback<Integer>() {
                    @Override
                    public void onSuccess(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, Integer o) {
                        if (o == 0) {
                            BatteryTxt.setText("충천중");
                        } else {
                            BatteryTxt.setText(o + "%");
                        }
                    }

                    @Override
                    public void onFailure(String s, DpNotifyModel.ACTION action, DpNotifyModel.SUB_ACTION sub_action, String s1, String s2) {

                    }
                });
                mDeviceControl.publishCameraDps(DpWirelessElectricity.ID, null);
                mDeviceControl.publishCameraDps(DpWirelessBatterylock.ID, true);
                mDeviceControl.publishCameraDps(DpWirelessLowpower.ID, 50);
                mDeviceControl.publishCameraDps(DpWirelessPowermode.ID, null);
                mDeviceControl.publishCameraDps(DpPIRSwitch.ID, "3");
                break;
            default:
                break;
        }
    }

    public void openDoorNowData() {
        try {
            String key = ActivityUtils.GetString(getBaseContext(), "topic_key");
            if (key == null || key.trim().equals("")) {
                ActivityUtils.SetString(getBaseContext(), "topic_key", "jb");
                key = "jb";
            }
            String topicName = key + "/data/door";
            AWSIotQos qos = AWSIotQos.QOS0;
            MyTopic myTopic = new MyTopic(topicName, qos, this);
            client.subscribe(myTopic, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 데이터 받기
    public void doorOn(String data) {
        Log.e("데이터 : ", data);
        System.out.println("data : " + data);
        if (data.equals("unlocked")) {
            isDoorOpened = true;
            doorTxt.setText("문열림");
            doorImage.setBackgroundResource(R.drawable.unlock);
//            OpenDoorTxt.setEnabled(true);
//            OpenDoorTxt.setSelected(true);
        } else {
            isDoorOpened = false;
            doorTxt.setText("문닫힘");
            doorImage.setBackgroundResource(R.drawable.lock);
//            OpenDoorTxt.setEnabled(true);
//            OpenDoorTxt.setSelected(false);
        }

    }

    private boolean isDoorOpened; //문열기 상태 체크

    // 문열기 버튼 함수
    private int DoorOpen() {
        if (!isDoorOpened) {
            String clientId = "BroadwaveHomeService" + UUID.randomUUID();
            String BROADWAVE_APP_ID = "AKIAVG3JOTHKHQVLA446";
            String BROADWAVE_APP_KEY = "vMexKS9HnUkrfAoGhjaiub208eArU3ulm9CrhDRq";
            String BROADWAVE_APP_ENDPOINT = "a1n71evdux6hhf-ats.iot.ap-northeast-2.amazonaws.com";
            client = new AWSIotMqttClient(BROADWAVE_APP_ENDPOINT, clientId
                    , BROADWAVE_APP_ID
                    , BROADWAVE_APP_KEY);
            Log.e("client : ", client.toString());
            try {
                client.connect();
                return 1;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    // 문열기 명령
    public void openDoorActive() {
        try {
            String key = ActivityUtils.GetString(getBaseContext(), "topic_key");
            if (key == null || key.trim().equals("")) {
                ActivityUtils.SetString(getBaseContext(), "topic_key", "jb");
                key = "jb";
            }

            String topic = key + "/command/door1/unlock";
            String payload = "{ \"door\" : \"appopen\"}";
            client.publish(topic, AWSIotQos.QOS1, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openDoorActiveData() {
        try {
            String key = ActivityUtils.GetString(getBaseContext(), "topic_key");
            if (key == null || key.trim().equals("")) {
                ActivityUtils.SetString(getBaseContext(), "topic_key", "jb");
                key = "jb";
            }
            String topic = key + "/data/door";
            AWSIotQos qos = AWSIotQos.QOS0;
            MyTopic myTopic = new MyTopic(topic, qos, this);
            client.subscribe(myTopic, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void snapShotClick() {
        String picPath = null;
        if (Constants.hasStoragePermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String destPath = Objects.requireNonNull(getApplicationContext().getExternalFilesDir(null)).getAbsolutePath();
                String[] paths = destPath.split("Android", 2);
                String path;
                if (paths.length > 0) {
                    path = paths[0] + "DCIM/DoorBell/";
                    File folder = new File(path);
                    if (!folder.exists()) {
                        File wallpaperDirectory = new File(path);
                        boolean created = wallpaperDirectory.mkdirs();
                        Log.d(TAG, "created : " + created);
                    }
                    picPath = path;
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                picPath = path;
            }
        } else {
            Constants.requestPermission(CameraPanelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.EXTERNAL_STORAGE_REQ_CODE, "open_storage");
        }
        Log.e(TAG, "KMK 사진촬영 picPath : "+picPath);

        mCameraP2P.snapshot(picPath, CameraPanelActivity.this, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_SUCCESS, data));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_FAIL));
            }
        });
    }

    private void recordClick() {
        if (!isRecording) {
            Log.e(TAG, "KMK 동영상촬영 시작");
            String picPath = null;
            if (Constants.hasStoragePermission()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Log.e(TAG, "KMK 버전이 Q이상 입니다.");
                    String destPath = getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
                    String[] paths = destPath.split("Android", 2);
                    String path;
                    if (paths.length > 0) {
                        System.out.println(Arrays.toString(paths));
                        path = paths[0] + "DCIM/DoorBell/";
                        File folder = new File(path);
                        if (!folder.exists()) {
                            File wallpaperDirectory = new File(path);
                            boolean created = wallpaperDirectory.mkdirs();
                            Log.e(TAG, "KMK created : "+created);
                        }
                        picPath = path;
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Log.e(TAG, "KMK 버전이 Q이하 입니다.");
                    picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                    File file = new File(picPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }
                Log.e(TAG, "KMK 동영상촬영 picPath : "+picPath);
                mCameraP2P.startRecordLocalMp4(picPath, CameraPanelActivity.this, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isRecording = true;
                        Log.e(TAG, "KMK 동영상촬영시작 성공.");
                        mHandler.sendEmptyMessage(MSG_VIDEO_RECORD_BEGIN);
                    }
                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        Log.e(TAG, "KMK 동영상촬영시작 실패.");
                        mHandler.sendEmptyMessage(MSG_VIDEO_RECORD_FAIL);
                    }
                });
                recordStatue(true);
            } else {
                Constants.requestPermission(CameraPanelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.EXTERNAL_STORAGE_REQ_CODE, "open_storage");
            }
        } else {
            mCameraP2P.stopRecordLocalMp4(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isRecording = false;
                    Log.e(TAG, "KMK 동영상촬영 종료");
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_SUCCESS, data));
                }
                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isRecording = false;
                    Log.e(TAG, "KMK 동영상촬영 종료");
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_FAIL));
                }
            });
            recordStatue(false);
        }
    }

    private void muteClick() {
        int mute;
        mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(ICameraP2P.PLAYMODE.LIVE, mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                previewMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
            }
        });
    }

    private void speakClick() {
        if (isSpeaking) {
            mCameraP2P.stopAudioTalk(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isSpeaking = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_OVER, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isSpeaking = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_OVER, ARG1_OPERATE_FAIL));
                }
            });
            speakStatue(false);
        } else {
            mCameraP2P.startAudioTalk(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isSpeaking = true;
                    speakStatue(true);
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isSpeaking = false;
                    speakStatue(false);
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_FAIL));
                }
            });
        }
    }

    private void setVideoClarity() {
        mCameraP2P.setVideoClarity(videoClarity == ICameraP2P.HD ? ICameraP2P.STANDEND : ICameraP2P.HD, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                videoClarity = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_CLARITY, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_CLARITY, ARG1_OPERATE_FAIL));
            }
        });

    }

    private void recordStatue(boolean isRecording) {
        photoTxt.setEnabled(!isRecording);
        recordTxt.setEnabled(true);
        recordTxt.setSelected(isRecording);
    }

    private void speakStatue(boolean isSpeaking) {
        speakTxt.setEnabled(true);
        speakTxt.setSelected(isSpeaking);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onRefresh();
    }

    private void onRefresh() {
        if (!isPlay) {
            mVideoView.onResume();
            BatteryTxt.performClick();
            if (!Boolean.parseBoolean(device)) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, DEVICENOT));
            } else {
                if (null != mCameraP2P) {
                    AudioUtils.getModel(this);
                    mCameraP2P.registorOnP2PCameraListener(p2pCameraListener);
                    mCameraP2P.generateCameraView(mVideoView.createdView());
                    ProgressUtil.showLoading(this, "카메라 로딩중...");

                    Log.e(TAG,"KMK 커넥팅테스트 mCameraP2P.isConnecting() : "+mCameraP2P.isConnecting());

                    if (mCameraP2P.isConnecting()) {
                        Log.e(TAG,"KMK 커넥팅이 되어 있음.");
                        mCameraP2P.startPreview(new OperationDelegateCallBack() {
                            @Override
                            public void onSuccess(int sessionId, int requestId, String data) {
                                Log.e(TAG,"KMK 카메라 연결성공");
                                mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
                            }

                            @Override
                            public void onFailure(int sessionId, int requestId, int errCode) {
                                Log.e(TAG,"KMK 카메라 연결실패");
                            }
                        });
                    } else {
                        cameraOn();
                        initData();
                        if(Boolean.parseBoolean(devicestart)) {
                            Log.e(TAG, "KMK 커넥팅이 되어있지 않음. 커넥트시작");
                            mCameraP2P.connect(devId, new OperationDelegateCallBack() {
                                @Override
                                public void onSuccess(int i, int i1, String s) {
                                    Log.e(TAG, "KMK 커넥팅 성공");
                                    mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
                                    //전화받고 말하기
                                    if (Boolean.parseBoolean(callin)) {
                                        int mute;
                                        mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
                                        mCameraP2P.setMute(mute, new OperationDelegateCallBack() {
                                            @Override
                                            public void onSuccess(int sessionId, int requestId, String data) {
                                                previewMute = Integer.parseInt(data);
                                                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
                                                callin = "false";
                                                mCameraP2P.startAudioTalk(new OperationDelegateCallBack() {
                                                    @Override
                                                    public void onSuccess(int sessionId, int requestId, String data) {
                                                        isSpeaking = true;
                                                        speakStatue(true);
                                                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_SUCCESS));
                                                    }

                                                    @Override
                                                    public void onFailure(int sessionId, int requestId, int errCode) {
                                                        isSpeaking = false;
                                                        speakStatue(false);
                                                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_FAIL));
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFailure(int sessionId, int requestId, int errCode) {
                                                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
                                                Log.e(TAG, "KMK 커넥팅 실패2 ");
                                                devicestart = "false";
                                                Log.e(TAG, "KMK 연결실패3 devicestart : " + devicestart);
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onFailure(int i, int i1, int i2) {
                                    mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_FAIL, i2));
                                    Log.e(TAG, "KMK 커넥팅 실패1 ");
                                    devicestart = "false";
                                    Log.e(TAG, "KMK 연결실패4 devicestart : " + devicestart);
                                }
                            });
                        }else{
                            devicestart = "false";
                        }
                    }
                }
            }
        }
    }

    private final AbsP2pCameraListener p2pCameraListener = new AbsP2pCameraListener() {
        @Override
        public void onReceiveSpeakerEchoData(ByteBuffer pcm, int sampleRate) {
            if (null != mCameraP2P) {
                int length = pcm.capacity();
                L.d(TAG, "receiveSpeakerEchoData pcmlength " + length + " sampleRate " + sampleRate);
                byte[] pcmData = new byte[length];
                pcm.get(pcmData, 0, length);
                mCameraP2P.sendAudioTalkData(pcmData, length);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (isSpeaking) {
            mCameraP2P.stopAudioTalk(null);
        }
        if (isPlay) {
            mCameraP2P.stopPreview(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {

                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {

                }
            });
            isPlay = false;
        }
        if (null != mCameraP2P) {
            mCameraP2P.disconnect(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int i, int i1, String s) {

                }

                @Override
                public void onFailure(int i, int i1, int i2) {

                }
            });
        }
        AudioUtils.changeToNomal(this);
    }

    private String nickName; // 닉네임

    @Override
    public void reNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public void onLogout() {

    }

}