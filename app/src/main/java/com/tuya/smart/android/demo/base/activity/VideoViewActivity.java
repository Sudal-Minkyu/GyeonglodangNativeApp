package com.tuya.smart.android.demo.base.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.tuya.smart.android.demo.R;

import java.util.Objects;

// 3월 19일 김민규 - 갤러리 동영상 선택후 크게보는 페이지
public class VideoViewActivity extends BaseActivity  {

    private static final String TAG = "VideoViewActivity";

    private final int GET_GALLERY_VIDEO = 200;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        initToolbar();
        videoView = findViewById(R.id.video_View);
        RelativeLayout camera_video_btn = findViewById(R.id.camera_video_btn);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, GET_GALLERY_VIDEO);
        camera_video_btn.setOnClickListener(v -> startActivityForResult(intent, GET_GALLERY_VIDEO));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "KMK 동영상 requestCode : "+requestCode);
        Log.e(TAG, "KMK 동영상 resultCode : "+resultCode);
        Log.e(TAG, "KMK 동영상 data : "+data);
        Log.e(TAG, "KMK 동영상 data.getData() : "+data.getData());
        if (requestCode == GET_GALLERY_VIDEO && resultCode == RESULT_OK && data.getData() != null) {
            Log.e(TAG, "KMK 동영상 데이터받아오기 성공");
            videoView.setMediaController(new MediaController(this)); // 비디오 컨트롤 가능하게(일시정지, 재시작 등)
            Uri fileUri = data.getData();
//            videoView.setVideoPath(String.valueOf(fileUri));

            videoView.setVideoURI(fileUri);  // 선택한 비디오 경로 비디오뷰에 셋

//            videoView.start();  // 비디오뷰 시작
            videoView.setOnPreparedListener(mediaPlayer -> {
                //비디오 시작
                videoView.start();
            });
        }
    }

    protected void initToolbar() {
        if (mToolBar == null) {
            mToolBar = findViewById(R.id.toolbar_view);
            mToolBar.setNavigationIcon(R.drawable.tysmart_back_white);
            TypedArray a = obtainStyledAttributes(new int[]{
                    R.attr.status_font_color});
            int titleColor = a.getInt(0, Color.WHITE);
            mToolBar.setTitle("검은화면을 클릭하면 목록으로 갑니다.");
            mToolBar.setTitleTextColor(titleColor);
            setSupportActionBar(mToolBar);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolBar.setNavigationOnClickListener(v -> finish());
        }
    }

}
