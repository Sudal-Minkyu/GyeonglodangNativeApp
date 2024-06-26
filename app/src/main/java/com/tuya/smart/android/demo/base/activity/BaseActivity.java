package com.tuya.smart.android.demo.base.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.app.Constant;
import com.tuya.smart.android.demo.base.utils.ActivityUtils;
import com.tuya.smart.android.demo.base.utils.CommonUtil;
import com.tuya.smart.android.demo.base.utils.LoginHelper;
import com.tuya.smart.android.demo.base.utils.ProgressUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.utils.Constants;
import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

import java.util.Timer;
import java.util.TimerTask;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1123;

    protected Toolbar mToolBar;

    private boolean mIsPaused = true;

    protected View mPanelTopView;

    private long resumeUptime;

    private GestureDetector mGestureDetector;

    private boolean mNeedDefaultAni = true;

    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.SYSTEM_ALERT_WINDOW) != PackageManager.PERMISSION_GRANTED) {
            // 동영상, 오디오 권한
            if (!Constants.hasRecordPermission()) {
                // 동영상, 오디오 권한 요청
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SYSTEM_ALERT_WINDOW}, 1);
            }

            // 다른앱 위에 그리기 체크
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder setdialog = new AlertDialog.Builder(BaseActivity.this);
                setdialog.setTitle("권한이 필요합니다.")
                        .setMessage("슬립모드에서 알람을 확인하기 위해서는\"다른앱 위에 그리기\" 기능을 켜야 합니다. 설정화면으로 이동 하시겠습니까?")
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                                checkWhiteListRegist();
                            }
                        })
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(BaseActivity.this, "슬립모드에서 사용하려면 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create()
                        .show();
            }
        }
    }

    public void checkWhiteListRegist() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isWhiteListing = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getBaseContext().getPackageName());
        }
        if (!isWhiteListing) {
            AlertDialog.Builder setdialog = new AlertDialog.Builder(BaseActivity.this);
            setdialog.setTitle("권한이 필요합니다.")
                    .setMessage("슬립모드에서 알람을 확인하기 위해서는\"배터리 사용량 최적화\" 기능을 꺼야 합니다. 설정화면으로 이동 하시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + getBaseContext().getPackageName()));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(BaseActivity.this, "슬립모드에서 사용하려면 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create()
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } catch (IllegalStateException ignore) {
        }
        GestureDetector.OnGestureListener gestureListener = obtainGestureListener();
        if (gestureListener != null) {
            mGestureDetector = new GestureDetector(this, gestureListener);
        }
        Constant.attachActivity(this);
        checkLogin();
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectAll()
                .penaltyLog()
                .build());
    }

    private void checkLogin() {
        if (needLogin() && !TuyaHomeSdk.getUserInstance().isLogin()) {
            LoginHelper.reLogin(this);
        }
    }

    protected void initToolbar() {
        if (mToolBar == null) {
            mToolBar = findViewById(R.id.toolbar_top_view);
            if (mToolBar == null) {
            } else {
                TypedArray a = obtainStyledAttributes(new int[]{
                        R.attr.status_font_color});
                int titleColor = a.getInt(0, Color.WHITE);
                mToolBar.setTitleTextColor(titleColor);
            }
        }
    }

    protected void setTitle(String title) {
        if (mToolBar != null) {
            mToolBar.setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        if (mToolBar != null) {
            mToolBar.setTitle(titleId);
        }
    }

    protected void setMenu(int resId, Toolbar.OnMenuItemClickListener listener) {
        if (mToolBar != null) {
            mToolBar.inflateMenu(resId);
            mToolBar.setOnMenuItemClickListener(listener);
        }
    }

    protected void setDisplayHomeAsUpEnabled(int iconResId, final View.OnClickListener listener) {
        if (mToolBar != null) {
            mToolBar.setNavigationIcon(iconResId);
            if (listener != null) {
                mToolBar.setNavigationOnClickListener(listener);
            } else {
                mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
        }
    }

    // 뒤로가기 버튼 액티브
    protected void setDisplayHomeAsUpEnabled() {
        setDisplayHomeAsUpEnabled(R.drawable.tysmart_back_white, null);
    }

    protected void setDisplayHomeAsUpEnabled(final View.OnClickListener listener) {
        setDisplayHomeAsUpEnabled(R.drawable.tysmart_back_white, listener);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        if (mNeedDefaultAni) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        super.startActivityForResult(intent, requestCode, options);
        if (mNeedDefaultAni) {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    ToastUtil.shortToast(this, "백그라운드 알림기능은 앱위에 그리기 허용 권한이 필요합니다.");
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
        resumeUptime = SystemClock.uptimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    public void onBackPressed() {
        ActivityUtils.back(this);
        super.onBackPressed();
        if (mNeedDefaultAni) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    private static boolean isExit = false;

    protected void exitByClick(ITuyaSmartCameraP2P mCameraP2P) {
        Timer tExit;
        if (!isExit) {
            isExit = true;
            ToastUtil.shortToast(this, getString(R.string.action_tips_exit_hint));
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            if (null != mCameraP2P) {
                mCameraP2P.destroyP2P();
            }
            LoginHelper.exit2();
        }
    }

    protected void exitBy2Click() {
        Timer tExit;
        if (!isExit) {
            isExit = true;
            ToastUtil.shortToast(this, getString(R.string.action_tips_exit_hint));
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            LoginHelper.exit();
        }
    }

    protected GestureDetector.OnGestureListener obtainGestureListener() {
        return null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!this.isFinishing()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                long eventtime = event.getEventTime();
                if (Math.abs(eventtime - resumeUptime) < 400) {
                    L.d(TAG, "baseactivity onKeyDown after onResume to close, do none");
                    return true;
                }
            }

            if (!(event.getRepeatCount() > 0) && !onPanelKeyDown(keyCode, event)) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    ActivityUtils.back(this);
                    return true;
                } else {
                    return super.onKeyDown(keyCode, event);
                }
            } else {
                L.d(TAG, "baseactivity onKeyDown true");
                return true;
            }

        } else {
            return true;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SETTINGS) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    protected boolean onPanelKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SETTINGS) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPanelTopView = inflater.inflate(layoutResID, null);
        super.setContentView(mPanelTopView);
        initSystemBarColor();
    }

    @Override
    public void setContentView(View view) {
        mPanelTopView = view;
        super.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        mPanelTopView = view;
        super.setContentView(view, params);
    }

    public boolean needLogin() {
        return true;
    }

    public void initSystemBarColor() {
        CommonUtil.initSystemBarColor(this);
    }

    public void showToast(int resId) {
        ToastUtil.showToast(this, resId);
    }

    public void showToast(String tip) {
        ToastUtil.showToast(this, tip);
    }

    public void showLoading(int resId) {
        ProgressUtil.showLoading(this, resId);
    }

    public void showLoading() {
        ProgressUtil.showLoading(this, R.string.loading);
    }

    public void hideLoading() {
        ProgressUtil.hideLoading();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void finishActivity() {
        onBackPressed();
    }
}
