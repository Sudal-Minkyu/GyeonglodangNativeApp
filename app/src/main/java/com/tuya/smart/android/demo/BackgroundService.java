package com.tuya.smart.android.demo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tuya.smart.android.base.utils.PreferencesUtil;
import com.tuya.smart.android.camera.api.bean.CameraPushDataBean;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.utils.CollectionUtils;
import com.tuya.smart.android.demo.base.utils.LoginHelper;
import com.tuya.smart.android.demo.camera.CameraPanelActivity;
import com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter;
import com.tuya.smart.android.demo.family.activity.IFamilyAddView;
import com.tuya.smart.android.demo.family.presenter.FamilyAddPresenter;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.ITuyaGetBeanCallback;
import com.tuyasmart.camera.devicecontrol.ITuyaCameraDevice;
import com.tuyasmart.camera.devicecontrol.TuyaCameraDeviceControlSDK;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import static com.tuya.smart.camera.ipccamerasdk.utils.MqttServiceUtils.homeCamera;


/**
 * dev.shinyu
 * <p>
 * Tuya 알림 기능 동작 감지 백그라운드 서비스
 */
public class BackgroundService extends Service implements IFamilyAddView, ITuyaGetHomeListCallback, ITuyaHomeResultCallback, ITuyaGetBeanCallback<CameraPushDataBean>, OnCompleteListener<String> {

    private final static String TAG = BackgroundService.class.getSimpleName();

    public int counter = 0;

    // 생성자 1 : 반듯이 필요
    public BackgroundService() {
    }

    // 생성자2
    public BackgroundService(Context applicationContext) {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private FamilyAddPresenter mPresenter;

    private void initPresenter() {
        mPresenter = new FamilyAddPresenter(this);
    }

    private void checkLogin() {
        if (needLogin() && !TuyaHomeSdk.getUserInstance().isLogin()) {
            LoginHelper.reLogin(this);
        }
    }


    public boolean needLogin() {
        return true;
    }

    private static boolean is_service_running = false;
    private Context mContext;
    private PowerManager.WakeLock mWakeLock;
    private static final String STARTFOREGROUND_ACTION = "STARTFOREGROUND_ACTION";
    private static final String STOPFOREGROUND_ACTION = "STOPFOREGROUND_ACTION";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.e(TAG, this.getClass().getSimpleName() + ".onCreate");

        mContext = getApplicationContext();
        checkLogin();
        initPresenter();

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("isLogin", TuyaHomeSdk.getUserInstance().isLogin() + "");
                if (TuyaHomeSdk.getUserInstance().isLogin()) {
                    Log.e("homeCamera", homeCamera + "");

//                    if (homeCamera != null) {
//                        homeCamera.unRegisterCameraPushListener(BackgroundService.this::onResult);
//                    }
//                    homeCamera = null;
//                    homeCamera = TuyaHomeSdk.getCameraInstance();
//                    if (homeCamera != null) {
//                        homeCamera.registerCameraPushListener(BackgroundService.this::onResult);
//                    }
                    Log.e(TAG, "CIS 백그라운드서비스 실행시 로그인 완료되면 수행");
                    LoginHelper.afterLogin();
                    deviceLoad();
                }
            }
        }, 1000); //3초 뒤에 Runner객체 실행하도록 함
        //startTimer(); /테스트용이라서 삭제함
        reciever();
    }


    private void reciever() {
        ScreenReceiver screenOnReceiver = new ScreenReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        registerReceiver(screenOnReceiver, filter);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        super.onStartCommand(intent, flags, startId);
        // 서비스가 호출될 때마다 실행

        if (intent == null) return START_STICKY;

        if (!is_service_running && STARTFOREGROUND_ACTION.equals(intent.getAction())) {
            Log.i(TAG, "Received Start Foreground Intent ");
            showNotification();
            is_service_running = true;
            acquireWakeLock();

        } else if (is_service_running && STOPFOREGROUND_ACTION.equals(intent.getAction())) {
            Log.i(TAG, "Received Stop Foreground Intent");
            is_service_running = false;
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    // 사용자가 강제로 서비스를 종료하더라도 자동으로 서비스를 다시 시작하는 방법은 무엇입니까?
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved");
        //create an intent that you want to start again.
        Intent intent = new Intent(getApplicationContext(), BackgroundService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    private void showNotification() {
        Log.d(TAG, "showNotification");

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    public void releaseWakeLock() {
        Log.d(TAG, "releaseWakeLock");

        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    public void acquireWakeLock() {
        Log.d(TAG, "acquireWakeLock");

        final PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        releaseWakeLock();
        //Acquire new wake lock
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + "PARTIAL_WAKE_LOCK");
        mWakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        releaseWakeLock();
        super.onDestroy();
        // 서비스가 종료될 때 실행
        Intent broadcastIntent = new Intent("kr.co.gyeonglodang.jbgyeonglodang.RestartService");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i(TAG, "in timer ++++  " + (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private int count;

    @Override
    public void onComplete(@NonNull Task<String> task) {
        // CIS - 이함수는 사용하지 않는다.
        Log.d(TAG, "onComplete");

        // Get new FCM registration token
        String token = task.getResult();

        Log.e("Firebase Token : ", token);
        Log.d(TAG, "backgorund count : " + ++count);

        try {
            JSONObject data = new JSONObject();
            JSONObject auth = new JSONObject();

            data.put("title", "초인종이 울렸습니다~");
            data.put("message", "확인해주세요~");

            auth.put("to", token);
            auth.put("priority", "high");
            auth.put("direct_book_ok", true);
            auth.put("data", data);

            //new LoginHelper.HttpUtil().execute(auth.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deviceLoad() {
        TuyaHomeSdk.getHomeManagerInstance().queryHomeList(this);
    }

    @Override
    public void onSuccess(List<HomeBean> homeBeans) {
        if (CollectionUtils.isEmpty(homeBeans)) {
            List<String> checkRoomList = new ArrayList<>();
            String name = "경로당";
            TuyaHomeSdk.getHomeManagerInstance().createHome(name, 1, 2, name, checkRoomList, new ITuyaHomeResultCallback() {
                @Override
                public void onError(String errorCode, String errorMsg) {

                }

                @Override
                public void onSuccess(HomeBean bean) {
                    deviceLoad();
                }
            });
        } else {
            final long homeId = homeBeans.get(0).getHomeId();
            Constant.HOME_ID = homeId;
            PreferencesUtil.set("homeId", Constant.HOME_ID);
            TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                @Override
                public void onSuccess(HomeBean bean) {

                }

                @Override
                public void onError(String errorCode, String errorMsg) {

                }
            });
        }
    }

    @Override
    public void onSuccess(HomeBean bean) {
        Intent intent = new Intent(getApplicationContext(), CameraPanelActivity.class);
        if (bean.getDeviceList().size() == 0) {
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, "devId");
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY, "localId");
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, "p2pId");
        } else {
            ITuyaCameraDevice mDeviceControl;
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_DEVID, bean.getDeviceList().get(0).getDevId());
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_LOCALKEY, bean.getDeviceList().get(0).getLocalKey());
            Map<String, Object> map = bean.getDeviceList().get(0).getSkills();
            int p2pType = -1;
            if (map == null || map.size() == 0) {
                p2pType = -1;
            } else {
                p2pType = (Integer) (map.get("p2pType"));
            }
            intent.putExtra(CommonDeviceDebugPresenter.INTENT_P2P_TYPE, p2pType);
            mDeviceControl = TuyaCameraDeviceControlSDK.getCameraDeviceInstance(bean.getDeviceList().get(0).getDevId());
            mDeviceControl.wirelessWake(bean.getDeviceList().get(0).getLocalKey(), bean.getDeviceList().get(0).getDevId());
        }
        startActivity(intent);
    }

    @Override
    public void onError(String errorCode, String error) {
        TuyaHomeSdk.newHomeInstance(Constant.HOME_ID).getHomeLocalCache(this);
    }

    @Override
    public void onResult(CameraPushDataBean bean) {
        // 새 버전 fcm 전송
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(this);
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void doSaveSuccess() {
        Log.e(TAG, getString(R.string.save_success));
        deviceLoad();
    }

    @Override
    public void doSaveFailed() {
        Log.e(TAG, getString(R.string.save_failed));
        deviceLoad();
    }

}