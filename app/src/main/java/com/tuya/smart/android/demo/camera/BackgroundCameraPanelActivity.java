package com.tuya.smart.android.demo.camera;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.tuya.smart.android.base.utils.PreferencesUtil;
import com.tuya.smart.android.camera.api.ITuyaHomeCamera;
import com.tuya.smart.android.camera.api.bean.CameraPushDataBean;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.BackgroundService;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.utils.CollectionUtils;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.android.demo.base.utils.ProgressUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.camera.awsiot.MyTopic;
import com.tuya.smart.android.demo.family.activity.IFamilyAddView;
import com.tuya.smart.android.demo.family.presenter.FamilyAddPresenter;
import com.tuya.smart.camera.camerasdk.typlayer.callback.AbsP2pCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
import com.tuya.smart.camera.middleware.p2p.TuyaSmartCameraP2PFactory;
import com.tuya.smart.camera.utils.AudioUtils;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.ITuyaGetBeanCallback;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_DEVID;
import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_P2P_TYPE;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.DEVICENOT;
import static com.tuya.smart.android.demo.utils.Constants.MSG_CONNECT;
import static com.tuya.smart.android.demo.utils.Constants.MSG_MUTE;
import static com.tuya.smart.android.demo.utils.Constants.MSG_TALK_BACK_BEGIN;
import static com.tuya.smart.android.demo.utils.Constants.MSG_VIDEO_RECORD_BEGIN;
import static com.tuya.smart.android.demo.utils.Constants.MSG_VIDEO_RECORD_FAIL;

/**
 * @author chenbj
 */
public class BackgroundCameraPanelActivity extends Activity implements IFamilyAddView {

    private static final String TAG = "CameraPanelActivity";


    private boolean isSpeaking = false;
    private boolean isPlay = false;
    private int previewMute = ICameraP2P.MUTE;

    private int p2pType;

    private String devId;
    private String localKey;
    private ITuyaCameraDevice mDeviceControl;
    private ITuyaSmartCameraP2P mCameraP2P;

    private AWSIotMqttClient client;
    private int value;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT:
                    handleConnect(msg);
                    break;

                case MSG_VIDEO_RECORD_BEGIN:
                    ToastUtil.shortToast(BackgroundCameraPanelActivity.this, "동영상촬영 시작");
                    break;
                case MSG_VIDEO_RECORD_FAIL:
                    ToastUtil.shortToast(BackgroundCameraPanelActivity.this, "동영상촬영 실패");
                    break;

            }
            super.handleMessage(msg);
        }
    };


    private void handleConnect(Message msg) {
        cameraOn();
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ProgressUtil.hideLoading();
            preview();
        } else if (msg.arg1 == DEVICENOT) {
            ToastUtil.shortToast(BackgroundCameraPanelActivity.this, "등록된 도어벨이 없습니다.\n도어벨을 등록해주세요.");
            ProgressUtil.hideLoading();
        }
    }

    private IntentFilter scrFilter;
    private BroadcastReceiver scrOffReceiver;
    /**
     * the lower power Doorbell device change to true
     */
    private static final int THREAD_ID = 10000;

    private FamilyAddPresenter mPresenter;

    private void initPresenter() {
        mPresenter = new FamilyAddPresenter(this);
    }

    private static ITuyaHomeCamera homeCamera;


    private void initService() {
        new Handler().postDelayed(new Runnable() {
            // 3초 후에 실행
            @Override
            public void run() {
                    // BackgroundService
                    Intent serviceLauncher = new Intent(BackgroundCameraPanelActivity.this, BackgroundService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceLauncher);
                    } else {
                        startService(serviceLauncher);
                    }
            }
        }, 3000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent_activity);


        initPresenter();

        initService();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (TuyaHomeSdk.getUserInstance().isLogin()) {
                    if (homeCamera != null) {
                        homeCamera.unRegisterCameraPushListener(mTuyaGetBeanCallback);
                    }
                    homeCamera = null;
                    homeCamera = TuyaHomeSdk.getCameraInstance();
                    if (homeCamera != null) {
                        homeCamera.registerCameraPushListener(mTuyaGetBeanCallback);
                    }
//                    LoginHelper.afterLogin();
                    deviceLoad();
                }
            }
        }, 1000); //3초 뒤에 Runner객체 실행하도록 함


    }

    private void init() {

        Log.e(TAG, "init");
        initData();
        cameraOn();

        TrafficStats.setThreadStatsTag(THREAD_ID);
        value = DoorOpen();
        if (value == 1) {
            openDoorNowData();
        }


        // TODO:  전원버튼누를때, 액션 카메라절전모드들어가기, 카메라키기
        scrOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    if (null != mCameraP2P) {
                        mCameraP2P.removeOnP2PCameraListener();
                        mCameraP2P.destroyP2P();
                    }
                }
            }
        };

        // TODO: IntentFilter에 Action 등록
        scrFilter = new IntentFilter();
        scrFilter.addAction(Intent.ACTION_SCREEN_OFF);
        scrFilter.addAction(Intent.ACTION_SCREEN_ON);
//        registerReceiver(scrOffReceiver, scrFilter);


        finish();
    }

    public void deviceLoad() {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homeBeans) {
                if (CollectionUtils.isEmpty(homeBeans)) {
                    List<String> checkRoomList = new ArrayList<>();
                    String name = "경로당";
                    mPresenter.addFamily(name, checkRoomList);
                } else {
                    final long homeId = homeBeans.get(0).getHomeId();
                    Constant.HOME_ID = homeId;
                    PreferencesUtil.set("homeId", Constant.HOME_ID);
                    TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                        @Override
                        public void onSuccess(HomeBean bean) {
                            if (bean.getDeviceList().size() == 0) {
                                devId = "devId";
                                localKey = "localId";
                                p2pType = -1;
                            } else {
                                ITuyaCameraDevice mDeviceControl;

                                devId = bean.getDeviceList().get(0).getDevId();
                                localKey = bean.getDeviceList().get(0).getLocalKey();


                                Map<String, Object> map = bean.getDeviceList().get(0).getSkills();
                                int _p2pType = -1;
                                if (map == null || map.size() == 0) {
                                    _p2pType = -1;
                                } else {
                                    _p2pType = (Integer) (map.get("p2pType"));
                                }
                                p2pType = _p2pType;
                                mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(bean.getDeviceList().get(0).getDevId());
                                mDeviceControl.wirelessWake(bean.getDeviceList().get(0).getLocalKey(), bean.getDeviceList().get(0).getDevId());
                            }
                            init();
                        }

                        @Override
                        public void onError(String errorCode, String errorMsg) {

                        }
                    });
                }
            }

            @Override
            public void onError(String errorCode, String error) {
                TuyaHomeSdk.newHomeInstance(Constant.HOME_ID).getHomeLocalCache(new ITuyaHomeResultCallback() {
                    @Override
                    public void onSuccess(HomeBean bean) {

                    }

                    @Override
                    public void onError(String errorCode, String errorMsg) {

                    }
                });
            }
        });
    }

    private static ITuyaGetBeanCallback<CameraPushDataBean> mTuyaGetBeanCallback = new ITuyaGetBeanCallback<CameraPushDataBean>() {
        @Override
        public void onResult(CameraPushDataBean o) {
            // 새 버전 fcm 전송
//            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
//                @Override
//                public void onComplete(@NonNull Task<String> task) {
//                    // Get new FCM registration token
//                    String token = task.getResult();
//
//                    // Log and toast
//                    // String msg = getString(R.string.msg_token_fmt, token);
//                    Log.e("Firebase Token : ", token);
//
//                    try {
//                        JSONObject data = new JSONObject();
//                        JSONObject auth = new JSONObject();
//
//                        data.put("title", "초인종이 울렸습니다~");
//                        data.put("message", "확인해주세요~");
//
//                        auth.put("to", token);
//                        auth.put("priority", "high");
//                        auth.put("direct_book_ok", true);
//                        auth.put("data", data);
//
//                        //new LoginHelper.HttpUtil().execute(auth.toString());
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
        }
    };

    @Override
    public void onUserLeaveHint() {
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
            mCameraP2P.destroyP2P();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
            mCameraP2P.destroyP2P();
        }
        ProgressUtil.hideLoading();
    }


    private void cameraOn() {
        mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(devId);
        mDeviceControl.wirelessWake(localKey, devId);
    }


    private void initData() {
        devId = getIntent().getStringExtra(INTENT_DEVID);
        p2pType = getIntent().getIntExtra(INTENT_P2P_TYPE, -1);
        mCameraP2P = TuyaSmartCameraP2PFactory.createCameraP2P(p2pType, devId);


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
        ToastUtil.shortToast(BackgroundCameraPanelActivity.this, "device is not support!");
    }

    private void preview() {
        mCameraP2P.startPreview(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                cameraOn();
                isPlay = true;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
//                Log.d(TAG, "start preview onFailure, errCode: " + errCode);
                cameraOn();
                isPlay = false;
            }
        });
    }


    public void openDoorNowData() {
        try {
            String topicName = "jb/data/door";
            AWSIotQos qos = AWSIotQos.QOS0;
            MyTopic myTopic = new MyTopic(topicName, qos);
            client.subscribe(myTopic, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // TODO:  문열기 버튼 함수
    private int DoorOpen() {
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


    @Override
    protected void onResume() {
        super.onResume();
        if (null != mCameraP2P) {
            AudioUtils.getModel(this);
            mCameraP2P.registorOnP2PCameraListener(p2pCameraListener);
            cameraOn();
            ProgressUtil.showLoading(this, "카메라 로딩중...");
            System.out.println("Connetcion : " + mCameraP2P.isConnecting());
            if (mCameraP2P.isConnecting()) {
                mCameraP2P.startPreview(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
                        isPlay = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        Log.d(TAG, "start preview onFailure, errCode: " + errCode);
                    }
                });
            }
            if (!mCameraP2P.isConnecting()) {
                mCameraP2P.connect(devId, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int i, int i1, String s) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
                        //전화받고 말하기
                        int mute;
                        mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
                        mCameraP2P.setMute(ICameraP2P.PLAYMODE.LIVE, mute, new OperationDelegateCallBack() {
                            @Override
                            public void onSuccess(int sessionId, int requestId, String data) {
                                previewMute = Integer.valueOf(data);
                                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
                                mCameraP2P.startAudioTalk(new OperationDelegateCallBack() {
                                    @Override
                                    public void onSuccess(int sessionId, int requestId, String data) {
                                        isSpeaking = true;
                                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_SUCCESS));
                                    }

                                    @Override
                                    public void onFailure(int sessionId, int requestId, int errCode) {
                                        isSpeaking = false;
                                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_FAIL));
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int sessionId, int requestId, int errCode) {
                                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
                            }
                        });
                    }

                    @Override
                    public void onFailure(int i, int i1, int i2) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_FAIL, i2));
                    }
                });
            }
        }
//        }
    }

    private AbsP2pCameraListener p2pCameraListener = new AbsP2pCameraListener() {
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
//        if (scrOffReceiver != null)
//            unregisterReceiver(scrOffReceiver);

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

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void doSaveSuccess() {

    }

    @Override
    public void doSaveFailed() {

    }
}
