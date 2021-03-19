package com.tuya.smart.android.demo.base.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {


    public static void translateView(View view, float dxFrom, float dxTo, float dyFrom, float dyTo, long duration,
                                     boolean fillafter, Animation.AnimationListener animListener) {
        TranslateAnimation anim = new TranslateAnimation(dxFrom, dxTo, dyFrom, dyTo);
        anim.setDuration(duration);
        anim.setFillAfter(fillafter);
        anim.setAnimationListener(animListener);
        view.startAnimation(anim);
    }

}