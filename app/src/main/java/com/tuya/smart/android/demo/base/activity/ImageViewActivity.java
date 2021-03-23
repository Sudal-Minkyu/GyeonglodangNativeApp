package com.tuya.smart.android.demo.base.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.tuya.smart.android.demo.R;

// 3월 19일 김민규가만든것. 갤러리 이미지 선택후 크게보는 페이지
public class ImageViewActivity extends BaseActivity  {

    private final int GET_GALLERY_IMAGE = 200;
    private ImageView imageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        initToolbar();
        imageview = findViewById(R.id.image_View);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(Intent.createChooser(intent, "Get Album"), GET_GALLERY_IMAGE);

        imageview.setOnClickListener(v -> startActivityForResult(Intent.createChooser(intent, "Get Album"), GET_GALLERY_IMAGE));
    }

    protected void initToolbar() {
        if (mToolBar == null) {
            mToolBar = findViewById(R.id.toolbar_view);
            mToolBar.setNavigationIcon(R.drawable.tysmart_back_white);
            TypedArray a = obtainStyledAttributes(new int[]{
                    R.attr.status_font_color});
            int titleColor = a.getInt(0, Color.WHITE);
            mToolBar.setTitle("사진을 클릭하면 갤러리로 갑니다.");
            mToolBar.setTitleTextColor(titleColor);
            setSupportActionBar(mToolBar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            mToolBar.setNavigationOnClickListener(v -> finish());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            imageview.setImageURI(selectedImageUri);
        }
    }

}
