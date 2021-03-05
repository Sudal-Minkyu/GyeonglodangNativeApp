package com.tuya.smart.android.demo.camera;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.utils.MessageUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.camera.adapter.AlarmBasicAdapter;
import com.tuya.smart.android.demo.camera.adapter.AlarmDetectionAdapter;
import com.tuya.smart.android.demo.camera.bean.AlarmMessage;
import com.tuya.smart.android.demo.utils.DateUtils;
import com.tuya.smart.android.demo.utils.TimeZoneUtils;
import com.tuya.smart.android.network.Business;
import com.tuya.smart.android.network.http.BusinessResponse;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageBean;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageClassifyBean;
import com.tuya.smart.ipc.messagecenter.business.CameraMessageBusiness;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.tuya.smart.android.demo.device.common.CommonDeviceDebugPresenter.INTENT_DEVID;
import static com.tuya.smart.android.demo.utils.Constants.ALARM_DETECTION_DATE_MONTH_FAILED;
import static com.tuya.smart.android.demo.utils.Constants.ALARM_DETECTION_DATE_MONTH_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.MOTION_CLASSIFY_FAILED;
import static com.tuya.smart.android.demo.utils.Constants.MOTION_CLASSIFY_SUCCESS;
import static com.tuya.smart.android.demo.utils.Constants.MSG_DELETE_ALARM_DETECTION;
import static com.tuya.smart.android.demo.utils.Constants.MSG_GET_ALARM_DETECTION;

/**
 * huangdaju
 * 2019-11-19
 **/

public class AlarmDetectionActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    private static final String TAG = AlarmDetectionActivity.class.getSimpleName();
    private String devId;
    private List<CameraMessageBean> mWaitingDeleteCameraMessageList;
    protected List<CameraMessageBean> mCameraMessageList;
    protected List<AlarmMessage> mBasicMessageList;
    private CameraMessageBusiness messageBusiness;
    private CameraMessageClassifyBean selectClassify;
    private TextView dateStartTxt, dateEndTxt;
    private RecyclerView queryRv, basicRv;
    private Button queryBtn;

    private RadioGroup mainRadioGroup, bellRadioGroup;
    private RadioButton monthRadioBtn, directInputRadioBtn;
    private LinearLayout dateLayout;

    private AlarmDetectionAdapter adapter;
    private AlarmBasicAdapter basicAdapter;
    private int startDay, startYear, startMonth;
    private int endDay, endYear, endMonth;
    protected Toolbar mToolBar;
    private int offset = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALARM_DETECTION_DATE_MONTH_FAILED:
                    handlAlarmDetectionDateFail(msg);
                    break;
                case ALARM_DETECTION_DATE_MONTH_SUCCESS:
                    handlAlarmDetectionDateSuccess(msg);
                    break;
                case MSG_GET_ALARM_DETECTION:
                    handleAlarmDetection();
                    break;
                case MSG_DELETE_ALARM_DETECTION:
                    handleDeleteAlarmDetection();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    protected void initToolbar() {
        if (mToolBar == null) {
            mToolBar = (Toolbar) findViewById(R.id.toolbar_view);
            mToolBar.setNavigationIcon(R.drawable.tysmart_back_white);
            TypedArray a = obtainStyledAttributes(new int[]{
                    R.attr.status_font_color});
            int titleColor = a.getInt(0, Color.WHITE);
            mToolBar.setTitle("장방경로당");
            mToolBar.setTitleTextColor(titleColor);
            setSupportActionBar(mToolBar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

    }


    private void handleDeleteAlarmDetection() {
        Log.d(TAG, "handleDeleteAlarmDetection size " + mCameraMessageList.size());

        mCameraMessageList.removeAll(mWaitingDeleteCameraMessageList);
        adapter.updateAlarmDetectionMessage(mCameraMessageList);
        adapter.notifyDataSetChanged();
    }

    private void handleAlarmDetection() {
        Log.d(TAG, "handleAlarmDetection size " + mCameraMessageList.size());
        if (mCameraMessageList.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AlarmDetectionActivity.this, "해당 날짜에 저장된 기록이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        } else {
            adapter.updateAlarmDetectionMessage(mCameraMessageList);
            adapter.notifyDataSetChanged();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AlarmDetectionActivity.this, "조회가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handlAlarmDetectionDateFail(Message msg) {

    }

    private void handlAlarmDetectionDateSuccess(Message msg) {
        if (null != messageBusiness) {
            long startTime = DateUtils.getTodayStart(DateUtils.getCurrentTime(startYear, startMonth, startDay));
            long endTime = DateUtils.getTodayEnd(DateUtils.getCurrentTime(endYear, endMonth, endDay)) - 1L;
            offset = 0;
            JSONObject object = new JSONObject();
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
            messageBusiness.getAlarmDetectionMessageList(object.toJSONString(), new Business.ResultListener<JSONObject>() {
                @Override
                public void onFailure(BusinessResponse businessResponse, JSONObject jsonObject, String s) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                }

                @Override
                public void onSuccess(BusinessResponse businessResponse, JSONObject jsonObject, String s) {
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_message);
        devId = getIntent().getStringExtra(INTENT_DEVID);
        initView();
        initData();
        initListener();
        initToolbar();
    }

    /**
     * 화재 , 경고 fcm 알림 메시지 로컬 디비 데이터 로드
     */
    private void LoadAlarmListView() {
        queryRv.setVisibility(View.GONE);
        basicRv.setVisibility(View.VISIBLE);

        if (mBasicMessageList == null)
            mBasicMessageList = new ArrayList<>();
        else {
            mBasicMessageList.clear();
        }

        try {
            for (Iterator<AlarmMessage> it = AlarmMessage.findAll(AlarmMessage.class); it.hasNext(); ) {
                AlarmMessage message = it.next();
                mBasicMessageList.add(message);
                if (message != null) {
                    Log.d(TAG, "message title : " + message.getTitle());
                    Log.d(TAG, "message text : " + message.getText());
                    Log.d(TAG, "message date : " + message.getDate());
                }
            }
        } catch (java.lang.IllegalArgumentException error) {
            Log.e(TAG, "error : " + error.getMessage());
        }


        if (mBasicMessageList.size() == 0) {
            ToastUtil.shortToast(getBaseContext(), "저장된 알림이 존재하지 않습니다.");
        }


        basicAdapter.notifyDataSetChanged();
    }

    /**
     * 로드 타입
     * MONTH 월단위
     * DIRECT_INPUT 직접입력
     */
    private enum LOAD_BELL_TYPE {
        MONTH,
        DIRECT_INPUT;
    }

    private void LoadBellListView(LOAD_BELL_TYPE type) {


        // 월단위
        if (type == LOAD_BELL_TYPE.MONTH) {

            queryAlarmMonth();
            queryRv.setVisibility(View.GONE);
            basicRv.setVisibility(View.VISIBLE);
            // 직접입력
        } else if (type == LOAD_BELL_TYPE.DIRECT_INPUT) {
            queryAlarmDirectInput();
            basicRv.setVisibility(View.GONE);
            queryRv.setVisibility(View.VISIBLE);
        }

    }

    private int checkedResId = R.id.rg_directInput; // default setting

    //라디오 그룹 클릭 리스너
    RadioGroup.OnCheckedChangeListener mainRadioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if (i == R.id.rg_bell) {
                dateLayout.setVisibility(View.VISIBLE);
                bellRadioGroup.setVisibility(View.VISIBLE);
                if (checkedResId != R.id.rg_month) {
                    directInputRadioBtn.setChecked(true);
                    LoadBellListView(LOAD_BELL_TYPE.DIRECT_INPUT);
                } else {
                    monthRadioBtn.setChecked(true);
                    LoadBellListView(LOAD_BELL_TYPE.MONTH);
                }
            } else if (i == R.id.rg_alarm) {
                dateLayout.setVisibility(View.GONE);
                bellRadioGroup.setVisibility(View.GONE);
                LoadAlarmListView();
            }
        }
    };

    RadioGroup.OnCheckedChangeListener bellRadioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if (i == R.id.rg_month) {
                dateLayout.setVisibility(View.GONE);
                checkedResId = i;
                queryAlarmMonth();
            } else if (i == R.id.rg_directInput) {
                dateLayout.setVisibility(View.VISIBLE);
                checkedResId = i;
                queryAlarmDirectInput();
            }
        }
    };

    private void initListener() {
        queryBtn.setOnClickListener(this);
    }

    private void initView() {
        dateStartTxt = findViewById(R.id.date_start_txt);
        dateStartTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AlarmDetectionActivity.this,
                        now.get(Calendar.YEAR),
                        startMonth == 0 ? now.get(Calendar.MONTH) : startMonth - 1,
                        startDay == 0 ? now.get(Calendar.DAY_OF_MONTH) : startDay
                );
                dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                dpd.setThemeDark(true);
                dpd.show(getFragmentManager(), "Datepickerdialog1");
            }
        });

        dateEndTxt = findViewById(R.id.date_end_txt);
        dateEndTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AlarmDetectionActivity.this,
                        now.get(Calendar.YEAR),
                        endMonth == 0 ? now.get(Calendar.MONTH) : endMonth - 1,
                        endDay == 0 ? now.get(Calendar.DAY_OF_MONTH) : endDay
                );
                dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                dpd.setThemeDark(true);
                dpd.show(getFragmentManager(), "Datepickerdialog2");
            }
        });
        queryBtn = findViewById(R.id.query_btn);
        queryRv = findViewById(R.id.query_list);
        basicRv = findViewById(R.id.basic_list);

        mainRadioGroup = findViewById(R.id.mainRadioGroup);
        mainRadioGroup.setOnCheckedChangeListener(mainRadioGroupButtonChangeListener);

        bellRadioGroup = findViewById(R.id.bellRadioGroup);
        bellRadioGroup.setOnCheckedChangeListener(bellRadioGroupButtonChangeListener);

        monthRadioBtn = findViewById(R.id.rg_month);
        directInputRadioBtn = findViewById(R.id.rg_directInput);

        dateLayout = findViewById(R.id.date_ll);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        dateStartTxt.setText(simpleDateFormat.format(date));
        dateEndTxt.setText(simpleDateFormat.format(date));


    }

    private void initData() {
        mWaitingDeleteCameraMessageList = new ArrayList<>();
        mCameraMessageList = new ArrayList<>();
        messageBusiness = new CameraMessageBusiness();
        queryCameraMessageClassify(devId);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        queryRv.setLayoutManager(mLayoutManager);
        queryRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new AlarmDetectionAdapter(this, mCameraMessageList);
        adapter.setListener(new AlarmDetectionAdapter.OnItemListener() {
            @Override
            public void onLongClick(CameraMessageBean o) {
                deleteCameraMessageClassify(o);
            }

            @Override
            public void onItemClick(CameraMessageBean o) {
                if (o.getAttachVideos() != null && o.getAttachVideos().length > 0) {
                    Intent intent = new Intent(AlarmDetectionActivity.this, CameraCloudVideoActivity.class);
                    String attachVideo = o.getAttachVideos()[0];
                    String playUrl = attachVideo.substring(0, attachVideo.lastIndexOf('@'));
                    String encryptKey = attachVideo.substring(attachVideo.lastIndexOf('@') + 1);
                    intent.putExtra("playUrl", playUrl);
                    intent.putExtra("encryptKey", encryptKey);
                    startActivity(intent);
                }
            }
        });


        queryRv.setAdapter(adapter);

        mBasicMessageList = new ArrayList<>();
        try {
//            mBasicMessageList = Select.from(AlarmMessage.class).fetch();
            for (Iterator<AlarmMessage> it = AlarmMessage.findAll(AlarmMessage.class); it.hasNext(); ) {
                AlarmMessage message = it.next();
                mBasicMessageList.add(message);
            }
        } catch (java.lang.IllegalArgumentException error) {
            Log.e(TAG, "error : " + error.getMessage());
        }
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        basicRv.setLayoutManager(mLayoutManager);
        basicRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        basicAdapter = new AlarmBasicAdapter(this, mBasicMessageList);

        basicRv.setAdapter(basicAdapter);


        basicRv.setVisibility(View.GONE);
        queryAlarmDirectInput();

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

    public void deleteCameraMessageClassify(CameraMessageBean cameraMessageBean) {
        mWaitingDeleteCameraMessageList.add(cameraMessageBean);

        if (messageBusiness != null) {
            messageBusiness.deleteAlarmDetectionMessageList(cameraMessageBean.getId(), new Business.ResultListener<Boolean>() {
                @Override
                public void onFailure(BusinessResponse businessResponse, Boolean aBoolean, String s) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_DELETE_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                }

                @Override
                public void onSuccess(BusinessResponse businessResponse, Boolean aBoolean, String s) {
                    mCameraMessageList.removeAll(mWaitingDeleteCameraMessageList);
                    mWaitingDeleteCameraMessageList.clear();
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_DELETE_ALARM_DETECTION, ARG1_OPERATE_SUCCESS));
                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.query_btn:
                queryAlarmDirectInput();
                break;
            default:
                break;
        }
    }


    private void queryAlarmMonth() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        String currentDate = simpleDateFormat.format(date);

        String inputStr = currentDate;
        if (TextUtils.isEmpty(inputStr)) {
            ToastUtil.shortToast(this, "input query date");
            return;
        }
        String[] substring = inputStr.split("/");
        startYear = Integer.parseInt(substring[0]);
        startMonth = Integer.parseInt(substring[1]);
        startDay = Integer.parseInt("1");

        Log.d(TAG, "query startYear = "+startYear);
        Log.d(TAG, "query startMonth = "+startMonth);
        Log.d(TAG, "query startDay = "+startDay);

        endYear = Integer.parseInt(substring[0]);
        endMonth = Integer.parseInt(substring[1]);
        endDay = Integer.parseInt(substring[2]);
        JSONObject object = new JSONObject();
        object.put("msgSrcId", devId);
        object.put("timeZone", TimeZoneUtils.getTimezoneGCMById(TimeZone.getDefault().getID()));

        String monthDate = endYear + "-" + endMonth;
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

    private void queryAlarmDirectInput() {
        String inputStr = dateStartTxt.getText().toString();
        if (TextUtils.isEmpty(inputStr)) {
            ToastUtil.shortToast(this, "input query date");
            return;
        }
        String[] substring = inputStr.split("/");
        startYear = Integer.parseInt(substring[0]);
        startMonth = Integer.parseInt(substring[1]);
        startDay = Integer.parseInt(substring[2]);


        Log.d(TAG, "query startYear = "+startYear);
        Log.d(TAG, "query startMonth = "+startMonth);
        Log.d(TAG, "query startDay = "+startDay);

        inputStr = dateEndTxt.getText().toString();
        if (TextUtils.isEmpty(inputStr)) {
            ToastUtil.shortToast(this, "input query date");
            return;
        }
        substring = inputStr.split("/");
        endYear = Integer.parseInt(substring[0]);
        endMonth = Integer.parseInt(substring[1]);
        endDay = Integer.parseInt(substring[2]);
        JSONObject object = new JSONObject();
        object.put("msgSrcId", devId);
        object.put("timeZone", TimeZoneUtils.getTimezoneGCMById(TimeZone.getDefault().getID()));

        String monthDate = endYear + "-" + endMonth;
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
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (null != messageBusiness) {
            messageBusiness.onDestroy();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        if (view.getTag().equals("Datepickerdialog1")) {
            if (dayOfMonth > endDay) {
                ToastUtil.shortToast(getApplicationContext(), "시작날짜를 최종날짜보다 낮게 지정 해주세요");
                return;
            }

            if (monthOfYear + 1 != endMonth) {
                ToastUtil.shortToast(getApplicationContext(), "동일한 월구간으로 조회가 가능합니다.");
                return;
            }
            dateStartTxt.setText(GetDateStringFormat(year, monthOfYear, dayOfMonth));
        } else {
            LoadBellListView(LOAD_BELL_TYPE.DIRECT_INPUT);
            dateEndTxt.setText(GetDateStringFormat(year, monthOfYear, dayOfMonth));

        }


    }

    public String GetDateStringFormat(int year, int monthOfYear, int dayOfMonth) {
        return String.format("%d/" + GetMonthStringFormat(monthOfYear) + GetDayStringFormat(dayOfMonth), year, monthOfYear + 1, dayOfMonth);
    }

    public String GetMonthStringFormat(int monthOfYear) {
        return monthOfYear + 1 > 9 ? "%d/" : "0%d/";
    }

    public String GetDayStringFormat(int dayOfMonth) {
        return dayOfMonth > 9 ? "%d" : "0%d";
    }
}